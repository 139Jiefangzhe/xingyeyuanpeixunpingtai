package com.playedu.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.domain.result.PageResult;
import com.playedu.common.domain.result.Result;
import com.playedu.course.dto.query.CourseQueryDTO;
import com.playedu.course.dto.req.CourseSaveReq;
import com.playedu.course.dto.resp.CourseCategoryOptionResp;
import com.playedu.course.dto.resp.CourseChapterResp;
import com.playedu.course.dto.resp.CourseDetailResp;
import com.playedu.course.dto.resp.CourseSimpleResp;
import com.playedu.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.function.Function;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "课程查询", description = "课程中心-只读课程查询接口")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(summary = "创建课程")
    public Result<Integer> createCourse(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CourseSaveReq req) {
        return Result.success(courseService.createCourse(userId, req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程")
    public Result<Void> updateCourse(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CourseSaveReq req) {
        courseService.updateCourse(id, userId, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public Result<Void> deleteCourse(@PathVariable Integer id, @RequestHeader("X-User-Id") Long userId) {
        courseService.deleteCourse(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询课程详情")
    public Result<CourseDetailResp> getCourseById(@PathVariable Integer id) {
        return Result.success(courseService.getCourseById(id));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "查询课程完整详情")
    public Result<CourseDetailResp> getCourseDetail(@PathVariable Integer id) {
        return Result.success(courseService.getCourseById(id));
    }

    @GetMapping("/{id}/chapters")
    @Operation(summary = "查询课程章节列表")
    public Result<List<CourseChapterResp>> getCourseChapters(@PathVariable Integer id) {
        return Result.success(courseService.getCourseChapters(id));
    }

    @GetMapping
    @Operation(summary = "分页查询课程")
    public Result<PageResult<CourseSimpleResp>> listCourses(@Valid @ModelAttribute CourseQueryDTO query) {
        Page<CourseSimpleResp> page = courseService.listCourses(query);
        return Result.success(PageResult.fromPage(page, Function.identity()));
    }

    @GetMapping("/categories")
    @Operation(summary = "查询课程分类选项")
    public Result<List<CourseCategoryOptionResp>> listCategoryOptions() {
        return Result.success(courseService.listCategoryOptions());
    }
}
