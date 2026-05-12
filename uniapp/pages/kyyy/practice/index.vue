<!--
@file KyyyPracticePage
@project pipker-do
@module 考研英语 / 小程序背词页
@description 编排默认词库下的背词会话页，负责会话加载、反馈提交以及子组件状态分发。
@logic 1. 加载或恢复学习会话；2. 用户先选择认识程度再显示释义并确认反馈；3. 将卡片、底栏和导航拆分到独立组件后统一调度。
@dependencies API: @/pages/kyyy/api/practice, Component: PageShell, Component: PracticeSessionCard, Shared: @/shared/session/session
@index_tags 考研英语, 背词页, 学习卡片, 复习会话
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-practice-page theme-page"
		content-style="padding: 0 24rpx 220rpx;"
		:show-navbar="false"
	>
		<practice-session-navbar @exit="handleExit" />

		<view class="kyyy-practice-page__ambient kyyy-practice-page__ambient--left"></view>
		<view class="kyyy-practice-page__ambient kyyy-practice-page__ambient--right"></view>

		<view v-if="sessionState.loading && !sessionState.loaded" class="kyyy-practice-page__state-card">
			<view class="kyyy-practice-page__loader"></view>
			<text class="kyyy-practice-page__state-title">正在整理学习会话</text>
			<text class="kyyy-practice-page__state-desc">会优先恢复你还没完成的那一轮。</text>
		</view>

		<practice-session-card
			v-else-if="sessionState.status === 'active' && sessionState.currentCard"
			:card="sessionState.currentCard"
			:revealed="sessionState.revealed"
			:progress-text="progressText"
			:progress-percent="progressPercent"
			:current-source-label="currentSourceLabel"
			:show-source-star-badge="showSourceStarBadge"
			:round-star-slots="roundStarSlots"
			:display-round-star-count="displayRoundStarCount"
			:round-stars-state-class="roundStarsStateClass"
		/>

		<view v-else-if="sessionState.status === 'completed'" class="kyyy-practice-page__result-card">
			<text class="kyyy-practice-page__result-title">这一轮结束了</text>
			<text class="kyyy-practice-page__result-desc">系统已经按你的反馈更新了后续节奏。</text>
			<view class="kyyy-practice-page__result-grid">
				<view class="kyyy-practice-page__result-item">
					<text class="kyyy-practice-page__result-value">{{ sessionState.completionSummary?.passedNewCount || 0 }}</text>
					<text class="kyyy-practice-page__result-label">通过新词</text>
				</view>
				<view class="kyyy-practice-page__result-item">
					<text class="kyyy-practice-page__result-value">{{ sessionState.completionSummary?.dueSoonCount || 0 }}</text>
					<text class="kyyy-practice-page__result-label">尽快回看</text>
				</view>
				<view class="kyyy-practice-page__result-item">
					<text class="kyyy-practice-page__result-value">{{ sessionState.completionSummary?.unknownCount || 0 }}</text>
					<text class="kyyy-practice-page__result-label">不认识</text>
				</view>
			</view>
			<view class="kyyy-practice-page__panel-actions">
				<view class="kyyy-practice-page__panel-button" @tap="handlePrimaryAction">
					<text>{{ primaryActionText }}</text>
				</view>
				<view class="kyyy-practice-page__panel-button kyyy-practice-page__panel-button--ghost" @tap="handleExit">
					<text>返回首页</text>
				</view>
			</view>
		</view>

		<view v-else class="kyyy-practice-page__state-card">
			<text class="kyyy-practice-page__state-title">{{ sessionState.emptyState?.title || '当前暂无可学习内容' }}</text>
			<text class="kyyy-practice-page__state-desc">{{ sessionState.emptyState?.description || '稍后再回来看看。' }}</text>
			<view class="kyyy-practice-page__panel-actions">
				<view class="kyyy-practice-page__panel-button" @tap="handlePrimaryAction">
					<text>{{ sessionState.emptyState?.actionText || '返回首页' }}</text>
				</view>
				<view class="kyyy-practice-page__panel-button kyyy-practice-page__panel-button--ghost" @tap="handleExit">
					<text>返回首页</text>
				</view>
			</view>
		</view>

		<practice-session-footer-actions
			v-if="sessionState.status === 'active'"
			:revealed="sessionState.revealed"
			:footer-hint-text="footerHintText"
			:show-mistake-action="showMistakeAction"
			@select-rating="handleSelectRating"
			@next="handleAdvanceToNextWord"
			@mistake="handleMarkAsMistake"
		/>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import PracticeSessionNavbar from '@/components/kyyy/practice/PracticeSessionNavbar.vue'
