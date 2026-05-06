package com.playedu.exam.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionQueryDTO {
    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String bankId;

    private Integer type;

    private Integer difficulty;

    private String knowledgePointLike;

    private String contentLike;

    @Size(max = 32)
    @Pattern(regexp = "createTime|difficulty|score|type")
    private String sortField = "createTime";

    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String sortOrder = "desc";
}
