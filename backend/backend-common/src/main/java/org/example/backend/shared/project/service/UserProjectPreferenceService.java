package org.example.backend.shared.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.project.dto.UserDefaultProjectResponse;
import org.example.backend.shared.project.entity.AppUserProjectPreference;
import org.example.backend.shared.project.mapper.AppUserProjectPreferenceMapper;
import org.example.backend.shared.project.support.ProjectCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserProjectPreferenceService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppUserProjectPreferenceMapper appUserProjectPreferenceMapper;

    public UserProjectPreferenceService(AppUserProjectPreferenceMapper appUserProjectPreferenceMapper) {
        this.appUserProjectPreferenceMapper = appUserProjectPreferenceMapper;
    }

    public UserDefaultProjectResponse getDefaultProject(Long userId) {
        AppUserProjectPreference preference = findPreference(userId);
        String projectCode = preference == null ? ProjectCatalog.DEFAULT_PROJECT_CODE : preference.getDefaultProjectCode();
        return buildResponse(projectCode, preference == null ? null : preference.getLastVisitAt());
    }

    @Transactional
    public UserDefaultProjectResponse updateDefaultProject(Long userId, String projectCode) {
        String normalizedProjectCode = normalizeProjectCode(projectCode);
        LocalDateTime now = LocalDateTime.now();
        AppUserProjectPreference preference = findPreference(userId);
        if (preference == null) {
            preference = new AppUserProjectPreference();
            preference.setUserId(userId);
            preference.setDefaultProjectCode(normalizedProjectCode);
            preference.setLastVisitAt(now);
            appUserProjectPreferenceMapper.insert(preference);
            return buildResponse(normalizedProjectCode, now);
        }

        appUserProjectPreferenceMapper.update(null, new LambdaUpdateWrapper<AppUserProjectPreference>()
                .eq(AppUserProjectPreference::getUserId, userId)
                .set(AppUserProjectPreference::getDefaultProjectCode, normalizedProjectCode)
                .set(AppUserProjectPreference::getLastVisitAt, now));
        return buildResponse(normalizedProjectCode, now);
    }

    private AppUserProjectPreference findPreference(Long userId) {
        return appUserProjectPreferenceMapper.selectOne(new LambdaQueryWrapper<AppUserProjectPreference>()
                .eq(AppUserProjectPreference::getUserId, userId)
                .last("limit 1"));
    }

    private String normalizeProjectCode(String projectCode) {
        String normalizedProjectCode = projectCode == null ? "" : projectCode.trim();
        if (!ProjectCatalog.supports(normalizedProjectCode)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "项目编码不支持");
        }
        return normalizedProjectCode;
    }

    private UserDefaultProjectResponse buildResponse(String projectCode, LocalDateTime lastVisitAt) {
        ProjectCatalog.ProjectDescriptor project = ProjectCatalog.getOrDefault(projectCode);
        return new UserDefaultProjectResponse(
                project.code(),
                project.name(),
                lastVisitAt == null ? null : lastVisitAt.format(DATE_TIME_FORMATTER)
        );
    }
}
