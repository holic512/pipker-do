package org.example.backend.shared.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.account.dto.vip.AdminVipRecordResponse;
import org.example.backend.shared.account.dto.vip.AdminVipUserResponse;
import org.example.backend.shared.account.dto.vip.VipCardGroupResponse;
import org.example.backend.shared.account.dto.vip.VipCardGroupStatusUpdateRequest;
import org.example.backend.shared.account.dto.vip.VipCardGroupUpsertRequest;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchCreateRequest;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchCreateResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyBatchVoidResponse;
import org.example.backend.shared.account.dto.vip.VipCardKeyResponse;
import org.example.backend.shared.account.dto.vip.VipManualGrantRequest;
import org.example.backend.shared.account.dto.vip.VipRedeemRequest;
import org.example.backend.shared.account.dto.vip.VipVoidRequest;
import org.example.backend.shared.account.entity.AppUser;
import org.example.backend.shared.account.entity.AppUserVip;
import org.example.backend.shared.account.entity.VipCardGroup;
import org.example.backend.shared.account.entity.VipCardKey;
import org.example.backend.shared.account.mapper.AppUserMapper;
import org.example.backend.shared.account.mapper.AppUserVipMapper;
import org.example.backend.shared.account.mapper.VipCardGroupMapper;
import org.example.backend.shared.account.mapper.VipCardKeyMapper;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: 后台 VIP 卡组、兑换 Key、用户会员记录管理服务。
 */
