package com.playedu.train.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_train_user_task", autoResultMap = true)
public class TrainUserTask extends BaseEntity {
    @TableField("project_id")
    private String projectId;

    @TableField("task_id")
    private String taskId;

    @TableField("user_id")
    private Long userId;

    /**
     * NOT_STARTED / IN_PROGRESS / COMPLETED / OVERDUE
     */
    private String status;

    @TableField("completed_at")
    private LocalDateTime completedAt;
}
