package com.playedu.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.exception.BizException;
import com.playedu.live.domain.entity.LiveRoom;
import com.playedu.live.dto.query.LiveRoomQueryDTO;
import com.playedu.live.dto.req.LiveRoomCreateReq;
import com.playedu.live.dto.resp.LiveRoomResp;
import com.playedu.live.mapper.LiveRoomMapper;
import com.playedu.live.service.LiveRoomService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LiveRoomServiceImpl implements LiveRoomService {
    private final LiveRoomMapper liveRoomMapper;

    public LiveRoomServiceImpl(LiveRoomMapper liveRoomMapper) {
        this.liveRoomMapper = liveRoomMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom createRoom(Long operatorId, LiveRoomCreateReq req) {
        validateRoomTime(req.getStartTime(), req.getEndTime());
        LiveRoom room = new LiveRoom();
        room.setTitle(req.getTitle());
        room.setCourseId(req.getCourseId());
        room.setStartTime(req.getStartTime());
        room.setEndTime(req.getEndTime());
        room.setStatus(1);
        room.setPushUrl(req.getPushUrl());
        room.setPlayUrl(req.getPlayUrl());
        room.setRecordUrl(req.getRecordUrl());
        room.setCreatorId(operatorId);
        room.setCreateBy(operatorId);
        room.setUpdateBy(operatorId);
        liveRoomMapper.insert(room);
        return requireRoom(room.getId());
    }

    @Override
    public LiveRoomResp getRoomById(String id) {
        return LiveRoomResp.fromEntity(requireRoom(id));
    }

    @Override
    public Page<LiveRoomResp> listRooms(LiveRoomQueryDTO query) {
        Page<LiveRoom> page =
                new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        LambdaQueryWrapper<LiveRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LiveRoom::getIsDeleted, 0);
        if (query.getCourseId() != null) {
            queryWrapper.eq(LiveRoom::getCourseId, query.getCourseId());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(LiveRoom::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getTitleLike())) {
            queryWrapper.like(LiveRoom::getTitle, query.getTitleLike());
        }
        applySort(queryWrapper, query.getSortField(), query.getSortOrder());
        Page<LiveRoom> entityPage = liveRoomMapper.selectPage(page, queryWrapper);
        Page<LiveRoomResp> result =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(LiveRoomResp::fromEntity).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom startLive(String id, Long operatorId) {
        LiveRoom room = requireRoom(id);
        if (!Integer.valueOf(1).equals(room.getStatus())) {
            throw new BizException("LIVE_ROOM_STATUS_INVALID", "只有未开始的直播间允许启动");
        }
        room.setStatus(2);
        room.setUpdateBy(operatorId);
        if (room.getStartTime() == null) {
            room.setStartTime(LocalDateTime.now());
        }
        liveRoomMapper.updateById(room);
        return requireRoom(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom stopLive(String id, Long operatorId) {
        LiveRoom room = requireRoom(id);
        if (!Integer.valueOf(2).equals(room.getStatus())) {
            throw new BizException("LIVE_ROOM_STATUS_INVALID", "只有直播中的直播间允许结束");
        }
        room.setStatus(3);
        room.setUpdateBy(operatorId);
        room.setEndTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);
        return requireRoom(id);
    }

    private LiveRoom requireRoom(String id) {
        LiveRoom room = liveRoomMapper.selectById(id);
        if (room == null || !Integer.valueOf(0).equals(room.getIsDeleted())) {
            throw new BizException("LIVE_ROOM_NOT_FOUND", "直播间不存在: " + id);
        }
        return room;
    }

    private void validateRoomTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BizException("LIVE_ROOM_TIME_INVALID", "直播开始时间不能晚于结束时间");
        }
    }

    private void applySort(
            LambdaQueryWrapper<LiveRoom> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortField) ? sortField : "createTime";
        switch (field) {
            case "startTime" -> queryWrapper.orderBy(true, asc, LiveRoom::getStartTime);
            case "endTime" -> queryWrapper.orderBy(true, asc, LiveRoom::getEndTime);
            case "status" -> queryWrapper.orderBy(true, asc, LiveRoom::getStatus);
            default -> queryWrapper.orderBy(true, asc, LiveRoom::getCreateTime);
        }
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum.longValue();
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }
}
