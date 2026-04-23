<template>
	<!-- AI 索引: KYZZ 练习页 -->
	<page-shell
		current="practice"
		root-class="practice-page"
		content-class="practice-page__content"
		:show-tabbar="!switchPopupVisible"
	>
		<view class="practice-page__inner">
			<view class="practice-page__halo practice-page__halo--top"></view>
			<view class="practice-page__halo practice-page__halo--bottom"></view>

			<view v-if="uiState.loading && !uiState.loadedOnce" class="practice-page__state-shell">
				<practice-state-notice :notice="loadingNotice" />
			</view>

			<view v-else-if="resolvedStateNotice" class="practice-page__state-shell">
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
					:question="question"
					:selected-option-keys="answerDraft.selectedOptionKeys"
					:answer-text="answerDraft.answerText"
					:review-result="reviewState.result"
					@select-option="handleOptionTap"
					@change-answer-text="handleAnswerTextChange"
					@open-switcher="openSwitchPopup"
				/>

				<practice-review-panel
					v-if="reviewState.result"
					:review-result="reviewState.result"
					:awaiting-self-judgement="awaitingSelfJudgement"
					:user-answer-display="userAnswerDisplay"
					:standard-answer-display="standardAnswerDisplay"
					:show-wrong-book-hint="showWrongBookHint"
					@open-wrong-book="goWrongBook"
				/>

				<practice-footer-actions
					:review-result="reviewState.result"
					:awaiting-self-judgement="awaitingSelfJudgement"
					:can-submit="canSubmit"
					:submitting="uiState.submitting"
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
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import PracticeStateNotice from '@/components/kyzz/practice/PracticeStateNotice.vue'
import PracticeQuestionPanel from '@/components/kyzz/practice/PracticeQuestionPanel.vue'
import PracticeReviewPanel from '@/components/kyzz/practice/PracticeReviewPanel.vue'
import PracticeFooterActions from '@/components/kyzz/practice/PracticeFooterActions.vue'
import PracticeBankSwitcher from '@/components/kyzz/practice/PracticeBankSwitcher.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getPracticeSession, reviewPracticeQuestion, selfJudgePracticeQuestion } from '@/pages/kyzz/api/practice'
import { consumePracticeLaunchTarget } from '@/pages/kyzz/practice/navigation'
import type {
	KyzzPracticeAnswerDraftState,
	KyzzPracticeBankViewRecord,
	KyzzPracticeNoticeViewModel,
	KyzzPracticeReviewRequest,
	KyzzPracticeReviewState,
	KyzzPracticeSelfJudgementRequest,
	KyzzPracticeSessionQuery,
	KyzzPracticeSessionState,
	KyzzPracticeUiState,
	UniPopupRef
} from '@/pages/kyzz/practice/types'
import {
	buildCompletedNotice,
	buildLoadingNotice,
	buildNoBankNotice,
	buildNoQuestionNotice,
	createEmptyPracticeAnswerDraft,
	createEmptyPracticeReviewState,
	createEmptyPracticeSession,
	createEmptyPracticeUiState,
	normalizePracticeReviewResult,
	normalizePracticeSession,
	resolvePracticeEmptyState
} from '@/pages/kyzz/practice/view'

interface PracticePageQuery {
	bankId?: string
	questionId?: string
	freshAttempt?: string
}

