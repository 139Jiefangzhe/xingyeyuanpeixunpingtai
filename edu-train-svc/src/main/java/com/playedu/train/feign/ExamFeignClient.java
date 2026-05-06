package com.playedu.train.feign;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.ExamPaperFeignResp;
import com.playedu.train.dto.resp.ExamPaperStatsFeignResp;
import com.playedu.train.fallback.ExamFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "edu-exam-svc",
        contextId = "examFeignClient",
        fallbackFactory = ExamFeignClientFallbackFactory.class)
public interface ExamFeignClient {
    @GetMapping("/api/v1/exam-papers/{paperId}")
    Result<ExamPaperFeignResp> getPaperById(@PathVariable("paperId") String paperId);

    @GetMapping("/api/v1/exam-records/papers/{paperId}/stats")
    Result<ExamPaperStatsFeignResp> getPaperStats(@PathVariable("paperId") String paperId);
}
