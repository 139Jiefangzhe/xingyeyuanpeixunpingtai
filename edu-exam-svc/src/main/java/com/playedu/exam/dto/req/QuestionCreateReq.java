package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionCreateReq {
    @NotBlank
    private String bankId;

    @NotNull
    private Integer type;

    @NotBlank
    private String content;

    private String options;

    @NotBlank
    private String answer;

    private String analysis;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer difficulty;

    @Size(max = 200)
    private String knowledgePoint;

    @NotNull
    @Min(1)
    private Integer score;
}
