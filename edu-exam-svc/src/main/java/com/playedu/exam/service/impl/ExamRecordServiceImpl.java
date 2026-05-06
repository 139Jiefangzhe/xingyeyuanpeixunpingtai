package com.playedu.exam.service.impl;

import com.playedu.common.domain.result.Result;
import com.playedu.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.exam.domain.entity.ExamPaperQuestion;
import com.playedu.exam.domain.entity.ExamRecord;
import com.playedu.exam.domain.entity.ExamSession;
import com.playedu.exam.domain.entity.Question;
import com.playedu.exam.dto.query.ExamRecordQueryDTO;
import com.playedu.exam.dto.resp.ExamRecordListResp;
import com.playedu.exam.dto.resp.ExamResultResp;
import com.playedu.exam.dto.resp.PaperExamStatsResp;
import com.playedu.exam.dto.resp.PaperExamStudentResp;
import com.playedu.exam.dto.resp.UserFeignResp;
import com.playedu.exam.feign.UserFeignClient;
import com.playedu.exam.mapper.ExamPaperMapper;
import com.playedu.exam.mapper.ExamPaperQuestionMapper;
import com.playedu.exam.mapper.ExamRecordMapper;
import com.playedu.exam.mapper.ExamSessionMapper;
import com.playedu.exam.mapper.QuestionMapper;
import com.playedu.exam.service.ExamRecordService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class ExamRecordServiceImpl implements ExamRecordService {
    private static final double MULTI_SELECT_PARTIAL_SCORE_RATIO = 0.5D;
    private static final TypeReference<Map<String, String>> ANSWER_MAP_TYPE = new TypeReference<>() {};

    @Value("${edu.local.user-bypass:false}")
    private boolean localUserBypass;

    private final ExamRecordMapper examRecordMapper;
    private final ExamSessionMapper examSessionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final UserFeignClient userFeignClient;
    private final ObjectMapper objectMapper;

    public ExamRecordServiceImpl(
            ExamRecordMapper examRecordMapper,
            ExamSessionMapper examSessionMapper,
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            QuestionMapper questionMapper,
            UserFeignClient userFeignClient,
            ObjectMapper objectMapper) {
        this.examRecordMapper = examRecordMapper;
        this.examSessionMapper = examSessionMapper;
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.questionMapper = questionMapper;
        this.userFeignClient = userFeignClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public String startExam(String paperId, Long userId, String ipAddress) {
        UserFeignResp user = requireUser(userId);
        ExamPaper paper = requirePublishedPaper(paperId);
        LocalDateTime now = LocalDateTime.now();

        ExamRecord activeRecord = findActiveRecord(paperId, userId);
        if (activeRecord != null) {
            ExamSession activeSession = getSessionByRecordIdInternal(activeRecord.getId());
            if (activeSession != null
                    && activeSession.getEndTime() != null
                    && !activeSession.getEndTime().isAfter(now)) {
                submitExam(activeRecord.getId());
            } else {
                throw new IllegalStateException("Current user already has an active exam session: " + userId);
            }
        }

        int totalScore = calculatePaperTotalScore(paperId, paper.getTotalScore());
        ExamRecord record = new ExamRecord();
        record.setExamId(paperId);
        record.setPaperId(paperId);
        record.setUserId(userId);
        record.setDeptId(resolveDeptId(user));
        record.setTotalScore(totalScore);
        record.setObtainScore(0);
        record.setStatus(1);
        record.setStartTime(now);
        record.setIpAddress(ipAddress);
        record.setSwitchCount(0);
        record.setAnswers("{}");
        examRecordMapper.insert(record);

        ExamSession session = new ExamSession();
        session.setRecordId(record.getId());
        session.setExamId(paperId);
        session.setPaperId(paperId);
        session.setUserId(userId);
        session.setStatus(1);
        session.setStartTime(now);
        session.setEndTime(now.plusMinutes(resolveDuration(paper)));
        session.setLastActiveTime(now);
        session.setIpAddress(ipAddress);
        examSessionMapper.insert(session);
        return record.getId();
    }

    @Override
    @Transactional
    public void saveAnswer(String recordId, String questionId, String answer) {
        ExamRecord record = requireActiveRecord(recordId);
        ensureQuestionBelongsToPaper(record.getPaperId(), questionId);

        Map<String, String> answerMap = readAnswers(record.getAnswers());
        if (answer == null) {
            answerMap.remove(questionId);
        } else {
            answerMap.put(questionId, answer);
        }

        record.setAnswers(writeAnswers(answerMap));
        examRecordMapper.updateById(record);
        touchSession(recordId);
    }

    @Override
    @Transactional
    public void submitExam(String recordId) {
        ExamRecord record = requireRecord(recordId);
        if (!Integer.valueOf(1).equals(record.getStatus())) {
            throw new IllegalStateException("Exam record is not in progress: " + recordId);
        }

        LocalDateTime now = LocalDateTime.now();
        record.setStatus(2);
        record.setSubmitTime(now);
        examRecordMapper.updateById(record);
        markSessionSubmitted(recordId, 2, now);

        int obtainScore = autoGrade(recordId);
        log.info("Simulate MQ event exam.result.submitted recordId={}, obtainScore={}", recordId, obtainScore);
    }

    @Override
    @Transactional
    public int autoGrade(String recordId) {
        ExamRecord record = requireRecord(recordId);
        if (Integer.valueOf(1).equals(record.getStatus())) {
            throw new IllegalStateException("Exam record must be submitted before grading: " + recordId);
        }

        ExamResultResp result = buildExamResult(record);
        boolean pendingManualReview =
                result.getDetails().stream()
                        .anyMatch(item -> Boolean.TRUE.equals(item.getPendingManualReview()));

        record.setObtainScore(result.getObtainScore());
        record.setStatus(pendingManualReview ? 2 : 3);
        examRecordMapper.updateById(record);
        return result.getObtainScore();
    }

    @Override
    public ExamResultResp getExamResult(String recordId) {
        return buildExamResult(requireRecord(recordId));
    }

    @Override
    public PaperExamStatsResp getPaperExamStats(String paperId) {
        ExamPaper paper = requirePaper(paperId);
        List<ExamRecord> records = listRecordsByPaperId(paperId);
        Map<Long, ExamRecord> latestRecordMap = new LinkedHashMap<>();
        for (ExamRecord record : records) {
            if (record.getUserId() == null) {
                continue;
            }
            latestRecordMap.merge(record.getUserId(), record, this::pickLatestRecord);
        }

        List<PaperExamStudentResp> students =
                latestRecordMap.values().stream()
                        .sorted(Comparator.comparing(this::resolveRecordTime).reversed())
                        .map(record -> PaperExamStudentResp.fromEntity(record, isPassed(record, paper.getPassScore())))
                        .toList();

        int participantCount = students.size();
        int passedCount = (int) students.stream().filter(item -> Boolean.TRUE.equals(item.getPassed())).count();

        PaperExamStatsResp resp = new PaperExamStatsResp();
        resp.setPaperId(paper.getId());
        resp.setPaperTitle(paper.getTitle());
        resp.setPassScore(paper.getPassScore());
        resp.setParticipantCount(participantCount);
        resp.setPassedCount(passedCount);
        resp.setPassRate(toPercentage(passedCount, participantCount));
        resp.setStudents(students);
        return resp;
    }

    @Override
    public Page<ExamRecord> listRecords(ExamRecordQueryDTO query) {
        Page<ExamRecord> page =
                new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getIsDeleted, 0);
        if (StringUtils.hasText(query.getExamId())) {
            queryWrapper.eq(ExamRecord::getExamId, query.getExamId());
        }
        if (query.getUserId() != null) {
            queryWrapper.eq(ExamRecord::getUserId, query.getUserId());
        }
        if (query.getDeptId() != null) {
            queryWrapper.eq(ExamRecord::getDeptId, query.getDeptId());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(ExamRecord::getStatus, query.getStatus());
        }
        applyRecordSort(queryWrapper, query.getSortField(), query.getSortOrder());
        return examRecordMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<ExamRecordListResp> listRecordSummaries(ExamRecordQueryDTO query) {
        Page<ExamRecord> page = listRecords(query);
        List<ExamRecord> records = page.getRecords() == null ? Collections.emptyList() : page.getRecords();
        List<String> paperIds =
                records.stream()
                        .map(ExamRecord::getPaperId)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .toList();
        Map<String, ExamPaper> paperMap =
                paperIds.isEmpty()
                        ? Collections.emptyMap()
                        : examPaperMapper.selectBatchIds(paperIds).stream()
                                .collect(Collectors.toMap(ExamPaper::getId, item -> item));

        Page<ExamRecordListResp> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(
                records.stream()
                        .map(item -> ExamRecordListResp.fromEntity(item, paperMap.get(item.getPaperId())))
                        .toList());
        return result;
    }

    private UserFeignResp requireUser(Long userId) {
        if (localUserBypass) {
            return buildLocalUser(userId);
        }
        Result<UserFeignResp> result = userFeignClient.getUserById(userId);
        if (result == null) {
            throw new BizException("USER_SVC_UNAVAILABLE", "用户服务响应为空");
        }
        if (!"0".equals(result.getCode())) {
            if ("404001".equals(result.getCode())) {
                throw new BizException("USER_NOT_FOUND", "用户不存在");
            }
            throw new BizException("USER_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "用户服务暂不可用，请稍后重试"));
        }
        if (result.getData() == null) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        return result.getData();
    }

    private UserFeignResp buildLocalUser(Long userId) {
        if (userId == null || userId <= 0L) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        UserFeignResp user = new UserFeignResp();
        user.setId(userId);
        user.setName("local-exam-user-" + userId);
        user.setEmail("local-exam-" + userId + "@playedu.test");
        user.setAvatar(0);
        user.setIsActive(1);
        user.setIsLock(0);
        user.setDeptIds(List.of(resolveLocalDeptId(userId)));
        return user;
    }

    private Long resolveLocalDeptId(Long userId) {
        long normalized = userId == null ? 1L : userId;
        return switch ((int) (Math.floorMod(normalized, 5L))) {
            case 1 -> 10L;
            case 2 -> 20L;
            case 3 -> 30L;
            case 4 -> 40L;
            default -> 50L;
        };
    }

    private Long resolveDeptId(UserFeignResp user) {
        if (user.getDeptIds() == null || user.getDeptIds().isEmpty() || user.getDeptIds().get(0) == null) {
            return 0L;
        }
        return user.getDeptIds().get(0);
    }

    private String defaultMessage(String message, String fallback) {
        return StringUtils.hasText(message) ? message : fallback;
    }

    private ExamResultResp buildExamResult(ExamRecord record) {
        List<ExamPaperQuestion> relations = loadPaperQuestions(record.getPaperId());
        Map<String, Question> questionMap =
                loadQuestionsByIds(
                                relations.stream()
                                        .map(ExamPaperQuestion::getQuestionId)
                                        .filter(StringUtils::hasText)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Question::getId, item -> item));
        Map<String, String> answerMap = readAnswers(record.getAnswers());

        List<ExamResultResp.QuestionResultDetail> details = new ArrayList<>();
        int totalScore = 0;
        int obtainScore = 0;

        for (ExamPaperQuestion relation : relations) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question == null) {
                continue;
            }

            int questionScore = relation.getScore() == null ? defaultScore(question.getScore()) : relation.getScore();
            totalScore += questionScore;

            ScoreResult scoreResult = evaluateQuestion(question, answerMap.get(question.getId()), questionScore);
            obtainScore += scoreResult.obtainScore();

            ExamResultResp.QuestionResultDetail detail = new ExamResultResp.QuestionResultDetail();
            detail.setQuestionId(question.getId());
            detail.setQuestionType(question.getType());
            detail.setContent(question.getContent());
            detail.setScore(questionScore);
            detail.setObtainScore(scoreResult.obtainScore());
            detail.setCorrect(scoreResult.correct());
            detail.setUserAnswer(answerMap.get(question.getId()));
            detail.setCorrectAnswer(question.getAnswer());
            detail.setPendingManualReview(scoreResult.pendingManualReview());
            details.add(detail);
        }

        ExamResultResp resp = new ExamResultResp();
        resp.setRecordId(record.getId());
        resp.setExamId(record.getExamId());
        resp.setPaperId(record.getPaperId());
        resp.setUserId(record.getUserId());
        resp.setTotalScore(record.getTotalScore() == null || record.getTotalScore() == 0 ? totalScore : record.getTotalScore());
        resp.setObtainScore(obtainScore);
        resp.setStatus(record.getStatus());
        resp.setStartTime(record.getStartTime());
        resp.setSubmitTime(record.getSubmitTime());
        resp.setDetails(details);
        return resp;
    }

    private ScoreResult evaluateQuestion(Question question, String userAnswer, int questionScore) {
        Integer questionType = question.getType();
        if (questionType == null) {
            return new ScoreResult(0, Boolean.FALSE, Boolean.TRUE);
        }
        return switch (questionType) {
            case 1, 3 -> evaluateSingleChoice(question.getAnswer(), userAnswer, questionScore);
            case 2 -> evaluateMultiChoice(question.getAnswer(), userAnswer, questionScore);
            default -> new ScoreResult(0, null, Boolean.TRUE);
        };
    }

    private ScoreResult evaluateSingleChoice(String correctAnswer, String userAnswer, int questionScore) {
        boolean matched = normalizeScalarAnswer(correctAnswer).equals(normalizeScalarAnswer(userAnswer));
        return new ScoreResult(matched ? questionScore : 0, matched, Boolean.FALSE);
    }

    private ScoreResult evaluateMultiChoice(String correctAnswer, String userAnswer, int questionScore) {
        Set<String> correctOptions = normalizeOptionSet(correctAnswer);
        Set<String> userOptions = normalizeOptionSet(userAnswer);
        if (correctOptions.equals(userOptions)) {
            return new ScoreResult(questionScore, Boolean.TRUE, Boolean.FALSE);
        }
        if (!userOptions.isEmpty() && correctOptions.containsAll(userOptions)) {
            int partialScore = (int) Math.round(questionScore * MULTI_SELECT_PARTIAL_SCORE_RATIO);
            return new ScoreResult(partialScore, Boolean.FALSE, Boolean.FALSE);
        }
        return new ScoreResult(0, Boolean.FALSE, Boolean.FALSE);
    }

    private String normalizeScalarAnswer(String answer) {
        if (!StringUtils.hasText(answer)) {
            return "";
        }
        return answer.trim().replace("，", ",").replace(" ", "").toUpperCase();
    }

    private Set<String> normalizeOptionSet(String answer) {
        if (!StringUtils.hasText(answer)) {
            return Collections.emptySet();
        }

        String normalized = answer.trim();
        try {
            List<String> options = objectMapper.readValue(normalized, new TypeReference<List<String>>() {});
            return options.stream()
                    .filter(StringUtils::hasText)
                    .map(this::normalizeScalarAnswer)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (JsonProcessingException ignored) {
            return List.of(normalized.replace("，", ",").split(",")).stream()
                    .filter(StringUtils::hasText)
                    .map(this::normalizeScalarAnswer)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private ExamPaper requirePublishedPaper(String paperId) {
        ExamPaper paper = requirePaper(paperId);
        if (!Integer.valueOf(2).equals(paper.getStatus())) {
            throw new IllegalStateException("ExamPaper must be published before starting exam: " + paperId);
        }
        return paper;
    }

    private ExamPaper requirePaper(String paperId) {
        LambdaQueryWrapper<ExamPaper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaper::getId, paperId).eq(ExamPaper::getIsDeleted, 0);
        ExamPaper paper = examPaperMapper.selectOne(queryWrapper);
        if (paper == null) {
            throw new BizException("EXAM_PAPER_NOT_FOUND", "试卷不存在: " + paperId);
        }
        return paper;
    }

    private ExamRecord requireRecord(String recordId) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getId, recordId).eq(ExamRecord::getIsDeleted, 0);
        ExamRecord record = examRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            throw new IllegalArgumentException("ExamRecord not found: " + recordId);
        }
        return record;
    }

    private ExamRecord requireActiveRecord(String recordId) {
        ExamRecord record = requireRecord(recordId);
        if (!Integer.valueOf(1).equals(record.getStatus())) {
            throw new IllegalStateException("Exam record is not active: " + recordId);
        }
        ExamSession session = getSessionByRecordIdInternal(recordId);
        if (session != null && session.getEndTime() != null && !session.getEndTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Exam session has expired: " + recordId);
        }
        return record;
    }

    private ExamRecord findActiveRecord(String paperId, Long userId) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getStatus, 1)
                .eq(ExamRecord::getIsDeleted, 0)
                .last("LIMIT 1");
        return examRecordMapper.selectOne(queryWrapper);
    }

    private List<ExamRecord> listRecordsByPaperId(String paperId) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getIsDeleted, 0)
                .orderByDesc(ExamRecord::getSubmitTime)
                .orderByDesc(ExamRecord::getStartTime)
                .orderByDesc(ExamRecord::getCreateTime);
        List<ExamRecord> records = examRecordMapper.selectList(queryWrapper);
        return records == null ? Collections.emptyList() : records;
    }

    private ExamRecord pickLatestRecord(ExamRecord left, ExamRecord right) {
        return resolveRecordTime(right).isAfter(resolveRecordTime(left)) ? right : left;
    }

    private LocalDateTime resolveRecordTime(ExamRecord record) {
        if (record.getSubmitTime() != null) {
            return record.getSubmitTime();
        }
        if (record.getStartTime() != null) {
            return record.getStartTime();
        }
        return record.getCreateTime() == null ? LocalDateTime.MIN : record.getCreateTime();
    }

    private boolean isPassed(ExamRecord record, Integer passScore) {
        int threshold = passScore == null ? 0 : passScore;
        int score = record.getObtainScore() == null ? 0 : record.getObtainScore();
        return Integer.valueOf(3).equals(record.getStatus()) && score >= threshold;
    }

    private ExamSession getSessionByRecordIdInternal(String recordId) {
        LambdaQueryWrapper<ExamSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamSession::getRecordId, recordId).eq(ExamSession::getIsDeleted, 0).last("LIMIT 1");
        return examSessionMapper.selectOne(queryWrapper);
    }

    private void markSessionSubmitted(String recordId, int status, LocalDateTime submitTime) {
        ExamSession session = getSessionByRecordIdInternal(recordId);
        if (session == null) {
            return;
        }
        session.setStatus(status);
        session.setSubmitTime(submitTime);
        session.setLastActiveTime(submitTime);
        examSessionMapper.updateById(session);
    }

    private void touchSession(String recordId) {
        ExamSession session = getSessionByRecordIdInternal(recordId);
        if (session == null) {
            return;
        }
        session.setLastActiveTime(LocalDateTime.now());
        examSessionMapper.updateById(session);
    }

    private void ensureQuestionBelongsToPaper(String paperId, String questionId) {
        LambdaQueryWrapper<ExamPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaperQuestion::getPaperId, paperId)
                .eq(ExamPaperQuestion::getQuestionId, questionId)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .last("LIMIT 1");
        if (examPaperQuestionMapper.selectOne(queryWrapper) == null) {
            throw new IllegalArgumentException("Question does not belong to paper: " + questionId);
        }
    }

    private List<ExamPaperQuestion> loadPaperQuestions(String paperId) {
        LambdaQueryWrapper<ExamPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaperQuestion::getPaperId, paperId)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .orderByAsc(ExamPaperQuestion::getSort)
                .orderByAsc(ExamPaperQuestion::getCreateTime);
        List<ExamPaperQuestion> relations = examPaperQuestionMapper.selectList(queryWrapper);
        return relations == null ? Collections.emptyList() : relations;
    }

    private List<Question> loadQuestionsByIds(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, questionIds).eq(Question::getIsDeleted, 0);
        List<Question> questions = questionMapper.selectList(queryWrapper);
        return questions == null ? Collections.emptyList() : questions;
    }

    private Map<String, String> readAnswers(String answers) {
        if (!StringUtils.hasText(answers)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, String> value = objectMapper.readValue(answers, ANSWER_MAP_TYPE);
            return value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Exam answers JSON is invalid");
        }
    }

    private String writeAnswers(Map<String, String> answers) {
        Map<String, String> safeAnswers = answers == null ? new HashMap<>() : answers;
        try {
            return objectMapper.writeValueAsString(safeAnswers);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Exam answers JSON serialization failed");
        }
    }

    private int calculatePaperTotalScore(String paperId, Integer fallbackScore) {
        List<ExamPaperQuestion> relations = loadPaperQuestions(paperId);
        if (relations.isEmpty()) {
            return fallbackScore == null ? 0 : fallbackScore;
        }
        return relations.stream()
                .map(ExamPaperQuestion::getScore)
                .filter(item -> item != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private long resolveDuration(ExamPaper paper) {
        return paper.getDuration() == null || paper.getDuration() < 1 ? 60L : paper.getDuration();
    }

    private int defaultScore(Integer score) {
        return score == null ? 0 : score;
    }

    private void applyRecordSort(
            LambdaQueryWrapper<ExamRecord> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortField) ? sortField : "createTime";
        switch (field) {
            case "startTime" -> queryWrapper.orderBy(true, asc, ExamRecord::getStartTime);
            case "submitTime" -> queryWrapper.orderBy(true, asc, ExamRecord::getSubmitTime);
            case "obtainScore" -> queryWrapper.orderBy(true, asc, ExamRecord::getObtainScore);
            default -> queryWrapper.orderBy(true, asc, ExamRecord::getCreateTime);
        }
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }

    private int toPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return (int) Math.round(numerator * 100D / denominator);
    }

    private record ScoreResult(int obtainScore, Boolean correct, Boolean pendingManualReview) {}
}
