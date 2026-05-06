package com.playedu.live.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.live.domain.entity.LiveRoom;
import com.playedu.live.dto.query.LiveRoomQueryDTO;
import com.playedu.live.dto.req.LiveRoomCreateReq;
import com.playedu.live.dto.resp.LiveRoomResp;

public interface LiveRoomService {
    LiveRoom createRoom(Long operatorId, LiveRoomCreateReq req);

    LiveRoomResp getRoomById(String id);

    Page<LiveRoomResp> listRooms(LiveRoomQueryDTO query);

    LiveRoom startLive(String id, Long operatorId);

    LiveRoom stopLive(String id, Long operatorId);
}
