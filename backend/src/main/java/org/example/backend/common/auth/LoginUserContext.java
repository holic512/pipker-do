package org.example.backend.common.auth;

import cn.dev33.satoken.stp.StpUtil;
import org.example.backend.util.satoken.StpKit;

public final class LoginUserContext {

    private LoginUserContext() {
    }

    public static Long requireUserId() {
        return StpKit.USER.getLoginIdAsLong();
    }

    public static String currentToken() {
        return StpUtil.getTokenValue();
    }
}
