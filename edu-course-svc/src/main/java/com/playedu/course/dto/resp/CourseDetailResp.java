package com.playedu.course.dto.resp;

import com.playedu.course.domain.entity.Course;
import com.playedu.course.support.CourseExtraPayload;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CourseDetailResp {
    private Integer id;

    private String title;

    private Integer thumb;

    private Integer charge;

    private String shortDesc;

    private Integer type;

    private String coverUrl;

    private Integer isRequired;

    private Integer classHour;

    private Integer isShow;

    private LocalDateTime createdAt;

    private LocalDateTime sortAt;

    private String extra;

    private Integer adminId;

    private List<Integer> categoryIds;

    private List<CourseChapterResp> chapters;

    public static CourseDetailResp fromEntity(Course course) {
        CourseExtraPayload extraPayload = CourseExtraPayload.fromJson(course.getExtra());
        CourseDetailResp resp = new CourseDetailResp();
        resp.setId(course.getId());
        resp.setTitle(course.getTitle());
        resp.setThumb(course.getThumb());
        resp.setCharge(course.getCharge());
        resp.setShortDesc(course.getShortDesc());
        resp.setType(extraPayload.getCourseType());
        resp.setCoverUrl(extraPayload.getCoverUrl());
        resp.setIsRequired(course.getIsRequired());
        resp.setClassHour(course.getClassHour());
        resp.setIsShow(course.getIsShow());
        resp.setCreatedAt(course.getCreatedAt());
        resp.setSortAt(course.getSortAt());
        resp.setExtra(course.getExtra());
        resp.setAdminId(course.getAdminId());
        return resp;
    }
}
