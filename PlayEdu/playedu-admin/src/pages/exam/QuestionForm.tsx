import {
  Alert,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { z } from "zod";
import { enumApi } from "../../api/enumApi";
import { examApi } from "../../api/examApi";
import type { EnumDictionary, EnumOption } from "../../types/api";
import type {
  QuestionCreateReq,
  QuestionResp,
  QuestionUpdateReq,
} from "../../types/exam";

const QUESTION_TYPE_ENUM_KEY = "questionType";
const QUESTION_DIFFICULTY_ENUM_KEY = "questionDifficulty";

const questionSchema = z.object({
  bankId: z.string().min(1, "题库ID不能为空"),
  type: z.number({ invalid_type_error: "题目类型不能为空" }),
  content: z.string().min(1, "题干不能为空"),
  options: z.string().optional(),
  answer: z.string().min(1, "答案不能为空"),
  analysis: z.string().optional(),
  difficulty: z.number().min(1, "难度不能为空"),
  knowledgePoint: z.string().optional(),
  score: z.number().min(1, "分值必须大于0"),
});

type QuestionFormValues = z.infer<typeof questionSchema>;

interface QuestionFormProps {
  visible: boolean;
  initialValue?: QuestionResp | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const parseOptionsText = (value: unknown) => {
  if (typeof value === "string") {
    return value;
  }
  if (value == null) {
    return "";
  }
  return JSON.stringify(value, null, 2);
};

const loadEnums = async () =>
  enumApi.getMany([QUESTION_TYPE_ENUM_KEY, QUESTION_DIFFICULTY_ENUM_KEY]);

const QuestionForm = ({
  visible,
  initialValue,
  onCancel,
  onSuccess,
}: QuestionFormProps) => {
  const [form] = Form.useForm<QuestionFormValues>();
  const [loading, setLoading] = useState(false);
  const [enumDictionary, setEnumDictionary] = useState<EnumDictionary>({});

  useEffect(() => {
    if (!visible) {
      return;
    }
    loadEnums().then(setEnumDictionary);
  }, [visible]);

  useEffect(() => {
    if (!visible) {
      return;
    }
    form.setFieldsValue({
      bankId: initialValue?.bankId || "",
      type: initialValue?.type ?? 1,
      content: initialValue?.content || "",
      options: parseOptionsText(initialValue?.options),
      answer: initialValue?.answer || "",
      analysis: initialValue?.analysis || "",
      difficulty: initialValue?.difficulty ?? 3,
      knowledgePoint: initialValue?.knowledgePoint || "",
      score: initialValue?.score ?? 5,
    });
  }, [form, initialValue, visible]);

  const typeOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_TYPE_ENUM_KEY] || [],
    [enumDictionary]
  );
  const difficultyOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_DIFFICULTY_ENUM_KEY] || [],
    [enumDictionary]
  );

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const parsed = questionSchema.parse(values);
      setLoading(true);

      const payload: QuestionCreateReq | QuestionUpdateReq = {
        bankId: parsed.bankId,
        type: parsed.type,
        content: parsed.content,
        options: parsed.options,
        answer: parsed.answer,
        analysis: parsed.analysis,
        difficulty: parsed.difficulty,
        knowledgePoint: parsed.knowledgePoint,
        score: parsed.score,
      };

      if (initialValue?.id) {
        await examApi.updateQuestion(initialValue.id, payload);
        message.success("题目更新成功");
      } else {
        await examApi.createQuestion(payload as QuestionCreateReq);
        message.success("题目创建成功");
      }

      onSuccess();
      form.resetFields();
    } catch (error) {
      if (error instanceof z.ZodError) {
        message.error(error.issues[0]?.message || "表单校验失败");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={initialValue?.id ? "编辑题目" : "新建题目"}
      open={visible}
      width={860}
      confirmLoading={loading}
      onOk={() => void handleSubmit()}
      onCancel={onCancel}
      destroyOnClose
    >
      {typeOptions.length === 0 || difficultyOptions.length === 0 ? (
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="后端 /api/v1/enums 暂未返回题型/难度枚举，当前回退为数字输入。"
        />
      ) : null}

      <Form<QuestionFormValues> form={form} layout="vertical">
        <Form.Item label="题库 ID" name="bankId" rules={[{ required: true }]}>
          <Input placeholder="例如：bank-java-basic" />
        </Form.Item>

        <Form.Item label="题目类型" name="type" rules={[{ required: true }]}>
          {typeOptions.length > 0 ? (
            <Select
              options={typeOptions}
              placeholder="请选择题目类型"
              allowClear
            />
          ) : (
            <InputNumber style={{ width: "100%" }} min={1} />
          )}
        </Form.Item>

        <Form.Item label="题干" name="content" rules={[{ required: true }]}>
          <Input.TextArea rows={4} placeholder="请输入题目题干" />
        </Form.Item>

        <Form.Item label="选项 JSON" name="options">
          <Input.TextArea
            rows={6}
            placeholder='例如：[{"label":"A","value":"Spring Boot"},{"label":"B","value":"React"}]'
          />
        </Form.Item>

        <Form.Item label="答案" name="answer" rules={[{ required: true }]}>
          <Input placeholder="例如：A 或 A,B" />
        </Form.Item>

        <Form.Item label="解析" name="analysis">
          <Input.TextArea rows={3} placeholder="请输入题目解析" />
        </Form.Item>

        <Form.Item label="难度" name="difficulty" rules={[{ required: true }]}>
          {difficultyOptions.length > 0 ? (
            <Select
              options={difficultyOptions}
              placeholder="请选择难度"
              allowClear
            />
          ) : (
            <InputNumber style={{ width: "100%" }} min={1} max={5} />
          )}
        </Form.Item>

        <Form.Item label="知识点" name="knowledgePoint">
          <Input placeholder="例如：Spring IOC" />
        </Form.Item>

        <Form.Item label="分值" name="score" rules={[{ required: true }]}>
          <InputNumber style={{ width: "100%" }} min={1} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default QuestionForm;
