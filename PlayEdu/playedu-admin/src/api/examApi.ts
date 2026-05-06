import type { ApiResult, PageResult } from "../types/api";
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
import { isMockEnabled, mockExamApi } from "./mockExamData";
import request from "../utils/request";

const realExamApi = {
  listPapers(query: ExamPaperQuery) {
    return request.get<PageResult<ExamPaperSimpleResp>>("/api/v1/exam-papers", {
      params: query,
    });
  },
  getPaperById(id: string) {
    return request.get<ExamPaperDetailResp>(`/api/v1/exam-papers/${id}`);
  },
  getPaperDetail(id: string) {
    return request.get<ExamPaperDetailResp>(`/api/v1/exam-papers/${id}`);
  },
  createPaper(payload: ExamPaperCreateReq) {
    return request.post<string, ExamPaperCreateReq>("/api/v1/exam-papers", payload);
  },
  updatePaper(id: string, payload: ExamPaperUpdateReq) {
    return request.put<void, ExamPaperUpdateReq>(`/api/v1/exam-papers/${id}`, payload);
  },
  deletePaper(id: string) {
    return request.delete<void>(`/api/v1/exam-papers/${id}`);
  },
  publishPaper(id: string) {
    return request.post<void>(`/api/v1/exam-papers/${id}/publish`);
  },
  copyPaper(id: string) {
    return request.post<string>(`/api/v1/exam-papers/${id}/copy`);
  },
  addQuestionsToPaper(paperId: string, questions: PaperQuestionReq[]) {
    return request.post<void, PaperQuestionReq[]>(
      `/api/v1/exam-papers/${paperId}/questions`,
      questions
    );
  },
  removeQuestionsFromPaper(paperId: string, questionIds: string[]) {
    return request.delete<void>(`/api/v1/exam-papers/${paperId}/questions`, {
      data: questionIds,
    });
  },
  reorderPaperQuestions(paperId: string, questionIdsInOrder: string[]) {
    return request.put<void, string[]>(
      `/api/v1/exam-papers/${paperId}/questions/reorder`,
      questionIdsInOrder
    );
  },
  generatePaper(payload: PaperGenerateReq) {
    return request.post<string, PaperGenerateReq>("/api/v1/exam-papers/generate", payload);
  },
  listQuestions(query: QuestionQuery) {
    return request.get<PageResult<QuestionResp>>("/api/v1/questions", {
      params: query,
    });
  },
  getQuestionById(id: string) {
    return request.get<QuestionResp>(`/api/v1/questions/${id}`);
  },
  createQuestion(payload: QuestionCreateReq) {
    return request.post<string, QuestionCreateReq>("/api/v1/questions", payload);
  },
  updateQuestion(id: string, payload: QuestionUpdateReq) {
    return request.put<void, QuestionUpdateReq>(`/api/v1/questions/${id}`, payload);
  },
  deleteQuestion(id: string) {
    return request.delete<void>(`/api/v1/questions/${id}`);
  },
  listExamRecords(query: ExamRecordQuery) {
    return request.get<PageResult<ExamRecordListResp>>("/api/v1/exam-records", {
      params: query,
    });
  },
  submitExam(recordId: string) {
    return request.post<ExamResultResp>(`/api/v1/exam-records/${recordId}/submit`);
  },
  getExamResult(recordId: string) {
    return request.get<ExamResultResp>(`/api/v1/exam-records/${recordId}/result`);
  },
  getSessionStatus(recordId: string) {
    return request.get(`/api/v1/exam-sessions/${recordId}/status`);
  },
} satisfies Record<string, (...args: any[]) => Promise<ApiResult<any>>>;

export const examApi = isMockEnabled ? mockExamApi : realExamApi;
