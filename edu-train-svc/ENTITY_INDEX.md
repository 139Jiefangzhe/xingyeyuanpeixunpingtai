# edu-train-svc 实体索引

- TrainProject (title, description, type, status, startTime, endTime, assigneeScope, targetDeptIds + BaseEntity字段)
  - 用途：存储培训项目主信息、指派范围和项目时间窗
  - 关联表：edu_train_project
  - 最后更新：2026-05-05

- TrainTask (projectId, name, type, refId, sort, required, passRule + BaseEntity字段)
  - 用途：维护培训项目下的课程、考试、直播、作业等任务节点
  - 关联表：edu_train_task
  - 最后更新：2026-05-05
