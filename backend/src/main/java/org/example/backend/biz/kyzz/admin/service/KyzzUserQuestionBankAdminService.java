package org.example.backend.biz.kyzz.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminBankOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminPaginationResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminSelectedBankResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminStatsResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminSummaryResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminUserDetailResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankAdminUserItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankSelectionUpdateRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzUserQuestionBankSelectionUpdateResponse;
import org.example.backend.biz.kyzz.admin.support.KyzzAdminAccessSupport;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.biz.kyzz.support.KyzzPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.account.entity.AppUser;
import org.example.backend.shared.account.mapper.AppUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ 管理端用户题库选择服务。
 */
@Service
public class KyzzUserQuestionBankAdminService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SUPPORTED_SELECTION_STATUS = Set.of("all", "selected", "unselected");

    private final AppUserMapper appUserMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final KyzzPracticeSupport kyzzPracticeSupport;
    private final KyzzAdminAccessSupport kyzzAdminAccessSupport;

    public KyzzUserQuestionBankAdminService(AppUserMapper appUserMapper,
                                            KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                            KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                                            KyzzPracticeSupport kyzzPracticeSupport,
                                            KyzzAdminAccessSupport kyzzAdminAccessSupport) {
        this.appUserMapper = appUserMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.kyzzPracticeSupport = kyzzPracticeSupport;
        this.kyzzAdminAccessSupport = kyzzAdminAccessSupport;
    }

    public KyzzUserQuestionBankAdminDashboardResponse getDashboard(Long operatorId,
                                                                   String keyword,
                                                                   String selectionStatus,
                                                                   Long pageNo,
                                                                   Long pageSize) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);

        String normalizedSelectionStatus = normalizeSelectionStatus(selectionStatus);
        PageFilter filter = normalizePageFilter(pageNo, pageSize, keyword, normalizedSelectionStatus);

        Page<AppUser> page = new Page<>(filter.pageNo(), filter.pageSize());
        LambdaQueryWrapper<AppUser> queryWrapper = buildUserQuery(filter.keyword(), filter.selectionStatus());
        Page<AppUser> pageResult = appUserMapper.selectPage(page, queryWrapper);

        List<AppUser> users = pageResult.getRecords();
        Map<Long, UserSelectionAggregate> aggregateMap = loadUserSelectionAggregate(users.stream().map(AppUser::getId).toList());

        List<KyzzUserQuestionBankAdminUserItemResponse> records = users.stream()
                .map(user -> {
                    UserSelectionAggregate aggregate = aggregateMap.getOrDefault(user.getId(), UserSelectionAggregate.empty());
                    return new KyzzUserQuestionBankAdminUserItemResponse(
                            user.getId(),
                            user.getNickname(),
                            user.getUsername(),
                            user.getPhone(),
                            user.getStatus(),
                            aggregate.selectedBankCount(),
                            aggregate.lastPracticeAt(),
                            aggregate.updatedAt()
                    );
                })
                .toList();

        long totalUsers = appUserMapper.selectCount(new LambdaQueryWrapper<AppUser>());
        long selectedUsers = countDistinctSelectedUsers();
        long totalSelections = kyzzUserQuestionBankMapper.selectCount(new LambdaQueryWrapper<KyzzUserQuestionBank>());

        return new KyzzUserQuestionBankAdminDashboardResponse(
                new KyzzUserQuestionBankAdminStatsResponse(
                        totalUsers,
                        selectedUsers,
                        Math.max(0, totalUsers - selectedUsers),
                        totalSelections
                ),
                records,
                new KyzzUserQuestionBankAdminPaginationResponse(
                        pageResult.getCurrent(),
                        pageResult.getSize(),
                        pageResult.getTotal(),
                        pageResult.getPages()
                )
        );
    }

    public KyzzUserQuestionBankAdminUserDetailResponse getUserDetail(Long operatorId, Long userId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        AppUser user = requireUser(userId);

        List<KyzzUserQuestionBank> relations = kyzzUserQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .orderByDesc(KyzzUserQuestionBank::getCreatedAt)
                .orderByDesc(KyzzUserQuestionBank::getId));

        Map<Long, KyzzUserQuestionBank> relationMap = relations.stream()
                .collect(Collectors.toMap(
                        KyzzUserQuestionBank::getQuestionBankId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<KyzzQuestionBank> selectedBanks = relationMap.isEmpty()
                ? List.of()
                : kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                        .in(KyzzQuestionBank::getId, relationMap.keySet())
                        .orderByAsc(KyzzQuestionBank::getSortNo)
                        .orderByDesc(KyzzQuestionBank::getId));

        Map<Long, KyzzCategory> categoryMap = kyzzPracticeSupport.buildCategoryMap();
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(
                userId,
                selectedBanks.stream().map(KyzzQuestionBank::getId).toList(),
                selectedBanks
        );

        List<KyzzUserQuestionBankAdminSelectedBankResponse> selectedItems = selectedBanks.stream()
                .map(bank -> toSelectedItem(bank, relationMap.get(bank.getId()), categoryMap.get(bank.getCategoryId()), progressMap.get(bank.getId())))
                .sorted(Comparator.comparing(KyzzUserQuestionBankAdminSelectedBankResponse::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<KyzzQuestionBank> activeBanks = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getStatus, 1)
                .orderByAsc(KyzzQuestionBank::getSortNo)
                .orderByDesc(KyzzQuestionBank::getId));
        List<KyzzUserQuestionBankAdminBankOptionResponse> availableBanks = activeBanks.stream()
                .map(bank -> new KyzzUserQuestionBankAdminBankOptionResponse(
                        bank.getId(),
                        bank.getBankCode(),
                        bank.getBankName(),
                        bank.getCategoryId() == null ? null : resolveCategoryName(categoryMap.get(bank.getCategoryId())),
                        bank.getDifficultyLevel(),
                        bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                        bank.getSortNo() == null ? 0 : bank.getSortNo(),
                        relationMap.containsKey(bank.getId())
                ))
                .toList();

        int selectedCount = selectedItems.size();
        int completedCount = (int) selectedItems.stream()
                .filter(item -> item.getQuestionCount() != null && item.getQuestionCount() > 0)
                .filter(item -> item.getCurrentProgress() != null && item.getCurrentProgress().compareTo(KyzzPracticeSupport.FULL_PROGRESS) >= 0)
                .count();
        int inProgressCount = (int) selectedItems.stream()
                .filter(item -> item.getCurrentProgress() != null)
                .filter(item -> item.getCurrentProgress().compareTo(KyzzPracticeSupport.ZERO_PROGRESS) > 0)
                .filter(item -> item.getCurrentProgress().compareTo(KyzzPracticeSupport.FULL_PROGRESS) < 0)
                .count();

        return new KyzzUserQuestionBankAdminUserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getUsername(),
                user.getPhone(),
                user.getStatus(),
                user.getLastLoginAt(),
                new KyzzUserQuestionBankAdminSummaryResponse(selectedCount, inProgressCount, completedCount),
                selectedItems,
                availableBanks
        );
    }

    @Transactional
    public KyzzUserQuestionBankSelectionUpdateResponse updateSelection(Long operatorId,
                                                                       Long userId,
                                                                       Long bankId,
                                                                       KyzzUserQuestionBankSelectionUpdateRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        requireUser(userId);
        if (request == null || request.getSelected() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择要执行的题库操作");
        }

        KyzzQuestionBank bank = kyzzPracticeSupport.requireActiveQuestionBank(bankId);
        KyzzUserQuestionBank relation = kyzzUserQuestionBankMapper.selectOne(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId)
                .last("limit 1"));

        if (Boolean.TRUE.equals(request.getSelected())) {
            if (relation == null) {
                relation = new KyzzUserQuestionBank();
                relation.setUserId(userId);
                relation.setQuestionBankId(bankId);
                relation.setJoinSource("manual");
                relation.setCurrentProgress(KyzzPracticeSupport.ZERO_PROGRESS);
                relation.setStudiedCount(0);
                relation.setCorrectCount(0);
                relation.setWrongCount(0);
                kyzzUserQuestionBankMapper.insert(relation);
            }
        } else if (relation != null) {
            kyzzUserQuestionBankMapper.deleteById(relation.getId());
            relation = null;
        }

        kyzzPracticeSupport.syncStudyUserCount(bankId);
        KyzzQuestionBank refreshedBank = kyzzQuestionBankMapper.selectById(bankId);
        if (refreshedBank == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题库不存在");
        }

        if (relation == null) {
            return new KyzzUserQuestionBankSelectionUpdateResponse(false, null);
        }

        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(
                userId,
                List.of(bankId),
                List.of(refreshedBank)
        );
        KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot = progressMap.getOrDefault(bankId, KyzzPracticeSupport.QuestionBankProgressSnapshot.empty());
        kyzzPracticeSupport.syncRelationSnapshot(relation.getId(), snapshot);
        KyzzUserQuestionBank refreshedRelation = kyzzUserQuestionBankMapper.selectById(relation.getId());
        Map<Long, KyzzCategory> categoryMap = kyzzPracticeSupport.buildCategoryMap();
        KyzzUserQuestionBankAdminSelectedBankResponse record = toSelectedItem(
                refreshedBank,
                refreshedRelation,
                categoryMap.get(refreshedBank.getCategoryId()),
                snapshot
        );
        return new KyzzUserQuestionBankSelectionUpdateResponse(true, record);
    }

    private LambdaQueryWrapper<AppUser> buildUserQuery(String keyword, String selectionStatus) {
        LambdaQueryWrapper<AppUser> queryWrapper = new LambdaQueryWrapper<AppUser>()
                .orderByDesc(AppUser::getId);

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> {
                wrapper.like(AppUser::getNickname, trimmedKeyword)
                        .or().like(AppUser::getUsername, trimmedKeyword)
                        .or().like(AppUser::getPhone, trimmedKeyword);
                if (trimmedKeyword.matches("^\\d+$")) {
                    wrapper.or().eq(AppUser::getId, Long.parseLong(trimmedKeyword));
                }
            });
        }

        if ("selected".equals(selectionStatus)) {
            queryWrapper.apply("EXISTS (SELECT 1 FROM kyzz_user_question_bank rel WHERE rel.user_id = app_user.id)");
        } else if ("unselected".equals(selectionStatus)) {
            queryWrapper.apply("NOT EXISTS (SELECT 1 FROM kyzz_user_question_bank rel WHERE rel.user_id = app_user.id)");
        }
        return queryWrapper;
    }

    private Map<Long, UserSelectionAggregate> loadUserSelectionAggregate(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<Map<String, Object>> rows = kyzzUserQuestionBankMapper.selectMaps(new QueryWrapper<KyzzUserQuestionBank>()
                .select(
                        "user_id AS relationId",
                        "COUNT(*) AS relationCount",
                        "MAX(last_practice_at) AS lastPracticeAt",
                        "MAX(updated_at) AS updatedAt"
                )
                .in("user_id", userIds)
                .groupBy("user_id"));

        Map<Long, UserSelectionAggregate> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = toLong(row.get("relationId"));
            if (userId == null) {
                continue;
            }
            Integer relationCount = toInteger(row.get("relationCount"));
            result.put(userId, new UserSelectionAggregate(
                    relationCount == null ? 0 : relationCount,
                    toLocalDateTime(row.get("lastPracticeAt")),
                    toLocalDateTime(row.get("updatedAt"))
            ));
        }
        return result;
    }

    private KyzzUserQuestionBankAdminSelectedBankResponse toSelectedItem(KyzzQuestionBank bank,
                                                                         KyzzUserQuestionBank relation,
                                                                         KyzzCategory category,
                                                                         KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot) {
        KyzzPracticeSupport.QuestionBankProgressSnapshot resolvedSnapshot = snapshot == null
                ? KyzzPracticeSupport.QuestionBankProgressSnapshot.empty()
                : snapshot;

        BigDecimal currentProgress = resolvedSnapshot.currentProgress() == null
                ? KyzzPracticeSupport.ZERO_PROGRESS
                : resolvedSnapshot.currentProgress();

        return new KyzzUserQuestionBankAdminSelectedBankResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                resolveCategoryName(category),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                currentProgress,
                resolvedSnapshot.studiedCount(),
                resolvedSnapshot.correctCount(),
                resolvedSnapshot.wrongCount(),
                resolvedSnapshot.lastPracticeAt(),
                relation == null ? null : relation.getJoinSource(),
                relation == null ? null : relation.getCreatedAt(),
                bank.getStatus()
        );
    }

    private String resolveCategoryName(KyzzCategory category) {
        return category == null ? null : category.getCategoryName();
    }

    private AppUser requireUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private String normalizeSelectionStatus(String selectionStatus) {
        if (!StringUtils.hasText(selectionStatus)) {
            return "all";
        }
        String normalized = selectionStatus.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SELECTION_STATUS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "selectionStatus 仅支持 all、selected、unselected");
        }
        return normalized;
    }

    private PageFilter normalizePageFilter(Long pageNo, Long pageSize, String keyword, String selectionStatus) {
        long normalizedPageNo = pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
        long normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return new PageFilter(normalizedPageNo, normalizedPageSize, normalizedKeyword, selectionStatus);
    }

    private long countDistinctSelectedUsers() {
        List<Map<String, Object>> rows = kyzzUserQuestionBankMapper.selectMaps(new QueryWrapper<KyzzUserQuestionBank>()
                .select("COUNT(DISTINCT user_id) AS selectedUsers"));
        if (rows.isEmpty()) {
            return 0L;
        }
        Long count = toLong(rows.get(0).get("selectedUsers"));
        return count == null ? 0L : count;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private record PageFilter(Long pageNo, Long pageSize, String keyword, String selectionStatus) {
    }

    private record UserSelectionAggregate(Integer selectedBankCount,
                                          LocalDateTime lastPracticeAt,
                                          LocalDateTime updatedAt) {
        private static UserSelectionAggregate empty() {
            return new UserSelectionAggregate(0, null, null);
        }
    }
}
