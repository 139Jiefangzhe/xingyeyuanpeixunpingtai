# edu-point-svc 实体索引

- PointRule (id, name, ruleType, points, status, isDel, createTime, updateTime)
  - 用途：定义积分发放规则
  - 关联表：point_rule
  - 最后更新：2026-05-07

- PointRecord (id, userId, ruleType, points, balance, sourceId, sourceType, createTime)
  - 用途：记录学员积分增减流水与余额
  - 关联表：point_record
  - 最后更新：2026-05-07

- PointProduct (id, name, imageUrl, pointsPrice, stock, status, isDel, createTime, updateTime)
  - 用途：定义积分商城可兑换商品
  - 关联表：point_product
  - 最后更新：2026-05-07

- PointOrder (id, userId, productId, productName, totalPoints, status, address, createTime, updateTime)
  - 用途：记录积分兑换订单
  - 关联表：point_order
  - 最后更新：2026-05-07

