package com.playedu.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_session")
public class ExamSession extends BaseEntity {
    @TableField("record_id")
    private String recordId;

    @TableField("exam_id")
    private String examId;

    @TableField("paper_id")
    private String paperId;

    @TableField("user_id")
    private Long userId;

    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    @TableField("ip_address")
    private String ipAddress;
}
