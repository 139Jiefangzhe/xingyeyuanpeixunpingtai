import { useEffect, useMemo, useRef, useState } from "react";
import { Button, Dialog, SpinLoading, Toast } from "antd-mobile";
import { useNavigate, useParams } from "react-router-dom";
import { exam } from "../../api";
import type {
  ExamOption,
  ExamPaperDetail,
  ExamQuestion,
} from "../../api/exam";
import styles from "./exam.module.scss";
import {
  clearLocalExamSession,
  getLocalExamResult,
  getLocalExamSession,
  saveLocalExamResult,
  saveLocalExamSession,
  type LocalExamResult,
  type LocalExamSession,
} from "../../utils/examLocal";

type AnswerValue = string | string[];

const serializeAnswer = (value: AnswerValue | undefined) => {
  if (Array.isArray(value)) {
    return value.join(",");
  }
  return value || "";
};

const getQuestionOptions = (question: ExamQuestion): ExamOption[] => {
  if (Array.isArray(question.options)) {
    return question.options;
  }
  if (question.type === 3) {
    return [
      { label: "true", value: "正确" },
      { label: "false", value: "错误" },
    ];
  }
  return [];
};

const resolveFirstPendingIndex = (
  questions: ExamQuestion[],
  answers: Record<string, AnswerValue>
) => {
  const index = questions.findIndex((item) => {
    const answer = answers[item.id];
    if (Array.isArray(answer)) {
      return answer.length === 0;
    }
    return !answer;
  });
  return index >= 0 ? index : 0;
};

