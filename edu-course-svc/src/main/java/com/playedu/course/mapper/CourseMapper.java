package com.playedu.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playedu.course.domain.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {}
