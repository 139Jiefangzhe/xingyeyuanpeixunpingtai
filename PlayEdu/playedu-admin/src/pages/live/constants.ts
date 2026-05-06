export const LIVE_STATUS_OPTIONS = [
  { label: "未开始", value: 1, color: "default" },
  { label: "直播中", value: 2, color: "processing" },
  { label: "已结束", value: 3, color: "success" },
];

export const getLiveStatusLabel = (value?: number) =>
  LIVE_STATUS_OPTIONS.find((item) => item.value === value)?.label || "-";
