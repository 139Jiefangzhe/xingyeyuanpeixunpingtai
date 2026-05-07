import { Card, message, Select, Spin, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { pointApi, type PointOrder } from "../../api/point";
import { dateFormat } from "../../utils";
import styles from "./point.module.less";

const STATUS_LABEL: Record<string, { text: string; color: string }> = {
  PENDING: { text: "待发货", color: "orange" },
  SHIPPED: { text: "已发货", color: "blue" },
  COMPLETED: { text: "已完成", color: "green" },
  CANCELLED: { text: "已取消", color: "default" },
};

const PointOrderManagePage = () => {
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState<PointOrder[]>([]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const res = await pointApi.listOrders();
      setOrders(res.data || []);
    } catch (error) {
      message.error("加载兑换订单失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders();
  }, []);

  const handleStatusChange = async (orderId: string, newStatus: string) => {
    try {
      await pointApi.updateOrderStatus(orderId, newStatus);
      setOrders((prev) =>
        prev.map((item) =>
          item.id === orderId ? { ...item, status: newStatus } : item
        )
      );
      message.success("订单状态更新成功");
    } catch (error) {
      message.error("订单状态更新失败");
    }
  };

  const columns: ColumnsType<PointOrder> = [
    {
      title: "订单号",
      dataIndex: "id",
      key: "id",
      width: 180,
    },
    {
      title: "学员ID",
      dataIndex: "userId",
      key: "userId",
      width: 100,
    },
    {
      title: "商品名称",
      dataIndex: "productName",
      key: "productName",
      width: 220,
    },
    {
      title: "数量",
      dataIndex: "quantity",
      key: "quantity",
      width: 80,
    },
    {
      title: "总积分",
      dataIndex: "totalPoints",
      key: "totalPoints",
      width: 120,
      render: (value: number) => <Tag color="error">{value}</Tag>,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => {
        const matched = STATUS_LABEL[value] || { text: value, color: "default" };
        return <Tag color={matched.color}>{matched.text}</Tag>;
      },
    },
    {
      title: "操作",
      key: "action",
      width: 160,
      render: (_value, record) => (
        <Select
          size="small"
          value={record.status}
          style={{ width: 120 }}
          onChange={(value) => void handleStatusChange(record.id, value)}
          options={[
            { value: "PENDING", label: "待发货" },
            { value: "SHIPPED", label: "已发货" },
            { value: "COMPLETED", label: "已完成" },
            { value: "CANCELLED", label: "已取消" },
          ]}
        />
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 180,
      render: (value?: string) => dateFormat(value || ""),
    },
  ];

  return (
    <div className={styles.page}>
      <Card title="兑换订单管理">
        <Spin spinning={loading}>
          <Table rowKey="id" columns={columns} dataSource={orders} />
        </Spin>
      </Card>
    </div>
  );
};

export default PointOrderManagePage;
