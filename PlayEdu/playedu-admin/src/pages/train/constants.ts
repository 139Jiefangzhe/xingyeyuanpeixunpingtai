export const TRAIN_PROJECT_TYPE_OPTIONS = [
  { label: "新员工培训", value: 1 },
  { label: "岗位晋升", value: 2 },
  { label: "O2O混合", value: 3 },
];

export const TRAIN_PROJECT_STATUS_OPTIONS = [
  { label: "草稿", value: 1, color: "default" },
  { label: "进行中", value: 2, color: "processing" },
  { label: "已完成", value: 3, color: "success" },
];

export const TRAIN_ASSIGNEE_SCOPE_OPTIONS = [
  { label: "全员", value: 1 },
  { label: "部门", value: 2 },
  { label: "指定人员", value: 3 },
];

export const TRAIN_TASK_TYPE_OPTIONS = [
  { label: "课程任务", value: 1 },
  { label: "考试任务", value: 2 },
  { label: "直播任务", value: 3 },
  { label: "作业任务", value: 4 },
];

export const TRAIN_TASK_PASS_RULE_OPTIONS = [
  { label: "查看即完成", value: 1 },
  { label: "需通过考试", value: 2 },
  { label: "需提交作业", value: 3 },
];

export const getOptionLabel = (
  options: Array<{ label: string; value: number | string }>,
  value?: number | string | null
) => {
  if (value == null) {
    return "-";
  }
  return options.find((item) => item.value === value)?.label || String(value);
};
