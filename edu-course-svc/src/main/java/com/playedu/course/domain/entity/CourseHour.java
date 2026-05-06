package com.playedu.course.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("course_hour")
public class CourseHour implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("course_id")
    private Integer courseId;

    @TableField("chapter_id")
    private Integer chapterId;

    private Integer sort;

    private String title;

    private String type;

    private Integer rid;

    private Integer duration;

    @TableField("created_at")
    private LocalDateTime createdAt;

    private Integer deleted;
}
