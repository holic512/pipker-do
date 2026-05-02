package org.example.backend.common.api;

import org.example.backend.common.web.RequestIdHolder;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseFactory {

    public <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data, RequestIdHolder.getRequestId());
    }

    public <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data, RequestIdHolder.getRequestId());
    }

    public <T> ApiResponse<T> failure(int code, String message, T data) {
        return ApiResponse.failure(code, message, data, RequestIdHolder.getRequestId());
    }
}
