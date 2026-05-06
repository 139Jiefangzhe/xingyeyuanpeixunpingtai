package com.playedu.common.domain.result;

import lombok.Data;

@Data
public class Result<T> {
    private String code;

    private String msg;

    private T data;

    private String version;

    private long timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode("0");
        result.setMsg("success");
        result.setData(data);
        result.setVersion("v1");
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        result.setVersion("v1");
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
