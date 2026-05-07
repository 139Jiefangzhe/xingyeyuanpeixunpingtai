import { useEffect, useState, type CSSProperties, type ReactNode } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Empty,
  List,
  NavBar,
  ProgressBar,
  SpinLoading,
  Tag,
  Toast,
} from "antd-mobile";
import {
  getProjectMyDetail,
  type TrainProjectMyDetail,
  type TrainTaskItem,
} from "../../api/train";
import { dateFormat } from "../../utils";
import styles from "./train.module.scss";

const TASK_ICON: Record<
  string,
  { icon: ReactNode; color: string; label: string }
> = {
  COURSE: { icon: <span>课</span>, color: "#1677ff", label: "课程" },
  EXAM: { icon: <span>考</span>, color: "#ff4d4f", label: "考试" },
  LIVE: { icon: <span>播</span>, color: "#722ed1", label: "直播" },
  ASSIGNMENT: {
    icon: <span>作</span>,
    color: "#fa8c16",
    label: "作业",
  },
};

const STATUS_TAG: Record<string, { text: string; color: string }> = {
  NOT_STARTED: { text: "未开始", color: "#999999" },
  IN_PROGRESS: { text: "进行中", color: "#1677ff" },
  COMPLETED: { text: "已完成", color: "#52c41a" },
  OVERDUE: { text: "已逾期", color: "#ff4d4f" },
};

const TrainDetailPage = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<TrainProjectMyDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const loadDetail = async (currentProjectId: string) => {
    try {
      setLoading(true);
      const res: any = await getProjectMyDetail(currentProjectId);
      if (res.code === "0" || res.code === 0) {
        setDetail(res.data);
        document.title = res.data?.title || "培训详情";
        return;
      }
      Toast.show({ icon: "fail", content: res.msg || "加载失败" });
    } catch (error) {
      Toast.show({ icon: "fail", content: "网络错误，请重试" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!projectId) {
      Toast.show({ icon: "fail", content: "参数错误" });
      setLoading(false);
      return;
    }
    void loadDetail(projectId);
  }, [projectId]);

  const handleTaskClick = (task: TrainTaskItem) => {
    switch (task.taskType) {
      case "EXAM":
        navigate(`/exam/room/${task.resourceId}`);
        break;
      case "COURSE":
        navigate(`/course/${task.resourceId}`);
        break;
      case "LIVE":
        Toast.show({ content: "直播功能即将上线" });
        break;
      case "ASSIGNMENT":
        Toast.show({ content: "作业功能即将上线" });
        break;
      default:
        Toast.show({ content: "未知任务类型" });
        break;
    }
  };

  if (loading) {
    return (
      <div className={styles.detailPage}>
        <NavBar onBack={() => navigate(-1)}>培训详情</NavBar>
        <div style={{ textAlign: "center", padding: 80 }}>
          <SpinLoading color="primary" />
        </div>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className={styles.detailPage}>
        <NavBar onBack={() => navigate(-1)}>培训详情</NavBar>
        <Empty description="加载失败，请刷新重试" />
      </div>
    );
  }

  return (
    <div className={styles.detailPage}>
      <NavBar onBack={() => navigate(-1)}>{detail.title}</NavBar>

      <div className={styles.detailHeader}>
        <div className={styles.detailDesc}>{detail.description || "暂无项目说明"}</div>
        <div className={styles.detailTime}>
          {detail.startTime ? dateFormat(detail.startTime) : "--"} ~{" "}
          {detail.endTime ? dateFormat(detail.endTime) : "--"}
        </div>
        <div className={styles.progressWrap}>
          <div className={styles.progressLabel}>
            <span>总进度</span>
            <span>{detail.overallProgress}%</span>
          </div>
          <ProgressBar
            percent={detail.overallProgress}
            style={{ "--fill-color": "#1677ff" } as CSSProperties}
          />
        </div>
      </div>

      <div className={styles.taskSection}>
        <div className={styles.taskSectionTitle}>学习任务</div>
        {detail.tasks.length === 0 ? (
          <Empty description="暂无学习任务" />
        ) : (
          <List>
            {detail.tasks.map((task, idx) => {
              const iconCfg = TASK_ICON[task.taskType] || TASK_ICON.ASSIGNMENT;
              const statusCfg = STATUS_TAG[task.status] || STATUS_TAG.NOT_STARTED;
              return (
                <List.Item
                  key={task.taskId}
                  prefix={
                    <div
                      className={styles.taskIcon}
                      style={{
                        background: `${iconCfg.color}15`,
                        color: iconCfg.color,
                      }}
                    >
                      {iconCfg.icon}
                    </div>
                  }
                  extra={
                    <div className={styles.taskExtra}>
                      <Tag
                        color={statusCfg.color}
                        fill="outline"
                        style={{ fontSize: 12 }}
                      >
                        {statusCfg.text}
                      </Tag>
                      {task.status === "COMPLETED" ? (
                        <span style={{ color: "#52c41a", fontSize: 16 }}>✓</span>
                      ) : null}
                    </div>
                  }
                  onClick={() => handleTaskClick(task)}
                  arrow={task.status !== "COMPLETED"}
                  className={
                    task.status === "COMPLETED" ? styles.taskCompleted : ""
                  }
                >
                  <div className={styles.taskContent}>
                    <div className={styles.taskStep}>
                      步骤 {task.sort || idx + 1} · {iconCfg.label}
                    </div>
                    <div className={styles.taskName}>
                      {task.taskName}
                      {task.required ? (
                        <span className={styles.requiredTag}>必做</span>
                      ) : null}
                    </div>
                  </div>
                </List.Item>
              );
            })}
          </List>
        )}
      </div>
    </div>
  );
};

export default TrainDetailPage;
