import client, { HttpClient } from "./internal/httpClient";

const COURSE_APP_URL =
  import.meta.env.VITE_COURSE_API_URL ||
  import.meta.env.VITE_APP_URL ||
  "http://127.0.0.1:8083";

const courseClient = new HttpClient(COURSE_APP_URL);

// 线上课详情
export function detail(id: number) {
  return courseClient.get(`/api/v1/courses/${id}`, {}).then((res: any) => {
    const course = res.data || {};
    const chapters = Array.isArray(course.chapters)
      ? course.chapters.map((item: any) => ({
          id: item.id,
          name: item.name,
          course_id: item.courseId,
          sort: item.sort,
        }))
      : [];

    const hours = chapters.reduce(
      (acc: Record<number, CourseHourModel[]>, chapter: ChapterModel) => {
        const sourceChapter = course.chapters.find(
          (item: any) => item.id === chapter.id
        );
        acc[chapter.id] = Array.isArray(sourceChapter?.lessons)
          ? sourceChapter.lessons.map((lesson: any) => ({
              id: lesson.id,
              course_id: lesson.courseId,
              chapter_id: lesson.chapterId,
              duration: lesson.duration,
              rid: lesson.rid,
              sort: lesson.sort,
              title: lesson.title,
              type: lesson.type,
            }))
          : [];
        return acc;
      },
      {}
    );

    return {
      code: res.code,
      msg: res.msg,
      data: {
        course: {
          id: course.id,
          title: course.title,
          thumb: course.thumb,
          short_desc: course.shortDesc,
          is_required: course.isRequired,
          charge: course.charge,
          class_hour: course.classHour,
        },
        chapters,
        hours,
        attachments: [],
        learn_record: null,
        learn_hour_records: {},
      },
    };
  });
}

// 线上课课时详情
export function play(courseId: number, id: number) {
  return client.get(`/api/v1/course/${courseId}/hour/${id}`, {});
}

// 获取播放地址
export function playUrl(courseId: number, hourId: number) {
  return client.get(`/api/v1/course/${courseId}/hour/${hourId}/play`, {});
}

// 记录学员观看时长
export function record(courseId: number, hourId: number, duration: number) {
  return client.post(`/api/v1/course/${courseId}/hour/${hourId}/record`, {
    duration,
  });
}

//观看ping
export function playPing(courseId: number, hourId: number) {
  return client.post(`/api/v1/course/${courseId}/hour/${hourId}/ping`, {});
}

//最近学习课程
export function latestLearn() {
  return client.get(`/api/v1/user/latest-learn`, {});
}

//下载课件
export function downloadAttachment(courseId: number, id: number) {
  return client.get(`/api/v1/course/${courseId}/attach/${id}/download`, {});
}
