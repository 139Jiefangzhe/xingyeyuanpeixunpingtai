package com.playedu.train.dto.resp;

import com.playedu.train.domain.entity.TrainTask;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrainTaskResp {
    private String id;

    private String projectId;

    private String name;

    private Integer type;

    private String refId;

    private Integer sort;

    private Integer required;

    private Integer passRule;

    private String examPaperTitle;

    private String courseTitle;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static TrainTaskResp fromEntity(TrainTask task) {
        return fromEntity(task, null, null);
    }

    public static TrainTaskResp fromEntity(TrainTask task, String courseTitle, String examPaperTitle) {
        TrainTaskResp resp = new TrainTaskResp();
        resp.setId(task.getId());
        resp.setProjectId(task.getProjectId());
        resp.setName(task.getName());
        resp.setType(task.getType());
        resp.setRefId(task.getRefId());
        resp.setSort(task.getSort());
        resp.setRequired(task.getRequired());
        resp.setPassRule(task.getPassRule());
        resp.setCourseTitle(courseTitle);
        resp.setExamPaperTitle(examPaperTitle);
        resp.setCreateTime(task.getCreateTime());
        resp.setUpdateTime(task.getUpdateTime());
        return resp;
    }
}
