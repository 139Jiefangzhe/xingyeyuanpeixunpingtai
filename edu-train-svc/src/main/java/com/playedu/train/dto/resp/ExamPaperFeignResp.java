package com.playedu.train.dto.resp;

import lombok.Data;

@Data
public class ExamPaperFeignResp {
    private String id;

    private String title;

    private Integer status;

    private Integer totalScore;

    private Integer duration;
}
