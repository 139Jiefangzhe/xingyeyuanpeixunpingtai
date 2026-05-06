import { Button, Card, Form, Input, Popconfirm, Select, Space, Table, Tag, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { trainApi } from "../../api/trainApi";
import { useTable } from "../../hooks/useTable";
import type { TrainProjectListResp, TrainProjectQuery } from "../../types/train";
import { dateFormat } from "../../utils";
import {
  getOptionLabel,
  TRAIN_PROJECT_STATUS_OPTIONS,
  TRAIN_PROJECT_TYPE_OPTIONS,
} from "./constants";

const defaultQuery: TrainProjectQuery = {
  pageNum: 1,
  pageSize: 10,
  sortField: "createTime",
  sortOrder: "desc",
};

const TrainProjectList = () => {
  const navigate = useNavigate();
  const [filterForm] = Form.useForm<TrainProjectQuery>();
  const { data, loading, query, pagination, updateQuery, resetQuery, reload, handleTableChange } =
    useTable<TrainProjectListResp, TrainProjectQuery>(trainApi.listProjects, defaultQuery);

  const statusOptions = useMemo(
    () =>
      TRAIN_PROJECT_STATUS_OPTIONS.map((item) => ({
        label: item.label,
        value: item.value,
      })),
    []
  );
  const typeOptions = useMemo(() => TRAIN_PROJECT_TYPE_OPTIONS, []);

  const handlePublish = async (id: string) => {
    await trainApi.publishProject(id);
    message.success("培训项目已发布");
    reload();
  };

  const handleSearch = () => {
    const values = filterForm.getFieldsValue();
    updateQuery({
      ...values,
      pageNum: 1,
    });
  };

  const handleReset = () => {
    filterForm.resetFields();
    resetQuery(defaultQuery);
  };

  const getProjectEntryPath = (record: TrainProjectListResp) =>
    record.status === 1 ? `/train/projects/${record.id}/tasks` : `/train/projects/${record.id}`;

  const columns: ColumnsType<TrainProjectListResp> = [
    {
      title: "项目名称",
      dataIndex: "title",
      key: "title",
      width: 260,
      render: (value: string, record) => (
        <Button type="link" onClick={() => navigate(getProjectEntryPath(record))}>
          {value}
        </Button>
      ),
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 140,
      render: (value: number) => <Tag>{getOptionLabel(TRAIN_PROJECT_TYPE_OPTIONS, value)}</Tag>,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 140,
      render: (value: number) => {
        const matched = TRAIN_PROJECT_STATUS_OPTIONS.find((item) => item.value === value);
        return <Tag color={matched?.color}>{matched?.label || value}</Tag>;
      },
    },
    {
      title: "任务数",
      dataIndex: "taskCount",
      key: "taskCount",
      width: 120,
    },
    {
      title: "开始时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 180,
      render: (value?: string) => dateFormat(value || ""),
    },
    {
      title: "结束时间",
      dataIndex: "endTime",
      key: "endTime",
      width: 180,
      render: (value?: string) => dateFormat(value || ""),
    },
    {
      title: "操作",
      key: "action",
      width: 280,
      render: (_value, record) => (
        <Space wrap>
          {record.status === 1 ? (
            <Button type="link" onClick={() => navigate(`/train/projects/${record.id}/tasks`)}>
              {record.taskCount > 0 ? "查看任务" : "配置任务"}
            </Button>
          ) : (
            <Button type="link" onClick={() => navigate(`/train/projects/${record.id}`)}>
              查看详情
            </Button>
          )}
          <Popconfirm
            title="确定发布该培训项目吗？"
            onConfirm={() => void handlePublish(record.id)}
          >
            <Button type="link" disabled={record.status !== 1}>
              发布
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="培训项目"
      extra={
        <Button type="primary" onClick={() => navigate("/train/projects/create")}>
          新建项目
        </Button>
      }
    >
      <Form form={filterForm} layout="inline" initialValues={query}>
        <Form.Item name="titleLike" label="项目名称">
          <Input placeholder="按培训项目标题筛选" allowClear />
        </Form.Item>
        <Form.Item name="type" label="项目类型">
          <Select
            style={{ width: 160 }}
            options={typeOptions}
            allowClear
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item name="status" label="项目状态">
          <Select
            style={{ width: 160 }}
            options={statusOptions}
            allowClear
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" onClick={handleSearch}>
              查询
            </Button>
            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        style={{ marginTop: 16 }}
        rowKey="id"
        loading={loading}
        columns={columns}
        dataSource={data.list}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />
    </Card>
  );
};

export default TrainProjectList;
