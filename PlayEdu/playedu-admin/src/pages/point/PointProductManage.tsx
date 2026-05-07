import { PlusOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Spin,
  Table,
  Tag,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  pointApi,
  type PointProduct,
  type PointProductSaveReq,
} from "../../api/point";
import { dateFormat } from "../../utils";
import styles from "./point.module.less";

const PointProductManagePage = () => {
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState<PointProduct[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState<PointProduct | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<PointProductSaveReq>();

  const loadProducts = async () => {
    try {
      setLoading(true);
      const res = await pointApi.listProducts();
      setProducts(res.data || []);
    } catch (error) {
      message.error("加载积分商品失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  const handleAdd = () => {
    setEditingProduct(null);
    form.setFieldsValue({
      name: "",
      description: "",
      imageUrl: "",
      pointsPrice: 100,
      stock: 0,
      status: 1,
      sort: 0,
    });
    setModalVisible(true);
  };

  const handleEdit = (record: PointProduct) => {
    setEditingProduct(record);
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      imageUrl: record.imageUrl,
      pointsPrice: record.pointsPrice,
      stock: record.stock,
      status: record.status,
      sort: record.sort,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await pointApi.deleteProduct(id);
      message.success("商品删除成功");
      await loadProducts();
    } catch (error) {
      message.error("商品删除失败");
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      if (editingProduct) {
        await pointApi.updateProduct(editingProduct.id, values);
        message.success("商品更新成功");
      } else {
        await pointApi.createProduct(values);
        message.success("商品创建成功");
      }
      setModalVisible(false);
      setEditingProduct(null);
      form.resetFields();
      await loadProducts();
    } catch (error: any) {
      if (error?.errorFields) {
        return;
      }
      message.error("商品保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  const columns: ColumnsType<PointProduct> = [
    {
      title: "商品名称",
      dataIndex: "name",
      key: "name",
      width: 220,
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "积分价格",
      dataIndex: "pointsPrice",
      key: "pointsPrice",
      width: 130,
      render: (value: number) => <Tag color="error">{value} 积分</Tag>,
    },
    {
      title: "库存",
      dataIndex: "stock",
      key: "stock",
      width: 100,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: number) => (
        <Tag color={value === 1 ? "success" : "default"}>
          {value === 1 ? "上架" : "下架"}
        </Tag>
      ),
    },
    {
      title: "排序",
      dataIndex: "sort",
      key: "sort",
      width: 100,
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: 180,
      render: (value?: string) => dateFormat(value || ""),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      render: (_value, record) => (
        <Space size="small">
          <Button type="link" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该商品吗？"
            onConfirm={() => void handleDelete(record.id)}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <Card
        title="积分商品管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增商品
          </Button>
        }
      >
        <Spin spinning={loading}>
          <Table rowKey="id" columns={columns} dataSource={products} />
        </Spin>
      </Card>

      <Modal
        title={editingProduct ? "编辑商品" : "新增商品"}
        open={modalVisible}
        onOk={() => void handleSubmit()}
        onCancel={() => {
          setModalVisible(false);
          setEditingProduct(null);
        }}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ pointsPrice: 100, stock: 0, status: 1, sort: 0 }}
        >
          <Form.Item
            name="name"
            label="商品名称"
            rules={[{ required: true, message: "请输入商品名称" }]}
          >
            <Input placeholder="如：企业定制笔记本" />
          </Form.Item>
          <Form.Item name="description" label="商品描述">
            <Input.TextArea rows={3} placeholder="商品描述" />
          </Form.Item>
          <Form.Item name="imageUrl" label="商品图片 URL">
            <Input placeholder="https://example.com/product.png" />
          </Form.Item>
          <Form.Item
            name="pointsPrice"
            label="积分价格"
            rules={[{ required: true, message: "请输入积分价格" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="stock"
            label="库存数量"
            rules={[{ required: true, message: "请输入库存数量" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="status" label="商品状态" initialValue={1}>
            <Select
              options={[
                { value: 1, label: "上架" },
                { value: 0, label: "下架" },
              ]}
            />
          </Form.Item>
          <Form.Item name="sort" label="排序号" initialValue={0}>
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PointProductManagePage;
