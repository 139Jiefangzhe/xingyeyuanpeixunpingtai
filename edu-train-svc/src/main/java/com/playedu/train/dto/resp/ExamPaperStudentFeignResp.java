package com.playedu.train.dto.resp;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ExamPaperStudentFeignResp {
    private String recordId;

    private Long userId;

    private Integer status;

    private Integer obtainScore;

    private Boolean passed;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;
}