import PracticeSessionCard from '@/components/kyyy/practice/PracticeSessionCard.vue'
import PracticeSessionFooterActions from '@/components/kyyy/practice/PracticeSessionFooterActions.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getPracticeSession, submitPracticeFeedback } from '@/pages/kyyy/api/practice'
import type {
	KyyyPracticeRating,
	KyyyPracticeSessionMode,
	KyyyPracticeSessionState
} from '@/pages/kyyy/practice/types'
import {
	createEmptyPracticeSession,
	normalizePracticeMode,
	normalizePracticeSession
} from '@/pages/kyyy/practice/view'

interface PracticePageState {
	entryMode: KyyyPracticeSessionMode
	sessionState: KyyyPracticeSessionState
	cardShownAt: number
	cardToken: string
	pendingRating: KyyyPracticeRating | ''
}

export default defineComponent({
	name: 'KyyyPracticePage',
	components: {
		PageShell,
		PracticeSessionNavbar,
		PracticeSessionCard,
		PracticeSessionFooterActions
	},
	data(): PracticePageState {
		return {
			entryMode: 'study',
			sessionState: createEmptyPracticeSession('study'),
			cardShownAt: 0,
			cardToken: '',
			pendingRating: ''
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.entryMode = normalizePracticeMode(query?.mode)
		this.sessionState = createEmptyPracticeSession(this.entryMode)
	},
	onShow() {
		this.bootstrapAndLoad(false)
	},
	computed: {
		progressText(): string {
			const total = this.progressTotalNumber
			if (!total) {
				return '0/20'
			}
			return `${this.progressCurrentNumber}/${total}`
		},
		progressCurrentNumber(): number {
			const progress = this.sessionState.progressSummary
			const fallbackCurrent = this.sessionState.status === 'active' && this.sessionState.currentCard
				? (progress.completedCards || 0) + 1
				: progress.completedCards || 0
			return Math.max(progress.currentIndex || fallbackCurrent || 0, 0)
		},
		progressTotalNumber(): number {
			return Math.max(this.sessionState.progressSummary.totalCards || 0, 0)
		},
		progressPercent(): number {
			if (!this.progressTotalNumber) {
				return 0
			}
			return Math.min(100, Math.max(0, Math.round((this.progressCurrentNumber / this.progressTotalNumber) * 100)))
		},
		currentSourceLabel(): string {
			const sourceType = this.sessionState.currentCard?.sourceType || ''
			if (sourceType === 'review') {
				return '到期复习'
			}
			if (sourceType === 'relearn') {
				return '本轮回看'
			}
			return '新词学习'
		},
		showSourceStarBadge(): boolean {
			const sourceType = this.sessionState.currentCard?.sourceType || ''
			return sourceType !== 'review' && sourceType !== 'relearn'
		},
		baseRoundStarCount(): number {
			return Math.max(1, Math.min(Number(this.sessionState.currentCard?.roundNo || 1), 3))
		},
		displayRoundStarCount(): number {
			if (this.sessionState.revealed && this.pendingRating === 'know') {
				return Math.min(this.baseRoundStarCount + 1, 3)
			}
			return this.baseRoundStarCount
		},
		roundStarSlots(): number[] {
			return [0, 1, 2]
		},
		roundStarsStateClass(): string {
			if (!this.sessionState.revealed) {
				return ''
			}
			if (this.pendingRating === 'know') {
				return 'is-success'
			}
			if (this.pendingRating === 'unknown') {
				return 'is-failure'
			}
			if (this.pendingRating === 'fuzzy') {
				return 'is-fuzzy'
			}
			return ''
		},
		canChooseRating(): boolean {
			return this.sessionState.status === 'active'
				&& !!this.sessionState.sessionId
				&& !!this.sessionState.currentCard?.wordId
				&& !this.sessionState.revealed
				&& !this.sessionState.loading
				&& !this.sessionState.submitting
		},
		canConfirmPendingRating(): boolean {
			return this.sessionState.status === 'active'
				&& !!this.sessionState.sessionId
				&& !!this.sessionState.currentCard?.wordId
				&& this.sessionState.revealed
				&& !!this.pendingRating
				&& !this.sessionState.loading
				&& !this.sessionState.submitting
		},
		showMistakeAction(): boolean {
			return this.pendingRating === 'know' || this.pendingRating === 'fuzzy'
		},
		footerHintText(): string {
			if (!this.sessionState.revealed) {
				return '先按第一反应判断，释义会在你选择后展开。'
			}
			if (this.pendingRating === 'unknown') {
				return '释义已经展开，看完后直接进入下一词。'
			}
			if (this.pendingRating === 'know') {
				return '你刚才选了“认识”，确认无误就下一词；如果记偏了，点“记错了”。'
			}
			if (this.pendingRating === 'fuzzy') {
				return '你刚才选了“模糊”，确认后进入下一词；如果其实记错了，点“记错了”。'
			}
			return ''
		},
		primaryActionText(): string {
			const mode = this.sessionState.completionSummary?.primaryActionMode || this.sessionState.emptyState?.suggestedMode
			if (mode === 'review') {
				return '继续复习'
			}
			if (mode === 'study') {
				return '去学习'
			}
			return '返回首页'
		}
	},
	methods: {
		async bootstrapAndLoad(freshAttempt: boolean): Promise<void> {
			this.sessionState = {
				...this.sessionState,
				loading: true
			}
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-practice] bootstrap auth failed', error)
			}
			await this.loadSession(freshAttempt)
		},
		async loadSession(freshAttempt: boolean): Promise<void> {
			try {
				const response = await getPracticeSession(this.entryMode, freshAttempt)
				const nextState = normalizePracticeSession(response, this.entryMode)
				if (nextState.status === 'bank_required') {
					this.sessionState = {
						...nextState,
						loading: false
					}
					this.routeToWordBank()
					return
				}
				this.applySessionState(nextState)
			} catch (error) {
				console.warn('[kyyy-practice] load session failed', error)
				this.sessionState = {
					...this.sessionState,
					loading: false,
					loaded: true
				}
				uni.showToast({
					title: '学习会话加载失败',
					icon: 'none'
				})
			}
		},
		applySessionState(nextState: KyyyPracticeSessionState): void {
			const cardToken = nextState.status === 'active' && nextState.currentCard?.wordId
				? `${nextState.sessionId || 0}:${nextState.currentCard.wordId}:${nextState.progressSummary.completedCards}`
				: ''
			const isNextCard = !!cardToken && cardToken !== this.cardToken
			this.sessionState = {
				...nextState,
				loading: false,
				submitting: false,
				revealed: false
			}
			this.pendingRating = ''
			if (isNextCard) {
				this.cardToken = cardToken
				this.cardShownAt = Date.now()
				return
			}
			if (!cardToken) {
				this.cardToken = ''
				this.cardShownAt = 0
			}
		},
		handleSelectRating(rating: KyyyPracticeRating): void {
			if (!this.canChooseRating) {
				return
			}
			this.pendingRating = rating
			this.sessionState = {
				...this.sessionState,
				revealed: true
			}
		},
		async submitPendingRating(rating: KyyyPracticeRating): Promise<void> {
			if (!this.canConfirmPendingRating || !this.sessionState.sessionId || !this.sessionState.currentCard?.wordId) {
				return
			}
			this.sessionState = {
				...this.sessionState,
				submitting: true
			}
			try {
				const response = await submitPracticeFeedback(this.sessionState.sessionId, {
					wordId: this.sessionState.currentCard.wordId,
					rating,
					revealed: true,
					responseDurationMs: Math.max(Date.now() - this.cardShownAt, 0)
				})
				this.applySessionState(normalizePracticeSession(response, this.entryMode))
			} catch (error) {
				console.warn('[kyyy-practice] submit rating failed', error)
				this.sessionState = {
					...this.sessionState,
					submitting: false
				}
				uni.showToast({
					title: '提交失败，请重试',
					icon: 'none'
				})
			}
		},
		handleAdvanceToNextWord(): void {
			if (!this.pendingRating) {
				return
			}
			this.submitPendingRating(this.pendingRating)
		},
		handleMarkAsMistake(): void {
			if (!this.showMistakeAction) {
				return
			}
			this.submitPendingRating('unknown')
		},
		handlePrimaryAction(): void {
			if (this.sessionState.status === 'completed' && this.sessionState.completionSummary?.primaryActionMode) {
				this.switchModeAndReload(this.sessionState.completionSummary.primaryActionMode)
				return
			}
			if (this.sessionState.status === 'empty' && this.sessionState.emptyState?.suggestedMode) {
				this.switchModeAndReload(this.sessionState.emptyState.suggestedMode)
				return
			}
			this.handleExit()
		},
		switchModeAndReload(mode: KyyyPracticeSessionMode): void {
			this.entryMode = mode
			this.sessionState = createEmptyPracticeSession(mode)
			this.cardShownAt = 0
			this.cardToken = ''
			this.pendingRating = ''
			this.bootstrapAndLoad(true)
		},
		handleExit(): void {
			uni.reLaunch({
				url: '/pages/kyyy/index',
				fail: (error: unknown) => {
					console.warn('[kyyy-practice] exit failed', error)
				}
			})
		},
		routeToWordBank(): void {
			uni.reLaunch({
				url: `/pages/kyyy/word-bank/index?mode=${this.entryMode}`,
				fail: (error: unknown) => {
					console.warn('[kyyy-practice] open word bank failed', error)
					uni.showToast({
						title: '打开词库失败',
						icon: 'none'
					})
				}
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-practice-page__state-card,
.kyyy-practice-page__result-card {
	position: relative;
	margin-top: 16rpx;
	padding: 34rpx 30rpx 36rpx;
	border-radius: 34rpx;
	background:
		linear-gradient(180deg, rgba(238, 246, 252, 0.95) 0%, rgba(246, 240, 233, 0.94) 100%);
	box-shadow:
		0 24rpx 54rpx rgba(67, 83, 105, 0.1),
		inset 0 0 0 1rpx rgba(226, 233, 241, 0.92);
	overflow: hidden;
}

.kyyy-practice-page__state-card::before,
.kyyy-practice-page__result-card::before {
	content: '';
	position: absolute;
	inset: 0;
	background:
		radial-gradient(circle at top left, rgba(255, 255, 255, 0.58), transparent 42%),
		radial-gradient(circle at bottom right, rgba(241, 222, 205, 0.34), transparent 40%);
	pointer-events: none;
}

.kyyy-practice-page__result-title,
.kyyy-practice-page__result-desc,
.kyyy-practice-page__result-grid,
.kyyy-practice-page__state-title,
.kyyy-practice-page__state-desc,
.kyyy-practice-page__panel-actions {
	position: relative;
	z-index: 1;
}

.kyyy-practice-page__state-card,
.kyyy-practice-page__result-card {
	text-align: center;
}

.kyyy-practice-page__state-title,
.kyyy-practice-page__result-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.3;
	font-weight: 780;
	color: #233041;
}

.kyyy-practice-page__state-desc,
.kyyy-practice-page__result-desc {
	display: block;
	margin-top: 16rpx;
	font-size: 25rpx;
	line-height: 1.7;
	color: #4e5d72;
}

.kyyy-practice-page__result-grid {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 18rpx;
	margin-top: 30rpx;
}

.kyyy-practice-page__result-item {
	padding: 24rpx 14rpx;
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.72);
	box-shadow: inset 0 0 0 1rpx rgba(226, 232, 239, 0.9);
}

.kyyy-practice-page__result-value {
	display: block;
	font-size: 42rpx;
	line-height: 1.1;
	font-weight: 800;
	color: #203041;
}

.kyyy-practice-page__result-label {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.5;
	color: #556478;
}

.kyyy-practice-page__panel-actions {
	margin-top: 30rpx;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.kyyy-practice-page__panel-button {
	height: 82rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, rgba(89, 110, 139, 0.96), rgba(73, 90, 116, 0.98));
	box-shadow: 0 18rpx 36rpx rgba(73, 92, 116, 0.16);
	font-size: 27rpx;
	font-weight: 760;
	color: #ffffff;
}

.kyyy-practice-page__panel-button--ghost {
	background: rgba(255, 255, 255, 0.76);
	box-shadow: inset 0 0 0 1rpx rgba(220, 227, 236, 0.92);
	color: #586879;
}

.kyyy-practice-page__loader {
	width: 68rpx;
	height: 68rpx;
	margin: 0 auto 18rpx;
	border-radius: 999rpx;
	border: 6rpx solid rgba(117, 135, 159, 0.16);
	border-top-color: rgba(92, 113, 142, 0.9);
	animation: kyyy-practice-spin 1s linear infinite;
}

.kyyy-practice-page__ambient {
	position: fixed;
	z-index: 0;
	border-radius: 999rpx;
	filter: blur(16rpx);
	opacity: 0.7;
	pointer-events: none;
}

.kyyy-practice-page__ambient--left {
	top: 132rpx;
	left: -110rpx;
	width: 260rpx;
	height: 260rpx;
	background: radial-gradient(circle, rgba(199, 226, 243, 0.82) 0%, rgba(199, 226, 243, 0) 72%);
}

.kyyy-practice-page__ambient--right {
	right: -96rpx;
	bottom: 220rpx;
	width: 248rpx;
	height: 248rpx;
	background: radial-gradient(circle, rgba(241, 217, 200, 0.74) 0%, rgba(241, 217, 200, 0) 74%);
}

@keyframes kyyy-practice-spin {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}
</style>
