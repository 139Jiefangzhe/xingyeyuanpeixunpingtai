package com.playedu.train.dto.resp;

import lombok.Data;

@Data
public class ProjectTaskProgressResp {
    private String taskId;

    private String taskName;

    private Integer taskType;

    private String refId;

    private String resourceTitle;

    private Integer completedCount;

    private Integer totalCount;

    private Integer completionRate;

    private String metricLabel;
}
