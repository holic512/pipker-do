package org.example.backend.biz.kyzz.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminStatsResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagOptionResponse;
import org.example.backend.biz.kyzz.admin.support.KyzzAdminAccessSupport;
import org.example.backend.biz.kyzz.entity.KyzzQuestionTagRel;
import org.example.backend.biz.kyzz.entity.KyzzTag;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionTagRelMapper;
import org.example.backend.biz.kyzz.mapper.KyzzTagMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * AI 索引: KYZZ 题目标签后台服务。
 */
@Service
public class KyzzQuestionTagAdminService {

    private static final String QUESTION_TAG_TYPE = "question";
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private final KyzzTagMapper kyzzTagMapper;
    private final KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper;
    private final KyzzAdminAccessSupport kyzzAdminAccessSupport;

    public KyzzQuestionTagAdminService(KyzzTagMapper kyzzTagMapper,
                                       KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper,
                                       KyzzAdminAccessSupport kyzzAdminAccessSupport) {
        this.kyzzTagMapper = kyzzTagMapper;
        this.kyzzQuestionTagRelMapper = kyzzQuestionTagRelMapper;
        this.kyzzAdminAccessSupport = kyzzAdminAccessSupport;
    }

    public KyzzQuestionTagAdminDashboardResponse getDashboard(Long operatorId, String keyword, Integer usedStatus) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        validateUsedStatus(usedStatus, true);

        List<KyzzTag> allTags = loadAllQuestionTags();
        Map<Long, Integer> actualUseCountMap = buildActualUseCountMap(allTags.stream().map(KyzzTag::getId).toList());

        List<KyzzQuestionTagAdminItemResponse> tags = allTags.stream()
                .filter(tag -> matchesKeyword(tag, keyword))
                .filter(tag -> matchesUsedStatus(usedStatus, actualUseCountMap.getOrDefault(tag.getId(), 0)))
                .sorted(tagComparator(actualUseCountMap))
                .map(tag -> toItem(tag, actualUseCountMap.getOrDefault(tag.getId(), 0)))
                .toList();

