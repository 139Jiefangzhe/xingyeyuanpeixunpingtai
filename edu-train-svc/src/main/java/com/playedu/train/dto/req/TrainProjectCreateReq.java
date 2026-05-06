package com.playedu.train.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrainProjectCreateReq {
    @NotBlank(message = "培训项目标题不能为空")
    @Size(max = 200, message = "培训项目标题长度不能超过200字符")
    private String title;

    @Size(max = 500, message = "培训项目描述长度不能超过500字符")
    private String description;

    @NotNull(message = "培训项目类型不能为空")
    @Min(value = 1, message = "培训项目类型不合法")
    @Max(value = 3, message = "培训项目类型不合法")
    private Integer type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "指派范围不能为空")
    @Min(value = 1, message = "指派范围不合法")
    @Max(value = 3, message = "指派范围不合法")
    private Integer assigneeScope;

    @Size(max = 1000, message = "目标部门ID列表长度不能超过1000字符")
    private String targetDeptIds;
}
