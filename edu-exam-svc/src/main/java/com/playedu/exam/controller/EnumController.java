package com.playedu.exam.controller;

import com.playedu.common.domain.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enums")
@Tag(name = "枚举字典", description = "考试中心-枚举字典接口")
public class EnumController {

    @GetMapping
    @Operation(summary = "查询考试中心枚举字典")
    public Result<Map<String, Map<String, String>>> getEnums() {
        Map<String, Map<String, String>> payload = new LinkedHashMap<>();
        payload.put("QuestionType", orderedMap("1", "单选题", "2", "多选题", "3", "判断题", "4", "填空题", "5", "问答题"));
        payload.put("ExamPaperStatus", orderedMap("1", "草稿", "2", "已发布", "3", "已归档"));
        payload.put("ExamPaperType", orderedMap("1", "普通考试", "2", "随机抽题"));
        payload.put("Difficulty", orderedMap("1", "极易", "2", "容易", "3", "中等", "4", "困难", "5", "极难"));
        return Result.success(payload);
    }

    private Map<String, String> orderedMap(String... keyValuePairs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < keyValuePairs.length; index += 2) {
            result.put(keyValuePairs[index], keyValuePairs[index + 1]);
        }
        return result;
    }
}
