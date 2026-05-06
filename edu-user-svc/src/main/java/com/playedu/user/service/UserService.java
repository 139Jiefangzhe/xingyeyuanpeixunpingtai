package com.playedu.user.service;

import com.playedu.user.dto.resp.UserResp;
import java.util.List;

public interface UserService {
    UserResp getUserById(Long id);

    List<UserResp> batchGetUsers(List<Long> userIds);
}
