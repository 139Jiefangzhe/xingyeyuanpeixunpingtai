export const COURSE_TYPE_OPTIONS = [
  { label: "视频课", value: 1, color: "blue" },
  { label: "文档课", value: 2, color: "gold" },
  { label: "直播课", value: 3, color: "purple" },
];

export const COURSE_STATUS_OPTIONS = [
  { label: "上架", value: 1, color: "green" },
  { label: "隐藏", value: 0, color: "default" },
];

export const getCourseOptionLabel = (
  options: Array<{ label: string; value: number }>,
  value?: number
) => options.find((item) => item.value === value)?.label || "-";
