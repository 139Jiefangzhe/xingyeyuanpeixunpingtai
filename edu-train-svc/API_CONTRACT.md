# edu-train-svc API 契约

- SERVICE `TrainProjectService.createProject` — 创建培训项目草稿并校验创建人
  - 请求：Long creatorId, TrainProjectCreateReq
  - 响应：TrainProject
  - 最后更新：2026-05-05

- SERVICE `TrainProjectService.addTasks` — 为培训项目批量添加任务节点
  - 请求：String projectId, List<TrainTaskReq>
  - 响应：List<TrainTask>
  - 最后更新：2026-05-05

- SERVICE `TrainProjectService.publishProject` — 发布培训项目
  - 请求：String projectId
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `TrainProjectService.getProjectDetail` — 查询培训项目详情（含任务列表）
  - 请求：String projectId
  - 响应：TrainProjectDetailResp
  - 最后更新：2026-05-05

- SERVICE `TrainProjectService.getProjectStats` — 查询培训项目效果统计（任务完成度 + 学员进度）
  - 请求：String projectId
  - 响应：ProjectStatsResp
  - 最后更新：2026-05-06

- SERVICE `TrainProjectService.listProjects` — 分页查询培训项目
  - 请求：TrainProjectQueryDTO
  - 响应：Page<TrainProjectListResp>
  - 最后更新：2026-05-05

- FEIGN `ExamFeignClient.getPaperById` — 查询考试试卷详情预留接口
  - 请求：String paperId
  - 响应：Result<ExamPaperFeignResp>
  - 最后更新：2026-05-05

- FEIGN `ExamFeignClient.getPaperStats` — 查询试卷考试参与/通过统计
  - 请求：String paperId
  - 响应：Result<ExamPaperStatsFeignResp>
  - 最后更新：2026-05-06

- FEIGN `CourseFeignClient.getCourseById` — 查询课程详情预留接口
  - 请求：String courseId
  - 响应：Result<CourseFeignResp>
  - 最后更新：2026-05-05

- FEIGN `LiveFeignClient.getRoomById` — 查询直播间详情预留接口
  - 请求：String roomId
  - 响应：Result<LiveRoomFeignResp>
  - 最后更新：2026-05-05

- FEIGN `UserFeignClient.getUserById` — 查询单个用户基础信息
  - 请求：Long userId
  - 响应：Result<UserFeignResp>
  - 最后更新：2026-05-05

- FEIGN `UserFeignClient.batchGetUsers` — 批量查询用户基础信息
  - 请求：List<Long>
  - 响应：Result<List<UserFeignResp>>
  - 最后更新：2026-05-05

- API `POST /api/v1/train-projects` — 创建培训项目
  - 请求：Header X-User-Id + TrainProjectCreateReq
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `POST /api/v1/train-projects/{id}/tasks` — 批量添加培训任务
  - 请求：PathVariable id + List<TrainTaskReq>
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `POST /api/v1/train-projects/{id}/publish` — 发布培训项目
  - 请求：PathVariable id
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `GET /api/v1/train-projects/{id}` — 查询培训项目详情
  - 请求：PathVariable id
  - 响应：Result<TrainProjectDetailResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/train-projects/{id}/stats` — 查询培训项目效果统计
  - 请求：PathVariable id
  - 响应：Result<ProjectStatsResp>
  - 最后更新：2026-05-06

- API `GET /api/v1/train-projects` — 分页查询培训项目
  - 请求：TrainProjectQueryDTO
  - 响应：Result<PageResult<TrainProjectListResp>>
  - 最后更新：2026-05-05
