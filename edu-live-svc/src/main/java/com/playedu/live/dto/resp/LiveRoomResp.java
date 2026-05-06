package com.playedu.live.dto.resp;

import com.playedu.live.domain.entity.LiveRoom;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LiveRoomResp {
    private String id;

    private String title;

    private Integer courseId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String pushUrl;

    private String playUrl;

    private String recordUrl;

    private Long creatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static LiveRoomResp fromEntity(LiveRoom room) {
        LiveRoomResp resp = new LiveRoomResp();
        resp.setId(room.getId());
        resp.setTitle(room.getTitle());
        resp.setCourseId(room.getCourseId());
        resp.setStartTime(room.getStartTime());
        resp.setEndTime(room.getEndTime());
        resp.setStatus(room.getStatus());
        resp.setPushUrl(room.getPushUrl());
        resp.setPlayUrl(room.getPlayUrl());
        resp.setRecordUrl(room.getRecordUrl());
        resp.setCreatorId(room.getCreatorId());
        resp.setCreateTime(room.getCreateTime());
        resp.setUpdateTime(room.getUpdateTime());
        return resp;
    }
}
