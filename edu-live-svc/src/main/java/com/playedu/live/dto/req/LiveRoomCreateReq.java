package com.playedu.live.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LiveRoomCreateReq {
    @NotBlank(message = "直播间标题不能为空")
    @Size(max = 200, message = "直播间标题长度不能超过200字符")
    private String title;

    @NotNull(message = "关联课程不能为空")
    @Min(value = 1, message = "关联课程ID不合法")
    private Integer courseId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Size(max = 512, message = "推流地址长度不能超过512字符")
    private String pushUrl;

    @Size(max = 512, message = "播放地址长度不能超过512字符")
    private String playUrl;

    @Size(max = 512, message = "回放地址长度不能超过512字符")
    private String recordUrl;
}