const ExamRoomPage = () => {
  const navigate = useNavigate();
  const { paperId = "" } = useParams();
  const [loading, setLoading] = useState(true);
  const [paper, setPaper] = useState<ExamPaperDetail | null>(null);
  const [session, setSession] = useState<LocalExamSession | null>(null);
  const [answers, setAnswers] = useState<Record<string, AnswerValue>>({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const saveTimerRef = useRef<number>();
  const submitLockRef = useRef(false);

  const currentQuestion = paper?.questions?.[currentIndex];

  const updateRemainingTime = (endAt: number) => {
    setRemainingSeconds(Math.max(0, Math.floor((endAt - Date.now()) / 1000)));
  };

  const loadPaperAndSession = async () => {
    setLoading(true);
    try {
      const paperRes: any = await exam.getPaperById(paperId);
      const paperDetail = paperRes.data as ExamPaperDetail;
      setPaper(paperDetail);

      let localSession = getLocalExamSession(paperId);
      if (!localSession) {
        const startRes: any = await exam.startExam(paperId);
        localSession = {
          paperId,
          paperTitle: paperDetail.title,
          recordId: startRes.data,
          endAt: Date.now() + paperDetail.duration * 60 * 1000,
          answers: {},
        };
        saveLocalExamSession(localSession);
      }
      setSession(localSession);
      setAnswers(localSession.answers || {});
      setCurrentIndex(resolveFirstPendingIndex(paperDetail.questions || [], localSession.answers || {}));
      updateRemainingTime(localSession.endAt);
    } catch (error: any) {
      Toast.show({
        content: error?.msg || "考试初始化失败",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = "考试答题";
    if (getLocalExamResult(paperId)) {
      navigate(`/exam/result/${paperId}`, { replace: true });
      return;
    }
    void loadPaperAndSession();
    return () => {
      if (saveTimerRef.current) {
        window.clearTimeout(saveTimerRef.current);
      }
    };
  }, [paperId]);

  useEffect(() => {
    if (!session) {
      return;
    }
    const timer = window.setInterval(() => {
      updateRemainingTime(session.endAt);
    }, 1000);
    return () => window.clearInterval(timer);
  }, [session]);

  useEffect(() => {
    if (remainingSeconds <= 0 && session && !submitLockRef.current) {
      void handleSubmit(true);
    }
  }, [remainingSeconds, session]);

  const syncAnswer = (questionId: string, answer: AnswerValue) => {
    if (!session) {
      return;
    }
    const nextAnswers = {
      ...answers,
      [questionId]: answer,
    };
    setAnswers(nextAnswers);
    const nextSession = {
      ...session,
      answers: nextAnswers,
    };
    setSession(nextSession);
    saveLocalExamSession(nextSession);
    if (saveTimerRef.current) {
      window.clearTimeout(saveTimerRef.current);
    }
    saveTimerRef.current = window.setTimeout(async () => {
      try {
        await exam.saveAnswer(questionId ? session.recordId : "", questionId, serializeAnswer(answer));
      } catch (error) {
        Toast.show({
          content: "答案自动保存失败",
        });
      }
    }, 300);
  };

  const handleSingleSelect = (questionId: string, optionLabel: string) => {
    syncAnswer(questionId, optionLabel);
  };

  const handleMultiToggle = (questionId: string, optionLabel: string) => {
    const current = Array.isArray(answers[questionId]) ? (answers[questionId] as string[]) : [];
    const exists = current.includes(optionLabel);
    const next = exists
      ? current.filter((item) => item !== optionLabel)
      : [...current, optionLabel];
    syncAnswer(questionId, next);
  };

  const handleTextAnswer = (questionId: string, value: string) => {
    syncAnswer(questionId, value);
  };

  const handleSubmit = async (silent?: boolean) => {
    if (!session || !paper || submitLockRef.current) {
      return;
    }
    if (!silent) {
      const confirmed = await Dialog.confirm({
        content: "确认提交试卷吗？提交后将立即评分。",
      });
      if (!confirmed) {
        return;
      }
    }
    submitLockRef.current = true;
    setSubmitting(true);
    try {
      const resultRes: any = await exam.submitExam(session.recordId);
      const result = resultRes.data;
      const localResult: LocalExamResult = {
        paperId,
        paperTitle: paper.title,
        recordId: result.recordId,
        passScore: paper.passScore,
        totalScore: result.totalScore,
        obtainScore: result.obtainScore,
        status: result.status,
        startTime: result.startTime,
        submitTime: result.submitTime,
        details: result.details || [],
      };
      saveLocalExamResult(localResult);
      clearLocalExamSession(paperId);
      navigate(`/exam/result/${paperId}`, { replace: true });
    } catch (error: any) {
      submitLockRef.current = false;
      Toast.show({
        content: error?.msg || "交卷失败",
      });
    } finally {
      setSubmitting(false);
    }
  };

  const answeredQuestionIds = useMemo(
    () =>
      Object.keys(answers).filter((key) => {
        const value = answers[key];
        return Array.isArray(value) ? value.length > 0 : Boolean(value);
      }),
    [answers]
  );

  if (loading) {
    return (
      <div className={styles.centerState}>
        <SpinLoading color="primary" />
      </div>
    );
  }

  if (!paper || !session || !currentQuestion) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>考试数据加载失败，请返回重试。</div>
        <Button block onClick={() => navigate("/exam", { replace: true })}>
          返回考试列表
        </Button>
      </div>
    );
  }

  const currentValue = answers[currentQuestion.id];
  const optionList = getQuestionOptions(currentQuestion);

  return (
    <div className={styles.page}>
      <div className={styles.headerCard}>
        <div className={styles.title}>{paper.title}</div>
        <div className={styles.questionMeta}>
          <span>
            第 {currentIndex + 1} / {paper.questions.length} 题
          </span>
          <span
            className={`${styles.countdown} ${
              remainingSeconds <= 300 ? styles.countdownDanger : ""
            }`}
          >
            剩余 {Math.floor(remainingSeconds / 60)}:{String(remainingSeconds % 60).padStart(2, "0")}
          </span>
        </div>
      </div>

      <div className={styles.card}>
        <div className={styles.questionMeta}>
          <span>题型 {currentQuestion.type}</span>
          <span>{currentQuestion.score} 分</span>
        </div>
        <div className={styles.questionTitle}>{currentQuestion.content}</div>

        {currentQuestion.type === 2 ? (
          <>
            <div className={styles.bottomActions} style={{ marginBottom: 12 }}>
              <Button
                size="small"
                className={styles.flexButton}
                onClick={() =>
                  syncAnswer(
                    currentQuestion.id,
                    optionList.map((item) => item.label)
                  )
                }
              >
                全选
              </Button>
              <Button
                size="small"
                className={styles.flexButton}
                onClick={() => syncAnswer(currentQuestion.id, [])}
              >
                清除
              </Button>
            </div>
            <div className={styles.optionList}>
              {optionList.map((option) => {
                const checked = Array.isArray(currentValue)
                  ? currentValue.includes(option.label)
                  : false;
                return (
                  <button
                    key={option.label}
                    className={`${styles.optionItem} ${checked ? styles.optionChecked : ""}`}
                    onClick={() => handleMultiToggle(currentQuestion.id, option.label)}
                  >
                    <span className={`${styles.optionMark} ${styles.optionMarkSquare}`}>
                      {option.label}
                    </span>
                    <span className={styles.optionBody}>
                      <div className={styles.optionLabel}>{option.label}</div>
                      <div className={styles.optionValue}>{option.value}</div>
                    </span>
                  </button>
                );
              })}
            </div>
          </>
        ) : optionList.length > 0 ? (
          <div className={styles.optionList}>
            {optionList.map((option) => {
              const checked = currentValue === option.label;
              return (
                <button
                  key={option.label}
                  className={`${styles.optionItem} ${checked ? styles.optionChecked : ""}`}
                  onClick={() => handleSingleSelect(currentQuestion.id, option.label)}
                >
                  <span className={styles.optionMark}>{option.label}</span>
                  <span className={styles.optionBody}>
                    <div className={styles.optionLabel}>{option.label}</div>
                    <div className={styles.optionValue}>{option.value}</div>
                  </span>
                </button>
              );
            })}
          </div>
        ) : (
          <textarea
            className={styles.textarea}
            placeholder="请输入答案"
            value={typeof currentValue === "string" ? currentValue : ""}
            onChange={(event) => handleTextAnswer(currentQuestion.id, event.target.value)}
          />
        )}
      </div>

      <div className={styles.fixedBottom}>
        <div className={styles.numberGrid}>
          {paper.questions.map((item: ExamQuestion, index: number) => {
            const answered = answeredQuestionIds.includes(item.id);
            return (
              <button
                key={item.id}
                className={`${styles.numberButton} ${
                  index === currentIndex
                    ? styles.numberCurrent
                    : answered
                    ? styles.numberAnswered
                    : ""
                }`}
                onClick={() => setCurrentIndex(index)}
              >
                {index + 1}
              </button>
            );
          })}
        </div>
        <div className={styles.bottomActions}>
          <Button
            className={styles.flexButton}
            disabled={currentIndex === 0}
            onClick={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
          >
            上一题
          </Button>
          <Button
            className={styles.flexButton}
            disabled={currentIndex >= paper.questions.length - 1}
            onClick={() =>
              setCurrentIndex((prev) => Math.min(paper.questions.length - 1, prev + 1))
            }
          >
            下一题
          </Button>
          <Button
            color="primary"
            className={`${styles.flexButton} ${styles.primaryButton}`}
            loading={submitting}
            onClick={() => void handleSubmit()}
          >
            交卷
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ExamRoomPage;
