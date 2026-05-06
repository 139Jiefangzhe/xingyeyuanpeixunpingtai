package com.playedu.course.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CourseSaveReq {
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 200, message = "课程标题长度不能超过200字符")
    private String title;

    @Size(max = 500, message = "课程描述长度不能超过500字符")
    private String shortDesc;

    @Size(max = 512, message = "封面URL长度不能超过512字符")
    private String coverUrl;

    @NotNull(message = "课程类型不能为空")
    @Min(value = 1, message = "课程类型不合法")
    @Max(value = 3, message = "课程类型不合法")
    private Integer type;

    @NotEmpty(message = "课程分类不能为空")
    private List<@NotNull(message = "课程分类不能为空") @Min(value = 1, message = "课程分类不合法") Integer> categoryIds =
            new ArrayList<>();

    @NotNull(message = "课程状态不能为空")
    @Min(value = 0, message = "课程状态不合法")
    @Max(value = 1, message = "课程状态不合法")
    private Integer isShow;

    @NotNull(message = "课时数不能为空")
    @Min(value = 0, message = "课时数不能为负数")
    @Max(value = 10000, message = "课时数不能超过10000")
    private Integer classHour;

    @Valid
    private List<CourseChapterReq> chapters = new ArrayList<>();
}
