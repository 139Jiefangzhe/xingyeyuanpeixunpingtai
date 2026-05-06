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
  Form,
  Input,
  InputNumber,
  List,
  Pagination,
  Row,
  Select,
  Space,
  Spin,
  Tag,
  Typography,
  message,
} from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { enumApi } from "../../api/enumApi";
import { examApi } from "../../api/examApi";
import { useTable } from "../../hooks/useTable";
import type { EnumDictionary, EnumOption } from "../../types/api";
import type {
  ComposedQuestionItem,
  ExamPaperDetailResp,
  PaperQuestionReq,
  QuestionQuery,
  QuestionResp,
} from "../../types/exam";
import { getEnumLabel } from "../../utils/enumMap";

const QUESTION_TYPE_ENUM_KEY = "questionType";
const QUESTION_DIFFICULTY_ENUM_KEY = "questionDifficulty";

const defaultQuestionQuery: QuestionQuery = {
  pageNum: 1,
  pageSize: 8,
  sortField: "createTime",
  sortOrder: "desc",
};

const truncate = (value: string, max = 30) =>
  value.length > max ? `${value.slice(0, max)}...` : value;

const clampScore = (value?: number | null) => {
  if (value == null || Number.isNaN(value)) {
    return 1;
  }
  return Math.min(100, Math.max(1, Math.round(value)));
};

const normalizeSelectedQuestions = (
  questions: Array<Omit<ComposedQuestionItem, "sort"> & { sort?: number }>
): ComposedQuestionItem[] =>
  questions.map((item, index) => ({
    ...item,
    sort: index + 1,
    score: clampScore(item.score),
  }));

const buildSelectedQuestions = (paper: ExamPaperDetailResp): ComposedQuestionItem[] =>
  normalizeSelectedQuestions(
    paper.questions.map((question) => ({
      ...question,
      score: clampScore(question.score),
    }))
  );

