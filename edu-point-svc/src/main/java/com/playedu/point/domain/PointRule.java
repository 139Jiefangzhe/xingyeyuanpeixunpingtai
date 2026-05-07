package com.playedu.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("point_rule")
public class PointRule {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String ruleType;

    private Integer points;

    private String description;

    private Integer status;

    @TableLogic
    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

