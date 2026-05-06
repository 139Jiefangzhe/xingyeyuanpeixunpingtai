package com.playedu.course.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("courses")
public class Course implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private Integer thumb;

    private Integer charge;

    @TableField("short_desc")
    private String shortDesc;

    @TableField("is_required")
    private Integer isRequired;

    @TableField("class_hour")
    private Integer classHour;

    @TableField("is_show")
    private Integer isShow;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("sort_at")
    private LocalDateTime sortAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    private String extra;

    @TableField("admin_id")
    private Integer adminId;
}
