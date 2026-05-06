export interface TrainProjectQuery {
  pageNum: number;
  pageSize: number;
  titleLike?: string;
  type?: number;
  status?: number;
  sortField?: "createTime" | "startTime" | "endTime" | "status";
  sortOrder?: "asc" | "desc";
}

export interface TrainProjectCreateReq {
  title: string;
  description?: string;
  type: number;
  startTime?: string;
  endTime?: string;
  assigneeScope: number;
  targetDeptIds?: string;
}

export interface TrainTaskReq {
  name: string;
  type: number;
  refId: string;
  sort: number;
  required: number;
  passRule: number;
}

export interface TrainProjectListResp {
  id: string;
  title: string;
  type: number;
  status: number;
  startTime?: string;
  endTime?: string;
  taskCount: number;
}

export interface TrainTaskResp {
  id: string;
  projectId: string;
  name: string;
  type: number;
  refId: string;
  sort: number;
  required: number;
  passRule: number;
  courseTitle?: string;
  examPaperTitle?: string;
  createTime?: string;
  updateTime?: string;
}

export interface TrainProjectDetailResp {
  id: string;
  title: string;
  description?: string;
  type: number;
  status: number;
  startTime?: string;
  endTime?: string;
  assigneeScope: number;
  targetDeptIds?: string;
  createTime?: string;
  updateTime?: string;
  tasks: TrainTaskResp[];
}

export interface ProjectTaskProgressResp {
  taskId: string;
  taskName: string;
  taskType: number;
  refId: string;
  resourceTitle: string;
  completedCount: number;
  totalCount: number;
  completionRate: number;
  metricLabel: string;
}

export interface StudentProgressResp {
  userId: number;
  userName: string;
  deptName: string;
  courseStatus: string;
  examStatus: string;
  liveStatus: string;
  homeworkStatus: string;
  completedTaskCount: number;
  totalTaskCount: number;
  overallCompletionRate: number;
}

export interface ProjectStatsResp {
  projectId: string;
  title: string;
  status: number;
  startTime?: string;
  endTime?: string;
  participantCount: number;
  totalUserCount: number;
  overallCompletionRate: number;
  taskProgressList: ProjectTaskProgressResp[];
  studentProgressList: StudentProgressResp[];
}
