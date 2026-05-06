package com.playedu.train.fallback;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.CourseFeignResp;
import com.playedu.train.feign.CourseFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CourseFeignClientFallbackFactory implements FallbackFactory<CourseFeignClient> {
    @Override
    public CourseFeignClient create(Throwable cause) {
        log.error("edu-course-svc 调用失败: {}", cause.getMessage(), cause);
        return new CourseFeignClient() {
            @Override
            public Result<CourseFeignResp> getCourseById(String courseId) {
                return Result.error("COURSE_SVC_UNAVAILABLE", "课程服务暂不可用，请稍后重试");
            }
        };
    }
}
