<template>
	<view class="wrong-book-page">
		<view class="wrong-book-page__toolbar">
			<view class="wrong-book-page__toolbar-head">
				<view class="wrong-book-page__tabs">
					<view class="wrong-book-page__tabs-track">
						<view
							v-for="tab in statusTabs"
							:key="tab.status"
							class="wrong-book-page__tab"
							:class="{ 'is-active': currentStatus === tab.status }"
							@tap="handleStatusChange(tab.status)"
						>
							<text class="wrong-book-page__tab-label">{{ tab.label }}</text>
							<text class="wrong-book-page__tab-count">{{ tab.count }}</text>
						</view>
					</view>
				</view>
				<text class="wrong-book-page__toolbar-meta">累计错题 {{ dashboard.summary.totalWrongTimes }} 次</text>
			</view>

			<view class="wrong-book-page__search-shell">
				<view class="wrong-book-page__search-box">
					<uni-icons type="search" size="18" color="#99a5b5" />
					<input
						v-model="keywordDraft"
						class="wrong-book-page__search-input"
						placeholder="搜索题干或题库名"
						placeholder-class="wrong-book-page__search-placeholder"
						confirm-type="search"
						@confirm="handleSearchConfirm"
					/>
					<view v-if="keywordDraft" class="wrong-book-page__search-clear" @tap="handleSearchClear">
						<text class="wrong-book-page__search-clear-text">×</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="loading && !loadedOnce" class="wrong-book-page__state-card">
			<text class="wrong-book-page__state-title">正在整理你的错题本...</text>
			<text class="wrong-book-page__state-desc">会优先把待巩固的题目排在前面。</text>
		</view>

		<view v-else-if="dashboard.records.length" class="wrong-book-page__list">
			<view
				v-for="item in dashboard.records"
				:key="item.questionId"
				class="wrong-book-page__card"
			>
				<view class="wrong-book-page__card-head">
					<view class="wrong-book-page__card-tags">
						<text class="wrong-book-page__tag">{{ questionTypeLabel(item.questionType) }}</text>
						<text class="wrong-book-page__tag" :class="difficultyTagClass(item.difficultyLevel)">{{ difficultyLabel(item.difficultyLevel) }}</text>
						<text class="wrong-book-page__tag wrong-book-page__tag--bank">题库 · {{ item.bankName }}</text>
					</view>
					<text
						class="wrong-book-page__status"
						:class="item.isMastered ? 'is-mastered' : 'is-active'"
					>
						{{ item.isMastered ? '已掌握' : '待巩固' }}
					</text>
				</view>

				<view
					class="wrong-book-page__stem"
					:class="{ 'is-collapsed': shouldShowStemToggle(item) && !isStemExpanded(item.questionId) }"
				>
					{{ item.stem }}
				</view>
				<view
					v-if="shouldShowStemToggle(item)"
					class="wrong-book-page__stem-toggle"
					@tap="toggleStem(item.questionId)"
				>
					<text class="wrong-book-page__stem-toggle-text">{{ isStemExpanded(item.questionId) ? '收起题干' : '展开题干' }}</text>
					<uni-icons :type="isStemExpanded(item.questionId) ? 'top' : 'bottom'" size="14" color="#7a8799" />
				</view>

				<view class="wrong-book-page__meta-row">
					<view class="wrong-book-page__meta-pill">
						<uni-icons type="calendar" size="14" color="#7b8798" />
						<text class="wrong-book-page__meta-pill-text">最近答错 {{ formatWrongQuestionTime(item.lastWrongAt) }}</text>
					</view>
					<view class="wrong-book-page__meta-pill">
						<uni-icons type="info-filled" size="14" color="#7b8798" />
						<text class="wrong-book-page__meta-pill-text">累计错 {{ item.wrongCount }} 次</text>
					</view>
					<view v-if="item.isMastered && item.masteredAt" class="wrong-book-page__meta-pill wrong-book-page__meta-pill--mastered">
						<uni-icons type="checkbox-filled" size="14" color="#5b7a65" />
						<text class="wrong-book-page__meta-pill-text wrong-book-page__meta-pill-text--mastered">掌握于 {{ formatWrongQuestionTime(item.masteredAt) }}</text>
					</view>
				</view>

				<view class="wrong-book-page__card-foot">
					<text class="wrong-book-page__foot-tip">
						{{ item.isMastered ? '可以再刷一遍，确认这题已经真正稳住。' : '建议趁记忆还在，重新做一遍这题。' }}
					</text>
					<button class="wrong-book-page__retry-button" @tap="handleRetry(item)">再练这题</button>
				</view>
			</view>
		</view>

		<view v-else class="wrong-book-page__empty">
			<view class="wrong-book-page__empty-icon-shell">
				<view class="wrong-book-page__empty-icon-core">
					<uni-icons :type="emptyStateIcon" size="32" color="#617089" />
				</view>
			</view>
			<text class="wrong-book-page__empty-title">{{ emptyStateTitle }}</text>
			<text class="wrong-book-page__empty-desc">{{ emptyStateDescription }}</text>
			<button class="wrong-book-page__empty-button" @tap="handleEmptyAction">{{ emptyStateActionText }}</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWrongQuestions } from '@/pages/kyzz/api/wrong-book'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'
