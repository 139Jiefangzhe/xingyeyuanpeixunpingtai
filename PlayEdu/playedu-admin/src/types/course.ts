export interface CourseQuery {
  pageNum: number;
  pageSize: number;
  titleLike?: string;
  type?: number;
  categoryId?: number;
  isShow?: number;
  sortField?: string;
  sortOrder?: "asc" | "desc";
}

export interface CourseCategoryOption {
  value: number;
  label: string;
}

export interface CourseSimpleResp {
  id: number;
  title: string;
  thumb: number;
  charge: number;
  classHour: number;
  type?: number;
  coverUrl?: string;
  isRequired: number;
  isShow: number;
  createdAt?: string;
  categoryIds: number[];
}

export interface CourseHourResp {
  id: number;
  courseId: number;
  chapterId: number;
  sort: number;
  title: string;
  type: string;
  rid: number;
  duration: number;
  resourceUrl?: string;
  createdAt?: string;
}

export interface CourseChapterResp {
  id: number;
  courseId: number;
  name: string;
  sort: number;
  createdAt?: string;
  updatedAt?: string;
  lessons: CourseHourResp[];
}

export interface CourseDetailResp {
  id: number;
  title: string;
  thumb: number;
  charge: number;
  shortDesc?: string;
  type?: number;
  coverUrl?: string;
  isRequired: number;
  classHour: number;
  isShow: number;
  createdAt?: string;
  sortAt?: string;
  extra?: string;
  adminId: number;
  categoryIds: number[];
  chapters: CourseChapterResp[];
}

export interface CourseLessonReq {
  title: string;
  resourceUrl?: string;
  duration: number;
}

export interface CourseChapterReq {
  name: string;
  lessons: CourseLessonReq[];
}

export interface CourseSaveReq {
  title: string;
  shortDesc?: string;
  coverUrl?: string;
  type: number;
  categoryIds: number[];
  isShow: number;
  classHour: number;
  chapters: CourseChapterReq[];
}
