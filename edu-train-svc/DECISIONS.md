# edu-train-svc 设计决策记录

## 记录规范
- 每个决策按日期分组记录，包含：问题、决策、理由、反转条件
- 反转条件：什么情况下可以推翻这个决策

## 已确认决策

### 2026-05-05
- 问题：培训项目的部门指派范围如何建模
- 决策：`TrainProject.targetDeptIds` 当前使用逗号分隔字符串保存部门ID列表，不拆关联表
- 理由：当前阶段目标是先完成服务骨架和项目/任务主链路，字符串字段足以支撑基础发布与查询
- 反转条件：当需要按部门做高频筛选、统计或部门指派规模明显扩大时，拆分为独立关联表

- 问题：培训任务是否立即落跨服务考试校验
- 决策：当前只预留 `ExamFeignClient.getPaperById` 和 `fallbackFactory`，`TrainProjectService.addTasks` 暂不主动调用考试服务
- 理由：本轮重点是验证 Starter 复用和双服务 Maven 依赖链路，避免在第二个服务启动阶段引入跨服务耦合和远程依赖阻塞
- 反转条件：当 Controller 和任务校验规则落地后，在添加考试类任务时接入 `edu-exam-svc` 试卷存在性校验

- 问题：培训任务布尔字段如何兼顾当前字段语义和数据库命名规范
- 决策：Java 属性保持 `required`，数据库列使用 `is_required`，通过 `@TableField` 映射
- 理由：既保留业务层可读性，也遵守数据库布尔字段 `is_` 前缀规范
- 反转条件：当团队统一要求 Java 实体也使用 `isRequired` 命名时，再同步调整属性命名

- 问题：培训项目创建人身份从哪里获取
- 决策：`TrainProjectController.createProject` 从 Gateway 注入的 `X-User-Id` 读取创建人，并在 `TrainProjectService.createProject` 中通过 `user-svc` 校验用户存在
- 理由：避免客户端伪造创建人身份，同时让 `train-svc` 的第一条用户链路走真实微服务调用
- 反转条件：当统一登录上下文可直接注入当前用户对象时，可移除显式 Header 参数

- 问题：发布培训项目时如何处理 `targetDeptIds`
- 决策：本阶段只校验 `targetDeptIds` 为逗号分隔的正整数格式，不调用用户服务做部门成员展开
- 理由：`user-svc` 当前尚未提供按部门查询用户列表的接口，先保证发布前基础数据格式合法
- 反转条件：当 `user-svc` 提供部门成员查询接口后，改为发布前校验部门范围内是否存在有效受训人

- 问题：培训项目详情里的考试任务标题如何展示
- 决策：`getProjectDetail` 对考试类任务按需调用 `exam-svc` 填充 `examPaperTitle`，若远端不可用则记录告警并保留任务主体返回
- 理由：详情页需要尽量展示可读标题，但不应因为外部服务短时异常导致整个培训项目详情不可读
- 反转条件：当考试服务提供批量题目元数据接口或 train-svc 建立本地只读缓存后，改为批量查询或缓存填充

- 问题：课程类培训任务如何校验引用资源有效性
- 决策：`addTasks` 在写入课程类任务前通过 `course-svc` 的只读接口校验课程存在，详情页按需填充 `courseTitle`
- 理由：培训项目的课程节点必须绑定真实课程资源，否则发布后的学习链路无法执行
- 反转条件：当 `course-svc` 提供批量校验/批量元数据接口或 train-svc 建立本地课程缓存后，改为批量调用或缓存校验

- 问题：直播类培训任务是否在首版就校验直播间存在
- 决策：`addTasks` 在写入直播类任务前调用 `edu-live-svc` 校验 `roomId` 存在，但详情页暂不额外查询直播标题
- 理由：先保证任务引用不悬空，同时控制跨服务调用数量，避免在直播中心首版阶段就扩散详情聚合复杂度
- 反转条件：当培训详情页需要展示直播间更多元数据时，再补充批量查询或本地缓存

### 2026-05-06
- 问题：`edu-train-svc` 本地联调时如何在不依赖 Nacos 的情况下完成 Feign 调用
- 决策：新增 `local` profile，禁用 `spring.cloud.nacos.discovery/config`，但保留 `spring.cloud.discovery`，并通过 `spring.cloud.discovery.client.simple.instances` 显式声明 `exam/course/live/user` 本地实例地址
- 理由：本地单机联调需要保留 Spring Cloud LoadBalancer 才能让 Feign 通过服务名路由到 `8081-8085`，如果整体关闭 discovery 会导致 `Load balancer does not contain an instance` 异常
- 反转条件：当本地开发统一切回 Gateway + Nacos 或使用 Docker Compose 提供注册中心时，可移除 simple discovery 映射

- 问题：本地未启动 `edu-user-svc` 时如何继续演示培训项目创建流程
- 决策：`local` profile 下开启 `edu.local.user-bypass=true`，`TrainProjectServiceImpl.requireUser()` 返回本地虚拟用户，不阻塞项目创建和任务配置
- 理由：当前本地联调的目标是验证培训项目与课程/考试/直播的真实链路，用户链路可以暂时退化为 header + mock 用户，避免演示被基础设施依赖卡住
- 反转条件：当 `edu-user-svc` 也纳入本地联调编排并稳定可用时，应关闭 bypass，恢复真实用户校验

- 问题：培训项目详情页的效果数据在课程/直播尚无学习记录表时如何展示
- 决策：`getProjectStats` 采用“考试真实、课程/直播 mock”的混合聚合策略：考试通过率通过 `exam-svc` 远程统计接口获取，课程/直播完成度先在 `train-svc` 内按固定学员样本生成稳定 mock 数据
- 理由：当前最有演示价值的是项目完成度与考试通过率，先保证真实的考试链路可见，同时不给未交付的学习记录/观看记录能力制造接口空洞
- 反转条件：当 `course-svc` 和 `live-svc` 补齐真实学习记录/观看记录表与查询接口后，移除 mock 逻辑并切换为全真实聚合

### 2026-05-07
- 问题：学员端查看培训项目详情时，任务个人状态从哪里读取
- 决策：新增 `edu_train_user_task` 表作为培训任务个人完成状态的主记录源，`GET /api/v1/train-projects/{id}/my-detail` 直接聚合项目任务与该表；缺失记录统一按 `NOT_STARTED` 返回
- 理由：学员视角的任务完成度不能继续依赖管理端 mock 聚合，需要一个可持续扩展到课程、考试、直播回写的统一状态表
- 反转条件：当后续引入独立学习轨迹聚合服务，或改为由任务编排引擎统一投影用户状态时，可将 `train_user_task` 降级为投影表或被替代

## 待决策问题
