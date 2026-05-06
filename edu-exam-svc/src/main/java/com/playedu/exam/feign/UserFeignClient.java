package com.playedu.exam.feign;

import com.playedu.common.config.FeignConfig;
import com.playedu.common.domain.result.Result;
import com.playedu.exam.dto.resp.UserFeignResp;
import com.playedu.exam.fallback.UserFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "edu-user-svc",
        contextId = "userFeignClient",
        configuration = FeignConfig.class,
        fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {
    @GetMapping("/api/v1/users/{userId}")
    Result<UserFeignResp> getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/users/batch")
    Result<java.util.List<UserFeignResp>> batchGetUsers(@RequestBody java.util.List<Long> userIds);
}
