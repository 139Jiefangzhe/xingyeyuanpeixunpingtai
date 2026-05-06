import { Alert, Button, Card, DatePicker, Form, Input, Select, Space, message } from "antd";
import type { Dayjs } from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { courseApi } from "../../api/courseApi";
import { liveApi } from "../../api/liveApi";
import type { CourseSimpleResp } from "../../types/course";
import type { LiveRoomCreateReq } from "../../types/live";

const liveRoomSchema = z.object({
  title: z.string().min(1, "直播间标题不能为空").max(200, "直播间标题不能超过200字符"),
  courseId: z.number({ invalid_type_error: "关联课程不能为空" }).min(1),
  dateRange: z.array(z.any()).length(2).optional(),
});

interface LiveRoomFormValues {
  title: string;
  courseId: number;
  dateRange?: [Dayjs, Dayjs];
}

const formatDateTime = (value?: Dayjs) => value?.format("YYYY-MM-DDTHH:mm:ss");

const LiveRoomForm = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm<LiveRoomFormValues>();
  const [loading, setLoading] = useState(false);
  const [courseOptions, setCourseOptions] = useState<CourseSimpleResp[]>([]);
  const currentUserId = useSelector(
    (state: any) => state.loginUser.value.user?.id
  ) as number | undefined;

  const operatorId = useMemo(
    () => (currentUserId && currentUserId > 0 ? currentUserId : 1),
    [currentUserId]
  );

  useEffect(() => {
    courseApi
      .listCourses({
        pageNum: 1,
        pageSize: 100,
        sortField: "createdAt",
        sortOrder: "desc",
      })
      .then((result) => setCourseOptions(result.data.list || []));
  }, []);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const parsed = liveRoomSchema.parse(values);
      const payload: LiveRoomCreateReq = {
        title: parsed.title.trim(),
        courseId: parsed.courseId,
        startTime: formatDateTime(values.dateRange?.[0]),
        endTime: formatDateTime(values.dateRange?.[1]),
      };
      setLoading(true);
      await liveApi.createRoom(payload, operatorId);
      message.success("直播间创建成功");
      navigate("/live/rooms");
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
      title="新建直播间"
      extra={
        <Space>
          <Button onClick={() => navigate("/live/rooms")}>返回列表</Button>
          <Button type="primary" loading={loading} onClick={() => void handleSubmit()}>
            保存直播间
          </Button>
        </Space>
      }
    >
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="当前直播间创建只维护元数据，推流地址/回放地址等真实能力留待接入直播 SaaS 后补齐。"
      />

      <Form<LiveRoomFormValues>
        form={form}
        layout="vertical"
        initialValues={{
          title: "",
        }}
      >
        <Form.Item label="直播间标题" name="title" rules={[{ required: true }]}>
          <Input placeholder="请输入直播间标题" />
        </Form.Item>

        <Form.Item label="关联课程" name="courseId" rules={[{ required: true }]}>
          <Select
            showSearch
            optionFilterProp="label"
            options={courseOptions.map((item) => ({
              label: item.title,
              value: item.id,
            }))}
            placeholder="请选择课程"
          />
        </Form.Item>

        <Form.Item label="计划开始 / 结束时间" name="dateRange">
          <DatePicker.RangePicker
            style={{ width: "100%" }}
            showTime
            format="YYYY-MM-DD HH:mm"
          />
        </Form.Item>
      </Form>
    </Card>
  );
};

export default LiveRoomForm;
