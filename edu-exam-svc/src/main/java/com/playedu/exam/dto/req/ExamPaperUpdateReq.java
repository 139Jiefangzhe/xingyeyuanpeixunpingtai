package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamPaperUpdateReq {
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;

    @Min(1)
    private Integer duration;

    @Min(0)
    private Integer passScore;

    @Min(0)
    private Integer totalScore;

    private Integer type;

    @Min(0)
    @Max(1)
    private Integer allowRedo;

    private String knowledgeConfig;
}
