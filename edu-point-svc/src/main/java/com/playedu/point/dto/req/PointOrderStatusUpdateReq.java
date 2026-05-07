package com.playedu.point.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PointOrderStatusUpdateReq {
    @NotBlank(message = "订单状态不能为空")
    private String status;
}
