package com.playedu.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("point_order")
public class PointOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private Long userId;

    private String productId;

    private String productName;

    private Integer pointsPrice;

    private Integer quantity;

    private Integer totalPoints;

    private String status;

    private String address;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

