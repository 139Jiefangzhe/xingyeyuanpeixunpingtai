package com.playedu.exam.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "试卷详情响应")
public class ExamPaperDetailResp {
    private String id;

    private String title;

    private String description;

    private Integer totalScore;

    private Integer duration;

    private Integer passScore;

    @Schema(description = "状态：1-草稿 2-已发布 3-已归档")
    private Integer status;

    @Schema(description = "类型：1-普通考试 2-随机抽题")
    private Integer type;

    private Integer allowRedo;

    private String knowledgeConfig;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<QuestionResp> questions;
}
