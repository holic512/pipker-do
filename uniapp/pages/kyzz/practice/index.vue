<template>
	<!-- AI 索引: KYZZ 练习页 -->
	<page-shell
		current="practice"
		root-class="practice-page"
		content-class="practice-page__content"
		:show-tabbar="!switchPopupVisible && !settingsPopupVisible"
	>
		<view class="practice-page__inner">
			<view class="practice-page__halo practice-page__halo--top"></view>
			<view class="practice-page__halo practice-page__halo--bottom"></view>

			<view v-if="resolvedStateNotice" class="practice-page__state-shell">
				<practice-state-notice
					:notice="resolvedStateNotice"
					@primary="handleEmptyPrimary"
					@secondary="handleEmptySecondary"
				/>
			</view>

			<view v-else-if="question && currentBank" class="practice-page__scroll">
				<practice-state-notice
					v-if="completedNotice"
					class="practice-page__inline-notice"
					:notice="completedNotice"
				/>

				<practice-question-panel
					:current-bank="currentBank"
					:progress="sessionState.progress"
					:source-title="sessionState.sourceTitle"
					:is-source-practice="sessionState.sourceType !== 'bank'"
					:question="question"
					:is-favorite="isCurrentQuestionFavorite"
					:selected-option-keys="answerDraft.selectedOptionKeys"
					:answer-text="answerDraft.answerText"
					:review-result="reviewState.result"
					@select-option="handleOptionTap"
					@change-answer-text="handleAnswerTextChange"
					@open-switcher="openSwitchPopup"
					@toggle-favorite="handleToggleFavorite"
					@open-settings="openSettingsPopup"
				/>

				<practice-review-panel
					v-if="reviewState.result"
					:review-result="reviewState.result"
					:awaiting-self-judgement="awaitingSelfJudgement"
					:user-answer-display="userAnswerDisplay"
					:standard-answer-display="standardAnswerDisplay"
					:show-wrong-book-hint="showWrongBookHint"
					:syncing="reviewSyncing"
					:sync-error-message="reviewSyncErrorMessage"
					@open-wrong-book="goWrongBook"
					@retry-review-sync="handleRetryReviewSync"
				/>

				<practice-footer-actions
					v-if="reviewState.result"
					:review-result="reviewState.result"
					:awaiting-self-judgement="awaitingSelfJudgement"
					:can-submit="canSubmit"
					:submitting="uiState.submitting || autoJumping"
					:submit-button-text="submitButtonText"
					:can-go-previous="canGoPrevious"
					:can-go-next="canGoNext"
					:next-button-text="nextButtonText"
					@submit-review="handleReview"
					@judge-correct="handleSelfJudgement(true)"
					@judge-wrong="handleSelfJudgement(false)"
					@previous-question="handlePreviousQuestion"
					@next-question="handleNextQuestion"
				/>

				<practice-comment-composer
					v-if="reviewState.result"
					v-model="commentState.composerContent"
					:submitting="commentState.submitting || autoJumping"
					@submit="handleSubmitComment"
				/>

				<practice-comment-list
					v-if="reviewState.result"
					:records="commentState.records"
					:total="commentState.total"
					:loading="commentState.loading"
					:loading-more="commentState.loadingMore"
					:has-more="commentState.hasMore"
					:error-message="commentState.errorMessage"
					@load-more="handleLoadMoreComments"
					@retry="handleRetryComments"
					@toggle-like="handleToggleCommentLike"
				/>

				<practice-footer-actions
					v-if="!reviewState.result"
					:review-result="reviewState.result"
					:awaiting-self-judgement="awaitingSelfJudgement"
					:can-submit="canSubmit"
					:submitting="uiState.submitting || autoJumping"
					:submit-button-text="submitButtonText"
					:can-go-previous="canGoPrevious"
					:can-go-next="canGoNext"
					:next-button-text="nextButtonText"
					@submit-review="handleReview"
					@judge-correct="handleSelfJudgement(true)"
					@judge-wrong="handleSelfJudgement(false)"
					@previous-question="handlePreviousQuestion"
					@next-question="handleNextQuestion"
				/>

				<view class="practice-page__bottom-space"></view>
			</view>

			<button
				v-if="reviewState.result"
				class="practice-page__scroll-top"
				@tap="scrollToTop"
			>
				<uni-icons type="top" size="18" color="#4f6078" />
			</button>

			<uni-popup
				ref="switchPopup"
				type="bottom"
				background-color="#ffffff"
				border-radius="28rpx 28rpx 0 0"
				:is-mask-click="true"
				@change="handleSwitchPopupChange"
			>
				<practice-bank-switcher
					:banks="sessionState.switchableBanks"
					:current-bank-id="currentBank ? currentBank.bankId : null"
					@close="closeSwitchPopup"
					@switch-bank="handleSwitchBank"
				/>
			</uni-popup>

			<uni-popup
				ref="settingsPopup"
				type="bottom"
				background-color="#ffffff"
				border-radius="28rpx 28rpx 0 0"
				:is-mask-click="true"
				@change="handleSettingsPopupChange"
			>
				<practice-settings-popup
					:auto-jump-on-correct="practiceSettings.autoJumpOnCorrect"
					:syncing="practiceSettings.syncing"
					@close="closeSettingsPopup"
					@change-auto-jump="handleAutoJumpSettingChange"
				/>
			</uni-popup>

			<view v-if="uiState.loading" class="practice-page__loading-mask">
				<view class="practice-page__loading-card">
					<view class="practice-page__loading-spinner"></view>
					<text class="practice-page__loading-title">{{ loadingMaskTitle }}</text>
					<text class="practice-page__loading-desc">{{ loadingMaskDescription }}</text>
				</view>
			</view>
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import PracticeStateNotice from '@/components/kyzz/practice/PracticeStateNotice.vue'
import PracticeQuestionPanel from '@/components/kyzz/practice/PracticeQuestionPanel.vue'
import PracticeReviewPanel from '@/components/kyzz/practice/PracticeReviewPanel.vue'
import PracticeCommentComposer from '@/components/kyzz/practice/PracticeCommentComposer.vue'
import PracticeCommentList from '@/components/kyzz/practice/PracticeCommentList.vue'
import PracticeFooterActions from '@/components/kyzz/practice/PracticeFooterActions.vue'
import PracticeBankSwitcher from '@/components/kyzz/practice/PracticeBankSwitcher.vue'
import PracticeSettingsPopup from '@/components/kyzz/practice/PracticeSettingsPopup.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getPracticeSession, reviewPracticeQuestion, selfJudgePracticeQuestion } from '@/pages/kyzz/api/practice'
import {
	getCachedPracticeAnswerPreview,
	getCachedPracticeSession,
	preloadPracticeAnswerPreview,
	preloadPracticeSession,
	setCachedPracticeSession
} from '@/shared/preload/kyzz'
import { favoriteQuestion, unfavoriteQuestion } from '@/pages/kyzz/api/favorite'
import {
	createPracticeQuestionComment,
	getPracticeQuestionComments,
	likePracticeQuestionComment,
	unlikePracticeQuestionComment
} from '@/pages/kyzz/api/comment'
import { consumePracticeLaunchTarget } from '@/pages/kyzz/practice/navigation'
import {
	cachePracticeSettings,
	loadPracticeSettingsWithFallback,
	readCachedPracticeSettings,
	syncPracticeSettings
} from '@/pages/kyzz/practice/settings'
import type {
	KyzzPracticeAnswerPreviewResponse,
	KyzzPracticeAnswerDraftState,
	KyzzPracticeBankViewRecord,
	KyzzPracticeBankRecordResponse,
	KyzzPracticeCommentItem,
	KyzzPracticeCommentState,
	KyzzPracticeCommentCreateRequest,
	KyzzPracticeNoticeViewModel,
	KyzzPracticeReviewRequest,
	KyzzPracticeReviewResponse,
	KyzzPracticeReviewState,
	KyzzPracticeReviewViewResult,
	KyzzPracticeSelfJudgementRequest,
	KyzzPracticeSettingState,
	KyzzPracticeSessionQuery,
	KyzzPracticeSessionResponse,
	KyzzPracticeSessionState,
	KyzzPracticeUiState,
	UniPopupRef,
	KyzzPracticeSourceStatus,
	KyzzPracticeSourceType
} from '@/pages/kyzz/practice/types'
import {
	buildCompletedNotice,
	buildNoBankNotice,
	buildNoQuestionNotice,
	createEmptyPracticeAnswerDraft,
	createEmptyPracticeCommentState,
	createEmptyPracticeReviewState,
	createEmptyPracticeSession,
	createEmptyPracticeUiState,
	normalizePracticeCommentLikeToggleResponse,
	normalizePracticeCommentItem,
	normalizePracticeCommentPage,
	normalizePracticeReviewResult,
	normalizePracticeSession,
	resolvePracticeEmptyState,
	sortPracticeCommentsByLike
} from '@/pages/kyzz/practice/view'