import { difficultyLabel, difficultyTagClass, questionTypeLabel } from '@/pages/kyzz/practice/view'
import type {
	KyzzWrongQuestionDashboardState,
	KyzzWrongQuestionStatus,
	KyzzWrongQuestionViewRecord,
	SearchConfirmEvent
} from '@/pages/kyzz/wrong-book/types'
import {
	createEmptyWrongQuestionDashboard,
	formatWrongQuestionTime,
	normalizeWrongQuestionDashboard
} from '@/pages/kyzz/wrong-book/view'

// AI 索引: KYZZ 小程序错题本页面。

interface WrongBookPageState {
	loading: boolean
	loadedOnce: boolean
	currentStatus: KyzzWrongQuestionStatus
	keyword: string
	keywordDraft: string
	dashboard: KyzzWrongQuestionDashboardState
	expandedQuestionIds: number[]
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'KyzzWrongBookPage',
	data(): WrongBookPageState {
		return {
			loading: false,
			loadedOnce: false,
			currentStatus: 'all',
			keyword: '',
			keywordDraft: '',
			dashboard: createEmptyWrongQuestionDashboard(),
			expandedQuestionIds: []
		}
	},
	computed: {
		statusTabs(): Array<{ status: KyzzWrongQuestionStatus; label: string; count: number }> {
			return [
				{ status: 'all', label: '全部', count: this.dashboard.summary.totalCount },
				{ status: 'active', label: '待巩固', count: this.dashboard.summary.activeCount },
				{ status: 'mastered', label: '已掌握', count: this.dashboard.summary.masteredCount }
			]
		},
		emptyStateKind(): 'never' | 'mastered' | 'filtered' {
			if (this.dashboard.summary.totalCount <= 0) {
				return 'never'
			}
			if (this.currentStatus === 'active' && !this.keyword.trim() && this.dashboard.summary.activeCount <= 0) {
				return 'mastered'
			}
			return 'filtered'
		},
		emptyStateIcon(): string {
			if (this.emptyStateKind === 'never') {
				return 'checkbox-filled'
			}
			if (this.emptyStateKind === 'mastered') {
				return 'medal'
			}
			return 'help'
		},
		emptyStateTitle(): string {
			if (this.emptyStateKind === 'never') {
				return '还没有产生错题'
			}
			if (this.emptyStateKind === 'mastered') {
				return '当前没有待巩固的错题'
			}
			return '当前筛选下没有结果'
		},
		emptyStateDescription(): string {
			if (this.emptyStateKind === 'never') {
				return '先去刷题，答错的题会自动收进这里，后面就能集中回顾。'
			}
			if (this.emptyStateKind === 'mastered') {
				return '这一批错题已经都练回来了，可以去做新题，或者切换到“已掌握”再过一遍。'
			}
			return '试试切换筛选，或者换个关键词重新搜一下。'
		},
		emptyStateActionText(): string {
			if (this.emptyStateKind === 'filtered') {
				return '清空筛选'
			}
			return '去刷题'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		difficultyLabel,
		difficultyTagClass,
		questionTypeLabel,
		formatWrongQuestionTime,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadWrongQuestions()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '错题本加载失败'),
					icon: 'none'
				})
			}
		},
		async loadWrongQuestions(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getWrongQuestions({
					status: this.currentStatus,
					keyword: this.keyword.trim() || undefined
				})
				this.dashboard = normalizeWrongQuestionDashboard(result)
				const activeQuestionIds = new Set(this.dashboard.records.map((item) => item.questionId))
				this.expandedQuestionIds = this.expandedQuestionIds.filter((questionId) => activeQuestionIds.has(questionId))
				this.loadedOnce = true
			} catch (error) {
				this.loadedOnce = true
				uni.showToast({
					title: resolveErrorMessage(error, '错题本加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		handleStatusChange(status: KyzzWrongQuestionStatus): void {
			if (this.currentStatus === status) {
				return
			}
			this.currentStatus = status
			this.loadWrongQuestions().catch(() => {})
		},
		handleSearchConfirm(event: SearchConfirmEvent): void {
			const value = (event?.detail?.value || event?.value || this.keywordDraft || '').trim()
			this.keyword = value
			this.keywordDraft = value
			this.loadWrongQuestions().catch(() => {})
		},
		handleSearchClear(): void {
			if (!this.keyword && !this.keywordDraft) {
				return
			}
			this.keyword = ''
			this.keywordDraft = ''
			this.loadWrongQuestions().catch(() => {})
		},
		shouldShowStemToggle(record: KyzzWrongQuestionViewRecord): boolean {
			return (record.stem || '').trim().length > 68
		},
		isStemExpanded(questionId: number): boolean {
			return this.expandedQuestionIds.includes(questionId)
		},
		toggleStem(questionId: number): void {
			if (this.isStemExpanded(questionId)) {
				this.expandedQuestionIds = this.expandedQuestionIds.filter((item) => item !== questionId)
				return
			}
			this.expandedQuestionIds = [...this.expandedQuestionIds, questionId]
		},
		handleRetry(record: KyzzWrongQuestionViewRecord): void {
			openPracticeTab({
				bankId: record.bankId,
				questionId: record.questionId,
				freshAttempt: true
			}).catch(() => {
				uni.showToast({
					title: '跳转刷题失败',
					icon: 'none'
				})
			})
		},
		handleEmptyAction(): void {
			if (this.emptyStateKind === 'filtered') {
				this.currentStatus = 'all'
				this.keyword = ''
				this.keywordDraft = ''
				this.loadWrongQuestions().catch(() => {})
				return
			}
			uni.switchTab({
				url: '/pages/kyzz/practice/index'
			})
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.wrong-book-page {
	min-height: 100vh;
	padding: 28rpx 24rpx calc(env(safe-area-inset-bottom) + 48rpx);
	box-sizing: border-box;
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.99) 0%, rgba(243, 247, 252, 0.97) 46%, rgba(233, 239, 247, 0.95) 100%);
}

.wrong-book-page__toolbar {
	padding: 2rpx 8rpx 0;
}

.wrong-book-page__toolbar-head {
	display: flex;
	flex-wrap: wrap;
	align-items: flex-end;
	justify-content: space-between;
	gap: 12rpx 20rpx;
}

.wrong-book-page__tabs {
	flex: 1;
	min-width: 0;
}

.wrong-book-page__tabs-track {
	display: flex;
	flex-wrap: wrap;
	align-items: flex-end;
	gap: 18rpx 30rpx;
}

.wrong-book-page__tab {
	position: relative;
	display: inline-flex;
	align-items: baseline;
	gap: 8rpx;
	padding-bottom: 14rpx;
}

.wrong-book-page__tab::after {
	content: '';
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	height: 4rpx;
	border-radius: 999rpx;
	background: transparent;
	transition: background-color 0.2s ease;
}

.wrong-book-page__tab.is-active::after {
	background: #59657d;
}

.wrong-book-page__tab-label,
.wrong-book-page__tab-count {
	font-size: 28rpx;
	line-height: 1;
	font-weight: 600;
	color: #7d8795;
}

.wrong-book-page__tab-count {
	font-size: 24rpx;
	color: #9ba5b3;
}

.wrong-book-page__tab.is-active .wrong-book-page__tab-label,
.wrong-book-page__tab.is-active .wrong-book-page__tab-count {
	color: #283241;
	font-weight: 700;
}

.wrong-book-page__toolbar-meta {
	padding-bottom: 14rpx;
	font-size: 22rpx;
	line-height: 1.4;
	color: #adb5c1;
	white-space: nowrap;
}

.wrong-book-page__search-shell {
	margin-top: 18rpx;
	padding-top: 18rpx;
	border-top: 1rpx solid rgba(220, 227, 236, 0.78);
}

.wrong-book-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	height: 82rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: rgba(244, 247, 251, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(225, 232, 241, 0.98);
}

.wrong-book-page__search-input {
	flex: 1;
	min-width: 0;
	height: 100%;
	font-size: 26rpx;
	color: #2d3645;
}

.wrong-book-page__search-placeholder {
	font-size: 24rpx;
	color: #a9b2bf;
}

.wrong-book-page__search-clear {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: rgba(210, 218, 229, 0.92);
}

.wrong-book-page__search-clear-text {
	font-size: 28rpx;
	line-height: 1;
	color: #ffffff;
}

.wrong-book-page__state-card,
.wrong-book-page__empty {
	margin-top: 22rpx;
	padding: 34rpx 28rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 20rpx 38rpx rgba(43, 52, 55, 0.06);
}

.wrong-book-page__state-title,
.wrong-book-page__empty-title {
	display: block;
	font-size: 32rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2c3443;
}

.wrong-book-page__state-desc,
.wrong-book-page__empty-desc {
	display: block;
	margin-top: 14rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #778395;
}

.wrong-book-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 22rpx;
}

.wrong-book-page__card {
	padding: 28rpx 24rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.92);
	box-shadow: 0 20rpx 38rpx rgba(43, 52, 55, 0.05);
}

