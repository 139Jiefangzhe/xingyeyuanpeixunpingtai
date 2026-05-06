package com.playedu.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.query.ExamRecordQueryDTO;
import com.playedu.exam.dto.req.SaveAnswerReq;
import com.playedu.exam.dto.req.StartExamReq;
import com.playedu.exam.dto.resp.ExamRecordListResp;
import com.playedu.exam.dto.resp.ExamResultResp;
import com.playedu.exam.dto.resp.PaperExamStatsResp;
import com.playedu.exam.service.ExamRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.function.Function;
import org.springframework.util.StringUtils;
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
@RequestMapping("/api/v1/exam-records")
@Tag(name = "考试记录", description = "考试中心-考生答题与成绩")
public class ExamRecordController {
    private final ExamRecordService examRecordService;

    public ExamRecordController(ExamRecordService examRecordService) {
        this.examRecordService = examRecordService;
    }

    @PostMapping("/start")
    @Operation(summary = "开始考试")
    public Result<String> startExam(
            @Valid @RequestBody StartExamReq req,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("X-User-Id 请求头不能为空");
        }
        return Result.success(examRecordService.startExam(req.getPaperId(), userId, resolveClientIp(request)));
    }

    @PostMapping("/{recordId}/answers")
    @Operation(summary = "保存答案")
    public Result<Void> saveAnswer(@PathVariable String recordId, @Valid @RequestBody SaveAnswerReq req) {
        examRecordService.saveAnswer(recordId, req.getQuestionId(), req.getAnswer());
        return Result.success();
    }

    @PostMapping("/{recordId}/submit")
    @Operation(summary = "提交考试并返回成绩")
    public Result<ExamResultResp> submitExam(@PathVariable String recordId) {
        examRecordService.submitExam(recordId);
        return Result.success(examRecordService.getExamResult(recordId));
    }

    @GetMapping("/{recordId}/result")
    @Operation(summary = "查询考试结果")
    public Result<ExamResultResp> getResult(@PathVariable String recordId) {
        return Result.success(examRecordService.getExamResult(recordId));
    }

    @GetMapping("/papers/{paperId}/stats")
    @Operation(summary = "按试卷统计考试参与与通过情况")
    public Result<PaperExamStatsResp> getPaperStats(@PathVariable String paperId) {
        return Result.success(examRecordService.getPaperExamStats(paperId));
    }

    @GetMapping
    @Operation(summary = "分页查询考试记录")
    public Result<PageResult<ExamRecordListResp>> listRecords(@Valid @ModelAttribute ExamRecordQueryDTO query) {
        Page<ExamRecordListResp> page = examRecordService.listRecordSummaries(query);
        return Result.success(PageResult.fromPage(page, Function.identity()));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
