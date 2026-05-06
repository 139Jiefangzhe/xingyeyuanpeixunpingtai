package com.playedu.user.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.user.dto.resp.DepartmentTreeResp;
import com.playedu.user.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "部门查询", description = "用户中心-只读部门查询接口")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/tree")
    @Operation(summary = "查询部门树")
    public Result<List<DepartmentTreeResp>> getDeptTree() {
        return Result.success(departmentService.getDeptTree());
    }
}
