# edu-live-svc API 契约

- SERVICE `LiveRoomService.createRoom` — 创建直播间元数据
  - 请求：Long operatorId, LiveRoomCreateReq
  - 响应：LiveRoom
  - 最后更新：2026-05-05

- SERVICE `LiveRoomService.getRoomById` — 查询直播间详情
  - 请求：String id
  - 响应：LiveRoomResp
  - 最后更新：2026-05-05

- SERVICE `LiveRoomService.listRooms` — 分页查询直播间列表
  - 请求：LiveRoomQueryDTO
  - 响应：Page<LiveRoomResp>
  - 最后更新：2026-05-05

- SERVICE `LiveRoomService.startLive` — 启动直播间
  - 请求：String id, Long operatorId
  - 响应：LiveRoom
  - 最后更新：2026-05-05

- SERVICE `LiveRoomService.stopLive` — 结束直播间
  - 请求：String id, Long operatorId
  - 响应：LiveRoom
  - 最后更新：2026-05-05

- API `POST /api/v1/live-rooms` — 创建直播间
  - 请求：Header X-User-Id + LiveRoomCreateReq
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `GET /api/v1/live-rooms/{id}` — 查询直播间详情
  - 请求：PathVariable id
  - 响应：Result<LiveRoomResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/live-rooms` — 分页查询直播间
  - 请求：LiveRoomQueryDTO
  - 响应：Result<PageResult<LiveRoomResp>>
  - 最后更新：2026-05-05

- API `POST /api/v1/live-rooms/{id}/start` — 启动直播
  - 请求：PathVariable id + Header X-User-Id
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `POST /api/v1/live-rooms/{id}/stop` — 结束直播
  - 请求：PathVariable id + Header X-User-Id
  - 响应：Result<Void>
  - 最后更新：2026-05-05
