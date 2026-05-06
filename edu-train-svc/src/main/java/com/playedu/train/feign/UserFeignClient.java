package com.playedu.train.feign;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.UserFeignResp;
import com.playedu.train.fallback.UserFeignClientFallbackFactory;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "edu-user-svc",
        contextId = "userFeignClient",
        fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {
    @GetMapping("/api/v1/users/{userId}")
    Result<UserFeignResp> getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/users/batch")
    Result<List<UserFeignResp>> batchGetUsers(@RequestBody List<Long> ids);
}
