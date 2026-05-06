package com.playedu.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_exam_question", autoResultMap = true)
public class Question extends BaseEntity {
    @TableField("bank_id")
    private String bankId;

    private Integer type;

    private String content;

    private String options;

    private String answer;

    private String analysis;

    private Integer difficulty;

    @TableField("knowledge_point")
    private String knowledgePoint;

    private Integer score;
}
