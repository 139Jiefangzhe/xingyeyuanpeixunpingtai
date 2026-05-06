package com.playedu.train.dto.resp;

import java.util.List;
import lombok.Data;

@Data
public class ExamPaperStatsFeignResp {
    private String paperId;

    private String paperTitle;

    private Integer passScore;

    private Integer participantCount;

    private Integer passedCount;

    private Integer passRate;

    private List<ExamPaperStudentFeignResp> students;
}
