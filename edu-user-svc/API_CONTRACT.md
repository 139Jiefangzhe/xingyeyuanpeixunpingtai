# edu-user-svc API 契约

- SERVICE `UserService.getUserById` — 查询单个用户基础信息
  - 请求：Long id
  - 响应：UserResp
  - 最后更新：2026-05-05

- SERVICE `UserService.batchGetUsers` — 批量查询用户基础信息
  - 请求：List<Long> userIds
  - 响应：List<UserResp>
  - 最后更新：2026-05-05

- SERVICE `DepartmentService.getDeptTree` — 查询部门树
  - 请求：无
  - 响应：List<DepartmentTreeResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/users/{id}` — 查询用户详情
  - 请求：PathVariable id
  - 响应：Result<UserResp>
  - 最后更新：2026-05-05

- API `POST /api/v1/users/batch` — 批量查询用户
  - 请求：Body List<Long>
  - 响应：Result<List<UserResp>>
  - 最后更新：2026-05-05

- API `GET /api/v1/departments/tree` — 查询部门树
  - 请求：无
  - 响应：Result<List<DepartmentTreeResp>>
  - 最后更新：2026-05-05
