# edu-course-svc 实体索引

- Course (title, thumb, charge, shortDesc, isRequired, classHour, isShow, sortAt, extra, adminId)
  - 用途：承载旧版课程主表数据，供课程列表、课程详情和培训任务课程校验复用
  - 关联表：courses
  - 最后更新：2026-05-05

- CourseChapter (courseId, name, sort, createdAt, updatedAt)
  - 用途：维护课程章节结构
  - 关联表：course_chapters
  - 最后更新：2026-05-05

- CourseHour (courseId, chapterId, sort, title, type, rid, duration, deleted)
  - 用途：维护课程课时/课节信息
  - 关联表：course_hour
  - 最后更新：2026-05-05

- CourseCategory (courseId, categoryId)
  - 用途：维护课程与分类的关联关系，支撑按分类筛选
  - 关联表：resource_course_category
  - 最后更新：2026-05-05
