package com.playedu.train.dto.resp;

import lombok.Data;

@Data
public class LiveRoomFeignResp {
    private String id;

    private String title;

    private Integer courseId;

    private Integer status;
}
