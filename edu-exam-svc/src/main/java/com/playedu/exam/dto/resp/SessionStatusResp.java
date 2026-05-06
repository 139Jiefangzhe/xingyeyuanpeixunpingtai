package com.playedu.exam.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "考试会话状态响应")
public class SessionStatusResp {
    @Schema(description = "剩余时间，单位秒")
    private Long remainingTime;

    @Schema(description = "当前题号，从 1 开始")
    private Integer currentQuestionIndex;

    @Schema(description = "已作答题数")
    private Integer answeredCount;

    @Schema(description = "总题数")
    private Integer totalCount;

    @Schema(description = "是否已提交")
    private Boolean isSubmitted;
}
