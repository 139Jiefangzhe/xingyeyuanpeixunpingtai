import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { Alert, Button, Card, Form, Input, InputNumber, Select, Space, Tag, message } from "antd";
import type { RuleObject } from "antd/es/form";
import { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import { z } from "zod";
import { courseApi } from "../../api/courseApi";
import type {
  CourseCategoryOption,
  CourseChapterReq,
  CourseDetailResp,
  CourseLessonReq,
  CourseSaveReq,
} from "../../types/course";
import { COURSE_STATUS_OPTIONS, COURSE_TYPE_OPTIONS, getCourseOptionLabel } from "./manageConstants";

const lessonSchema = z.object({
  title: z.string().min(1, "课节标题不能为空").max(200, "课节标题不能超过200字符"),
  resourceUrl: z.string().max(512, "资源 URL 不能超过512字符").optional().or(z.literal("")),
  duration: z.number().min(0, "课节时长不能小于0").max(86400, "课节时长不能超过86400秒"),
});

const chapterSchema = z.object({
  name: z.string().min(1, "章节名称不能为空").max(200, "章节名称不能超过200字符"),
  lessons: z.array(lessonSchema).min(1, "每个章节至少需要1个课节"),
});

const courseSchema = z.object({
  title: z.string().min(1, "课程标题不能为空").max(200, "课程标题不能超过200字符"),
  shortDesc: z.string().max(500, "课程描述不能超过500字符").optional().or(z.literal("")),
  coverUrl: z.string().max(512, "封面 URL 不能超过512字符").optional().or(z.literal("")),
  type: z.number({ invalid_type_error: "课程类型不能为空" }).min(1).max(3),
  categoryId: z.number({ invalid_type_error: "课程分类不能为空" }).min(1),
  isShow: z.number({ invalid_type_error: "课程状态不能为空" }).min(0).max(1),
  classHour: z.number({ invalid_type_error: "课时数不能为空" }).min(0).max(10000),
  chapters: z.array(chapterSchema).min(1, "至少需要1个章节"),
});

type CourseFormValues = z.infer<typeof courseSchema>;

const createEmptyLesson = (): CourseLessonReq => ({
  title: "",
  resourceUrl: "",
  duration: 0,
});

const createEmptyChapter = (): CourseChapterReq => ({
  name: "",
  lessons: [createEmptyLesson()],
});

const buildInitialValues = (detail?: CourseDetailResp | null): CourseFormValues => ({
  title: detail?.title || "",
  shortDesc: detail?.shortDesc || "",
  coverUrl: detail?.coverUrl || "",
  type: detail?.type || 1,
  categoryId: detail?.categoryIds?.[0] || 1,
  isShow: detail?.isShow ?? 1,
  classHour: detail?.classHour ?? 0,
  chapters:
    detail?.chapters?.map((chapter) => ({
      name: chapter.name,
      lessons:
        chapter.lessons?.map((lesson) => ({
          title: lesson.title,
          resourceUrl: lesson.resourceUrl || "",
          duration: lesson.duration || 0,
        })) || [createEmptyLesson()],
    })) || [createEmptyChapter()],
});

const CourseForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form] = Form.useForm<CourseFormValues>();
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [categoryOptions, setCategoryOptions] = useState<CourseCategoryOption[]>([]);
  const currentUserId = useSelector(
    (state: any) => state.loginUser.value.user?.id
  ) as number | undefined;
  const courseType = Form.useWatch("type", form) || 1;

  const operatorId = useMemo(
    () => (currentUserId && currentUserId > 0 ? currentUserId : 1),
    [currentUserId]
  );
  const editing = Boolean(id);

  useEffect(() => {
    courseApi
      .listCategoryOptions()
      .then((result) => setCategoryOptions(result.data || []));
  }, []);

  useEffect(() => {
    if (!id) {
      form.setFieldsValue(buildInitialValues());
      return;
    }
    setDetailLoading(true);
    courseApi
      .getCourseDetail(id)
      .then((result) => {
        form.setFieldsValue(buildInitialValues(result.data));
      })
      .finally(() => {
        setDetailLoading(false);
      });
  }, [form, id]);

  const normalizePayload = (values: CourseFormValues): CourseSaveReq => ({
    title: values.title.trim(),
    shortDesc: values.shortDesc?.trim() || "",
    coverUrl: values.coverUrl?.trim() || "",
    type: values.type,
    categoryIds: [values.categoryId],
    isShow: values.isShow,
    classHour: values.classHour,
    chapters: values.chapters.map((chapter) => ({
      name: chapter.name.trim(),
      lessons: chapter.lessons.map((lesson) => ({
        title: lesson.title.trim(),
        resourceUrl: lesson.resourceUrl?.trim() || "",
        duration: lesson.duration || 0,
      })),
    })),
  });

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const parsed = courseSchema.parse(values);
      setLoading(true);
      const payload = normalizePayload(parsed);
      if (editing && id) {
        await courseApi.updateCourse(id, payload, operatorId);
        message.success("课程更新成功");
      } else {
        await courseApi.createCourse(payload, operatorId);
        message.success("课程创建成功");
      }
      navigate("/courses");
    } catch (error) {
      if (error instanceof z.ZodError) {
        message.error(error.issues[0]?.message || "表单校验失败");
      }
    } finally {
      setLoading(false);
    }
  };

  const chapterRules = (_rule: RuleObject, value: CourseChapterReq[]) => {
    if (Array.isArray(value) && value.length > 0) {
      return Promise.resolve();
    }
    return Promise.reject(new Error("至少需要1个章节"));
  };

  return (
    <Card
      loading={detailLoading}
      title={editing ? "编辑课程" : "新建课程"}
      extra={
        <Space>
          <Button onClick={() => navigate("/courses")}>返回列表</Button>
          <Button type="primary" loading={loading} onClick={() => void handleSubmit()}>
            保存课程
          </Button>
        </Space>
      }
    >
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="课程封面 URL 和课节资源 URL 当前通过 local profile 走轻量存储，后续接 file-svc 后可替换为正式上传组件。"
      />

      <Form<CourseFormValues>
        form={form}
        layout="vertical"
        initialValues={buildInitialValues()}
      >
        <Form.Item label="课程标题" name="title" rules={[{ required: true }]}>
          <Input placeholder="请输入课程标题" />
        </Form.Item>

        <Form.Item label="课程描述" name="shortDesc">
          <Input.TextArea rows={4} placeholder="请输入课程描述" />
        </Form.Item>

        <Form.Item label="封面 URL" name="coverUrl">
          <Input placeholder="先输入封面图片 URL，后续再接上传组件" />
        </Form.Item>

        <Form.Item label="课程类型" name="type" rules={[{ required: true }]}>
          <Select
            options={COURSE_TYPE_OPTIONS.map((item) => ({
              label: item.label,
              value: item.value,
            }))}
            placeholder="请选择课程类型"
          />
        </Form.Item>

        <Form.Item label="课程分类" name="categoryId" rules={[{ required: true }]}>
          <Select
            showSearch
            options={categoryOptions}
            optionFilterProp="label"
            placeholder="请选择课程分类"
          />
        </Form.Item>

        <Form.Item label="课程状态" name="isShow" rules={[{ required: true }]}>
          <Select
            options={COURSE_STATUS_OPTIONS.map((item) => ({
              label: item.label,
              value: item.value,
            }))}
            placeholder="请选择课程状态"
          />
        </Form.Item>

        <Form.Item label="课时数" name="classHour" rules={[{ required: true }]}>
          <InputNumber style={{ width: "100%" }} min={0} max={10000} />
        </Form.Item>

        <Form.List name="chapters" rules={[{ validator: chapterRules }]}>
          {(chapterFields, chapterOperations, chapterMeta) => (
            <Card
              type="inner"
              title="章节 / 课节设置"
              extra={
                <Button
                  type="dashed"
                  icon={<PlusOutlined />}
                  onClick={() => chapterOperations.add(createEmptyChapter())}
                >
                  添加章节
                </Button>
              }
            >
              {chapterFields.map((chapterField, chapterIndex) => (
                <Card
                  key={chapterField.key}
                  type="inner"
                  style={{ marginBottom: 16 }}
                  title={
                    <Space>
                      <Tag color="blue">{`章节 ${chapterIndex + 1}`}</Tag>
                      <span>章节信息</span>
                    </Space>
                  }
                  extra={
                    chapterFields.length > 1 ? (
                      <Button
                        danger
                        type="text"
                        icon={<DeleteOutlined />}
                        onClick={() => chapterOperations.remove(chapterField.name)}
                      >
                        删除章节
                      </Button>
                    ) : null
                  }
                >
                  <Form.Item
                    label="章节名称"
                    name={[chapterField.name, "name"]}
                    rules={[{ required: true, message: "请输入章节名称" }]}
                  >
                    <Input placeholder="请输入章节名称" />
                  </Form.Item>

                  <Form.List name={[chapterField.name, "lessons"]}>
                    {(lessonFields, lessonOperations) => (
                      <>
                        {lessonFields.map((lessonField, lessonIndex) => (
                          <Card
                            key={lessonField.key}
                            size="small"
                            style={{ marginBottom: 12 }}
                            title={
                              <Space>
                                <Tag color="purple">{`课节 ${lessonIndex + 1}`}</Tag>
                                <span>
                                  {getCourseOptionLabel(
                                    COURSE_TYPE_OPTIONS.map((item) => ({
                                      label: item.label,
                                      value: item.value,
                                    })),
                                    courseType
                                  )}
                                </span>
                              </Space>
                            }
                            extra={
                              lessonFields.length > 1 ? (
                                <Button
                                  danger
                                  type="text"
                                  icon={<DeleteOutlined />}
                                  onClick={() => lessonOperations.remove(lessonField.name)}
                                >
                                  删除课节
                                </Button>
                              ) : null
                            }
                          >
                            <Form.Item
                              label="课节标题"
                              name={[lessonField.name, "title"]}
                              rules={[{ required: true, message: "请输入课节标题" }]}
                            >
                              <Input placeholder="请输入课节标题" />
                            </Form.Item>
                            <Form.Item label="资源 URL" name={[lessonField.name, "resourceUrl"]}>
                              <Input placeholder="先输入资源 URL，后续再接资源库/上传组件" />
                            </Form.Item>
                            <Form.Item
                              label="时长（秒）"
                              name={[lessonField.name, "duration"]}
                              rules={[{ required: true, message: "请输入课节时长" }]}
                            >
                              <InputNumber style={{ width: "100%" }} min={0} max={86400} />
                            </Form.Item>
                          </Card>
                        ))}
                        <Button
                          type="dashed"
                          icon={<PlusOutlined />}
                          onClick={() => lessonOperations.add(createEmptyLesson())}
                        >
                          添加课节
                        </Button>
                      </>
                    )}
                  </Form.List>
                </Card>
              ))}
              <Form.ErrorList errors={chapterMeta.errors} />
            </Card>
          )}
        </Form.List>
      </Form>
    </Card>
  );
};

export default CourseForm;
