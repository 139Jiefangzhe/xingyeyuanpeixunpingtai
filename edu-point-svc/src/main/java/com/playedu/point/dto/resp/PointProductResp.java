package com.playedu.point.dto.resp;

import com.playedu.point.domain.PointProduct;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointProductResp {
    private String id;

    private String name;

    private String description;

    private String imageUrl;

    private Integer pointsPrice;

    private Integer stock;

    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static PointProductResp fromEntity(PointProduct entity) {
        PointProductResp resp = new PointProductResp();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setDescription(entity.getDescription());
        resp.setImageUrl(entity.getImageUrl());
        resp.setPointsPrice(entity.getPointsPrice());
        resp.setStock(entity.getStock());
        resp.setStatus(entity.getStatus());
        resp.setSort(entity.getSort());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }
}

