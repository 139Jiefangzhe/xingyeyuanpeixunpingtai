package com.playedu.point.dto.resp;

import com.playedu.point.domain.PointRule;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointRuleResp {
    private String id;

    private String name;

    private String ruleType;

    private Integer points;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static PointRuleResp fromEntity(PointRule entity) {
        PointRuleResp resp = new PointRuleResp();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setRuleType(entity.getRuleType());
        resp.setPoints(entity.getPoints());
        resp.setDescription(entity.getDescription());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }
}

