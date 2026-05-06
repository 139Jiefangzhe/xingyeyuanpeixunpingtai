package com.playedu.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.course.dto.query.CourseQueryDTO;
import com.playedu.course.dto.req.CourseSaveReq;
import com.playedu.course.dto.resp.CourseCategoryOptionResp;
import com.playedu.course.dto.resp.CourseChapterResp;
import com.playedu.course.dto.resp.CourseDetailResp;
import com.playedu.course.dto.resp.CourseSimpleResp;
import java.util.List;

public interface CourseService {
    Integer createCourse(Long operatorId, CourseSaveReq req);

    void updateCourse(Integer id, Long operatorId, CourseSaveReq req);

    void deleteCourse(Integer id, Long operatorId);

    CourseDetailResp getCourseById(Integer id);

    List<CourseChapterResp> getCourseChapters(Integer id);

    Page<CourseSimpleResp> listCourses(CourseQueryDTO query);

    List<CourseCategoryOptionResp> listCategoryOptions();
}
