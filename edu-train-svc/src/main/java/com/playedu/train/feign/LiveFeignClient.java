package com.playedu.train.feign;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.LiveRoomFeignResp;
import com.playedu.train.fallback.LiveFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "edu-live-svc",
        contextId = "liveFeignClient",
        fallbackFactory = LiveFeignClientFallbackFactory.class)
public interface LiveFeignClient {
    @GetMapping("/api/v1/live-rooms/{roomId}")
    Result<LiveRoomFeignResp> getRoomById(@PathVariable("roomId") String roomId);
}
