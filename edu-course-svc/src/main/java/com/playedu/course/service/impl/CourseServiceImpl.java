package com.playedu.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.common.exception.BizException;
import com.playedu.course.domain.entity.Course;
import com.playedu.course.domain.entity.CourseCategory;
import com.playedu.course.domain.entity.CourseChapter;
import com.playedu.course.domain.entity.CourseHour;
import com.playedu.course.dto.query.CourseQueryDTO;
import com.playedu.course.dto.req.CourseChapterReq;
import com.playedu.course.dto.req.CourseLessonReq;
import com.playedu.course.dto.req.CourseSaveReq;
import com.playedu.course.dto.resp.CourseCategoryOptionResp;
import com.playedu.course.dto.resp.CourseChapterResp;
import com.playedu.course.dto.resp.CourseDetailResp;
import com.playedu.course.dto.resp.CourseHourResp;
import com.playedu.course.dto.resp.CourseSimpleResp;
import com.playedu.course.mapper.CourseCategoryMapper;
import com.playedu.course.mapper.CourseChapterMapper;
import com.playedu.course.mapper.CourseHourMapper;
import com.playedu.course.mapper.CourseMapper;
import com.playedu.course.service.CourseService;
import com.playedu.course.support.CourseExtraPayload;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CourseServiceImpl implements CourseService {
    private static final Map<Integer, String> DEFAULT_CATEGORY_LABELS =
            Map.of(1, "后端开发", 2, "前端工程", 3, "直播教学");

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseHourMapper courseHourMapper;
    private final CourseCategoryMapper courseCategoryMapper;

    public CourseServiceImpl(
            CourseMapper courseMapper,
            CourseChapterMapper courseChapterMapper,
            CourseHourMapper courseHourMapper,
            CourseCategoryMapper courseCategoryMapper) {
        this.courseMapper = courseMapper;
        this.courseChapterMapper = courseChapterMapper;
        this.courseHourMapper = courseHourMapper;
        this.courseCategoryMapper = courseCategoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createCourse(Long operatorId, CourseSaveReq req) {
        validateCourseSaveReq(req);
        Course course = new Course();
        course.setId(nextCourseId());
        applyCourseValue(course, operatorId, req, true);
        courseMapper.insert(course);
        replaceCourseCategories(course.getId(), req.getCategoryIds());
        replaceCourseStructure(course.getId(), req.getType(), req.getChapters());
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Integer id, Long operatorId, CourseSaveReq req) {
        validateCourseSaveReq(req);
        Course course = requireCourse(id);
        applyCourseValue(course, operatorId, req, false);
        courseMapper.updateById(course);
        replaceCourseCategories(course.getId(), req.getCategoryIds());
        replaceCourseStructure(course.getId(), req.getType(), req.getChapters());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Integer id, Long operatorId) {
        Course course = requireCourse(id);
        course.setDeletedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.setAdminId(normalizeOperatorId(operatorId));
        courseMapper.updateById(course);
    }

    @Override
    public CourseDetailResp getCourseById(Integer id) {
        Course course = requireCourse(id);
        return buildCourseDetail(course);
    }

    @Override
    public List<CourseChapterResp> getCourseChapters(Integer id) {
        requireCourse(id);
        return buildChapterResponses(id);
    }

    @Override
    public Page<CourseSimpleResp> listCourses(CourseQueryDTO query) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(Course::getDeletedAt);
        if (StringUtils.hasText(query.getTitleLike())) {
            queryWrapper.like(Course::getTitle, query.getTitleLike());
        }
        if (query.getIsRequired() != null) {
            queryWrapper.eq(Course::getIsRequired, query.getIsRequired());
        }
        if (query.getIsShow() != null) {
            queryWrapper.eq(Course::getIsShow, query.getIsShow());
        }

        if (query.getCategoryId() != null) {
            List<Integer> courseIds = resolveCourseIdsByCategoryId(query.getCategoryId());
            if (courseIds.isEmpty()) {
                return new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()), 0);
            }
            queryWrapper.in(Course::getId, courseIds);
        }

        if (query.getType() != null) {
            applySort(queryWrapper, query.getSortField(), query.getSortOrder());
            List<Course> courses = courseMapper.selectList(queryWrapper);
            Map<Integer, List<Integer>> categoryIdMap = loadCategoryIdMap(courses.stream().map(Course::getId).toList());
            List<CourseSimpleResp> filtered =
                    courses.stream()
                            .map(course -> CourseSimpleResp.fromEntity(course, categoryIdMap.getOrDefault(course.getId(), Collections.emptyList())))
                            .filter(item -> Objects.equals(item.getType(), query.getType()))
                            .toList();
            return paginate(filtered, query.getPageNum(), query.getPageSize());
        }

        Page<Course> page = new Page<>(defaultPageNum(query.getPageNum()), defaultPageSize(query.getPageSize()));
        applySort(queryWrapper, query.getSortField(), query.getSortOrder());
        Page<Course> entityPage = courseMapper.selectPage(page, queryWrapper);
        Map<Integer, List<Integer>> categoryIdMap =
                loadCategoryIdMap(entityPage.getRecords().stream().map(Course::getId).toList());

        Page<CourseSimpleResp> respPage =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        respPage.setRecords(
                entityPage.getRecords().stream()
                        .map(
                                course ->
                                        CourseSimpleResp.fromEntity(
                                                course, categoryIdMap.getOrDefault(course.getId(), Collections.emptyList())))
                        .toList());
        return respPage;
    }

    @Override
    public List<CourseCategoryOptionResp> listCategoryOptions() {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CourseCategory::getCategoryId);
        return courseCategoryMapper.selectList(queryWrapper).stream()
                .map(CourseCategory::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(categoryId -> new CourseCategoryOptionResp(categoryId, resolveCategoryLabel(categoryId)))
                .toList();
    }

    private List<CourseChapter> listChaptersByCourseId(Integer courseId) {
        LambdaQueryWrapper<CourseChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseChapter::getCourseId, courseId).orderByAsc(CourseChapter::getSort).orderByAsc(CourseChapter::getId);
        return courseChapterMapper.selectList(queryWrapper);
    }

    private Course requireCourse(Integer id) {
        Course course = courseMapper.selectById(id);
        if (course == null || course.getDeletedAt() != null) {
            throw new BizException("404001", "课程不存在");
        }
        return course;
    }

    private CourseDetailResp buildCourseDetail(Course course) {
        List<CourseCategory> categories = listCategoriesByCourseIds(List.of(course.getId()));
        CourseDetailResp resp = CourseDetailResp.fromEntity(course);
        resp.setCategoryIds(
                categories.stream()
                        .map(CourseCategory::getCategoryId)
                        .collect(Collectors.toCollection(ArrayList::new)));
        resp.setChapters(buildChapterResponses(course.getId()));
        return resp;
    }

    private List<CourseChapterResp> buildChapterResponses(Integer courseId) {
        Course course = requireCourse(courseId);
        CourseExtraPayload extraPayload = CourseExtraPayload.fromJson(course.getExtra());
        List<CourseChapter> chapters = listChaptersByCourseId(courseId);
        List<CourseHour> hours = listHoursByCourseId(courseId);
        Map<Integer, List<CourseHourResp>> lessonMap =
                hours.stream()
                        .map(
                                hour ->
                                        CourseHourResp.fromEntity(
                                                hour,
                                                extraPayload
                                                        .getLessonResourceUrls()
                                                        .getOrDefault(
                                                                CourseExtraPayload.buildLessonKey(
                                                                        resolveChapterSort(chapters, hour.getChapterId()),
                                                                        hour.getSort() == null ? 1 : hour.getSort()),
                                                                null)))
                        .collect(Collectors.groupingBy(CourseHourResp::getChapterId));
        return chapters.stream()
                .map(chapter -> CourseChapterResp.fromEntity(chapter, lessonMap.getOrDefault(chapter.getId(), Collections.emptyList())))
                .toList();
    }

    private int resolveChapterSort(List<CourseChapter> chapters, Integer chapterId) {
        return chapters.stream()
                .filter(item -> Objects.equals(item.getId(), chapterId))
                .map(CourseChapter::getSort)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(1);
    }

    private List<CourseHour> listHoursByCourseId(Integer courseId) {
        LambdaQueryWrapper<CourseHour> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseHour::getCourseId, courseId)
                .eq(CourseHour::getDeleted, 0)
                .orderByAsc(CourseHour::getChapterId)
                .orderByAsc(CourseHour::getSort)
                .orderByAsc(CourseHour::getId);
        return courseHourMapper.selectList(queryWrapper);
    }

    private List<Integer> resolveCourseIdsByCategoryId(Integer categoryId) {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCategory::getCategoryId, categoryId);
        return courseCategoryMapper.selectList(queryWrapper).stream()
                .map(CourseCategory::getCourseId)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }

    private Map<Integer, List<Integer>> loadCategoryIdMap(Collection<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CourseCategory> categories = listCategoriesByCourseIds(courseIds);
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (CourseCategory category : categories) {
            result.computeIfAbsent(category.getCourseId(), key -> new ArrayList<>()).add(category.getCategoryId());
        }
        return result;
    }

    private List<CourseCategory> listCategoriesByCourseIds(Collection<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> normalizedIds = new LinkedHashSet<>(courseIds);
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CourseCategory::getCourseId, normalizedIds);
        return courseCategoryMapper.selectList(queryWrapper);
    }

    private void applySort(LambdaQueryWrapper<Course> queryWrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if ("charge".equalsIgnoreCase(sortField)) {
            queryWrapper.orderBy(true, asc, Course::getCharge);
            return;
        }
        if ("classHour".equalsIgnoreCase(sortField) || "class_hour".equalsIgnoreCase(sortField)) {
            queryWrapper.orderBy(true, asc, Course::getClassHour);
            return;
        }
        if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
            queryWrapper.orderBy(true, asc, Course::getCreatedAt);
            return;
        }
        if ("sortAt".equalsIgnoreCase(sortField) || "sort_at".equalsIgnoreCase(sortField)) {
            queryWrapper.orderBy(true, asc, Course::getSortAt);
            return;
        }
        queryWrapper.orderByDesc(Course::getId);
    }

    private void validateCourseSaveReq(CourseSaveReq req) {
        if (CollectionUtils.isEmpty(req.getChapters())) {
            return;
        }
        for (CourseChapterReq chapter : req.getChapters()) {
            if (CollectionUtils.isEmpty(chapter.getLessons())) {
                continue;
            }
            for (CourseLessonReq lesson : chapter.getLessons()) {
                if (!StringUtils.hasText(lesson.getTitle())) {
                    throw new BizException("COURSE_LESSON_TITLE_REQUIRED", "课节标题不能为空");
                }
            }
        }
    }

    private void applyCourseValue(Course course, Long operatorId, CourseSaveReq req, boolean creating) {
        LocalDateTime now = LocalDateTime.now();
        course.setTitle(req.getTitle().trim());
        course.setShortDesc(StringUtils.hasText(req.getShortDesc()) ? req.getShortDesc().trim() : null);
        course.setCharge(0);
        course.setThumb(0);
        course.setIsRequired(0);
        course.setClassHour(req.getClassHour());
        course.setIsShow(req.getIsShow());
        course.setSortAt(now);
        course.setUpdatedAt(now);
        course.setExtra(CourseExtraPayload.toJson(req));
        course.setAdminId(normalizeOperatorId(operatorId));
        if (creating) {
            course.setCreatedAt(now);
            course.setDeletedAt(null);
        }
    }

    private int normalizeOperatorId(Long operatorId) {
        if (operatorId == null || operatorId <= 0L) {
            return 0;
        }
        return operatorId > Integer.MAX_VALUE ? Integer.MAX_VALUE : operatorId.intValue();
    }

    private void replaceCourseCategories(Integer courseId, List<Integer> categoryIds) {
        LambdaQueryWrapper<CourseCategory> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CourseCategory::getCourseId, courseId);
        courseCategoryMapper.delete(deleteWrapper);
        if (CollectionUtils.isEmpty(categoryIds)) {
            return;
        }
        for (Integer categoryId : new LinkedHashSet<>(categoryIds)) {
            CourseCategory category = new CourseCategory();
            category.setCourseId(courseId);
            category.setCategoryId(categoryId);
            courseCategoryMapper.insert(category);
        }
    }

    private void replaceCourseStructure(Integer courseId, Integer courseType, List<CourseChapterReq> chapters) {
        LambdaQueryWrapper<CourseHour> hourDeleteWrapper = new LambdaQueryWrapper<>();
        hourDeleteWrapper.eq(CourseHour::getCourseId, courseId);
        courseHourMapper.delete(hourDeleteWrapper);

        LambdaQueryWrapper<CourseChapter> chapterDeleteWrapper = new LambdaQueryWrapper<>();
        chapterDeleteWrapper.eq(CourseChapter::getCourseId, courseId);
        courseChapterMapper.delete(chapterDeleteWrapper);

        if (CollectionUtils.isEmpty(chapters)) {
            return;
        }

        int nextChapterId = nextChapterId();
        int nextHourId = nextHourId();
        String lessonType = mapLessonType(courseType);
        LocalDateTime now = LocalDateTime.now();

        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            CourseChapterReq chapterReq = chapters.get(chapterIndex);
            CourseChapter chapter = new CourseChapter();
            chapter.setId(nextChapterId++);
            chapter.setCourseId(courseId);
            chapter.setName(chapterReq.getName().trim());
            chapter.setSort(chapterIndex + 1);
            chapter.setCreatedAt(now);
            chapter.setUpdatedAt(now);
            courseChapterMapper.insert(chapter);

            List<CourseLessonReq> lessons = chapterReq.getLessons();
            if (CollectionUtils.isEmpty(lessons)) {
                continue;
            }
            for (int lessonIndex = 0; lessonIndex < lessons.size(); lessonIndex++) {
                CourseLessonReq lessonReq = lessons.get(lessonIndex);
                CourseHour hour = new CourseHour();
                hour.setId(nextHourId++);
                hour.setCourseId(courseId);
                hour.setChapterId(chapter.getId());
                hour.setSort(lessonIndex + 1);
                hour.setTitle(lessonReq.getTitle().trim());
                hour.setType(lessonType);
                hour.setRid(0);
                hour.setDuration(lessonReq.getDuration() == null ? 0 : lessonReq.getDuration());
                hour.setCreatedAt(now);
                hour.setDeleted(0);
                courseHourMapper.insert(hour);
            }
        }
    }

    private String mapLessonType(Integer courseType) {
        return switch (courseType == null ? 1 : courseType) {
            case 2 -> "document";
            case 3 -> "live";
            default -> "video";
        };
    }

    private Integer nextCourseId() {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Course::getId).last("limit 1");
        return courseMapper.selectList(queryWrapper).stream().findFirst().map(item -> item.getId() + 1).orElse(1);
    }

    private Integer nextChapterId() {
        LambdaQueryWrapper<CourseChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CourseChapter::getId).last("limit 1");
        return courseChapterMapper.selectList(queryWrapper).stream().findFirst().map(item -> item.getId() + 1).orElse(1);
    }

    private Integer nextHourId() {
        LambdaQueryWrapper<CourseHour> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CourseHour::getId).last("limit 1");
        return courseHourMapper.selectList(queryWrapper).stream().findFirst().map(item -> item.getId() + 1).orElse(1);
    }

    private String resolveCategoryLabel(Integer categoryId) {
        return DEFAULT_CATEGORY_LABELS.getOrDefault(categoryId, "分类" + categoryId);
    }

    private Page<CourseSimpleResp> paginate(List<CourseSimpleResp> records, Integer pageNum, Integer pageSize) {
        long current = defaultPageNum(pageNum);
        long size = defaultPageSize(pageSize);
        int fromIndex = (int) Math.min(Math.max((current - 1) * size, 0), records.size());
        int toIndex = (int) Math.min(fromIndex + size, records.size());
        Page<CourseSimpleResp> page = new Page<>(current, size, records.size());
        page.setRecords(records.subList(fromIndex, toIndex));
        return page;
    }

    private long defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum.longValue();
    }

    private long defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100);
    }
}
