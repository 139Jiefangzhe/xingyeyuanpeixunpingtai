import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { enumApi } from "../../api/enumApi";
import { examApi } from "../../api/examApi";
import { useTable } from "../../hooks/useTable";
import type { EnumDictionary, EnumOption } from "../../types/api";
import type { ExamPaperQuery, ExamPaperSimpleResp } from "../../types/exam";
import { getEnumLabel } from "../../utils/enumMap";
import { dateFormat } from "../../utils/index";

const EXAM_PAPER_STATUS_ENUM_KEY = "examPaperStatus";
const EXAM_PAPER_TYPE_ENUM_KEY = "examPaperType";

const defaultQuery: ExamPaperQuery = {
  pageNum: 1,
  pageSize: 10,
  sortField: "createTime",
  sortOrder: "desc",
};

const ExamPaperList = () => {
  const navigate = useNavigate();
  const {
    data,
    query,
    loading,
    updateQuery,
    resetQuery,
    reload,
    handleTableChange,
    pagination,
  } = useTable<ExamPaperSimpleResp, ExamPaperQuery>(
    examApi.listPapers,
    defaultQuery
  );
  const [filterForm] = Form.useForm<ExamPaperQuery>();
  const [enumDictionary, setEnumDictionary] = useState<EnumDictionary>({});

  useEffect(() => {
    enumApi
      .getMany([EXAM_PAPER_STATUS_ENUM_KEY, EXAM_PAPER_TYPE_ENUM_KEY])
      .then(setEnumDictionary);
  }, []);

  const statusOptions = useMemo<EnumOption[]>(
    () => enumDictionary[EXAM_PAPER_STATUS_ENUM_KEY] || [],
    [enumDictionary]
  );
  const typeOptions = useMemo<EnumOption[]>(
    () => enumDictionary[EXAM_PAPER_TYPE_ENUM_KEY] || [],
    [enumDictionary]
  );

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

  const handlePublish = async (id: string) => {
    await examApi.publishPaper(id);
    message.success("试卷发布成功");
    reload();
  };

  const handleCopy = async (id: string) => {
    const result = await examApi.copyPaper(id);
    message.success(`试卷复制成功，新试卷 ID：${result.data}`);
    reload();
  };

  const handleDelete = async (id: string) => {
    await examApi.deletePaper(id);
    message.success("试卷删除成功");
    reload();
  };

  const columns = [
    {
      title: "试卷标题",
      dataIndex: "title",
      key: "title",
      ellipsis: true,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 130,
      render: (value: number) => (
        <Tag>{getEnumLabel(enumDictionary, EXAM_PAPER_TYPE_ENUM_KEY, value)}</Tag>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 130,
      render: (value: number) => (
        <Tag>{getEnumLabel(enumDictionary, EXAM_PAPER_STATUS_ENUM_KEY, value)}</Tag>
      ),
    },
    {
      title: "总分",
      dataIndex: "totalScore",
      key: "totalScore",
      width: 100,
    },
    {
      title: "时长",
      dataIndex: "duration",
      key: "duration",
      width: 120,
      render: (value: number) => `${value} 分钟`,
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 180,
      render: (value: string) => dateFormat(value),
    },
    {
      title: "操作",
      key: "action",
      width: 360,
      render: (_: unknown, record: ExamPaperSimpleResp) => (
        <Space wrap>
          <Button type="link" onClick={() => navigate(`/exam/papers/${record.id}`)}>
            编辑
          </Button>
          {record.status === 1 ? (
            <Button
              type="link"
              onClick={() => navigate(`/exam/papers/${record.id}/compose`)}
            >
              编排题目
            </Button>
          ) : null}
          <Popconfirm
            title="确定发布该试卷吗？"
            onConfirm={() => void handlePublish(record.id)}
          >
            <Button type="link" disabled={record.status !== 1}>
              发布
            </Button>
          </Popconfirm>
          <Button type="link" onClick={() => void handleCopy(record.id)}>
            复制
          </Button>
          <Popconfirm
            title="确定删除该试卷吗？"
            onConfirm={() => void handleDelete(record.id)}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="试卷管理"
      extra={
        <Button type="primary" onClick={() => navigate("/exam/papers/create")}>
          新建试卷
        </Button>
      }
    >
      {statusOptions.length === 0 || typeOptions.length === 0 ? (
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="后端 /api/v1/enums 暂未返回试卷状态/类型枚举，当前筛选会回退为数字输入。"
        />
      ) : null}

      <Form form={filterForm} layout="inline" initialValues={query}>
        <Form.Item name="titleLike" label="试卷标题">
          <Input placeholder="按试卷标题筛选" allowClear />
        </Form.Item>
        <Form.Item name="status" label="状态">
          {statusOptions.length > 0 ? (
            <Select
              style={{ width: 160 }}
              options={statusOptions}
              allowClear
              placeholder="全部"
            />
          ) : (
            <InputNumber style={{ width: 120 }} min={1} />
          )}
        </Form.Item>
        <Form.Item name="type" label="类型">
          {typeOptions.length > 0 ? (
            <Select
              style={{ width: 160 }}
              options={typeOptions}
              allowClear
              placeholder="全部"
            />
          ) : (
            <InputNumber style={{ width: 120 }} min={1} />
          )}
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
        scroll={{ x: 1100 }}
      />
    </Card>
  );
};

export default ExamPaperList;