.wrong-book-page__card-head,
.wrong-book-page__card-foot {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18rpx;
}

.wrong-book-page__card-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.wrong-book-page__tag,
.wrong-book-page__status {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 42rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 600;
}

.wrong-book-page__tag {
	background: rgba(242, 246, 251, 0.98);
	color: #5d6779;
}

.wrong-book-page__tag.is-simple {
	background: rgba(225, 236, 226, 0.98);
	color: #466354;
}

.wrong-book-page__tag.is-medium {
	background: rgba(235, 240, 247, 0.98);
	color: #4f6078;
}

.wrong-book-page__tag.is-hard {
	background: rgba(245, 235, 226, 0.98);
	color: #8d5d47;
}

.wrong-book-page__tag.is-sprint {
	background: rgba(247, 226, 224, 0.98);
	color: #965251;
}

.wrong-book-page__tag--bank {
	max-width: 100%;
	background: linear-gradient(135deg, rgba(232, 239, 251, 0.98) 0%, rgba(242, 247, 255, 0.98) 100%);
	color: #4a6287;
}

.wrong-book-page__status.is-active {
	background: rgba(247, 226, 224, 0.98);
	color: #964e4b;
}

.wrong-book-page__status.is-mastered {
	background: rgba(225, 236, 226, 0.98);
	color: #476453;
}

