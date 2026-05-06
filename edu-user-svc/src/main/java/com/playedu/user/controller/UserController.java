package com.playedu.user.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.user.dto.resp.UserResp;
import com.playedu.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户查询", description = "用户中心-只读用户查询接口")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询单个用户")
    public Result<UserResp> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量查询用户")
    public Result<List<UserResp>> batchGetUsers(
            @RequestBody @NotEmpty(message = "userIds不能为空") List<@NotNull(message = "userId不能为空") Long> userIds) {
        return Result.success(userService.batchGetUsers(userIds));
    }
}
