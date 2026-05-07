package com.playedu.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("point_product")
public class PointProduct {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String description;

    private String imageUrl;

    private Integer pointsPrice;

    private Integer stock;

    private Integer status;

    private Integer sort;

    @TableLogic
    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

