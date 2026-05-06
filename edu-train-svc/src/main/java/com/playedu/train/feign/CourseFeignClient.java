package com.playedu.train.feign;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.CourseFeignResp;
import com.playedu.train.fallback.CourseFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "edu-course-svc",
        contextId = "courseFeignClient",
        fallbackFactory = CourseFeignClientFallbackFactory.class)
public interface CourseFeignClient {
    @GetMapping("/api/v1/courses/{courseId}")
    Result<CourseFeignResp> getCourseById(@PathVariable("courseId") String courseId);
}
