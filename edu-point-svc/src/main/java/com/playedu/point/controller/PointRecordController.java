package com.playedu.point.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.point.dto.resp.PointRecordResp;
import com.playedu.point.service.PointRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/point-records")
@Tag(name = "积分流水", description = "积分商城-积分流水查询")
public class PointRecordController {
    private final PointRecordService pointRecordService;

    public PointRecordController(PointRecordService pointRecordService) {
        this.pointRecordService = pointRecordService;
    }

    @GetMapping("/my")
    @Operation(summary = "我的积分流水")
    public Result<List<PointRecordResp>> myRecords(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(pointRecordService.listMyRecords(userId));
    }

    @GetMapping("/balance")
    @Operation(summary = "查询学员当前积分余额")
    public Result<Integer> getBalance(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(pointRecordService.getBalance(userId));
    }
}

