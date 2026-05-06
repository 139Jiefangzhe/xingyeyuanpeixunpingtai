package com.playedu.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.exam.domain.entity.ExamPaperQuestion;
import com.playedu.exam.domain.entity.Question;
import com.playedu.exam.dto.req.PaperQuestionReq;
import com.playedu.exam.mapper.ExamPaperMapper;
import com.playedu.exam.mapper.ExamPaperQuestionMapper;
import com.playedu.exam.mapper.QuestionMapper;
import com.playedu.exam.service.ExamPaperQuestionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ExamPaperQuestionServiceImpl implements ExamPaperQuestionService {
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final QuestionMapper questionMapper;

    public ExamPaperQuestionServiceImpl(
            ExamPaperQuestionMapper examPaperQuestionMapper,
            ExamPaperMapper examPaperMapper,
            QuestionMapper questionMapper) {
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.examPaperMapper = examPaperMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public List<ExamPaperQuestion> addQuestionsToPaper(String paperId, List<PaperQuestionReq> questions) {
        ExamPaper paper = requireDraftPaper(paperId);
        if (CollectionUtils.isEmpty(questions)) {
            return getQuestionsByPaperId(paper.getId());
        }

        Set<String> requestedIds = new HashSet<>();
        for (PaperQuestionReq req : questions) {
            if (!requestedIds.add(req.getQuestionId())) {
                throw new IllegalArgumentException("Duplicate question in request: " + req.getQuestionId());
            }
        }

        List<Question> questionEntities = loadQuestionsByIds(new ArrayList<>(requestedIds));
        Map<String, Question> questionMap =
                questionEntities.stream().collect(Collectors.toMap(Question::getId, item -> item));

        List<ExamPaperQuestion> existing = getQuestionsByPaperId(paperId);
        Set<String> existingIds =
                existing.stream().map(ExamPaperQuestion::getQuestionId).collect(Collectors.toSet());

        for (PaperQuestionReq req : questions) {
            if (existingIds.contains(req.getQuestionId())) {
                throw new IllegalArgumentException(
                        "Question already exists in paper: " + req.getQuestionId());
            }
            Question question = questionMap.get(req.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("Question not found: " + req.getQuestionId());
            }

            ExamPaperQuestion relation = new ExamPaperQuestion();
            relation.setPaperId(paperId);
            relation.setQuestionId(req.getQuestionId());
            relation.setSort(req.getSort() == null ? 0 : req.getSort());
            relation.setScore(req.getScore() == null ? question.getScore() : req.getScore());
            examPaperQuestionMapper.insert(relation);
        }

        recalculatePaperTotalScore(paperId);
        return getQuestionsByPaperId(paperId);
    }

    @Override
    @Transactional
    public void removeQuestionsFromPaper(String paperId, List<String> questionIds) {
        requireDraftPaper(paperId);
        if (CollectionUtils.isEmpty(questionIds)) {
            return;
        }
        LambdaUpdateWrapper<ExamPaperQuestion> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamPaperQuestion::getPaperId, paperId)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .in(ExamPaperQuestion::getQuestionId, questionIds)
                .set(ExamPaperQuestion::getIsDeleted, 1);
        examPaperQuestionMapper.update(null, updateWrapper);
        recalculatePaperTotalScore(paperId);
    }

    @Override
    @Transactional
    public List<ExamPaperQuestion> reorderQuestions(String paperId, List<String> questionIdsInOrder) {
        requireDraftPaper(paperId);
        List<ExamPaperQuestion> current = getQuestionsByPaperId(paperId);
        if (CollectionUtils.isEmpty(current) || CollectionUtils.isEmpty(questionIdsInOrder)) {
            return current;
        }
        Set<String> currentIds = current.stream().map(ExamPaperQuestion::getQuestionId).collect(Collectors.toSet());
        Set<String> incomingIds = new HashSet<>(questionIdsInOrder);
        if (currentIds.size() != incomingIds.size() || !currentIds.equals(incomingIds)) {
            throw new IllegalArgumentException("Question order list does not match current paper questions");
        }

        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < questionIdsInOrder.size(); i++) {
            orderMap.put(questionIdsInOrder.get(i), i + 1);
        }

        for (ExamPaperQuestion relation : current) {
            relation.setSort(orderMap.get(relation.getQuestionId()));
            examPaperQuestionMapper.updateById(relation);
        }

        return getQuestionsByPaperId(paperId);
    }

    @Override
    public List<ExamPaperQuestion> getQuestionsByPaperId(String paperId) {
        LambdaQueryWrapper<ExamPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaperQuestion::getPaperId, paperId)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .orderByAsc(ExamPaperQuestion::getSort)
                .orderByAsc(ExamPaperQuestion::getCreateTime);
        List<ExamPaperQuestion> relations = examPaperQuestionMapper.selectList(queryWrapper);
        return relations == null ? Collections.emptyList() : relations;
    }

    @Transactional
    public void deleteRelationsByPaperId(String paperId) {
        LambdaUpdateWrapper<ExamPaperQuestion> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamPaperQuestion::getPaperId, paperId)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .set(ExamPaperQuestion::getIsDeleted, 1);
        examPaperQuestionMapper.update(null, updateWrapper);
    }

    @Transactional
    public void recalculatePaperTotalScore(String paperId) {
        List<ExamPaperQuestion> relations = getQuestionsByPaperId(paperId);
        int totalScore = relations.stream().map(ExamPaperQuestion::getScore).filter(item -> item != null).mapToInt(Integer::intValue).sum();
        ExamPaper paper = requirePaper(paperId);
        paper.setTotalScore(totalScore);
        examPaperMapper.updateById(paper);
    }

    private ExamPaper requireDraftPaper(String paperId) {
        ExamPaper paper = requirePaper(paperId);
        if (!Integer.valueOf(1).equals(paper.getStatus())) {
            throw new IllegalStateException("Paper is not in draft status: " + paperId);
        }
        return paper;
    }

    private ExamPaper requirePaper(String paperId) {
        LambdaQueryWrapper<ExamPaper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaper::getId, paperId).eq(ExamPaper::getIsDeleted, 0);
        ExamPaper paper = examPaperMapper.selectOne(queryWrapper);
        if (paper == null) {
            throw new IllegalArgumentException("ExamPaper not found: " + paperId);
        }
        return paper;
    }

    private List<Question> loadQuestionsByIds(List<String> questionIds) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, questionIds).eq(Question::getIsDeleted, 0);
        List<Question> questions = questionMapper.selectList(queryWrapper);
        return questions == null ? Collections.emptyList() : questions;
    }
}
