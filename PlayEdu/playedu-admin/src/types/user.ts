export interface UserResp {
  id: number;
  name: string;
  email?: string;
  deptIds?: number[];
}

export interface DepartmentTreeResp {
  id: number;
  name: string;
  children?: DepartmentTreeResp[];
}
