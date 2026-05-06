import { useEffect, useState } from "react";
import { Button, Empty } from "antd-mobile";
import { useNavigate, useParams } from "react-router-dom";
import styles from "./exam.module.scss";
import { getLocalExamResult, type LocalExamResult } from "../../utils/examLocal";

const ExamResultPage = () => {
  const navigate = useNavigate();
  const { paperId = "" } = useParams();
  const [result, setResult] = useState<LocalExamResult | null>(null);

  useEffect(() => {
    document.title = "考试成绩";
    setResult(getLocalExamResult(paperId));
  }, [paperId]);

  if (!result) {
    return (
      <div className={styles.page}>
        <Empty description="暂无成绩数据" />
        <Button block color="primary" onClick={() => navigate("/exam", { replace: true })}>
          返回考试列表
        </Button>
      </div>
    );
  }

  const passed = result.obtainScore >= result.passScore;

  return (
    <div className={styles.page}>
      <div className={styles.headerCard}>
        <div className={styles.title}>{result.paperTitle}</div>
        <div className={styles.metaRow}>
          <span>考试记录: {result.recordId}</span>
          <span>提交时间: {result.submitTime || "-"}</span>
        </div>
        <div className={styles.scoreBox}>
          <div className={styles.scoreCard}>
            <div className={styles.scoreLabel}>获得分数</div>
            <div className={styles.scoreValue}>{result.obtainScore}</div>
          </div>
          <div className={styles.scoreCard}>
            <div className={styles.scoreLabel}>结果</div>
            <div className={styles.scoreValue}>{passed ? "通过" : "未通过"}</div>
          </div>
        </div>
      </div>

      <div className={styles.sectionTitle}>答题回顾</div>
      {result.details.map((item, index) => (
        <div className={styles.card} key={item.questionId}>
          <div className={styles.cardHeader}>
            <div>
              <div className={styles.optionLabel}>第 {index + 1} 题</div>
              <div className={styles.optionValue}>{item.content}</div>
            </div>
            <span
              className={`${styles.statusTag} ${
                item.pendingManualReview
                  ? styles.statusDoing
                  : item.correct
                  ? styles.statusDone
                  : styles.statusPending
              }`}
            >
              {item.pendingManualReview ? "待阅卷" : item.correct ? "正确" : "错误"}
            </span>
          </div>
          <div className={styles.answerLine}>你的答案：{item.userAnswer || "-"}</div>
          <div className={styles.answerLine}>正确答案：{item.correctAnswer || "-"}</div>
          <div className={styles.answerLine}>得分：{item.obtainScore} / {item.score}</div>
        </div>
      ))}

      <Button block color="primary" className={styles.primaryButton} onClick={() => navigate("/exam", { replace: true })}>
        返回考试列表
      </Button>
    </div>
  );
};

export default ExamResultPage;
