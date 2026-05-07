# edu-point-svc 设计决策

## 2026-05-07
- 问题：Phase 3 首轮积分服务需要先满足本地演示与后续微服务联动
- 决策：第一轮仅提供规则、商品、流水、订单四类只读查询接口，写操作与考试/培训积分发放事件后续补充
- 理由：先形成可独立启动的积分商城骨架，再接入 exam/train 事件闭环
- 反转条件：当开始实现“考试通过发积分”和“兑换商品扣积分”时，再补齐写接口、幂等和锁逻辑

## 2026-05-07
- 问题：本地联调环境缺少 MySQL/Nacos/Redis 集群
- 决策：local profile 使用 H2 内存库 + `schema-local.sql/data-local.sql`，同时通过 `bootstrap-local.yml` 禁用 Nacos
- 理由：保证 `spring-boot:run -Dspring-boot.run.profiles=local` 可以单点启动
- 反转条件：当本地统一切换到 Docker Compose 全栈环境时，可改回 MySQL + Flyway 本地验证

## 2026-05-07
- 问题：Controller 是否直接返回 Entity
- 决策：查询接口统一返回 Resp DTO，不直接暴露 Entity
- 理由：与现有微服务的 DTO 分层和前后端字段契约保持一致
- 反转条件：无

