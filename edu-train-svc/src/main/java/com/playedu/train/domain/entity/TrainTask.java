package com.playedu.train.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "edu_train_task", autoResultMap = true)
public class TrainTask extends BaseEntity {
    @TableField("project_id")
    private String projectId;

    private String name;

    private Integer type;

    @TableField("ref_id")
    private String refId;

    private Integer sort;

    @TableField("is_required")
    private Integer required;

    @TableField("pass_rule")
    private Integer passRule;
}
