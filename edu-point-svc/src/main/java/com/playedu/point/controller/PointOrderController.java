package com.playedu.point.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.point.dto.req.PointOrderStatusUpdateReq;
import com.playedu.point.dto.resp.PointOrderResp;
import com.playedu.point.service.PointOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/point-orders")
@Tag(name = "兑换订单管理", description = "积分商城-兑换订单查询")
public class PointOrderController {
    private final PointOrderService pointOrderService;

    public PointOrderController(PointOrderService pointOrderService) {
        this.pointOrderService = pointOrderService;
    }

    @GetMapping
    @Operation(summary = "兑换订单列表")
    public Result<List<PointOrderResp>> list() {
        return Result.success(pointOrderService.listOrders());
    }

    @GetMapping("/my")
    @Operation(summary = "我的兑换订单")
    public Result<List<PointOrderResp>> myOrders(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(pointOrderService.listMyOrders(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<PointOrderResp> detail(@PathVariable String id) {
        return Result.success(pointOrderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态")
    public Result<Void> updateStatus(
            @PathVariable String id, @Valid @RequestBody PointOrderStatusUpdateReq req) {
        pointOrderService.updateOrderStatus(id, req.getStatus());
        return Result.success();
    }
}