interface PracticePageQuery {
	bankId?: string
	questionId?: string
	freshAttempt?: string
	sourceType?: string
	sourceStatus?: string
	keyword?: string
}

interface PracticePageState {
	sessionState: KyzzPracticeSessionState
	answerDraft: KyzzPracticeAnswerDraftState
	reviewState: KyzzPracticeReviewState
	commentState: KyzzPracticeCommentState
	uiState: KyzzPracticeUiState
	routeQuery: KyzzPracticeSessionQuery
	practiceSettings: KyzzPracticeSettingState
	reviewSyncing: boolean
	reviewSyncErrorMessage: string
	pendingReviewSync: PracticeReviewSyncTask | null
	switchPopupVisible: boolean
	settingsPopupVisible: boolean
	autoJumping: boolean
	autoJumpTimer: ReturnType<typeof setTimeout> | null
}

interface PracticeReviewSyncTask {
	questionId: number
	payload: KyzzPracticeReviewRequest
}

interface UniPopupChangeEvent {
	show?: boolean
	type?: string
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

function parseOptionalNumber(value: string | undefined): number | null {
	if (!value) {
		return null
	}
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : null
}

function parseOptionalBoolean(value: string | undefined): boolean | null {
	if (value === 'true' || value === '1') {
		return true
	}
	if (value === 'false' || value === '0') {
		return false
	}
	return null
}

function parseSourceType(value: string | undefined): KyzzPracticeSourceType | null {
	if (value === 'bank' || value === 'wrong_book' || value === 'favorite') {
		return value
	}
	return null
}

function parseSourceStatus(value: string | undefined): KyzzPracticeSourceStatus | null {
	if (value === 'all' || value === 'active' || value === 'mastered') {
		return value
	}
	return null
}

function parseOptionalString(value: string | undefined): string | null {
	return value && value.trim() ? value.trim() : null
}

function showModal(options: { title: string; content: string; confirmText?: string; cancelText?: string }): Promise<boolean> {
	return new Promise((resolve) => {
		uni.showModal({
			title: options.title,
			content: options.content,
			confirmText: options.confirmText || '确定',
			cancelText: options.cancelText || '取消',
			success: (result: { confirm: boolean }) => {
				resolve(Boolean(result.confirm))
			},
			fail: () => {
				resolve(false)
			}
		})
	})
}

function normalizeOptionKeyList(value: string[] | null | undefined): string[] {
	return Array.isArray(value)
		? value.map((item) => String(item).trim().toUpperCase()).filter(Boolean).sort()
		: []
}

function areOptionKeyListsEqual(left: string[] | null | undefined, right: string[] | null | undefined): boolean {
	const leftKeys = normalizeOptionKeyList(left)
	const rightKeys = normalizeOptionKeyList(right)
	return leftKeys.length === rightKeys.length && leftKeys.every((item, index) => item === rightKeys[index])
}

function hasRouteTarget(query: KyzzPracticeSessionQuery): boolean {
	return query.bankId !== null && query.bankId !== undefined
		|| query.questionId !== null && query.questionId !== undefined
		|| query.sourceType !== null && query.sourceType !== undefined
}

function mergeSessionQuery(base: KyzzPracticeSessionQuery, patch: KyzzPracticeSessionQuery): KyzzPracticeSessionQuery {
	const hasExplicitBank = patch.bankId !== null && patch.bankId !== undefined
	const hasExplicitSource = patch.sourceType !== null && patch.sourceType !== undefined
	const bankChanged = patch.bankId !== null
		&& patch.bankId !== undefined
		&& patch.bankId !== base.bankId
	const hasExplicitQuestion = patch.questionId !== null && patch.questionId !== undefined
	const shouldResetToBankSource = hasExplicitBank
		&& (patch.sourceType === null || patch.sourceType === undefined)
	const sourceChanged = patch.sourceType !== null
		&& patch.sourceType !== undefined
		&& patch.sourceType !== base.sourceType
	const nextSourceType = shouldResetToBankSource
		? null
		: patch.sourceType ?? base.sourceType ?? null
	const shouldUseRecommendedBank = patch.sourceType === 'bank' && !hasExplicitBank
	const hasLaunchTarget = hasExplicitBank || hasExplicitQuestion || hasExplicitSource
	return {
		bankId: shouldUseRecommendedBank ? null : patch.bankId ?? base.bankId ?? null,
		questionId: hasExplicitQuestion
			? patch.questionId ?? null
			: bankChanged || sourceChanged || shouldResetToBankSource || shouldUseRecommendedBank
				? null
				: base.questionId ?? null,
		freshAttempt: patch.freshAttempt ?? base.freshAttempt ?? null,
		sourceType: nextSourceType,
		sourceStatus: hasLaunchTarget ? patch.sourceStatus ?? null : base.sourceStatus ?? null,
		keyword: hasLaunchTarget ? patch.keyword ?? null : base.keyword ?? null
	}
}

export default defineComponent({
	name: 'PracticePage',
	components: {
		PageShell,
		PracticeStateNotice,
		PracticeQuestionPanel,
		PracticeReviewPanel,
		PracticeCommentComposer,
		PracticeCommentList,
		PracticeFooterActions,
		PracticeBankSwitcher,
		PracticeSettingsPopup
	},
	data(): PracticePageState {
		return {
			sessionState: createEmptyPracticeSession(),
			answerDraft: createEmptyPracticeAnswerDraft(),
			reviewState: createEmptyPracticeReviewState(),
			commentState: createEmptyPracticeCommentState(),
			uiState: createEmptyPracticeUiState(),
			routeQuery: {
				bankId: null,
				questionId: null,
				freshAttempt: null,
				sourceType: null,
				sourceStatus: null,
				keyword: null
			},
			practiceSettings: readCachedPracticeSettings(),
			reviewSyncing: false,
			reviewSyncErrorMessage: '',
			pendingReviewSync: null,
			switchPopupVisible: false,
			settingsPopupVisible: false,
			autoJumping: false,
			autoJumpTimer: null
		}
	},
	computed: {
		currentBank(): KyzzPracticeBankViewRecord | null {
			return this.sessionState.activeBank
		},
		question() {
			return this.sessionState.question
		},
		isCurrentQuestionFavorite(): boolean {
			return Boolean(this.question && this.question.isFavorite)
		},
		awaitingSelfJudgement(): boolean {
			return Boolean(this.reviewState.result && this.reviewState.result.requiresSelfJudgement && this.reviewState.result.isCorrect === null)
		},
		canSubmit(): boolean {
			if (!this.question || this.uiState.submitting || this.autoJumping || Boolean(this.reviewState.result)) {
				return false
			}
			if (this.question.questionType === 'single') {
				return this.answerDraft.selectedOptionKeys.length === 1
			}
			if (this.question.questionType === 'multiple') {
				return this.answerDraft.selectedOptionKeys.length > 0
			}
			return true
		},
		submitButtonText(): string {
			return '查看答案'
		},
		canGoPrevious(): boolean {
			return Boolean(this.sessionState.previousQuestionId && !this.autoJumping && !this.reviewSyncing && !this.reviewSyncErrorMessage)
		},
		canGoNext(): boolean {
			return Boolean(
				this.reviewState.result
				&& !this.awaitingSelfJudgement
				&& !this.autoJumping
				&& !this.reviewSyncing
				&& !this.reviewSyncErrorMessage
				&& this.reviewState.result.nextQuestionId
			)
		},
		nextButtonText(): string {
			if (this.reviewState.result?.completedSource && !this.reviewState.result.nextQuestionId) {
				return this.sessionState.sourceType === 'bank' ? '再刷一遍' : '已完成本轮'
			}
			if (this.reviewState.result?.completedBank) {
				return '再刷一遍'
			}
			return '下一题'
		},
		showWrongBookHint(): boolean {
			return Boolean(
				this.reviewState.result
				&& this.reviewState.result.isCorrect === false
				&& !this.awaitingSelfJudgement
				&& !this.reviewSyncing
				&& !this.reviewSyncErrorMessage
			)
		},
		userAnswerDisplay(): string {
			if (!this.reviewState.result) {
				return '未作答'
			}
			if (this.reviewState.result.questionType === 'short') {
				return this.reviewState.result.submittedAnswerText || '暂未填写'
			}
			return this.reviewState.result.submittedOptionKeys.length
				? this.reviewState.result.submittedOptionKeys.join('、')
				: '未选择'
		},
		standardAnswerDisplay(): string {
			if (!this.reviewState.result) {
				return ''
			}
			if (this.reviewState.result.questionType === 'short') {
				return this.reviewState.result.answerText || '暂无标准答案'
			}
			if (this.reviewState.result.correctOptionKeys.length) {
				return this.reviewState.result.correctOptionKeys.join('、')
			}
			return this.reviewState.result.answerText || '暂无标准答案'
		},
		hasPendingState(): boolean {
			if (this.reviewSyncing || this.reviewSyncErrorMessage) {
				return true
			}
			if (this.awaitingSelfJudgement) {
				return true
			}
			if (this.reviewState.result || !this.question) {
				return false
			}
			if (this.question.questionType === 'short') {
				return this.answerDraft.answerText.trim().length > 0
			}
			return this.answerDraft.selectedOptionKeys.length > 0
		},
		loadingMaskTitle(): string {
			if (!this.uiState.loadedOnce && hasRouteTarget(this.routeQuery)) {
				return '正在进入题目'
			}
			if (!this.uiState.loadedOnce) {
				return '正在恢复练习进度'
			}
			return '正在切换题目'
		},
		loadingMaskDescription(): string {
			if (!this.uiState.loadedOnce && hasRouteTarget(this.routeQuery)) {
				return '马上定位到选中的题库'
			}
			if (!this.uiState.loadedOnce) {
				return '为你定位上次未完成的位置'
			}
			return '请稍等，题目马上就好'
		},
		emptyNotice(): KyzzPracticeNoticeViewModel | null {
			if (this.uiState.emptyState === 'no_bank') {
				return buildNoBankNotice()
			}
			if (this.uiState.emptyState === 'no_question') {
				return buildNoQuestionNotice()
			}
			return null
		},
		resolvedStateNotice(): KyzzPracticeNoticeViewModel | null {
			if (this.emptyNotice) {
				return this.emptyNotice
			}
			if (this.uiState.loadedOnce && (!this.question || !this.currentBank)) {
				return buildNoQuestionNotice()
			}
			return null
		},
		completedNotice(): KyzzPracticeNoticeViewModel | null {
			if (!this.reviewState.result || !this.reviewState.result.completedBank) {
				return null
			}
			return buildCompletedNotice(this.currentBank)
		}
	},
	onLoad(query: PracticePageQuery) {
		this.routeQuery = {
			bankId: parseOptionalNumber(query.bankId),
			questionId: parseOptionalNumber(query.questionId),
			freshAttempt: parseOptionalBoolean(query.freshAttempt),
			sourceType: parseSourceType(query.sourceType),
			sourceStatus: parseSourceStatus(query.sourceStatus),
			keyword: parseOptionalString(query.keyword)
		}
	},
	onShow() {
		uni.hideTabBar({
			animation: false,
			fail: () => {}
		})
		const launchTarget = consumePracticeLaunchTarget()
		if (hasRouteTarget(launchTarget)) {
			this.routeQuery = mergeSessionQuery(this.routeQuery, launchTarget)
			this.bootstrapAndLoad(this.routeQuery)
			return
		}
		if (!this.uiState.loadedOnce) {
			this.bootstrapAndLoad(this.routeQuery)
		}
	},
	onHide() {
		this.cancelAutoJump()
	},
	onUnload() {
		this.cancelAutoJump()
	},
	methods: {
		async bootstrapAndLoad(query: KyzzPracticeSessionQuery): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				const settingsPromise = this.refreshPracticeSettings()
				await this.loadSession(query)
				await settingsPromise
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '刷题页面加载失败'),
					icon: 'none'
				})
			}
		},
		async refreshPracticeSettings(): Promise<void> {
			this.practiceSettings = {
				...this.practiceSettings,
				...readCachedPracticeSettings(),
				syncing: false
			}
			const settings = await loadPracticeSettingsWithFallback()
			if (this.practiceSettings.syncing) {
				return
			}
			this.practiceSettings = {
				...settings,
				syncing: false
			}
		},
		applySessionResult(result: KyzzPracticeSessionResponse, query: KyzzPracticeSessionQuery): void {
			this.sessionState = normalizePracticeSession(result)
			this.reviewState = {
				result: this.sessionState.reviewResult
			}
			this.reviewSyncing = false
			this.reviewSyncErrorMessage = ''
			this.pendingReviewSync = null
			this.answerDraft = this.reviewState.result
				? {
					selectedOptionKeys: [...this.reviewState.result.submittedOptionKeys],
					answerText: this.reviewState.result.submittedAnswerText || '',
					questionStartedAt: Date.now()
				}
				: createEmptyPracticeAnswerDraft()
			this.routeQuery = {
				bankId: this.sessionState.activeBank?.bankId ?? query.bankId ?? null,
				questionId: this.sessionState.question?.id ?? query.questionId ?? null,
				freshAttempt: null,
				sourceType: this.sessionState.sourceType === 'bank' ? null : this.sessionState.sourceType,
				sourceStatus: query.sourceStatus ?? null,
				keyword: query.keyword ?? null
			}
			if (this.reviewState.result && this.sessionState.question) {
				this.loadComments({ reset: true, questionId: this.sessionState.question.id, silent: true }).catch((error) => {
					console.warn('[practice] load comments after session failed', error)
				})
			} else {
				this.resetComments()
			}
			this.uiState.emptyState = null
			this.uiState.loadedOnce = true
			this.warmNextPracticeSession()
			uni.pageScrollTo({
				scrollTop: 0,
				duration: 0
			})
		},
		async loadSession(query: KyzzPracticeSessionQuery, options: { preferCache?: boolean } = {}): Promise<void> {
			if (this.uiState.loading) {
				return
			}
			if (options.preferCache) {
				const cachedSession = getCachedPracticeSession(query)
				if (cachedSession) {
					this.applySessionResult(cachedSession, query)
					return
				}
			}
			this.uiState.loading = true
			try {
				const result = options.preferCache
					? await preloadPracticeSession(query)
					: await getPracticeSession(query)
				this.applySessionResult(result, query)
			} catch (error) {
				this.sessionState = createEmptyPracticeSession()
				this.answerDraft = createEmptyPracticeAnswerDraft()
				this.reviewState = createEmptyPracticeReviewState()
				this.reviewSyncing = false
				this.reviewSyncErrorMessage = ''
				this.pendingReviewSync = null
				this.resetComments()
				this.uiState.loadedOnce = true
				this.uiState.emptyState = resolvePracticeEmptyState(error)
				if (!this.uiState.emptyState) {
					uni.showToast({
						title: resolveErrorMessage(error, '刷题页面加载失败'),
						icon: 'none'
					})
				}
			} finally {
				this.uiState.loading = false
			}
		},
		buildQuestionSessionQuery(questionId: number | null | undefined): KyzzPracticeSessionQuery | null {
			if (!questionId || !this.currentBank) {
				return null
			}
			return {
				bankId: this.currentBank.bankId,
				questionId,
				sourceType: this.routeQuery.sourceType ?? null,
				sourceStatus: this.routeQuery.sourceStatus ?? null,
				keyword: this.routeQuery.keyword ?? null
			}
		},
		warmNextPracticeSession(): void {
			const query = this.buildQuestionSessionQuery(this.sessionState.nextQuestionId)
			if (!query) {
				return
			}
			preloadPracticeSession(query).catch((error) => {
				console.warn('[preload:practice] next session skipped', error)
			})
		},
		patchCachedPracticeSessionBank(
			query: KyzzPracticeSessionQuery | null,
			updatedBank: KyzzPracticeBankRecordResponse | KyzzPracticeBankViewRecord | null
		): void {
			if (!query || !updatedBank) {
				return
			}
			const cachedSession = getCachedPracticeSession(query)
			if (!cachedSession) {
				return
			}
			setCachedPracticeSession(query, {
				...cachedSession,
				activeBank: cachedSession.activeBank?.bankId === updatedBank.bankId ? updatedBank : cachedSession.activeBank,
				switchableBanks: Array.isArray(cachedSession.switchableBanks)
					? cachedSession.switchableBanks.map((item) => item.bankId === updatedBank.bankId ? updatedBank : item)
					: []
			})
		},
		warmAnswerPreviewForCurrentQuestion(): void {
			if (!this.question || !this.currentBank || this.reviewState.result) {
				return
			}
			preloadPracticeAnswerPreview(this.question.id, this.currentBank.bankId).catch((error) => {
				console.warn('[preload:practice] answer preview skipped', error)
			})
		},
		async loadAnswerPreviewForCurrentQuestion(): Promise<KyzzPracticeAnswerPreviewResponse> {
			if (!this.question || !this.currentBank) {
				throw new Error('题目不能为空')
			}
			const cachedPreview = getCachedPracticeAnswerPreview(this.question.id, this.currentBank.bankId)
			if (cachedPreview) {
				return cachedPreview
			}
			return preloadPracticeAnswerPreview(this.question.id, this.currentBank.bankId)
		},
		buildReviewPayload(): KyzzPracticeReviewRequest | null {
			if (!this.question || !this.currentBank) {
				return null
			}
			const payload: KyzzPracticeReviewRequest = {
				bankId: this.currentBank.bankId,
				usedSeconds: this.buildUsedSeconds(),
				sourceType: this.routeQuery.sourceType ?? null,
				sourceStatus: this.routeQuery.sourceStatus ?? null,
				keyword: this.routeQuery.keyword ?? null
			}
			if (this.question.questionType === 'short') {
				payload.answerText = this.answerDraft.answerText.trim()
			} else {
				payload.selectedOptionKeys = [...this.answerDraft.selectedOptionKeys]
			}
			return payload
		},
		buildPreviewReviewResult(
			preview: KyzzPracticeAnswerPreviewResponse,
			payload: KyzzPracticeReviewRequest
		): KyzzPracticeReviewViewResult {
			const submittedOptionKeys = normalizeOptionKeyList(payload.selectedOptionKeys)
			const isShortQuestion = preview.questionType === 'short'
			const response: KyzzPracticeReviewResponse = {
				questionId: preview.questionId,
				bankId: preview.bankId,
				questionType: preview.questionType,
				submittedOptionKeys: isShortQuestion ? [] : submittedOptionKeys,
				submittedAnswerText: isShortQuestion ? payload.answerText || '' : null,
				requiresSelfJudgement: isShortQuestion,
				isCorrect: isShortQuestion ? null : areOptionKeyListsEqual(submittedOptionKeys, preview.correctOptionKeys),
				correctOptionKeys: normalizeOptionKeyList(preview.correctOptionKeys),
				answerText: preview.answerText,
				analysis: preview.analysis,
				updatedBank: null,
				nextQuestionId: isShortQuestion ? null : this.sessionState.nextQuestionId,
				nextQuestionIndex: isShortQuestion ? null : this.sessionState.nextQuestionIndex,
				completedBank: false,
				sourceType: this.sessionState.sourceType,
				sourceTitle: this.sessionState.sourceTitle,
				completedSource: !isShortQuestion && !this.sessionState.nextQuestionId
			}
			return normalizePracticeReviewResult(response)
		},
		async handleAutoJumpSettingChange(value: boolean): Promise<void> {
			const autoJumpOnCorrect = Boolean(value)
			this.practiceSettings = {
				...this.practiceSettings,
				autoJumpOnCorrect,
				loaded: true,
				syncing: true
			}
			cachePracticeSettings(this.practiceSettings)
			if (!autoJumpOnCorrect) {
				this.cancelAutoJump()
			}
			try {
				this.practiceSettings = await syncPracticeSettings({
					autoJumpOnCorrect
				})
			} catch (error) {
				this.practiceSettings = {
					...this.practiceSettings,
					loaded: true,
					syncing: false
				}
				uni.showToast({
					title: '设置已在本机生效',
					icon: 'none'
				})
			}
		},
		openSettingsPopup(): void {
			if (this.autoJumping) {
				return
			}
			this.settingsPopupVisible = true
			;(this.$refs.settingsPopup as UniPopupRef | undefined)?.open()
		},
		closeSettingsPopup(): void {
			this.settingsPopupVisible = false
			;(this.$refs.settingsPopup as UniPopupRef | undefined)?.close()
		},
		handleSettingsPopupChange(event: UniPopupChangeEvent): void {
			this.settingsPopupVisible = Boolean(event?.show)
		},
		cancelAutoJump(): void {
			if (this.autoJumpTimer) {
				clearTimeout(this.autoJumpTimer)
				this.autoJumpTimer = null
			}
			this.autoJumping = false
		},
		scheduleAutoJumpIfNeeded(result: KyzzPracticeReviewViewResult | null): boolean {
			if (!result || result.isCorrect !== true || result.requiresSelfJudgement) {
				return false
			}
			if (!this.practiceSettings.autoJumpOnCorrect) {
				return false
			}
			if (!result.nextQuestionId) {
				uni.showToast({
					title: '已完成当前题组',
					icon: 'none'
				})
				return false
			}
			if (this.autoJumping) {
				return true
			}
			const bankId = this.currentBank?.bankId ?? result.bankId
			if (!bankId) {
				return false
			}
			this.cancelAutoJump()
			this.autoJumping = true
			uni.showToast({
				title: '回答正确，正在进入下一题',
				icon: 'none',
				duration: 700
			})
			this.autoJumpTimer = setTimeout(() => {
				this.autoJumpTimer = null
				this.loadSession({
					bankId,
					questionId: result.nextQuestionId,
					sourceType: this.routeQuery.sourceType ?? null,
					sourceStatus: this.routeQuery.sourceStatus ?? null,
					keyword: this.routeQuery.keyword ?? null
				}, { preferCache: true }).finally(() => {
					this.autoJumping = false
				})
			}, 500)
			return true
		},
		handleAnswerTextChange(value: string): void {
			this.answerDraft.answerText = value
			if (value.trim()) {
				this.warmAnswerPreviewForCurrentQuestion()
			}
		},
		handleOptionTap(optionKey: string): void {
			if (this.reviewState.result || !this.question || this.autoJumping) {
				return
			}
			if (this.question.questionType === 'single') {
				this.answerDraft.selectedOptionKeys = [optionKey]
				this.warmAnswerPreviewForCurrentQuestion()
				return
			}
			if (this.question.questionType === 'multiple') {
				const selected = new Set(this.answerDraft.selectedOptionKeys)
				if (selected.has(optionKey)) {
					selected.delete(optionKey)
				} else {
					selected.add(optionKey)
				}
				this.answerDraft.selectedOptionKeys = Array.from(selected).sort()
				if (this.answerDraft.selectedOptionKeys.length) {
					this.warmAnswerPreviewForCurrentQuestion()
				}
			}
		},
		buildUsedSeconds(): number {
			const diff = Math.floor((Date.now() - this.answerDraft.questionStartedAt) / 1000)
			return diff > 0 ? diff : 0
		},
		async handleReview(): Promise<void> {
			if (!this.question || !this.currentBank || !this.canSubmit || this.uiState.submitting || this.autoJumping) {
				return
			}
			this.uiState.submitting = true
			try {
				const questionId = this.question.id
				const payload = this.buildReviewPayload()
				if (!payload) {
					return
				}
				let preview: KyzzPracticeAnswerPreviewResponse | null = null
				try {
					preview = await this.loadAnswerPreviewForCurrentQuestion()
				} catch (previewError) {
					console.warn('[practice] answer preview fallback to review request', previewError)
				}
				if (preview) {
					this.reviewState.result = this.buildPreviewReviewResult(preview, payload)
					if (this.question.questionType === 'short') {
						this.uiState.submitting = false
						await this.loadComments({ reset: true, questionId, silent: true })
						return
					}
					this.resetComments(questionId)
					this.uiState.submitting = false
					this.syncObjectiveReview({
						questionId,
						payload
					}).catch((error) => {
						console.warn('[practice] objective review sync failed', error)
					})
					return
				}
				const result = await reviewPracticeQuestion(questionId, payload)
				this.reviewState.result = normalizePracticeReviewResult(result)
				this.patchCachedPracticeSessionBank(
					this.buildQuestionSessionQuery(this.reviewState.result.nextQuestionId),
					this.reviewState.result.updatedBank
				)
				this.replaceBankRecord(this.reviewState.result.updatedBank)
				if (!this.scheduleAutoJumpIfNeeded(this.reviewState.result)) {
					await this.loadComments({ reset: true, questionId, silent: true })
				}
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '查看答案失败'),
					icon: 'none'
				})
			} finally {
				this.uiState.submitting = false
			}
		},
		async syncObjectiveReview(task: PracticeReviewSyncTask): Promise<void> {
			this.reviewSyncing = true
			this.reviewSyncErrorMessage = ''
			this.pendingReviewSync = task
			try {
				const result = await reviewPracticeQuestion(task.questionId, task.payload)
				if (!this.sessionState.question || this.sessionState.question.id !== task.questionId) {
					this.reviewSyncing = false
					this.pendingReviewSync = null
					return
				}
				this.reviewState.result = normalizePracticeReviewResult(result)
				this.patchCachedPracticeSessionBank(
					this.buildQuestionSessionQuery(this.reviewState.result.nextQuestionId),
					this.reviewState.result.updatedBank
				)
				this.replaceBankRecord(this.reviewState.result.updatedBank)
				this.reviewSyncing = false
				this.reviewSyncErrorMessage = ''
				this.pendingReviewSync = null
				if (!this.scheduleAutoJumpIfNeeded(this.reviewState.result)) {
					await this.loadComments({ reset: true, questionId: task.questionId, silent: true })
				}
			} catch (error) {
				this.reviewSyncing = false
				this.reviewSyncErrorMessage = resolveErrorMessage(error, '作答记录同步失败，重试后才能进入下一题')
				uni.showToast({
					title: this.reviewSyncErrorMessage,
					icon: 'none'
				})
				throw error
			}
		},
		async handleRetryReviewSync(): Promise<void> {
			if (!this.pendingReviewSync || this.reviewSyncing || this.autoJumping) {
				return
			}
			try {
				await this.syncObjectiveReview(this.pendingReviewSync)
			} catch (error) {
				console.warn('[practice] retry review sync failed', error)
			}
		},
		async handleSelfJudgement(selfJudgedCorrect: boolean): Promise<void> {
			if (!this.question || !this.currentBank || this.uiState.submitting || this.autoJumping || !this.awaitingSelfJudgement) {
				return
			}
			this.uiState.submitting = true
			try {
				const payload: KyzzPracticeSelfJudgementRequest = {
					bankId: this.currentBank.bankId,
					answerText: this.answerDraft.answerText.trim(),
					usedSeconds: this.buildUsedSeconds(),
					selfJudgedCorrect,
					sourceType: this.routeQuery.sourceType ?? null,
					sourceStatus: this.routeQuery.sourceStatus ?? null,
					keyword: this.routeQuery.keyword ?? null
				}
				const result = await selfJudgePracticeQuestion(this.question.id, payload)
				this.reviewState.result = normalizePracticeReviewResult(result)
				this.patchCachedPracticeSessionBank(
					this.buildQuestionSessionQuery(this.reviewState.result.nextQuestionId),
					this.reviewState.result.updatedBank
				)
				this.replaceBankRecord(this.reviewState.result.updatedBank)
				if (!this.scheduleAutoJumpIfNeeded(this.reviewState.result)) {
					await this.loadComments({ reset: true, questionId: this.question.id, silent: true })
				}
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '自判结果提交失败'),
					icon: 'none'
				})
			} finally {
				this.uiState.submitting = false
			}
		},
		replaceBankRecord(updatedBank: KyzzPracticeBankViewRecord | null): void {
			if (!updatedBank) {
				return
			}
			this.sessionState.activeBank = updatedBank
			this.sessionState.switchableBanks = this.sessionState.switchableBanks.map((item) => {
				return item.bankId === updatedBank.bankId ? updatedBank : item
			})
		},
		resetComments(questionId: number | null = null): void {
			const nextState = createEmptyPracticeCommentState()
			nextState.questionId = questionId
			this.commentState = nextState
		},
		async loadComments(options: { reset: boolean; questionId?: number; silent?: boolean }): Promise<void> {
			const questionId = options.questionId ?? this.sessionState.question?.id ?? null
			if (!questionId || !this.reviewState.result) {
				this.resetComments()
				return
			}
			if (options.reset) {
				const nextState = createEmptyPracticeCommentState()
				nextState.questionId = questionId
				nextState.loading = true
				nextState.composerContent = this.commentState.composerContent
				this.commentState = nextState
			} else {
				if (this.commentState.loading || this.commentState.loadingMore || !this.commentState.hasMore) {
					return
				}
				this.commentState.loadingMore = true
				this.commentState.errorMessage = ''
			}
			const targetPageNo = options.reset ? 1 : this.commentState.pageNo + 1
			try {
				const result = normalizePracticeCommentPage(await getPracticeQuestionComments(questionId, {
					pageNo: targetPageNo,
					pageSize: this.commentState.pageSize
				}))
				this.commentState = {
					...this.commentState,
					questionId,
					records: sortPracticeCommentsByLike(options.reset ? result.records : this.commentState.records.concat(result.records)),
					pageNo: Number(result.pageNo || targetPageNo),
					pageSize: Number(result.pageSize || this.commentState.pageSize),
					total: Number(result.total || 0),
					hasMore: Boolean(result.hasMore),
					loading: false,
					loadingMore: false,
					initialized: true,
					errorMessage: ''
				}
			} catch (error) {
				this.commentState.loading = false
				this.commentState.loadingMore = false
				this.commentState.initialized = true
				this.commentState.errorMessage = resolveErrorMessage(error, '评论加载失败')
				if (!options.silent) {
					uni.showToast({
						title: this.commentState.errorMessage,
						icon: 'none'
					})
				}
			}
		},
		async handleLoadMoreComments(): Promise<void> {
			if (!this.reviewState.result || !this.commentState.hasMore || this.autoJumping) {
				return
			}
			await this.loadComments({ reset: false, silent: true })
		},
		async handleRetryComments(): Promise<void> {
			if (!this.reviewState.result || !this.sessionState.question || this.autoJumping) {
				return
			}
			const shouldReset = !this.commentState.records.length
			await this.loadComments({
				reset: shouldReset,
				questionId: this.sessionState.question.id,
				silent: false
			})
		},
		async handleSubmitComment(): Promise<void> {
			if (!this.reviewState.result || !this.sessionState.question || this.commentState.submitting || this.autoJumping) {
				return
			}
			const content = this.commentState.composerContent.trim()
			if (!content) {
				return
			}
			this.commentState.submitting = true
			try {
				const payload: KyzzPracticeCommentCreateRequest = {
					content
				}
				const createdComment = normalizePracticeCommentItem(await createPracticeQuestionComment(
					this.sessionState.question.id,
					payload
				))
				this.commentState = {
					...this.commentState,
					questionId: this.sessionState.question.id,
					records: sortPracticeCommentsByLike([createdComment, ...this.commentState.records]),
					total: this.commentState.total + 1,
					initialized: true,
					composerContent: '',
					submitting: false
				}
				uni.showToast({
					title: '评论已发布',
					icon: 'none'
				})
			} catch (error) {
				this.commentState.submitting = false
				uni.showToast({
					title: resolveErrorMessage(error, '评论发布失败'),
					icon: 'none'
				})
			}
		},
		async handleToggleCommentLike(commentId: number): Promise<void> {
			if (this.autoJumping || this.commentState.likingCommentIds.includes(commentId)) {
				return
			}
			const currentComment = this.commentState.records.find((item) => item.commentId === commentId)
			if (!currentComment) {
				return
			}
			const wasLiked = currentComment.isLiked
			const optimisticLikeCount = Math.max(0, currentComment.likeCount + (wasLiked ? -1 : 1))
			this.commentState = {
				...this.commentState,
				likingCommentIds: [...this.commentState.likingCommentIds, commentId],
				records: this.updateCommentRecord(commentId, {
					isLiked: !wasLiked,
					likeCount: optimisticLikeCount
				})
			}
			try {
				const result = normalizePracticeCommentLikeToggleResponse(
					wasLiked
						? await unlikePracticeQuestionComment(commentId)
						: await likePracticeQuestionComment(commentId)
				)
				const resolvedCommentId = Number(result.commentId || commentId)
				this.commentState = {
					...this.commentState,
					likingCommentIds: this.commentState.likingCommentIds.filter((item) => item !== commentId),
					records: this.updateCommentRecord(resolvedCommentId, {
						isLiked: Boolean(result.isLiked),
						likeCount: Number(result.likeCount || 0)
					})
				}
			} catch (error) {
				this.commentState = {
					...this.commentState,
					likingCommentIds: this.commentState.likingCommentIds.filter((item) => item !== commentId),
					records: this.updateCommentRecord(commentId, {
						isLiked: wasLiked,
						likeCount: currentComment.likeCount
					})
				}
				uni.showToast({
					title: resolveErrorMessage(error, '操作失败'),
					icon: 'none'
				})
			}
		},
		updateCommentRecord(commentId: number, patch: Partial<Pick<KyzzPracticeCommentItem, 'isLiked' | 'likeCount'>>): KyzzPracticeCommentItem[] {
			return sortPracticeCommentsByLike(this.commentState.records.map((item) => {
				return item.commentId === commentId
					? {
						...item,
						...patch
					}
					: item
			}))
		},
		async handleToggleFavorite(): Promise<void> {
			if (!this.question || this.uiState.submitting || this.autoJumping) {
				return
			}
			const questionId = this.question.id
			const willFavorite = !this.question.isFavorite
			this.sessionState.question = {
				...this.question,
				isFavorite: willFavorite
			}
			try {
				const result = willFavorite
					? await favoriteQuestion(questionId)
					: await unfavoriteQuestion(questionId)
				if (this.sessionState.question && this.sessionState.question.id === questionId) {
					this.sessionState.question = {
						...this.sessionState.question,
						isFavorite: Boolean(result.isFavorite)
					}
				}
				uni.showToast({
					title: result.isFavorite ? '已收藏' : '已取消收藏',
					icon: 'none'
				})
			} catch (error) {
				if (this.sessionState.question && this.sessionState.question.id === questionId) {
					this.sessionState.question = {
						...this.sessionState.question,
						isFavorite: !willFavorite
					}
				}
				uni.showToast({
					title: resolveErrorMessage(error, willFavorite ? '收藏失败' : '取消收藏失败'),
					icon: 'none'
				})
			}
		},
		async handleNextQuestion(): Promise<void> {
			if (!this.reviewState.result || !this.currentBank || !this.reviewState.result.nextQuestionId || this.autoJumping || this.reviewSyncing || this.reviewSyncErrorMessage) {
				return
			}
			this.cancelAutoJump()
			await this.loadSession({
				bankId: this.currentBank.bankId,
				questionId: this.reviewState.result.nextQuestionId,
				sourceType: this.routeQuery.sourceType ?? null,
				sourceStatus: this.routeQuery.sourceStatus ?? null,
				keyword: this.routeQuery.keyword ?? null
			}, { preferCache: true })
		},
		async handlePreviousQuestion(): Promise<void> {
			if (!this.canGoPrevious || this.uiState.loading || this.uiState.submitting || this.autoJumping) {
				return
			}
			if (this.hasPendingState) {
				const confirmed = await showModal({
					title: '返回上一题',
					content: this.awaitingSelfJudgement
						? '当前简答题还没完成自判，返回上一题后这次作答不会记入进度。确定返回吗？'
						: '当前题目还有未提交内容，返回上一题后这次输入不会保留。确定返回吗？',
					confirmText: '返回上一题'
				})
				if (!confirmed) {
					return
				}
			}
			if (!this.currentBank || !this.sessionState.previousQuestionId) {
				return
			}
			this.cancelAutoJump()
			await this.loadSession({
				bankId: this.currentBank.bankId,
				questionId: this.sessionState.previousQuestionId,
				sourceType: this.routeQuery.sourceType ?? null,
				sourceStatus: this.routeQuery.sourceStatus ?? null,
				keyword: this.routeQuery.keyword ?? null
			})
		},
		async handleSwitchBank(item: KyzzPracticeBankViewRecord): Promise<void> {
			if (this.autoJumping) {
				return
			}
			if (this.currentBank && this.currentBank.bankId === item.bankId) {
				this.closeSwitchPopup()
				return
			}
			if (this.hasPendingState) {
				const confirmed = await showModal({
					title: '切换题库',
					content: this.awaitingSelfJudgement
						? '当前简答题还没确认结果，切换后这次作答不会记入进度。确定切换吗？'
						: '当前题目还有未提交的内容，切换后不会保留这次输入。确定切换吗？',
					confirmText: '继续切换'
				})
				if (!confirmed) {
					return
				}
			}
			this.closeSwitchPopup()
			this.cancelAutoJump()
			await this.loadSession({
				bankId: item.bankId,
				sourceType: null,
				sourceStatus: null,
				keyword: null
			})
		},
		openSwitchPopup(): void {
			if (this.autoJumping || !this.sessionState.switchableBanks.length || this.uiState.emptyState === 'no_bank') {
				return
			}
			this.switchPopupVisible = true
			;(this.$refs.switchPopup as UniPopupRef | undefined)?.open()
		},
		closeSwitchPopup(): void {
			this.switchPopupVisible = false
			;(this.$refs.switchPopup as UniPopupRef | undefined)?.close()
		},
		handleSwitchPopupChange(event: UniPopupChangeEvent): void {
			this.switchPopupVisible = Boolean(event?.show)
		},
		handleEmptyPrimary(): void {
			if (this.uiState.emptyState === 'no_bank' || this.uiState.emptyState === 'no_question') {
				this.goPublicBanks()
			}
		},
		handleEmptySecondary(): void {
			if (this.uiState.emptyState === 'no_bank') {
				this.goQuestionBank()
				return
			}
			this.goStudy()
		},
		goStudy(): void {
			uni.switchTab({
				url: '/pages/kyzz/study/index'
			})
		},
		goQuestionBank(): void {
			uni.switchTab({
				url: '/pages/kyzz/question-bank/index'
			})
		},
		goPublicBanks(): void {
			uni.navigateTo({
				url: '/pages/kyzz/question-bank/public'
			})
		},
		goWrongBook(): void {
			uni.navigateTo({
				url: '/pages/kyzz/wrong-book/index'
			})
		},
		scrollToTop(): void {
			uni.pageScrollTo({
				scrollTop: 0,
				duration: 280
			})
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.practice-page {
	min-height: 100vh;
	background:
		linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
	box-sizing: border-box;
}

.practice-page__content {
	padding: 0 0 calc(env(safe-area-inset-bottom) + 172rpx);
	box-sizing: border-box;
}

.practice-page__inner {
	position: relative;
	min-height: calc(100vh - 140rpx);
}

.practice-page__halo {
	position: absolute;
	border-radius: 999rpx;
	filter: blur(28rpx);
	pointer-events: none;
}

.practice-page__halo--top {
	left: 40rpx;
	top: 32rpx;
	width: 240rpx;
	height: 240rpx;
	background: rgba(149, 168, 197, 0.08);
}

.practice-page__halo--bottom {
	right: 24rpx;
	bottom: 180rpx;
	width: 220rpx;
	height: 220rpx;
	background: rgba(116, 140, 178, 0.07);
}

.practice-page__scroll,
.practice-page__state-shell {
	position: relative;
	z-index: 1;
	padding: 18rpx 24rpx calc(env(safe-area-inset-bottom) + 140rpx);
}

.practice-page__inline-notice {
	display: block;
	margin-bottom: 18rpx;
}

.practice-page__bottom-space {
	height: 20rpx;
}

.practice-page__scroll-top {
	position: fixed;
	right: 28rpx;
	bottom: calc(env(safe-area-inset-bottom) + 220rpx);
	z-index: 20;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 84rpx;
	height: 84rpx;
	margin: 0;
	padding: 0;
	border-radius: 50%;
	background: #ffffff;
	box-shadow:
		0 16rpx 32rpx rgba(54, 68, 90, 0.14),
		inset 0 0 0 1rpx #cbd5e4;
}

.practice-page__scroll-top::after {
	border: 0;
}

.practice-page .uni-popup {
	z-index: 1200;
}

.practice-page__loading-mask {
	position: fixed;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 1100;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 48rpx calc(env(safe-area-inset-bottom) + 132rpx);
	background: rgba(244, 248, 252, 0.68);
	backdrop-filter: blur(6rpx);
	box-sizing: border-box;
}

.practice-page__loading-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 288rpx;
	padding: 34rpx 32rpx 32rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-shadow:
		0 22rpx 52rpx rgba(54, 68, 90, 0.13),
		inset 0 0 0 1rpx #d2dbe7;
	box-sizing: border-box;
}

.practice-page__loading-spinner {
	width: 44rpx;
	height: 44rpx;
	margin-bottom: 20rpx;
	border-radius: 50%;
	border: 4rpx solid rgba(113, 126, 148, 0.18);
	border-top-color: #5f6d83;
	animation: practice-loading-spin 0.82s linear infinite;
}

.practice-page__loading-title {
	font-size: 27rpx;
	line-height: 1.35;
	font-weight: 700;
	color: #263142;
	text-align: center;
}

.practice-page__loading-desc {
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.45;
	color: #728094;
	text-align: center;
}

@keyframes practice-loading-spin {
	to {
		transform: rotate(360deg);
	}
}

@media screen and (max-width: 375px) {
	.practice-page__scroll,
	.practice-page__state-shell {
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: calc(env(safe-area-inset-bottom) + 140rpx);
	}
}
</style>
