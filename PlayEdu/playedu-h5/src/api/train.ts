import { trainClient } from "./internal/serviceClients";

const LOCAL_USER_ID = String(import.meta.env.VITE_LOCAL_USER_ID || "10005");

export interface TrainProjectListItem {
  id: string;
  title: string;
  type: number;
  status: number;
  startTime?: string;
  endTime?: string;
  taskCount: number;
}

export const listProjects = (params: {
  pageNum: number;
  pageSize: number;
  status?: number;
}) => trainClient.get("/api/v1/train-projects", params);

export interface TrainProjectMyDetail {
  projectId: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  overallProgress: number;
  tasks: TrainTaskItem[];
}

export interface TrainTaskItem {
  taskId: string;
  taskName: string;
  taskType: string;
  sort: number;
  resourceId: string;
  status: string;
  completedAt?: string;
  required: boolean;
}

export const getProjectMyDetail = (projectId: string) =>
  trainClient.request({
    url: `/api/v1/train-projects/${projectId}/my-detail`,
    method: "get",
    headers: {
      "X-User-Id": LOCAL_USER_ID,
    },
  });
