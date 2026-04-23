package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentAuthorResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentCreateRequest;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentItemResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionCommentPageResponse;
import org.example.backend.biz.kyzz.entity.KyzzComment;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.mapper.KyzzCommentMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.account.entity.AppUser;
import org.example.backend.shared.account.mapper.AppUserMapper;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: KYZZ 用户侧题目评论服务。
 */
@Service
public class KyzzQuestionCommentUserService {

    private static final String TARGET_TYPE_QUESTION = "question";
    private static final int STATUS_ACTIVE = 1;
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 30L;
    private static final int MAX_CONTENT_LENGTH = 300;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final KyzzCommentMapper kyzzCommentMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final AppUserMapper appUserMapper;
    private final LocalFileStorage localFileStorage;

    public KyzzQuestionCommentUserService(KyzzCommentMapper kyzzCommentMapper,
                                          KyzzQuestionMapper kyzzQuestionMapper,
                                          AppUserMapper appUserMapper,
                                          LocalFileStorage localFileStorage) {
        this.kyzzCommentMapper = kyzzCommentMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.appUserMapper = appUserMapper;
        this.localFileStorage = localFileStorage;
    }

    public KyzzQuestionCommentPageResponse getQuestionComments(Long userId,
                                                               Long questionId,
                                                               Long pageNo,
                                                               Long pageSize) {
        requireActiveQuestion(questionId);
        Pagination pagination = normalizePagination(pageNo, pageSize);
        Page<KyzzComment> page = kyzzCommentMapper.selectPage(
                new Page<>(pagination.pageNo(), pagination.pageSize()),
                new LambdaQueryWrapper<KyzzComment>()
                        .eq(KyzzComment::getTargetType, TARGET_TYPE_QUESTION)
                        .eq(KyzzComment::getTargetId, questionId)
                        .eq(KyzzComment::getParentId, 0L)
                        .eq(KyzzComment::getStatus, STATUS_ACTIVE)
                        .orderByDesc(KyzzComment::getCreatedAt)
                        .orderByDesc(KyzzComment::getId)
        );
        Map<Long, AppUser> authorMap = buildAuthorMap(page.getRecords());
        List<KyzzQuestionCommentItemResponse> records = page.getRecords().stream()
                .map(comment -> toItemResponse(comment, authorMap.get(comment.getUserId()), userId))
                .toList();
        return new KyzzQuestionCommentPageResponse(
                records,
                page.getCurrent(),
                page.getSize(),
                page.getCurrent() < page.getPages(),
                page.getTotal()
        );
    }

    @Transactional
    public KyzzQuestionCommentItemResponse createQuestionComment(Long userId,
                                                                 Long questionId,
                                                                 KyzzQuestionCommentCreateRequest request) {
        requireActiveQuestion(questionId);
        AppUser user = requireActiveUser(userId);
        String content = normalizeContent(request == null ? null : request.getContent());

        KyzzComment comment = new KyzzComment();
        comment.setUserId(userId);
        comment.setTargetType(TARGET_TYPE_QUESTION);
        comment.setTargetId(questionId);
        comment.setParentId(0L);
        comment.setReplyToUserId(null);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(STATUS_ACTIVE);
        kyzzCommentMapper.insert(comment);

        KyzzComment savedComment = kyzzCommentMapper.selectById(comment.getId());
        if (savedComment == null) {
            throw new BusinessException(ApiResponseCode.INTERNAL_ERROR, "评论发布失败，请稍后重试");
        }
        return toItemResponse(savedComment, user, userId);
    }

    private Map<Long, AppUser> buildAuthorMap(List<KyzzComment> comments) {
        LinkedHashSet<Long> userIds = comments.stream()
                .map(KyzzComment::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<AppUser> users = appUserMapper.selectBatchIds(userIds);
        Map<Long, AppUser> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user.getId(), user));
        return result;
    }

    private KyzzQuestionCommentItemResponse toItemResponse(KyzzComment comment, AppUser author, Long currentUserId) {
        KyzzQuestionCommentAuthorResponse authorResponse = new KyzzQuestionCommentAuthorResponse(
                author == null ? comment.getUserId() : author.getId(),
                resolveNickname(author),
                resolveAvatarUrl(author == null ? null : author.getAvatarUrl())
        );
        return new KyzzQuestionCommentItemResponse(
                comment.getId(),
                comment.getTargetId(),
                comment.getContent(),
                comment.getCreatedAt() == null ? null : comment.getCreatedAt().format(DATE_TIME_FORMATTER),
                normalizeCount(comment.getLikeCount()),
                normalizeCount(comment.getReplyCount()),
                authorResponse,
                currentUserId != null && currentUserId.equals(comment.getUserId())
        );
    }

    private int normalizeCount(Integer count) {
        return count == null || count < 0 ? 0 : count;
    }

    private AppUser requireActiveUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() != STATUS_ACTIVE) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前账号已被禁用");
        }
        return user;
    }

    private KyzzQuestion requireActiveQuestion(Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目参数非法");
        }
        KyzzQuestion question = kyzzQuestionMapper.selectOne(new LambdaQueryWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getId, questionId)
                .eq(KyzzQuestion::getStatus, STATUS_ACTIVE)
                .last("limit 1"));
        if (question == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题目不存在或暂不可评论");
        }
        return question;
    }

    private Pagination normalizePagination(Long pageNo, Long pageSize) {
        long normalizedPageNo = pageNo == null || pageNo < DEFAULT_PAGE_NO ? DEFAULT_PAGE_NO : pageNo;
        long normalizedPageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        return new Pagination(normalizedPageNo, normalizedPageSize);
    }

    private String normalizeContent(String content) {
        if (content == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "评论内容不能为空");
        }
        String value = content.trim();
        if (value.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "评论内容不能为空");
        }
        if (value.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "评论内容不能超过300个字符");
        }
        return value;
    }

    private String resolveNickname(AppUser user) {
        if (user == null || !StringUtils.hasText(user.getNickname())) {
            return "同学";
        }
        return user.getNickname().trim();
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

    private record Pagination(long pageNo, long pageSize) {
    }
}
