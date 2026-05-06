package com.playedu.train.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TrainTaskReq {
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200字符")
    private String name;

    @NotNull(message = "任务类型不能为空")
    @Min(value = 1, message = "任务类型不合法")
    @Max(value = 4, message = "任务类型不合法")
    private Integer type;

    @NotBlank(message = "关联资源ID不能为空")
    @Size(max = 64, message = "关联资源ID长度不能超过64字符")
    private String refId;

    @Min(value = 1, message = "任务排序必须大于0")
    private Integer sort;

    @NotNull(message = "是否必做不能为空")
    @Min(value = 0, message = "是否必做取值不合法")
    @Max(value = 1, message = "是否必做取值不合法")
    private Integer required;

    @NotNull(message = "完成标准不能为空")
    @Min(value = 1, message = "完成标准不合法")
    @Max(value = 3, message = "完成标准不合法")
    private Integer passRule;
}
