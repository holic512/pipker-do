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
						<text class="wrong-book-page__tag wrong-book-page__tag--type" :class="questionTypeTagClass(item.questionType)">{{ questionTypeLabel(item.questionType) }}</text>
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

				<view class="wrong-book-page__stem-wrap">
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
						<uni-icons :type="isStemExpanded(item.questionId) ? 'top' : 'bottom'" size="14" color="#7a8799" />
					</view>
				</view>

				<view class="wrong-book-page__meta-row">
					<view class="wrong-book-page__meta-pills">
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
					<button class="wrong-book-page__retry-button" @tap="handleRetry(item)">从这题开始练</button>
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
import { openBankPracticeTab, openPracticeTab } from '@/pages/kyzz/practice/navigation'
import { difficultyLabel, difficultyTagClass, questionTypeLabel } from '@/pages/kyzz/practice/view'
import type { KyzzPracticeQuestionType } from '@/pages/kyzz/practice/types'
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
		questionTypeTagClass(questionType: KyzzPracticeQuestionType): string {
			if (questionType === 'single') {
				return 'is-single'
			}
			if (questionType === 'multiple') {
				return 'is-multiple'
			}
			if (questionType === 'short') {
				return 'is-short'
			}
			return ''
		},
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
			return (record.stem || '').trim().length > 42
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
				sourceType: 'wrong_book',
				sourceStatus: this.currentStatus,
				keyword: this.keyword.trim() || null,
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
			openBankPracticeTab().catch(() => {
				uni.showToast({
					title: '跳转刷题失败',
					icon: 'none'
				})
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
	background: linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
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
	background: #465a7e;
}

.wrong-book-page__tab-label,
.wrong-book-page__tab-count {
	font-size: 28rpx;
	line-height: 1;
	font-weight: 700;
	color: #667386;
}

.wrong-book-page__tab-count {
	font-size: 24rpx;
	color: #9ba5b3;
}

.wrong-book-page__tab.is-active .wrong-book-page__tab-label,
.wrong-book-page__tab.is-active .wrong-book-page__tab-count {
	color: #1f2937;
	font-weight: 800;
}

.wrong-book-page__toolbar-meta {
	padding-bottom: 14rpx;
	font-size: 22rpx;
	line-height: 1.4;
	color: #5f6d7f;
	white-space: nowrap;
}

.wrong-book-page__search-shell {
	margin-top: 18rpx;
	padding-top: 18rpx;
	border-top: 1rpx solid #d6deea;
}

.wrong-book-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	height: 82rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: #ffffff;
	box-shadow:
		0 10rpx 24rpx rgba(45, 58, 77, 0.05),
		inset 0 0 0 1rpx #cfd8e6;
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
	background: #9aa7ba;
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
	background: #ffffff;
	box-shadow: 0 16rpx 34rpx rgba(45, 58, 77, 0.06);
	border: 1rpx solid #d6deea;
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
	background: #ffffff;
	box-shadow: 0 14rpx 30rpx rgba(45, 58, 77, 0.055);
	border: 1rpx solid #d6deea;
}

.wrong-book-page__card-head {
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
	font-weight: 700;
}

.wrong-book-page__tag {
	background: #ffffff;
	box-shadow: inset 0 0 0 1rpx #cfd8e6;
	color: #39465a;
}

.wrong-book-page__tag--type.is-single {
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
	color: #334f7c;
}

.wrong-book-page__tag--type.is-multiple {
	background: #edf5ea;
	box-shadow: inset 0 0 0 1rpx #c2d9bc;
	color: #3d6237;
}

.wrong-book-page__tag--type.is-short {
	background: #f4ecff;
	box-shadow: inset 0 0 0 1rpx #d5c2ec;
	color: #5b4678;
}

.wrong-book-page__tag.is-simple {
	background: #e5f4e8;
	box-shadow: inset 0 0 0 1rpx #bcdabe;
	color: #315f42;
}

.wrong-book-page__tag.is-medium {
	background: #e7efff;
	box-shadow: inset 0 0 0 1rpx #b9cae8;
	color: #314f79;
}

.wrong-book-page__tag.is-hard {
	background: #fff0dc;
	box-shadow: inset 0 0 0 1rpx #e6c58e;
	color: #78511f;
}

.wrong-book-page__tag.is-sprint {
	background: #fde8e5;
	box-shadow: inset 0 0 0 1rpx #e8b8b3;
	color: #87413e;
}

.wrong-book-page__tag--bank {
	max-width: 100%;
	background: #eef2f6;
	box-shadow: inset 0 0 0 1rpx #c9d2dc;
	color: #3c4a58;
}

.wrong-book-page__status.is-active {
	background: #fde8e5;
	box-shadow: inset 0 0 0 1rpx #e8b8b3;
	color: #87413e;
}

.wrong-book-page__status.is-mastered {
	background: #e5f4e8;
	box-shadow: inset 0 0 0 1rpx #bcdabe;
	color: #315f42;
}

.wrong-book-page__stem-wrap {
	position: relative;
	margin-top: 18rpx;
}

.wrong-book-page__stem {
	display: block;
	font-size: 28rpx;
	line-height: 1.7;
	font-weight: 700;
	color: #1f2937;
}

.wrong-book-page__stem.is-collapsed {
	display: -webkit-box;
	overflow: hidden;
	text-overflow: ellipsis;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	padding-right: 50rpx;
}

.wrong-book-page__stem-toggle {
	position: absolute;
	right: 0;
	bottom: 3rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 42rpx;
	height: 36rpx;
	border-radius: 999rpx;
	background: #f7f9fc;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.wrong-book-page__meta-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
	margin-top: 16rpx;
}

.wrong-book-page__meta-pills {
	display: flex;
	flex: 1;
	min-width: 0;
	flex-wrap: wrap;
	gap: 10rpx;
}

.wrong-book-page__meta-pill {
	display: inline-flex;
	min-width: 0;
	align-items: center;
	gap: 8rpx;
	min-height: 44rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: #f7f9fc;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.wrong-book-page__meta-pill--mastered {
	background: #e5f4e8;
	box-shadow: inset 0 0 0 1rpx #bcdabe;
}

.wrong-book-page__meta-pill-text,
.wrong-book-page__meta-pill-text--mastered {
	font-size: 22rpx;
	line-height: 1.6;
	color: #5f6d7f;
}

.wrong-book-page__meta-pill-text--mastered {
	color: #5b7a65;
}

.wrong-book-page__retry-button,
.wrong-book-page__empty-button {
	margin: 0;
	padding: 0 20rpx;
	height: 54rpx;
	line-height: 54rpx;
	border-radius: 18rpx;
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 700;
	box-shadow: 0 12rpx 22rpx rgba(54, 75, 111, 0.18);
}

.wrong-book-page__retry-button {
	flex-shrink: 0;
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
	background: #eef2f6;
	box-shadow:
		0 18rpx 34rpx rgba(45, 58, 77, 0.06),
		inset 0 0 0 1rpx #c9d2dc;
}

.wrong-book-page__empty-icon-core {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 86rpx;
	height: 86rpx;
	border-radius: 28rpx;
	background: #ffffff;
}

.wrong-book-page__empty-button {
	margin-top: 24rpx;
}
</style>
