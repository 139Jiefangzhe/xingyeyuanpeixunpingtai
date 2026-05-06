package com.playedu.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.exam.domain.entity.ExamPaper;
import com.playedu.exam.domain.entity.ExamPaperQuestion;
import com.playedu.exam.domain.entity.Question;
import com.playedu.exam.domain.rule.PaperGenerateRule;
import com.playedu.exam.dto.query.ExamPaperQueryDTO;
import com.playedu.exam.dto.req.ExamPaperCreateReq;
import com.playedu.exam.dto.req.PaperGenerateReq;
import com.playedu.exam.dto.req.ExamPaperUpdateReq;
import com.playedu.exam.dto.resp.ExamPaperDetailResp;
import com.playedu.exam.dto.resp.QuestionResp;
import com.playedu.common.exception.BizException;
import com.playedu.exam.mapper.ExamPaperMapper;
import com.playedu.exam.mapper.ExamPaperQuestionMapper;
import com.playedu.exam.mapper.QuestionMapper;
import com.playedu.exam.service.ExamPaperQuestionService;
import com.playedu.exam.service.ExamPaperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class ExamPaperServiceImpl implements ExamPaperService {
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamPaperQuestionService examPaperQuestionService;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<RedissonClient> redissonClientProvider;

    public ExamPaperServiceImpl(
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            ExamPaperQuestionService examPaperQuestionService,
            QuestionMapper questionMapper,
            ObjectMapper objectMapper,
            ObjectProvider<RedissonClient> redissonClientProvider) {
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.examPaperQuestionService = examPaperQuestionService;
        this.questionMapper = questionMapper;
        this.objectMapper = objectMapper;
        this.redissonClientProvider = redissonClientProvider;
    }

    @Override
    @Transactional
    public ExamPaper createPaper(ExamPaperCreateReq req) {
        ExamPaper paper = new ExamPaper();
        paper.setTitle(req.getTitle());
        paper.setDescription(req.getDescription());
        paper.setDuration(req.getDuration());
        paper.setPassScore(req.getPassScore());
        paper.setTotalScore(0);
        paper.setStatus(1);
        paper.setType(req.getType());
        paper.setAllowRedo(req.getAllowRedo());
        paper.setKnowledgeConfig(req.getKnowledgeConfig());
        examPaperMapper.insert(paper);
        return getExistingPaper(paper.getId());
    }

    @Override
    @Transactional
    public ExamPaper updatePaper(String id, ExamPaperUpdateReq req) {
        ExamPaper paper = requireDraftPaper(id);
        if (StringUtils.hasText(req.getTitle())) {
            paper.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            paper.setDescription(req.getDescription());
        }
        if (req.getDuration() != null) {
            paper.setDuration(req.getDuration());
        }
        if (req.getPassScore() != null) {
            paper.setPassScore(req.getPassScore());
        }
        if (req.getTotalScore() != null) {
            paper.setTotalScore(req.getTotalScore());
        }
        if (req.getType() != null) {
            paper.setType(req.getType());
        }
        if (req.getAllowRedo() != null) {
            paper.setAllowRedo(req.getAllowRedo());
        }
        if (req.getKnowledgeConfig() != null) {
            paper.setKnowledgeConfig(req.getKnowledgeConfig());
        }
        examPaperMapper.updateById(paper);
        return getExistingPaper(id);
    }

    @Override
    @Transactional
    public void deletePaper(String id) {
        ExamPaper paper = getExistingPaper(id);
        LambdaUpdateWrapper<ExamPaper> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamPaper::getId, paper.getId()).set(ExamPaper::getIsDeleted, 1);
        examPaperMapper.update(null, updateWrapper);
        LambdaUpdateWrapper<ExamPaperQuestion> relationUpdateWrapper = new LambdaUpdateWrapper<>();
        relationUpdateWrapper.eq(ExamPaperQuestion::getPaperId, id)
                .eq(ExamPaperQuestion::getIsDeleted, 0)
                .set(ExamPaperQuestion::getIsDeleted, 1);
        examPaperQuestionMapper.update(null, relationUpdateWrapper);
    }

    @Override
    public ExamPaperDetailResp getPaperById(String id) {
        ExamPaper paper = getExistingPaper(id);
        List<ExamPaperQuestion> relations = examPaperQuestionService.getQuestionsByPaperId(id);
        ExamPaperDetailResp resp = new ExamPaperDetailResp();
        BeanUtils.copyProperties(paper, resp);
        resp.setQuestions(buildQuestionDetails(relations));
        return resp;
    }

    @Override
    public Page<ExamPaper> listPapers(ExamPaperQueryDTO query) {
        Page<ExamPaper> page =
                new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        LambdaQueryWrapper<ExamPaper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaper::getIsDeleted, 0);
        if (query.getStatus() != null) {
            queryWrapper.eq(ExamPaper::getStatus, query.getStatus());
        }
        if (query.getType() != null) {
            queryWrapper.eq(ExamPaper::getType, query.getType());
        }
        if (StringUtils.hasText(query.getTitleLike())) {
            queryWrapper.like(ExamPaper::getTitle, query.getTitleLike());
        }
        applyPaperSort(queryWrapper, query.getSortField(), query.getSortOrder());
        return examPaperMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamPaper generatePaper(PaperGenerateReq req) {
        PaperGenerateRule rule = parseGenerateRule(req);
        String ruleHash = buildRuleHash(rule);
        String lockKey = "lock:exam:generate:" + req.getBankId() + ":" + ruleHash;

        RedissonClient redissonClient = redissonClientProvider.getIfAvailable();
        if (redissonClient == null) {
            throw new BizException("EXAM_GENERATE_LOCK_UNAVAILABLE", "RedissonClient 未配置，无法执行智能组卷");
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BizException("EXAM_GENERATE_INTERRUPTED", "系统繁忙，请稍后重试");
        }
        if (!locked) {
            throw new BizException("EXAM_GENERATE_BUSY", "系统繁忙，请稍后重试");
        }

        try {
            List<Question> selectedQuestions = selectQuestionsForRule(req.getBankId(), rule);
            Collections.shuffle(selectedQuestions);

            int totalScore =
                    selectedQuestions.stream()
                            .map(Question::getScore)
                            .filter(item -> item != null)
                            .mapToInt(Integer::intValue)
                            .sum();

            ExamPaper paper = new ExamPaper();
            paper.setTitle(req.getTitle());
            paper.setDuration(req.getDuration());
            paper.setPassScore(req.getPassScore());
            paper.setDescription("智能组卷生成");
            paper.setStatus(1);
            paper.setType(2);
            paper.setAllowRedo(0);
            paper.setTotalScore(totalScore);
            paper.setKnowledgeConfig(writeRuleJson(rule));
            examPaperMapper.insert(paper);

            int sort = 1;
            for (Question question : selectedQuestions) {
                ExamPaperQuestion relation = new ExamPaperQuestion();
                relation.setPaperId(paper.getId());
                relation.setQuestionId(question.getId());
                relation.setSort(sort++);
                relation.setScore(question.getScore());
                examPaperQuestionMapper.insert(relation);
            }
            return getExistingPaper(paper.getId());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public ExamPaper publishPaper(String id) {
        ExamPaper paper = requireDraftPaper(id);
        List<ExamPaperQuestion> questions = examPaperQuestionService.getQuestionsByPaperId(id);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Cannot publish paper without questions");
        }
        int totalScore = questions.stream().map(ExamPaperQuestion::getScore).filter(item -> item != null).mapToInt(Integer::intValue).sum();
        if (totalScore <= 0) {
            throw new IllegalStateException("Cannot publish paper with zero total score");
        }
        paper.setTotalScore(totalScore);
        paper.setStatus(2);
        examPaperMapper.updateById(paper);
        return getExistingPaper(id);
    }

    @Override
    @Transactional
    public ExamPaper copyPaper(String id) {
        ExamPaper source = getExistingPaper(id);
        ExamPaper copy = new ExamPaper();
        copy.setTitle(source.getTitle() + "（副本）");
        copy.setDescription(source.getDescription());
        copy.setDuration(source.getDuration());
        copy.setPassScore(source.getPassScore());
        copy.setTotalScore(source.getTotalScore());
        copy.setStatus(1);
        copy.setType(source.getType());
        copy.setAllowRedo(source.getAllowRedo());
        copy.setKnowledgeConfig(source.getKnowledgeConfig());
        examPaperMapper.insert(copy);

        List<ExamPaperQuestion> sourceRelations = examPaperQuestionService.getQuestionsByPaperId(id);
        for (ExamPaperQuestion sourceRelation : sourceRelations) {
            ExamPaperQuestion relation = new ExamPaperQuestion();
            relation.setPaperId(copy.getId());
            relation.setQuestionId(sourceRelation.getQuestionId());
            relation.setSort(sourceRelation.getSort());
            relation.setScore(sourceRelation.getScore());
            examPaperQuestionMapper.insert(relation);
        }
        return getExistingPaper(copy.getId());
    }

    private ExamPaper requireDraftPaper(String id) {
        ExamPaper paper = getExistingPaper(id);
        if (!Integer.valueOf(1).equals(paper.getStatus())) {
            throw new IllegalStateException("Only draft paper can be modified: " + id);
        }
        return paper;
    }

    private ExamPaper getExistingPaper(String id) {
        LambdaQueryWrapper<ExamPaper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaper::getId, id).eq(ExamPaper::getIsDeleted, 0);
        ExamPaper paper = examPaperMapper.selectOne(queryWrapper);
        if (paper == null) {
            throw new IllegalArgumentException("ExamPaper not found: " + id);
        }
        return paper;
    }

    private void applyPaperSort(
            LambdaQueryWrapper<ExamPaper> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortField) ? sortField : "createTime";
        switch (field) {
            case "title" -> queryWrapper.orderBy(true, asc, ExamPaper::getTitle);
            case "status" -> queryWrapper.orderBy(true, asc, ExamPaper::getStatus);
            case "type" -> queryWrapper.orderBy(true, asc, ExamPaper::getType);
            case "totalScore" -> queryWrapper.orderBy(true, asc, ExamPaper::getTotalScore);
            default -> queryWrapper.orderBy(true, asc, ExamPaper::getCreateTime);
        }
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }

    private PaperGenerateRule parseGenerateRule(PaperGenerateReq req) {
        try {
            PaperGenerateRule rule =
                    objectMapper.readValue(req.getKnowledgeConfig(), PaperGenerateRule.class);
            if (rule.getQuestionTypeRules() == null) {
                rule.setQuestionTypeRules(new HashMap<>());
            }
            if (rule.getDifficultyDistribution() == null) {
                rule.setDifficultyDistribution(new HashMap<>());
            }
            if (rule.getKnowledgePoints() == null) {
                rule.setKnowledgePoints(new ArrayList<>());
            }
            if (rule.getAllowRepeat() == null) {
                rule.setAllowRepeat(Boolean.FALSE);
            }
            rule.setTotalScore(req.getTotalScore());
            if (rule.getQuestionTypeRules().isEmpty()) {
                throw new BizException("EXAM_GENERATE_RULE_INVALID", "组卷规则不能为空：未配置题型数量");
            }
            return rule;
        } catch (JsonProcessingException ex) {
            throw new BizException("EXAM_GENERATE_RULE_INVALID", "组卷规则解析失败");
        }
    }

    private String buildRuleHash(PaperGenerateRule rule) {
        String payload = writeRuleJson(rule);
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String writeRuleJson(PaperGenerateRule rule) {
        try {
            return objectMapper.writeValueAsString(rule);
        } catch (JsonProcessingException ex) {
            throw new BizException("EXAM_GENERATE_RULE_INVALID", "组卷规则序列化失败");
        }
    }

    private List<Question> selectQuestionsForRule(String bankId, PaperGenerateRule rule) {
        List<Integer> typeSlots = expandRule(rule.getQuestionTypeRules());
        if (typeSlots.isEmpty()) {
            throw new BizException("EXAM_GENERATE_RULE_INVALID", "组卷规则不能为空：未配置题型数量");
        }

        List<Integer> difficultySlots = expandRule(rule.getDifficultyDistribution());
        if (!difficultySlots.isEmpty() && difficultySlots.size() != typeSlots.size()) {
            throw new BizException("EXAM_GENERATE_RULE_INVALID", "题型数量与难度数量总和不一致");
        }
        if (difficultySlots.isEmpty()) {
            difficultySlots = new ArrayList<>(Collections.nCopies(typeSlots.size(), null));
        }

        Collections.shuffle(typeSlots);
        Collections.shuffle(difficultySlots);

        Map<QuestionBucket, Integer> bucketCounts = new HashMap<>();
        for (int i = 0; i < typeSlots.size(); i++) {
            QuestionBucket bucket = new QuestionBucket(typeSlots.get(i), difficultySlots.get(i));
            bucketCounts.merge(bucket, 1, Integer::sum);
        }

        Set<String> usedQuestionIds = new HashSet<>();
        if (!Boolean.TRUE.equals(rule.getAllowRepeat())) {
            usedQuestionIds.addAll(loadUsedQuestionIds());
        }

        List<Question> selectedQuestions = new ArrayList<>();
        for (Map.Entry<QuestionBucket, Integer> entry : bucketCounts.entrySet()) {
            QuestionBucket bucket = entry.getKey();
            int requiredCount = entry.getValue();
            List<Question> candidates =
                    loadCandidates(
                            bankId,
                            bucket.type(),
                            bucket.difficulty(),
                            rule.getKnowledgePoints(),
                            Math.max(requiredCount * 10, 100));

            List<Question> available =
                    candidates.stream()
                            .filter(item -> item.getId() != null)
                            .filter(item -> !usedQuestionIds.contains(item.getId()))
                            .collect(Collectors.toCollection(ArrayList::new));

            if (available.size() < requiredCount) {
                throw new BizException(
                        "EXAM_GENERATE_STOCK_SHORTAGE",
                        "题库库存不足："
                                + formatBucketName(bucket)
                                + "需"
                                + requiredCount
                                + "道，实际仅"
                                + available.size()
                                + "道");
            }

            Collections.shuffle(available);
            List<Question> chosen = available.subList(0, requiredCount);
            selectedQuestions.addAll(chosen);
            chosen.stream().map(Question::getId).forEach(usedQuestionIds::add);
        }
        return selectedQuestions;
    }

    private List<Question> loadCandidates(
            String bankId,
            Integer type,
            Integer difficulty,
            List<String> knowledgePoints,
            int limit) {
        Map<String, Question> candidates = new HashMap<>();
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            List<Question> questions =
                    questionMapper.selectByConditions(bankId, type, difficulty, null, limit);
            questions.forEach(item -> candidates.put(item.getId(), item));
        } else {
            for (String knowledgePoint : knowledgePoints) {
                List<Question> questions =
                        questionMapper.selectByConditions(bankId, type, difficulty, knowledgePoint, limit);
                questions.forEach(item -> candidates.put(item.getId(), item));
            }
        }
        return new ArrayList<>(candidates.values());
    }

    private Set<String> loadUsedQuestionIds() {
        LambdaQueryWrapper<ExamPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamPaperQuestion::getIsDeleted, 0).select(ExamPaperQuestion::getQuestionId);
        List<ExamPaperQuestion> relations = examPaperQuestionMapper.selectList(queryWrapper);
        return relations.stream()
                .map(ExamPaperQuestion::getQuestionId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private List<Integer> expandRule(Map<Integer, Integer> config) {
        List<Integer> slots = new ArrayList<>();
        if (config == null || config.isEmpty()) {
            return slots;
        }
        for (Map.Entry<Integer, Integer> entry : config.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            for (int i = 0; i < entry.getValue(); i++) {
                slots.add(entry.getKey());
            }
        }
        return slots;
    }

    private String formatBucketName(QuestionBucket bucket) {
        String typeName =
                switch (bucket.type()) {
                    case 1 -> "单选题";
                    case 2 -> "多选题";
                    case 3 -> "判断题";
                    case 4 -> "填空题";
                    case 5 -> "问答题";
                    case 6 -> "组合题";
                    default -> "题型" + bucket.type();
                };
        if (bucket.difficulty() == null) {
            return typeName;
        }
        return typeName + "[难度" + bucket.difficulty() + "]";
    }

    private List<QuestionResp> buildQuestionDetails(List<ExamPaperQuestion> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> questionIds =
                relations.stream()
                        .map(ExamPaperQuestion::getQuestionId)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .toList();
        if (questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, questionIds).eq(Question::getIsDeleted, 0);
        List<Question> questions = questionMapper.selectList(queryWrapper);
        Map<String, Question> questionMap =
                questions.stream().collect(Collectors.toMap(Question::getId, item -> item));

        List<QuestionResp> result = new ArrayList<>();
        for (ExamPaperQuestion relation : relations) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question == null) {
                continue;
            }
            QuestionResp resp = QuestionResp.fromEntity(question, objectMapper);
            resp.setScore(relation.getScore());
            result.add(resp);
        }
        return result;
    }

    private record QuestionBucket(Integer type, Integer difficulty) {}
}
