import { trainClient } from "./internal/serviceClients";

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
