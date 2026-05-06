package com.playedu.exam.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionUpdateReq {
    private String bankId;

    private Integer type;

    private String content;

    private String options;

    private String answer;

    private String analysis;

    @Min(1)
    @Max(5)
    private Integer difficulty;

    @Size(max = 200)
    private String knowledgePoint;

    @Min(1)
    private Integer score;
}
