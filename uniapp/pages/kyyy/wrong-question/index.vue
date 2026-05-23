<!--
@file KyyyWrongQuestionPage
@project pipker-do
@module 考研英语 / 小程序阅读错题本
@description 展示阅读错题沉淀列表，支持状态筛选、关键词搜索和回到原阅读重练。
@logic 1. 加载阅读错题汇总与列表；2. 按待巩固/已掌握和关键词筛选；3. 从指定错题跳回阅读页继续练习。
@dependencies API: @/pages/kyyy/api/wrong-question, View: @/pages/kyyy/wrong-question/view, Shared: @/shared/session/session
@index_tags 考研英语, 错题本, 阅读错题, 重练入口
@author holic512
-->
<template>
	<view class="kyyy-wrong-question-page">
		<view class="kyyy-wrong-question-page__toolbar">
			<view class="kyyy-wrong-question-page__toolbar-head">
				<view class="kyyy-wrong-question-page__tabs">
					<view class="kyyy-wrong-question-page__tabs-track">
						<view
							v-for="tab in statusTabs"
							:key="tab.status"
							class="kyyy-wrong-question-page__tab"
							:class="{ 'is-active': currentStatus === tab.status }"
							@tap="handleStatusChange(tab.status)"
						>
							<text class="kyyy-wrong-question-page__tab-label">{{ tab.label }}</text>
							<text class="kyyy-wrong-question-page__tab-count">{{ tab.count }}</text>
						</view>
					</view>
				</view>
				<text class="kyyy-wrong-question-page__toolbar-meta">累计答错 {{ dashboard.summary.totalWrongTimes }} 次</text>
			</view>

			<view class="kyyy-wrong-question-page__search-shell">
				<view class="kyyy-wrong-question-page__search-box">
					<uni-icons type="search" size="18" color="#99a5b5" />
					<input
						v-model="keywordDraft"
						class="kyyy-wrong-question-page__search-input"
						placeholder="搜索题干或文章来源"
						placeholder-class="kyyy-wrong-question-page__search-placeholder"
						confirm-type="search"
						@confirm="handleSearchConfirm"
					/>
					<view v-if="keywordDraft" class="kyyy-wrong-question-page__search-clear" @tap="handleSearchClear">
						<text class="kyyy-wrong-question-page__search-clear-text">×</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="loading && !loadedOnce" class="kyyy-wrong-question-page__state-card">
			<text class="kyyy-wrong-question-page__state-title">正在整理你的阅读错题...</text>
			<text class="kyyy-wrong-question-page__state-desc">会优先把待巩固的题目排在前面。</text>
		</view>

		<view v-else-if="dashboard.records.length" class="kyyy-wrong-question-page__list">
			<view
				v-for="item in dashboard.records"
				:key="item.questionId"
				class="kyyy-wrong-question-page__card"
			>
				<view class="kyyy-wrong-question-page__card-head">
					<view class="kyyy-wrong-question-page__card-tags">
						<text class="kyyy-wrong-question-page__tag kyyy-wrong-question-page__tag--source">阅读错题</text>
						<text class="kyyy-wrong-question-page__tag">{{ buildWrongQuestionSourceText(item) }}</text>
					</view>
					<text
						class="kyyy-wrong-question-page__status"
						:class="item.isMastered ? 'is-mastered' : 'is-active'"
					>
						{{ item.isMastered ? '已掌握' : '待巩固' }}
					</text>
				</view>

				<view class="kyyy-wrong-question-page__stem-wrap">
					<view
						class="kyyy-wrong-question-page__stem"
						:class="{ 'is-collapsed': shouldShowStemToggle(item) && !isStemExpanded(item.questionId) }"
					>
						{{ item.stem }}
					</view>
					<view
						v-if="shouldShowStemToggle(item)"
						class="kyyy-wrong-question-page__stem-toggle"
						@tap="toggleStem(item.questionId)"
					>
						<uni-icons :type="isStemExpanded(item.questionId) ? 'top' : 'bottom'" size="14" color="#7a8799" />
					</view>
				</view>

				<view class="kyyy-wrong-question-page__meta-row">
					<view class="kyyy-wrong-question-page__meta-pills">
						<view class="kyyy-wrong-question-page__meta-pill">
							<uni-icons type="calendar" size="14" color="#7b8798" />
							<text class="kyyy-wrong-question-page__meta-pill-text">最近答错 {{ formatWrongQuestionTime(item.lastWrongAt) }}</text>
						</view>
						<view class="kyyy-wrong-question-page__meta-pill">
							<uni-icons type="info-filled" size="14" color="#7b8798" />
							<text class="kyyy-wrong-question-page__meta-pill-text">累计错 {{ item.wrongCount }} 次</text>
						</view>
						<view v-if="item.isMastered && item.masteredAt" class="kyyy-wrong-question-page__meta-pill kyyy-wrong-question-page__meta-pill--mastered">
							<uni-icons type="checkbox-filled" size="14" color="#5b7a65" />
							<text class="kyyy-wrong-question-page__meta-pill-text kyyy-wrong-question-page__meta-pill-text--mastered">掌握于 {{ formatWrongQuestionTime(item.masteredAt) }}</text>
						</view>
					</view>
					<button class="kyyy-wrong-question-page__retry-button" @tap="handleRetry(item)">从这题开始练</button>
				</view>
			</view>
		</view>

		<view v-else class="kyyy-wrong-question-page__empty">
			<view class="kyyy-wrong-question-page__empty-icon-shell">
				<view class="kyyy-wrong-question-page__empty-icon-core">
					<uni-icons :type="emptyStateIcon" size="32" color="#617089" />
				</view>
			</view>
			<text class="kyyy-wrong-question-page__empty-title">{{ emptyStateTitle }}</text>
			<text class="kyyy-wrong-question-page__empty-desc">{{ emptyStateDescription }}</text>
			<button class="kyyy-wrong-question-page__empty-button" @tap="handleEmptyAction">{{ emptyStateActionText }}</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWrongQuestions } from '@/pages/kyyy/api/wrong-question'
