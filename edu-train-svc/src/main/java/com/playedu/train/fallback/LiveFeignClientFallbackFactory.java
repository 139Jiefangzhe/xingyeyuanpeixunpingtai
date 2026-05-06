package com.playedu.train.fallback;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.LiveRoomFeignResp;
import com.playedu.train.feign.LiveFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LiveFeignClientFallbackFactory implements FallbackFactory<LiveFeignClient> {
    @Override
    public LiveFeignClient create(Throwable cause) {
        log.error("edu-live-svc 调用失败: {}", cause.getMessage(), cause);
        return new LiveFeignClient() {
            @Override
            public Result<LiveRoomFeignResp> getRoomById(String roomId) {
                return Result.error("LIVE_SVC_UNAVAILABLE", "直播服务暂不可用，请稍后重试");
            }
        };
    }
}
