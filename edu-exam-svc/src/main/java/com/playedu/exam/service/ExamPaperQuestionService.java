package com.playedu.exam.service;

import com.playedu.exam.domain.entity.ExamPaperQuestion;
import com.playedu.exam.dto.req.PaperQuestionReq;
import java.util.List;

public interface ExamPaperQuestionService {
    List<ExamPaperQuestion> addQuestionsToPaper(String paperId, List<PaperQuestionReq> questions);

    void removeQuestionsFromPaper(String paperId, List<String> questionIds);

    List<ExamPaperQuestion> reorderQuestions(String paperId, List<String> questionIdsInOrder);

    List<ExamPaperQuestion> getQuestionsByPaperId(String paperId);
}
