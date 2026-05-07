package com.playedu.point.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PointProductSaveReq {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String name;

    @Size(max = 1000, message = "商品描述长度不能超过1000个字符")
    private String description;

    @Size(max = 500, message = "商品图片地址长度不能超过500个字符")
    private String imageUrl;

    @NotNull(message = "积分价格不能为空")
    @Min(value = 1, message = "积分价格必须大于0")
    private Integer pointsPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Min(value = 0, message = "商品状态不合法")
    private Integer status;

    @Min(value = 0, message = "排序号不能为负数")
    private Integer sort;
}
