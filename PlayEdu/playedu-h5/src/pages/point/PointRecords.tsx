import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Empty, List, NavBar, SpinLoading, Tag, Toast } from "antd-mobile";
import { listRecords, type PointRecord } from "../../api/point";
import styles from "./point.module.scss";

const TYPE_COLOR: Record<string, string> = {
  EXAM_PASS: "#1677ff",
  COURSE_COMPLETE: "#52c41a",
  TRAIN_COMPLETE: "#722ed1",
  LOGIN_DAILY: "#fa8c16",
  EXCHANGE: "#ff4d4f",
  EXAM: "#1677ff",
  COURSE: "#52c41a",
  TRAIN: "#722ed1",
};

const TYPE_LABEL: Record<string, string> = {
  EXAM_PASS: "考试通过",
  COURSE_COMPLETE: "课程学完",
  TRAIN_COMPLETE: "培训结业",
  LOGIN_DAILY: "每日签到",
  EXCHANGE: "积分兑换",
  EXAM: "考试",
  COURSE: "课程",
  TRAIN: "培训",
};

const formatTime = (time: string) => {
  if (!time) {
    return "--";
  }
  const date = new Date(time);
  return `${date.getMonth() + 1}-${date.getDate()} ${String(
    date.getHours()
  ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
};

const PointRecordsPage = () => {
  const navigate = useNavigate();
  const [records, setRecords] = useState<PointRecord[]>([]);
  const [loading, setLoading] = useState(true);

  const loadRecords = async () => {
    try {
      setLoading(true);
      const res: any = await listRecords();
      if (res.code === "0" || res.code === 0) {
        setRecords(res.data || []);
      }
    } catch (error) {
      Toast.show({ icon: "fail", content: "加载失败" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = "积分明细";
    void loadRecords();
  }, []);

  return (
    <div className={styles.pointPage}>
      <NavBar onBack={() => navigate(-1)}>积分明细</NavBar>
      {loading ? (
        <div className={styles.loadingWrap}>
          <SpinLoading color="primary" />
        </div>
      ) : records.length === 0 ? (
        <Empty description="暂无积分记录" />
      ) : (
        <List className={styles.recordList}>
          {records.map((record) => {
            const typeKey = record.ruleType || record.sourceType || "OTHER";
            return (
              <List.Item
                key={record.id}
                prefix={
                  <Tag
                    color={TYPE_COLOR[typeKey] || "#999999"}
                    fill="outline"
                    style={{ fontSize: 11 }}
                  >
                    {TYPE_LABEL[typeKey] || typeKey}
                  </Tag>
                }
                extra={
                  <span
                    className={styles.pointsChange}
                    style={{
                      color: record.points >= 0 ? "#52c41a" : "#ff4d4f",
                    }}
                  >
                    {record.points >= 0 ? "+" : ""}
                    {record.points}
                  </span>
                }
              >
                <div className={styles.recordRemark}>{record.remark || "-"}</div>
                <div className={styles.recordMeta}>
                  <span>余额: {record.balance}</span>
                  <span>{formatTime(record.createTime)}</span>
                </div>
              </List.Item>
            );
          })}
        </List>
      )}
    </div>
  );
};

export default PointRecordsPage;
