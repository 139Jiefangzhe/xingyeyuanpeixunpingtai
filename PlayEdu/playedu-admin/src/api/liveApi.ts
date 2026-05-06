import type { ApiResult, PageResult } from "../types/api";
import type { LiveRoomCreateReq, LiveRoomQuery, LiveRoomResp } from "../types/live";
import request from "../utils/request";

export const liveApi = {
  listRooms(query: LiveRoomQuery) {
    return request.get<PageResult<LiveRoomResp>>("/api/v1/live-rooms", {
      params: query,
    });
  },
  getRoomById(id: string) {
    return request.get<LiveRoomResp>(`/api/v1/live-rooms/${id}`);
  },
  createRoom(payload: LiveRoomCreateReq, userId: number) {
    return request.post<string, LiveRoomCreateReq>("/api/v1/live-rooms", payload, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
  startLive(id: string, userId: number) {
    return request.post<void>(`/api/v1/live-rooms/${id}/start`, undefined, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
  stopLive(id: string, userId: number) {
    return request.post<void>(`/api/v1/live-rooms/${id}/stop`, undefined, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
} satisfies Record<string, (...args: any[]) => Promise<ApiResult<any>>>;
