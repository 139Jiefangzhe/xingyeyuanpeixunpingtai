import type { ApiResult, EnumDictionary, EnumOption, PageResult } from "../types/api";
import type {
  ExamPaperCreateReq,
  ExamPaperDetailResp,
  ExamPaperQuery,
  ExamPaperSimpleResp,
  ExamPaperUpdateReq,
  ExamRecordListResp,
  ExamRecordQuery,
  ExamResultResp,
  PaperQuestionReq,
  PaperGenerateReq,
  QuestionCreateReq,
  QuestionQuery,
  QuestionResp,
  QuestionUpdateReq,
} from "../types/exam";

export const isMockEnabled = import.meta.env.VITE_USE_MOCK === "true";

export const fallbackExamEnums: EnumDictionary = {
  questionType: [
    { value: 1, label: "单选题" },
    { value: 2, label: "多选题" },
    { value: 3, label: "判断题" },
    { value: 4, label: "填空题" },
    { value: 5, label: "问答题" },
  ],
  questionDifficulty: [
    { value: 1, label: "极易" },
    { value: 2, label: "容易" },
    { value: 3, label: "中等" },
    { value: 4, label: "困难" },
    { value: 5, label: "极难" },
  ],
  examPaperStatus: [
    { value: 1, label: "草稿" },
    { value: 2, label: "已发布" },
    { value: 3, label: "已归档" },
  ],
  examPaperType: [
    { value: 1, label: "普通考试" },
    { value: 2, label: "随机抽题" },
  ],
};

export const mockPapers: ExamPaperSimpleResp[] = [
  {
    id: "ep-001",
    title: "Java 基础测试",
    totalScore: 100,
    duration: 60,
    status: 2,
    type: 1,
    createTime: "2024-01-15T10:00:00",
  },
  {
    id: "ep-002",
    title: "Spring Boot 进阶",
    totalScore: 100,
    duration: 90,
    status: 1,
    type: 2,
    createTime: "2024-01-14T14:30:00",
  },
];

export const mockQuestions: QuestionResp[] = [
  {
    id: "q-001",
    bankId: "bank-java-basic",
    content: "Java 中 main 方法的正确签名是？",
    type: 1,
    options: [
      { label: "A", value: "public static void main(String[] args)" },
      { label: "B", value: "public void main(String args)" },
      { label: "C", value: "static void main()" },
      { label: "D", value: "public static int main(String[] args)" },
    ],
    answer: "A",
    analysis: "入口方法必须是 public static void main(String[] args)。",
    difficulty: 2,
    knowledgePoint: "基础语法",
    score: 5,
    createTime: "2024-01-15T09:30:00",
    updateTime: "2024-01-15T09:30:00",
  },
  {
    id: "q-002",
    bankId: "bank-spring-core",
    content: "Spring Bean 默认作用域是什么？",
    type: 1,
    options: [
      { label: "A", value: "prototype" },
      { label: "B", value: "request" },
      { label: "C", value: "singleton" },
      { label: "D", value: "session" },
    ],
    answer: "C",
    analysis: "Spring 容器中的 Bean 默认是 singleton。",
    difficulty: 3,
    knowledgePoint: "Spring Core",
    score: 5,
    createTime: "2024-01-15T09:35:00",
    updateTime: "2024-01-15T09:35:00",
  },
];

const mockPaperDetailsSeed: ExamPaperDetailResp[] = [
  {
    id: "ep-001",
    title: "Java 基础测试",
    description: "覆盖 Java 基础语法与 Spring 入门知识",
    totalScore: 100,
    duration: 60,
    passScore: 60,
    status: 2,
    type: 1,
    allowRedo: 0,
    knowledgeConfig: '{"questionTypeRules":{"1":20},"difficultyDistribution":{"2":10,"3":10}}',
    createTime: "2024-01-15T10:00:00",
    updateTime: "2024-01-15T10:10:00",
    questions: [],
  },
  {
    id: "ep-002",
    title: "Spring Boot 进阶",
    description: "用于演示智能组卷和草稿试卷流程",
    totalScore: 100,
    duration: 90,
    passScore: 70,
    status: 1,
    type: 2,
    allowRedo: 1,
    knowledgeConfig:
      '{"questionTypeRules":{"1":10,"2":5},"difficultyDistribution":{"2":8,"3":7},"knowledgePoints":["IOC","AOP"]}',
    createTime: "2024-01-14T14:30:00",
    updateTime: "2024-01-14T14:30:00",
    questions: [],
  },
];

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T;

let paperStore = clone(
  mockPaperDetailsSeed.map((paper, index) => ({
    ...paper,
    questions: index === 0 ? clone(mockQuestions) : clone(mockQuestions.slice(0, 1)),
  }))
);
let questionStore = clone(mockQuestions);

const MOCK_DELAY_MS = 150;

