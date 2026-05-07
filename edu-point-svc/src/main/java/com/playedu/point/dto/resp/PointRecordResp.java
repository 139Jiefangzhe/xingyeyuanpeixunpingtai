package com.playedu.point.dto.resp;

import com.playedu.point.domain.PointRecord;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointRecordResp {
    private Long id;

    private Long userId;

    private String ruleType;

    private Integer points;

    private Integer balance;

    private String sourceId;

    private String sourceType;

    private String remark;

    private LocalDateTime createTime;

    public static PointRecordResp fromEntity(PointRecord entity) {
        PointRecordResp resp = new PointRecordResp();
        resp.setId(entity.getId());
        resp.setUserId(entity.getUserId());
        resp.setRuleType(entity.getRuleType());
        resp.setPoints(entity.getPoints());
        resp.setBalance(entity.getBalance());
        resp.setSourceId(entity.getSourceId());
        resp.setSourceType(entity.getSourceType());
        resp.setRemark(entity.getRemark());
        resp.setCreateTime(entity.getCreateTime());
        return resp;
    }
}

