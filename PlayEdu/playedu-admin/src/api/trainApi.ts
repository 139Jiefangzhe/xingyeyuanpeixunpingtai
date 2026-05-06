import type { ApiResult, PageResult } from "../types/api";
import type {
  ProjectStatsResp,
  TrainProjectCreateReq,
  TrainProjectDetailResp,
  TrainProjectListResp,
  TrainProjectQuery,
  TrainTaskReq,
} from "../types/train";
import request from "../utils/request";

export const trainApi = {
  listProjects(query: TrainProjectQuery) {
    return request.get<PageResult<TrainProjectListResp>>(
      "/api/v1/train-projects",
      {
        params: query,
      }
    );
  },
  getProjectDetail(id: string) {
    return request.get<TrainProjectDetailResp>(`/api/v1/train-projects/${id}`);
  },
  getProjectStats(id: string) {
    return request.get<ProjectStatsResp>(`/api/v1/train-projects/${id}/stats`);
  },
  createProject(payload: TrainProjectCreateReq, userId: number) {
    return request.post<string, TrainProjectCreateReq>(
      "/api/v1/train-projects",
      payload,
      {
        headers: {
          "X-User-Id": String(userId),
        },
      }
    );
  },
  addTasks(projectId: string, tasks: TrainTaskReq[]) {
    return request.post<void, TrainTaskReq[]>(
      `/api/v1/train-projects/${projectId}/tasks`,
      tasks
    );
  },
  publishProject(id: string) {
    return request.post<void>(`/api/v1/train-projects/${id}/publish`);
  },
} satisfies Record<string, (...args: any[]) => Promise<ApiResult<any>>>;
