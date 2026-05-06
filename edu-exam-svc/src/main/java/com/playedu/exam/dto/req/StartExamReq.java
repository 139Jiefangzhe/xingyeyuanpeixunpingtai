package com.playedu.exam.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "开始考试请求")
public class StartExamReq {
    @NotBlank(message = "试卷ID不能为空")
    @Schema(description = "试卷ID")
    private String paperId;
}
