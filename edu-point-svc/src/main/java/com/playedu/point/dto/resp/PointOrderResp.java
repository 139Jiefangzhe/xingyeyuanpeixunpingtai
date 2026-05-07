package com.playedu.point.dto.resp;

import com.playedu.point.domain.PointOrder;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointOrderResp {
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

    public static PointOrderResp fromEntity(PointOrder entity) {
        PointOrderResp resp = new PointOrderResp();
        resp.setId(entity.getId());
        resp.setUserId(entity.getUserId());
        resp.setProductId(entity.getProductId());
        resp.setProductName(entity.getProductName());
        resp.setPointsPrice(entity.getPointsPrice());
        resp.setQuantity(entity.getQuantity());
        resp.setTotalPoints(entity.getTotalPoints());
        resp.setStatus(entity.getStatus());
        resp.setAddress(entity.getAddress());
        resp.setRemark(entity.getRemark());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }
}

