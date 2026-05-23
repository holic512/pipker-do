package org.example.backend.shared.security;

import cn.dev33.satoken.stp.StpUtil;
import org.example.backend.shared.security.StpKit;

public final class LoginUserContext {

    private LoginUserContext() {
    }

    public static Long requireUserId() {
        return StpKit.USER.getLoginIdAsLong();
    }

    public static Long currentUserIdOrNull() {
        if (!StpKit.USER.isLogin()) {
            return null;
        }
        return StpKit.USER.getLoginIdAsLong();
    }

    public static String currentToken() {
        return StpUtil.getTokenValue();
    }
}
