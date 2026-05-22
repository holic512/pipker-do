/**
 * @file KyyyReadingAnnotationUserService
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 负责阅读正文和题干标注的区间校验、持久化、删除与恢复过滤。
 * @logic 1. 校验字符区间与选中文本一致；2. 校验同内容区内无重叠标注；3. 计算 sourceTextHash 并过滤已失效标注。
 * @dependencies Mapper: KyyyUserReadingAnnotationMapper, Mapper: KyyyReadingPassageMapper, Mapper: KyyyReadingQuestionMapper
 * @index_tags 考研英语, 阅读标注服务, 区间校验, 备注持久化
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationCreateRequest;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationUpdateRequest;
import org.example.backend.biz.kyyy.entity.KyyyReadingPassage;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestion;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingAnnotation;
import org.example.backend.biz.kyyy.mapper.KyyyReadingPassageMapper;
import org.example.backend.biz.kyyy.mapper.KyyyReadingQuestionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserReadingAnnotationMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KyyyReadingAnnotationUserService {

    public static final String CONTENT_TYPE_PASSAGE_TEXT = "passage_text";
    public static final String CONTENT_TYPE_QUESTION_STEM = "question_stem";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DELETED = "deleted";
    private static final int ACTIVE_STATUS = 1;
    private static final int NOTE_MAX_LENGTH = 200;

    private final KyyyUserReadingAnnotationMapper kyyyUserReadingAnnotationMapper;
    private final KyyyReadingPassageMapper kyyyReadingPassageMapper;
    private final KyyyReadingQuestionMapper kyyyReadingQuestionMapper;

    public KyyyReadingAnnotationUserService(KyyyUserReadingAnnotationMapper kyyyUserReadingAnnotationMapper,
                                            KyyyReadingPassageMapper kyyyReadingPassageMapper,
                                            KyyyReadingQuestionMapper kyyyReadingQuestionMapper) {
        this.kyyyUserReadingAnnotationMapper = kyyyUserReadingAnnotationMapper;
        this.kyyyReadingPassageMapper = kyyyReadingPassageMapper;
        this.kyyyReadingQuestionMapper = kyyyReadingQuestionMapper;
    }

    @Transactional
    public KyyyReadingAnnotationResponse createAnnotation(Long userId, KyyyReadingAnnotationCreateRequest request) {
        AnnotationTargetContext targetContext = resolveTargetContext(request == null ? null : request.getPassageId(),
                request == null ? null : request.getQuestionId(),
                request == null ? null : request.getContentType());
        int startOffset = normalizeStartOffset(request == null ? null : request.getStartOffset());
        int endOffset = normalizeEndOffset(request == null ? null : request.getEndOffset(), startOffset);
        String selectedText = requireSelectedText(targetContext.sourceText(), startOffset, endOffset, request == null ? null : request.getSelectedText());
        String noteContent = normalizeCreateNoteContent(request == null ? null : request.getNoteContent());
        ensureNoOverlap(userId, targetContext.passage().getId(), targetContext.questionId(), targetContext.contentType(), startOffset, endOffset, null);

        LocalDateTime now = LocalDateTime.now();
        KyyyUserReadingAnnotation annotation = new KyyyUserReadingAnnotation();
        annotation.setUserId(userId);
        annotation.setPassageId(targetContext.passage().getId());
        annotation.setQuestionId(targetContext.questionId());
        annotation.setContentType(targetContext.contentType());
        annotation.setStartOffset(startOffset);
        annotation.setEndOffset(endOffset);
        annotation.setSelectedTextSnapshot(selectedText);
        annotation.setNoteContent(noteContent);
        annotation.setSourceTextHash(hashText(targetContext.sourceText()));
        annotation.setStatus(STATUS_ACTIVE);
        annotation.setCreatedAt(now);
        annotation.setUpdatedAt(now);
        kyyyUserReadingAnnotationMapper.insert(annotation);
        return toResponse(annotation);
    }

    @Transactional
    public KyyyReadingAnnotationResponse updateAnnotation(Long userId,
                                                          Long annotationId,
                                                          KyyyReadingAnnotationUpdateRequest request) {
        KyyyUserReadingAnnotation annotation = requireOwnedActiveAnnotation(userId, annotationId);
        annotation.setNoteContent(normalizeNoteContent(request == null ? null : request.getNoteContent()));
        annotation.setUpdatedAt(LocalDateTime.now());
        kyyyUserReadingAnnotationMapper.updateById(annotation);
        return toResponse(annotation);
    }

    @Transactional
    public void deleteAnnotation(Long userId, Long annotationId) {
        KyyyUserReadingAnnotation annotation = requireOwnedActiveAnnotation(userId, annotationId);
        kyyyUserReadingAnnotationMapper.update(null, new LambdaUpdateWrapper<KyyyUserReadingAnnotation>()
                .eq(KyyyUserReadingAnnotation::getId, annotationId)
                .eq(KyyyUserReadingAnnotation::getUserId, userId)
                .eq(KyyyUserReadingAnnotation::getStatus, STATUS_ACTIVE)
                .set(KyyyUserReadingAnnotation::getStatus, STATUS_DELETED)
                .set(KyyyUserReadingAnnotation::getUpdatedAt, LocalDateTime.now()));
    }

    public List<KyyyUserReadingAnnotation> loadValidActiveAnnotations(Long userId,
                                                                      KyyyReadingPassage passage,
                                                                      List<KyyyReadingQuestion> questions) {
        if (userId == null || passage == null) {
            return List.of();
        }
        List<Long> questionIds = questions == null ? List.of() : questions.stream().map(KyyyReadingQuestion::getId).toList();
        LambdaQueryWrapper<KyyyUserReadingAnnotation> queryWrapper = new LambdaQueryWrapper<KyyyUserReadingAnnotation>()
                .eq(KyyyUserReadingAnnotation::getUserId, userId)
                .eq(KyyyUserReadingAnnotation::getPassageId, passage.getId())
                .eq(KyyyUserReadingAnnotation::getStatus, STATUS_ACTIVE)
                .orderByAsc(KyyyUserReadingAnnotation::getQuestionId)
                .orderByAsc(KyyyUserReadingAnnotation::getStartOffset)
                .orderByAsc(KyyyUserReadingAnnotation::getEndOffset)
                .orderByAsc(KyyyUserReadingAnnotation::getId)
                .and(wrapper -> {
                    wrapper.isNull(KyyyUserReadingAnnotation::getQuestionId);
                    if (!questionIds.isEmpty()) {
                        wrapper.or().in(KyyyUserReadingAnnotation::getQuestionId, questionIds);
                    }
                });
        List<KyyyUserReadingAnnotation> annotations = kyyyUserReadingAnnotationMapper.selectList(queryWrapper);
        if (annotations.isEmpty()) {
            return List.of();
        }
        Map<Long, KyyyReadingQuestion> questionMap = new LinkedHashMap<>();
        if (questions != null) {
            for (KyyyReadingQuestion question : questions) {
                questionMap.put(question.getId(), question);
            }
        }
        String passageHash = hashText(passage.getPassageText());
        List<KyyyUserReadingAnnotation> validAnnotations = new ArrayList<>();
        for (KyyyUserReadingAnnotation annotation : annotations) {
            if (CONTENT_TYPE_PASSAGE_TEXT.equals(annotation.getContentType())) {
                if (Objects.equals(annotation.getSourceTextHash(), passageHash)) {
                    validAnnotations.add(annotation);
                }
                continue;
            }
            if (!CONTENT_TYPE_QUESTION_STEM.equals(annotation.getContentType())) {
                continue;
            }
            KyyyReadingQuestion question = questionMap.get(annotation.getQuestionId());
            if (question == null) {
                continue;
            }
            if (Objects.equals(annotation.getSourceTextHash(), hashText(question.getStem()))) {
                validAnnotations.add(annotation);
            }
        }
        return validAnnotations;
    }

    public KyyyReadingAnnotationResponse toResponse(KyyyUserReadingAnnotation annotation) {
        return new KyyyReadingAnnotationResponse(
                annotation.getId(),
                annotation.getContentType(),
                annotation.getStartOffset(),
                annotation.getEndOffset(),
                annotation.getSelectedTextSnapshot(),
                annotation.getNoteContent()
        );
    }

    private AnnotationTargetContext resolveTargetContext(Long passageId, Long questionId, String contentTypeValue) {
        String contentType = normalizeContentType(contentTypeValue);
        KyyyReadingPassage passage = requireActivePassage(passageId);
        if (CONTENT_TYPE_PASSAGE_TEXT.equals(contentType)) {
            if (questionId != null) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "正文标注不能携带题目ID");
            }
            return new AnnotationTargetContext(passage, null, contentType, passage.getPassageText());
        }
        KyyyReadingQuestion question = requireActiveQuestion(questionId, passage.getId());
        return new AnnotationTargetContext(passage, question.getId(), contentType, question.getStem());
    }

    private KyyyReadingPassage requireActivePassage(Long passageId) {
        if (passageId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "阅读文章不能为空");
        }
        KyyyReadingPassage passage = kyyyReadingPassageMapper.selectById(passageId);
        if (passage == null || !Objects.equals(passage.getStatus(), ACTIVE_STATUS)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "阅读文章不存在");
        }
        return passage;
    }

    private KyyyReadingQuestion requireActiveQuestion(Long questionId, Long passageId) {
        if (questionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目正文标注必须指定题目ID");
        }
        KyyyReadingQuestion question = kyyyReadingQuestionMapper.selectById(questionId);
        if (question == null
                || !Objects.equals(question.getPassageId(), passageId)
                || !Objects.equals(question.getStatus(), ACTIVE_STATUS)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "阅读题目不存在");
        }
        return question;
    }

    private KyyyUserReadingAnnotation requireOwnedActiveAnnotation(Long userId, Long annotationId) {
        KyyyUserReadingAnnotation annotation = kyyyUserReadingAnnotationMapper.selectById(annotationId);
        if (annotation == null
                || !Objects.equals(annotation.getUserId(), userId)
                || !Objects.equals(annotation.getStatus(), STATUS_ACTIVE)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "阅读标注不存在");
        }
        return annotation;
    }

    private void ensureNoOverlap(Long userId,
                                 Long passageId,
                                 Long questionId,
                                 String contentType,
                                 int startOffset,
                                 int endOffset,
                                 Long excludeAnnotationId) {
        LambdaQueryWrapper<KyyyUserReadingAnnotation> queryWrapper = new LambdaQueryWrapper<KyyyUserReadingAnnotation>()
                .eq(KyyyUserReadingAnnotation::getUserId, userId)
                .eq(KyyyUserReadingAnnotation::getPassageId, passageId)
                .eq(KyyyUserReadingAnnotation::getContentType, contentType)
                .eq(KyyyUserReadingAnnotation::getStatus, STATUS_ACTIVE)
                .lt(KyyyUserReadingAnnotation::getStartOffset, endOffset)
                .gt(KyyyUserReadingAnnotation::getEndOffset, startOffset);
        if (questionId == null) {
            queryWrapper.isNull(KyyyUserReadingAnnotation::getQuestionId);
        } else {
            queryWrapper.eq(KyyyUserReadingAnnotation::getQuestionId, questionId);
        }
        if (excludeAnnotationId != null) {
            queryWrapper.ne(KyyyUserReadingAnnotation::getId, excludeAnnotationId);
        }
        Long count = kyyyUserReadingAnnotationMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "当前内容区已有重叠标注");
        }
    }

    private int normalizeStartOffset(Integer value) {
        if (value == null || value < 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注起始位置不合法");
        }
        return value;
    }

    private int normalizeEndOffset(Integer value, int startOffset) {
        if (value == null || value <= startOffset) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注结束位置不合法");
        }
        return value;
    }

    private String normalizeContentType(String value) {
        String normalized = value == null ? "" : value.trim();
        if (CONTENT_TYPE_PASSAGE_TEXT.equals(normalized) || CONTENT_TYPE_QUESTION_STEM.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注内容类型仅支持 passage_text 或 question_stem");
    }

    private String normalizeNoteContent(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "备注内容不能为空");
        }
        if (normalized.length() > NOTE_MAX_LENGTH) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "备注内容不能超过 200 字");
        }
        return normalized;
    }

    private String normalizeCreateNoteContent(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > NOTE_MAX_LENGTH) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "备注内容不能超过 200 字");
        }
        return normalized;
    }

    private String requireSelectedText(String sourceText, int startOffset, int endOffset, String selectedTextValue) {
        String safeSourceText = sourceText == null ? "" : sourceText;
        if (endOffset > safeSourceText.length()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注区间超出正文范围");
        }
        String actualSelectedText = safeSourceText.substring(startOffset, endOffset);
        if (!StringUtils.hasText(actualSelectedText.trim())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注内容不能为空白");
        }
        String selectedText = selectedTextValue == null ? "" : selectedTextValue;
        if (!Objects.equals(actualSelectedText, selectedText)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "标注文本与选区内容不一致");
        }
        return actualSelectedText;
    }

    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not supported", exception);
        }
    }

    private record AnnotationTargetContext(KyyyReadingPassage passage,
                                           Long questionId,
                                           String contentType,
                                           String sourceText) {
    }
}
