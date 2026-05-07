package com.playedu.point.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointRuleStatusUpdateReq {
    @NotNull(message = "规则状态不能为空")
    @Min(value = 0, message = "规则状态不合法")
    @Max(value = 1, message = "规则状态不合法")
    private Integer status;
}
