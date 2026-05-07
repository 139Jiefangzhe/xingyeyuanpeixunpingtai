package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.point.domain.PointProduct;
import com.playedu.point.dto.resp.PointProductResp;
import com.playedu.point.mapper.PointProductMapper;
import com.playedu.point.service.PointProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PointProductServiceImpl implements PointProductService {
    private final PointProductMapper pointProductMapper;

    public PointProductServiceImpl(PointProductMapper pointProductMapper) {
        this.pointProductMapper = pointProductMapper;
    }

    @Override
    public List<PointProductResp> listProducts() {
        return pointProductMapper.selectList(
                        new LambdaQueryWrapper<PointProduct>()
                                .eq(PointProduct::getIsDel, 0)
                                .eq(PointProduct::getStatus, 1)
                                .orderByAsc(PointProduct::getSort))
                .stream()
                .map(PointProductResp::fromEntity)
                .toList();
    }

    @Override
    public PointProductResp getProductById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        PointProduct entity = pointProductMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getIsDel())) {
            throw new BizException("POINT_PRODUCT_NOT_FOUND", "积分商品不存在");
        }
        return PointProductResp.fromEntity(entity);
    }
}

