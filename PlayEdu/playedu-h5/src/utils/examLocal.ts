export interface LocalExamSession {
  paperId: string;
  paperTitle: string;
  recordId: string;
  endAt: number;
  answers: Record<string, string | string[]>;
}

export interface LocalExamResultDetail {
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

export interface LocalExamResult {
  paperId: string;
  paperTitle: string;
  recordId: string;
  passScore: number;
  totalScore: number;
  obtainScore: number;
  status: number;
  startTime?: string;
  submitTime?: string;
  details: LocalExamResultDetail[];
}

const SESSION_PREFIX = "playedu-h5-exam-session:";
const RESULT_PREFIX = "playedu-h5-exam-result:";

export function getLocalExamUserId() {
  return Number(import.meta.env.VITE_LOCAL_USER_ID || "10005");
}

export function getLocalExamSession(paperId: string): LocalExamSession | null {
  const raw = window.localStorage.getItem(SESSION_PREFIX + paperId);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as LocalExamSession;
  } catch (error) {
    window.localStorage.removeItem(SESSION_PREFIX + paperId);
    return null;
  }
}

export function saveLocalExamSession(session: LocalExamSession) {
  window.localStorage.setItem(
    SESSION_PREFIX + session.paperId,
    JSON.stringify(session)
  );
}

export function clearLocalExamSession(paperId: string) {
  window.localStorage.removeItem(SESSION_PREFIX + paperId);
}

export function getLocalExamResult(paperId: string): LocalExamResult | null {
  const raw = window.localStorage.getItem(RESULT_PREFIX + paperId);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as LocalExamResult;
  } catch (error) {
    window.localStorage.removeItem(RESULT_PREFIX + paperId);
    return null;
  }
}

export function saveLocalExamResult(result: LocalExamResult) {
  window.localStorage.setItem(
    RESULT_PREFIX + result.paperId,
    JSON.stringify(result)
  );
}

export function clearLocalExamResult(paperId: string) {
  window.localStorage.removeItem(RESULT_PREFIX + paperId);
}
