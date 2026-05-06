package com.playedu.exam.fallback;

import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.resp.UserFeignResp;
import com.playedu.exam.feign.UserFeignClient;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("edu-user-svc 调用失败: {}", cause.getMessage(), cause);
        return new UserFeignClient() {
            @Override
            public Result<UserFeignResp> getUserById(Long userId) {
                return Result.error("USER_SVC_UNAVAILABLE", "用户服务暂不可用，请稍后重试");
            }

            @Override
            public Result<List<UserFeignResp>> batchGetUsers(List<Long> userIds) {
                return Result.success(Collections.emptyList());
            }
        };
    }
}
