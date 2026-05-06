import { Alert, Button, Card, DatePicker, Form, Input, Radio, Space, message } from "antd";
import type { Dayjs } from "dayjs";
import { useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { trainApi } from "../../api/trainApi";
import type { TrainProjectCreateReq } from "../../types/train";
import {
  TRAIN_ASSIGNEE_SCOPE_OPTIONS,
  TRAIN_PROJECT_TYPE_OPTIONS,
} from "./constants";

const trainProjectSchema = z.object({
  title: z.string().min(1, "项目标题不能为空").max(200, "项目标题不能超过200字符"),
  description: z.string().max(500, "项目描述不能超过500字符").optional(),
  type: z.number({ invalid_type_error: "项目类型不能为空" }),
  assigneeScope: z.number({ invalid_type_error: "指派范围不能为空" }),
  targetDeptIds: z.string().max(1000, "部门 ID 长度不能超过1000字符").optional(),
  dateRange: z.array(z.any()).length(2).optional(),
});

interface TrainProjectFormValues {
  title: string;
  description?: string;
  type: number;
  assigneeScope: number;
  targetDeptIds?: string;
  dateRange?: [Dayjs, Dayjs];
}

const formatDateTime = (value?: Dayjs) => value?.format("YYYY-MM-DDTHH:mm:ss");

const TrainProjectForm = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm<TrainProjectFormValues>();
  const [loading, setLoading] = useState(false);
  const assigneeScope = Form.useWatch("assigneeScope", form) || 1;
  const currentUserId = useSelector(
    (state: any) => state.loginUser.value.user?.id
  ) as number | undefined;

  const creatorId = useMemo(
    () => (currentUserId && currentUserId > 0 ? currentUserId : 1),
    [currentUserId]
  );

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const parsed = trainProjectSchema.parse(values);
      const payload: TrainProjectCreateReq = {
        title: parsed.title,
        description: parsed.description,
        type: parsed.type,
        assigneeScope: parsed.assigneeScope,
        targetDeptIds: parsed.targetDeptIds,
        startTime: formatDateTime(values.dateRange?.[0]),
        endTime: formatDateTime(values.dateRange?.[1]),
      };

      setLoading(true);
      const result = await trainApi.createProject(payload, creatorId);
      message.success("培训项目创建成功，请继续配置任务");
      navigate(`/train/projects/${result.data}/tasks`);
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
      title="新建培训项目"
      extra={
        <Space>
          <Button onClick={() => navigate("/train/projects")}>返回列表</Button>
          <Button type="primary" loading={loading} onClick={() => void handleSubmit()}>
            保存并配置任务
          </Button>
        </Space>
      }
    >
      {creatorId === 1 && currentUserId !== 1 ? (
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message="当前未拿到真实登录用户 ID，创建请求会回退使用 X-User-Id=1 进行本地联调。"
        />
      ) : null}

      <Form<TrainProjectFormValues>
        form={form}
        layout="vertical"
        initialValues={{
          title: "",
          description: "",
          type: 1,
          assigneeScope: 1,
          targetDeptIds: "",
        }}
      >
        <Form.Item label="项目标题" name="title" rules={[{ required: true }]}>
          <Input placeholder="请输入培训项目标题" />
        </Form.Item>

        <Form.Item label="项目描述" name="description">
          <Input.TextArea rows={4} placeholder="请输入培训项目描述" />
        </Form.Item>

        <Form.Item label="培训类型" name="type" rules={[{ required: true }]}>
          <Radio.Group options={TRAIN_PROJECT_TYPE_OPTIONS} optionType="button" />
        </Form.Item>

        <Form.Item label="起止时间" name="dateRange">
          <DatePicker.RangePicker
            style={{ width: "100%" }}
            showTime
            format="YYYY-MM-DD HH:mm"
          />
        </Form.Item>

        <Form.Item label="指派范围" name="assigneeScope" rules={[{ required: true }]}>
          <Radio.Group options={TRAIN_ASSIGNEE_SCOPE_OPTIONS} />
        </Form.Item>

        {assigneeScope === 2 ? (
          <Form.Item label="目标部门 ID" name="targetDeptIds">
            <Input placeholder="先输入部门 ID 字符串，后续再接部门树接口" />
          </Form.Item>
        ) : null}

        {assigneeScope === 3 ? (
          <Alert
            type="warning"
            showIcon
            message="指定人员选择器依赖 user-svc 的选人能力，本轮先保留后端预留字段，不在前端展开。"
          />
        ) : null}
      </Form>
    </Card>
  );
};

export default TrainProjectForm;
