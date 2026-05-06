import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  DeleteOutlined,
  PlusOutlined,
} from "@ant-design/icons";
import {
  Alert,
  Button,
  Card,
  Col,
  Empty,
  Input,
  List,
  Row,
  Select,
  Space,
  Spin,
  Statistic,
  Switch,
  Tag,
  Typography,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { courseApi } from "../../api/courseApi";
import { examApi } from "../../api/examApi";
import { liveApi } from "../../api/liveApi";
import { trainApi } from "../../api/trainApi";
import type { CourseSimpleResp } from "../../types/course";
import type { ExamPaperSimpleResp } from "../../types/exam";
import type { LiveRoomResp } from "../../types/live";
import type {
  TrainProjectDetailResp,
  TrainTaskReq,
  TrainTaskResp,
} from "../../types/train";
import { dateFormat } from "../../utils";
import {
  getOptionLabel,
  TRAIN_PROJECT_STATUS_OPTIONS,
  TRAIN_TASK_PASS_RULE_OPTIONS,
  TRAIN_TASK_TYPE_OPTIONS,
} from "./constants";

interface DraftTrainTask extends TrainTaskReq {
  localId: string;
  resourceTitle: string;
  metricText?: string;
}

type RenderTrainTask = DraftTrainTask | (TrainTaskResp & {
  localId: string;
  resourceTitle: string;
  metricText?: string;
});

const normalizeDraftTasks = (tasks: DraftTrainTask[]): DraftTrainTask[] =>
  tasks.map((task, index) => ({
    ...task,
    sort: index + 1,
  }));

const buildOptionFilter = (input: string, option?: { label?: string | number }) =>
  String(option?.label || "")
    .toLowerCase()
    .includes(input.toLowerCase());

const TrainTaskConfig = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [projectLoading, setProjectLoading] = useState(false);
  const [resourceLoading, setResourceLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [projectDetail, setProjectDetail] = useState<TrainProjectDetailResp | null>(null);
  const [draftTasks, setDraftTasks] = useState<DraftTrainTask[]>([]);
  const [courseOptions, setCourseOptions] = useState<CourseSimpleResp[]>([]);
  const [examOptions, setExamOptions] = useState<ExamPaperSimpleResp[]>([]);
  const [liveOptions, setLiveOptions] = useState<LiveRoomResp[]>([]);
  const [examFallbackNotice, setExamFallbackNotice] = useState(false);
  const [selectedCourseId, setSelectedCourseId] = useState<number>();
  const [selectedExamId, setSelectedExamId] = useState<string>();
  const [selectedLiveId, setSelectedLiveId] = useState<string>();
  const [homeworkName, setHomeworkName] = useState("");

  const isDraft = projectDetail?.status === 1;
  const hasPersistedTasks = (projectDetail?.tasks?.length || 0) > 0;
  const canEditDraftTasks = Boolean(projectDetail && isDraft && !hasPersistedTasks);

  const loadProjectDetail = async () => {
    if (!id) {
      return;
    }
    setProjectLoading(true);
    try {
      const result = await trainApi.getProjectDetail(id);
      setProjectDetail(result.data);
    } finally {
      setProjectLoading(false);
    }
  };

  useEffect(() => {
    void loadProjectDetail();
  }, [id]);

  useEffect(() => {
    const loadResources = async () => {
      setResourceLoading(true);
      try {
        const [courseResult, liveResult] = await Promise.all([
          courseApi.listCourses({
            pageNum: 1,
            pageSize: 100,
            sortField: "createdAt",
            sortOrder: "desc",
          }),
          liveApi.listRooms({
            pageNum: 1,
            pageSize: 100,
            sortField: "createTime",
            sortOrder: "desc",
          }),
        ]);
        const publishedExamResult = await examApi.listPapers({
          pageNum: 1,
          pageSize: 100,
          status: 2,
          sortField: "createTime",
          sortOrder: "desc",
        });
        let papers = publishedExamResult.data.list || [];
        if (papers.length === 0) {
          const fallbackExamResult = await examApi.listPapers({
            pageNum: 1,
            pageSize: 100,
            sortField: "createTime",
            sortOrder: "desc",
          });
          papers = fallbackExamResult.data.list || [];
          setExamFallbackNotice(papers.length > 0);
        } else {
          setExamFallbackNotice(false);
        }
        setCourseOptions(courseResult.data.list || []);
        setExamOptions(papers);
        setLiveOptions(liveResult.data.list || []);
      } finally {
        setResourceLoading(false);
      }
    };

    void loadResources();
  }, []);

  const renderedTasks = useMemo<RenderTrainTask[]>(() => {
    if (canEditDraftTasks) {
      return draftTasks;
    }
    return (projectDetail?.tasks || []).map((task) => ({
      ...task,
      resourceTitle:
        task.courseTitle ||
        task.examPaperTitle ||
        task.name,
      metricText: undefined,
      localId: task.id,
    }));
  }, [canEditDraftTasks, draftTasks, projectDetail?.tasks]);

  const requiredTaskCount = useMemo(
    () => renderedTasks.filter((task) => task.required === 1).length,
    [renderedTasks]
  );

  const ensureProjectEditable = () => {
    if (!projectDetail || !isDraft) {
      message.warning("当前项目不是草稿状态，不能再配置任务");
      return false;
    }
    if (!canEditDraftTasks) {
      message.warning("当前版本仅支持首次批量配置任务，已保存任务会以只读方式展示");
      return false;
    }
    return true;
  };

  const appendDraftTask = (task: Omit<DraftTrainTask, "localId" | "sort">) => {
    if (!ensureProjectEditable()) {
      return;
    }
    const duplicate = draftTasks.find(
      (item) => item.type === task.type && item.refId === task.refId
    );
    if (duplicate) {
      message.warning("该资源已加入任务流");
      return;
    }
    setDraftTasks((current) =>
      normalizeDraftTasks([
        ...current,
        {
          ...task,
          localId: `${task.type}-${task.refId}`,
          sort: current.length + 1,
        },
      ])
    );
  };

  const handleAddCourse = () => {
    const course = courseOptions.find((item) => item.id === selectedCourseId);
    if (!course) {
      message.warning("请先选择课程");
      return;
    }
    appendDraftTask({
      name: course.title,
      type: 1,
      refId: String(course.id),
      required: 1,
      passRule: 1,
      resourceTitle: course.title,
      metricText: `${course.classHour || 0} 课时`,
    });
    setSelectedCourseId(undefined);
  };

  const handleAddExam = () => {
    const paper = examOptions.find((item) => item.id === selectedExamId);
    if (!paper) {
      message.warning("请先选择试卷");
      return;
    }
    appendDraftTask({
      name: paper.title,
      type: 2,
      refId: paper.id,
      required: 1,
      passRule: 2,
      resourceTitle: paper.title,
      metricText: `${paper.totalScore || 0} 分`,
    });
    setSelectedExamId(undefined);
  };

  const handleAddLive = () => {
    const room = liveOptions.find((item) => item.id === selectedLiveId);
    if (!room) {
      message.warning("请先选择直播间");
      return;
    }
    appendDraftTask({
      name: room.title,
      type: 3,
      refId: room.id,
      required: 0,
      passRule: 1,
      resourceTitle: room.title,
      metricText: room.startTime ? `开播：${dateFormat(room.startTime)}` : undefined,
    });
    setSelectedLiveId(undefined);
  };

  const handleAddHomework = () => {
    const trimmedName = homeworkName.trim();
    if (!trimmedName) {
      message.warning("请先输入作业名称");
      return;
    }
    appendDraftTask({
      name: trimmedName,
      type: 4,
      refId: `homework:${Date.now()}`,
      required: 1,
      passRule: 3,
      resourceTitle: trimmedName,
      metricText: "占位任务",
    });
    setHomeworkName("");
  };

  const handleDeleteDraftTask = (localId: string) => {
    if (!ensureProjectEditable()) {
      return;
    }
    setDraftTasks((current) =>
      normalizeDraftTasks(current.filter((item) => item.localId !== localId))
    );
  };

  const handleMoveDraftTask = (localId: string, direction: "up" | "down") => {
    if (!ensureProjectEditable()) {
      return;
    }
    setDraftTasks((current) => {
      const index = current.findIndex((item) => item.localId === localId);
      if (index < 0) {
        return current;
      }
      const targetIndex = direction === "up" ? index - 1 : index + 1;
      if (targetIndex < 0 || targetIndex >= current.length) {
        return current;
      }
      const next = [...current];
      const [task] = next.splice(index, 1);
      next.splice(targetIndex, 0, task);
      return normalizeDraftTasks(next);
    });
  };

  const updateDraftTask = (localId: string, patch: Partial<DraftTrainTask>) => {
    if (!ensureProjectEditable()) {
      return;
    }
    setDraftTasks((current) =>
      current.map((item) =>
        item.localId === localId
          ? {
              ...item,
              ...patch,
            }
          : item
      )
    );
  };

  const buildPayload = (): TrainTaskReq[] =>
    normalizeDraftTasks(draftTasks).map((item) => ({
      name: item.name,
      type: item.type,
      refId: item.refId,
      sort: item.sort,
      required: item.required,
      passRule: item.passRule,
    }));

  const handleSaveTasks = async () => {
    if (!id || draftTasks.length === 0) {
      message.warning("请至少添加一个任务");
      return;
    }
    setSubmitting(true);
    try {
      await trainApi.addTasks(id, buildPayload());
      message.success("任务配置已保存");
      setDraftTasks([]);
      await loadProjectDetail();
    } finally {
      setSubmitting(false);
    }
  };

  const handlePublish = async () => {
    if (!id || !projectDetail) {
      return;
    }
    setSubmitting(true);
    try {
      if (canEditDraftTasks) {
        if (draftTasks.length === 0) {
          message.warning("请至少添加一个任务后再发布");
          return;
        }
        await trainApi.addTasks(id, buildPayload());
      }
      await trainApi.publishProject(id);
      message.success("培训项目已发布");
      navigate("/train/projects");
    } finally {
      setSubmitting(false);
    }
  };

  const renderTaskName = (task: RenderTrainTask) =>
    task.resourceTitle ||
    ("courseTitle" in task ? task.courseTitle : undefined) ||
    ("examPaperTitle" in task ? task.examPaperTitle : undefined) ||
    task.name;

  if (!id) {
    return (
      <Card>
        <Alert type="error" showIcon message="缺少项目 ID，无法进入任务配置页面。" />
      </Card>
    );
  }

  return (
    <Spin spinning={projectLoading || resourceLoading}>
      <Row gutter={16} align="stretch">
        <Col span={8}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Card title="任务资源池" bordered={false}>
              <Alert
                type={canEditDraftTasks ? "info" : "warning"}
                showIcon
                style={{ marginBottom: 16 }}
                message={
                  canEditDraftTasks
                    ? "先在左侧选择资源，再在右侧调整必做、通过标准和顺序。"
                    : "当前版本仅支持首次批量配置任务。已保存任务会以只读方式展示，可直接发布项目。"
                }
              />
              {examFallbackNotice ? (
                <Alert
                  type="warning"
                  showIcon
                  style={{ marginBottom: 16 }}
                  message="当前本地没有已发布试卷，考试任务选择器已回退为显示全部试卷，便于联调。"
                />
              ) : null}

              <Space direction="vertical" size={16} style={{ width: "100%" }}>
                <Card type="inner" title="课程任务" size="small">
                  <Space direction="vertical" style={{ width: "100%" }}>
                    <Select
                      showSearch
                      allowClear
                      value={selectedCourseId}
                      disabled={!canEditDraftTasks}
                      placeholder="搜索并选择课程"
                      options={courseOptions.map((item) => ({
                        label: item.title,
                        value: item.id,
                      }))}
                      filterOption={buildOptionFilter}
                      onChange={(value) => setSelectedCourseId(value)}
                    />
                    <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      disabled={!canEditDraftTasks}
                      onClick={handleAddCourse}
                    >
                      添加课程任务
                    </Button>
                  </Space>
                </Card>

                <Card type="inner" title="考试任务" size="small">
                  <Space direction="vertical" style={{ width: "100%" }}>
                    <Select
                      showSearch
                      allowClear
                      value={selectedExamId}
                      disabled={!canEditDraftTasks}
                      placeholder="搜索并选择已发布试卷"
                      options={examOptions.map((item) => ({
                        label: item.title,
                        value: item.id,
                      }))}
                      filterOption={buildOptionFilter}
                      onChange={(value) => setSelectedExamId(value)}
                    />
                    <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      disabled={!canEditDraftTasks}
                      onClick={handleAddExam}
                    >
                      添加考试任务
                    </Button>
                  </Space>
                </Card>

                <Card type="inner" title="直播任务" size="small">
                  <Space direction="vertical" style={{ width: "100%" }}>
                    <Select
                      showSearch
                      allowClear
                      value={selectedLiveId}
                      disabled={!canEditDraftTasks}
                      placeholder="搜索并选择直播间"
                      options={liveOptions.map((item) => ({
                        label: item.title,
                        value: item.id,
                      }))}
                      filterOption={buildOptionFilter}
                      onChange={(value) => setSelectedLiveId(value)}
                    />
                    <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      disabled={!canEditDraftTasks}
                      onClick={handleAddLive}
                    >
                      添加直播任务
                    </Button>
                  </Space>
                </Card>

                <Card type="inner" title="作业任务" size="small">
                  <Space direction="vertical" style={{ width: "100%" }}>
                    <Input
                      value={homeworkName}
                      disabled={!canEditDraftTasks}
                      placeholder="输入作业名称，当前为占位任务"
                      onChange={(event) => setHomeworkName(event.target.value)}
                    />
                    <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      disabled={!canEditDraftTasks}
                      onClick={handleAddHomework}
                    >
                      添加作业任务
                    </Button>
                  </Space>
                </Card>
              </Space>
            </Card>
          </Space>
        </Col>

        <Col span={16}>
          <Card
            bordered={false}
            title={
              <Space direction="vertical" size={4}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  {projectDetail?.title || "培训任务配置"}
                </Typography.Title>
                <Space wrap>
                  <Tag>
                    {getOptionLabel(TRAIN_PROJECT_STATUS_OPTIONS, projectDetail?.status)}
                  </Tag>
                  <span>
                    起止时间：
                    {projectDetail?.startTime
                      ? `${dateFormat(projectDetail.startTime)} - ${dateFormat(
                          projectDetail.endTime || ""
                        )}`
                      : "未设置"}
                  </span>
                </Space>
              </Space>
            }
            extra={
              <Space>
                <Button onClick={() => navigate("/train/projects")}>返回列表</Button>
                {canEditDraftTasks ? (
                  <>
                    <Button loading={submitting} onClick={() => void handleSaveTasks()}>
                      保存任务配置
                    </Button>
                    <Button type="primary" loading={submitting} onClick={() => void handlePublish()}>
                      保存并发布项目
                    </Button>
                  </>
                ) : isDraft ? (
                  <Button type="primary" loading={submitting} onClick={() => void handlePublish()}>
                    直接发布项目
                  </Button>
                ) : null}
              </Space>
            }
          >
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={8}>
                <Statistic title="总任务数" value={renderedTasks.length} />
              </Col>
              <Col span={8}>
                <Statistic title="必做任务数" value={requiredTaskCount} />
              </Col>
              <Col span={8}>
                <Statistic
                  title="可编辑状态"
                  value={canEditDraftTasks ? "首次配置中" : isDraft ? "已保存只读" : "已发布"}
                />
              </Col>
            </Row>

            {renderedTasks.length === 0 ? (
              <Empty description="当前项目还没有任务，请从左侧添加课程/考试/直播/作业任务" />
            ) : (
              <List
                itemLayout="vertical"
                dataSource={renderedTasks}
                renderItem={(task, index) => (
                  <List.Item
                    key={task.localId}
                    actions={[
                      <Space key="meta">
                        <Tag>
                          {getOptionLabel(TRAIN_TASK_TYPE_OPTIONS, task.type)}
                        </Tag>
                        <span>{task.metricText || "-"}</span>
                      </Space>,
                    ]}
                  >
                    <Space
                      align="start"
                      style={{
                        width: "100%",
                        justifyContent: "space-between",
                      }}
                    >
                      <Space align="start" size={16}>
                        <Typography.Text strong>{index + 1}</Typography.Text>
                        <div>
                          <Typography.Text strong>{renderTaskName(task)}</Typography.Text>
                          <div style={{ marginTop: 8 }}>
                            <Space wrap>
                              <Tag color="blue">
                                {getOptionLabel(TRAIN_TASK_TYPE_OPTIONS, task.type)}
                              </Tag>
                              <span>资源 ID：{task.refId}</span>
                            </Space>
                          </div>
                        </div>
                      </Space>

                      <Space wrap align="center" size={12}>
                        <span>必做</span>
                        <Switch
                          checked={task.required === 1}
                          disabled={!canEditDraftTasks}
                          onChange={(checked) =>
                            updateDraftTask(task.localId, {
                              required: checked ? 1 : 0,
                            })
                          }
                        />
                        <Select
                          style={{ width: 160 }}
                          value={task.passRule}
                          disabled={!canEditDraftTasks}
                          options={TRAIN_TASK_PASS_RULE_OPTIONS}
                          onChange={(value) =>
                            updateDraftTask(task.localId, {
                              passRule: value,
                            })
                          }
                        />
                        {canEditDraftTasks ? (
                          <Space>
                            <Button
                              icon={<ArrowUpOutlined />}
                              disabled={index === 0}
                              onClick={() => handleMoveDraftTask(task.localId, "up")}
                            />
                            <Button
                              icon={<ArrowDownOutlined />}
                              disabled={index === renderedTasks.length - 1}
                              onClick={() => handleMoveDraftTask(task.localId, "down")}
                            />
                            <Button
                              danger
                              icon={<DeleteOutlined />}
                              onClick={() => handleDeleteDraftTask(task.localId)}
                            />
                          </Space>
                        ) : null}
                      </Space>
                    </Space>
                  </List.Item>
                )}
              />
            )}
          </Card>
        </Col>
      </Row>
    </Spin>
  );
};

export default TrainTaskConfig;
