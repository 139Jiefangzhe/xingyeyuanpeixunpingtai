package com.playedu.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.ExamRecord;
import com.playedu.exam.dto.query.ExamRecordQueryDTO;
import com.playedu.exam.dto.resp.ExamRecordListResp;
import com.playedu.exam.dto.resp.PaperExamStatsResp;
import com.playedu.exam.dto.resp.ExamResultResp;

public interface ExamRecordService {
    String startExam(String paperId, Long userId, String ipAddress);

    void saveAnswer(String recordId, String questionId, String answer);

    void submitExam(String recordId);

    int autoGrade(String recordId);

    ExamResultResp getExamResult(String recordId);

    PaperExamStatsResp getPaperExamStats(String paperId);

    Page<ExamRecord> listRecords(ExamRecordQueryDTO query);

    Page<ExamRecordListResp> listRecordSummaries(ExamRecordQueryDTO query);
}
