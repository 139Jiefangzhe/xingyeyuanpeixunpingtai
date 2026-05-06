package com.playedu.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.query.ExamPaperQueryDTO;
import com.playedu.exam.dto.req.ExamPaperCreateReq;
import com.playedu.exam.dto.req.ExamPaperUpdateReq;
import com.playedu.exam.dto.req.PaperGenerateReq;
import com.playedu.exam.dto.req.PaperQuestionReq;
import com.playedu.exam.dto.resp.ExamPaperDetailResp;
import com.playedu.exam.dto.resp.ExamPaperSimpleResp;
import com.playedu.exam.service.ExamPaperQuestionService;
import com.playedu.exam.service.ExamPaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/exam-papers")
@Tag(name = "试卷管理", description = "考试中心-试卷相关接口")
public class ExamPaperController {
    private final ExamPaperService examPaperService;
    private final ExamPaperQuestionService examPaperQuestionService;

    public ExamPaperController(
            ExamPaperService examPaperService, ExamPaperQuestionService examPaperQuestionService) {
        this.examPaperService = examPaperService;
        this.examPaperQuestionService = examPaperQuestionService;
    }

    @PostMapping
    @Operation(summary = "创建试卷")
    public Result<String> createPaper(@Valid @RequestBody ExamPaperCreateReq req) {
        ExamPaper paper = examPaperService.createPaper(req);
        return Result.success(paper.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询试卷详情")
    public Result<ExamPaperDetailResp> getPaperById(@PathVariable String id) {
        return Result.success(examPaperService.getPaperById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新试卷")
    public Result<Void> updatePaper(@PathVariable String id, @Valid @RequestBody ExamPaperUpdateReq req) {
        examPaperService.updatePaper(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除试卷")
    public Result<Void> deletePaper(@PathVariable String id) {
        examPaperService.deletePaper(id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "分页查询试卷")
    public Result<PageResult<ExamPaperSimpleResp>> listPapers(@Valid @ModelAttribute ExamPaperQueryDTO query) {
        Page<ExamPaper> page = examPaperService.listPapers(query);
        PageResult<ExamPaperSimpleResp> data = PageResult.fromPage(page, ExamPaperSimpleResp::fromEntity);
        return Result.success(data);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布试卷")
    public Result<Void> publishPaper(@PathVariable String id) {
        examPaperService.publishPaper(id);
        return Result.success();
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制试卷")
    public Result<String> copyPaper(@PathVariable String id) {
        ExamPaper paper = examPaperService.copyPaper(id);
        return Result.success(paper.getId());
    }

    @PostMapping("/{id}/questions")
    @Operation(summary = "批量添加题目到试卷")
    public Result<Void> addQuestionsToPaper(
            @PathVariable String id, @Valid @RequestBody List<PaperQuestionReq> questions) {
        examPaperQuestionService.addQuestionsToPaper(id, questions);
        return Result.success();
    }

    @DeleteMapping("/{id}/questions")
    @Operation(summary = "从试卷移除题目")
    public Result<Void> removeQuestionsFromPaper(
            @PathVariable String id, @RequestBody List<String> questionIds) {
        examPaperQuestionService.removeQuestionsFromPaper(id, questionIds);
        return Result.success();
    }

    @PutMapping("/{id}/questions/reorder")
    @Operation(summary = "调整试卷题目顺序")
    public Result<Void> reorderQuestions(
            @PathVariable String id, @RequestBody List<String> questionIdsInOrder) {
        examPaperQuestionService.reorderQuestions(id, questionIdsInOrder);
        return Result.success();
    }

    @PostMapping("/generate")
    @Operation(summary = "智能组卷")
    public Result<String> generatePaper(@Valid @RequestBody PaperGenerateReq req) {
        ExamPaper paper = examPaperService.generatePaper(req);
        return Result.success(paper.getId());
    }
}