        int usedTags = (int) allTags.stream()
                .filter(tag -> actualUseCountMap.getOrDefault(tag.getId(), 0) > 0)
                .count();
        return new KyzzQuestionTagAdminDashboardResponse(
                new KyzzQuestionTagAdminStatsResponse(
                        allTags.size(),
                        usedTags,
                        allTags.size() - usedTags,
                        countTaggedQuestionCount()
                ),
                tags
        );
    }

    @Transactional
    public KyzzQuestionTagAdminItemResponse createQuestionTag(Long operatorId, KyzzQuestionTagAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标签参数不能为空");
        }

        NormalizedTagPayload payload = normalizePayload(request);
        ensureQuestionTagNameUnique(payload.tagName(), null);

        KyzzTag tag = new KyzzTag();
        tag.setTagName(payload.tagName());
        tag.setTagType(QUESTION_TAG_TYPE);
        tag.setColor(payload.color());
        tag.setUseCount(0);
        kyzzTagMapper.insert(tag);
        return requireItem(tag.getId());
    }

    @Transactional
    public KyzzQuestionTagAdminItemResponse updateQuestionTag(Long operatorId,
                                                              Long tagId,
                                                              KyzzQuestionTagAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标签参数不能为空");
        }

        requireQuestionTag(tagId);
        NormalizedTagPayload payload = normalizePayload(request);
        ensureQuestionTagNameUnique(payload.tagName(), tagId);

        KyzzTag entity = new KyzzTag();
        entity.setId(tagId);
        entity.setTagName(payload.tagName());
        entity.setColor(payload.color());
        kyzzTagMapper.updateById(entity);
        return requireItem(tagId);
    }

    @Transactional
    public void deleteQuestionTag(Long operatorId, Long tagId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        requireQuestionTag(tagId);

        int actualUseCount = buildActualUseCountMap(List.of(tagId)).getOrDefault(tagId, 0);
        if (actualUseCount > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, buildDeleteBlockReason(actualUseCount));
        }
        kyzzTagMapper.deleteById(tagId);
    }

    @Transactional
    public KyzzQuestionTagAdminItemResponse syncUseCount(Long operatorId, Long tagId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        requireQuestionTag(tagId);
        syncUseCounts(List.of(tagId));
        return requireItem(tagId);
    }

    public List<KyzzQuestionTagOptionResponse> listQuestionTagOptions() {
        List<KyzzTag> tags = loadAllQuestionTags();
        Map<Long, Integer> actualUseCountMap = buildActualUseCountMap(tags.stream().map(KyzzTag::getId).toList());
        return tags.stream()
                .sorted(tagComparator(actualUseCountMap))
                .map(tag -> new KyzzQuestionTagOptionResponse(
                        tag.getId(),
                        tag.getTagName(),
                        tag.getColor(),
                        actualUseCountMap.getOrDefault(tag.getId(), 0)
                ))
                .toList();
    }

    public List<Long> normalizeQuestionTagIds(List<Long> incomingTagIds) {
        if (incomingTagIds == null || incomingTagIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> normalizedIds = incomingTagIds.stream()
                .filter(Objects::nonNull)
                .filter(tagId -> tagId > 0)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return List.of();
        }

        Map<Long, KyzzTag> tagMap = new HashMap<>();
        kyzzTagMapper.selectList(new LambdaQueryWrapper<KyzzTag>()
                        .in(KyzzTag::getId, normalizedIds)
                        .eq(KyzzTag::getTagType, QUESTION_TAG_TYPE))
                .forEach(tag -> tagMap.put(tag.getId(), tag));
        if (tagMap.size() != normalizedIds.size()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选标签不存在或已失效，请重新选择");
        }

        List<Long> result = new ArrayList<>();
        normalizedIds.forEach(tagId -> result.add(tagMap.get(tagId).getId()));
        return result;
    }

    @Transactional
    public void syncUseCounts(Collection<Long> tagIds) {
        LinkedHashSet<Long> normalizedIds = tagIds == null
                ? new LinkedHashSet<>()
                : tagIds.stream()
                .filter(Objects::nonNull)
                .filter(tagId -> tagId > 0)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return;
        }

        Map<Long, Integer> actualUseCountMap = buildActualUseCountMap(normalizedIds);
        kyzzTagMapper.selectList(new LambdaQueryWrapper<KyzzTag>()
                        .in(KyzzTag::getId, normalizedIds)
                        .eq(KyzzTag::getTagType, QUESTION_TAG_TYPE))
                .forEach(tag -> {
                    int actualUseCount = actualUseCountMap.getOrDefault(tag.getId(), 0);
                    if (!Objects.equals(tag.getUseCount(), actualUseCount)) {
                        KyzzTag entity = new KyzzTag();
                        entity.setId(tag.getId());
                        entity.setUseCount(actualUseCount);
                        kyzzTagMapper.updateById(entity);
                    }
                });
    }

    private KyzzQuestionTagAdminItemResponse requireItem(Long tagId) {
        KyzzTag tag = requireQuestionTag(tagId);
        int actualUseCount = buildActualUseCountMap(List.of(tagId)).getOrDefault(tagId, 0);
        return toItem(tag, actualUseCount);
    }

    private List<KyzzTag> loadAllQuestionTags() {
        return kyzzTagMapper.selectList(new LambdaQueryWrapper<KyzzTag>()
                .eq(KyzzTag::getTagType, QUESTION_TAG_TYPE)
                .orderByDesc(KyzzTag::getUpdatedAt)
                .orderByDesc(KyzzTag::getId));
    }

    private Map<Long, Integer> buildActualUseCountMap(Collection<Long> tagIds) {
        LinkedHashSet<Long> normalizedIds = tagIds == null
                ? new LinkedHashSet<>()
                : tagIds.stream()
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> result = new HashMap<>();
        kyzzQuestionTagRelMapper.selectMaps(new QueryWrapper<KyzzQuestionTagRel>()
                        .select("tag_id AS relationId", "COUNT(*) AS relationCount")
                        .in("tag_id", normalizedIds)
                        .groupBy("tag_id"))
                .forEach(row -> {
                    Object relationId = row.get("relationId");
                    Object relationCount = row.get("relationCount");
                    if (relationId instanceof Number id && relationCount instanceof Number count) {
                        result.put(id.longValue(), count.intValue());
                    }
                });
        return result;
    }

    private int countTaggedQuestionCount() {
        return kyzzQuestionTagRelMapper.selectMaps(new QueryWrapper<KyzzQuestionTagRel>()
                        .select("question_id AS questionId")
                        .groupBy("question_id"))
                .size();
    }

    private Comparator<KyzzTag> tagComparator(Map<Long, Integer> actualUseCountMap) {
        return Comparator
                .comparing((KyzzTag tag) -> actualUseCountMap.getOrDefault(tag.getId(), 0), Comparator.reverseOrder())
                .thenComparing(KyzzTag::getTagName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(KyzzTag::getId, Comparator.reverseOrder());
    }

    private KyzzQuestionTagAdminItemResponse toItem(KyzzTag tag, int actualUseCount) {
        String deleteBlockReason = actualUseCount > 0 ? buildDeleteBlockReason(actualUseCount) : null;
        return new KyzzQuestionTagAdminItemResponse(
                tag.getId(),
                tag.getTagName(),
                tag.getColor(),
                tag.getUseCount() == null ? 0 : tag.getUseCount(),
                actualUseCount,
                actualUseCount == 0,
                deleteBlockReason,
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }

    private boolean matchesKeyword(KyzzTag tag, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return tag.getTagName() != null && tag.getTagName().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private boolean matchesUsedStatus(Integer usedStatus, int actualUseCount) {
        if (usedStatus == null) {
            return true;
        }
        return usedStatus == 1 ? actualUseCount > 0 : actualUseCount == 0;
    }

    private String buildDeleteBlockReason(int actualUseCount) {
        return "标签已关联 " + actualUseCount + " 道题目，请先解除题目引用后再删除";
    }

    private NormalizedTagPayload normalizePayload(KyzzQuestionTagAdminUpsertRequest request) {
        String tagName = normalizeRequiredText(request.getTagName(), 50, "标签名称不能为空", "标签名称不能超过 50 个字符");
        String color = normalizeColor(request.getColor());
        return new NormalizedTagPayload(tagName, color);
    }

    private void ensureQuestionTagNameUnique(String tagName, Long excludeId) {
        LambdaQueryWrapper<KyzzTag> wrapper = new LambdaQueryWrapper<KyzzTag>()
                .eq(KyzzTag::getTagType, QUESTION_TAG_TYPE)
                .eq(KyzzTag::getTagName, tagName);
        if (excludeId != null) {
            wrapper.ne(KyzzTag::getId, excludeId);
        }
        if (kyzzTagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标签名称已存在");
        }
    }

    private KyzzTag requireQuestionTag(Long tagId) {
        if (tagId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标签 ID 不能为空");
        }
        KyzzTag tag = kyzzTagMapper.selectById(tagId);
        if (tag == null || !Objects.equals(tag.getTagType(), QUESTION_TAG_TYPE)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "标签不存在");
        }
        return tag;
    }

    private String normalizeRequiredText(String value, int maxLength, String emptyMessage, String lengthMessage) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, emptyMessage);
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, lengthMessage);
        }
        return normalized;
    }

    private String normalizeColor(String value) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        String upperCased = normalized.toUpperCase(Locale.ROOT);
        if (!HEX_COLOR_PATTERN.matcher(upperCased).matches()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标签颜色仅支持十六进制颜色值，例如 #3E7BFA");
        }
        return upperCased;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private void validateUsedStatus(Integer usedStatus, boolean allowNull) {
        if (usedStatus == null && allowNull) {
            return;
        }
        if (usedStatus == null || (usedStatus != 0 && usedStatus != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "引用状态仅支持 0 或 1");
        }
    }

    private record NormalizedTagPayload(String tagName, String color) {
    }
}
