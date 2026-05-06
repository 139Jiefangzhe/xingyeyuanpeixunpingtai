import { examClient } from "./internal/serviceClients";
import { getLocalExamUserId } from "../utils/examLocal";

export interface ExamPaperListQuery {
  pageNum: number;
  pageSize: number;
  status?: number;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface ExamPaperSimple {
  id: string;
  title: string;
  totalScore: number;
  duration: number;
  status: number;
  type: number;
  createTime?: string;
}

export interface ExamOption {
  label: string;
  value: string;
}

export interface ExamQuestion {
  id: string;
  type: number;
  content: string;
  options?: ExamOption[];
  answer?: string;
  analysis?: string;
  score: number;
}

export interface ExamPaperDetail {
  id: string;
  title: string;
  description?: string;
  totalScore: number;
  duration: number;
  passScore: number;
  status: number;
  questions: ExamQuestion[];
}

export interface ExamResultDetail {
  questionId: string;
  questionType: number;
  content: string;
  score: number;
  obtainScore: number;
  correct?: boolean | null;
  userAnswer?: string;
  correctAnswer?: string;
  pendingManualReview?: boolean;
}

export interface ExamResult {
  recordId: string;
  examId: string;
  paperId: string;
  userId: number;
  totalScore: number;
  obtainScore: number;
  status: number;
  startTime?: string;
  submitTime?: string;
  details: ExamResultDetail[];
}

export const listPapers = (params: ExamPaperListQuery) =>
  examClient.get("/api/v1/exam-papers", params);

export const getPaperById = (paperId: string) =>
  examClient.get(`/api/v1/exam-papers/${paperId}`, {});

export const startExam = (paperId: string) =>
  examClient.request({
    url: "/api/v1/exam-records/start",
    method: "post",
    data: { paperId },
    headers: {
      "X-User-Id": String(getLocalExamUserId()),
    },
  });

export const saveAnswer = (
  recordId: string,
  questionId: string,
  answer: string
) =>
  examClient.post(`/api/v1/exam-records/${recordId}/answers`, {
    questionId,
    answer,
  });

export const submitExam = (recordId: string) =>
  examClient.post(`/api/v1/exam-records/${recordId}/submit`, {});

export const getExamResult = (recordId: string) =>
  examClient.get(`/api/v1/exam-records/${recordId}/result`, {});
