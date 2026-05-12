/**
 * @file KyyyPracticeUserService
 * @project pipker-do
 * @module 考研英语 / 用户侧背词
 * @description 承担默认词库设置、首页学习统计、单词学习会话生成与学习反馈推进。
 * @logic 1. 解析默认词库并生成学习或复习会话；2. 按认识程度更新单词进度与回插队列；3. 维护首页学习统计与兼容 next-word 查询。
 * @dependencies Mapper: KyyyWordMapper, Mapper: KyyyWordPracticeSessionMapper, Mapper: KyyyWordPracticeSessionItemMapper, Support: KyyyWordPracticeSupport
 * @index_tags 考研英语, 背词服务, 学习算法, 复习会话
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeDashboardResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeFeedbackRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeNextWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionBankResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionCardResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionCompletionResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionEmptyStateResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionProgressResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSessionResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.dto.KyyyRelatedWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordSourceBankResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordProgress;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBankWordRel;
import org.example.backend.biz.kyyy.entity.KyyyWordPracticeSession;
import org.example.backend.biz.kyyy.entity.KyyyWordPracticeSessionItem;
import org.example.backend.biz.kyyy.entity.KyyyWordRelated;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordProgressMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankWordRelMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordPracticeSessionItemMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordPracticeSessionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordRelatedMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.example.backend.biz.kyyy.support.KyyyWordPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KyyyPracticeUserService {

    static final String MODE_STUDY = "study";
    static final String MODE_REVIEW = "review";

    static final String SESSION_STATUS_ACTIVE = "active";
    static final String SESSION_STATUS_COMPLETED = "completed";
    static final String SESSION_STATUS_ABANDONED = "abandoned";

    static final String RESPONSE_STATUS_ACTIVE = "active";
    static final String RESPONSE_STATUS_EMPTY = "empty";
    static final String RESPONSE_STATUS_BANK_REQUIRED = "bank_required";
    static final String RESPONSE_STATUS_COMPLETED = "completed";

    static final String ITEM_STATUS_PENDING = "pending";
    static final String ITEM_STATUS_ANSWERED = "answered";

    static final String SOURCE_TYPE_NEW = "new";
    static final String SOURCE_TYPE_REVIEW = "review";
    static final String SOURCE_TYPE_RELEARN = "relearn";

    static final String STUDY_STATUS_NEW = "new";
    static final String STUDY_STATUS_LEARNING = "learning";
    static final String STUDY_STATUS_REVIEW = "review";
    static final String STUDY_STATUS_RELEARNING = "relearning";
    static final String STUDY_STATUS_MASTERED = "mastered";

    static final String RATING_KNOW = "know";
    static final String RATING_FUZZY = "fuzzy";
    static final String RATING_UNKNOWN = "unknown";

    private static final int SESSION_CARD_LIMIT = 20;
    private static final int STUDY_SEED_LIMIT = 8;
    private static final int REVIEW_SEED_LIMIT = 10;
    private static final int UNKNOWN_REINSERT_DELAY = 2;
    private static final int FUZZY_REINSERT_DELAY = 4;
    private static final int KNOW_REINSERT_DELAY = 6;
    private static final List<Integer> KNOW_INTERVAL_DAYS = List.of(1, 3, 7, 14, 30, 60);
    private static final List<Duration> FUZZY_INTERVALS = List.of(
            Duration.ofHours(12),
            Duration.ofDays(1),
            Duration.ofDays(2),
            Duration.ofDays(4),
            Duration.ofDays(7),
            Duration.ofDays(15)
    );
    private static final KyyyHomeDashboardResponse EMPTY_HOME_DASHBOARD =
            new KyyyHomeDashboardResponse(0, 0, null, null);

    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;
    private final KyyyWordMapper kyyyWordMapper;
    private final KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper;
    private final KyyyUserWordProgressMapper kyyyUserWordProgressMapper;
    private final KyyyWordRelatedMapper kyyyWordRelatedMapper;
    private final KyyyWordPracticeSessionMapper kyyyWordPracticeSessionMapper;
    private final KyyyWordPracticeSessionItemMapper kyyyWordPracticeSessionItemMapper;
    private final KyyyWordPracticeSupport kyyyWordPracticeSupport;

    public KyyyPracticeUserService(KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper,
                                   KyyyWordMapper kyyyWordMapper,
                                   KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper,
                                   KyyyUserWordProgressMapper kyyyUserWordProgressMapper,
                                   KyyyWordRelatedMapper kyyyWordRelatedMapper,
                                   KyyyWordPracticeSessionMapper kyyyWordPracticeSessionMapper,
                                   KyyyWordPracticeSessionItemMapper kyyyWordPracticeSessionItemMapper,
                                   KyyyWordPracticeSupport kyyyWordPracticeSupport) {
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
        this.kyyyWordMapper = kyyyWordMapper;
        this.kyyyWordBankWordRelMapper = kyyyWordBankWordRelMapper;
        this.kyyyUserWordProgressMapper = kyyyUserWordProgressMapper;
        this.kyyyWordRelatedMapper = kyyyWordRelatedMapper;
        this.kyyyWordPracticeSessionMapper = kyyyWordPracticeSessionMapper;
        this.kyyyWordPracticeSessionItemMapper = kyyyWordPracticeSessionItemMapper;
        this.kyyyWordPracticeSupport = kyyyWordPracticeSupport;
    }

    public KyyyPracticeSettingResponse getSettings(Long userId) {
        KyyyUserPracticeSetting setting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = setting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, setting.getDefaultWordBankId());
        return toPracticeSettingResponse(setting, defaultWordBank);
    }

    public KyyyHomeDashboardResponse getHomeDashboard(Long userId) {
        KyyyWordBank defaultWordBank = loadDefaultWordBank(userId);
        if (defaultWordBank == null) {
            return EMPTY_HOME_DASHBOARD;
        }
        List<Long> wordIds = loadOrderedActiveWordIds(defaultWordBank.getId());
        if (wordIds.isEmpty()) {
            return new KyyyHomeDashboardResponse(0, 0, defaultWordBank.getId(), defaultWordBank.getBankName());
        }
        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(userId, wordIds);
        LocalDateTime now = LocalDateTime.now();
        int studyCount = 0;
        int reviewCount = 0;
        for (Long wordId : wordIds) {
            KyyyUserWordProgress progress = progressMap.get(wordId);
            if (isStudyCandidate(progress)) {
                studyCount++;
            }
            if (isReviewCandidate(progress, now)) {
                reviewCount++;
            }
        }
        return new KyyyHomeDashboardResponse(
                studyCount,
                reviewCount,
                defaultWordBank.getId(),
                defaultWordBank.getBankName()
        );
    }

    public KyyyPracticeNextWordResponse getNextWord(Long userId) {
        KyyyPracticeSessionResponse studySession = getSession(userId, MODE_STUDY, false);
        if (hasActiveCard(studySession)) {
            return toNextWordResponse(studySession.getCurrentCard());
        }
        KyyyPracticeSessionResponse reviewSession = getSession(userId, MODE_REVIEW, false);
        if (hasActiveCard(reviewSession)) {
            return toNextWordResponse(reviewSession.getCurrentCard());
        }
        if (RESPONSE_STATUS_BANK_REQUIRED.equals(studySession.getStatus())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先选择默认词库后再开始背词");
        }
        throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前默认词库暂无可学习单词");
    }

    public KyyyPracticeSessionResponse getSession(Long userId, String modeValue, Boolean freshAttempt) {
        String mode = normalizeMode(modeValue);
        KyyyWordBank defaultWordBank = loadDefaultWordBank(userId);
        if (defaultWordBank == null) {
            return buildBankRequiredResponse(mode);
        }

        if (Boolean.TRUE.equals(freshAttempt)) {
            abandonActiveSessions(userId, defaultWordBank.getId(), mode);
        }

        KyyyWordPracticeSession activeSession = loadLatestActiveSession(userId, defaultWordBank.getId(), mode);
        if (activeSession != null) {
            SessionContext sessionContext = loadSessionContext(activeSession);
            if (sessionContext.pendingItems().isEmpty()) {
                completeSession(activeSession, sessionContext.allItems(), LocalDateTime.now());
                return buildCompletedSessionResponse(activeSession, defaultWordBank, sessionContext);
            }
            return buildActiveSessionResponse(activeSession, defaultWordBank, sessionContext);
        }

        return createNewSession(userId, defaultWordBank, mode);
    }

    @Transactional
    public KyyyPracticeSessionResponse submitFeedback(Long userId,
                                                      Long sessionId,
                                                      KyyyPracticeFeedbackRequest request) {
        if (sessionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "学习会话不能为空");
        }
        if (request == null || request.getWordId() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前单词不能为空");
        }

        KyyyWordPracticeSession session = kyyyWordPracticeSessionMapper.selectById(sessionId);
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "学习会话不存在");
        }
        if (!SESSION_STATUS_ACTIVE.equals(session.getStatus())) {
            SessionContext finishedContext = loadSessionContext(session);
            KyyyWordBank defaultWordBank = kyyyWordPracticeSupport.requireActiveWordBank(session.getWordBankId());
            return buildCompletedSessionResponse(session, defaultWordBank, finishedContext);
        }

        KyyyWordBank wordBank = kyyyWordPracticeSupport.requireActiveWordBank(session.getWordBankId());
        String rating = normalizeRating(request.getRating());
        SessionContext sessionContext = loadSessionContext(session);
        if (sessionContext.pendingItems().isEmpty()) {
            completeSession(session, sessionContext.allItems(), LocalDateTime.now());
            return buildCompletedSessionResponse(session, wordBank, sessionContext);
        }

        KyyyWordPracticeSessionItem currentItem = sessionContext.pendingItems().get(0);
        if (!Objects.equals(currentItem.getWordId(), request.getWordId())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前单词已变更，请刷新后重试");
        }

        LocalDateTime answeredAt = LocalDateTime.now();
        KyyyUserWordProgress progress = loadOrInitProgress(userId, currentItem.getWordId(), sessionContext.progressMap().get(currentItem.getWordId()));
        applyProgressUpdate(progress, session.getMode(), rating, answeredAt);
        saveProgress(progress);

        currentItem.setStatus(ITEM_STATUS_ANSWERED);
        currentItem.setRating(rating);
        currentItem.setShownAt(currentItem.getShownAt() == null ? answeredAt : currentItem.getShownAt());
        currentItem.setAnsweredAt(answeredAt);
        kyyyWordPracticeSessionItemMapper.updateById(currentItem);

        incrementSessionCounters(session, rating, answeredAt);
        kyyyWordPracticeSessionMapper.updateById(session);
        kyyyWordPracticeSupport.touchUserWordBankPractice(userId, wordBank.getId(), answeredAt);

        Long insertedItemId = null;
        Integer insertedDelay = null;
        if (shouldReinsert(session, progress, rating)) {
            insertedDelay = resolveReinsertDelay(rating);
            if (countSessionItems(session.getId()) < safeInt(session.getTotalCards(), SESSION_CARD_LIMIT)) {
                KyyyWordPracticeSessionItem reinsertion = createReinsertItem(
                        session,
                        currentItem,
                        session.getCompletedCards(),
                        insertedDelay
                );
                kyyyWordPracticeSessionItemMapper.insert(reinsertion);
                insertedItemId = reinsertion.getId();
            } else if (RATING_UNKNOWN.equals(rating)) {
                progress.setNextReviewAt(answeredAt.plusMinutes(30));
                saveProgress(progress);
            }
        }

        reorderPendingItems(session.getId(), safeInt(session.getCompletedCards(), 0), insertedItemId, insertedDelay);
        KyyyWordPracticeSession refreshedSession = kyyyWordPracticeSessionMapper.selectById(session.getId());
        SessionContext refreshedContext = loadSessionContext(refreshedSession);
        if (refreshedContext.pendingItems().isEmpty()) {
            completeSession(refreshedSession, refreshedContext.allItems(), answeredAt);
            SessionContext completedContext = loadSessionContext(refreshedSession);
            return buildCompletedSessionResponse(refreshedSession, wordBank, completedContext);
        }
        return buildActiveSessionResponse(refreshedSession, wordBank, refreshedContext);
    }

    @Transactional
    public KyyyPracticeSettingResponse updateSettings(Long userId, KyyyPracticeSettingRequest request) {
        KyyyUserPracticeSetting existing = loadPracticeSetting(userId);
        KyyyWordBank requestedDefaultWordBank = request == null || request.getDefaultWordBankId() == null
                ? null
                : kyyyWordPracticeSupport.requireActiveSelectedWordBank(userId, request.getDefaultWordBankId());
        if (existing == null) {
            KyyyUserPracticeSetting setting = new KyyyUserPracticeSetting();
            setting.setUserId(userId);
            setting.setExamDirection(KyyyExamDirectionSupport.normalizeOrDefault(
                    request == null ? null : request.getExamDirection()
            ));
            setting.setDefaultWordBankId(requestedDefaultWordBank == null ? null : requestedDefaultWordBank.getId());
            kyyyUserPracticeSettingMapper.insert(setting);
            KyyyUserPracticeSetting normalizedSetting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
            KyyyWordBank defaultWordBank = normalizedSetting == null
                    ? null
                    : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, normalizedSetting.getDefaultWordBankId());
            return toPracticeSettingResponse(normalizedSetting == null ? setting : normalizedSetting, defaultWordBank);
        }

        if (request != null) {
            if (request.getExamDirection() != null) {
                existing.setExamDirection(KyyyExamDirectionSupport.normalize(request.getExamDirection()));
            }
            if (request.getDefaultWordBankId() != null) {
                existing.setDefaultWordBankId(requestedDefaultWordBank.getId());
            }
            kyyyUserPracticeSettingMapper.updateById(existing);
        }
        KyyyUserPracticeSetting normalizedSetting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = normalizedSetting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, normalizedSetting.getDefaultWordBankId());
        return toPracticeSettingResponse(normalizedSetting == null ? existing : normalizedSetting, defaultWordBank);
    }

    private KyyyPracticeSessionResponse createNewSession(Long userId, KyyyWordBank defaultWordBank, String mode) {
        List<Long> candidateWordIds = MODE_REVIEW.equals(mode)
                ? loadReviewCandidateWordIds(userId, defaultWordBank.getId(), LocalDateTime.now())
                : loadStudyCandidateWordIds(userId, defaultWordBank.getId());
        int seedLimit = MODE_REVIEW.equals(mode) ? REVIEW_SEED_LIMIT : STUDY_SEED_LIMIT;
        List<Long> seedWordIds = candidateWordIds.stream().limit(seedLimit).toList();
        if (seedWordIds.isEmpty()) {
            return buildEmptyResponse(mode, defaultWordBank);
        }

        LocalDateTime now = LocalDateTime.now();
        KyyyWordPracticeSession session = new KyyyWordPracticeSession();
        session.setUserId(userId);
        session.setWordBankId(defaultWordBank.getId());
        session.setMode(mode);
        session.setStatus(SESSION_STATUS_ACTIVE);
        session.setTotalCards(SESSION_CARD_LIMIT);
        session.setCompletedCards(0);
        session.setKnownCount(0);
        session.setFuzzyCount(0);
        session.setUnknownCount(0);
        session.setStartedAt(now);
        session.setLastAnsweredAt(null);
        session.setFinishedAt(null);
        kyyyWordPracticeSessionMapper.insert(session);

        for (int index = 0; index < seedWordIds.size(); index++) {
            KyyyWordPracticeSessionItem item = new KyyyWordPracticeSessionItem();
            item.setSessionId(session.getId());
            item.setWordId(seedWordIds.get(index));
            item.setSourceType(MODE_REVIEW.equals(mode) ? SOURCE_TYPE_REVIEW : SOURCE_TYPE_NEW);
            item.setRoundNo(1);
            item.setQueueOrder(index + 1);
            item.setStatus(ITEM_STATUS_PENDING);
            item.setRating(null);
            item.setScheduledAfterIndex(index + 1);
            kyyyWordPracticeSessionItemMapper.insert(item);
        }

        SessionContext sessionContext = loadSessionContext(session);
        return buildActiveSessionResponse(session, defaultWordBank, sessionContext);
    }

    private void abandonActiveSessions(Long userId, Long wordBankId, String mode) {
        LocalDateTime now = LocalDateTime.now();
        kyyyWordPracticeSessionMapper.update(null, new LambdaUpdateWrapper<KyyyWordPracticeSession>()
                .eq(KyyyWordPracticeSession::getUserId, userId)
                .eq(KyyyWordPracticeSession::getWordBankId, wordBankId)
                .eq(KyyyWordPracticeSession::getMode, mode)
                .eq(KyyyWordPracticeSession::getStatus, SESSION_STATUS_ACTIVE)
                .set(KyyyWordPracticeSession::getStatus, SESSION_STATUS_ABANDONED)
                .set(KyyyWordPracticeSession::getFinishedAt, now));
    }

    private KyyyWordPracticeSession loadLatestActiveSession(Long userId, Long wordBankId, String mode) {
        return kyyyWordPracticeSessionMapper.selectOne(new LambdaQueryWrapper<KyyyWordPracticeSession>()
                .eq(KyyyWordPracticeSession::getUserId, userId)
                .eq(KyyyWordPracticeSession::getWordBankId, wordBankId)
                .eq(KyyyWordPracticeSession::getMode, mode)
                .eq(KyyyWordPracticeSession::getStatus, SESSION_STATUS_ACTIVE)
                .orderByDesc(KyyyWordPracticeSession::getUpdatedAt)
                .orderByDesc(KyyyWordPracticeSession::getId)
                .last("limit 1"));
    }

    private SessionContext loadSessionContext(KyyyWordPracticeSession session) {
        List<KyyyWordPracticeSessionItem> items = kyyyWordPracticeSessionItemMapper.selectList(new LambdaQueryWrapper<KyyyWordPracticeSessionItem>()
                .eq(KyyyWordPracticeSessionItem::getSessionId, session.getId())
                .orderByAsc(KyyyWordPracticeSessionItem::getQueueOrder)
                .orderByAsc(KyyyWordPracticeSessionItem::getId));
        List<KyyyWordPracticeSessionItem> pendingItems = items.stream()
                .filter(item -> ITEM_STATUS_PENDING.equals(item.getStatus()))
                .toList();
        List<Long> wordIds = items.stream()
                .map(KyyyWordPracticeSessionItem::getWordId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, KyyyWord> wordMap = loadActiveWordMap(wordIds);
        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(session.getUserId(), wordIds);
        return new SessionContext(items, pendingItems, wordMap, progressMap);
    }

    private List<Long> loadStudyCandidateWordIds(Long userId, Long wordBankId) {
        List<Long> orderedWordIds = loadOrderedActiveWordIds(wordBankId);
        if (orderedWordIds.isEmpty()) {
            return List.of();
        }
        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(userId, orderedWordIds);
        return orderedWordIds.stream()
                .filter(wordId -> isStudyCandidate(progressMap.get(wordId)))
                .toList();
    }

    private List<Long> loadReviewCandidateWordIds(Long userId, Long wordBankId, LocalDateTime now) {
        List<Long> orderedWordIds = loadOrderedActiveWordIds(wordBankId);
        if (orderedWordIds.isEmpty()) {
            return List.of();
        }
        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(userId, orderedWordIds);
        return orderedWordIds.stream()
                .filter(wordId -> isReviewCandidate(progressMap.get(wordId), now))
                .sorted(Comparator
                        .comparing((Long wordId) -> !STUDY_STATUS_RELEARNING.equals(normalizeStudyStatus(progressMap.get(wordId) == null
                                ? null
                                : progressMap.get(wordId).getStudyStatus())))
                        .thenComparing(wordId -> progressMap.get(wordId) == null ? null : progressMap.get(wordId).getNextReviewAt(),
                                Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(wordId -> progressMap.get(wordId) == null ? 0 : -safeInt(progressMap.get(wordId).getWrongCount(), 0))
                        .thenComparing(wordId -> progressMap.get(wordId) == null ? null : progressMap.get(wordId).getLastStudiedAt(),
                                Comparator.nullsFirst(LocalDateTime::compareTo)))
                .toList();
    }

    private List<Long> loadOrderedActiveWordIds(Long wordBankId) {
        List<KyyyWordBankWordRel> relations = kyyyWordBankWordRelMapper.selectList(new LambdaQueryWrapper<KyyyWordBankWordRel>()
                .eq(KyyyWordBankWordRel::getWordBankId, wordBankId)
                .orderByAsc(KyyyWordBankWordRel::getSortNo)
                .orderByAsc(KyyyWordBankWordRel::getId));
        if (relations.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> orderedWordIds = new LinkedHashSet<>();
        relations.forEach(item -> orderedWordIds.add(item.getWordId()));
        Map<Long, KyyyWord> activeWordMap = loadActiveWordMap(new ArrayList<>(orderedWordIds));
        return orderedWordIds.stream()
                .filter(activeWordMap::containsKey)
                .toList();
    }

    private Map<Long, KyyyWord> loadActiveWordMap(List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyWord> activeWords = kyyyWordMapper.selectList(new LambdaQueryWrapper<KyyyWord>()
                .in(KyyyWord::getId, wordIds)
                .eq(KyyyWord::getStatus, 1));
        Map<Long, KyyyWord> wordMap = new LinkedHashMap<>();
        activeWords.forEach(item -> wordMap.putIfAbsent(item.getId(), item));
        return wordMap;
    }

    private Map<Long, KyyyUserWordProgress> buildUserWordProgressMap(Long userId, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyUserWordProgress> progresses = kyyyUserWordProgressMapper.selectList(new LambdaQueryWrapper<KyyyUserWordProgress>()
                .eq(KyyyUserWordProgress::getUserId, userId)
                .in(KyyyUserWordProgress::getWordId, wordIds)
                .orderByDesc(KyyyUserWordProgress::getUpdatedAt)
                .orderByDesc(KyyyUserWordProgress::getId));
        Map<Long, KyyyUserWordProgress> progressMap = new LinkedHashMap<>();
        progresses.forEach(item -> progressMap.putIfAbsent(item.getWordId(), item));
        return progressMap;
    }

    private boolean isStudyCandidate(KyyyUserWordProgress progress) {
        if (progress == null) {
            return true;
        }
        String status = normalizeStudyStatus(progress.getStudyStatus());
        return STUDY_STATUS_NEW.equals(status) || STUDY_STATUS_LEARNING.equals(status);
    }

    private boolean isReviewCandidate(KyyyUserWordProgress progress, LocalDateTime now) {
        if (progress == null) {
            return false;
        }
        String status = normalizeStudyStatus(progress.getStudyStatus());
        if (STUDY_STATUS_RELEARNING.equals(status)) {
            return true;
        }
        return progress.getNextReviewAt() != null && !progress.getNextReviewAt().isAfter(now);
    }

    private KyyyPracticeSessionResponse buildActiveSessionResponse(KyyyWordPracticeSession session,
                                                                   KyyyWordBank wordBank,
                                                                   SessionContext sessionContext) {
        KyyyWordPracticeSessionItem currentItem = sessionContext.pendingItems().get(0);
        KyyyWord currentWord = sessionContext.wordMap().get(currentItem.getWordId());
        KyyyUserWordProgress currentProgress = sessionContext.progressMap().get(currentItem.getWordId());
        return new KyyyPracticeSessionResponse(
                session.getId(),
                session.getMode(),
                RESPONSE_STATUS_ACTIVE,
                toSessionBankResponse(wordBank),
                buildProgressSummary(session, sessionContext.pendingItems().size()),
                buildCardResponse(currentItem, currentWord, currentProgress, wordBank),
                null,
                null
        );
    }

    private KyyyPracticeSessionResponse buildCompletedSessionResponse(KyyyWordPracticeSession session,
                                                                      KyyyWordBank wordBank,
                                                                      SessionContext sessionContext) {
        return new KyyyPracticeSessionResponse(
                session.getId(),
                session.getMode(),
                RESPONSE_STATUS_COMPLETED,
                toSessionBankResponse(wordBank),
                buildProgressSummary(session, 0),
                null,
                null,
                buildCompletionSummary(session, sessionContext)
        );
    }

    private KyyyPracticeSessionResponse buildEmptyResponse(String mode, KyyyWordBank wordBank) {
        String title = MODE_REVIEW.equals(mode) ? "当前没有到期复习单词" : "当前词库的新词已学完";
        String description = MODE_REVIEW.equals(mode)
                ? "可以先学习新词，系统会在后续自动安排复习。"
                : "先去复习到期单词，能把刚学过的词记得更稳。";
        String actionText = MODE_REVIEW.equals(mode) ? "去学习" : "去复习";
        String suggestedMode = MODE_REVIEW.equals(mode) ? MODE_STUDY : MODE_REVIEW;
        return new KyyyPracticeSessionResponse(
                null,
                mode,
                RESPONSE_STATUS_EMPTY,
                toSessionBankResponse(wordBank),
                new KyyyPracticeSessionProgressResponse(SESSION_CARD_LIMIT, 0, 0, 0, 0, 0, 0),
                null,
                new KyyyPracticeSessionEmptyStateResponse(title, description, actionText, suggestedMode),
                null
        );
    }

    private KyyyPracticeSessionResponse buildBankRequiredResponse(String mode) {
        return new KyyyPracticeSessionResponse(
                null,
                mode,
                RESPONSE_STATUS_BANK_REQUIRED,
                null,
                new KyyyPracticeSessionProgressResponse(SESSION_CARD_LIMIT, 0, 0, 0, 0, 0, 0),
                null,
                new KyyyPracticeSessionEmptyStateResponse(
                        "先选默认词库",
                        "学习和复习都会围绕默认词库进行，先去词库页确认一个默认词库。",
                        "去选词库",
                        mode
                ),
                null
        );
    }

    private KyyyPracticeSessionProgressResponse buildProgressSummary(KyyyWordPracticeSession session, int pendingCount) {
        int completedCards = safeInt(session.getCompletedCards(), 0);
        int totalCards = safeInt(session.getTotalCards(), SESSION_CARD_LIMIT);
        int currentIndex = pendingCount > 0 ? Math.min(totalCards, completedCards + 1) : completedCards;
        return new KyyyPracticeSessionProgressResponse(
                totalCards,
                completedCards,
                pendingCount,
                currentIndex,
                safeInt(session.getKnownCount(), 0),
                safeInt(session.getFuzzyCount(), 0),
                safeInt(session.getUnknownCount(), 0)
        );
    }

    private KyyyPracticeSessionCompletionResponse buildCompletionSummary(KyyyWordPracticeSession session,
                                                                        SessionContext sessionContext) {
        LocalDateTime soonBoundary = LocalDateTime.now().plusMinutes(30);
        List<Long> newSourceWordIds = sessionContext.allItems().stream()
                .filter(item -> SOURCE_TYPE_NEW.equals(item.getSourceType()))
                .map(KyyyWordPracticeSessionItem::getWordId)
                .distinct()
                .toList();
        int passedNewCount = (int) newSourceWordIds.stream()
                .map(sessionContext.progressMap()::get)
                .filter(Objects::nonNull)
                .filter(progress -> List.of(STUDY_STATUS_REVIEW, STUDY_STATUS_MASTERED).contains(normalizeStudyStatus(progress.getStudyStatus())))
                .count();
        int dueSoonCount = (int) sessionContext.progressMap().values().stream()
                .filter(Objects::nonNull)
                .filter(progress -> STUDY_STATUS_RELEARNING.equals(normalizeStudyStatus(progress.getStudyStatus()))
                        || (progress.getNextReviewAt() != null && !progress.getNextReviewAt().isAfter(soonBoundary)))
                .count();
        String primaryActionMode = MODE_STUDY.equals(session.getMode()) ? MODE_REVIEW : MODE_STUDY;
        return new KyyyPracticeSessionCompletionResponse(
                passedNewCount,
                dueSoonCount,
                safeInt(session.getUnknownCount(), 0),
                primaryActionMode
        );
    }

    private KyyyPracticeSessionCardResponse buildCardResponse(KyyyWordPracticeSessionItem item,
                                                              KyyyWord word,
                                                              KyyyUserWordProgress progress,
                                                              KyyyWordBank wordBank) {
        if (word == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前单词已失效，请重新开始学习");
        }
        return new KyyyPracticeSessionCardResponse(
                word.getId(),
                word.getWordText(),
                word.getPhoneticUs(),
                word.getPhoneticUk(),
                word.getPartOfSpeech(),
                word.getMeaningCn(),
                word.getExampleSentence(),
                word.getExampleTranslation(),
                word.getDifficultyLevel(),
                normalizeStudyStatus(progress == null ? null : progress.getStudyStatus()),
                progress == null ? 0 : safeInt(progress.getMasteryLevel(), 0),
                progress == null ? 0 : safeInt(progress.getMemoryStage(), 0),
                progress == null ? 0 : safeInt(progress.getLearningStep(), 0),
                progress == null ? 0 : safeInt(progress.getReviewCount(), 0),
                progress == null ? 0 : safeInt(progress.getCorrectCount(), 0),
                progress == null ? 0 : safeInt(progress.getWrongCount(), 0),
                progress == null ? null : progress.getLastResult(),
                progress == null ? null : progress.getLastStudiedAt(),
                progress == null ? null : progress.getNextReviewAt(),
                item.getSourceType(),
                safeInt(item.getRoundNo(), 1),
                safeInt(item.getQueueOrder(), 0),
                List.of(new KyyyWordSourceBankResponse(wordBank.getId(), wordBank.getBankCode(), wordBank.getBankName())),
                buildRelatedWordResponses(word.getId())
        );
    }

    private List<KyyyRelatedWordResponse> buildRelatedWordResponses(Long wordId) {
        List<KyyyWordRelated> relations = kyyyWordRelatedMapper.selectList(new LambdaQueryWrapper<KyyyWordRelated>()
                .eq(KyyyWordRelated::getWordId, wordId)
                .eq(KyyyWordRelated::getStatus, 1)
                .orderByAsc(KyyyWordRelated::getSortNo)
                .orderByAsc(KyyyWordRelated::getId));
        return relations.stream()
                .map(item -> new KyyyRelatedWordResponse(
                        item.getId(),
                        item.getRelatedWordId(),
                        item.getRelatedWordText(),
                        item.getMeaningCn(),
                        item.getRelationType()
                ))
                .toList();
    }

    private KyyyWordPracticeSessionItem createReinsertItem(KyyyWordPracticeSession session,
                                                           KyyyWordPracticeSessionItem currentItem,
                                                           Integer completedCards,
                                                           Integer delay) {
        KyyyWordPracticeSessionItem reinsertion = new KyyyWordPracticeSessionItem();
        reinsertion.setSessionId(session.getId());
        reinsertion.setWordId(currentItem.getWordId());
        reinsertion.setSourceType(SOURCE_TYPE_RELEARN);
        reinsertion.setRoundNo(safeInt(currentItem.getRoundNo(), 1) + 1);
        reinsertion.setQueueOrder(0);
        reinsertion.setStatus(ITEM_STATUS_PENDING);
        reinsertion.setRating(null);
        reinsertion.setScheduledAfterIndex(safeInt(completedCards, 0) + safeInt(delay, 0));
        return reinsertion;
    }

    private void reorderPendingItems(Long sessionId,
                                     Integer completedCards,
                                     Long insertedItemId,
                                     Integer insertedDelay) {
        List<KyyyWordPracticeSessionItem> pendingItems = new ArrayList<>(kyyyWordPracticeSessionItemMapper.selectList(
                new LambdaQueryWrapper<KyyyWordPracticeSessionItem>()
                        .eq(KyyyWordPracticeSessionItem::getSessionId, sessionId)
                        .eq(KyyyWordPracticeSessionItem::getStatus, ITEM_STATUS_PENDING)
                        .orderByAsc(KyyyWordPracticeSessionItem::getQueueOrder)
                        .orderByAsc(KyyyWordPracticeSessionItem::getId)
        ));
        if (insertedItemId != null) {
            KyyyWordPracticeSessionItem insertedItem = null;
            for (KyyyWordPracticeSessionItem item : pendingItems) {
                if (Objects.equals(item.getId(), insertedItemId)) {
                    insertedItem = item;
                    break;
                }
            }
            if (insertedItem != null) {
                pendingItems.remove(insertedItem);
                int insertIndex = Math.min(Math.max(safeInt(insertedDelay, 0), 0), pendingItems.size());
                pendingItems.add(insertIndex, insertedItem);
            }
        }

        int baseOrder = safeInt(completedCards, 0) + 1;
        for (int index = 0; index < pendingItems.size(); index++) {
            KyyyWordPracticeSessionItem item = pendingItems.get(index);
            int nextQueueOrder = baseOrder + index;
            if (!Objects.equals(item.getQueueOrder(), nextQueueOrder)) {
                item.setQueueOrder(nextQueueOrder);
                kyyyWordPracticeSessionItemMapper.updateById(item);
            }
        }
    }

    private void incrementSessionCounters(KyyyWordPracticeSession session,
                                          String rating,
                                          LocalDateTime answeredAt) {
        session.setCompletedCards(safeInt(session.getCompletedCards(), 0) + 1);
        session.setLastAnsweredAt(answeredAt);
        if (RATING_KNOW.equals(rating)) {
            session.setKnownCount(safeInt(session.getKnownCount(), 0) + 1);
            return;
        }
        if (RATING_FUZZY.equals(rating)) {
            session.setFuzzyCount(safeInt(session.getFuzzyCount(), 0) + 1);
            return;
        }
        session.setUnknownCount(safeInt(session.getUnknownCount(), 0) + 1);
    }

    private void completeSession(KyyyWordPracticeSession session,
                                 List<KyyyWordPracticeSessionItem> items,
                                 LocalDateTime finishedAt) {
        session.setStatus(SESSION_STATUS_COMPLETED);
        session.setFinishedAt(finishedAt);
        int completedCards = (int) items.stream()
                .filter(item -> ITEM_STATUS_ANSWERED.equals(item.getStatus()))
                .count();
        session.setCompletedCards(completedCards);
        kyyyWordPracticeSessionMapper.updateById(session);
    }

    private void applyProgressUpdate(KyyyUserWordProgress progress,
                                     String mode,
                                     String rating,
                                     LocalDateTime answeredAt) {
        progress.setStudyStatus(normalizeStudyStatus(progress.getStudyStatus()));
        progress.setMasteryLevel(safeInt(progress.getMasteryLevel(), 0));
        progress.setMemoryStage(clampStage(progress.getMemoryStage()));
        progress.setLearningStep(safeInt(progress.getLearningStep(), 0));
        progress.setLapseCount(safeInt(progress.getLapseCount(), 0));
        progress.setConsecutiveKnownCount(safeInt(progress.getConsecutiveKnownCount(), 0));
        progress.setReviewCount(safeInt(progress.getReviewCount(), 0) + 1);
        progress.setCorrectCount(safeInt(progress.getCorrectCount(), 0));
        progress.setWrongCount(safeInt(progress.getWrongCount(), 0));
        progress.setLastResult(rating);
        progress.setLastStudiedAt(answeredAt);

        if (MODE_REVIEW.equals(mode)) {
            applyReviewProgress(progress, rating, answeredAt);
            return;
        }
        applyStudyProgress(progress, rating, answeredAt);
    }

    private void applyStudyProgress(KyyyUserWordProgress progress,
                                    String rating,
                                    LocalDateTime answeredAt) {
        if (RATING_KNOW.equals(rating)) {
            progress.setCorrectCount(safeInt(progress.getCorrectCount(), 0) + 1);
            int nextKnownCount = safeInt(progress.getConsecutiveKnownCount(), 0) + 1;
            progress.setConsecutiveKnownCount(nextKnownCount);
            if (nextKnownCount >= 2) {
                promoteToReview(progress, 0, answeredAt);
            } else {
                progress.setStudyStatus(STUDY_STATUS_LEARNING);
                progress.setLearningStep(1);
                progress.setMasteryLevel(2);
                progress.setNextReviewAt(null);
            }
            return;
        }

        progress.setWrongCount(safeInt(progress.getWrongCount(), 0) + 1);
        progress.setConsecutiveKnownCount(0);
        if (RATING_FUZZY.equals(rating)) {
            progress.setStudyStatus(STUDY_STATUS_LEARNING);
            progress.setLearningStep(1);
            progress.setMasteryLevel(1);
            progress.setNextReviewAt(null);
            return;
        }

        progress.setStudyStatus(STUDY_STATUS_RELEARNING);
        progress.setLearningStep(0);
        progress.setMemoryStage(0);
        progress.setLapseCount(safeInt(progress.getLapseCount(), 0) + 1);
        progress.setMasteryLevel(1);
        progress.setNextReviewAt(answeredAt.plusMinutes(30));
    }

    private void applyReviewProgress(KyyyUserWordProgress progress,
                                     String rating,
                                     LocalDateTime answeredAt) {
        if (RATING_KNOW.equals(rating)) {
            progress.setCorrectCount(safeInt(progress.getCorrectCount(), 0) + 1);
            progress.setConsecutiveKnownCount(safeInt(progress.getConsecutiveKnownCount(), 0) + 1);
            int currentStage = clampStage(progress.getMemoryStage());
            String currentStatus = normalizeStudyStatus(progress.getStudyStatus());
            int nextStage = STUDY_STATUS_RELEARNING.equals(currentStatus)
                    ? 0
                    : Math.min(currentStage + 1, KNOW_INTERVAL_DAYS.size() - 1);
            promoteToReview(progress, nextStage, answeredAt);
            return;
        }

        progress.setWrongCount(safeInt(progress.getWrongCount(), 0) + 1);
        progress.setConsecutiveKnownCount(0);
        if (RATING_FUZZY.equals(rating)) {
            int fuzzyStage = Math.max(clampStage(progress.getMemoryStage()) - 1, 0);
            progress.setMemoryStage(fuzzyStage);
            progress.setStudyStatus(STUDY_STATUS_RELEARNING);
            progress.setLearningStep(0);
            progress.setMasteryLevel(2);
            progress.setNextReviewAt(answeredAt.plus(FUZZY_INTERVALS.get(fuzzyStage)));
            return;
        }

        progress.setStudyStatus(STUDY_STATUS_RELEARNING);
        progress.setLearningStep(0);
        progress.setMemoryStage(0);
        progress.setLapseCount(safeInt(progress.getLapseCount(), 0) + 1);
        progress.setMasteryLevel(1);
        progress.setNextReviewAt(answeredAt.plusMinutes(30));
    }

    private void promoteToReview(KyyyUserWordProgress progress,
                                 int memoryStage,
                                 LocalDateTime answeredAt) {
        int normalizedStage = Math.min(Math.max(memoryStage, 0), KNOW_INTERVAL_DAYS.size() - 1);
        progress.setStudyStatus(normalizedStage >= KNOW_INTERVAL_DAYS.size() - 1 ? STUDY_STATUS_MASTERED : STUDY_STATUS_REVIEW);
        progress.setMasteryLevel(normalizedStage >= 3 ? 4 : 3);
        progress.setMemoryStage(normalizedStage);
        progress.setLearningStep(2);
        progress.setNextReviewAt(answeredAt.plusDays(KNOW_INTERVAL_DAYS.get(normalizedStage)));
    }

    private void saveProgress(KyyyUserWordProgress progress) {
        if (progress.getId() == null) {
            kyyyUserWordProgressMapper.insert(progress);
            return;
        }
        kyyyUserWordProgressMapper.updateById(progress);
    }

    private KyyyUserWordProgress loadOrInitProgress(Long userId,
                                                    Long wordId,
                                                    KyyyUserWordProgress existingProgress) {
        if (existingProgress != null) {
            return existingProgress;
        }
        KyyyUserWordProgress progress = new KyyyUserWordProgress();
        progress.setUserId(userId);
        progress.setWordId(wordId);
        progress.setStudyStatus(STUDY_STATUS_NEW);
        progress.setMasteryLevel(0);
        progress.setMemoryStage(0);
        progress.setLearningStep(0);
        progress.setLapseCount(0);
        progress.setConsecutiveKnownCount(0);
        progress.setReviewCount(0);
        progress.setCorrectCount(0);
        progress.setWrongCount(0);
        return progress;
    }

    private boolean shouldReinsert(KyyyWordPracticeSession session,
                                   KyyyUserWordProgress progress,
                                   String rating) {
        if (RATING_UNKNOWN.equals(rating) || RATING_FUZZY.equals(rating)) {
            return true;
        }
        return MODE_STUDY.equals(session.getMode()) && safeInt(progress.getConsecutiveKnownCount(), 0) < 2;
    }

    private int resolveReinsertDelay(String rating) {
        if (RATING_UNKNOWN.equals(rating)) {
            return UNKNOWN_REINSERT_DELAY;
        }
        if (RATING_FUZZY.equals(rating)) {
            return FUZZY_REINSERT_DELAY;
        }
        return KNOW_REINSERT_DELAY;
    }

    private long countSessionItems(Long sessionId) {
        Long total = kyyyWordPracticeSessionItemMapper.selectCount(new LambdaQueryWrapper<KyyyWordPracticeSessionItem>()
                .eq(KyyyWordPracticeSessionItem::getSessionId, sessionId));
        return total == null ? 0L : total;
    }

    private KyyyPracticeNextWordResponse toNextWordResponse(KyyyPracticeSessionCardResponse card) {
        return new KyyyPracticeNextWordResponse(
                card.getWordId(),
                card.getWordText(),
                card.getPhoneticUs(),
                card.getPhoneticUk(),
                card.getPartOfSpeech(),
                card.getMeaningCn(),
                card.getExampleSentence(),
                card.getExampleTranslation(),
                card.getDifficultyLevel(),
                card.getStudyStatus(),
                card.getMasteryLevel(),
                card.getReviewCount(),
                card.getCorrectCount(),
                card.getWrongCount(),
                card.getLastResult(),
                card.getLastStudiedAt(),
                card.getNextReviewAt(),
                card.getSourceBanks(),
                card.getRelatedWords()
        );
    }

    private boolean hasActiveCard(KyyyPracticeSessionResponse sessionResponse) {
        return sessionResponse != null
                && RESPONSE_STATUS_ACTIVE.equals(sessionResponse.getStatus())
                && sessionResponse.getCurrentCard() != null;
    }

    private String normalizeMode(String value) {
        return MODE_REVIEW.equalsIgnoreCase(value) ? MODE_REVIEW : MODE_STUDY;
    }

    private String normalizeRating(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单词评分不能为空");
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case RATING_KNOW, "known", "认识" -> RATING_KNOW;
            case RATING_FUZZY, "模糊" -> RATING_FUZZY;
            case RATING_UNKNOWN, "不认识" -> RATING_UNKNOWN;
            default -> throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单词评分仅支持认识、模糊、不认识");
        };
    }

    private String normalizeStudyStatus(String value) {
        if (!StringUtils.hasText(value)) {
            return STUDY_STATUS_NEW;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case STUDY_STATUS_LEARNING -> STUDY_STATUS_LEARNING;
            case STUDY_STATUS_REVIEW -> STUDY_STATUS_REVIEW;
            case STUDY_STATUS_RELEARNING -> STUDY_STATUS_RELEARNING;
            case STUDY_STATUS_MASTERED -> STUDY_STATUS_MASTERED;
            default -> STUDY_STATUS_NEW;
        };
    }

    private int safeInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private int clampStage(Integer value) {
        int normalized = safeInt(value, 0);
        if (normalized < 0) {
            return 0;
        }
        return Math.min(normalized, KNOW_INTERVAL_DAYS.size() - 1);
    }

    private KyyyWordBank loadDefaultWordBank(Long userId) {
        KyyyUserPracticeSetting setting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        return setting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, setting.getDefaultWordBankId());
    }

    private KyyyUserPracticeSetting loadPracticeSetting(Long userId) {
        return kyyyWordPracticeSupport.loadPracticeSetting(userId);
    }

    private KyyyPracticeSettingResponse toPracticeSettingResponse(KyyyUserPracticeSetting setting, KyyyWordBank defaultWordBank) {
        String examDirection = KyyyExamDirectionSupport.normalizeOrDefault(
                setting == null ? null : setting.getExamDirection()
        );
        return new KyyyPracticeSettingResponse(
                examDirection,
                KyyyExamDirectionSupport.labelOf(examDirection),
                defaultWordBank == null ? null : defaultWordBank.getId(),
                defaultWordBank == null ? null : defaultWordBank.getBankName(),
                KyyyExamDirectionSupport.options()
        );
    }

    private KyyyPracticeSessionBankResponse toSessionBankResponse(KyyyWordBank wordBank) {
        if (wordBank == null) {
            return null;
        }
        return new KyyyPracticeSessionBankResponse(
                wordBank.getId(),
                wordBank.getBankCode(),
                wordBank.getBankName(),
                wordBank.getSubtitle()
        );
    }

    private record SessionContext(List<KyyyWordPracticeSessionItem> allItems,
                                  List<KyyyWordPracticeSessionItem> pendingItems,
                                  Map<Long, KyyyWord> wordMap,
                                  Map<Long, KyyyUserWordProgress> progressMap) {
    }
}
