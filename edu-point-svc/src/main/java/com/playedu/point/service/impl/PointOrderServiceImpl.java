package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.point.domain.PointOrder;
import com.playedu.point.dto.resp.PointOrderResp;
import com.playedu.point.mapper.PointOrderMapper;
import com.playedu.point.service.PointOrderService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PointOrderServiceImpl implements PointOrderService {
    private final PointOrderMapper pointOrderMapper;

    public PointOrderServiceImpl(PointOrderMapper pointOrderMapper) {
        this.pointOrderMapper = pointOrderMapper;
    }

    @Override
    public List<PointOrderResp> listMyOrders(Long userId) {
        validateUserId(userId);
        return pointOrderMapper.selectList(
                        new LambdaQueryWrapper<PointOrder>()
                                .eq(PointOrder::getUserId, userId)
                                .orderByDesc(PointOrder::getCreateTime))
                .stream()
                .map(PointOrderResp::fromEntity)
                .toList();
    }

    @Override
    public PointOrderResp getOrderById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        PointOrder entity = pointOrderMapper.selectById(id);
        if (entity == null) {
            throw new BizException("POINT_ORDER_NOT_FOUND", "兑换订单不存在");
        }
        return PointOrderResp.fromEntity(entity);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0L) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }
}

