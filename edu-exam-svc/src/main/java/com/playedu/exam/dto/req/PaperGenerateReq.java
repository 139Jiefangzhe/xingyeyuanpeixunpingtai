package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaperGenerateReq {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String bankId;

    @NotBlank
    private String knowledgeConfig;

    @NotNull
    @Min(0)
    private Integer totalScore;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    @Min(0)
    private Integer passScore;
}
