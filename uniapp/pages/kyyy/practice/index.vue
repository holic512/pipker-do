<!--
@file KyyyPracticePage
@project pipker-do
@module 考研英语 / 小程序背词页
@description 提供默认词库下的学习与复习卡片流，支持揭晓释义后评分、会话恢复与完成总结。
@logic 1. 加载或恢复学习会话；2. 在揭晓释义后提交认识程度反馈；3. 在缺少默认词库、空态和完成态之间切换。
@dependencies API: @/pages/kyyy/api/practice, Component: PageShell, Shared: @/shared/session/session
@index_tags 考研英语, 背词页, 学习卡片, 复习会话
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-practice-page theme-page"
		content-style="padding: 0 24rpx 220rpx;"
		:show-navbar="false"
	>
		<practice-session-navbar :progress-text="progressText" @exit="handleExit" />

		<view class="kyyy-practice-page__ambient kyyy-practice-page__ambient--left"></view>
		<view class="kyyy-practice-page__ambient kyyy-practice-page__ambient--right"></view>

		<view v-if="sessionState.loading && !sessionState.loaded" class="kyyy-practice-page__state-card">
			<view class="kyyy-practice-page__loader"></view>
			<text class="kyyy-practice-page__state-title">正在整理学习会话</text>
			<text class="kyyy-practice-page__state-desc">会优先恢复你还没完成的那一轮。</text>
		</view>

		<view v-else-if="sessionState.status === 'active' && sessionState.currentCard" class="kyyy-practice-page__card">
			<view class="kyyy-practice-page__card-top">
				<view class="kyyy-practice-page__card-tag">{{ currentSourceLabel }}</view>
				<view class="kyyy-practice-page__card-tag kyyy-practice-page__card-tag--muted">
					第 {{ sessionState.currentCard.roundNo }} 轮
				</view>
			</view>

			<view class="kyyy-practice-page__card-head">
				<text class="kyyy-practice-page__word">{{ sessionState.currentCard.wordText }}</text>
				<view v-if="phoneticText" class="kyyy-practice-page__phonetic-row">
					<view class="kyyy-practice-page__phonetic-pill">
						<text>美</text>
						<uni-icons type="sound" size="12" color="#7d8898" />
					</view>
					<text class="kyyy-practice-page__phonetic">{{ phoneticText }}</text>
				</view>
			</view>

			<view v-if="!sessionState.revealed" class="kyyy-practice-page__memory-panel">
				<text class="kyyy-practice-page__memory-title">先在脑中回忆词义</text>
				<text class="kyyy-practice-page__memory-desc">想到后再点“查看释义”，再判断自己是认识、模糊还是不认识。</text>
				<view class="kyyy-practice-page__reveal-button" @tap="handleReveal">
					<text>查看释义</text>
				</view>
			</view>

			<view v-else class="kyyy-practice-page__detail-panel">
				<view class="kyyy-practice-page__meaning-row">
					<text v-if="sessionState.currentCard.partOfSpeech" class="kyyy-practice-page__part-of-speech">
						{{ sessionState.currentCard.partOfSpeech }}
					</text>
					<text class="kyyy-practice-page__meaning">{{ sessionState.currentCard.meaningCn || '释义暂缺' }}</text>
				</view>

				<view class="kyyy-practice-page__detail-block">
					<view class="kyyy-practice-page__detail-title">
						<view class="kyyy-practice-page__detail-mark"></view>
						<text>例句</text>
					</view>
					<text class="kyyy-practice-page__detail-text">
						{{ sessionState.currentCard.exampleSentence || '当前单词暂未补充例句。' }}
					</text>
					<text v-if="sessionState.currentCard.exampleTranslation" class="kyyy-practice-page__detail-subtext">
						{{ sessionState.currentCard.exampleTranslation }}
					</text>
				</view>

				<view v-if="sessionState.currentCard.relatedWords.length" class="kyyy-practice-page__detail-block">
					<view class="kyyy-practice-page__detail-title">
						<view class="kyyy-practice-page__detail-mark"></view>
						<text>相关词</text>
					</view>
					<view class="kyyy-practice-page__related-list">
						<view
							v-for="item in sessionState.currentCard.relatedWords"
							:key="`${item.id || item.relatedWordText}-${item.relationType}`"
							class="kyyy-practice-page__related-item"
						>
							<text class="kyyy-practice-page__related-word">{{ item.relatedWordText }}</text>
							<text class="kyyy-practice-page__related-meaning">{{ item.meaningCn }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

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

		<view v-if="sessionState.status === 'active'" class="kyyy-practice-page__footer">
			<text class="kyyy-practice-page__footer-hint">
				{{ sessionState.revealed ? '按照刚才的回忆状态，给这个单词一个判断。' : '先点“查看释义”，再选择认识程度。' }}
			</text>
			<view class="kyyy-practice-page__footer-actions">
				<view
					class="kyyy-practice-page__rate-button kyyy-practice-page__rate-button--know"
					:class="{ 'is-disabled': !canSubmitFeedback }"
					@tap="handleSubmitRating('know')"
				>
					<text class="kyyy-practice-page__rate-text">认识</text>
				</view>
				<view
					class="kyyy-practice-page__rate-button kyyy-practice-page__rate-button--fuzzy"
					:class="{ 'is-disabled': !canSubmitFeedback }"
					@tap="handleSubmitRating('fuzzy')"
				>
					<text class="kyyy-practice-page__rate-text">模糊</text>
				</view>
				<view
					class="kyyy-practice-page__rate-button kyyy-practice-page__rate-button--unknown"
					:class="{ 'is-disabled': !canSubmitFeedback }"
					@tap="handleSubmitRating('unknown')"
				>
					<text class="kyyy-practice-page__rate-text">不认识</text>
				</view>
			</view>
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import PracticeSessionNavbar from '@/components/kyyy/practice/PracticeSessionNavbar.vue'
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
}

