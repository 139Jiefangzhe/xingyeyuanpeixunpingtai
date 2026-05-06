package com.playedu.course.dto.resp;

import com.playedu.course.domain.entity.CourseChapter;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CourseChapterResp {
    private Integer id;

    private Integer courseId;

    private String name;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<CourseHourResp> lessons;

    public static CourseChapterResp fromEntity(CourseChapter chapter, List<CourseHourResp> lessons) {
        CourseChapterResp resp = new CourseChapterResp();
        resp.setId(chapter.getId());
        resp.setCourseId(chapter.getCourseId());
        resp.setName(chapter.getName());
        resp.setSort(chapter.getSort());
        resp.setCreatedAt(chapter.getCreatedAt());
        resp.setUpdatedAt(chapter.getUpdatedAt());
        resp.setLessons(lessons);
        return resp;
    }
}
