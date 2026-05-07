package com.playedu.point.service;

import com.playedu.point.dto.resp.PointRuleResp;
import java.util.List;

public interface PointRuleService {
    List<PointRuleResp> listRules();

    PointRuleResp getRuleById(String id);

    void updateStatus(String id, Integer status);
}
