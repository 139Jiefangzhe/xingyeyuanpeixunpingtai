package com.playedu.point.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.point.dto.resp.PointProductResp;
import com.playedu.point.service.PointProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/point-products")
@Tag(name = "积分商品管理", description = "积分商城-商品查询")
public class PointProductController {
    private final PointProductService pointProductService;

    public PointProductController(PointProductService pointProductService) {
        this.pointProductService = pointProductService;
    }

    @GetMapping
    @Operation(summary = "积分商品列表")
    public Result<List<PointProductResp>> list() {
        return Result.success(pointProductService.listProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<PointProductResp> detail(@PathVariable String id) {
        return Result.success(pointProductService.getProductById(id));
    }
}

