import { useEffect, useState } from "react";
import { Button, Empty, SpinLoading, Toast } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { exam } from "../../api";
import type { ExamPaperSimple } from "../../api/exam";
import {
  getLocalExamResult,
  getLocalExamSession,
  getLocalExamUserId,
} from "../../utils/examLocal";
import styles from "./exam.module.scss";

type PaperStatusView = {
  label: string;
  actionLabel: string;
  className: string;
  scoreText?: string;
  target: "room" | "result";
};

const resolvePaperStatus = (paperId: string): PaperStatusView => {
  const result = getLocalExamResult(paperId);
  if (result) {
    return {
      label: "已完成",
      actionLabel: "查看成绩",
      className: styles.statusDone,
      scoreText: `${result.obtainScore}/${result.totalScore} 分`,
      target: "result",
    };
  }
  const session = getLocalExamSession(paperId);
  if (session) {
    return {
      label: "进行中",
      actionLabel: "继续答题",
      className: styles.statusDoing,
      target: "room",
    };
  }
  return {
    label: "未开始",
    actionLabel: "进入考试",
    className: styles.statusPending,
    target: "room",
  };
};

const ExamListPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [papers, setPapers] = useState<ExamPaperSimple[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const res: any = await exam.listPapers({
        pageNum: 1,
        pageSize: 50,
        status: 2,
        sortField: "createTime",
        sortOrder: "desc",
      });
      setPapers(res.data.list || []);
    } catch (error) {
      Toast.show({
        content: "考试列表加载失败",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = "我的考试";
    void loadData();
  }, []);

  return (
    <div className={styles.page}>
      <div className={styles.headerCard}>
        <div className={styles.title}>我的考试</div>
        <div className={styles.metaRow}>
          <span>本地联调学员 ID: {getLocalExamUserId()}</span>
          <span>支持开始考试、自动保存、交卷查分</span>
        </div>
      </div>

      {loading ? (
        <div className={styles.centerState}>
          <SpinLoading color="primary" />
        </div>
      ) : papers.length === 0 ? (
        <Empty description="暂无可参加的考试" />
      ) : (
        papers.map((paper) => {
          const paperStatus = resolvePaperStatus(paper.id);
          return (
            <div className={styles.card} key={paper.id}>
              <div className={styles.cardHeader}>
                <div>
                  <div className={styles.title}>{paper.title}</div>
                  <div className={styles.metaRow}>
                    <span>{paper.totalScore} 分</span>
                    <span>{paper.duration} 分钟</span>
                    {paperStatus.scoreText ? <span>{paperStatus.scoreText}</span> : null}
                  </div>
                </div>
                <span className={`${styles.statusTag} ${paperStatus.className}`}>
                  {paperStatus.label}
                </span>
              </div>
              <Button
                block
                color="primary"
                className={styles.primaryButton}
                onClick={() =>
                  navigate(
                    paperStatus.target === "room"
                      ? `/exam/room/${paper.id}`
                      : `/exam/result/${paper.id}`
                  )
                }
              >
                {paperStatus.actionLabel}
              </Button>
            </div>
          );
        })
      )}
    </div>
  );
};

export default ExamListPage;
