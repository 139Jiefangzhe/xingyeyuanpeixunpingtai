package com.playedu.course.dto.resp;

import com.playedu.course.domain.entity.CourseHour;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseHourResp {
    private Integer id;

    private Integer courseId;

    private Integer chapterId;

    private Integer sort;

    private String title;

    private String type;

    private Integer rid;

    private Integer duration;

    private String resourceUrl;

    private LocalDateTime createdAt;

    public static CourseHourResp fromEntity(CourseHour hour, String resourceUrl) {
        CourseHourResp resp = new CourseHourResp();
        resp.setId(hour.getId());
        resp.setCourseId(hour.getCourseId());
        resp.setChapterId(hour.getChapterId());
        resp.setSort(hour.getSort());
        resp.setTitle(hour.getTitle());
        resp.setType(hour.getType());
        resp.setRid(hour.getRid());
        resp.setDuration(hour.getDuration());
        resp.setResourceUrl(resourceUrl);
        resp.setCreatedAt(hour.getCreatedAt());
        return resp;
    }
}
