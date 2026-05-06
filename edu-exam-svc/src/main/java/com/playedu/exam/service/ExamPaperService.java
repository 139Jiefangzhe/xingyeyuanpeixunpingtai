package com.playedu.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.exam.dto.query.ExamPaperQueryDTO;
import com.playedu.exam.dto.req.ExamPaperCreateReq;
import com.playedu.exam.dto.req.PaperGenerateReq;
import com.playedu.exam.dto.req.ExamPaperUpdateReq;
import com.playedu.exam.dto.resp.ExamPaperDetailResp;

public interface ExamPaperService {
    ExamPaper createPaper(ExamPaperCreateReq req);

    ExamPaper updatePaper(String id, ExamPaperUpdateReq req);

    void deletePaper(String id);

    ExamPaperDetailResp getPaperById(String id);

    Page<ExamPaper> listPapers(ExamPaperQueryDTO query);

    ExamPaper generatePaper(PaperGenerateReq req);

    ExamPaper publishPaper(String id);

    ExamPaper copyPaper(String id);
}
