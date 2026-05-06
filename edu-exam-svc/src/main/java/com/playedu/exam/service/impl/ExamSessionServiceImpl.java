package com.playedu.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.exam.domain.entity.ExamSession;
import com.playedu.exam.dto.resp.ExamResultResp;
import com.playedu.exam.dto.resp.SessionStatusResp;
import java.time.Duration;
import com.playedu.exam.service.ExamRecordService;
import com.playedu.exam.service.ExamSessionService;
import com.playedu.exam.mapper.ExamSessionMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class ExamSessionServiceImpl implements ExamSessionService {
    private final ExamSessionMapper examSessionMapper;
    private final ExamRecordService examRecordService;

    public ExamSessionServiceImpl(ExamSessionMapper examSessionMapper, ExamRecordService examRecordService) {
        this.examSessionMapper = examSessionMapper;
        this.examRecordService = examRecordService;
    }

    @Override
    public ExamSession getSessionByRecordId(String recordId) {
        LambdaQueryWrapper<ExamSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamSession::getRecordId, recordId).eq(ExamSession::getIsDeleted, 0).last("LIMIT 1");
        ExamSession session = examSessionMapper.selectOne(queryWrapper);
        if (session == null) {
            throw new IllegalArgumentException("ExamSession not found for record: " + recordId);
        }
        return session;
    }

    @Override
    public SessionStatusResp getSessionStatus(String recordId) {
        ExamSession session = getSessionByRecordId(recordId);
        ExamResultResp result = examRecordService.getExamResult(recordId);
        List<ExamResultResp.QuestionResultDetail> details =
                result.getDetails() == null ? Collections.emptyList() : result.getDetails();

        int answeredCount =
                (int) details.stream()
                        .map(ExamResultResp.QuestionResultDetail::getUserAnswer)
                        .filter(StringUtils::hasText)
                        .count();
        boolean submitted =
                !Integer.valueOf(1).equals(result.getStatus()) || !Integer.valueOf(1).equals(session.getStatus());

        SessionStatusResp resp = new SessionStatusResp();
        resp.setRemainingTime(submitted ? 0L : resolveRemainingTime(session));
        resp.setCurrentQuestionIndex(resolveCurrentQuestionIndex(details));
        resp.setAnsweredCount(answeredCount);
        resp.setTotalCount(details.size());
        resp.setIsSubmitted(submitted);
        return resp;
    }

    @Override
    @Transactional
    public int autoSubmitTimeoutExams() {
        LambdaQueryWrapper<ExamSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamSession::getStatus, 1)
                .eq(ExamSession::getIsDeleted, 0)
                .le(ExamSession::getEndTime, LocalDateTime.now())
                .orderByAsc(ExamSession::getEndTime);
        List<ExamSession> sessions = examSessionMapper.selectList(queryWrapper);
        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (ExamSession session : sessions == null ? Collections.<ExamSession>emptyList() : sessions) {
            try {
                examRecordService.submitExam(session.getRecordId());
                session.setStatus(3);
                session.setSubmitTime(LocalDateTime.now());
                session.setLastActiveTime(LocalDateTime.now());
                examSessionMapper.updateById(session);
                count++;
            } catch (Exception ex) {
                log.error("Auto submit timeout exam failed, recordId={}", session.getRecordId(), ex);
            }
        }
        return count;
    }

    private long resolveRemainingTime(ExamSession session) {
        if (session.getEndTime() == null) {
            return 0L;
        }
        long seconds = Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
        return Math.max(seconds, 0L);
    }

    private int resolveCurrentQuestionIndex(List<ExamResultResp.QuestionResultDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        for (int index = 0; index < details.size(); index++) {
            if (!StringUtils.hasText(details.get(index).getUserAnswer())) {
                return index + 1;
            }
        }
        return details.size();
    }
}
