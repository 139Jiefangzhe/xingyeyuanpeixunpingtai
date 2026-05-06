package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaperQuestionReq {
    @NotBlank
    private String questionId;

    @Min(0)
    private Integer sort;

    @Min(0)
    private Integer score;
}
