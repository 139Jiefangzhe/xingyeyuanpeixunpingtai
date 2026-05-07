package com.playedu.train.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.query.TrainProjectQueryDTO;
import com.playedu.train.dto.req.TrainProjectCreateReq;
import com.playedu.train.dto.req.TrainTaskReq;
import com.playedu.train.dto.resp.ProjectStatsResp;
import com.playedu.train.dto.resp.TrainProjectDetailResp;
import com.playedu.train.dto.resp.TrainProjectListResp;
import com.playedu.train.dto.resp.TrainProjectMyDetailDTO;
import com.playedu.train.service.TrainProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
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
@RequestMapping("/api/v1/train-projects")
@Tag(name = "培训项目", description = "培训中心-培训项目管理")
public class TrainProjectController {
    private final TrainProjectService trainProjectService;

    public TrainProjectController(TrainProjectService trainProjectService) {
        this.trainProjectService = trainProjectService;
    }

    @PostMapping
    @Operation(summary = "创建培训项目")
    public Result<String> createProject(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody TrainProjectCreateReq req) {
        return Result.success(trainProjectService.createProject(userId, req).getId());
    }

    @PostMapping("/{id}/tasks")
    @Operation(summary = "批量添加培训任务")
    public Result<Void> addTasks(
            @PathVariable String id, @RequestBody @NotEmpty(message = "任务列表不能为空") List<@Valid TrainTaskReq> tasks) {
        trainProjectService.addTasks(id, tasks);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布培训项目")
    public Result<Void> publishProject(@PathVariable String id) {
        trainProjectService.publishProject(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询培训项目详情")
    public Result<TrainProjectDetailResp> getProjectDetail(@PathVariable String id) {
        return Result.success(trainProjectService.getProjectDetail(id));
    }

    @GetMapping("/{id}/my-detail")
    @Operation(summary = "查询学员视角的培训项目详情")
    public Result<TrainProjectMyDetailDTO> getMyProjectDetail(
            @PathVariable String id, @RequestHeader("X-User-Id") Long userId) {
        return Result.success(trainProjectService.getMyProjectDetail(id, userId));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "查询培训项目效果统计")
    public Result<ProjectStatsResp> getProjectStats(@PathVariable String id) {
        return Result.success(trainProjectService.getProjectStats(id));
    }

    @GetMapping
    @Operation(summary = "分页查询培训项目")
    public Result<PageResult<TrainProjectListResp>> listProjects(@Valid @ModelAttribute TrainProjectQueryDTO query) {
        Page<TrainProjectListResp> page = trainProjectService.listProjects(query);
        return Result.success(PageResult.fromPage(page, Function.identity()));
    }
}
