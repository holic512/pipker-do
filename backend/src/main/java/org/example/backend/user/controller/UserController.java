package org.example.backend.user.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.auth.LoginUserContext;
import org.example.backend.user.dto.CurrentUserResponse;
import org.example.backend.user.dto.UpdateProfileRequest;
import org.example.backend.user.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserProfileService userProfileService;
    private final ApiResponseFactory responseFactory;

    public UserController(UserProfileService userProfileService, ApiResponseFactory responseFactory) {
        this.userProfileService = userProfileService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return responseFactory.success(userProfileService.buildCurrentUser(LoginUserContext.requireUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<CurrentUserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return responseFactory.success(userProfileService.updateProfile(LoginUserContext.requireUserId(), request));
    }
}
