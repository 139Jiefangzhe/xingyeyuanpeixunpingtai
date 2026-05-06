package com.playedu.train.dto.resp;

import com.playedu.train.domain.entity.TrainProject;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrainProjectListResp {
    private String id;

    private String title;

    private Integer type;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer taskCount;

    public static TrainProjectListResp fromEntity(TrainProject project, int taskCount) {
        TrainProjectListResp resp = new TrainProjectListResp();
        resp.setId(project.getId());
        resp.setTitle(project.getTitle());
        resp.setType(project.getType());
        resp.setStatus(project.getStatus());
        resp.setStartTime(project.getStartTime());
        resp.setEndTime(project.getEndTime());
        resp.setTaskCount(taskCount);
        return resp;
    }
}
