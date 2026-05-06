package com.playedu.train.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_train_project", autoResultMap = true)
public class TrainProject extends BaseEntity {
    private String title;

    private String description;

    private Integer type;

    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("assignee_scope")
    private Integer assigneeScope;

    @TableField("target_dept_ids")
    private String targetDeptIds;
}
