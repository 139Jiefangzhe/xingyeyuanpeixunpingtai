package com.playedu.exam.dto.resp;

import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.exam.domain.entity.ExamRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "考试记录列表响应")
public class ExamRecordListResp {
    private String id;

    private String paperTitle;

    @Schema(description = "考生姓名；当前阶段未接入用户服务时可能为空")
    private String userName;

    private Integer score;

    @Schema(description = "状态：1-进行中 2-已提交 3-已评分")
    private Integer status;

    private LocalDateTime startTime;

    @Schema(description = "考试时长，单位分钟")
    private Integer duration;

    public static ExamRecordListResp fromEntity(ExamRecord record, ExamPaper paper) {
        ExamRecordListResp resp = new ExamRecordListResp();
        resp.setId(record.getId());
        resp.setPaperTitle(paper == null ? null : paper.getTitle());
        resp.setUserName(null);
        resp.setScore(record.getObtainScore());
        resp.setStatus(record.getStatus());
        resp.setStartTime(record.getStartTime());
        resp.setDuration(paper == null ? null : paper.getDuration());
        return resp;
    }
}