import type {
	KyyyWrongQuestionDashboardState,
	KyyyWrongQuestionStatus,
	KyyyWrongQuestionViewRecord,
	SearchConfirmEvent
} from '@/pages/kyyy/wrong-question/types'
import {
	buildWrongQuestionSourceText,
	createEmptyWrongQuestionDashboard,
	formatWrongQuestionTime,
	normalizeWrongQuestionDashboard
} from '@/pages/kyyy/wrong-question/view'

interface WrongQuestionPageState {
	loading: boolean
	loadedOnce: boolean
	currentStatus: KyyyWrongQuestionStatus
	keyword: string
	keywordDraft: string
	dashboard: KyyyWrongQuestionDashboardState
	expandedQuestionIds: number[]
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'KyyyWrongQuestionPage',
	data(): WrongQuestionPageState {
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
		statusTabs(): Array<{ status: KyyyWrongQuestionStatus; label: string; count: number }> {
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
				return '还没有产生阅读错题'
			}
			if (this.emptyStateKind === 'mastered') {
				return '当前没有待巩固的阅读错题'
			}
			return '当前筛选下没有结果'
		},
		emptyStateDescription(): string {
			if (this.emptyStateKind === 'never') {
				return '先去做阅读，答错的题会自动收进这里，后面就能集中回顾。'
			}
			if (this.emptyStateKind === 'mastered') {
				return '这一批阅读错题已经练回来了，可以去做新阅读，或者切到“已掌握”再过一遍。'
			}
			return '试试切换筛选，或者换个关键词重新搜一下。'
		},
		emptyStateActionText(): string {
			if (this.emptyStateKind === 'filtered') {
				return '清空筛选'
			}
			return '去做阅读'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		buildWrongQuestionSourceText,
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
			} finally {
				this.loading = false
			}
		},
		handleStatusChange(status: KyyyWrongQuestionStatus): void {
			if (this.currentStatus === status || this.loading) {
				return
			}
			this.currentStatus = status
			this.loadWrongQuestions().catch((error) => {
				uni.showToast({
					title: resolveErrorMessage(error, '错题本加载失败'),
					icon: 'none'
				})
			})
		},
		handleSearchConfirm(event?: SearchConfirmEvent): void {
			const nextKeyword = event?.detail?.value ?? event?.value ?? this.keywordDraft
			this.keywordDraft = typeof nextKeyword === 'string' ? nextKeyword.trim() : ''
			if (this.keyword === this.keywordDraft) {
				return
			}
			this.keyword = this.keywordDraft
			this.loadWrongQuestions().catch((error) => {
				uni.showToast({
					title: resolveErrorMessage(error, '错题本加载失败'),
					icon: 'none'
				})
			})
		},
		handleSearchClear(): void {
			this.keywordDraft = ''
			if (!this.keyword) {
				return
			}
			this.keyword = ''
			this.loadWrongQuestions().catch((error) => {
				uni.showToast({
					title: resolveErrorMessage(error, '错题本加载失败'),
					icon: 'none'
				})
			})
		},
		shouldShowStemToggle(item: KyyyWrongQuestionViewRecord): boolean {
			return item.stem.length > 70
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
		handleRetry(item: KyyyWrongQuestionViewRecord): void {
			if (!item.passageId || !item.questionId) {
				uni.showToast({
					title: '当前错题缺少重练信息',
					icon: 'none'
				})
				return
			}
			uni.navigateTo({
				url: `/pages/kyyy/reading/index?passageId=${item.passageId}&questionId=${item.questionId}`
			})
		},
		handleEmptyAction(): void {
			if (this.emptyStateKind === 'filtered') {
				this.keyword = ''
				this.keywordDraft = ''
				this.currentStatus = 'all'
				this.loadWrongQuestions().catch((error) => {
					uni.showToast({
						title: resolveErrorMessage(error, '错题本加载失败'),
						icon: 'none'
					})
				})
				return
			}
			uni.switchTab({
				url: '/pages/kyyy/index'
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-wrong-question-page {
	min-height: 100vh;
	padding: 32rpx 24rpx 48rpx;
	background:
		radial-gradient(circle at top left, rgba(245, 234, 220, 0.78), transparent 32%),
		linear-gradient(180deg, #f7f4ef 0%, #f5f7fa 22%, #f8fafc 100%);
}

.kyyy-wrong-question-page__toolbar,
.kyyy-wrong-question-page__state-card,
.kyyy-wrong-question-page__card,
.kyyy-wrong-question-page__empty {
	background: rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(220, 227, 234, 0.92);
	border-radius: 28rpx;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.08);
}

.kyyy-wrong-question-page__toolbar {
	display: flex;
	flex-direction: column;
	gap: 28rpx;
	padding: 28rpx;
}

.kyyy-wrong-question-page__toolbar-head,
.kyyy-wrong-question-page__card-head,
.kyyy-wrong-question-page__meta-row {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18rpx;
}

.kyyy-wrong-question-page__tabs {
	flex: 1;
	min-width: 0;
}

.kyyy-wrong-question-page__tabs-track {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 8rpx;
	border-radius: 24rpx;
	background: #f3f6f8;
}

.kyyy-wrong-question-page__tab {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	padding: 18rpx 10rpx;
	border-radius: 18rpx;
	color: #708092;
}

.kyyy-wrong-question-page__tab.is-active {
	background: linear-gradient(135deg, #f4e6d6, #ecd6b8);
	color: #6f5330;
	box-shadow: 0 10rpx 18rpx rgba(141, 106, 58, 0.14);
}

.kyyy-wrong-question-page__tab-label,
.kyyy-wrong-question-page__tab-count,
.kyyy-wrong-question-page__toolbar-meta,
.kyyy-wrong-question-page__state-title,
.kyyy-wrong-question-page__state-desc,
.kyyy-wrong-question-page__tag,
.kyyy-wrong-question-page__status,
.kyyy-wrong-question-page__stem,
.kyyy-wrong-question-page__meta-pill-text,
.kyyy-wrong-question-page__retry-button,
.kyyy-wrong-question-page__empty-title,
.kyyy-wrong-question-page__empty-desc,
.kyyy-wrong-question-page__empty-button {
	line-height: 1.6;
}

.kyyy-wrong-question-page__tab-label,
.kyyy-wrong-question-page__tag,
.kyyy-wrong-question-page__status,
.kyyy-wrong-question-page__meta-pill-text,
.kyyy-wrong-question-page__retry-button,
.kyyy-wrong-question-page__empty-button {
	font-size: 24rpx;
}

.kyyy-wrong-question-page__tab-count,
.kyyy-wrong-question-page__stem {
	font-size: 26rpx;
}

.kyyy-wrong-question-page__toolbar-meta,
.kyyy-wrong-question-page__state-desc,
.kyyy-wrong-question-page__empty-desc {
	font-size: 24rpx;
	color: #788697;
}

.kyyy-wrong-question-page__search-box {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 0 22rpx;
	height: 88rpx;
	border-radius: 22rpx;
	background: #f6f8fb;
	border: 1rpx solid rgba(222, 228, 236, 0.92);
}

.kyyy-wrong-question-page__search-input {
	flex: 1;
	font-size: 28rpx;
	color: #314255;
}

.kyyy-wrong-question-page__search-placeholder {
	color: #99a5b5;
}

.kyyy-wrong-question-page__search-clear {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40rpx;
	height: 40rpx;
	border-radius: 50%;
	background: rgba(161, 173, 186, 0.18);
}

.kyyy-wrong-question-page__search-clear-text {
	font-size: 26rpx;
	color: #748294;
	line-height: 1;
}

.kyyy-wrong-question-page__state-card,
.kyyy-wrong-question-page__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	gap: 14rpx;
	padding: 56rpx 36rpx;
	margin-top: 24rpx;
}

.kyyy-wrong-question-page__state-title,
.kyyy-wrong-question-page__empty-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #364557;
}

.kyyy-wrong-question-page__list {
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	margin-top: 24rpx;
}

.kyyy-wrong-question-page__card {
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	padding: 28rpx;
}

.kyyy-wrong-question-page__card-tags,
.kyyy-wrong-question-page__meta-pills {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.kyyy-wrong-question-page__tag {
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
	background: #f4f7fa;
	color: #68788a;
}

.kyyy-wrong-question-page__tag--source {
	background: rgba(244, 230, 214, 0.8);
	color: #7b5c35;
}

.kyyy-wrong-question-page__status {
	flex-shrink: 0;
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
}

.kyyy-wrong-question-page__status.is-active {
	background: rgba(247, 236, 220, 0.96);
	color: #8a6235;
}

.kyyy-wrong-question-page__status.is-mastered {
	background: rgba(231, 242, 235, 0.96);
	color: #55715e;
}

.kyyy-wrong-question-page__stem-wrap {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.kyyy-wrong-question-page__stem {
	color: #314255;
	white-space: pre-wrap;
	word-break: break-word;
}

.kyyy-wrong-question-page__stem.is-collapsed {
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.kyyy-wrong-question-page__stem-toggle {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	background: #f3f6f8;
}

.kyyy-wrong-question-page__meta-row {
	align-items: center;
}

.kyyy-wrong-question-page__meta-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	background: #f6f8fb;
}

.kyyy-wrong-question-page__meta-pill--mastered {
	background: rgba(231, 242, 235, 0.96);
}

.kyyy-wrong-question-page__meta-pill-text {
	color: #6f7f90;
}

.kyyy-wrong-question-page__meta-pill-text--mastered {
	color: #587162;
}

.kyyy-wrong-question-page__retry-button,
.kyyy-wrong-question-page__empty-button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 0 28rpx;
	height: 76rpx;
	border-radius: 999rpx;
	border: none;
	background: linear-gradient(135deg, #caa778, #b98f58);
	color: #ffffff;
}

.kyyy-wrong-question-page__empty-icon-shell {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background: rgba(243, 246, 249, 0.92);
}

.kyyy-wrong-question-page__empty-icon-core {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 84rpx;
	height: 84rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.96);
}
</style>
