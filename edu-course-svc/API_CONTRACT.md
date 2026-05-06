# edu-course-svc API 契约

- SERVICE `CourseService.createCourse` — 创建课程（含章节、课节、分类）
  - 请求：Long operatorId + CourseSaveReq
  - 响应：Integer courseId
  - 最后更新：2026-05-06

- SERVICE `CourseService.updateCourse` — 更新课程（重建章节、课节、分类）
  - 请求：Integer id + Long operatorId + CourseSaveReq
  - 响应：void
  - 最后更新：2026-05-06

- SERVICE `CourseService.deleteCourse` — 删除课程（逻辑删除课程主表）
  - 请求：Integer id + Long operatorId
  - 响应：void
  - 最后更新：2026-05-06

- SERVICE `CourseService.getCourseById` — 查询课程详情（含章节与课时）
  - 请求：Integer id
  - 响应：CourseDetailResp
  - 最后更新：2026-05-05

- SERVICE `CourseService.getCourseChapters` — 查询课程章节与课时列表
  - 请求：Integer id
  - 响应：List<CourseChapterResp>
  - 最后更新：2026-05-05

- SERVICE `CourseService.listCourses` — 分页查询课程列表
  - 请求：CourseQueryDTO（支持 `titleLike/type/categoryId/isRequired/isShow`）
  - 响应：Page<CourseSimpleResp>
  - 最后更新：2026-05-06

- SERVICE `CourseService.listCategoryOptions` — 查询课程分类选项
  - 请求：无
  - 响应：List<CourseCategoryOptionResp>
  - 最后更新：2026-05-06

- API `POST /api/v1/courses` — 创建课程
  - 请求：Header `X-User-Id` + CourseSaveReq
  - 响应：Result<Integer>
  - 最后更新：2026-05-06

- API `PUT /api/v1/courses/{id}` — 更新课程
  - 请求：PathVariable id + Header `X-User-Id` + CourseSaveReq
  - 响应：Result<Void>
  - 最后更新：2026-05-06

- API `DELETE /api/v1/courses/{id}` — 删除课程
  - 请求：PathVariable id + Header `X-User-Id`
  - 响应：Result<Void>
  - 最后更新：2026-05-06

- API `GET /api/v1/courses/{id}` — 查询课程详情
  - 请求：PathVariable id
  - 响应：Result<CourseDetailResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/courses/{id}/detail` — 查询课程完整详情
  - 请求：PathVariable id
  - 响应：Result<CourseDetailResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/courses/{id}/chapters` — 查询课程章节列表（含课时）
  - 请求：PathVariable id
  - 响应：Result<List<CourseChapterResp>>
  - 最后更新：2026-05-05

- API `GET /api/v1/courses` — 分页查询课程
  - 请求：CourseQueryDTO（支持 `titleLike/type/categoryId/isRequired/isShow`）
  - 响应：Result<PageResult<CourseSimpleResp>>
  - 最后更新：2026-05-06

- API `GET /api/v1/courses/categories` — 查询课程分类选项
  - 请求：无
  - 响应：Result<List<CourseCategoryOptionResp>>
  - 最后更新：2026-05-06
