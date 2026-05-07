import { useNavigate } from "react-router-dom";
import { Card, Row, Col, Statistic, Button } from "antd";
import {
  BookOutlined,
  FileTextOutlined,
  VideoCameraOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import styles from "./index.module.less";

export default function DashboardPage() {
  const navigate = useNavigate();

  const modules = [
    {
      title: "培训中心",
      icon: <TeamOutlined style={{ fontSize: 32, color: "#1677ff" }} />,
      path: "/train/projects",
      desc: "创建培训项目、配置任务流、查看学员进度",
    },
    {
      title: "考试中心",
      icon: <FileTextOutlined style={{ fontSize: 32, color: "#ff4d4f" }} />,
      path: "/exam/papers",
      desc: "题库管理、试卷组卷、考试成绩统计",
    },
    {
      title: "课程中心",
      icon: <BookOutlined style={{ fontSize: 32, color: "#52c41a" }} />,
      path: "/courses",
      desc: "课程上架、章节课节配置、分类管理",
    },
    {
      title: "直播中心",
      icon: <VideoCameraOutlined style={{ fontSize: 32, color: "#722ed1" }} />,
      path: "/live/rooms",
      desc: "直播间管理、开播状态控制",
    },
  ];

  return (
    <div className={styles.dashboard}>
      <h2 style={{ fontWeight: 500, marginBottom: 8 }}>欢迎使用企业培训平台</h2>
      <p style={{ color: "#666", marginBottom: 24 }}>
        请从下方选择要管理的模块
      </p>

      <Row gutter={[16, 16]}>
        {modules.map((m) => (
          <Col xs={24} sm={12} lg={12} xl={6} key={m.path}>
            <Card hoverable onClick={() => navigate(m.path)} className={styles.card}>
              <div style={{ textAlign: "center" }}>
                <div style={{ marginBottom: 12 }}>{m.icon}</div>
                <div style={{ fontSize: 16, fontWeight: 500, marginBottom: 4 }}>
                  {m.title}
                </div>
                <div
                  style={{
                    fontSize: 12,
                    color: "#999",
                    lineHeight: 1.5,
                    minHeight: 36,
                  }}
                >
                  {m.desc}
                </div>
                <Button type="primary" block style={{ marginTop: 12 }}>
                  进入管理
                </Button>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Card title="系统状态" style={{ marginTop: 24 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Statistic title="培训项目" value={3} suffix="个" />
          </Col>
          <Col span={6}>
            <Statistic title="在线考试" value={2} suffix="场" />
          </Col>
          <Col span={6}>
            <Statistic title="课程资源" value={5} suffix="门" />
          </Col>
          <Col span={6}>
            <Statistic title="学员人数" value={5} suffix="人" />
          </Col>
        </Row>
        <p style={{ color: "#999", marginTop: 12, fontSize: 12 }}>
          * 统计数据为演示值，正式版本对接 edu-stats-svc 后自动更新
        </p>
      </Card>
    </div>
  );
}
