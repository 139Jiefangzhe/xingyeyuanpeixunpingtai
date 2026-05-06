import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Switch,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { z } from "zod";
import { enumApi } from "../../api/enumApi";
import { examApi } from "../../api/examApi";
import type { EnumDictionary, EnumOption } from "../../types/api";
import type {
  ExamPaperCreateReq,
  ExamPaperDetailResp,
  ExamPaperUpdateReq,
} from "../../types/exam";

const EXAM_PAPER_TYPE_ENUM_KEY = "examPaperType";

const examPaperSchema = z.object({
  title: z.string().min(1, "试卷标题不能为空").max(200, "试卷标题不能超过200字符"),
  description: z.string().max(500, "试卷描述不能超过500字符").optional(),
  duration: z.number().min(1, "考试时长必须大于0"),
  passScore: z.number().min(0, "及格线不能小于0"),
  totalScore: z.number().min(0, "总分不能小于0"),
  type: z.number({ invalid_type_error: "试卷类型不能为空" }),
  allowRedo: z.boolean(),
  knowledgeConfig: z.string().optional(),
});

type ExamPaperFormValues = z.infer<typeof examPaperSchema>;

const ExamPaperForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const [form] = Form.useForm<ExamPaperFormValues>();
  const [loading, setLoading] = useState(false);
  const [enumDictionary, setEnumDictionary] = useState<EnumDictionary>({});
  const [paperDetail, setPaperDetail] = useState<ExamPaperDetailResp | null>(
    null
  );

  useEffect(() => {
    enumApi.getMany([EXAM_PAPER_TYPE_ENUM_KEY]).then(setEnumDictionary);
  }, []);

  useEffect(() => {
    if (!id) {
      form.setFieldsValue({
        title: "",
        description: "",
        duration: 60,
        passScore: 60,
        totalScore: 100,
        type: 1,
        allowRedo: false,
        knowledgeConfig: "",
      });
      return;
    }

    setLoading(true);
    examApi
      .getPaperById(id)
      .then((result) => {
        const detail = result.data;
        setPaperDetail(detail);
        form.setFieldsValue({
          title: detail.title,
          description: detail.description,
          duration: detail.duration,
          passScore: detail.passScore,
          totalScore: detail.totalScore,
          type: detail.type,
          allowRedo: detail.allowRedo === 1,
          knowledgeConfig: detail.knowledgeConfig || "",
        });
      })
      .finally(() => {
        setLoading(false);
      });
  }, [form, id]);

  const typeOptions = useMemo<EnumOption[]>(
    () => enumDictionary[EXAM_PAPER_TYPE_ENUM_KEY] || [],
    [enumDictionary]
  );

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const parsed = examPaperSchema.parse(values);
      setLoading(true);

      const payload: ExamPaperCreateReq | ExamPaperUpdateReq = {
        title: parsed.title,
        description: parsed.description,
        duration: parsed.duration,
        passScore: parsed.passScore,
        totalScore: parsed.totalScore,
        type: parsed.type,
        allowRedo: parsed.allowRedo ? 1 : 0,
        knowledgeConfig: parsed.knowledgeConfig,
      };

      if (isEdit && id) {
        await examApi.updatePaper(id, payload as ExamPaperUpdateReq);
        message.success("试卷更新成功");
      } else {
        await examApi.createPaper(payload as ExamPaperCreateReq);
        message.success("试卷创建成功");
      }
      navigate("/exam/papers");
    } catch (error) {
      if (error instanceof z.ZodError) {
        message.error(error.issues[0]?.message || "表单校验失败");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card
      title={isEdit ? "编辑试卷" : "新建试卷"}
      extra={
        <Space>
          <Button onClick={() => navigate("/exam/papers")}>返回列表</Button>
          <Button type="primary" loading={loading} onClick={() => void handleSubmit()}>
            保存
          </Button>
        </Space>
      }
      loading={loading}
    >
      {typeOptions.length === 0 ? (
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="后端 /api/v1/enums 暂未返回试卷类型枚举，当前回退为数字输入。"
        />
      ) : null}

      {isEdit && paperDetail?.questions?.length === 0 ? (
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message="当前试卷尚未关联题目，直接发布会被后端校验拦截。"
        />
      ) : null}

      <Form<ExamPaperFormValues> form={form} layout="vertical">
        <Form.Item label="试卷标题" name="title" rules={[{ required: true }]}>
          <Input placeholder="请输入试卷标题" />
        </Form.Item>

        <Form.Item label="试卷描述" name="description">
          <Input.TextArea rows={3} placeholder="请输入试卷描述" />
        </Form.Item>

        <Form.Item label="考试时长（分钟）" name="duration" rules={[{ required: true }]}>
          <InputNumber style={{ width: "100%" }} min={1} />
        </Form.Item>

        <Form.Item label="及格线" name="passScore" rules={[{ required: true }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item label="总分" name="totalScore" rules={[{ required: true }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item label="试卷类型" name="type" rules={[{ required: true }]}>
          {typeOptions.length > 0 ? (
            <Select options={typeOptions} placeholder="请选择试卷类型" allowClear />
          ) : (
            <InputNumber style={{ width: "100%" }} min={1} />
          )}
        </Form.Item>

        <Form.Item label="允许重考" name="allowRedo" valuePropName="checked">
          <Switch checkedChildren="允许" unCheckedChildren="不允许" />
        </Form.Item>

        <Form.Item label="组卷规则 JSON" name="knowledgeConfig">
          <Input.TextArea
            rows={8}
            placeholder='例如：{"questionTypeRules":{"1":10},"difficultyDistribution":{"1":5,"2":5}}'
          />
        </Form.Item>
      </Form>
    </Card>
  );
};

export default ExamPaperForm;
