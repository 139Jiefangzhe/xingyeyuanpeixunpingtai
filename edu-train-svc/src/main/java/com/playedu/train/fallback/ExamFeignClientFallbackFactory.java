package com.playedu.train.fallback;

import com.playedu.common.domain.result.Result;
import com.playedu.train.dto.resp.ExamPaperFeignResp;
import com.playedu.train.dto.resp.ExamPaperStatsFeignResp;
import com.playedu.train.feign.ExamFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExamFeignClientFallbackFactory implements FallbackFactory<ExamFeignClient> {
    @Override
    public ExamFeignClient create(Throwable cause) {
        log.error("edu-exam-svc 调用失败: {}", cause.getMessage(), cause);
        return new ExamFeignClient() {
            @Override
            public Result<ExamPaperFeignResp> getPaperById(String paperId) {
                return Result.error("EXAM_SVC_UNAVAILABLE", "考试服务暂不可用，请稍后重试");
            }

            @Override
            public Result<ExamPaperStatsFeignResp> getPaperStats(String paperId) {
                return Result.error("EXAM_SVC_UNAVAILABLE", "考试服务暂不可用，请稍后重试");
            }
        };
    }
}
