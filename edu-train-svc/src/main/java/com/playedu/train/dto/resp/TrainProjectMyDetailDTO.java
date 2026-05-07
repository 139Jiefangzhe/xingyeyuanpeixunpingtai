package com.playedu.train.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class TrainProjectMyDetailDTO {
    private String projectId;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer overallProgress;

    private List<TaskItemDTO> tasks;

    @Data
    public static class TaskItemDTO {
        private String taskId;

        private String taskName;

        /**
         * COURSE / EXAM / LIVE / ASSIGNMENT
         */
        private String taskType;

        private Integer sort;

        private String resourceId;

        /**
         * NOT_STARTED / IN_PROGRESS / COMPLETED / OVERDUE
         */
        private String status;

        private LocalDateTime completedAt;

        private Boolean required;
    }
}
