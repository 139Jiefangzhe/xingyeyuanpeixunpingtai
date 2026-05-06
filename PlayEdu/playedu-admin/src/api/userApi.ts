import type { DepartmentTreeResp, UserResp } from "../types/user";
import request from "../utils/request";

export const userApi = {
  getUserById(id: number) {
    return request.get<UserResp>(`/api/v1/users/${id}`);
  },
  batchGetUsers(ids: number[]) {
    return request.post<UserResp[], number[]>("/api/v1/users/batch", ids);
  },
  getDepartmentTree() {
    return request.get<DepartmentTreeResp[]>("/api/v1/departments/tree");
  },
};
