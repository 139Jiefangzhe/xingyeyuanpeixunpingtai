package com.playedu.live.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.live.dto.query.LiveRoomQueryDTO;
import com.playedu.live.dto.req.LiveRoomCreateReq;
import com.playedu.live.dto.resp.LiveRoomResp;
import com.playedu.live.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.function.Function;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/live-rooms")
@Tag(name = "直播间管理", description = "直播中心-直播间元数据管理")
public class LiveRoomController {
    private final LiveRoomService liveRoomService;

    public LiveRoomController(LiveRoomService liveRoomService) {
        this.liveRoomService = liveRoomService;
    }

    @PostMapping
    @Operation(summary = "创建直播间")
    public Result<String> createRoom(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody LiveRoomCreateReq req) {
        return Result.success(liveRoomService.createRoom(userId, req).getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询直播间详情")
    public Result<LiveRoomResp> getRoomById(@PathVariable String id) {
        return Result.success(liveRoomService.getRoomById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询直播间")
    public Result<PageResult<LiveRoomResp>> listRooms(@Valid @ModelAttribute LiveRoomQueryDTO query) {
        Page<LiveRoomResp> page = liveRoomService.listRooms(query);
        return Result.success(PageResult.fromPage(page, Function.identity()));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动直播")
    public Result<Void> startLive(@PathVariable String id, @RequestHeader("X-User-Id") Long userId) {
        liveRoomService.startLive(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "结束直播")
    public Result<Void> stopLive(@PathVariable String id, @RequestHeader("X-User-Id") Long userId) {
        liveRoomService.stopLive(id, userId);
        return Result.success();
    }
}
