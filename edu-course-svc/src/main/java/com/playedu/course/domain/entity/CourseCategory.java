package com.playedu.course.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("resource_course_category")
public class CourseCategory implements Serializable {
    @TableId(value = "course_id", type = IdType.INPUT)
    private Integer courseId;

    @TableField("category_id")
    private Integer categoryId;
}
