package com.playedu.exam.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "保存答案请求")
public class SaveAnswerReq {
    @NotBlank(message = "题目ID不能为空")
    @Schema(description = "题目ID")
    private String questionId;

    @Schema(description = "考生答案；传 null 表示清空该题答案")
    private String answer;
}
