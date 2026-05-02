package org.example.backend.common.api;

public final class ApiResponseCode {

    public static final int SUCCESS = 0;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int RATE_LIMITED = 429;
    public static final int RISK_BLOCKED = 430;
    public static final int WECHAT_LOGIN_FAILED = 4101;
    public static final int BUSINESS_ERROR = 422;
    public static final int INTERNAL_ERROR = 500;

    private ApiResponseCode() {
    }
}
