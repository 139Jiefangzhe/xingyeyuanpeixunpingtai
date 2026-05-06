package com.playedu.exam.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamPaperQueryDTO {
    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    @Min(1)
    @Max(3)
    private Integer status;

    @Min(1)
    @Max(2)
    private Integer type;

    private String titleLike;

    @Size(max = 32)
    @Pattern(regexp = "createTime|title|status|type|totalScore")
    private String sortField = "createTime";

    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String sortOrder = "desc";
}
