package com.playedu.train.dto.resp;

import lombok.Data;

@Data
public class StudentProgressResp {
    private Long userId;

    private String userName;

    private String deptName;

    private String courseStatus;

    private String examStatus;

    private String liveStatus;

    private String homeworkStatus;

    private Integer completedTaskCount;

    private Integer totalTaskCount;

    private Integer overallCompletionRate;
}
