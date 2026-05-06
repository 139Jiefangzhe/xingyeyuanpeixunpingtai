import type { ApiResult, PageResult } from "../types/api";
import type {
  CourseCategoryOption,
  CourseDetailResp,
  CourseQuery,
  CourseSaveReq,
  CourseSimpleResp,
} from "../types/course";
import request from "../utils/request";

export const courseApi = {
  listCourses(query: CourseQuery) {
    return request.get<PageResult<CourseSimpleResp>>("/api/v1/courses", {
      params: query,
    });
  },
  getCourseById(id: number | string) {
    return request.get<CourseDetailResp>(`/api/v1/courses/${id}`);
  },
  getCourseDetail(id: number | string) {
    return request.get<CourseDetailResp>(`/api/v1/courses/${id}/detail`);
  },
  getCourseChapters(id: number | string) {
    return request.get<CourseDetailResp["chapters"]>(`/api/v1/courses/${id}/chapters`);
  },
  listCategoryOptions() {
    return request.get<CourseCategoryOption[]>("/api/v1/courses/categories");
  },
  createCourse(payload: CourseSaveReq, userId: number) {
    return request.post<number, CourseSaveReq>("/api/v1/courses", payload, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
  updateCourse(id: number | string, payload: CourseSaveReq, userId: number) {
    return request.put<void, CourseSaveReq>(`/api/v1/courses/${id}`, payload, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
  deleteCourse(id: number | string, userId: number) {
    return request.delete<void>(`/api/v1/courses/${id}`, {
      headers: {
        "X-User-Id": String(userId),
      },
    });
  },
} satisfies Record<string, (...args: any[]) => Promise<ApiResult<any>>>;
