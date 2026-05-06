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
  Typography,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { enumApi } from "../../api/enumApi";
import { examApi } from "../../api/examApi";
import { useTable } from "../../hooks/useTable";
import type { EnumDictionary, EnumOption } from "../../types/api";
import type { QuestionQuery, QuestionResp } from "../../types/exam";
import { getEnumLabel } from "../../utils/enumMap";
import { dateFormat } from "../../utils/index";
import QuestionForm from "./QuestionForm";

const QUESTION_TYPE_ENUM_KEY = "questionType";
const QUESTION_DIFFICULTY_ENUM_KEY = "questionDifficulty";

const defaultQuery: QuestionQuery = {
  pageNum: 1,
  pageSize: 10,
  sortField: "createTime",
  sortOrder: "desc",
};

const QuestionList = () => {
  const {
    data,
    query,
    loading,
    updateQuery,
    resetQuery,
    reload,
    handleTableChange,
    pagination,
  } = useTable<QuestionResp, QuestionQuery>(examApi.listQuestions, defaultQuery);
  const [filterForm] = Form.useForm<QuestionQuery>();
  const [enumDictionary, setEnumDictionary] = useState<EnumDictionary>({});
  const [editingQuestion, setEditingQuestion] = useState<QuestionResp | null>(
    null
  );
  const [formVisible, setFormVisible] = useState(false);

  useEffect(() => {
    enumApi
      .getMany([QUESTION_TYPE_ENUM_KEY, QUESTION_DIFFICULTY_ENUM_KEY])
      .then(setEnumDictionary);
  }, []);

  const typeOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_TYPE_ENUM_KEY] || [],
    [enumDictionary]
  );
  const difficultyOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_DIFFICULTY_ENUM_KEY] || [],
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

  const handleDelete = async (id: string) => {
    await examApi.deleteQuestion(id);
    message.success("题目删除成功");
    reload();
  };

  const columns = [
    {
      title: "题干",
      dataIndex: "content",
      key: "content",
      ellipsis: true,
      render: (value: string) => (
        <Typography.Text ellipsis={{ tooltip: value }}>
          {value}
        </Typography.Text>
      ),
    },
    {
      title: "题库",
      dataIndex: "bankId",
      key: "bankId",
      width: 180,
    },
    {
      title: "题型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (value: number) => (
        <Tag>{getEnumLabel(enumDictionary, QUESTION_TYPE_ENUM_KEY, value)}</Tag>
      ),
    },
    {
      title: "难度",
      dataIndex: "difficulty",
      key: "difficulty",
      width: 120,
      render: (value: number) => (
        <Tag>{getEnumLabel(enumDictionary, QUESTION_DIFFICULTY_ENUM_KEY, value)}</Tag>
      ),
    },
    {
      title: "分值",
      dataIndex: "score",
      key: "score",
      width: 100,
    },
    {
      title: "知识点",
      dataIndex: "knowledgePoint",
      key: "knowledgePoint",
      width: 160,
      render: (value: string) => value || "-",
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
      width: 180,
      render: (_: unknown, record: QuestionResp) => (
        <Space>
          <Button
            type="link"
            onClick={() => {
              setEditingQuestion(record);
              setFormVisible(true);
            }}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除该题目吗？"
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
      title="题库管理"
      extra={
        <Button
          type="primary"
          onClick={() => {
            setEditingQuestion(null);
            setFormVisible(true);
          }}
        >
          新建题目
        </Button>
      }
    >
      {typeOptions.length === 0 || difficultyOptions.length === 0 ? (
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="后端 /api/v1/enums 暂未返回题型/难度枚举，当前筛选与编辑会回退为数字输入。"
        />
      ) : null}

      <Form form={filterForm} layout="inline" initialValues={query}>
        <Form.Item name="bankId" label="题库 ID">
          <Input placeholder="bank-java-basic" allowClear />
        </Form.Item>
        <Form.Item name="contentLike" label="题干">
          <Input placeholder="按题干关键词筛选" allowClear />
        </Form.Item>
        <Form.Item name="type" label="题型">
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
        <Form.Item name="difficulty" label="难度">
          {difficultyOptions.length > 0 ? (
            <Select
              style={{ width: 160 }}
              options={difficultyOptions}
              allowClear
              placeholder="全部"
            />
          ) : (
            <InputNumber style={{ width: 120 }} min={1} max={5} />
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
        scroll={{ x: 1200 }}
      />

      <QuestionForm
        visible={formVisible}
        initialValue={editingQuestion}
        onCancel={() => setFormVisible(false)}
        onSuccess={() => {
          setFormVisible(false);
          reload();
        }}
      />
    </Card>
  );
};

export default QuestionList;
