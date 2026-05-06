package com.playedu.course.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CourseQueryDTO {
    @Min(value = 1, message = "pageNum必须大于0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize必须大于0")
    @Max(value = 100, message = "pageSize不能超过100")
    private Integer pageSize = 10;

    private String titleLike;

    private Integer type;

    private Integer categoryId;

    private Integer isRequired;

    private Integer isShow;

    private String sortField;

    private String sortOrder;
}
