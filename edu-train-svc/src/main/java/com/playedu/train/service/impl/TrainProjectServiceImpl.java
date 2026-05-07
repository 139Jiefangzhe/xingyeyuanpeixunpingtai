package com.playedu.train.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.exception.BizException;
import com.playedu.train.domain.entity.TrainProject;
import com.playedu.train.domain.entity.TrainTask;
import com.playedu.train.domain.entity.TrainUserTask;
import com.playedu.train.dto.query.TrainProjectQueryDTO;
import com.playedu.train.dto.req.TrainProjectCreateReq;
import com.playedu.train.dto.req.TrainTaskReq;
import com.playedu.train.dto.resp.CourseFeignResp;
import com.playedu.train.dto.resp.ExamPaperFeignResp;
import com.playedu.train.dto.resp.ExamPaperStatsFeignResp;
import com.playedu.train.dto.resp.ExamPaperStudentFeignResp;
import com.playedu.train.dto.resp.LiveRoomFeignResp;
import com.playedu.train.dto.resp.ProjectStatsResp;
import com.playedu.train.dto.resp.ProjectTaskProgressResp;
import com.playedu.train.dto.resp.StudentProgressResp;
import com.playedu.train.dto.resp.TrainProjectDetailResp;
import com.playedu.train.dto.resp.TrainProjectListResp;
import com.playedu.train.dto.resp.TrainProjectMyDetailDTO;
import com.playedu.train.dto.resp.TrainTaskResp;
import com.playedu.train.dto.resp.UserFeignResp;
import com.playedu.train.feign.CourseFeignClient;
import com.playedu.train.feign.ExamFeignClient;
import com.playedu.train.feign.LiveFeignClient;
import com.playedu.train.feign.UserFeignClient;
import com.playedu.train.mapper.TrainProjectMapper;
import com.playedu.train.mapper.TrainTaskMapper;
import com.playedu.train.mapper.TrainUserTaskMapper;
import com.playedu.train.service.TrainProjectService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TrainProjectServiceImpl implements TrainProjectService {
    @Value("${edu.local.user-bypass:false}")
    private boolean localUserBypass;

    private final TrainProjectMapper trainProjectMapper;
    private final TrainTaskMapper trainTaskMapper;
    private final TrainUserTaskMapper trainUserTaskMapper;
    private final CourseFeignClient courseFeignClient;
    private final ExamFeignClient examFeignClient;
    private final LiveFeignClient liveFeignClient;
    private final UserFeignClient userFeignClient;
    private final TransactionTemplate transactionTemplate;

    public TrainProjectServiceImpl(
            TrainProjectMapper trainProjectMapper,
            TrainTaskMapper trainTaskMapper,
            TrainUserTaskMapper trainUserTaskMapper,
            CourseFeignClient courseFeignClient,
            ExamFeignClient examFeignClient,
            LiveFeignClient liveFeignClient,
            UserFeignClient userFeignClient,
            TransactionTemplate transactionTemplate) {
        this.trainProjectMapper = trainProjectMapper;
        this.trainTaskMapper = trainTaskMapper;
        this.trainUserTaskMapper = trainUserTaskMapper;
        this.courseFeignClient = courseFeignClient;
        this.examFeignClient = examFeignClient;
        this.liveFeignClient = liveFeignClient;
        this.userFeignClient = userFeignClient;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public TrainProject createProject(Long creatorId, TrainProjectCreateReq req) {
        requireUser(creatorId);
        validateProjectTime(req.getStartTime(), req.getEndTime());
        TrainProject project =
                transactionTemplate.execute(
                        status -> {
                            TrainProject entity = new TrainProject();
                            entity.setTitle(req.getTitle());
                            entity.setDescription(req.getDescription());
                            entity.setType(req.getType());
                            entity.setStatus(1);
                            entity.setStartTime(req.getStartTime());
                            entity.setEndTime(req.getEndTime());
                            entity.setAssigneeScope(req.getAssigneeScope());
                            entity.setTargetDeptIds(req.getTargetDeptIds());
                            entity.setCreateBy(creatorId);
                            entity.setUpdateBy(creatorId);
                            trainProjectMapper.insert(entity);
                            return getExistingProject(entity.getId());
                        });
        if (project == null) {
            throw new IllegalStateException("培训项目创建失败");
        }
        return project;
    }

    @Override
    public List<TrainTask> addTasks(String projectId, List<TrainTaskReq> tasks) {
        requireDraftProject(projectId);
        if (CollectionUtils.isEmpty(tasks)) {
            throw new BizException("TRAIN_TASK_EMPTY", "培训任务不能为空");
        }
        validateTaskReferences(tasks);

        List<TrainTask> createdTasks =
                transactionTemplate.execute(
                        status -> {
                            TrainProject project = requireDraftProject(projectId);
                            int nextSort = resolveNextSort(project.getId());
                            for (TrainTaskReq req : tasks) {
                                TrainTask task = new TrainTask();
                                task.setProjectId(project.getId());
                                task.setName(req.getName());
                                task.setType(req.getType());
                                task.setRefId(req.getRefId());
                                task.setSort(req.getSort() == null ? nextSort++ : req.getSort());
                                task.setRequired(req.getRequired());
                                task.setPassRule(req.getPassRule());
                                trainTaskMapper.insert(task);
                            }
                            return listTasksByProjectId(project.getId());
                        });
        if (createdTasks == null) {
            throw new IllegalStateException("培训任务创建失败");
        }
        return createdTasks;
    }

    @Override
    public void publishProject(String projectId) {
        TrainProject project = requireDraftProject(projectId);
        validateTargetDeptIdsFormat(project.getTargetDeptIds());
        if (listTasksByProjectId(project.getId()).isEmpty()) {
            throw new BizException("TRAIN_PROJECT_TASK_REQUIRED", "培训项目至少需要1个任务节点");
        }
        transactionTemplate.executeWithoutResult(
                status -> {
                    TrainProject latestProject = requireDraftProject(projectId);
                    latestProject.setStatus(2);
                    trainProjectMapper.updateById(latestProject);
                });
    }

    @Override
    public TrainProjectDetailResp getProjectDetail(String projectId) {
        TrainProject project = getExistingProject(projectId);
        List<TrainTask> tasks = listTasksByProjectId(projectId);
        Map<String, String> courseTitleMap = loadCourseTitles(tasks);
        Map<String, String> examPaperTitleMap = loadExamPaperTitles(tasks);
        TrainProjectDetailResp resp = new TrainProjectDetailResp();
        BeanUtils.copyProperties(project, resp);
        resp.setTasks(
                tasks.stream()
                        .map(
                                task ->
                                        TrainTaskResp.fromEntity(
                                                task,
                                                courseTitleMap.get(task.getRefId()),
                                                examPaperTitleMap.get(task.getRefId())))
                        .toList());
        return resp;
    }

    @Override
    public TrainProjectMyDetailDTO getMyProjectDetail(String projectId, Long userId) {
        if (userId == null || userId <= 0L) {
            throw new BizException("TRAIN_PROJECT_USER_INVALID", "学员ID不合法");
        }

        TrainProject project = getExistingProject(projectId);
        List<TrainTask> tasks = listTasksByProjectId(projectId);
        List<TrainUserTask> userTasks = listUserTasksByProjectIdAndUserId(projectId, userId);
        Map<String, TrainUserTask> userTaskMap =
                userTasks.stream()
                        .collect(
                                Collectors.toMap(
                                        TrainUserTask::getTaskId,
                                        Function.identity(),
                                        (left, right) -> right,
                                        LinkedHashMap::new));

        TrainProjectMyDetailDTO resp = new TrainProjectMyDetailDTO();
        resp.setProjectId(project.getId());
        resp.setTitle(project.getTitle());
        resp.setDescription(project.getDescription());
        resp.setStartTime(project.getStartTime());
        resp.setEndTime(project.getEndTime());
        resp.setOverallProgress(
                tasks.isEmpty()
                        ? 0
                        : toPercentage(
                                (int)
                                        userTasks.stream()
                                                .filter(item -> "COMPLETED".equals(item.getStatus()))
                                                .count(),
                                tasks.size()));
        resp.setTasks(
                tasks.stream()
                        .map(task -> toMyTaskItem(task, userTaskMap.get(task.getId())))
                        .toList());
        return resp;
    }

    @Override
    public ProjectStatsResp getProjectStats(String projectId) {
        TrainProject project = getExistingProject(projectId);
        List<TrainTask> tasks = listTasksByProjectId(projectId);
        Map<String, String> courseTitleMap = loadCourseTitles(tasks);
        Map<String, String> examPaperTitleMap = loadExamPaperTitles(tasks);
        Map<String, ExamPaperStatsFeignResp> examStatsMap = loadExamStats(tasks);

        List<LocalStudentProfile> studentProfiles = buildMockStudentProfiles();
        Map<Long, StudentProgressAccumulator> studentProgressMap = new LinkedHashMap<>();
        for (LocalStudentProfile profile : studentProfiles) {
            studentProgressMap.put(profile.userId(), new StudentProgressAccumulator(profile));
        }

        List<ProjectTaskProgressResp> taskProgressList = new ArrayList<>();
        for (TrainTask task : tasks) {
            ProjectTaskProgressResp taskProgress = new ProjectTaskProgressResp();
            taskProgress.setTaskId(task.getId());
            taskProgress.setTaskName(task.getName());
            taskProgress.setTaskType(task.getType());
            taskProgress.setRefId(task.getRefId());
            taskProgress.setResourceTitle(resolveTaskResourceTitle(task, courseTitleMap, examPaperTitleMap));
            taskProgress.setTotalCount(studentProfiles.size());

            int completedCount = switch (defaultTaskType(task.getType())) {
                case 1 -> applyMockCourseProgress(task, studentProgressMap);
                case 2 -> applyExamProgress(task, studentProgressMap, examStatsMap.get(task.getRefId()));
                case 3 -> applyMockLiveProgress(task, studentProgressMap);
                case 4 -> applyMockHomeworkProgress(task, studentProgressMap);
                default -> 0;
            };
            taskProgress.setCompletedCount(completedCount);
            taskProgress.setCompletionRate(toPercentage(completedCount, studentProfiles.size()));
            taskProgress.setMetricLabel(resolveMetricLabel(task.getType()));
            taskProgressList.add(taskProgress);
        }

        List<StudentProgressResp> studentProgressList =
                studentProgressMap.values().stream()
                        .map(item -> item.finish(tasks.size()))
                        .sorted(Comparator.comparing(StudentProgressResp::getOverallCompletionRate).reversed())
                        .toList();

        int participantCount =
                (int)
                        studentProgressList.stream()
                                .filter(item -> item.getCompletedTaskCount() != null && item.getCompletedTaskCount() > 0)
                                .count();
        int overallCompletionRate =
                studentProgressList.isEmpty()
                        ? 0
                        : (int)
                                Math.round(
                                        studentProgressList.stream()
                                                        .map(StudentProgressResp::getOverallCompletionRate)
                                                        .filter(Objects::nonNull)
                                                        .mapToInt(Integer::intValue)
                                                        .average()
                                                        .orElse(0D));

        ProjectStatsResp resp = new ProjectStatsResp();
        resp.setProjectId(project.getId());
        resp.setTitle(project.getTitle());
        resp.setStatus(project.getStatus());
        resp.setStartTime(project.getStartTime());
        resp.setEndTime(project.getEndTime());
        resp.setParticipantCount(participantCount);
        resp.setTotalUserCount(studentProfiles.size());
        resp.setOverallCompletionRate(overallCompletionRate);
        resp.setTaskProgressList(taskProgressList);
        resp.setStudentProgressList(studentProgressList);
        return resp;
    }

    @Override
    public Page<TrainProjectListResp> listProjects(TrainProjectQueryDTO query) {
        Page<TrainProject> page =
                new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        LambdaQueryWrapper<TrainProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainProject::getIsDeleted, 0);
        if (query.getStatus() != null) {
            queryWrapper.eq(TrainProject::getStatus, query.getStatus());
        }
        if (query.getType() != null) {
            queryWrapper.eq(TrainProject::getType, query.getType());
        }
        if (StringUtils.hasText(query.getTitleLike())) {
            queryWrapper.like(TrainProject::getTitle, query.getTitleLike());
        }
        applyProjectSort(queryWrapper, query.getSortField(), query.getSortOrder());
        Page<TrainProject> entityPage = trainProjectMapper.selectPage(page, queryWrapper);
        Map<String, Integer> taskCountMap = loadTaskCountMap(entityPage.getRecords());
        Page<TrainProjectListResp> respPage =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        respPage.setRecords(
                entityPage.getRecords().stream()
                        .map(item -> TrainProjectListResp.fromEntity(item, taskCountMap.getOrDefault(item.getId(), 0)))
                        .toList());
        return respPage;
    }

    private TrainProject getExistingProject(String projectId) {
        LambdaQueryWrapper<TrainProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainProject::getId, projectId).eq(TrainProject::getIsDeleted, 0);
        TrainProject project = trainProjectMapper.selectOne(queryWrapper);
        if (project == null) {
            throw new BizException("TRAIN_PROJECT_NOT_FOUND", "培训项目不存在: " + projectId);
        }
        return project;
    }

    private TrainProject requireDraftProject(String projectId) {
        TrainProject project = getExistingProject(projectId);
        if (!Integer.valueOf(1).equals(project.getStatus())) {
            throw new BizException("TRAIN_PROJECT_STATUS_INVALID", "只有草稿状态的培训项目允许编辑");
        }
        return project;
    }

    private List<TrainTask> listTasksByProjectId(String projectId) {
        LambdaQueryWrapper<TrainTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainTask::getProjectId, projectId)
                .eq(TrainTask::getIsDeleted, 0)
                .orderByAsc(TrainTask::getSort)
                .orderByAsc(TrainTask::getCreateTime);
        List<TrainTask> tasks = trainTaskMapper.selectList(queryWrapper);
        return tasks == null ? Collections.emptyList() : tasks;
    }

    private List<TrainUserTask> listUserTasksByProjectIdAndUserId(String projectId, Long userId) {
        LambdaQueryWrapper<TrainUserTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainUserTask::getProjectId, projectId)
                .eq(TrainUserTask::getUserId, userId)
                .eq(TrainUserTask::getIsDeleted, 0)
                .orderByDesc(TrainUserTask::getUpdateTime);
        List<TrainUserTask> userTasks = trainUserTaskMapper.selectList(queryWrapper);
        return userTasks == null ? Collections.emptyList() : userTasks;
    }

    private Map<String, Integer> loadTaskCountMap(List<TrainProject> projects) {
        if (CollectionUtils.isEmpty(projects)) {
            return Collections.emptyMap();
        }

        List<String> projectIds = projects.stream().map(TrainProject::getId).filter(StringUtils::hasText).toList();
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<TrainTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TrainTask::getProjectId, projectIds).eq(TrainTask::getIsDeleted, 0);
        return trainTaskMapper.selectList(queryWrapper).stream()
                .collect(Collectors.groupingBy(TrainTask::getProjectId, Collectors.summingInt(item -> 1)));
    }

    private void validateTaskReferences(List<TrainTaskReq> tasks) {
        Set<String> courseIds =
                tasks.stream()
                        .filter(item -> Integer.valueOf(1).equals(item.getType()))
                        .map(TrainTaskReq::getRefId)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> examPaperIds =
                tasks.stream()
                        .filter(item -> Integer.valueOf(2).equals(item.getType()))
                        .map(TrainTaskReq::getRefId)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> liveRoomIds =
                tasks.stream()
                        .filter(item -> Integer.valueOf(3).equals(item.getType()))
                        .map(TrainTaskReq::getRefId)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String courseId : courseIds) {
            requireCourse(courseId);
        }
        for (String paperId : examPaperIds) {
            requireExamPaper(paperId);
        }
        for (String roomId : liveRoomIds) {
            requireLiveRoom(roomId);
        }
    }

    private Map<String, String> loadCourseTitles(List<TrainTask> tasks) {
        List<TrainTask> courseTasks =
                tasks.stream()
                        .filter(item -> Integer.valueOf(1).equals(item.getType()) && StringUtils.hasText(item.getRefId()))
                        .toList();
        if (courseTasks.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> titleMap = new HashMap<>();
        Set<String> courseIds =
                courseTasks.stream().map(TrainTask::getRefId).collect(Collectors.toCollection(LinkedHashSet::new));
        for (String courseId : courseIds) {
            try {
                CourseFeignResp course = queryCourse(courseId, false);
                if (course != null && StringUtils.hasText(course.getTitle())) {
                    titleMap.put(courseId, course.getTitle());
                }
            } catch (BizException ex) {
                log.warn("加载培训任务课程标题失败, courseId={}, reason={}", courseId, ex.getMessage());
            }
        }
        return titleMap;
    }

    private Map<String, String> loadExamPaperTitles(List<TrainTask> tasks) {
        List<TrainTask> examTasks =
                tasks.stream()
                        .filter(item -> Integer.valueOf(2).equals(item.getType()) && StringUtils.hasText(item.getRefId()))
                        .toList();
        if (examTasks.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> titleMap = new HashMap<>();
        Set<String> examPaperIds = examTasks.stream().map(TrainTask::getRefId).collect(Collectors.toCollection(LinkedHashSet::new));
        for (String paperId : examPaperIds) {
            try {
                ExamPaperFeignResp paper = queryExamPaper(paperId, false);
                if (paper != null && StringUtils.hasText(paper.getTitle())) {
                    titleMap.put(paperId, paper.getTitle());
                }
            } catch (BizException ex) {
                log.warn("加载培训任务试卷标题失败, paperId={}, reason={}", paperId, ex.getMessage());
            }
        }
        return titleMap;
    }

    private Map<String, ExamPaperStatsFeignResp> loadExamStats(List<TrainTask> tasks) {
        List<TrainTask> examTasks =
                tasks.stream()
                        .filter(item -> Integer.valueOf(2).equals(item.getType()) && StringUtils.hasText(item.getRefId()))
                        .toList();
        if (examTasks.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ExamPaperStatsFeignResp> statsMap = new HashMap<>();
        Set<String> paperIds = examTasks.stream().map(TrainTask::getRefId).collect(Collectors.toCollection(LinkedHashSet::new));
        for (String paperId : paperIds) {
            try {
                ExamPaperStatsFeignResp stats = queryExamStats(paperId);
                if (stats != null) {
                    statsMap.put(paperId, stats);
                }
            } catch (BizException ex) {
                log.warn("加载培训项目考试效果统计失败, paperId={}, reason={}", paperId, ex.getMessage());
            }
        }
        return statsMap;
    }

    private UserFeignResp requireUser(Long creatorId) {
        if (creatorId == null || creatorId <= 0L) {
            throw new BizException("TRAIN_PROJECT_CREATOR_INVALID", "创建人ID不合法");
        }
        if (localUserBypass) {
            UserFeignResp user = new UserFeignResp();
            user.setId(creatorId);
            user.setName("local-dev-user-" + creatorId);
            user.setEmail("local-dev-" + creatorId + "@playedu.test");
            user.setIsActive(1);
            user.setIsLock(0);
            user.setDeptIds(List.of(1L));
            return user;
        }
        var result = userFeignClient.getUserById(creatorId);
        if (result == null) {
            throw new BizException("USER_SVC_UNAVAILABLE", "用户服务响应为空");
        }
        if ("USER_SVC_UNAVAILABLE".equals(result.getCode())) {
            throw new BizException("USER_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "用户服务暂不可用，请稍后重试"));
        }
        if (!"0".equals(result.getCode()) || result.getData() == null) {
            throw new BizException("USER_NOT_FOUND", "创建人不存在");
        }
        return result.getData();
    }

    private CourseFeignResp requireCourse(String courseId) {
        CourseFeignResp course = queryCourse(courseId, true);
        if (course == null) {
            throw new BizException("COURSE_NOT_FOUND", "课程不存在: " + courseId);
        }
        return course;
    }

    private CourseFeignResp queryCourse(String courseId, boolean strict) {
        Integer normalizedCourseId = normalizeCourseId(courseId, strict);
        if (normalizedCourseId == null) {
            if (strict) {
                throw new BizException("COURSE_NOT_FOUND", "课程不存在: " + courseId);
            }
            return null;
        }

        var result = courseFeignClient.getCourseById(String.valueOf(normalizedCourseId));
        if (result == null) {
            if (strict) {
                throw new BizException("COURSE_SVC_UNAVAILABLE", "课程服务响应为空");
            }
            return null;
        }
        if ("COURSE_SVC_UNAVAILABLE".equals(result.getCode())) {
            if (strict) {
                throw new BizException("COURSE_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "课程服务暂不可用，请稍后重试"));
            }
            return null;
        }
        if (!"0".equals(result.getCode()) || result.getData() == null) {
            if (strict) {
                throw new BizException("COURSE_NOT_FOUND", "课程不存在: " + courseId);
            }
            return null;
        }
        return result.getData();
    }

    private Integer normalizeCourseId(String courseId, boolean strict) {
        if (!StringUtils.hasText(courseId)) {
            return null;
        }
        try {
            int normalizedId = Integer.parseInt(courseId.trim());
            if (normalizedId <= 0) {
                throw new NumberFormatException("negative or zero");
            }
            return normalizedId;
        } catch (NumberFormatException ex) {
            if (strict) {
                throw new BizException("COURSE_NOT_FOUND", "课程不存在: " + courseId);
            }
            return null;
        }
    }

    private ExamPaperFeignResp requireExamPaper(String paperId) {
        ExamPaperFeignResp paper = queryExamPaper(paperId, true);
        if (paper == null) {
            throw new BizException("EXAM_PAPER_NOT_FOUND", "考试试卷不存在: " + paperId);
        }
        return paper;
    }

    private LiveRoomFeignResp requireLiveRoom(String roomId) {
        LiveRoomFeignResp room = queryLiveRoom(roomId, true);
        if (room == null) {
            throw new BizException("LIVE_ROOM_NOT_FOUND", "直播间不存在: " + roomId);
        }
        return room;
    }

    private ExamPaperFeignResp queryExamPaper(String paperId, boolean strict) {
        var result = examFeignClient.getPaperById(paperId);
        if (result == null) {
            if (strict) {
                throw new BizException("EXAM_SVC_UNAVAILABLE", "考试服务响应为空");
            }
            return null;
        }
        if ("EXAM_SVC_UNAVAILABLE".equals(result.getCode())) {
            if (strict) {
                throw new BizException("EXAM_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "考试服务暂不可用，请稍后重试"));
            }
            return null;
        }
        if (!"0".equals(result.getCode()) || result.getData() == null) {
            if (strict) {
                throw new BizException("EXAM_PAPER_NOT_FOUND", "考试试卷不存在: " + paperId);
            }
            return null;
        }
        return result.getData();
    }

    private ExamPaperStatsFeignResp queryExamStats(String paperId) {
        var result = examFeignClient.getPaperStats(paperId);
        if (result == null) {
            throw new BizException("EXAM_SVC_UNAVAILABLE", "考试服务响应为空");
        }
        if ("EXAM_SVC_UNAVAILABLE".equals(result.getCode())) {
            throw new BizException("EXAM_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "考试服务暂不可用，请稍后重试"));
        }
        if (!"0".equals(result.getCode()) || result.getData() == null) {
            return null;
        }
        return result.getData();
    }

    private LiveRoomFeignResp queryLiveRoom(String roomId, boolean strict) {
        if (!StringUtils.hasText(roomId)) {
            if (strict) {
                throw new BizException("LIVE_ROOM_NOT_FOUND", "直播间不存在: " + roomId);
            }
            return null;
        }

        var result = liveFeignClient.getRoomById(roomId);
        if (result == null) {
            if (strict) {
                throw new BizException("LIVE_SVC_UNAVAILABLE", "直播服务响应为空");
            }
            return null;
        }
        if ("LIVE_SVC_UNAVAILABLE".equals(result.getCode())) {
            if (strict) {
                throw new BizException("LIVE_SVC_UNAVAILABLE", defaultMessage(result.getMsg(), "直播服务暂不可用，请稍后重试"));
            }
            return null;
        }
        if (!"0".equals(result.getCode()) || result.getData() == null) {
            if (strict) {
                throw new BizException("LIVE_ROOM_NOT_FOUND", "直播间不存在: " + roomId);
            }
            return null;
        }
        return result.getData();
    }

    private void validateTargetDeptIdsFormat(String targetDeptIds) {
        if (!StringUtils.hasText(targetDeptIds)) {
            return;
        }

        String[] segments = targetDeptIds.split(",");
        List<Long> normalizedIds = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String value = segment.trim();
            if (!StringUtils.hasText(value)) {
                throw new BizException("TRAIN_PROJECT_TARGET_DEPT_IDS_INVALID", "目标部门ID格式不合法");
            }
            try {
                long deptId = Long.parseLong(value);
                if (deptId <= 0L) {
                    throw new NumberFormatException("negative or zero");
                }
                normalizedIds.add(deptId);
            } catch (NumberFormatException ex) {
                throw new BizException("TRAIN_PROJECT_TARGET_DEPT_IDS_INVALID", "目标部门ID格式不合法");
            }
        }

        if (normalizedIds.stream().filter(Objects::nonNull).distinct().count() != normalizedIds.size()) {
            throw new BizException("TRAIN_PROJECT_TARGET_DEPT_IDS_INVALID", "目标部门ID不允许重复");
        }
    }

    private List<LocalStudentProfile> buildMockStudentProfiles() {
        return List.of(
                new LocalStudentProfile(10001L, "学员1", "研发部"),
                new LocalStudentProfile(10002L, "学员2", "研发部"),
                new LocalStudentProfile(10003L, "学员3", "产品部"),
                new LocalStudentProfile(10004L, "学员4", "运营部"),
                new LocalStudentProfile(10005L, "学员5", "市场部"));
    }

    private String resolveTaskResourceTitle(
            TrainTask task, Map<String, String> courseTitleMap, Map<String, String> examPaperTitleMap) {
        if (Integer.valueOf(1).equals(task.getType())) {
            return defaultMessage(courseTitleMap.get(task.getRefId()), task.getName());
        }
        if (Integer.valueOf(2).equals(task.getType())) {
            return defaultMessage(examPaperTitleMap.get(task.getRefId()), task.getName());
        }
        return task.getName();
    }

    private int applyMockCourseProgress(TrainTask task, Map<Long, StudentProgressAccumulator> progressMap) {
        int completedCount = 0;
        for (Map.Entry<Long, StudentProgressAccumulator> entry : progressMap.entrySet()) {
            boolean completed = isMockTaskCompleted(task, entry.getKey(), 82);
            entry.getValue().recordCourse(completed);
            if (completed) {
                completedCount++;
            }
        }
        return completedCount;
    }

    private int applyMockLiveProgress(TrainTask task, Map<Long, StudentProgressAccumulator> progressMap) {
        int completedCount = 0;
        for (Map.Entry<Long, StudentProgressAccumulator> entry : progressMap.entrySet()) {
            boolean completed = isMockTaskCompleted(task, entry.getKey(), 64);
            entry.getValue().recordLive(completed);
            if (completed) {
                completedCount++;
            }
        }
        return completedCount;
    }

    private int applyMockHomeworkProgress(TrainTask task, Map<Long, StudentProgressAccumulator> progressMap) {
        int completedCount = 0;
        for (Map.Entry<Long, StudentProgressAccumulator> entry : progressMap.entrySet()) {
            boolean completed = isMockTaskCompleted(task, entry.getKey(), 58);
            entry.getValue().recordHomework(completed);
            if (completed) {
                completedCount++;
            }
        }
        return completedCount;
    }

    private int applyExamProgress(
            TrainTask task,
            Map<Long, StudentProgressAccumulator> progressMap,
            ExamPaperStatsFeignResp examStats) {
        Map<Long, ExamPaperStudentFeignResp> studentStats = new HashMap<>();
        if (examStats != null && !CollectionUtils.isEmpty(examStats.getStudents())) {
            for (ExamPaperStudentFeignResp student : examStats.getStudents()) {
                if (student.getUserId() != null) {
                    studentStats.put(student.getUserId(), student);
                }
            }
        }

        int completedCount = 0;
        for (Map.Entry<Long, StudentProgressAccumulator> entry : progressMap.entrySet()) {
            ExamPaperStudentFeignResp student = studentStats.get(entry.getKey());
            entry.getValue().recordExam(student);
            if (student != null && Boolean.TRUE.equals(student.getPassed())) {
                completedCount++;
            }
        }
        return completedCount;
    }

    private boolean isMockTaskCompleted(TrainTask task, Long userId, int threshold) {
        return Math.floorMod(Objects.hash(task.getId(), task.getRefId(), userId), 100) < threshold;
    }

    private Integer defaultTaskType(Integer taskType) {
        return taskType == null ? 0 : taskType;
    }

    private String resolveMetricLabel(Integer taskType) {
        if (Integer.valueOf(2).equals(taskType)) {
            return "通过人数";
        }
        return "完成人数";
    }

    private TrainProjectMyDetailDTO.TaskItemDTO toMyTaskItem(TrainTask task, TrainUserTask userTask) {
        TrainProjectMyDetailDTO.TaskItemDTO item = new TrainProjectMyDetailDTO.TaskItemDTO();
        item.setTaskId(task.getId());
        item.setTaskName(task.getName());
        item.setTaskType(resolveMyTaskType(task.getType()));
        item.setResourceId(task.getRefId());
        item.setSort(task.getSort());
        item.setRequired(Integer.valueOf(1).equals(task.getRequired()));
        if (userTask != null) {
            item.setStatus(defaultMessage(userTask.getStatus(), "NOT_STARTED"));
            item.setCompletedAt(userTask.getCompletedAt());
        } else {
            item.setStatus("NOT_STARTED");
        }
        return item;
    }

    private String resolveMyTaskType(Integer taskType) {
        return switch (defaultTaskType(taskType)) {
            case 1 -> "COURSE";
            case 2 -> "EXAM";
            case 3 -> "LIVE";
            case 4 -> "ASSIGNMENT";
            default -> "UNKNOWN";
        };
    }

    private int resolveNextSort(String projectId) {
        List<TrainTask> tasks = listTasksByProjectId(projectId);
        return tasks.stream()
                .map(TrainTask::getSort)
                .filter(item -> item != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    private void validateProjectTime(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BizException("TRAIN_PROJECT_TIME_INVALID", "培训开始时间不能晚于结束时间");
        }
    }

    private void applyProjectSort(
            LambdaQueryWrapper<TrainProject> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortField) ? sortField : "createTime";
        switch (field) {
            case "startTime" -> queryWrapper.orderBy(true, asc, TrainProject::getStartTime);
            case "endTime" -> queryWrapper.orderBy(true, asc, TrainProject::getEndTime);
            case "status" -> queryWrapper.orderBy(true, asc, TrainProject::getStatus);
            default -> queryWrapper.orderBy(true, asc, TrainProject::getCreateTime);
        }
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }

    private String defaultMessage(String message, String fallback) {
        return StringUtils.hasText(message) ? message : fallback;
    }

    private int toPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return (int) Math.round(numerator * 100D / denominator);
    }

    private String formatStatus(String completedLabel, int completedCount, int totalCount) {
        if (totalCount <= 0) {
            return "-";
        }
        if (totalCount == 1) {
            return completedCount > 0 ? completedLabel : "未完成";
        }
        return completedLabel + " " + completedCount + "/" + totalCount;
    }

    private String formatExamStatus(int totalCount, int passedCount, Integer latestStatus, Integer latestScore) {
        if (totalCount <= 0) {
            return "-";
        }
        if (totalCount == 1) {
            if (passedCount > 0) {
                return latestScore == null ? "已通过" : "已通过(" + latestScore + "分)";
            }
            if (Integer.valueOf(1).equals(latestStatus)) {
                return "进行中";
            }
            if (Integer.valueOf(2).equals(latestStatus)) {
                return "待评分";
            }
            if (latestScore != null && latestScore > 0) {
                return "未通过(" + latestScore + "分)";
            }
            return "未参加";
        }
        return "已通过 " + passedCount + "/" + totalCount;
    }

    private record LocalStudentProfile(Long userId, String userName, String deptName) {}

    private final class StudentProgressAccumulator {
        private final StudentProgressResp resp;
        private int completedTaskCount;
        private int courseCompletedCount;
        private int courseTaskCount;
        private int examPassedCount;
        private int examTaskCount;
        private Integer latestExamStatus;
        private Integer latestExamScore;
        private int liveCompletedCount;
        private int liveTaskCount;
        private int homeworkCompletedCount;
        private int homeworkTaskCount;

        private StudentProgressAccumulator(LocalStudentProfile profile) {
            this.resp = new StudentProgressResp();
            this.resp.setUserId(profile.userId());
            this.resp.setUserName(profile.userName());
            this.resp.setDeptName(profile.deptName());
        }

        private void recordCourse(boolean completed) {
            courseTaskCount++;
            if (completed) {
                courseCompletedCount++;
                completedTaskCount++;
            }
        }

        private void recordExam(ExamPaperStudentFeignResp examStudent) {
            examTaskCount++;
            if (examStudent != null) {
                latestExamStatus = examStudent.getStatus();
                latestExamScore = examStudent.getObtainScore();
            }
            if (examStudent != null && Boolean.TRUE.equals(examStudent.getPassed())) {
                examPassedCount++;
                completedTaskCount++;
            }
        }

        private void recordLive(boolean completed) {
            liveTaskCount++;
            if (completed) {
                liveCompletedCount++;
                completedTaskCount++;
            }
        }

        private void recordHomework(boolean completed) {
            homeworkTaskCount++;
            if (completed) {
                homeworkCompletedCount++;
                completedTaskCount++;
            }
        }

        private StudentProgressResp finish(int totalTaskCount) {
            resp.setCourseStatus(formatStatus("已完成", courseCompletedCount, courseTaskCount));
            resp.setExamStatus(formatExamStatus(examTaskCount, examPassedCount, latestExamStatus, latestExamScore));
            resp.setLiveStatus(formatStatus("已观看", liveCompletedCount, liveTaskCount));
            resp.setHomeworkStatus(formatStatus("已提交", homeworkCompletedCount, homeworkTaskCount));
            resp.setCompletedTaskCount(completedTaskCount);
            resp.setTotalTaskCount(totalTaskCount);
            resp.setOverallCompletionRate(toPercentage(completedTaskCount, totalTaskCount));
            return resp;
        }
    }
}