const wait = (duration = MOCK_DELAY_MS) =>
  new Promise((resolve) => {
    window.setTimeout(resolve, duration);
  });

const cloneDate = () => new Date().toISOString();

const buildSuccess = async <T,>(data: T): Promise<ApiResult<T>> => {
  await wait();
  return {
    code: "0",
    msg: "success",
    data,
    version: "v1",
    timestamp: Date.now(),
  };
};

const buildPageResult = <T,>(list: T[], pageNum: number, pageSize: number): PageResult<T> => {
  const start = (pageNum - 1) * pageSize;
  const pageList = list.slice(start, start + pageSize);
  return {
    list: clone(pageList),
    total: list.length,
    pageNum,
    pageSize,
    pages: Math.ceil(list.length / pageSize),
  };
};

const toPaperSimple = (paper: ExamPaperDetailResp): ExamPaperSimpleResp => ({
  id: paper.id,
  title: paper.title,
  type: paper.type,
  status: paper.status,
  totalScore: paper.totalScore,
  duration: paper.duration,
  createTime: paper.createTime,
});

const nextId = (prefix: string) => `${prefix}-${Date.now()}`;

const findPaper = (id: string) => paperStore.find((paper) => paper.id === id);

const requirePaper = (id: string) => {
  const paper = findPaper(id);
  if (!paper) {
    throw new Error(`Mock paper not found: ${id}`);
  }
  return paper;
};

const requireQuestion = (id: string) => {
  const question = questionStore.find((item) => item.id === id);
  if (!question) {
    throw new Error(`Mock question not found: ${id}`);
  }
  return question;
};

