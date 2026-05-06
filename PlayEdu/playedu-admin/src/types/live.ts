export interface LiveRoomQuery {
  pageNum: number;
  pageSize: number;
  courseId?: number;
  status?: number;
  titleLike?: string;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface LiveRoomResp {
  id: string;
  title: string;
  courseId?: number;
  startTime?: string;
  endTime?: string;
  status: number;
  pushUrl?: string;
  playUrl?: string;
  recordUrl?: string;
  creatorId?: number;
  createTime?: string;
  updateTime?: string;
}

export interface LiveRoomCreateReq {
  title: string;
  courseId: number;
  startTime?: string;
  endTime?: string;
  pushUrl?: string;
  playUrl?: string;
  recordUrl?: string;
}
