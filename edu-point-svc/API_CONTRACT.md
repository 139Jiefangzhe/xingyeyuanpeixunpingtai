# edu-point-svc API 契约

- API `GET /api/v1/point-rules` — 积分规则列表
  - 请求：无
  - 响应：`Result<List<PointRuleResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-rules/{id}` — 积分规则详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointRuleResp>`
  - 最后更新：2026-05-07

- API `PUT /api/v1/point-rules/{id}/status` — 更新积分规则状态
  - 请求：`PointRuleStatusUpdateReq`
  - 响应：`Result<Void>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-products` — 积分商品列表
  - 请求：Query `all` 可选，`true` 时返回管理端全量商品
  - 响应：`Result<List<PointProductResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-products/{id}` — 商品详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointProductResp>`
  - 最后更新：2026-05-07

- API `POST /api/v1/point-products` — 创建积分商品
  - 请求：`PointProductSaveReq`
  - 响应：`Result<String>`
  - 最后更新：2026-05-07

- API `PUT /api/v1/point-products/{id}` — 更新积分商品
  - 请求：PathVariable `id` + `PointProductSaveReq`
  - 响应：`Result<Void>`
  - 最后更新：2026-05-07

- API `DELETE /api/v1/point-products/{id}` — 删除积分商品
  - 请求：PathVariable `id`
  - 响应：`Result<Void>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-records/my` — 我的积分流水
  - 请求：Header `X-User-Id`
  - 响应：`Result<List<PointRecordResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-records/balance` — 当前积分余额
  - 请求：Header `X-User-Id`
  - 响应：`Result<Integer>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-orders/my` — 我的兑换订单
  - 请求：Header `X-User-Id`
  - 响应：`Result<List<PointOrderResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-orders` — 兑换订单列表
  - 请求：无
  - 响应：`Result<List<PointOrderResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-orders/{id}` — 订单详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointOrderResp>`
  - 最后更新：2026-05-07

- API `PUT /api/v1/point-orders/{id}/status` — 更新兑换订单状态
  - 请求：PathVariable `id` + `PointOrderStatusUpdateReq`
  - 响应：`Result<Void>`
  - 最后更新：2026-05-07
