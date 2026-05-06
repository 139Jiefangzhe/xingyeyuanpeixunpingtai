package com.playedu.exam.dto.resp;

import java.util.List;
import lombok.Data;

@Data
public class PaperExamStatsResp {
    private String paperId;

    private String paperTitle;

    private Integer passScore;

    private Integer participantCount;

    private Integer passedCount;

    private Integer passRate;

    private List<PaperExamStudentResp> students;
}
