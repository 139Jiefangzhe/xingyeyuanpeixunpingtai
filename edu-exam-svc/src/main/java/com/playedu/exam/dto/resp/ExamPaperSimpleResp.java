package com.playedu.exam.dto.resp;

import com.playedu.exam.domain.entity.ExamPaper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "试卷列表简要响应")
public class ExamPaperSimpleResp {
    private String id;

    private String title;

    @Schema(description = "类型：1-普通考试 2-随机抽题")
    private Integer type;

    @Schema(description = "状态：1-草稿 2-已发布 3-已归档")
    private Integer status;

    private Integer totalScore;

    private Integer duration;

    private LocalDateTime createTime;

    public static ExamPaperSimpleResp fromEntity(ExamPaper paper) {
        ExamPaperSimpleResp resp = new ExamPaperSimpleResp();
        resp.setId(paper.getId());
        resp.setTitle(paper.getTitle());
        resp.setType(paper.getType());
        resp.setStatus(paper.getStatus());
        resp.setTotalScore(paper.getTotalScore());
        resp.setDuration(paper.getDuration());
        resp.setCreateTime(paper.getCreateTime());
        return resp;
    }
}
