/**
 * @file KyzzQuestionBankAdminService
 * @project pipker-do
 * @module 考研政治 / 管理端题库
 * @description 维护政治题库后台列表、详情、封面、状态、统计与删除校验。
 * @logic 1. 管理题库基础信息和封面 storageKey；2. 聚合题目、学习、评分统计；3. 通过 FileStorage 删除和解析题库封面。
 * @dependencies Mapper: KyzzQuestionBankMapper, KyzzQuestionMapper; Service: FileStorage, KyzzAdminAccessSupport
 * @index_tags 考研政治, 题库管理, 题库封面, FileStorage, 管理端
 * @author holic512
 */
package org.example.backend.biz.kyzz.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminStatsResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankCoverUpdateRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankStatusUpdateRequest;
import org.example.backend.biz.kyzz.admin.support.KyzzAdminAccessSupport;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBankRating;
import org.example.backend.biz.kyzz.entity.KyzzUserAnswer;
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzCategoryMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankRatingMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserAnswerMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.biz.kyzz.support.KyzzCacheService;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.example.backend.shared.storage.core.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class KyzzQuestionBankAdminService {

    private static final Pattern BANK_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{1,47}$");
    private static final Pattern SIMPLE_SEGMENT_PATTERN = Pattern.compile("[a-z0-9]+");

    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzCategoryMapper kyzzCategoryMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final KyzzQuestionBankRatingMapper kyzzQuestionBankRatingMapper;
    private final KyzzUserAnswerMapper kyzzUserAnswerMapper;
    private final AdminUserMapper adminUserMapper;
    private final FileStorage fileStorage;
    private final KyzzAdminAccessSupport kyzzAdminAccessSupport;
    private final KyzzCacheService kyzzCacheService;

    public KyzzQuestionBankAdminService(KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                        KyzzCategoryMapper kyzzCategoryMapper,
                                        KyzzQuestionMapper kyzzQuestionMapper,
                                        KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                                        KyzzQuestionBankRatingMapper kyzzQuestionBankRatingMapper,
                                        KyzzUserAnswerMapper kyzzUserAnswerMapper,
                                        AdminUserMapper adminUserMapper,
                                        FileStorage fileStorage,
                                        KyzzAdminAccessSupport kyzzAdminAccessSupport,
                                        KyzzCacheService kyzzCacheService) {
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzCategoryMapper = kyzzCategoryMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.kyzzQuestionBankRatingMapper = kyzzQuestionBankRatingMapper;
        this.kyzzUserAnswerMapper = kyzzUserAnswerMapper;
        this.adminUserMapper = adminUserMapper;
        this.fileStorage = fileStorage;
        this.kyzzAdminAccessSupport = kyzzAdminAccessSupport;
        this.kyzzCacheService = kyzzCacheService;
    }

    public KyzzQuestionBankAdminDashboardResponse getDashboard(Long operatorId,
                                                               String keyword,
                                                               Integer status,
                                                               Long categoryId,
                                                               Integer difficultyLevel) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        validateStatus(status, true);
        validateDifficultyLevel(difficultyLevel, true);

        List<KyzzQuestionBank> allBanks = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .orderByAsc(KyzzQuestionBank::getSortNo)
                .orderByDesc(KyzzQuestionBank::getId));
        List<KyzzCategory> allCategories = kyzzCategoryMapper.selectList(new LambdaQueryWrapper<KyzzCategory>()
                .orderByAsc(KyzzCategory::getCategoryLevel)
                .orderByAsc(KyzzCategory::getSortNo)
                .orderByAsc(KyzzCategory::getId));
        Map<Long, KyzzCategory> categoryMap = new HashMap<>();
        allCategories.forEach(category -> categoryMap.put(category.getId(), category));
        Map<Long, String> adminNameMap = buildAdminNameMap(allBanks);

        QuestionBankAggregate aggregate = buildAggregate(allBanks);
        List<KyzzQuestionBankAdminItemResponse> banks = allBanks.stream()
                .filter(bank -> matchesKeyword(bank, keyword))
                .filter(bank -> status == null || Objects.equals(bank.getStatus(), status))
                .filter(bank -> categoryId == null || Objects.equals(bank.getCategoryId(), categoryId))
                .filter(bank -> difficultyLevel == null || Objects.equals(bank.getDifficultyLevel(), difficultyLevel))
                .sorted(Comparator.comparing(KyzzQuestionBank::getSortNo, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(KyzzQuestionBank::getId, Comparator.reverseOrder()))
                .map(bank -> toItem(bank, categoryMap, adminNameMap, aggregate))
                .toList();

        KyzzQuestionBankAdminStatsResponse stats = new KyzzQuestionBankAdminStatsResponse(
                allBanks.size(),
                (int) allBanks.stream().filter(bank -> Objects.equals(bank.getStatus(), 1)).count(),
                (int) allBanks.stream().filter(bank -> Objects.equals(bank.getStatus(), 0)).count(),
                (int) allBanks.stream().filter(bank -> bank.getCategoryId() == null).count(),
                aggregate.actualQuestionCountByBankId().values().stream().mapToInt(Integer::intValue).sum(),
                allBanks.stream().map(KyzzQuestionBank::getStudyUserCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum()
        );

        List<KyzzCategoryOptionResponse> categories = allCategories.stream()
                .map(category -> new KyzzCategoryOptionResponse(
                        category.getId(),
                        category.getCategoryCode(),
                        category.getCategoryName(),
                        category.getCategoryLevel(),
                        category.getIsEnabled()
                ))
                .toList();

        return new KyzzQuestionBankAdminDashboardResponse(stats, banks, categories);
    }

    @Transactional
    public KyzzQuestionBankAdminItemResponse createQuestionBank(Long operatorId, KyzzQuestionBankAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库参数不能为空");
        }

        NormalizedPayload payload = normalizePayload(request, null);
        ensureBankCodeUnique(payload.bankCode(), null);

        KyzzQuestionBank bank = new KyzzQuestionBank();
        bank.setBankCode(payload.bankCode());
        bank.setBankName(payload.bankName());
        bank.setSubtitle(payload.subtitle());
        bank.setDescription(payload.description());
        bank.setCategoryId(payload.categoryId());
        bank.setDifficultyLevel(payload.difficultyLevel());
        bank.setQuestionCount(0);
        bank.setTotalScore(BigDecimal.ZERO.setScale(2));
        bank.setRatingCount(0);
        bank.setCollectCount(0);
        bank.setStudyUserCount(0);
        bank.setStatus(payload.status());
        bank.setSortNo(payload.sortNo());
        bank.setCreatedBy(operatorId);
        kyzzQuestionBankMapper.insert(bank);
        kyzzCacheService.evictPublicBaseCaches();
        return requireItem(bank.getId());
    }

    @Transactional
    public KyzzQuestionBankAdminItemResponse updateQuestionBank(Long operatorId,
                                                                Long bankId,
                                                                KyzzQuestionBankAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库参数不能为空");
        }

        KyzzQuestionBank existing = requireQuestionBank(bankId);
        NormalizedPayload payload = normalizePayload(request, bankId);
        ensureBankCodeUnique(payload.bankCode(), bankId);

        int actualQuestionCount = countActualQuestions(bankId);
        kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getBankCode, payload.bankCode())
                .set(KyzzQuestionBank::getBankName, payload.bankName())
                .set(KyzzQuestionBank::getSubtitle, payload.subtitle())
                .set(KyzzQuestionBank::getDescription, payload.description())
                .set(KyzzQuestionBank::getCategoryId, payload.categoryId())
                .set(KyzzQuestionBank::getDifficultyLevel, payload.difficultyLevel())
                .set(KyzzQuestionBank::getQuestionCount, actualQuestionCount)
                .set(KyzzQuestionBank::getStatus, payload.status())
                .set(KyzzQuestionBank::getSortNo, payload.sortNo())
                .set(KyzzQuestionBank::getCreatedBy, existing.getCreatedBy()));
        kyzzCacheService.evictPublicBaseCaches();
        return requireItem(bankId);
    }

    @Transactional
    public KyzzQuestionBankAdminItemResponse updateQuestionBankStatus(Long operatorId,
                                                                      Long bankId,
                                                                      KyzzQuestionBankStatusUpdateRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null || request.getStatus() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库状态不能为空");
        }
        validateStatus(request.getStatus(), false);

        requireQuestionBank(bankId);
        kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getStatus, request.getStatus()));
        kyzzCacheService.evictPublicBaseCaches();
        return requireItem(bankId);
    }

    @Transactional
    public KyzzQuestionBankAdminItemResponse updateQuestionBankCover(Long operatorId,
                                                                     Long bankId,
                                                                     KyzzQuestionBankCoverUpdateRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库封面参数不能为空");
        }

        KyzzQuestionBank bank = requireQuestionBank(bankId);
        String nextCoverStorageKey = normalizeCoverStorageKey(request.getCoverStorageKey());
        String oldCoverValue = bank.getCoverUrl();

        kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getCoverUrl, nextCoverStorageKey));
        kyzzCacheService.evictPublicBaseCaches();

        if (StringUtils.hasText(oldCoverValue)
                && !Objects.equals(oldCoverValue, nextCoverStorageKey)
                && fileStorage.isManagedKey(oldCoverValue)) {
            fileStorage.deleteByKey(oldCoverValue);
        }
        return requireItem(bankId);
    }

    @Transactional
    public void deleteQuestionBank(Long operatorId, Long bankId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        KyzzQuestionBank bank = requireQuestionBank(bankId);

        if (countActualQuestions(bankId) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库下仍存在题目，不能直接删除");
        }
        if (kyzzUserQuestionBankMapper.selectCount(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId)) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库已产生学习记录，不能直接删除");
        }
        if (kyzzQuestionBankRatingMapper.selectCount(new LambdaQueryWrapper<KyzzQuestionBankRating>()
                .eq(KyzzQuestionBankRating::getQuestionBankId, bankId)) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库已产生评分记录，不能直接删除");
        }
        if (kyzzUserAnswerMapper.selectCount(new LambdaQueryWrapper<KyzzUserAnswer>()
                .eq(KyzzUserAnswer::getQuestionBankId, bankId)) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库已产生答题记录，不能直接删除");
        }

        if (StringUtils.hasText(bank.getCoverUrl()) && fileStorage.isManagedKey(bank.getCoverUrl())) {
            fileStorage.deleteByKey(bank.getCoverUrl());
        }
        kyzzQuestionBankMapper.deleteById(bankId);
        kyzzCacheService.evictPublicBaseCaches();
    }

    @Transactional
    public KyzzQuestionBankAdminItemResponse syncQuestionCount(Long operatorId, Long bankId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        requireQuestionBank(bankId);
        syncQuestionCounts(List.of(bankId));
        kyzzCacheService.evictPublicBaseCaches();
        return requireItem(bankId);
    }

    private KyzzQuestionBankAdminItemResponse requireItem(Long bankId) {
        KyzzQuestionBank bank = requireQuestionBank(bankId);
        Map<Long, KyzzCategory> categoryMap = new HashMap<>();
        Map<Long, String> adminNameMap = buildAdminNameMap(List.of(bank));
        if (bank.getCategoryId() != null) {
            KyzzCategory category = kyzzCategoryMapper.selectById(bank.getCategoryId());
            if (category != null) {
                categoryMap.put(category.getId(), category);
            }
        }
        return toItem(bank, categoryMap, adminNameMap, buildAggregate(List.of(bank)));
    }

    private Map<Long, String> buildAdminNameMap(Collection<KyzzQuestionBank> banks) {
        LinkedHashSet<Long> adminIds = banks.stream()
                .map(KyzzQuestionBank::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (adminIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> adminNameMap = new HashMap<>();
        adminUserMapper.selectList(new LambdaQueryWrapper<AdminUser>()
                        .in(AdminUser::getId, adminIds))
                .forEach(admin -> adminNameMap.put(admin.getId(), admin.getDisplayName()));
        return adminNameMap;
    }

    private NormalizedPayload normalizePayload(KyzzQuestionBankAdminUpsertRequest request, Long excludeId) {
        String bankCode = resolveBankCode(request.getBankCode(), request.getBankName(), excludeId);
        String bankName = trimText(request.getBankName());
        String subtitle = normalizeNullableText(request.getSubtitle(), 255, "题库副标题不能超过255个字符");
        String description = normalizeNullableText(request.getDescription(), 5000, "题库介绍不能超过5000个字符");
        Long categoryId = request.getCategoryId();
        Integer difficultyLevel = request.getDifficultyLevel() == null ? 2 : request.getDifficultyLevel();
        Integer sortNo = request.getSortNo() == null ? 0 : request.getSortNo();
        Integer status = request.getStatus() == null ? 1 : request.getStatus();

        validateBankCode(bankCode);
        validateBankName(bankName);
        validateDifficultyLevel(difficultyLevel, false);
        validateSortNo(sortNo);
        validateStatus(status, false);
        validateCategoryId(categoryId);
        return new NormalizedPayload(bankCode, bankName, subtitle, description, categoryId, difficultyLevel, sortNo, status);
    }

    private String resolveBankCode(String incomingCode, String bankName, Long excludeId) {
        String normalizedCode = normalizeCode(incomingCode);
        if (StringUtils.hasText(normalizedCode)) {
            validateBankCode(normalizedCode);
            return normalizedCode;
        }
        return generateUniqueCode(bankName, "bank", excludeId);
    }

    private void validateCategoryId(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        KyzzCategory category = kyzzCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选分类不存在");
        }
        if (!Objects.equals(category.getIsEnabled(), 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选分类已停用，暂不建议继续挂载题库");
        }
    }

    private void ensureBankCodeUnique(String bankCode, Long excludeId) {
        LambdaQueryWrapper<KyzzQuestionBank> queryWrapper = new LambdaQueryWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getBankCode, bankCode);
        if (excludeId != null) {
            queryWrapper.ne(KyzzQuestionBank::getId, excludeId);
        }
        if (kyzzQuestionBankMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库编码已存在");
        }
    }

    private QuestionBankAggregate buildAggregate(Collection<KyzzQuestionBank> banks) {
        LinkedHashSet<Long> bankIds = banks.stream()
                .map(KyzzQuestionBank::getId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (bankIds.isEmpty()) {
            return new QuestionBankAggregate(Map.of(), Map.of(), Map.of(), Map.of());
        }

        Map<Long, Integer> actualQuestionCountByBankId = loadRelationCountMap(kyzzQuestionMapper.selectMaps(
                new QueryWrapper<KyzzQuestion>()
                        .select("question_bank_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_bank_id", bankIds)
                        .groupBy("question_bank_id")));
        Map<Long, Integer> learningRecordCountByBankId = loadRelationCountMap(kyzzUserQuestionBankMapper.selectMaps(
                new QueryWrapper<KyzzUserQuestionBank>()
                        .select("question_bank_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_bank_id", bankIds)
                        .groupBy("question_bank_id")));
        Map<Long, Integer> ratingCountByBankId = loadRelationCountMap(kyzzQuestionBankRatingMapper.selectMaps(
                new QueryWrapper<KyzzQuestionBankRating>()
                        .select("question_bank_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_bank_id", bankIds)
                        .groupBy("question_bank_id")));
        Map<Long, Integer> answerCountByBankId = loadRelationCountMap(kyzzUserAnswerMapper.selectMaps(
                new QueryWrapper<KyzzUserAnswer>()
                        .select("question_bank_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_bank_id", bankIds)
                        .groupBy("question_bank_id")));
        return new QuestionBankAggregate(actualQuestionCountByBankId, learningRecordCountByBankId, ratingCountByBankId, answerCountByBankId);
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

    private KyzzQuestionBankAdminItemResponse toItem(KyzzQuestionBank bank,
                                                     Map<Long, KyzzCategory> categoryMap,
                                                     Map<Long, String> adminNameMap,
                                                     QuestionBankAggregate aggregate) {
        KyzzCategory category = bank.getCategoryId() == null ? null : categoryMap.get(bank.getCategoryId());
        int actualQuestionCount = aggregate.actualQuestionCountByBankId().getOrDefault(bank.getId(), 0);
        int learningRecordCount = aggregate.learningRecordCountByBankId().getOrDefault(bank.getId(), 0);
        int ratingCount = aggregate.ratingCountByBankId().getOrDefault(bank.getId(), 0);
        int answerCount = aggregate.answerCountByBankId().getOrDefault(bank.getId(), 0);
        return new KyzzQuestionBankAdminItemResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                resolveCoverUrl(bank.getCoverUrl()),
                resolveCoverStorageKey(bank.getCoverUrl()),
                bank.getDescription(),
                bank.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                category == null ? null : category.getCategoryLevel(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                actualQuestionCount,
                bank.getTotalScore() == null ? BigDecimal.ZERO.setScale(2) : bank.getTotalScore(),
                bank.getRatingCount() == null ? 0 : bank.getRatingCount(),
                bank.getCollectCount() == null ? 0 : bank.getCollectCount(),
                bank.getStudyUserCount() == null ? 0 : bank.getStudyUserCount(),
                bank.getStatus(),
                bank.getSortNo(),
                bank.getCreatedBy(),
                bank.getCreatedBy() == null ? null : adminNameMap.get(bank.getCreatedBy()),
                actualQuestionCount == 0 && learningRecordCount == 0 && ratingCount == 0 && answerCount == 0,
                bank.getCreatedAt(),
                bank.getUpdatedAt()
        );
    }

    private String resolveCoverUrl(String coverValue) {
        if (!StringUtils.hasText(coverValue)) {
            return null;
        }
        if (fileStorage.isManagedKey(coverValue)) {
            return fileStorage.resolveUrl(coverValue);
        }
        return coverValue;
    }

    private String resolveCoverStorageKey(String coverValue) {
        if (!StringUtils.hasText(coverValue) || !fileStorage.isManagedKey(coverValue)) {
            return null;
        }
        return coverValue;
    }

    void syncQuestionCounts(Collection<Long> bankIds) {
        LinkedHashSet<Long> normalizedIds = bankIds.stream()
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return;
        }

        Map<Long, Integer> actualQuestionCountByBankId = loadRelationCountMap(kyzzQuestionMapper.selectMaps(
                new QueryWrapper<KyzzQuestion>()
                        .select("question_bank_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_bank_id", normalizedIds)
                        .groupBy("question_bank_id")));
        normalizedIds.forEach(bankId -> kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getQuestionCount, actualQuestionCountByBankId.getOrDefault(bankId, 0))));
        kyzzCacheService.evictPublicBaseCaches();
    }

    private int countActualQuestions(Long bankId) {
        Long count = kyzzQuestionMapper.selectCount(new LambdaQueryWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getQuestionBankId, bankId));
        return count == null ? 0 : count.intValue();
    }

    private boolean matchesKeyword(KyzzQuestionBank bank, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(bank.getBankCode(), normalizedKeyword)
                || contains(bank.getBankName(), normalizedKeyword)
                || contains(bank.getSubtitle(), normalizedKeyword);
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullableText(String value, int maxLength, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, message);
        }
        return trimmed;
    }

    private String normalizeCoverStorageKey(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        if (!fileStorage.isManagedKey(trimmed)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库封面必须使用系统文件服务返回的存储键");
        }
        return trimmed;
    }

    private String generateUniqueCode(String seed, String prefix, Long excludeId) {
        String base = normalizeSegment(seed);
        if (!StringUtils.hasText(base)) {
            base = prefix + "_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        }
        if (!Character.isLetter(base.charAt(0))) {
            base = prefix + "_" + base;
        }
        base = truncateCode(base, 48);

        String candidate = base;
        int suffix = 2;
        while (bankCodeExists(candidate, excludeId)) {
            String tail = "_" + suffix;
            candidate = truncateCode(base, 48 - tail.length()) + tail;
            suffix++;
        }
        return candidate;
    }

    private boolean bankCodeExists(String bankCode, Long excludeId) {
        LambdaQueryWrapper<KyzzQuestionBank> queryWrapper = new LambdaQueryWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getBankCode, bankCode);
        if (excludeId != null) {
            queryWrapper.ne(KyzzQuestionBank::getId, excludeId);
        }
        return kyzzQuestionBankMapper.selectCount(queryWrapper) > 0;
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

    private void validateBankCode(String bankCode) {
        if (!StringUtils.hasText(bankCode)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库编码不能为空");
        }
        if (!BANK_CODE_PATTERN.matcher(bankCode).matches()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库编码需为 2 到 48 位小写字母、数字、下划线或中划线，且以字母开头");
        }
    }

    private void validateBankName(String bankName) {
        if (!StringUtils.hasText(bankName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库名称不能为空");
        }
        if (bankName.length() < 2 || bankName.length() > 100) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库名称长度需保持在 2 到 100 个字符之间");
        }
    }

    private void validateDifficultyLevel(Integer difficultyLevel, boolean allowNull) {
        if (difficultyLevel == null && allowNull) {
            return;
        }
        if (difficultyLevel == null || difficultyLevel < 1 || difficultyLevel > 4) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "难度等级仅支持 1 到 4");
        }
    }

    private void validateSortNo(Integer sortNo) {
        if (sortNo == null || sortNo < 0 || sortNo > 9999) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "排序值需保持在 0 到 9999 之间");
        }
    }

    private void validateStatus(Integer status, boolean allowNull) {
        if (status == null && allowNull) {
            return;
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库状态仅支持 0 或 1");
        }
    }

    private KyzzQuestionBank requireQuestionBank(Long bankId) {
        KyzzQuestionBank bank = kyzzQuestionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题库不存在");
        }
        return bank;
    }

    private record NormalizedPayload(String bankCode,
                                     String bankName,
                                     String subtitle,
                                     String description,
                                     Long categoryId,
                                     Integer difficultyLevel,
                                     Integer sortNo,
                                     Integer status) {
    }

    private record QuestionBankAggregate(Map<Long, Integer> actualQuestionCountByBankId,
                                         Map<Long, Integer> learningRecordCountByBankId,
                                         Map<Long, Integer> ratingCountByBankId,
                                         Map<Long, Integer> answerCountByBankId) {
    }
}
