package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.point.domain.PointOrder;
import com.playedu.point.dto.resp.PointOrderResp;
import com.playedu.point.mapper.PointOrderMapper;
import com.playedu.point.service.PointOrderService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PointOrderServiceImpl implements PointOrderService {
    private static final Set<String> ALLOWED_STATUSES =
            new LinkedHashSet<>(List.of("PENDING", "SHIPPED", "COMPLETED", "CANCELLED"));

    private final PointOrderMapper pointOrderMapper;

    public PointOrderServiceImpl(PointOrderMapper pointOrderMapper) {
        this.pointOrderMapper = pointOrderMapper;
    }

    @Override
    public List<PointOrderResp> listOrders() {
        return pointOrderMapper.selectList(
                        new LambdaQueryWrapper<PointOrder>().orderByDesc(PointOrder::getCreateTime))
                .stream()
                .map(PointOrderResp::fromEntity)
                .toList();
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
        return PointOrderResp.fromEntity(requireOrder(id));
    }

    @Override
    public void updateOrderStatus(String id, String status) {
        PointOrder entity = requireOrder(id);
        if (!StringUtils.hasText(status) || !ALLOWED_STATUSES.contains(status)) {
            throw new BizException("POINT_ORDER_STATUS_INVALID", "订单状态不合法");
        }
        entity.setStatus(status);
        pointOrderMapper.updateById(entity);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0L) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }

    private PointOrder requireOrder(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        PointOrder entity = pointOrderMapper.selectById(id);
        if (entity == null) {
            throw new BizException("POINT_ORDER_NOT_FOUND", "兑换订单不存在");
        }
        return entity;
    }
}
