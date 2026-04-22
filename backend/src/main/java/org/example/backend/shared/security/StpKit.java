package org.example.backend.shared.security;

import cn.dev33.satoken.stp.StpLogic;

/**
 * StpLogic 门面类，管理项目中账号体系
 */
public class StpKit {

    /**
     * User 会话对象，管理用户表所有账号的登录、权限认证
     */
    public static final StpLogic USER = new StpLogic("user");

}
