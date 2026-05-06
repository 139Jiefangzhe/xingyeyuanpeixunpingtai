package com.playedu.live.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playedu.common.domain.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_live_room")
public class LiveRoom extends BaseEntity {
    private String title;

    @TableField("course_id")
    private Integer courseId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private Integer status;

    @TableField("push_url")
    private String pushUrl;

    @TableField("play_url")
    private String playUrl;

    @TableField("record_url")
    private String recordUrl;

    @TableField("creator_id")
    private Long creatorId;
}
