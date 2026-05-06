package com.playedu.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_exam_paper", autoResultMap = true)
public class ExamPaper extends BaseEntity {
    private String title;

    private String description;

    @TableField("total_score")
    private Integer totalScore;

    private Integer duration;

    @TableField("pass_score")
    private Integer passScore;

    private Integer status;

    private Integer type;

    @TableField("allow_redo")
    private Integer allowRedo;

    @TableField("knowledge_config")
    private String knowledgeConfig;
}
