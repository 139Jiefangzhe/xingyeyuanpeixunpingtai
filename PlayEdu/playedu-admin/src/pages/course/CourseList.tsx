import { Button, Card, Form, Image, Input, Popconfirm, Select, Space, Table, Tag, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { courseApi } from "../../api/courseApi";
import { useTable } from "../../hooks/useTable";
import type { CourseCategoryOption, CourseQuery, CourseSimpleResp } from "../../types/course";
import { dateFormat } from "../../utils";
import { COURSE_STATUS_OPTIONS, COURSE_TYPE_OPTIONS } from "./manageConstants";

const defaultQuery: CourseQuery = {
  pageNum: 1,
  pageSize: 10,
  sortField: "createdAt",
  sortOrder: "desc",
};

const coverPlaceholder =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='68' viewBox='0 0 120 68'><rect width='120' height='68' fill='%23f5f5f5'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%23999' font-size='12'>No Cover</text></svg>";

const CourseList = () => {
  const navigate = useNavigate();
  const [filterForm] = Form.useForm<CourseQuery>();
  const [categoryOptions, setCategoryOptions] = useState<CourseCategoryOption[]>([]);
  const { data, loading, query, pagination, updateQuery, resetQuery, reload, handleTableChange } =
    useTable<CourseSimpleResp, CourseQuery>(courseApi.listCourses, defaultQuery);

  useEffect(() => {
    courseApi
      .listCategoryOptions()
      .then((result) => setCategoryOptions(result.data || []));
  }, []);

  const categoryMap = useMemo(
    () =>
      categoryOptions.reduce<Record<number, string>>((acc, item) => {
        acc[item.value] = item.label;
        return acc;
      }, {}),
    [categoryOptions]
  );

  const handleSearch = () => {
    const values = filterForm.getFieldsValue();
    updateQuery({
      ...values,
      pageNum: 1,
    });
  };

  const handleReset = () => {
    filterForm.resetFields();
    resetQuery(defaultQuery);
  };

  const handleDelete = async (id: number) => {
    await courseApi.deleteCourse(id, 1);
    message.success("课程删除成功");
    reload();
  };

  const columns: ColumnsType<CourseSimpleResp> = [
    {
      title: "课程名称",
      dataIndex: "title",
      key: "title",
      width: 260,
      render: (value: string, record) => (
        <Button type="link" onClick={() => navigate(`/courses/${record.id}`)}>
          {value}
        </Button>
      ),
    },
    {
      title: "封面",
      dataIndex: "coverUrl",
      key: "coverUrl",
      width: 120,
      render: (value?: string) => (
        <Image
          width={96}
          height={54}
          style={{ objectFit: "cover", borderRadius: 8 }}
          src={value || coverPlaceholder}
          preview={Boolean(value)}
          fallback={coverPlaceholder}
        />
      ),
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (value?: number) => {
        const option = COURSE_TYPE_OPTIONS.find((item) => item.value === value);
        return <Tag color={option?.color}>{option?.label || "未设置"}</Tag>;
      },
    },
    {
      title: "状态",
      dataIndex: "isShow",
      key: "isShow",
      width: 120,
      render: (value: number) => {
        const option = COURSE_STATUS_OPTIONS.find((item) => item.value === value);
        return <Tag color={option?.color}>{option?.label || value}</Tag>;
      },
    },
    {
      title: "分类",
      dataIndex: "categoryIds",
      key: "categoryIds",
      width: 200,
      render: (value: number[]) =>
        value?.length
          ? value.map((item) => <Tag key={item}>{categoryMap[item] || `分类${item}`}</Tag>)
          : "-",
    },
    {
      title: "课时",
      dataIndex: "classHour",
      key: "classHour",
      width: 100,
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (value?: string) => (value ? dateFormat(value) : "-"),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      render: (_value, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/courses/${record.id}`)}>
            编辑
          </Button>
          <Popconfirm
            title="确定删除该课程吗？"
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
    <Card
      title="课程管理"
      extra={
        <Button type="primary" onClick={() => navigate("/courses/create")}>
          新建课程
        </Button>
      }
    >
      <Form form={filterForm} layout="inline" initialValues={query}>
        <Form.Item name="titleLike" label="课程名称">
          <Input placeholder="按课程标题筛选" allowClear />
        </Form.Item>
        <Form.Item name="type" label="类型">
          <Select
            style={{ width: 160 }}
            options={COURSE_TYPE_OPTIONS.map((item) => ({
              label: item.label,
              value: item.value,
            }))}
            allowClear
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item name="categoryId" label="分类">
          <Select
            style={{ width: 160 }}
            options={categoryOptions}
            allowClear
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item name="isShow" label="状态">
          <Select
            style={{ width: 160 }}
            options={COURSE_STATUS_OPTIONS.map((item) => ({
              label: item.label,
              value: item.value,
            }))}
            allowClear
            placeholder="全部"
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" onClick={handleSearch}>
              查询
            </Button>
            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        style={{ marginTop: 16 }}
        rowKey="id"
        loading={loading}
        columns={columns}
        dataSource={data.list}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1400 }}
      />
    </Card>
  );
};

export default CourseList;
