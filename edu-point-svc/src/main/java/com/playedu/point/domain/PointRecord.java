package com.playedu.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("point_record")
public class PointRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String ruleType;

    private Integer points;

    private Integer balance;

    private String sourceId;

    private String sourceType;

    private String remark;

    private LocalDateTime createTime;
}

