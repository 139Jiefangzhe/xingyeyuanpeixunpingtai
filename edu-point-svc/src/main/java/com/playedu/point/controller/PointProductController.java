package com.playedu.point.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.point.dto.req.PointProductSaveReq;
import com.playedu.point.dto.resp.PointProductResp;
import com.playedu.point.service.PointProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
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
    public Result<List<PointProductResp>> list(
            @RequestParam(name = "all", defaultValue = "false") boolean all) {
        return Result.success(pointProductService.listProducts(all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<PointProductResp> detail(@PathVariable String id) {
        return Result.success(pointProductService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "创建积分商品")
    public Result<String> create(@Valid @RequestBody PointProductSaveReq req) {
        return Result.success(pointProductService.createProduct(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新积分商品")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody PointProductSaveReq req) {
        pointProductService.updateProduct(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除积分商品")
    public Result<Void> delete(@PathVariable String id) {
        pointProductService.deleteProduct(id);
        return Result.success();
    }
}
