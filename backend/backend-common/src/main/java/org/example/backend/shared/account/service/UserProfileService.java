/**
 * @file UserProfileService
 * @project pipker-do
 * @module 用户中心 / 微信自动注册
 * @description 维护小程序用户资料、自动注册、登录时间与会员资料聚合。
 * @logic 1. 按微信 openid 查询用户；2. 自动注册时生成带时间戳的默认用户名；3. openid 唯一冲突后回查已有用户并更新登录信息。
 * @dependencies Mapper: AppUserMapper, AppUserVipMapper; Service: LocalFileStorage
 * @index_tags 微信登录, 自动注册, openid唯一索引, 用户资料, 默认用户名
 * @author holic512
 */
package org.example.backend.shared.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.shared.account.entity.AppUser;
import org.example.backend.shared.account.entity.AppUserVip;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.account.mapper.AppUserMapper;
import org.example.backend.shared.account.mapper.AppUserVipMapper;
import org.example.backend.shared.account.dto.CurrentUserResponse;
import org.example.backend.shared.account.dto.UpdateProfileRequest;
import org.example.backend.shared.account.dto.VipInfoResponse;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserProfileService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DEFAULT_USERNAME_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final String CURRENT_AGREEMENT_VERSION = "2026-04-25";

    private final AppUserMapper appUserMapper;
    private final AppUserVipMapper appUserVipMapper;
    private final LocalFileStorage localFileStorage;

    public UserProfileService(AppUserMapper appUserMapper, AppUserVipMapper appUserVipMapper, LocalFileStorage localFileStorage) {
        this.appUserMapper = appUserMapper;
        this.appUserVipMapper = appUserVipMapper;
        this.localFileStorage = localFileStorage;
    }

    @Transactional
    public UpsertUserResult createOrUpdateWechatUser(String openId, String unionId, LocalDateTime now) {
        AppUser existing = findByWechatOpenid(openId);
        if (existing == null) {
            AppUser user = new AppUser();
            user.setWechatOpenid(openId);
            user.setWechatUnionid(unionId);
            user.setUsername(buildDefaultUsername(openId, now));
            user.setNickname(buildDefaultNickname(openId));
            user.setGender(0);
            user.setStatus(1);
            user.setLastLoginAt(now);
            try {
                appUserMapper.insert(user);
            } catch (DuplicateKeyException ex) {
                AppUser duplicatedOpenidUser = findByWechatOpenid(openId);
                if (duplicatedOpenidUser == null) {
                    throw ex;
                }
                return updateExistingWechatUser(duplicatedOpenidUser, unionId, now);
            }
            return new UpsertUserResult(user, true);
        }

        return updateExistingWechatUser(existing, unionId, now);
    }

    private UpsertUserResult updateExistingWechatUser(AppUser existing, String unionId, LocalDateTime now) {
        if (existing.getStatus() == null || existing.getStatus() != 1) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前账号已被禁用");
        }

        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, existing.getId())
                .set(AppUser::getWechatUnionid, StringUtils.hasText(unionId) ? unionId : existing.getWechatUnionid())
                .set(AppUser::getLastLoginAt, now));
        existing.setWechatUnionid(StringUtils.hasText(unionId) ? unionId : existing.getWechatUnionid());
        existing.setLastLoginAt(now);
        return new UpsertUserResult(existing, false);
    }

    private AppUser findByWechatOpenid(String openId) {
        return appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getWechatOpenid, openId)
                .last("limit 1"));
    }

    public CurrentUserResponse buildCurrentUser(Long userId) {
        AppUser user = requireUser(userId);
        VipInfoResponse vipInfo = getVipInfo(userId);
        return new CurrentUserResponse(
                user.getId(),
                user.getNickname(),
                resolveAvatarUrl(user.getAvatarUrl()),
                user.getGender() == null ? 0 : user.getGender(),
                user.getBio(),
                vipInfo,
                StringUtils.hasText(user.getNickname()) && StringUtils.hasText(resolveAvatarUrl(user.getAvatarUrl())),
                user.getAgreementVersion(),
                formatDateTime(user.getAgreementAcceptedAt()),
                isCurrentAgreementAccepted(user)
        );
    }

    @Transactional
    public CurrentUserResponse acceptCurrentAgreement(Long userId) {
        AppUser user = requireUser(userId);
        LocalDateTime now = LocalDateTime.now();
        user.setAgreementVersion(CURRENT_AGREEMENT_VERSION);
        user.setAgreementAcceptedAt(now);
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, user.getId())
                .set(AppUser::getAgreementVersion, CURRENT_AGREEMENT_VERSION)
                .set(AppUser::getAgreementAcceptedAt, now));
        return buildCurrentUser(userId);
    }

    @Transactional
    public CurrentUserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = requireUser(userId);
        String nickname = normalizeNickname(request.getNickname());
        String bio = normalizeText(request.getBio(), 255);
        Integer gender = normalizeGender(request.getGender());
        String avatarUrl = normalizeAvatarUrl(request.getAvatarUrl());

        String oldAvatarUrl = user.getAvatarUrl();
        user.setNickname(nickname);
        user.setBio(bio);
        user.setGender(gender);
        user.setAvatarUrl(avatarUrl);
        appUserMapper.updateById(user);

        if (StringUtils.hasText(oldAvatarUrl)
                && !oldAvatarUrl.equals(avatarUrl)
                && localFileStorage.isManagedKey(oldAvatarUrl)) {
            localFileStorage.deleteByKey(oldAvatarUrl);
        }
        return buildCurrentUser(userId);
    }

    private AppUser requireUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public VipInfoResponse getVipInfo(Long userId) {
        requireUser(userId);
        AppUserVip vip = appUserVipMapper.selectOne(new LambdaQueryWrapper<AppUserVip>()
                .eq(AppUserVip::getUserId, userId)
                .eq(AppUserVip::getVipStatus, 1)
                .ge(AppUserVip::getEndTime, LocalDateTime.now())
                .orderByDesc(AppUserVip::getEndTime)
                .last("limit 1"));
        if (vip == null) {
            return new VipInfoResponse(false, null, null);
        }
        return new VipInfoResponse(true, vip.getVipType(), vip.getEndTime().format(DATE_FORMATTER));
    }

    private boolean isCurrentAgreementAccepted(AppUser user) {
        return CURRENT_AGREEMENT_VERSION.equals(user.getAgreementVersion()) && user.getAgreementAcceptedAt() != null;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    private String buildDefaultUsername(String openId, LocalDateTime now) {
        return "wx_" + now.format(DEFAULT_USERNAME_TIME_FORMATTER)
                + "_" + openId.substring(Math.max(0, openId.length() - 12));
    }

    private String buildDefaultNickname(String openId) {
        return "微信用户" + openId.substring(Math.max(0, openId.length() - 4));
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "昵称不能为空");
        }
        String value = nickname.trim();
        if (value.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "昵称不能为空");
        }
        if (value.length() > 20) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "昵称长度不能超过20个字符");
        }
        return value;
    }

    private String normalizeText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String value = text.trim();
        if (value.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "内容长度不能超过" + maxLength + "个字符");
        }
        return value;
    }

    private Integer normalizeGender(Integer gender) {
        if (gender == null) {
            return 0;
        }
        if (gender < 0 || gender > 2) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "性别参数非法");
        }
        return gender;
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return null;
        }
        return avatarUrl.trim();
    }

    private String resolveAvatarUrl(String avatarValue) {
        if (!StringUtils.hasText(avatarValue)) {
            return null;
        }
        if (localFileStorage.isManagedKey(avatarValue)) {
            return localFileStorage.resolveUrl(avatarValue);
        }
        return avatarValue;
    }

    public record UpsertUserResult(AppUser user, boolean isNewUser) {
    }
}
