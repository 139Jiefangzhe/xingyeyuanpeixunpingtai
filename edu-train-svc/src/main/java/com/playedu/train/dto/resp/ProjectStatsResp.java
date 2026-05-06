package com.playedu.train.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProjectStatsResp {
    private String projectId;

    private String title;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer participantCount;

    private Integer totalUserCount;

    private Integer overallCompletionRate;

    private List<ProjectTaskProgressResp> taskProgressList;

    private List<StudentProgressResp> studentProgressList;
}
