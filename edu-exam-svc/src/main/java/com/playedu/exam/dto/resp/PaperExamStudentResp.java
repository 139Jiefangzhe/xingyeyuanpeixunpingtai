package com.playedu.exam.dto.resp;

import com.playedu.exam.domain.entity.ExamRecord;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaperExamStudentResp {
    private String recordId;

    private Long userId;

    private Integer status;

    private Integer obtainScore;

    private Boolean passed;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    public static PaperExamStudentResp fromEntity(ExamRecord record, boolean passed) {
        PaperExamStudentResp resp = new PaperExamStudentResp();
        resp.setRecordId(record.getId());
        resp.setUserId(record.getUserId());
        resp.setStatus(record.getStatus());
        resp.setObtainScore(record.getObtainScore());
        resp.setPassed(passed);
        resp.setStartTime(record.getStartTime());
        resp.setSubmitTime(record.getSubmitTime());
        return resp;
    }
}
