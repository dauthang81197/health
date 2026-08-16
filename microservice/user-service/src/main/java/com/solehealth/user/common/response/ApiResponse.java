package com.solehealth.user.common.response;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, String> errors;
    private Instant timestamp = Instant.now();
    private int code;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static ApiResponse build(int code, boolean status ,String errors, Object data)
    {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(code);
        apiResponse.setData(data);
        apiResponse.setMessage(errors);
        return apiResponse;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
