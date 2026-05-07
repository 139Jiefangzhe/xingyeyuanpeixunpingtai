package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.point.domain.PointProduct;
import com.playedu.point.dto.req.PointProductSaveReq;
import com.playedu.point.dto.resp.PointProductResp;
import com.playedu.point.mapper.PointProductMapper;
import com.playedu.point.service.PointProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Service
public class PointProductServiceImpl implements PointProductService {
    private final PointProductMapper pointProductMapper;

    public PointProductServiceImpl(PointProductMapper pointProductMapper) {
        this.pointProductMapper = pointProductMapper;
    }

    @Override
    public List<PointProductResp> listProducts(boolean includeDisabled) {
        LambdaQueryWrapper<PointProduct> queryWrapper =
                new LambdaQueryWrapper<PointProduct>().eq(PointProduct::getIsDel, 0);
        if (!includeDisabled) {
            queryWrapper.eq(PointProduct::getStatus, 1);
        }
        queryWrapper.orderByAsc(PointProduct::getSort).orderByDesc(PointProduct::getCreateTime);
        return pointProductMapper.selectList(queryWrapper)
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

    @Override
    public String createProduct(PointProductSaveReq req) {
        PointProduct entity = new PointProduct();
        fillEntity(entity, req);
        entity.setIsDel(0);
        pointProductMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateProduct(String id, PointProductSaveReq req) {
        PointProduct entity = requireProduct(id);
        fillEntity(entity, req);
        pointProductMapper.updateById(entity);
    }

    @Override
    public void deleteProduct(String id) {
        requireProduct(id);
        pointProductMapper.deleteById(id);
    }

    private PointProduct requireProduct(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        PointProduct entity = pointProductMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getIsDel())) {
            throw new BizException("POINT_PRODUCT_NOT_FOUND", "积分商品不存在");
        }
        return entity;
    }

    private void fillEntity(PointProduct entity, PointProductSaveReq req) {
        entity.setName(req.getName().trim());
        entity.setDescription(req.getDescription());
        entity.setImageUrl(req.getImageUrl());
        entity.setPointsPrice(req.getPointsPrice());
        entity.setStock(req.getStock());
        entity.setStatus(ObjectUtils.isEmpty(req.getStatus()) ? 1 : req.getStatus());
        entity.setSort(ObjectUtils.isEmpty(req.getSort()) ? 0 : req.getSort());
    }
}
