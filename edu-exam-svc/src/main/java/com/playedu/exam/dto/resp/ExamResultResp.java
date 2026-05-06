package com.playedu.exam.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ExamResultResp {
    private String recordId;

    private String examId;

    private String paperId;

    private Long userId;

    private Integer totalScore;

    private Integer obtainScore;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private List<QuestionResultDetail> details;

    @Data
    public static class QuestionResultDetail {
        private String questionId;

        private Integer questionType;

        private String content;

        private Integer score;

        private Integer obtainScore;

        private Boolean correct;

        private String userAnswer;

        private String correctAnswer;

        private Boolean pendingManualReview;
    }
}