export const mockExamApi = {
  async listPapers(query: ExamPaperQuery) {
    const filtered = paperStore
      .filter((paper) => (query.status ? paper.status === query.status : true))
      .filter((paper) => (query.type ? paper.type === query.type : true))
      .filter((paper) =>
        query.titleLike ? paper.title.includes(query.titleLike.trim()) : true
      )
      .sort((left, right) => right.createTime.localeCompare(left.createTime))
      .map(toPaperSimple);
    return buildSuccess(buildPageResult(filtered, query.pageNum, query.pageSize));
  },

  async getPaperById(id: string) {
    return buildSuccess(clone(requirePaper(id)));
  },

  async getPaperDetail(id: string) {
    return buildSuccess(clone(requirePaper(id)));
  },

  async createPaper(payload: ExamPaperCreateReq) {
    const now = cloneDate();
    const id = nextId("ep");
    paperStore.unshift({
      id,
      title: payload.title,
      description: payload.description,
      totalScore: payload.totalScore,
      duration: payload.duration,
      passScore: payload.passScore,
      status: 1,
      type: payload.type,
      allowRedo: payload.allowRedo,
      knowledgeConfig: payload.knowledgeConfig,
      createTime: now,
      updateTime: now,
      questions: [],
    });
    return buildSuccess(id);
  },

  async updatePaper(id: string, payload: ExamPaperUpdateReq) {
    const paper = requirePaper(id);
    Object.assign(paper, payload, {
      updateTime: cloneDate(),
    });
    return buildSuccess(undefined);
  },

  async deletePaper(id: string) {
    paperStore = paperStore.filter((paper) => paper.id !== id);
    return buildSuccess(undefined);
  },

  async publishPaper(id: string) {
    const paper = requirePaper(id);
    paper.status = 2;
    paper.updateTime = cloneDate();
    return buildSuccess(undefined);
  },

  async copyPaper(id: string) {
    const source = requirePaper(id);
    const copyId = nextId("ep");
    const now = cloneDate();
    paperStore.unshift({
      ...clone(source),
      id: copyId,
      title: `${source.title}（副本）`,
      status: 1,
      createTime: now,
      updateTime: now,
    });
    return buildSuccess(copyId);
  },

  async addQuestionsToPaper(paperId: string, questions: PaperQuestionReq[]) {
    const paper = requirePaper(paperId);
    const existingIds = new Set(paper.questions.map((item) => item.id));
    const incomingIds = new Set<string>();
    questions.forEach((item) => {
      incomingIds.add(item.questionId);
    });
    if (incomingIds.size !== questions.length) {
      throw new Error("Duplicate question in request");
    }
    questions.forEach((item) => {
      if (existingIds.has(item.questionId)) {
        throw new Error(`Question already exists in paper: ${item.questionId}`);
      }
      const question = clone(requireQuestion(item.questionId));
      paper.questions.push({
        ...question,
        score: item.score,
      });
    });
    paper.questions = paper.questions.sort((left, right) => {
      const leftOrder =
        questions.find((item) => item.questionId === left.id)?.sort ?? paper.questions.indexOf(left);
      const rightOrder =
        questions.find((item) => item.questionId === right.id)?.sort ?? paper.questions.indexOf(right);
      return leftOrder - rightOrder;
    });
    paper.totalScore = paper.questions.reduce((sum, item) => sum + item.score, 0);
    paper.updateTime = cloneDate();
    return buildSuccess(undefined);
  },

  async removeQuestionsFromPaper(paperId: string, questionIds: string[]) {
    const paper = requirePaper(paperId);
    paper.questions = paper.questions.filter((item) => !questionIds.includes(item.id));
    paper.totalScore = paper.questions.reduce((sum, item) => sum + item.score, 0);
    paper.updateTime = cloneDate();
    return buildSuccess(undefined);
  },

  async reorderPaperQuestions(paperId: string, questionIdsInOrder: string[]) {
    const paper = requirePaper(paperId);
    const orderMap = new Map<string, number>();
    questionIdsInOrder.forEach((id, index) => {
      orderMap.set(id, index);
    });
    paper.questions = clone(paper.questions).sort((left, right) => {
      return (orderMap.get(left.id) ?? Number.MAX_SAFE_INTEGER) - (orderMap.get(right.id) ?? Number.MAX_SAFE_INTEGER);
    });
    paper.totalScore = paper.questions.reduce((sum, item) => sum + item.score, 0);
    paper.updateTime = cloneDate();
    return buildSuccess(undefined);
  },

  async generatePaper(payload: PaperGenerateReq) {
    const id = nextId("ep");
    const now = cloneDate();
    const selectedQuestions = clone(questionStore.slice(0, Math.min(3, questionStore.length)));
    paperStore.unshift({
      id,
      title: payload.title,
      description: "Mock 智能组卷结果",
      totalScore: payload.totalScore,
      duration: payload.duration,
      passScore: payload.passScore,
      status: 1,
      type: 2,
      allowRedo: 0,
      knowledgeConfig: payload.knowledgeConfig,
      createTime: now,
      updateTime: now,
      questions: selectedQuestions,
    });
    return buildSuccess(id);
  },

  async listQuestions(query: QuestionQuery) {
    const filtered = questionStore
      .filter((question) => (query.bankId ? question.bankId === query.bankId : true))
      .filter((question) => (query.type ? question.type === query.type : true))
      .filter((question) =>
        query.difficulty ? question.difficulty === query.difficulty : true
      )
      .filter((question) =>
        query.contentLike ? question.content.includes(query.contentLike.trim()) : true
      )
      .filter((question) =>
        query.knowledgePointLike
          ? (question.knowledgePoint || "").includes(query.knowledgePointLike.trim())
          : true
      )
      .sort((left, right) => right.createTime.localeCompare(left.createTime));
    return buildSuccess(buildPageResult(filtered, query.pageNum, query.pageSize));
  },

  async getQuestionById(id: string) {
    return buildSuccess(clone(requireQuestion(id)));
  },

  async createQuestion(payload: QuestionCreateReq) {
    const now = cloneDate();
    const id = nextId("q");
    questionStore.unshift({
      id,
      bankId: payload.bankId,
      type: payload.type,
      content: payload.content,
      options: payload.options ? JSON.parse(payload.options) : [],
      answer: payload.answer,
      analysis: payload.analysis,
      difficulty: payload.difficulty,
      knowledgePoint: payload.knowledgePoint,
      score: payload.score,
      createTime: now,
      updateTime: now,
    });
    return buildSuccess(id);
  },

  async updateQuestion(id: string, payload: QuestionUpdateReq) {
    const question = requireQuestion(id);
    Object.assign(question, payload, {
      options: payload.options ? JSON.parse(payload.options) : question.options,
      updateTime: cloneDate(),
    });
    return buildSuccess(undefined);
  },

  async deleteQuestion(id: string) {
    questionStore = questionStore.filter((question) => question.id !== id);
    paperStore = paperStore.map((paper) => ({
      ...paper,
      questions: paper.questions.filter((question) => question.id !== id),
    }));
    return buildSuccess(undefined);
  },

  async listExamRecords(query: ExamRecordQuery) {
    const empty: ExamRecordListResp[] = [];
    return buildSuccess(buildPageResult(empty, query.pageNum, query.pageSize));
  },

  async submitExam(recordId: string) {
    const result: ExamResultResp = {
      recordId,
      totalScore: 100,
      obtainScore: 92,
      status: 3,
      startTime: "2024-01-15T09:00:00",
      submitTime: "2024-01-15T09:45:00",
      details: [
        {
          questionId: "q-001",
          score: 5,
          correct: true,
        },
      ],
    };
    return buildSuccess(result);
  },

  async getExamResult(recordId: string) {
    return this.submitExam(recordId);
  },

  async getSessionStatus(recordId: string) {
    return buildSuccess({
      recordId,
      remainingTime: 1800,
      currentQuestionIndex: 2,
      answeredCount: 5,
      totalCount: 20,
      isSubmitted: false,
    });
  },
};
