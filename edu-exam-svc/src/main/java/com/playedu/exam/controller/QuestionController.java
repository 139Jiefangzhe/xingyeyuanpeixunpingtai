package com.playedu.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playedu.exam.domain.entity.Question;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.query.QuestionQueryDTO;
import com.playedu.exam.dto.req.QuestionCreateReq;
import com.playedu.exam.dto.req.QuestionUpdateReq;
import com.playedu.exam.dto.resp.QuestionResp;
import com.playedu.exam.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "题库管理", description = "考试中心-题库相关接口")
public class QuestionController {
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    public QuestionController(QuestionService questionService, ObjectMapper objectMapper) {
        this.questionService = questionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @Operation(summary = "创建题目")
    public Result<String> createQuestion(@Valid @RequestBody QuestionCreateReq req) {
        Question question = questionService.createQuestion(req);
        return Result.success(question.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询题目详情")
    public Result<QuestionResp> getQuestionById(@PathVariable String id) {
        Question question = questionService.getQuestionById(id);
        return Result.success(QuestionResp.fromEntity(question, objectMapper));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目")
    public Result<Void> updateQuestion(@PathVariable String id, @Valid @RequestBody QuestionUpdateReq req) {
        questionService.updateQuestion(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "分页查询题目")
    public Result<PageResult<QuestionResp>> listQuestions(@Valid @ModelAttribute QuestionQueryDTO query) {
        Page<Question> page = questionService.listQuestions(query);
        PageResult<QuestionResp> data =
                PageResult.fromPage(page, item -> QuestionResp.fromEntity(item, objectMapper));
        return Result.success(data);
    }
}
