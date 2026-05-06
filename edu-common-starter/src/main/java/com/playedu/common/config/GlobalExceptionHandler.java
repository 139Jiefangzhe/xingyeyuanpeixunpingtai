package com.playedu.common.config;

import com.playedu.common.domain.result.Result;
import com.playedu.common.exception.BizException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public Result<Void> handleBadRequest(Exception ex) {
        return Result.error("400", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return Result.error("400", extractBindingMessage(ex));
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        return Result.error("400", extractBindingMessage(ex));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        return Result.error("500", "系统异常");
    }

    private String extractBindingMessage(BindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(item -> item.getDefaultMessage() == null ? item.getField() + "参数错误" : item.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}
