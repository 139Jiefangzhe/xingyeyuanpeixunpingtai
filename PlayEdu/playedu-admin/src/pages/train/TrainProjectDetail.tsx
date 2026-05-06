import { Button, Card, Col, Empty, Progress, Row, Space, Statistic, Table, Tabs, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { trainApi } from "../../api/trainApi";
import type {
  ProjectStatsResp,
  ProjectTaskProgressResp,
  StudentProgressResp,
} from "../../types/train";
import { dateFormat } from "../../utils";
import {
  getOptionLabel,
  TRAIN_PROJECT_STATUS_OPTIONS,
  TRAIN_TASK_TYPE_OPTIONS,
} from "./constants";

const buildTaskColumns = (): ColumnsType<ProjectTaskProgressResp> => [
  {
    title: "任务名称",
    dataIndex: "taskName",
    key: "taskName",
    width: 240,
    render: (value: string, record) => (
      <Space direction="vertical" size={0}>
        <Typography.Text strong>{value}</Typography.Text>
        <Typography.Text type="secondary">{record.resourceTitle || "-"}</Typography.Text>
      </Space>
    ),
  },
  {
    title: "任务类型",
    dataIndex: "taskType",
    key: "taskType",
    width: 140,
    render: (value: number) => <Tag>{getOptionLabel(TRAIN_TASK_TYPE_OPTIONS, value)}</Tag>,
  },
  {
    title: "完成情况",
    key: "completion",
    width: 220,
    render: (_value, record) => (
      <Space direction="vertical" size={4}>
        <Typography.Text>
          {record.metricLabel}: {record.completedCount}/{record.totalCount}
        </Typography.Text>
        <Progress percent={record.completionRate || 0} size="small" />
      </Space>
    ),
  },
];

const buildStudentColumns = (): ColumnsType<StudentProgressResp> => [
  {
    title: "姓名",
    dataIndex: "userName",
    key: "userName",
    width: 140,
  },
  {
    title: "部门",
    dataIndex: "deptName",
    key: "deptName",
    width: 140,
  },
  {
    title: "课程完成",
    dataIndex: "courseStatus",
    key: "courseStatus",
    width: 160,
  },
  {
    title: "考试状态",
    dataIndex: "examStatus",
    key: "examStatus",
    width: 180,
  },
  {
    title: "直播状态",
    dataIndex: "liveStatus",
    key: "liveStatus",
    width: 160,
  },
  {
    title: "整体进度",
    key: "overallCompletionRate",
    width: 220,
    render: (_value, record) => (
      <Space direction="vertical" size={4} style={{ width: "100%" }}>
        <Typography.Text>
          {record.completedTaskCount}/{record.totalTaskCount} 个任务
        </Typography.Text>
        <Progress percent={record.overallCompletionRate || 0} size="small" />
      </Space>
    ),
  },
];

const TrainProjectDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState<ProjectStatsResp | null>(null);

  const loadStats = async () => {
    if (!id) {
      return;
    }
    setLoading(true);
    try {
      const result = await trainApi.getProjectStats(id);
      setStats(result.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadStats();
  }, [id]);

  const statusTag = useMemo(() => {
    const matched = TRAIN_PROJECT_STATUS_OPTIONS.find((item) => item.value === stats?.status);
    if (!matched) {
      return null;
    }
    return <Tag color={matched.color}>{matched.label}</Tag>;
  }, [stats?.status]);

  const taskColumns = useMemo(() => buildTaskColumns(), []);
  const studentColumns = useMemo(() => buildStudentColumns(), []);

  return (
    <Space direction="vertical" size={16} style={{ display: "flex" }}>
      <Card
        loading={loading}
        title="培训项目效果"
        extra={
          <Space>
            {statusTag}
            <Button onClick={() => navigate(-1)}>返回</Button>
          </Space>
        }
      >
        {stats ? (
          <Space direction="vertical" size={16} style={{ display: "flex" }}>
            <Typography.Title level={4} style={{ margin: 0 }}>
              {stats.title}
            </Typography.Title>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Card size="small">
                  <Statistic
                    title="参与人数 / 总人数"
                    value={`${stats.participantCount}/${stats.totalUserCount}`}
                  />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card size="small">
                  <Statistic title="整体完成率" value={stats.overallCompletionRate} suffix="%" />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card size="small">
                  <Space direction="vertical" size={4}>
                    <Typography.Text type="secondary">起止时间</Typography.Text>
                    <Typography.Text>
                      {dateFormat(stats.startTime || "")} - {dateFormat(stats.endTime || "")}
                    </Typography.Text>
                  </Space>
                </Card>
              </Col>
            </Row>
            <Progress percent={stats.overallCompletionRate || 0} strokeColor="#1677ff" />
          </Space>
        ) : (
          <Empty description="暂无统计数据" />
        )}
      </Card>

      <Card loading={loading}>
        <Tabs
          items={[
            {
              key: "tasks",
              label: "任务概览",
              children: (
                <Table
                  rowKey="taskId"
                  columns={taskColumns}
                  dataSource={stats?.taskProgressList || []}
                  pagination={false}
                  scroll={{ x: 760 }}
                  locale={{ emptyText: "暂无任务统计" }}
                />
              ),
            },
            {
              key: "students",
              label: "学员进度",
              children: (
                <Table
                  rowKey="userId"
                  columns={studentColumns}
                  dataSource={stats?.studentProgressList || []}
                  pagination={false}
                  scroll={{ x: 980 }}
                  locale={{ emptyText: "暂无学员进度数据" }}
                />
              ),
            },
          ]}
        />
      </Card>
    </Space>
  );
};

export default TrainProjectDetail;
