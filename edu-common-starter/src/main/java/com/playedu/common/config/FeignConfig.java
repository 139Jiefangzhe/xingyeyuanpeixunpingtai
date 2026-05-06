package com.playedu.common.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration(proxyBeanMethods = false)
public class FeignConfig {
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            HttpServletRequest request = currentRequest();
            String traceId = StringUtils.hasText(MDC.get("traceId")) ? MDC.get("traceId") : headerValue(request, "X-Trace-Id");
            if (StringUtils.hasText(traceId)) {
                requestTemplate.header("X-Trace-Id", traceId);
            }
            copyHeaderIfPresent(requestTemplate, request, "X-User-Id");
            copyHeaderIfPresent(requestTemplate, request, "X-Dept-Id");
            copyHeaderIfPresent(requestTemplate, request, "Authorization");
        };
    }

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(10, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true);
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 2);
    }

    private void copyHeaderIfPresent(RequestTemplate requestTemplate, HttpServletRequest request, String headerName) {
        String value = headerValue(request, headerName);
        if (StringUtils.hasText(value)) {
            requestTemplate.header(headerName, value);
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String headerValue(HttpServletRequest request, String headerName) {
        return request == null ? null : request.getHeader(headerName);
    }
}
