package com.playedu.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_paper_question")
public class ExamPaperQuestion extends BaseEntity {
    @TableField("paper_id")
    private String paperId;

    @TableField("question_id")
    private String questionId;

    private Integer sort;

    private Integer score;
}
