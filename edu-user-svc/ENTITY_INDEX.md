# edu-user-svc 实体索引

- User (id, email, name, isActive, isLock + legacy users 字段)
  - 用途：提供用户基础信息只读查询，供 exam/train 等服务远程调用
  - 关联表：users
  - 最后更新：2026-05-05

- Department (id, name, parentId, parentChain, sort)
  - 用途：构建部门树和部门归属查询
  - 关联表：departments
  - 最后更新：2026-05-05

- UserDepartment (userId, depId)
  - 用途：维护用户与部门的关联，用于补齐用户所属部门列表
  - 关联表：user_department
  - 最后更新：2026-05-05

- Role (id, name, slug)
  - 用途：保留角色领域桥接实体，为后续权限中心拆分预留映射
  - 关联表：admin_roles
  - 最后更新：2026-05-05
