package com.playedu.course.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.playedu.common.util.JacksonUtil;
import com.playedu.course.dto.req.CourseChapterReq;
import com.playedu.course.dto.req.CourseLessonReq;
import com.playedu.course.dto.req.CourseSaveReq;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseExtraPayload {
    private Integer courseType;

    private String coverUrl;

    private Map<String, String> lessonResourceUrls = new LinkedHashMap<>();

    public static CourseExtraPayload fromJson(String extra) {
        if (!StringUtils.hasText(extra)) {
            return new CourseExtraPayload();
        }
        try {
            CourseExtraPayload payload = JacksonUtil.parse(extra, CourseExtraPayload.class);
            if (payload.lessonResourceUrls == null) {
                payload.lessonResourceUrls = new LinkedHashMap<>();
            }
            return payload;
        } catch (IllegalArgumentException ex) {
            return new CourseExtraPayload();
        }
    }

    public static String toJson(CourseSaveReq req) {
        CourseExtraPayload payload = new CourseExtraPayload();
        payload.setCourseType(req.getType());
        payload.setCoverUrl(req.getCoverUrl());
        payload.setLessonResourceUrls(buildLessonResourceUrlMap(req.getChapters()));
        return JacksonUtil.toJson(payload);
    }

    private static Map<String, String> buildLessonResourceUrlMap(List<CourseChapterReq> chapters) {
        Map<String, String> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(chapters)) {
            return result;
        }
        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            CourseChapterReq chapter = chapters.get(chapterIndex);
            List<CourseLessonReq> lessons = chapter.getLessons();
            if (CollectionUtils.isEmpty(lessons)) {
                continue;
            }
            for (int lessonIndex = 0; lessonIndex < lessons.size(); lessonIndex++) {
                CourseLessonReq lesson = lessons.get(lessonIndex);
                if (StringUtils.hasText(lesson.getResourceUrl())) {
                    result.put(buildLessonKey(chapterIndex + 1, lessonIndex + 1), lesson.getResourceUrl().trim());
                }
            }
        }
        return result;
    }

    public static String buildLessonKey(int chapterSort, int lessonSort) {
        return chapterSort + "-" + lessonSort;
    }
}
