package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamPaperCreateReq {
    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    @Min(0)
    private Integer passScore;

    @NotNull
    @Min(0)
    private Integer totalScore;

    @NotNull
    private Integer type;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer allowRedo;

    private String knowledgeConfig;
}
