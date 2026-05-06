package com.playedu.exam.dto.resp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.playedu.exam.domain.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@Schema(description = "题目响应")
public class QuestionResp {
    private String id;

    private String bankId;

    private Integer type;

    private String content;

    private JsonNode options;

    private String answer;

    private String analysis;

    private Integer difficulty;

    private String knowledgePoint;

    private Integer score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static QuestionResp fromEntity(Question question, ObjectMapper objectMapper) {
        QuestionResp resp = new QuestionResp();
        resp.setId(question.getId());
        resp.setBankId(question.getBankId());
        resp.setType(question.getType());
        resp.setContent(question.getContent());
        resp.setOptions(parseOptions(question.getOptions(), objectMapper));
        resp.setAnswer(question.getAnswer());
        resp.setAnalysis(question.getAnalysis());
        resp.setDifficulty(question.getDifficulty());
        resp.setKnowledgePoint(question.getKnowledgePoint());
        resp.setScore(question.getScore());
        resp.setCreateTime(question.getCreateTime());
        resp.setUpdateTime(question.getUpdateTime());
        return resp;
    }

    private static JsonNode parseOptions(String options, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(options)) {
            return null;
        }
        try {
            return objectMapper.readTree(options);
        } catch (Exception ex) {
            return TextNode.valueOf(options);
        }
    }
}