interface PracticePageState {
	sessionState: KyzzPracticeSessionState
	answerDraft: KyzzPracticeAnswerDraftState
	reviewState: KyzzPracticeReviewState
	uiState: KyzzPracticeUiState
	routeQuery: KyzzPracticeSessionQuery
	switchPopupVisible: boolean
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

function hasRouteTarget(query: KyzzPracticeSessionQuery): boolean {
	return query.bankId !== null && query.bankId !== undefined
		|| query.questionId !== null && query.questionId !== undefined
}

function mergeSessionQuery(base: KyzzPracticeSessionQuery, patch: KyzzPracticeSessionQuery): KyzzPracticeSessionQuery {
	return {
		bankId: patch.bankId ?? base.bankId ?? null,
		questionId: patch.questionId ?? base.questionId ?? null,
		freshAttempt: patch.freshAttempt ?? base.freshAttempt ?? null
	}
}

export default defineComponent({
	name: 'PracticePage',
	components: {
		PageShell,
		PracticeStateNotice,
		PracticeQuestionPanel,
		PracticeReviewPanel,
		PracticeFooterActions,
		PracticeBankSwitcher
	},
	data(): PracticePageState {
		return {
			sessionState: createEmptyPracticeSession(),
			answerDraft: createEmptyPracticeAnswerDraft(),
			reviewState: createEmptyPracticeReviewState(),
			uiState: createEmptyPracticeUiState(),
			routeQuery: {
				bankId: null,
				questionId: null,
				freshAttempt: null
			},
			switchPopupVisible: false
		}
	},
	computed: {
		currentBank(): KyzzPracticeBankViewRecord | null {
			return this.sessionState.activeBank
		},
		question() {
			return this.sessionState.question
		},
		awaitingSelfJudgement(): boolean {
			return Boolean(this.reviewState.result && this.reviewState.result.requiresSelfJudgement && this.reviewState.result.isCorrect === null)
		},
		canSubmit(): boolean {
			if (!this.question || this.uiState.submitting || Boolean(this.reviewState.result)) {
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
			return Boolean(this.sessionState.previousQuestionId)
		},
		canGoNext(): boolean {
			return Boolean(
				this.reviewState.result
				&& !this.awaitingSelfJudgement
				&& this.reviewState.result.nextQuestionId
			)
		},
		nextButtonText(): string {
			if (this.reviewState.result?.completedBank) {
				return '再刷一遍'
			}
			return '下一题'
		},
		showWrongBookHint(): boolean {
			return Boolean(this.reviewState.result && this.reviewState.result.isCorrect === false && !this.awaitingSelfJudgement)
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
		loadingNotice(): KyzzPracticeNoticeViewModel {
			return buildLoadingNotice()
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
			freshAttempt: parseOptionalBoolean(query.freshAttempt)
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
	methods: {
		async bootstrapAndLoad(query: KyzzPracticeSessionQuery): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadSession(query)
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '刷题页面加载失败'),
					icon: 'none'
				})
			}
		},
		async loadSession(query: KyzzPracticeSessionQuery): Promise<void> {
			if (this.uiState.loading) {
				return
			}
			this.uiState.loading = true
			try {
				const result = await getPracticeSession(query)
				this.sessionState = normalizePracticeSession(result)
				this.reviewState = {
					result: this.sessionState.reviewResult
				}
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
					freshAttempt: null
				}
				this.uiState.emptyState = null
				this.uiState.loadedOnce = true
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 0
				})
			} catch (error) {
				this.sessionState = createEmptyPracticeSession()
				this.answerDraft = createEmptyPracticeAnswerDraft()
				this.reviewState = createEmptyPracticeReviewState()
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
		handleAnswerTextChange(value: string): void {
			this.answerDraft.answerText = value
		},
		handleOptionTap(optionKey: string): void {
			if (this.reviewState.result || !this.question) {
				return
			}
			if (this.question.questionType === 'single') {
				this.answerDraft.selectedOptionKeys = [optionKey]
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
			}
		},
		buildUsedSeconds(): number {
			const diff = Math.floor((Date.now() - this.answerDraft.questionStartedAt) / 1000)
			return diff > 0 ? diff : 0
		},
		async handleReview(): Promise<void> {
			if (!this.question || !this.currentBank || !this.canSubmit || this.uiState.submitting) {
				return
			}
			this.uiState.submitting = true
			try {
				const payload: KyzzPracticeReviewRequest = {
					bankId: this.currentBank.bankId,
					usedSeconds: this.buildUsedSeconds()
				}
				if (this.question.questionType === 'short') {
					payload.answerText = this.answerDraft.answerText.trim()
				} else {
					payload.selectedOptionKeys = this.answerDraft.selectedOptionKeys
				}
				const result = await reviewPracticeQuestion(this.question.id, payload)
				this.reviewState.result = normalizePracticeReviewResult(result)
				this.replaceBankRecord(this.reviewState.result.updatedBank)
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '查看答案失败'),
					icon: 'none'
				})
			} finally {
				this.uiState.submitting = false
			}
		},
		async handleSelfJudgement(selfJudgedCorrect: boolean): Promise<void> {
			if (!this.question || !this.currentBank || this.uiState.submitting || !this.awaitingSelfJudgement) {
				return
			}
			this.uiState.submitting = true
			try {
				const payload: KyzzPracticeSelfJudgementRequest = {
					bankId: this.currentBank.bankId,
					answerText: this.answerDraft.answerText.trim(),
					usedSeconds: this.buildUsedSeconds(),
					selfJudgedCorrect
				}
				const result = await selfJudgePracticeQuestion(this.question.id, payload)
				this.reviewState.result = normalizePracticeReviewResult(result)
				this.replaceBankRecord(this.reviewState.result.updatedBank)
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
		async handleNextQuestion(): Promise<void> {
			if (!this.reviewState.result || !this.currentBank || !this.reviewState.result.nextQuestionId) {
				return
			}
			await this.loadSession({
				bankId: this.currentBank.bankId,
				questionId: this.reviewState.result.nextQuestionId
			})
		},
		async handlePreviousQuestion(): Promise<void> {
			if (!this.canGoPrevious || this.uiState.loading || this.uiState.submitting) {
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
			await this.loadSession({
				bankId: this.currentBank.bankId,
				questionId: this.sessionState.previousQuestionId
			})
		},
		async handleSwitchBank(item: KyzzPracticeBankViewRecord): Promise<void> {
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
			await this.loadSession({
				bankId: item.bankId
			})
		},
		openSwitchPopup(): void {
			if (!this.sessionState.switchableBanks.length || this.uiState.emptyState === 'no_bank') {
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
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.practice-page {
	min-height: 100vh;
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.99) 0%, rgba(242, 246, 251, 0.97) 42%, rgba(229, 236, 245, 0.96) 100%);
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
	filter: blur(24rpx);
	pointer-events: none;
}

.practice-page__halo--top {
	left: 40rpx;
	top: 32rpx;
	width: 240rpx;
	height: 240rpx;
	background: rgba(176, 191, 217, 0.18);
}

.practice-page__halo--bottom {
	right: 24rpx;
	bottom: 180rpx;
	width: 220rpx;
	height: 220rpx;
	background: rgba(201, 212, 230, 0.18);
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

.practice-page .uni-popup {
	z-index: 1200;
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
