package com.playedu.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.Question;
import com.playedu.exam.dto.query.QuestionQueryDTO;
import com.playedu.exam.dto.req.QuestionCreateReq;
import com.playedu.exam.dto.req.QuestionUpdateReq;
import com.playedu.exam.mapper.QuestionMapper;
import com.playedu.exam.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class QuestionServiceImpl implements QuestionService {
    private final QuestionMapper questionMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public Question createQuestion(QuestionCreateReq req) {
        Question question = new Question();
        applyCreate(question, req);
        questionMapper.insert(question);
        return getQuestionById(question.getId());
    }

    @Override
    @Transactional
    public Question updateQuestion(String id, QuestionUpdateReq req) {
        Question question = getQuestionById(id);
        if (StringUtils.hasText(req.getBankId())) {
            question.setBankId(req.getBankId());
        }
        if (req.getType() != null) {
            question.setType(req.getType());
        }
        if (StringUtils.hasText(req.getContent())) {
            question.setContent(req.getContent());
        }
        if (req.getOptions() != null) {
            question.setOptions(req.getOptions());
        }
        if (StringUtils.hasText(req.getAnswer())) {
            question.setAnswer(req.getAnswer());
        }
        if (req.getAnalysis() != null) {
            question.setAnalysis(req.getAnalysis());
        }
        if (req.getDifficulty() != null) {
            question.setDifficulty(req.getDifficulty());
        }
        if (req.getKnowledgePoint() != null) {
            question.setKnowledgePoint(req.getKnowledgePoint());
        }
        if (req.getScore() != null) {
            question.setScore(req.getScore());
        }
        questionMapper.updateById(question);
        return getQuestionById(id);
    }

    @Override
    @Transactional
    public void deleteQuestion(String id) {
        Question question = getQuestionById(id);
        LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Question::getId, question.getId()).set(Question::getIsDeleted, 1);
        questionMapper.update(null, updateWrapper);
    }

    @Override
    public Question getQuestionById(String id) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getId, id).eq(Question::getIsDeleted, 0);
        Question question = questionMapper.selectOne(queryWrapper);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + id);
        }
        return question;
    }

    @Override
    public Page<Question> listQuestions(QuestionQueryDTO query) {
        Page<Question> page = new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getIsDeleted, 0);
        if (StringUtils.hasText(query.getBankId())) {
            queryWrapper.eq(Question::getBankId, query.getBankId());
        }
        if (query.getType() != null) {
            queryWrapper.eq(Question::getType, query.getType());
        }
        if (query.getDifficulty() != null) {
            queryWrapper.eq(Question::getDifficulty, query.getDifficulty());
        }
        if (StringUtils.hasText(query.getKnowledgePointLike())) {
            queryWrapper.like(Question::getKnowledgePoint, query.getKnowledgePointLike());
        }
        if (StringUtils.hasText(query.getContentLike())) {
            queryWrapper.like(Question::getContent, query.getContentLike());
        }
        applyQuestionSort(queryWrapper, query.getSortField(), query.getSortOrder());
        return questionMapper.selectPage(page, queryWrapper);
    }

    private void applyCreate(Question question, QuestionCreateReq req) {
        question.setBankId(req.getBankId());
        question.setType(req.getType());
        question.setContent(req.getContent());
        question.setOptions(req.getOptions());
        question.setAnswer(req.getAnswer());
        question.setAnalysis(req.getAnalysis());
        question.setDifficulty(req.getDifficulty());
        question.setKnowledgePoint(req.getKnowledgePoint());
        question.setScore(req.getScore());
    }

    private void applyQuestionSort(
            LambdaQueryWrapper<Question> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortField) ? sortField : "createTime";
        switch (field) {
            case "difficulty" -> queryWrapper.orderBy(true, asc, Question::getDifficulty);
            case "score" -> queryWrapper.orderBy(true, asc, Question::getScore);
            case "type" -> queryWrapper.orderBy(true, asc, Question::getType);
            default -> queryWrapper.orderBy(true, asc, Question::getCreateTime);
        }
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }
}
