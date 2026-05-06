package com.playedu.train.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class TrainProjectDetailResp {
    private String id;

    private String title;

    private String description;

    private Integer type;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer assigneeScope;

    private String targetDeptIds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<TrainTaskResp> tasks;
}
