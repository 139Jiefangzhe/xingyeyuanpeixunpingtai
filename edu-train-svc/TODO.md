# edu-train-svc 待办

## 2026-05-06
- [ ] 将 `local` 模式下的用户旁路校验替换为真实 `edu-user-svc` 本地联调 — 依赖：user-svc 提供稳定 local profile 与种子数据 — 预计工时：1h
- [ ] 用 `course-svc` 学习记录和 `live-svc` 观看记录替换项目详情页中的课程/直播 mock 进度 — 依赖：course/live 服务补齐学习行为表与统计接口 — 预计工时：2h

## 2026-05-07
- [ ] 将考试提交、课程学习完成、直播观看完成事件回写到 `edu_train_user_task`，让 `my-detail` 不再依赖初始化种子数据 — 依赖：exam/course/live 服务提供任务完成回调或 MQ 事件 — 预计工时：3h
