export interface ExamPaperQuery {
  pageNum: number;
  pageSize: number;
  status?: number;
  type?: number;
  titleLike?: string;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface ExamPaperSimpleResp {
  id: string;
  title: string;
  type: number;
  status: number;
  totalScore: number;
  duration: number;
  createTime: string;
}

export interface QuestionResp {
  id: string;
  bankId: string;
  type: number;
  content: string;
  options: unknown;
  answer: string;
  analysis?: string;
  difficulty: number;
  knowledgePoint?: string;
  score: number;
  createTime: string;
  updateTime: string;
}

export interface PaperQuestionReq {
  questionId: string;
  sort: number;
  score: number;
}

export interface ComposedQuestionItem extends QuestionResp {
  sort: number;
}

export interface ExamPaperDetailResp {
  id: string;
  title: string;
  description?: string;
  totalScore: number;
  duration: number;
  passScore: number;
  status: number;
  type: number;
  allowRedo: number;
  knowledgeConfig?: string;
  createTime: string;
  updateTime: string;
  questions: QuestionResp[];
}

export interface ExamPaperCreateReq {
  title: string;
  description?: string;
  duration: number;
  passScore: number;
  totalScore: number;
  type: number;
  allowRedo: number;
  knowledgeConfig?: string;
}

export interface ExamPaperUpdateReq {
  title?: string;
  description?: string;
  duration?: number;
  passScore?: number;
  totalScore?: number;
  type?: number;
  allowRedo?: number;
  knowledgeConfig?: string;
}

export interface PaperGenerateReq {
  title: string;
  bankId: string;
  knowledgeConfig: string;
  totalScore: number;
  duration: number;
  passScore: number;
}

export interface QuestionQuery {
  pageNum: number;
  pageSize: number;
  bankId?: string;
  type?: number;
  difficulty?: number;
  knowledgePointLike?: string;
  contentLike?: string;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface QuestionCreateReq {
  bankId: string;
  type: number;
  content: string;
  options?: string;
  answer: string;
  analysis?: string;
  difficulty: number;
  knowledgePoint?: string;
  score: number;
}

export interface QuestionUpdateReq {
  bankId?: string;
  type?: number;
  content?: string;
  options?: string;
  answer?: string;
  analysis?: string;
  difficulty?: number;
  knowledgePoint?: string;
  score?: number;
}

export interface ExamRecordQuery {
  pageNum: number;
  pageSize: number;
  examId?: string;
  userId?: number;
  deptId?: number;
  status?: number;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface ExamRecordListResp {
  id: string;
  examId: string;
  paperId: string;
  userId: number;
  totalScore: number;
  obtainScore: number;
  status: number;
  startTime: string;
  submitTime?: string;
}

export interface ExamResultResp {
  recordId: string;
  totalScore: number;
  obtainScore: number;
  status: number;
  startTime: string;
  submitTime?: string;
  details: Array<{
    questionId: string;
    score: number;
    correct: boolean;
    pendingManualReview?: boolean;
  }>;
}
