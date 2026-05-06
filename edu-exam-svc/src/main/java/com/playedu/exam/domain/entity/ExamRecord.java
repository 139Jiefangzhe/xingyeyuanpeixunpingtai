package com.playedu.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_exam_record", autoResultMap = true)
public class ExamRecord extends BaseEntity {
    @TableField("exam_id")
    private String examId;

    @TableField("paper_id")
    private String paperId;

    @TableField("user_id")
    private Long userId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("obtain_score")
    private Integer obtainScore;

    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("switch_count")
    private Integer switchCount;

    private String answers;
}
