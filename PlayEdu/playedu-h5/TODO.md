# playedu-h5 TODO

## 已完成
- [x] 学员端本地联调绕过初始化，支持 `VITE_LOCAL_DEV_BYPASS` + `VITE_LOCAL_USER_ID`
- [x] 考试模块骨架：`/exam` 列表页、`/exam/room/:paperId` 答题页、`/exam/result/:paperId` 成绩页
- [x] 培训模块骨架：`/train` 培训任务列表页
- [x] 本地多服务联调：H5 直连 `edu-exam-svc` / `edu-train-svc`

## 下一步
- [ ] 接入真实登录态，移除本地 bypass 对线上路由的影响
- [ ] 培训任务页支持按项目查看任务明细，而不是统一跳到考试入口
- [ ] 我的考试列表改为服务端考试记录驱动，替换本地 `localStorage` 状态推断
- [ ] 成绩页补充解析展示优化和错题过滤
- [ ] 增加课程任务与直播任务的移动端参与页

## 已知限制
- [ ] 当前 H5 仅完整打通考试参与链路，课程学习与直播观看仍是占位入口
- [ ] 本地演示依赖 `edu-*-svc` 的 `local` profile 和 H2 种子数据
