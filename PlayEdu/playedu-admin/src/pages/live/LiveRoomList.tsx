import { Button, Card, Form, Input, Select, Space, Table, Tag, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { courseApi } from "../../api/courseApi";
import { liveApi } from "../../api/liveApi";
import { useTable } from "../../hooks/useTable";
import type { CourseSimpleResp } from "../../types/course";
import type { LiveRoomQuery, LiveRoomResp } from "../../types/live";
import { dateFormat } from "../../utils";
import { getLiveStatusLabel, LIVE_STATUS_OPTIONS } from "./constants";

const defaultQuery: LiveRoomQuery = {
  pageNum: 1,
  pageSize: 10,
  sortField: "createTime",
  sortOrder: "desc",
};

const LiveRoomList = () => {
  const navigate = useNavigate();
  const [filterForm] = Form.useForm<LiveRoomQuery>();
  const [courseOptions, setCourseOptions] = useState<CourseSimpleResp[]>([]);
  const { data, loading, query, pagination, updateQuery, resetQuery, reload, handleTableChange } =
    useTable<LiveRoomResp, LiveRoomQuery>(liveApi.listRooms, defaultQuery);

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

  const courseMap = useMemo(
    () =>
      courseOptions.reduce<Record<number, string>>((acc, item) => {
        acc[item.id] = item.title;
        return acc;
      }, {}),
    [courseOptions]
  );

  const handleSearch = () => {
    updateQuery({
      ...filterForm.getFieldsValue(),
      pageNum: 1,
    });
  };

  const handleReset = () => {
    filterForm.resetFields();
    resetQuery(defaultQuery);
  };

  const handleStart = async (id: string) => {
    await liveApi.startLive(id, 1);
    message.success("直播间已切换为直播中");
    reload();
  };

  const handleStop = async (id: string) => {
    await liveApi.stopLive(id, 1);
    message.success("直播间已结束");
    reload();
  };

  const columns: ColumnsType<LiveRoomResp> = [
    {
      title: "直播间名称",
      dataIndex: "title",
      key: "title",
      width: 240,
    },
    {
      title: "关联课程",
      dataIndex: "courseId",
      key: "courseId",
      width: 220,
      render: (value?: number) => (value ? courseMap[value] || `课程 ${value}` : "-"),
    },
    {
      title: "开始时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 180,
      render: (value?: string) => (value ? dateFormat(value) : "-"),
    },
    {
      title: "结束时间",
      dataIndex: "endTime",
      key: "endTime",
      width: 180,
      render: (value?: string) => (value ? dateFormat(value) : "-"),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: number) => {
        const option = LIVE_STATUS_OPTIONS.find((item) => item.value === value);
        return <Tag color={option?.color}>{option?.label || value}</Tag>;
      },
    },
    {
      title: "操作",
      key: "action",
      width: 260,
      render: (_value, record) => (
        <Space wrap>
          <Button type="link" disabled={record.status !== 1} onClick={() => void handleStart(record.id)}>
            开始直播
          </Button>
          <Button type="link" disabled={record.status !== 2} onClick={() => void handleStop(record.id)}>
            结束直播
          </Button>
          <Button
            type="link"
            disabled={!record.recordUrl}
            onClick={() => record.recordUrl && window.open(record.recordUrl, "_blank")}
          >
            查看回放
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="直播管理"
      extra={
        <Button type="primary" onClick={() => navigate("/live/rooms/create")}>
          新建直播间
        </Button>
      }
    >
      <Form form={filterForm} layout="inline" initialValues={query}>
        <Form.Item name="titleLike" label="直播间">
          <Input placeholder="按直播间名称筛选" allowClear />
        </Form.Item>
        <Form.Item name="courseId" label="课程">
          <Select
            style={{ width: 200 }}
            allowClear
            showSearch
            optionFilterProp="label"
            options={courseOptions.map((item) => ({
              label: item.title,
              value: item.id,
            }))}
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select
            style={{ width: 160 }}
            allowClear
            options={LIVE_STATUS_OPTIONS.map((item) => ({
              label: item.label,
              value: item.value,
            }))}
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

export default LiveRoomList;