const ExamPaperCompose = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [filterForm] = Form.useForm<QuestionQuery>();
  const [paperLoading, setPaperLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [paperDetail, setPaperDetail] = useState<ExamPaperDetailResp | null>(null);
  const [selectedQuestions, setSelectedQuestions] = useState<ComposedQuestionItem[]>([]);
  const [savedSnapshot, setSavedSnapshot] = useState<ComposedQuestionItem[]>([]);
  const [enumDictionary, setEnumDictionary] = useState<EnumDictionary>({});
  const {
    data: questionPage,
    query: questionQuery,
    loading: questionLoading,
    updateQuery,
    resetQuery,
  } = useTable<QuestionResp, QuestionQuery>(examApi.listQuestions, defaultQuestionQuery);

  useEffect(() => {
    enumApi
      .getMany([QUESTION_TYPE_ENUM_KEY, QUESTION_DIFFICULTY_ENUM_KEY])
      .then(setEnumDictionary);
  }, []);

  useEffect(() => {
    if (!id) {
      return;
    }
    const loadPaperDetail = async () => {
      setPaperLoading(true);
      try {
        const result = await examApi.getPaperDetail(id);
        const detail = result.data;
        const nextSelected = buildSelectedQuestions(detail);
        setPaperDetail(detail);
        setSelectedQuestions(nextSelected);
        setSavedSnapshot(nextSelected);
      } finally {
        setPaperLoading(false);
      }
    };
    void loadPaperDetail();
  }, [id]);

  const typeOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_TYPE_ENUM_KEY] || [],
    [enumDictionary]
  );
  const difficultyOptions = useMemo<EnumOption[]>(
    () => enumDictionary[QUESTION_DIFFICULTY_ENUM_KEY] || [],
    [enumDictionary]
  );
  const selectedQuestionIds = useMemo(
    () => new Set(selectedQuestions.map((item) => item.id)),
    [selectedQuestions]
  );
  const totalScore = useMemo(
    () => selectedQuestions.reduce((sum, item) => sum + clampScore(item.score), 0),
    [selectedQuestions]
  );
  const isDraft = paperDetail?.status === 1;

  const handleSearch = () => {
    const values = filterForm.getFieldsValue();
    updateQuery({
      ...values,
      pageNum: 1,
    });
  };

  const handleReset = () => {
    filterForm.resetFields();
    resetQuery(defaultQuestionQuery);
  };

  const handleAddQuestion = (question: QuestionResp) => {
    if (!isDraft) {
      return;
    }
    if (selectedQuestionIds.has(question.id)) {
      message.warning("该题目已加入试卷");
      return;
    }
    setSelectedQuestions((current) =>
      normalizeSelectedQuestions([
        ...current,
        {
          ...question,
          sort: current.length + 1,
          score: clampScore(question.score),
        },
      ])
    );
  };

  const handleRemoveQuestion = (questionId: string) => {
    if (!isDraft) {
      return;
    }
    setSelectedQuestions((current) =>
      normalizeSelectedQuestions(current.filter((item) => item.id !== questionId))
    );
  };

  const handleMoveQuestion = (questionId: string, direction: "up" | "down") => {
    if (!isDraft) {
      return;
    }
    setSelectedQuestions((current) => {
      const index = current.findIndex((item) => item.id === questionId);
      if (index < 0) {
        return current;
      }
      const targetIndex = direction === "up" ? index - 1 : index + 1;
      if (targetIndex < 0 || targetIndex >= current.length) {
        return current;
      }
      const next = [...current];
      const [item] = next.splice(index, 1);
      next.splice(targetIndex, 0, item);
      return normalizeSelectedQuestions(next);
    });
  };

  const handleScoreChange = (questionId: string, value: number | null) => {
    if (!isDraft) {
      return;
    }
    setSelectedQuestions((current) =>
      current.map((item) =>
        item.id === questionId
          ? {
              ...item,
              score: clampScore(value),
            }
          : item
      )
    );
  };

  const syncPaperDetail = async () => {
    if (!id) {
      return;
    }
    const result = await examApi.getPaperDetail(id);
    const detail = result.data;
    const nextSelected = buildSelectedQuestions(detail);
    setPaperDetail(detail);
    setSelectedQuestions(nextSelected);
    setSavedSnapshot(nextSelected);
  };

  const persistComposition = async () => {
    if (!id) {
      return;
    }
    const currentPayload: PaperQuestionReq[] = selectedQuestions.map((item, index) => ({
      questionId: item.id,
      sort: index + 1,
      score: clampScore(item.score),
    }));
    const savedMap = new Map(savedSnapshot.map((item) => [item.id, item]));
    const currentMap = new Map(selectedQuestions.map((item) => [item.id, item]));

    const removedIds = savedSnapshot
      .filter((item) => {
        const current = currentMap.get(item.id);
        return !current || clampScore(current.score) !== clampScore(item.score);
      })
      .map((item) => item.id);

    const addedOrChanged = currentPayload.filter((item) => {
      const saved = savedMap.get(item.questionId);
      return !saved || clampScore(saved.score) !== clampScore(item.score);
    });

    const currentOrder = selectedQuestions.map((item) => item.id);
    const savedOrder = savedSnapshot.map((item) => item.id);
    const orderChanged =
      currentOrder.length !== savedOrder.length ||
      currentOrder.some((item, index) => item !== savedOrder[index]);

    if (removedIds.length > 0) {
      await examApi.removeQuestionsFromPaper(id, removedIds);
    }
    if (addedOrChanged.length > 0) {
      await examApi.addQuestionsToPaper(id, addedOrChanged);
    }
    if (currentOrder.length > 0 && (orderChanged || removedIds.length > 0 || addedOrChanged.length > 0)) {
      await examApi.reorderPaperQuestions(id, currentOrder);
    }
  };

  const handleSave = async (publishAfterSave: boolean) => {
    if (!id || !isDraft) {
      return;
    }
    setSubmitting(true);
    try {
      await persistComposition();
      if (publishAfterSave) {
        await examApi.publishPaper(id);
        message.success("试卷编排并发布成功");
        navigate("/exam/papers");
        return;
      }
      await syncPaperDetail();
      message.success("试卷编排已保存");
    } finally {
      setSubmitting(false);
    }
  };

  if (!id) {
    return (
      <Card>
        <Alert type="error" showIcon message="缺少试卷 ID，无法进入编排页面。" />
      </Card>
    );
  }

  return (
    <Spin spinning={paperLoading}>
      <Row gutter={16} align="stretch">
        <Col span={10}>
          <Card title="题库选题区" bordered={false}>
            {typeOptions.length === 0 || difficultyOptions.length === 0 ? (
              <Alert
                type="warning"
                showIcon
                style={{ marginBottom: 16 }}
                message="后端枚举未完整返回，筛选将回退为基础输入。"
              />
            ) : null}

            <Form form={filterForm} layout="vertical" initialValues={questionQuery}>
              <Form.Item name="type" label="题型">
                <Select options={typeOptions} placeholder="全部题型" allowClear />
              </Form.Item>
              <Form.Item name="difficulty" label="难度">
                <Select options={difficultyOptions} placeholder="全部难度" allowClear />
              </Form.Item>
              <Form.Item name="knowledgePointLike" label="知识点关键词">
                <Input placeholder="例如：Spring / JVM" allowClear />
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

            <List
              loading={questionLoading}
              locale={{ emptyText: <Empty description="暂无可选题目" /> }}
              dataSource={questionPage.list}
              renderItem={(question) => (
                <List.Item
                  actions={[
                    <Button
                      key="add"
                      type="link"
                      icon={<PlusOutlined />}
                      disabled={!isDraft || selectedQuestionIds.has(question.id)}
                      onClick={() => handleAddQuestion(question)}
                    >
                      {selectedQuestionIds.has(question.id) ? "已添加" : "添加"}
                    </Button>,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Space size={8} wrap>
                        <Typography.Text>{truncate(question.content)}</Typography.Text>
                        <Tag>{getEnumLabel(enumDictionary, QUESTION_TYPE_ENUM_KEY, question.type)}</Tag>
                        <Tag>{getEnumLabel(enumDictionary, QUESTION_DIFFICULTY_ENUM_KEY, question.difficulty)}</Tag>
                        <Tag color="blue">{question.score} 分</Tag>
                      </Space>
                    }
                    description={`知识点：${question.knowledgePoint || "-"}`}
                  />
                </List.Item>
              )}
            />

            <Pagination
              style={{ marginTop: 16, textAlign: "right" }}
              current={questionPage.pageNum}
              pageSize={questionPage.pageSize}
              total={questionPage.total}
              showSizeChanger
              onChange={(page, pageSize) =>
                updateQuery({
                  pageNum: page,
                  pageSize,
                })
              }
            />
          </Card>
        </Col>

        <Col span={14}>
          <Card
            bordered={false}
            title={
              <Space direction="vertical" size={2}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  {paperDetail?.title || "试卷编排"}
                </Typography.Title>
                <Typography.Text type="secondary">
                  当前总分：{totalScore} 分，已选 {selectedQuestions.length} 题
                </Typography.Text>
              </Space>
            }
            extra={
              !isDraft ? (
                <Tag color="gold">当前试卷非草稿状态，仅支持查看</Tag>
              ) : null
            }
          >
            {!isDraft ? (
              <Alert
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
                message="已发布试卷不允许继续调整题目编排。如需修改，请先复制为草稿再操作。"
              />
            ) : null}

            <List
              locale={{ emptyText: <Empty description="右侧暂无已选题目" /> }}
              dataSource={selectedQuestions}
              renderItem={(question, index) => (
                <List.Item
                  actions={[
                    <InputNumber
                      key="score"
                      min={1}
                      max={100}
                      precision={0}
                      value={clampScore(question.score)}
                      disabled={!isDraft}
                      onChange={(value) => handleScoreChange(question.id, value)}
                    />,
                    <Button
                      key="up"
                      type="text"
                      icon={<ArrowUpOutlined />}
                      disabled={!isDraft || index === 0}
                      onClick={() => handleMoveQuestion(question.id, "up")}
                    />,
                    <Button
                      key="down"
                      type="text"
                      icon={<ArrowDownOutlined />}
                      disabled={!isDraft || index === selectedQuestions.length - 1}
                      onClick={() => handleMoveQuestion(question.id, "down")}
                    />,
                    <Button
                      key="delete"
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      disabled={!isDraft}
                      onClick={() => handleRemoveQuestion(question.id)}
                    />,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Space size={8} wrap>
                        <Tag color="processing">{index + 1}</Tag>
                        <Typography.Text>{truncate(question.content)}</Typography.Text>
                      </Space>
                    }
                    description={
                      <Space size={8} wrap>
                        <Tag>{getEnumLabel(enumDictionary, QUESTION_TYPE_ENUM_KEY, question.type)}</Tag>
                        <Tag>{getEnumLabel(enumDictionary, QUESTION_DIFFICULTY_ENUM_KEY, question.difficulty)}</Tag>
                        <Typography.Text type="secondary">
                          知识点：{question.knowledgePoint || "-"}
                        </Typography.Text>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />

            <Space style={{ marginTop: 24 }}>
              <Button onClick={() => navigate("/exam/papers")}>取消</Button>
              <Button
                type="primary"
                loading={submitting}
                disabled={!isDraft}
                onClick={() => void handleSave(false)}
              >
                保存编排
              </Button>
              <Button
                type="primary"
                ghost
                loading={submitting}
                disabled={!isDraft || selectedQuestions.length === 0}
                onClick={() => void handleSave(true)}
              >
                保存并发布
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>
    </Spin>
  );
};

export default ExamPaperCompose;
