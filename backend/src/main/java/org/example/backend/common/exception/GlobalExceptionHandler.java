package org.example.backend.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.security.anticrawler.AntiCrawlerViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiResponseFactory responseFactory;

    public GlobalExceptionHandler(ApiResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(AntiCrawlerViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAntiCrawlerViolation(AntiCrawlerViolationException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(responseFactory.failure(ex.getResponseCode(), ex.getMessage(), ex.getResponsePayload()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(responseFactory.failure(ex.getCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotLogin(NotLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(responseFactory.failure(ApiResponseCode.UNAUTHORIZED, "未登录或登录态已失效", null));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoPermission(NotPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(responseFactory.failure(ApiResponseCode.FORBIDDEN, "暂无权限访问该资源", null));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMediaTypeNotSupportedException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(responseFactory.failure(ApiResponseCode.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleInternal(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseFactory.failure(ApiResponseCode.INTERNAL_ERROR, "系统繁忙，请稍后重试", null));
    }
}