@Service
public class VipManagementService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter BATCH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String KEY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;
    private static final int KEY_STATUS_VOIDED = 0;
    private static final int KEY_STATUS_UNUSED = 1;
    private static final int KEY_STATUS_REDEEMED = 2;
    private static final int VIP_STATUS_INVALID = 0;
    private static final int VIP_STATUS_ACTIVE = 1;
    private static final LocalDateTime LIFETIME_END_TIME = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

    private final VipCardGroupMapper vipCardGroupMapper;
    private final VipCardKeyMapper vipCardKeyMapper;
    private final AppUserMapper appUserMapper;
    private final AppUserVipMapper appUserVipMapper;
    private final AdminUserMapper adminUserMapper;

    public VipManagementService(VipCardGroupMapper vipCardGroupMapper,
                                VipCardKeyMapper vipCardKeyMapper,
                                AppUserMapper appUserMapper,
                                AppUserVipMapper appUserVipMapper,
                                AdminUserMapper adminUserMapper) {
        this.vipCardGroupMapper = vipCardGroupMapper;
        this.vipCardKeyMapper = vipCardKeyMapper;
        this.appUserMapper = appUserMapper;
        this.appUserVipMapper = appUserVipMapper;
        this.adminUserMapper = adminUserMapper;
    }

    public List<VipCardGroupResponse> listCardGroups(Long operatorId, String keyword, Integer status) {
        requireAdmin(operatorId);
        LambdaQueryWrapper<VipCardGroup> query = new LambdaQueryWrapper<VipCardGroup>()
                .orderByDesc(VipCardGroup::getCreatedAt);
        String normalizedKeyword = normalizeNullable(keyword, 80);
        if (StringUtils.hasText(normalizedKeyword)) {
            query.and(wrapper -> wrapper.like(VipCardGroup::getGroupName, normalizedKeyword)
                    .or().like(VipCardGroup::getRemark, normalizedKeyword));
        }
        if (status != null) {
            validateBinaryStatus(status, "卡组状态");
            query.eq(VipCardGroup::getStatus, status);
        }
        List<VipCardGroup> groups = vipCardGroupMapper.selectList(query);
        GroupKeyStats stats = loadGroupKeyStats(groups.stream().map(VipCardGroup::getId).toList());
        Map<Long, String> adminNames = loadAdminNames(groups.stream().map(VipCardGroup::getCreatedBy).toList());
        return groups.stream().map(group -> toGroupResponse(group, stats, adminNames)).toList();
    }

    @Transactional
    public VipCardGroupResponse createCardGroup(Long operatorId, VipCardGroupUpsertRequest request) {
        requireAdmin(operatorId);
        NormalizedGroupPayload payload = normalizeGroupPayload(request, true);
        VipCardGroup group = new VipCardGroup();
        group.setGroupName(payload.groupName());
        group.setVipType(payload.vipType());
        group.setDurationDays(payload.durationDays());
        group.setStatus(payload.status());
        group.setRemark(payload.remark());
        group.setCreatedBy(operatorId);
        vipCardGroupMapper.insert(group);
        return requireGroupResponse(group.getId());
    }

    @Transactional
    public VipCardGroupResponse updateCardGroup(Long operatorId, Long groupId, VipCardGroupUpsertRequest request) {
        requireAdmin(operatorId);
        requireGroup(groupId);
        NormalizedGroupPayload payload = normalizeGroupPayload(request, false);
        vipCardGroupMapper.update(null, new LambdaUpdateWrapper<VipCardGroup>()
                .eq(VipCardGroup::getId, groupId)
                .set(VipCardGroup::getGroupName, payload.groupName())
                .set(VipCardGroup::getVipType, payload.vipType())
                .set(VipCardGroup::getDurationDays, payload.durationDays())
                .set(VipCardGroup::getStatus, payload.status())
                .set(VipCardGroup::getRemark, payload.remark()));
        return requireGroupResponse(groupId);
    }

    @Transactional
    public VipCardGroupResponse updateCardGroupStatus(Long operatorId, Long groupId, VipCardGroupStatusUpdateRequest request) {
        requireAdmin(operatorId);
        if (request == null || request.getStatus() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "卡组状态不能为空");
        }
        validateBinaryStatus(request.getStatus(), "卡组状态");
        requireGroup(groupId);
        vipCardGroupMapper.update(null, new LambdaUpdateWrapper<VipCardGroup>()
                .eq(VipCardGroup::getId, groupId)
                .set(VipCardGroup::getStatus, request.getStatus()));
        return requireGroupResponse(groupId);
    }

    @Transactional
    public VipCardKeyBatchCreateResponse batchCreateKeys(Long operatorId, Long groupId, VipCardKeyBatchCreateRequest request) {
        requireAdmin(operatorId);
        VipCardGroup group = requireGroup(groupId);
        if (!Objects.equals(group.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前卡组已停用，不能生成兑换Key");
        }
        int count = request == null || request.getCount() == null ? 0 : request.getCount();
        if (count < 1 || count > 500) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单次生成数量需保持在 1 到 500 之间");
        }
        String batchNo = generateBatchNo();
        LinkedHashSet<Long> insertedIds = new LinkedHashSet<>();
        for (int index = 0; index < count; index++) {
            VipCardKey cardKey = new VipCardKey();
            cardKey.setGroupId(groupId);
            cardKey.setCardKey(generateUniqueKey());
            cardKey.setBatchNo(batchNo);
            cardKey.setStatus(KEY_STATUS_UNUSED);
            cardKey.setCreatedBy(operatorId);
            vipCardKeyMapper.insert(cardKey);
            insertedIds.add(cardKey.getId());
        }
        List<VipCardKeyResponse> keys = listKeysByIds(insertedIds);
        return new VipCardKeyBatchCreateResponse(groupId, batchNo, keys.size(), keys);
    }

    public List<VipCardKeyResponse> listCardKeys(Long operatorId, Long groupId, Integer status, Long userId, String keyword, String batchNo) {
        requireAdmin(operatorId);
        LambdaQueryWrapper<VipCardKey> query = new LambdaQueryWrapper<VipCardKey>().orderByDesc(VipCardKey::getCreatedAt);
        if (groupId != null) {
            query.eq(VipCardKey::getGroupId, groupId);
        }
        if (status != null) {
            validateKeyStatus(status);
            query.eq(VipCardKey::getStatus, status);
        }
        if (userId != null) {
            query.eq(VipCardKey::getRedeemedUserId, userId);
        }
        String normalizedBatchNo = normalizeNullable(batchNo, 40);
        if (StringUtils.hasText(normalizedBatchNo)) {
            query.eq(VipCardKey::getBatchNo, normalizedBatchNo);
        }
        String normalizedKeyword = normalizeNullable(keyword, 80);
        if (StringUtils.hasText(normalizedKeyword)) {
            query.like(VipCardKey::getCardKey, normalizedKeyword.toUpperCase(Locale.ROOT));
        }
        return toKeyResponses(vipCardKeyMapper.selectList(query));
    }

    public List<VipCardKeyBatchResponse> listCardKeyBatches(Long operatorId, Long groupId) {
        requireAdmin(operatorId);
        requireGroup(groupId);
        List<Map<String, Object>> rows = vipCardKeyMapper.selectMaps(new QueryWrapper<VipCardKey>()
                .select("batch_no AS batchNo", "MIN(created_at) AS createdAt", "COUNT(*) AS totalCount",
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS unusedCount",
                        "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS redeemedCount",
                        "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS voidedCount")
                .eq("group_id", groupId)
                .isNotNull("batch_no")
                .groupBy("batch_no")
                .orderByDesc("createdAt"));
        return rows.stream()
                .map(row -> new VipCardKeyBatchResponse(
                        String.valueOf(row.get("batchNo")),
                        groupId,
                        asInteger(row.get("totalCount")),
                        asInteger(row.get("unusedCount")),
                        asInteger(row.get("redeemedCount")),
                        asInteger(row.get("voidedCount")),
                        formatSqlTime(row.get("createdAt"))
                ))
                .toList();
    }

    @Transactional
    public VipCardKeyBatchVoidResponse voidCardKeyBatch(Long operatorId, Long groupId, String batchNo, VipVoidRequest request) {
        requireAdmin(operatorId);
        requireGroup(groupId);
        String normalizedBatchNo = normalizeRequired(batchNo, "批次号", 40);
        String reason = normalizeReason(request == null ? null : request.getReason());
        long totalCount = vipCardKeyMapper.selectCount(new LambdaQueryWrapper<VipCardKey>()
                .eq(VipCardKey::getGroupId, groupId)
                .eq(VipCardKey::getBatchNo, normalizedBatchNo));
        if (totalCount == 0) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "批次不存在");
        }
        int voidedCount = vipCardKeyMapper.update(null, new LambdaUpdateWrapper<VipCardKey>()
                .eq(VipCardKey::getGroupId, groupId)
                .eq(VipCardKey::getBatchNo, normalizedBatchNo)
                .eq(VipCardKey::getStatus, KEY_STATUS_UNUSED)
                .set(VipCardKey::getStatus, KEY_STATUS_VOIDED)
                .set(VipCardKey::getVoidedAt, LocalDateTime.now())
                .set(VipCardKey::getVoidReason, reason));
        return new VipCardKeyBatchVoidResponse(normalizedBatchNo, voidedCount, (int) totalCount - voidedCount);
    }

    @Transactional
    public VipCardKeyResponse voidCardKey(Long operatorId, Long keyId, VipVoidRequest request) {
        requireAdmin(operatorId);
        VipCardKey key = requireCardKey(keyId);
        if (!Objects.equals(key.getStatus(), KEY_STATUS_UNUSED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "只有未使用的兑换Key可以作废");
        }
        String reason = normalizeReason(request == null ? null : request.getReason());
        LocalDateTime now = LocalDateTime.now();
        vipCardKeyMapper.update(null, new LambdaUpdateWrapper<VipCardKey>()
                .eq(VipCardKey::getId, keyId)
                .eq(VipCardKey::getStatus, KEY_STATUS_UNUSED)
                .set(VipCardKey::getStatus, KEY_STATUS_VOIDED)
                .set(VipCardKey::getVoidedAt, now)
                .set(VipCardKey::getVoidReason, reason));
        return listKeysByIds(List.of(keyId)).get(0);
    }

    @Transactional
    public void deleteCardKey(Long operatorId, Long keyId) {
        requireAdmin(operatorId);
        VipCardKey key = requireCardKey(keyId);
        if (!Objects.equals(key.getStatus(), KEY_STATUS_UNUSED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "只有未使用的兑换Key可以删除");
        }
        vipCardKeyMapper.deleteById(keyId);
    }

    public List<AdminVipUserResponse> listVipUsers(Long operatorId, String keyword, Integer vipStatus) {
        requireAdmin(operatorId);
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<AppUser>().orderByDesc(AppUser::getCreatedAt);
        String normalizedKeyword = normalizeNullable(keyword, 80);
        if (StringUtils.hasText(normalizedKeyword)) {
            query.and(wrapper -> wrapper.like(AppUser::getNickname, normalizedKeyword)
                    .or().like(AppUser::getUsername, normalizedKeyword)
                    .or().like(AppUser::getPhone, normalizedKeyword)
                    .or().like(AppUser::getEmail, normalizedKeyword));
        }
        List<AppUser> users = appUserMapper.selectList(query);
        Map<Long, AppUserVip> activeVipByUser = loadActiveVipByUser(users.stream().map(AppUser::getId).toList());
        Map<Long, Integer> recordCountByUser = loadVipRecordCount(users.stream().map(AppUser::getId).toList());
        return users.stream()
                .map(user -> toVipUserResponse(user, activeVipByUser.get(user.getId()), recordCountByUser.getOrDefault(user.getId(), 0)))
                .filter(user -> vipStatus == null || (vipStatus == 1 && Boolean.TRUE.equals(user.getIsVip())) || (vipStatus == 0 && !Boolean.TRUE.equals(user.getIsVip())))
                .toList();
    }

    public List<AdminVipRecordResponse> listVipRecords(Long operatorId, Long userId) {
        requireAdmin(operatorId);
        requireUser(userId);
        return appUserVipMapper.selectList(new LambdaQueryWrapper<AppUserVip>()
                        .eq(AppUserVip::getUserId, userId)
                        .orderByDesc(AppUserVip::getCreatedAt))
                .stream()
                .map(this::toRecordResponse)
                .toList();
    }

    @Transactional
    public AdminVipRecordResponse grantVip(Long operatorId, Long userId, VipManualGrantRequest request) {
        requireAdmin(operatorId);
        requireUser(userId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "会员参数不能为空");
        }
        String vipType = normalizeVipType(request.getVipType());
        int durationDays = normalizeDuration(vipType, request.getDurationDays());
        AppUserVip vip = createVipRecord(userId, vipType, durationDays, "manual", null,
                request.getAmount() == null ? BigDecimal.ZERO : request.getAmount(), null);
        return toRecordResponse(vip);
    }

    @Transactional
    public AdminVipRecordResponse voidVipRecord(Long operatorId, Long userId, Long vipId, VipVoidRequest request) {
        requireAdmin(operatorId);
        requireUser(userId);
        AppUserVip vip = appUserVipMapper.selectById(vipId);
        if (vip == null || !Objects.equals(vip.getUserId(), userId)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "会员记录不存在");
        }
        if (!Objects.equals(vip.getVipStatus(), VIP_STATUS_ACTIVE)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前会员记录已失效");
        }
        String reason = normalizeReason(request == null ? null : request.getReason());
        appUserVipMapper.update(null, new LambdaUpdateWrapper<AppUserVip>()
                .eq(AppUserVip::getId, vipId)
                .eq(AppUserVip::getVipStatus, VIP_STATUS_ACTIVE)
                .set(AppUserVip::getVipStatus, VIP_STATUS_INVALID)
                .set(AppUserVip::getInvalidReason, reason)
                .set(AppUserVip::getInvalidAt, LocalDateTime.now())
                .set(AppUserVip::getInvalidBy, operatorId));
        return toRecordResponse(appUserVipMapper.selectById(vipId));
    }

    @Transactional
    public AdminVipRecordResponse redeemVip(Long userId, VipRedeemRequest request) {
        requireUser(userId);
        String cardKeyValue = normalizeCardKey(request == null ? null : request.getKey());
        VipCardKey cardKey = vipCardKeyMapper.selectOne(new LambdaQueryWrapper<VipCardKey>()
                .eq(VipCardKey::getCardKey, cardKeyValue)
                .last("limit 1"));
        if (cardKey == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "兑换Key不存在");
        }
        if (!Objects.equals(cardKey.getStatus(), KEY_STATUS_UNUSED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "兑换Key已使用或已作废");
        }
        VipCardGroup group = requireGroup(cardKey.getGroupId());
        if (!Objects.equals(group.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前卡组已停用，无法兑换");
        }
        AppUserVip vip = createVipRecord(userId, group.getVipType(), group.getDurationDays(), "card", cardKey.getId(), BigDecimal.ZERO, null);
        int updated = vipCardKeyMapper.update(null, new LambdaUpdateWrapper<VipCardKey>()
                .eq(VipCardKey::getId, cardKey.getId())
                .eq(VipCardKey::getStatus, KEY_STATUS_UNUSED)
                .set(VipCardKey::getStatus, KEY_STATUS_REDEEMED)
                .set(VipCardKey::getRedeemedUserId, userId)
                .set(VipCardKey::getRedeemedAt, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "兑换Key状态已变化，请刷新后重试");
        }
        return toRecordResponse(vip);
    }

    private AppUserVip createVipRecord(Long userId, String vipType, int durationDays, String sourceType, Long sourceRefId,
                                       BigDecimal amount, LocalDateTime customStartTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latestEndTime = findLatestActiveEndTime(userId, now);
        LocalDateTime startTime = customStartTime == null ? (latestEndTime != null && latestEndTime.isAfter(now) ? latestEndTime : now) : customStartTime;
        LocalDateTime endTime = isLifetime(vipType) ? LIFETIME_END_TIME : startTime.plusDays(durationDays);
        AppUserVip vip = new AppUserVip();
        vip.setUserId(userId);
        vip.setVipType(vipType);
        vip.setVipStatus(VIP_STATUS_ACTIVE);
        vip.setSourceType(sourceType);
        vip.setSourceRefId(sourceRefId);
        vip.setAmount(amount == null ? BigDecimal.ZERO : amount);
        vip.setStartTime(startTime);
        vip.setEndTime(endTime);
        appUserVipMapper.insert(vip);
        return vip;
    }

    private LocalDateTime findLatestActiveEndTime(Long userId, LocalDateTime now) {
        AppUserVip vip = appUserVipMapper.selectOne(new LambdaQueryWrapper<AppUserVip>()
                .eq(AppUserVip::getUserId, userId)
                .eq(AppUserVip::getVipStatus, VIP_STATUS_ACTIVE)
                .ge(AppUserVip::getEndTime, now)
                .orderByDesc(AppUserVip::getEndTime)
                .last("limit 1"));
        return vip == null ? null : vip.getEndTime();
    }

    private VipCardGroupResponse requireGroupResponse(Long groupId) {
        VipCardGroup group = requireGroup(groupId);
        GroupKeyStats stats = loadGroupKeyStats(List.of(groupId));
        Map<Long, String> adminNames = loadAdminNames(List.of(group.getCreatedBy()));
        return toGroupResponse(group, stats, adminNames);
    }

    private VipCardGroup requireGroup(Long groupId) {
        if (groupId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "卡组ID不能为空");
        }
        VipCardGroup group = vipCardGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "卡组不存在");
        }
        return group;
    }

    private VipCardKey requireCardKey(Long keyId) {
        if (keyId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "兑换Key ID不能为空");
        }
        VipCardKey key = vipCardKeyMapper.selectById(keyId);
        if (key == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "兑换Key不存在");
        }
        return key;
    }

    private AppUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "用户ID不能为空");
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void requireAdmin(Long operatorId) {
        if (operatorId == null || adminUserMapper.selectById(operatorId) == null) {
            throw new BusinessException(ApiResponseCode.UNAUTHORIZED, "管理员登录态无效");
        }
    }

    private NormalizedGroupPayload normalizeGroupPayload(VipCardGroupUpsertRequest request, boolean creating) {
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "卡组参数不能为空");
        }
        String groupName = normalizeRequired(request.getGroupName(), "卡组名称", 80);
        String vipType = normalizeVipType(request.getVipType());
        int durationDays = normalizeDuration(vipType, request.getDurationDays());
        Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
        validateBinaryStatus(status, "卡组状态");
        String remark = normalizeNullable(request.getRemark(), 255);
        return new NormalizedGroupPayload(groupName, vipType, durationDays, status, remark);
    }

    private String normalizeVipType(String vipType) {
        String value = normalizeRequired(vipType, "VIP类型", 30).toLowerCase(Locale.ROOT);
        if (!Set.of("month", "quarter", "year", "lifetime", "custom").contains(value)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "VIP类型仅支持 month/quarter/year/lifetime/custom");
        }
        return value;
    }

    private int normalizeDuration(String vipType, Integer durationDays) {
        if (isLifetime(vipType)) {
            return 0;
        }
        int value = durationDays == null ? switch (vipType) {
            case "month" -> 30;
            case "quarter" -> 90;
            case "year" -> 365;
            default -> 30;
        } : durationDays;
        if (value < 1 || value > 3650) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "会员有效天数需保持在 1 到 3650 之间");
        }
        return value;
    }

    private boolean isLifetime(String vipType) {
        return "lifetime".equalsIgnoreCase(vipType);
    }

    private String normalizeCardKey(String key) {
        String value = normalizeRequired(key, "兑换Key", 64).replace("-", "").toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z0-9]{8,64}")) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "兑换Key格式不正确");
        }
        return value;
    }

    private String normalizeReason(String reason) {
        return normalizeRequired(reason, "作废原因", 255);
    }

    private String normalizeRequired(String value, String fieldName, int maxLength) {
        String normalized = normalizeNullable(value, maxLength);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeNullable(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "内容长度不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private void validateBinaryStatus(Integer status, String fieldName) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, fieldName + "仅支持 0 或 1");
        }
    }

    private void validateKeyStatus(Integer status) {
        if (status == null || (status != KEY_STATUS_VOIDED && status != KEY_STATUS_UNUSED && status != KEY_STATUS_REDEEMED)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "兑换Key状态仅支持 0、1、2");
        }
    }

    private String generateUniqueKey() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String key = randomKey();
            Long count = vipCardKeyMapper.selectCount(new LambdaQueryWrapper<VipCardKey>().eq(VipCardKey::getCardKey, key));
            if (count == 0) {
                return key;
            }
        }
        throw new BusinessException(ApiResponseCode.CONFLICT, "兑换Key生成冲突，请重试");
    }

    private String generateBatchNo() {
        return "B" + LocalDateTime.now().format(BATCH_FORMATTER) + randomToken(4);
    }

    private String randomKey() {
        StringBuilder builder = new StringBuilder("VIP");
        for (int index = 0; index < 17; index++) {
            builder.append(KEY_ALPHABET.charAt(SECURE_RANDOM.nextInt(KEY_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String randomToken(int length) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            builder.append(KEY_ALPHABET.charAt(SECURE_RANDOM.nextInt(KEY_ALPHABET.length())));
        }
        return builder.toString();
    }

    private List<VipCardKeyResponse> listKeysByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<VipCardKey> keys = vipCardKeyMapper.selectList(new LambdaQueryWrapper<VipCardKey>()
                .in(VipCardKey::getId, ids)
                .orderByDesc(VipCardKey::getCreatedAt));
        return toKeyResponses(keys);
    }

    private List<VipCardKeyResponse> toKeyResponses(List<VipCardKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }
        Map<Long, VipCardGroup> groups = loadGroups(keys.stream().map(VipCardKey::getGroupId).toList());
        Map<Long, String> users = loadUserNames(keys.stream().map(VipCardKey::getRedeemedUserId).toList());
        Map<Long, String> admins = loadAdminNames(keys.stream().map(VipCardKey::getCreatedBy).toList());
        return keys.stream().map(key -> {
            VipCardGroup group = groups.get(key.getGroupId());
            return new VipCardKeyResponse(
                    key.getId(),
                    key.getGroupId(),
                    group == null ? null : group.getGroupName(),
                    group == null ? null : group.getVipType(),
                    group == null ? null : group.getDurationDays(),
                    key.getCardKey(),
                    key.getBatchNo(),
                    key.getStatus(),
                    key.getRedeemedUserId(),
                    getNullable(users, key.getRedeemedUserId()),
                    formatTime(key.getRedeemedAt()),
                    formatTime(key.getVoidedAt()),
                    key.getVoidReason(),
                    key.getCreatedBy(),
                    getNullable(admins, key.getCreatedBy()),
                    formatTime(key.getCreatedAt()),
                    formatTime(key.getUpdatedAt())
            );
        }).toList();
    }

    private VipCardGroupResponse toGroupResponse(VipCardGroup group, GroupKeyStats stats, Map<Long, String> adminNames) {
        return new VipCardGroupResponse(
                group.getId(),
                group.getGroupName(),
                group.getVipType(),
                group.getDurationDays(),
                group.getStatus(),
                group.getRemark(),
                group.getCreatedBy(),
                getNullable(adminNames, group.getCreatedBy()),
                stats.totalByGroup().getOrDefault(group.getId(), 0),
                stats.unusedByGroup().getOrDefault(group.getId(), 0),
                stats.redeemedByGroup().getOrDefault(group.getId(), 0),
                stats.voidedByGroup().getOrDefault(group.getId(), 0),
                formatTime(group.getCreatedAt()),
                formatTime(group.getUpdatedAt())
        );
    }

    private AdminVipUserResponse toVipUserResponse(AppUser user, AppUserVip activeVip, int recordCount) {
        return new AdminVipUserResponse(
                user.getId(),
                user.getNickname(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getStatus(),
                activeVip != null,
                activeVip == null ? null : activeVip.getVipType(),
                activeVip == null ? null : formatTime(activeVip.getEndTime()),
                recordCount,
                formatTime(user.getLastLoginAt()),
                formatTime(user.getCreatedAt())
        );
    }

    private AdminVipRecordResponse toRecordResponse(AppUserVip vip) {
        return new AdminVipRecordResponse(
                vip.getId(),
                vip.getUserId(),
                vip.getVipType(),
                vip.getVipStatus(),
                vip.getSourceType(),
                vip.getSourceRefId(),
                vip.getAmount(),
                formatTime(vip.getStartTime()),
                formatTime(vip.getEndTime()),
                vip.getInvalidReason(),
                formatTime(vip.getInvalidAt()),
                vip.getInvalidBy(),
                formatTime(vip.getCreatedAt()),
                formatTime(vip.getUpdatedAt())
        );
    }

    private GroupKeyStats loadGroupKeyStats(Collection<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new GroupKeyStats(Map.of(), Map.of(), Map.of(), Map.of());
        }
        List<Map<String, Object>> rows = vipCardKeyMapper.selectMaps(new QueryWrapper<VipCardKey>()
                .select("group_id AS groupId", "status AS keyStatus", "COUNT(*) AS keyCount")
                .in("group_id", groupIds)
                .groupBy("group_id", "status"));
        Map<Long, Integer> total = new HashMap<>();
        Map<Long, Integer> unused = new HashMap<>();
        Map<Long, Integer> redeemed = new HashMap<>();
        Map<Long, Integer> voided = new HashMap<>();
        rows.forEach(row -> {
            Long groupId = asLong(row.get("groupId"));
            Integer status = asInteger(row.get("keyStatus"));
            Integer count = asInteger(row.get("keyCount"));
            total.merge(groupId, count, Integer::sum);
            if (Objects.equals(status, KEY_STATUS_UNUSED)) {
                unused.put(groupId, count);
            } else if (Objects.equals(status, KEY_STATUS_REDEEMED)) {
                redeemed.put(groupId, count);
            } else if (Objects.equals(status, KEY_STATUS_VOIDED)) {
                voided.put(groupId, count);
            }
        });
        return new GroupKeyStats(total, unused, redeemed, voided);
    }

    private Map<Long, AppUserVip> loadActiveVipByUser(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        LocalDateTime now = LocalDateTime.now();
        Map<Long, AppUserVip> result = new HashMap<>();
        appUserVipMapper.selectList(new LambdaQueryWrapper<AppUserVip>()
                        .in(AppUserVip::getUserId, userIds)
                        .eq(AppUserVip::getVipStatus, VIP_STATUS_ACTIVE)
                        .ge(AppUserVip::getEndTime, now)
                        .orderByDesc(AppUserVip::getEndTime))
                .forEach(vip -> result.putIfAbsent(vip.getUserId(), vip));
        return result;
    }

    private Map<Long, Integer> loadVipRecordCount(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return appUserVipMapper.selectMaps(new QueryWrapper<AppUserVip>()
                        .select("user_id AS userId", "COUNT(*) AS recordCount")
                        .in("user_id", userIds)
                        .groupBy("user_id"))
                .stream()
                .collect(Collectors.toMap(row -> asLong(row.get("userId")), row -> asInteger(row.get("recordCount"))));
    }

    private Map<Long, VipCardGroup> loadGroups(Collection<Long> groupIds) {
        Set<Long> ids = cleanIds(groupIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return vipCardGroupMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(VipCardGroup::getId, group -> group));
    }

    private Map<Long, String> loadUserNames(Collection<Long> userIds) {
        Set<Long> ids = cleanIds(userIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return appUserMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(AppUser::getId, AppUser::getNickname));
    }

    private Map<Long, String> loadAdminNames(Collection<Long> adminIds) {
        Set<Long> ids = cleanIds(adminIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return adminUserMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));
    }

    private Set<Long> cleanIds(Collection<Long> ids) {
        if (ids == null) {
            return Set.of();
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private <T> T getNullable(Map<Long, T> source, Long key) {
        return key == null ? null : source.get(key);
    }

    private String formatSqlTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return formatTime(localDateTime);
        }
        return String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_FORMATTER);
    }

    private record NormalizedGroupPayload(String groupName, String vipType, Integer durationDays, Integer status, String remark) {
    }

    private record GroupKeyStats(Map<Long, Integer> totalByGroup,
                                 Map<Long, Integer> unusedByGroup,
                                 Map<Long, Integer> redeemedByGroup,
                                 Map<Long, Integer> voidedByGroup) {
    }
}
