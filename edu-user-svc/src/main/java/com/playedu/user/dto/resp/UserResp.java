package com.playedu.user.dto.resp;

import com.playedu.user.domain.entity.User;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class UserResp {
    private Long id;

    private String email;

    private String name;

    private Integer avatar;

    private Integer isActive;

    private Integer isLock;

    private List<Long> deptIds;

    public static UserResp from(User user, List<Long> deptIds) {
        UserResp resp = new UserResp();
        resp.setId(user.getId() == null ? null : user.getId().longValue());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setAvatar(user.getAvatar());
        resp.setIsActive(user.getIsActive());
        resp.setIsLock(user.getIsLock());
        resp.setDeptIds(deptIds == null ? Collections.emptyList() : deptIds);
        return resp;
    }
}
