package org.example.backend.shared.project.controller;

import org.example.backend.common.api.ApiResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.ApiResponseFactory;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.project.dto.UserDefaultProjectResponse;
import org.example.backend.shared.project.dto.UserDefaultProjectUpdateRequest;
import org.example.backend.shared.project.service.UserProjectPreferenceService;
import org.example.backend.shared.security.LoginUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/default-project")
public class UserProjectPreferenceController {

    private final UserProjectPreferenceService userProjectPreferenceService;
    private final ApiResponseFactory responseFactory;

    public UserProjectPreferenceController(UserProjectPreferenceService userProjectPreferenceService,
                                           ApiResponseFactory responseFactory) {
        this.userProjectPreferenceService = userProjectPreferenceService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ApiResponse<UserDefaultProjectResponse> getDefaultProject() {
        return responseFactory.success(userProjectPreferenceService.getDefaultProject(LoginUserContext.requireUserId()));
    }

    @PutMapping
    public ApiResponse<UserDefaultProjectResponse> updateDefaultProject(@RequestBody UserDefaultProjectUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "默认项目参数不能为空");
        }
        return responseFactory.success(userProjectPreferenceService.updateDefaultProject(
                LoginUserContext.requireUserId(),
                request.getProjectCode()
        ));
    }
}