export default defineComponent({
	name: 'KyyyPracticePage',
	components: {
		PageShell,
		PracticeSessionNavbar
	},
	data(): PracticePageState {
		return {
			entryMode: 'study',
			sessionState: createEmptyPracticeSession('study'),
			cardShownAt: 0,
			cardToken: ''
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
			const progress = this.sessionState.progressSummary
			if (!progress.totalCards) {
				return '0/20'
			}
			const current = Math.max(progress.currentIndex || progress.completedCards || 0, 0)
			return `${current}/${progress.totalCards}`
		},
		phoneticText(): string {
			const card = this.sessionState.currentCard
			if (!card) {
				return ''
			}
			return card.phoneticUs || card.phoneticUk || ''
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
		canSubmitFeedback(): boolean {
			return this.sessionState.status === 'active'
				&& !!this.sessionState.sessionId
				&& !!this.sessionState.currentCard?.wordId
				&& this.sessionState.revealed
				&& !this.sessionState.loading
				&& !this.sessionState.submitting
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
		handleReveal(): void {
			if (this.sessionState.status !== 'active' || !this.sessionState.currentCard) {
				return
			}
			this.sessionState = {
				...this.sessionState,
				revealed: true
			}
		},
		async handleSubmitRating(rating: KyyyPracticeRating): Promise<void> {
			if (!this.canSubmitFeedback || !this.sessionState.sessionId || !this.sessionState.currentCard?.wordId) {
				if (!this.sessionState.revealed) {
					uni.showToast({
						title: '先查看释义再评分',
						icon: 'none'
					})
				}
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
					revealed: this.sessionState.revealed,
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
.kyyy-practice-page__card {
	position: relative;
	margin-top: 20rpx;
	padding: 0;
	background: transparent;
	box-shadow: none;
	border-radius: 0;
	overflow: visible;
}

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

.kyyy-practice-page__card::before {
	display: none;
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

.kyyy-practice-page__card-top,
.kyyy-practice-page__card-head,
.kyyy-practice-page__memory-panel,
.kyyy-practice-page__detail-panel,
.kyyy-practice-page__result-title,
.kyyy-practice-page__result-desc,
.kyyy-practice-page__result-grid,
.kyyy-practice-page__state-title,
.kyyy-practice-page__state-desc,
.kyyy-practice-page__panel-actions {
	position: relative;
	z-index: 1;
}

.kyyy-practice-page__card-top {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 0 4rpx;
}

.kyyy-practice-page__card-tag {
	display: inline-flex;
	align-items: center;
	height: 46rpx;
	padding: 0 18rpx;
	border-radius: 999rpx;
	background: rgba(88, 112, 141, 0.12);
	font-size: 21rpx;
	font-weight: 700;
	color: #50627a;
}

.kyyy-practice-page__card-tag--muted {
	background: rgba(255, 255, 255, 0.7);
	color: #77859a;
}

.kyyy-practice-page__card-head {
	margin-top: 32rpx;
	padding: 0 4rpx;
}

.kyyy-practice-page__word {
	display: block;
	font-size: 88rpx;
	line-height: 1.04;
	font-weight: 820;
	letter-spacing: -0.05em;
	color: #151d28;
	word-break: break-word;
}

.kyyy-practice-page__phonetic-row {
	margin-top: 20rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.kyyy-practice-page__phonetic-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	height: 46rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.84);
	box-shadow: inset 0 0 0 1rpx rgba(220, 227, 236, 0.92);
	font-size: 20rpx;
	font-weight: 700;
	color: #6d7a8a;
}

.kyyy-practice-page__phonetic {
	font-size: 38rpx;
	line-height: 1.3;
	color: #60708a;
}

.kyyy-practice-page__memory-panel,
.kyyy-practice-page__detail-panel {
	margin-top: 44rpx;
	padding: 28rpx 26rpx;
	border-radius: 26rpx;
	background: rgba(255, 255, 255, 0.66);
	box-shadow: inset 0 0 0 1rpx rgba(228, 233, 241, 0.88);
}

.kyyy-practice-page__memory-title {
	display: block;
	font-size: 31rpx;
	line-height: 1.34;
	font-weight: 760;
	color: #243143;
}

.kyyy-practice-page__memory-desc {
	display: block;
	margin-top: 14rpx;
	font-size: 24rpx;
	line-height: 1.72;
	color: #69788f;
}

.kyyy-practice-page__reveal-button {
	margin-top: 26rpx;
	height: 84rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, rgba(92, 114, 144, 0.96), rgba(72, 89, 115, 0.98));
	box-shadow: 0 18rpx 36rpx rgba(76, 95, 118, 0.18);
	font-size: 28rpx;
	font-weight: 760;
	color: #ffffff;
}

.kyyy-practice-page__meaning-row {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
}

.kyyy-practice-page__part-of-speech {
	margin-top: 2rpx;
	font-size: 24rpx;
	line-height: 1.5;
	font-weight: 700;
	color: #627287;
	white-space: nowrap;
}

.kyyy-practice-page__meaning {
	flex: 1;
	font-size: 31rpx;
	line-height: 1.64;
	font-weight: 700;
	color: #1f2b39;
}

.kyyy-practice-page__detail-block {
	margin-top: 28rpx;
}

.kyyy-practice-page__detail-title {
	display: flex;
	align-items: center;
	gap: 10rpx;
	font-size: 24rpx;
	font-weight: 700;
	color: #4f6078;
}

.kyyy-practice-page__detail-mark {
	width: 10rpx;
	height: 10rpx;
	border-radius: 999rpx;
	background: #6e8099;
}

.kyyy-practice-page__detail-text {
	display: block;
	margin-top: 14rpx;
	font-size: 27rpx;
	line-height: 1.78;
	color: #2d3848;
}

.kyyy-practice-page__detail-subtext {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #73839a;
}

.kyyy-practice-page__related-list {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	margin-top: 16rpx;
}

.kyyy-practice-page__related-item {
	padding: 18rpx 20rpx;
	border-radius: 22rpx;
	background: rgba(255, 255, 255, 0.7);
	box-shadow: inset 0 0 0 1rpx rgba(228, 233, 241, 0.88);
}

.kyyy-practice-page__related-word {
	display: block;
	font-size: 26rpx;
	line-height: 1.4;
	font-weight: 700;
	color: #293444;
}

.kyyy-practice-page__related-meaning {
	display: block;
	margin-top: 6rpx;
	font-size: 23rpx;
	line-height: 1.56;
	color: #728197;
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
	color: #6f7d93;
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
	color: #728197;
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

.kyyy-practice-page__footer {
	position: fixed;
	left: 24rpx;
	right: 24rpx;
	bottom: calc(env(safe-area-inset-bottom) + 20rpx);
	padding: 0;
	background: transparent;
	box-shadow: none;
	backdrop-filter: none;
}

.kyyy-practice-page__footer-hint {
	display: block;
	font-size: 23rpx;
	line-height: 1.6;
	text-align: center;
	color: #6f7b8d;
}

.kyyy-practice-page__footer-actions {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 18rpx;
}

.kyyy-practice-page__rate-button {
	height: 94rpx;
	border-radius: 26rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.55);
}

.kyyy-practice-page__rate-button--know {
	background: linear-gradient(180deg, rgba(203, 234, 226, 0.95), rgba(179, 220, 209, 0.98));
	color: #264f48;
}

.kyyy-practice-page__rate-button--fuzzy {
	background: linear-gradient(180deg, rgba(248, 231, 191, 0.96), rgba(243, 214, 137, 0.98));
	color: #6a5312;
}

.kyyy-practice-page__rate-button--unknown {
	background: linear-gradient(180deg, rgba(246, 210, 205, 0.96), rgba(238, 184, 177, 0.98));
	color: #7a2f2b;
}

.kyyy-practice-page__rate-button.is-disabled {
	opacity: 0.48;
}

.kyyy-practice-page__rate-text {
	font-size: 30rpx;
	font-weight: 780;
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
