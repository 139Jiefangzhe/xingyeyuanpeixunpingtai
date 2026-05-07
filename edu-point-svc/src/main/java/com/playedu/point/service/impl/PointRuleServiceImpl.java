package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.point.domain.PointRule;
import com.playedu.point.dto.resp.PointRuleResp;
import com.playedu.point.mapper.PointRuleMapper;
import com.playedu.point.service.PointRuleService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PointRuleServiceImpl implements PointRuleService {
    private final PointRuleMapper pointRuleMapper;

    public PointRuleServiceImpl(PointRuleMapper pointRuleMapper) {
        this.pointRuleMapper = pointRuleMapper;
    }

    @Override
    public List<PointRuleResp> listRules() {
        return pointRuleMapper.selectList(
                        new LambdaQueryWrapper<PointRule>()
                                .eq(PointRule::getIsDel, 0)
                                .orderByDesc(PointRule::getCreateTime))
                .stream()
                .map(PointRuleResp::fromEntity)
                .toList();
    }

    @Override
    public PointRuleResp getRuleById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("规则ID不能为空");
        }
        PointRule entity = pointRuleMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getIsDel())) {
            throw new BizException("POINT_RULE_NOT_FOUND", "积分规则不存在");
        }
        return PointRuleResp.fromEntity(entity);
    }
}

