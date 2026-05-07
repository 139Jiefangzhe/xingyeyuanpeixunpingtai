# edu-point-svc API 契约

- API `GET /api/v1/point-rules` — 积分规则列表
  - 请求：无
  - 响应：`Result<List<PointRuleResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-rules/{id}` — 积分规则详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointRuleResp>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-products` — 积分商品列表
  - 请求：无
  - 响应：`Result<List<PointProductResp>>`
  - 最后更新：2026-05-07

- API `GET /api/v1/point-products/{id}` — 商品详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointProductResp>`
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

- API `GET /api/v1/point-orders/{id}` — 订单详情
  - 请求：PathVariable `id`
  - 响应：`Result<PointOrderResp>`
  - 最后更新：2026-05-07

