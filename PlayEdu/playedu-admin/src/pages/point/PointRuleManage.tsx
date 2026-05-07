import { useEffect, useState } from "react";
import { Card, message, Spin, Switch, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { pointApi, type PointRule } from "../../api/point";
import styles from "./point.module.less";

const RULE_TYPE_LABEL: Record<string, string> = {
  EXAM_PASS: "考试通过",
  COURSE_COMPLETE: "课程学完",
  TRAIN_COMPLETE: "培训结业",
  LOGIN_DAILY: "每日签到",
};

const PointRuleManagePage = () => {
  const [loading, setLoading] = useState(true);
  const [rules, setRules] = useState<PointRule[]>([]);

  const loadRules = async () => {
    try {
      setLoading(true);
      const res = await pointApi.listRules();
      setRules(res.data || []);
    } catch (error) {
      message.error("加载积分规则失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadRules();
  }, []);

  const handleStatusChange = async (record: PointRule, checked: boolean) => {
    try {
      await pointApi.updateRuleStatus(record.id, checked ? 1 : 0);
      setRules((prev) =>
        prev.map((item) =>
          item.id === record.id ? { ...item, status: checked ? 1 : 0 } : item
        )
      );
      message.success(`规则「${record.name}」已${checked ? "启用" : "禁用"}`);
    } catch (error) {
      message.error("规则状态更新失败");
    }
  };

  const columns: ColumnsType<PointRule> = [
    {
      title: "规则名称",
      dataIndex: "name",
      key: "name",
      width: 220,
    },
    {
      title: "规则类型",
      dataIndex: "ruleType",
      key: "ruleType",
      width: 180,
      render: (value: string) => RULE_TYPE_LABEL[value] || value,
    },
    {
      title: "积分值",
      dataIndex: "points",
      key: "points",
      width: 120,
      render: (value: number) => (
        <Tag color={value >= 0 ? "success" : "error"}>
          {value >= 0 ? "+" : ""}
          {value}
        </Tag>
      ),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: number) => (
        <Tag color={value === 1 ? "success" : "default"}>
          {value === 1 ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 120,
      render: (_value, record) => (
        <Switch
          checked={record.status === 1}
          onChange={(checked) => void handleStatusChange(record, checked)}
        />
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <Card title="积分规则管理">
        <Spin spinning={loading}>
          <Table
            rowKey="id"
            columns={columns}
            dataSource={rules}
            pagination={false}
          />
        </Spin>
      </Card>
    </div>
  );
};

export default PointRuleManagePage;
