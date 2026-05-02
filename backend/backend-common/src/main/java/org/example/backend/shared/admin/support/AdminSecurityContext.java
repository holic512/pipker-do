package org.example.backend.shared.admin.support;

import org.example.backend.shared.security.StpKit;

public final class AdminSecurityContext {

    private AdminSecurityContext() {
    }

    public static Long requireAdminId() {
        return StpKit.ADMIN.getLoginIdAsLong();
    }
}
