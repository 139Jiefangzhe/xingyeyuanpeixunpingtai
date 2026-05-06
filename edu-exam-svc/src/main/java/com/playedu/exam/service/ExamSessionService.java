package com.playedu.exam.service;

import com.playedu.exam.domain.entity.ExamSession;
import com.playedu.exam.dto.resp.SessionStatusResp;

public interface ExamSessionService {
    ExamSession getSessionByRecordId(String recordId);

    SessionStatusResp getSessionStatus(String recordId);

    int autoSubmitTimeoutExams();
}
