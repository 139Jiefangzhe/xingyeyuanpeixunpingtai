package com.playedu.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.Question;
import com.playedu.exam.dto.query.QuestionQueryDTO;
import com.playedu.exam.dto.req.QuestionCreateReq;
import com.playedu.exam.dto.req.QuestionUpdateReq;

public interface QuestionService {
    Question createQuestion(QuestionCreateReq req);

    Question updateQuestion(String id, QuestionUpdateReq req);

    void deleteQuestion(String id);

    Question getQuestionById(String id);

    Page<Question> listQuestions(QuestionQueryDTO query);
}
