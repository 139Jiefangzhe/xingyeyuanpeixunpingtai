package com.playedu.course.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CourseChapterReq {
    @NotBlank(message = "章节名称不能为空")
    @Size(max = 200, message = "章节名称长度不能超过200字符")
    private String name;

    @Valid
    private List<CourseLessonReq> lessons = new ArrayList<>();
}
