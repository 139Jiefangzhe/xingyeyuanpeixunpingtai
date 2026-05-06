package com.playedu.exam.controller;

import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.resp.SessionStatusResp;
import com.playedu.exam.service.ExamSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/exam-sessions")
@Tag(name = "考试会话", description = "考试中心-会话管理")
public class ExamSessionController {
    private final ExamSessionService examSessionService;

    public ExamSessionController(ExamSessionService examSessionService) {
        this.examSessionService = examSessionService;
    }

    @GetMapping("/{recordId}/status")
    @Operation(summary = "查询考试会话状态")
    public Result<SessionStatusResp> getSessionStatus(@PathVariable String recordId) {
        return Result.success(examSessionService.getSessionStatus(recordId));
    }
}