.wrong-book-page__stem {
	display: block;
	margin-top: 18rpx;
	font-size: 28rpx;
	line-height: 1.7;
	font-weight: 600;
	color: #27313f;
}

.wrong-book-page__stem.is-collapsed {
	display: -webkit-box;
	overflow: hidden;
	text-overflow: ellipsis;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 3;
}

.wrong-book-page__stem-toggle {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	margin-top: 12rpx;
	padding: 10rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(243, 247, 252, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(227, 233, 241, 0.96);
}

.wrong-book-page__stem-toggle-text {
	font-size: 21rpx;
	line-height: 1;
	font-weight: 600;
	color: #6e7b8f;
}

.wrong-book-page__meta-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-top: 16rpx;
}

.wrong-book-page__meta-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	min-height: 44rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(243, 247, 252, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(227, 233, 241, 0.96);
}

.wrong-book-page__meta-pill--mastered {
	background: rgba(237, 247, 239, 0.98);
	box-shadow: inset 0 0 0 1rpx rgba(211, 231, 216, 0.96);
}

.wrong-book-page__meta-pill-text,
.wrong-book-page__meta-pill-text--mastered,
.wrong-book-page__foot-tip {
	font-size: 22rpx;
	line-height: 1.6;
	color: #7c8796;
}

.wrong-book-page__meta-pill-text--mastered {
	color: #5b7a65;
}

.wrong-book-page__card-foot {
	margin-top: 18rpx;
	padding-top: 18rpx;
	border-top: 1rpx solid rgba(224, 231, 239, 0.84);
}

.wrong-book-page__foot-tip {
	flex: 1;
	min-width: 0;
}

.wrong-book-page__retry-button,
.wrong-book-page__empty-button {
	margin: 0;
	padding: 0 28rpx;
	height: 74rpx;
	line-height: 74rpx;
	border-radius: 24rpx;
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
	box-shadow: 0 16rpx 30rpx rgba(84, 94, 118, 0.18);
}

.wrong-book-page__retry-button::after,
.wrong-book-page__empty-button::after {
	border: 0;
}

.wrong-book-page__empty {
	text-align: center;
}

.wrong-book-page__empty-icon-shell {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 132rpx;
	height: 132rpx;
	border-radius: 36rpx;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(237, 242, 249, 0.96) 100%);
	box-shadow:
		0 24rpx 44rpx rgba(84, 94, 118, 0.08),
		inset 0 0 0 1rpx rgba(222, 229, 239, 0.88);
}

.wrong-book-page__empty-icon-core {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 86rpx;
	height: 86rpx;
	border-radius: 28rpx;
	background: linear-gradient(135deg, rgba(222, 231, 245, 0.98) 0%, rgba(246, 249, 253, 0.98) 100%);
}

.wrong-book-page__empty-button {
	margin-top: 24rpx;
}
</style>
