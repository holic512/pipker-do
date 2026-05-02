package org.example.backend.shared.account.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.shared.account.dto.VipInfoResponse;
import org.example.backend.shared.account.dto.vip.AdminVipRecordResponse;
import org.example.backend.shared.account.dto.vip.VipRedeemRequest;
import org.example.backend.shared.account.service.UserProfileService;
import org.example.backend.shared.account.service.VipManagementService;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 索引: 用户端 VIP 兑换预留接口。
 */
@RestController
@RequestMapping("/api/user/vip")
public class UserVipController {

    private final VipManagementService vipManagementService;
    private final UserProfileService userProfileService;
    private final ApiResponseFactory responseFactory;

    public UserVipController(VipManagementService vipManagementService,
                             UserProfileService userProfileService,
                             ApiResponseFactory responseFactory) {
        this.vipManagementService = vipManagementService;
        this.userProfileService = userProfileService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/status")
    public ApiResponse<VipInfoResponse> status() {
        return responseFactory.success(userProfileService.getVipInfo(LoginUserContext.requireUserId()));
    }

    @PostMapping("/redeem")
    public ApiResponse<AdminVipRecordResponse> redeem(@RequestBody VipRedeemRequest request) {
        return responseFactory.success(vipManagementService.redeemVip(LoginUserContext.requireUserId(), request));
    }
}
