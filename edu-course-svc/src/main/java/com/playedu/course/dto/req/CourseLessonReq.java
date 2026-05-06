package com.playedu.course.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseLessonReq {
    @NotBlank(message = "课节标题不能为空")
    @Size(max = 200, message = "课节标题长度不能超过200字符")
    private String title;

    @Size(max = 512, message = "资源URL长度不能超过512字符")
    private String resourceUrl;

    @Min(value = 0, message = "课节时长不能为负数")
    @Max(value = 86400, message = "课节时长不能超过86400秒")
    private Integer duration;
}
