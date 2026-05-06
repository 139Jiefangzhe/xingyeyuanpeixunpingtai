package com.playedu.exam.domain.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PaperGenerateRule {
    private Map<Integer, Integer> questionTypeRules = new LinkedHashMap<>();

    private Map<Integer, Integer> difficultyDistribution = new LinkedHashMap<>();

    private List<String> knowledgePoints = new ArrayList<>();

    private Integer totalScore;

    private Boolean allowRepeat = Boolean.FALSE;
}
