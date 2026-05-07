package com.playedu.point.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.point.dto.req.PointRuleStatusUpdateReq;
import com.playedu.point.dto.resp.PointRuleResp;
import com.playedu.point.service.PointRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/point-rules")
@Tag(name = "积分规则管理", description = "积分商城-积分规则查询")
public class PointRuleController {
    private final PointRuleService pointRuleService;

    public PointRuleController(PointRuleService pointRuleService) {
        this.pointRuleService = pointRuleService;
    }

    @GetMapping
    @Operation(summary = "积分规则列表")
    public Result<List<PointRuleResp>> list() {
        return Result.success(pointRuleService.listRules());
    }

    @GetMapping("/{id}")
    @Operation(summary = "积分规则详情")
    public Result<PointRuleResp> detail(@PathVariable String id) {
        return Result.success(pointRuleService.getRuleById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新积分规则状态")
    public Result<Void> updateStatus(
            @PathVariable String id, @Valid @RequestBody PointRuleStatusUpdateReq req) {
        pointRuleService.updateStatus(id, req.getStatus());
        return Result.success();
    }
}
