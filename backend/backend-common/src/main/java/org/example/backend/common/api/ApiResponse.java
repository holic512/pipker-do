package org.example.backend.common.api;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final String requestId;
    private final String timestamp;

    private ApiResponse(int code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = OffsetDateTime.now().toString();
    }

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(0, "OK", data, requestId);
    }

    public static <T> ApiResponse<T> success(String message, T data, String requestId) {
        return new ApiResponse<>(0, message, data, requestId);
    }

    public static <T> ApiResponse<T> failure(int code, String message, T data, String requestId) {
        return new ApiResponse<>(code, message, data, requestId);
    }
}
