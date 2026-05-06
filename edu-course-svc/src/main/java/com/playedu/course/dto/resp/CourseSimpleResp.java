package com.playedu.course.dto.resp;

import com.playedu.course.domain.entity.Course;
import com.playedu.course.support.CourseExtraPayload;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CourseSimpleResp {
    private Integer id;

    private String title;

    private Integer thumb;

    private Integer charge;

    private Integer classHour;

    private Integer type;

    private String coverUrl;

    private Integer isRequired;

    private Integer isShow;

    private LocalDateTime createdAt;

    private List<Integer> categoryIds;

    public static CourseSimpleResp fromEntity(Course course, List<Integer> categoryIds) {
        CourseExtraPayload extraPayload = CourseExtraPayload.fromJson(course.getExtra());
        CourseSimpleResp resp = new CourseSimpleResp();
        resp.setId(course.getId());
        resp.setTitle(course.getTitle());
        resp.setThumb(course.getThumb());
        resp.setCharge(course.getCharge());
        resp.setClassHour(course.getClassHour());
        resp.setType(extraPayload.getCourseType());
        resp.setCoverUrl(extraPayload.getCoverUrl());
        resp.setIsRequired(course.getIsRequired());
        resp.setIsShow(course.getIsShow());
        resp.setCreatedAt(course.getCreatedAt());
        resp.setCategoryIds(categoryIds);
        return resp;
    }
}
