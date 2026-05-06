## 2026-05-05
- [ ] 修复 `VITE_LOCAL_DEV_BYPASS=true` 时首页 dashboard 仍请求 `/backend/v1/dashboard/index` 产生 404 的问题 — 依赖：本地初始化流程与 dashboard API 调用梳理 — 预计工时：1h

## 2026-05-06
- [x] 开发学员端 H5 培训/答题页面，承接培训项目效果页中的学员完成链路演示 — 已完成：`/exam`、`/exam/room/:paperId`、`/exam/result/:paperId`、`/train` 本地真实联调；管理员效果页已观察到学员 `10005` 提交后的统计变化
- [ ] 开发 H5 培训任务详情页，展示单个培训项目下的课程/考试/直播任务，并从任务项直接进入参与页面 — 依赖：train-svc 提供项目任务明细接口或现有详情接口下沉到 H5 可复用 DTO — 预计工时：4h
