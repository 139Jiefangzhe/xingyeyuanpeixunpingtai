package com.playedu.exam.dto.resp;

import java.util.List;
import lombok.Data;

@Data
public class UserFeignResp {
    private Long id;

    private String email;

    private String name;

    private Integer avatar;

    private Integer isActive;

    private Integer isLock;

    private List<Long> deptIds;
}
