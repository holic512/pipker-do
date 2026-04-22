package org.example.backend.auth.service;

import org.example.backend.auth.dto.LoginResponse;
import org.example.backend.auth.dto.WechatCode2SessionResponse;
import org.example.backend.common.entiy.AppUser;
import org.example.backend.user.dto.CurrentUserResponse;
import org.example.backend.user.service.UserProfileService;
import org.example.backend.util.satoken.StpKit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final WechatAuthClient wechatAuthClient;
    private final UserProfileService userProfileService;

    public AuthService(WechatAuthClient wechatAuthClient, UserProfileService userProfileService) {
        this.wechatAuthClient = wechatAuthClient;
        this.userProfileService = userProfileService;
    }

    @Transactional
    public LoginResponse loginByWechatCode(String code) {
        WechatCode2SessionResponse session = wechatAuthClient.code2Session(code);
        UserProfileService.UpsertUserResult result = userProfileService.createOrUpdateWechatUser(
                session.getOpenid(),
                session.getUnionid(),
                LocalDateTime.now()
        );
        AppUser user = result.user();
        StpKit.USER.login(user.getId());
        String token = StpKit.USER.getTokenValue();
        CurrentUserResponse currentUser = userProfileService.buildCurrentUser(user.getId());
        return new LoginResponse(token, result.isNewUser(), currentUser);
    }

    public void logout() {
        StpKit.USER.logout();
    }
}
