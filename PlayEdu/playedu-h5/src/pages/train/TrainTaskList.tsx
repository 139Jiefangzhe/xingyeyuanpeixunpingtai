import { useEffect, useState } from "react";
import { Button, Empty, SpinLoading, Toast } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { train } from "../../api";
import type { TrainProjectListItem } from "../../api/train";
import { dateFormat } from "../../utils";
import styles from "./train.module.scss";

const TrainTaskListPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState<TrainProjectListItem[]>([]);

  const loadProjects = async () => {
    setLoading(true);
    try {
      const res: any = await train.listProjects({
        pageNum: 1,
        pageSize: 20,
        status: 2,
      });
      setProjects(res.data.list || []);
    } catch (error) {
      Toast.show({
        content: "培训任务加载失败",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = "培训任务";
    void loadProjects();
  }, []);

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.title}>培训任务</div>
        <div className={styles.desc}>
          当前移动端优先开放考试参与链路。已发布培训项目会在这里聚合展示，考试任务可直接从下方入口进入。
        </div>
      </div>

      {loading ? (
        <div className={styles.hero} style={{ textAlign: "center" }}>
          <SpinLoading color="primary" />
        </div>
      ) : projects.length === 0 ? (
        <Empty description="暂无进行中的培训项目" />
      ) : (
        projects.map((item) => (
          <div className={styles.card} key={item.id}>
            <div className={styles.cardTitle}>{item.title}</div>
            <div className={styles.meta}>
              <span>任务数 {item.taskCount}</span>
              {item.startTime ? <span>开始 {dateFormat(item.startTime)}</span> : null}
              {item.endTime ? <span>结束 {dateFormat(item.endTime)}</span> : null}
            </div>
            <Button block color="primary" onClick={() => navigate("/exam")}>
              进入考试入口
            </Button>
          </div>
        ))
      )}
    </div>
  );
};

export default TrainTaskListPage;
