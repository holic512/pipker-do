package org.example.backend.biz.kyzz.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminStatsResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryStatusUpdateRequest;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzCategoryMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.entity.AdminProjectAccess;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.mapper.AdminProjectAccessMapper;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class KyzzCategoryAdminService {

    private static final String PROJECT_CODE = "kyzz";
    private static final Pattern CATEGORY_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{1,47}$");
    private static final Pattern SIMPLE_SEGMENT_PATTERN = Pattern.compile("[a-z0-9]+");

    private final KyzzCategoryMapper kyzzCategoryMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final AdminProjectAccessMapper adminProjectAccessMapper;
    private final AdminUserMapper adminUserMapper;

    public KyzzCategoryAdminService(KyzzCategoryMapper kyzzCategoryMapper,
                                    KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                    KyzzQuestionMapper kyzzQuestionMapper,
                                    AdminProjectAccessMapper adminProjectAccessMapper,
                                    AdminUserMapper adminUserMapper) {
        this.kyzzCategoryMapper = kyzzCategoryMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.adminProjectAccessMapper = adminProjectAccessMapper;
        this.adminUserMapper = adminUserMapper;
    }

    public KyzzCategoryAdminDashboardResponse getDashboard(Long operatorId, String keyword, Integer isEnabled, Long categoryLevel) {
        requireProjectAccess(operatorId);
        validateEnabled(isEnabled, true);

        List<KyzzCategory> allCategories = kyzzCategoryMapper.selectList(new LambdaQueryWrapper<KyzzCategory>()
                .orderByAsc(KyzzCategory::getCategoryLevel)
                .orderByAsc(KyzzCategory::getSortNo)
                .orderByAsc(KyzzCategory::getId));

        CategoryAggregate aggregate = buildAggregate(allCategories);
        List<KyzzCategoryAdminItemResponse> items = allCategories.stream()
                .filter(category -> matchesKeyword(category, keyword))
                .filter(category -> isEnabled == null || Objects.equals(category.getIsEnabled(), isEnabled))
                .filter(category -> categoryLevel == null || Objects.equals(category.getCategoryLevel(), categoryLevel.intValue()))
                .sorted(categoryComparator())
                .map(category -> toItem(category, aggregate))
                .toList();

        KyzzCategoryAdminStatsResponse stats = new KyzzCategoryAdminStatsResponse(
                allCategories.size(),
                (int) allCategories.stream().filter(category -> Objects.equals(category.getIsEnabled(), 1)).count(),
                (int) allCategories.stream().filter(category -> Objects.equals(category.getCategoryLevel(), 1)).count(),
                aggregate.questionBankCountByCategory().values().stream().mapToInt(Integer::intValue).sum(),
                aggregate.questionCountByCategory().values().stream().mapToInt(Integer::intValue).sum()
        );
        return new KyzzCategoryAdminDashboardResponse(stats, items);
    }

    @Transactional
    public KyzzCategoryAdminItemResponse createCategory(Long operatorId, KyzzCategoryAdminUpsertRequest request) {
        requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类参数不能为空");
        }

        NormalizedCategoryPayload payload = normalizePayload(request, null);
        ensureCategoryCodeUnique(payload.categoryCode(), null);

        KyzzCategory category = new KyzzCategory();
        category.setCategoryCode(payload.categoryCode());
        category.setCategoryName(payload.categoryName());
        category.setCategoryLevel(payload.categoryLevel());
        category.setSortNo(payload.sortNo());
        category.setIsEnabled(payload.isEnabled());
        kyzzCategoryMapper.insert(category);
        return requireItem(category.getId());
    }

    @Transactional
    public KyzzCategoryAdminItemResponse updateCategory(Long operatorId, Long categoryId, KyzzCategoryAdminUpsertRequest request) {
        requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类参数不能为空");
        }

        requireCategory(categoryId);
        NormalizedCategoryPayload payload = normalizePayload(request, categoryId);
        ensureCategoryCodeUnique(payload.categoryCode(), categoryId);

        kyzzCategoryMapper.update(null, new LambdaUpdateWrapper<KyzzCategory>()
                .eq(KyzzCategory::getId, categoryId)
                .set(KyzzCategory::getCategoryCode, payload.categoryCode())
                .set(KyzzCategory::getCategoryName, payload.categoryName())
                .set(KyzzCategory::getCategoryLevel, payload.categoryLevel())
                .set(KyzzCategory::getSortNo, payload.sortNo())
                .set(KyzzCategory::getIsEnabled, payload.isEnabled()));
        return requireItem(categoryId);
    }

    @Transactional
    public KyzzCategoryAdminItemResponse updateCategoryStatus(Long operatorId, Long categoryId, KyzzCategoryStatusUpdateRequest request) {
        requireProjectAccess(operatorId);
        if (request == null || request.getIsEnabled() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类状态不能为空");
        }
        validateEnabled(request.getIsEnabled(), false);

        requireCategory(categoryId);
        kyzzCategoryMapper.update(null, new LambdaUpdateWrapper<KyzzCategory>()
                .eq(KyzzCategory::getId, categoryId)
                .set(KyzzCategory::getIsEnabled, request.getIsEnabled()));
        return requireItem(categoryId);
    }

    @Transactional
    public void deleteCategory(Long operatorId, Long categoryId) {
        requireProjectAccess(operatorId);
        requireCategory(categoryId);

        long bankCount = kyzzQuestionBankMapper.selectCount(new LambdaQueryWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getCategoryId, categoryId));
        if (bankCount > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前分类已被题库使用，请先迁移题库再删除");
        }

        long questionCount = kyzzQuestionMapper.selectCount(new LambdaQueryWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getCategoryId, categoryId));
        if (questionCount > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前分类已被题目引用，请先处理题目归类再删除");
        }

        kyzzCategoryMapper.deleteById(categoryId);
    }

    private KyzzCategoryAdminItemResponse requireItem(Long categoryId) {
        KyzzCategory category = requireCategory(categoryId);
        return toItem(category, buildAggregate(List.of(category)));
    }

    private NormalizedCategoryPayload normalizePayload(KyzzCategoryAdminUpsertRequest request, Long excludeId) {
        String categoryName = normalizeCategoryName(request.getCategoryName());
        String categoryCode = resolveCategoryCode(request.getCategoryCode(), categoryName, excludeId);
        Integer categoryLevel = request.getCategoryLevel() == null ? 1 : request.getCategoryLevel();
        Integer sortNo = request.getSortNo() == null ? 0 : request.getSortNo();
        Integer isEnabled = request.getIsEnabled() == null ? 1 : request.getIsEnabled();

        validateCategoryName(categoryName);
        validateCategoryLevel(categoryLevel);
        validateSortNo(sortNo);
        validateEnabled(isEnabled, false);
        return new NormalizedCategoryPayload(categoryCode, categoryName, categoryLevel, sortNo, isEnabled);
    }

    private String resolveCategoryCode(String incomingCode, String categoryName, Long excludeId) {
        String normalizedCode = normalizeCategoryCode(incomingCode);
        if (StringUtils.hasText(normalizedCode)) {
            validateCategoryCode(normalizedCode);
            return normalizedCode;
        }
        return generateUniqueCode(categoryName, "cat", excludeId);
    }

    private void ensureCategoryCodeUnique(String categoryCode, Long excludeId) {
        LambdaQueryWrapper<KyzzCategory> queryWrapper = new LambdaQueryWrapper<KyzzCategory>()
                .eq(KyzzCategory::getCategoryCode, categoryCode);
        if (excludeId != null) {
            queryWrapper.ne(KyzzCategory::getId, excludeId);
        }
        if (kyzzCategoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类编码已存在");
        }
    }

    private CategoryAggregate buildAggregate(Collection<KyzzCategory> categories) {
        LinkedHashSet<Long> categoryIds = categories.stream()
                .map(KyzzCategory::getId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (categoryIds.isEmpty()) {
            return new CategoryAggregate(Map.of(), Map.of());
        }

        Map<Long, Integer> questionBankCountByCategory = loadRelationCountMap(kyzzQuestionBankMapper.selectMaps(
                new QueryWrapper<KyzzQuestionBank>()
                        .select("category_id AS relationId", "COUNT(*) AS relationCount")
                        .in("category_id", categoryIds)
                        .groupBy("category_id")));
        Map<Long, Integer> questionCountByCategory = loadRelationCountMap(kyzzQuestionMapper.selectMaps(
                new QueryWrapper<KyzzQuestion>()
                        .select("category_id AS relationId", "COUNT(*) AS relationCount")
                        .in("category_id", categoryIds)
                        .groupBy("category_id")));
        return new CategoryAggregate(questionBankCountByCategory, questionCountByCategory);
    }

    private Map<Long, Integer> loadRelationCountMap(List<Map<String, Object>> rows) {
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object relationId = row.get("relationId");
            Object relationCount = row.get("relationCount");
            if (relationId instanceof Number id && relationCount instanceof Number count) {
                result.put(id.longValue(), count.intValue());
            }
        }
        return result;
    }

    private KyzzCategoryAdminItemResponse toItem(KyzzCategory category, CategoryAggregate aggregate) {
        int questionBankCount = aggregate.questionBankCountByCategory().getOrDefault(category.getId(), 0);
        int questionCount = aggregate.questionCountByCategory().getOrDefault(category.getId(), 0);
        return new KyzzCategoryAdminItemResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                category.getCategoryLevel(),
                category.getSortNo(),
                category.getIsEnabled(),
                questionBankCount,
                questionCount,
                questionBankCount == 0 && questionCount == 0,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private Comparator<KyzzCategory> categoryComparator() {
        return Comparator
                .comparing(KyzzCategory::getCategoryLevel, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(KyzzCategory::getSortNo, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(KyzzCategory::getId);
    }

    private boolean matchesKeyword(KyzzCategory category, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(category.getCategoryCode(), normalizedKeyword)
                || contains(category.getCategoryName(), normalizedKeyword);
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeCategoryCode(String categoryCode) {
        return categoryCode == null ? "" : categoryCode.trim().toLowerCase(Locale.ROOT);
    }

    private String generateUniqueCode(String seed, String prefix, Long excludeId) {
        String base = normalizeSegment(seed);
        if (!StringUtils.hasText(base)) {
            base = prefix + "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        }
        if (!Character.isLetter(base.charAt(0))) {
            base = prefix + "_" + base;
        }
        base = truncateCode(base, 48);

        String candidate = base;
        int suffix = 2;
        while (categoryCodeExists(candidate, excludeId)) {
            String tail = "_" + suffix;
            candidate = truncateCode(base, 48 - tail.length()) + tail;
            suffix++;
        }
        return candidate;
    }

    private boolean categoryCodeExists(String categoryCode, Long excludeId) {
        LambdaQueryWrapper<KyzzCategory> queryWrapper = new LambdaQueryWrapper<KyzzCategory>()
                .eq(KyzzCategory::getCategoryCode, categoryCode);
        if (excludeId != null) {
            queryWrapper.ne(KyzzCategory::getId, excludeId);
        }
        return kyzzCategoryMapper.selectCount(queryWrapper) > 0;
    }

    private String normalizeSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String lowered = value.trim().toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        boolean lastUnderscore = false;
        for (int i = 0; i < lowered.length(); i++) {
            char ch = lowered.charAt(i);
            if (Character.isLetterOrDigit(ch) && SIMPLE_SEGMENT_PATTERN.matcher(String.valueOf(ch)).matches()) {
                builder.append(ch);
                lastUnderscore = false;
                continue;
            }
            if (!lastUnderscore && builder.length() > 0) {
                builder.append('_');
                lastUnderscore = true;
            }
        }
        while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '_') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    private String truncateCode(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizeCategoryName(String categoryName) {
        return categoryName == null ? "" : categoryName.trim();
    }

    private void validateCategoryCode(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类编码不能为空");
        }
        if (!CATEGORY_CODE_PATTERN.matcher(categoryCode).matches()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类编码需为 2 到 48 位小写字母、数字、下划线或中划线，且以字母开头");
        }
    }

    private void validateCategoryName(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类名称不能为空");
        }
        if (categoryName.length() < 2 || categoryName.length() > 50) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类名称长度需保持在 2 到 50 个字符之间");
        }
    }

    private void validateCategoryLevel(Integer categoryLevel) {
        if (categoryLevel == null || categoryLevel < 1 || categoryLevel > 3) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类层级仅支持 1 到 3");
        }
    }

    private void validateSortNo(Integer sortNo) {
        if (sortNo == null || sortNo < 0 || sortNo > 9999) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "排序值需保持在 0 到 9999 之间");
        }
    }

    private void validateEnabled(Integer isEnabled, boolean allowNull) {
        if (isEnabled == null && allowNull) {
            return;
        }
        if (isEnabled == null || (isEnabled != 0 && isEnabled != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类状态仅支持 0 或 1");
        }
    }

    private void requireProjectAccess(Long operatorId) {
        AdminUser adminUser = adminUserMapper.selectById(operatorId);
        if (adminUser == null || !Objects.equals(adminUser.getStatus(), 1)) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员不可用");
        }

        long projectAccess = adminProjectAccessMapper.selectCount(new LambdaQueryWrapper<AdminProjectAccess>()
                .eq(AdminProjectAccess::getUserId, operatorId)
                .eq(AdminProjectAccess::getProjectCode, PROJECT_CODE)
                .eq(AdminProjectAccess::getEnabled, 1));
        if (projectAccess <= 0) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员没有考研政治项目权限");
        }
    }

    private KyzzCategory requireCategory(Long categoryId) {
        KyzzCategory category = kyzzCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private record NormalizedCategoryPayload(String categoryCode,
                                             String categoryName,
                                             Integer categoryLevel,
                                             Integer sortNo,
                                             Integer isEnabled) {
    }

    private record CategoryAggregate(Map<Long, Integer> questionBankCountByCategory,
                                     Map<Long, Integer> questionCountByCategory) {
    }
}
