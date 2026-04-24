<template>
	<view class="favorite-page">
		<view class="favorite-page__toolbar">
			<view class="favorite-page__search-shell">
				<view class="favorite-page__search-box">
					<uni-icons type="search" size="18" color="#99a5b5" />
					<input
						v-model="keywordDraft"
						class="favorite-page__search-input"
						placeholder="搜索题干或题库名"
						placeholder-class="favorite-page__search-placeholder"
						confirm-type="search"
						@confirm="handleSearchConfirm"
					/>
					<view v-if="keywordDraft" class="favorite-page__search-clear" @tap="handleSearchClear">
						<text class="favorite-page__search-clear-text">×</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="loading && !loadedOnce" class="favorite-page__state-card">
			<text class="favorite-page__state-title">正在整理你的收藏...</text>
			<text class="favorite-page__state-desc">收藏越多，之后回看重点题就越方便。</text>
		</view>

		<view v-else-if="favoriteState.records.length" class="favorite-page__list">
			<view
				v-for="item in favoriteState.records"
				:key="item.questionId"
				class="favorite-page__card"
			>
				<view class="favorite-page__card-head">
					<view class="favorite-page__card-tags">
						<text class="favorite-page__tag">{{ questionTypeLabel(item.questionType) }}</text>
						<text class="favorite-page__tag" :class="difficultyTagClass(item.difficultyLevel)">{{ difficultyLabel(item.difficultyLevel) }}</text>
						<text class="favorite-page__tag favorite-page__tag--bank">题库 · {{ item.bankName }}</text>
					</view>
					<button class="favorite-page__favorite-button" @tap="handleUnfavorite(item)">
						<uni-icons type="star-filled" size="13" color="#a66a3f" />
						<text class="favorite-page__favorite-button-text">已收藏</text>
					</button>
				</view>

				<view class="favorite-page__stem-wrap">
					<view
						class="favorite-page__stem"
						:class="{ 'is-collapsed': shouldShowStemToggle(item) && !isStemExpanded(item.questionId) }"
					>
						{{ item.stem }}
					</view>
					<view
						v-if="shouldShowStemToggle(item)"
						class="favorite-page__stem-toggle"
						@tap="toggleStem(item.questionId)"
					>
						<uni-icons :type="isStemExpanded(item.questionId) ? 'top' : 'bottom'" size="14" color="#7a8799" />
					</view>
				</view>

				<view class="favorite-page__meta-row">
					<view class="favorite-page__meta-pill">
						<uni-icons type="calendar" size="14" color="#7b8798" />
						<text class="favorite-page__meta-pill-text">收藏于 {{ formatFavoriteQuestionTime(item.favoriteAt) }}</text>
					</view>
					<button class="favorite-page__retry-button" @tap="handleRetry(item)">从这题开始练</button>
				</view>
			</view>
		</view>

		<view v-else class="favorite-page__empty">
			<view class="favorite-page__empty-icon-shell">
				<view class="favorite-page__empty-icon-core">
					<uni-icons :type="emptyStateIcon" size="32" color="#7d5a4f" />
				</view>
			</view>
			<text class="favorite-page__empty-title">{{ emptyStateTitle }}</text>
			<text class="favorite-page__empty-desc">{{ emptyStateDescription }}</text>
			<button class="favorite-page__empty-button" @tap="handleEmptyAction">{{ emptyStateActionText }}</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getFavoriteQuestions, unfavoriteQuestion } from '@/pages/kyzz/api/favorite'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'
import { difficultyLabel, difficultyTagClass, questionTypeLabel } from '@/pages/kyzz/practice/view'
import type {
	KyzzFavoriteQuestionState,
	KyzzFavoriteQuestionViewRecord,
	SearchConfirmEvent
} from '@/pages/kyzz/favorite/types'
import {
	createEmptyFavoriteQuestionState,
	formatFavoriteQuestionTime,
	normalizeFavoriteQuestionState
} from '@/pages/kyzz/favorite/view'

// AI 索引: KYZZ 小程序收藏题目列表页。

interface FavoritePageState {
	loading: boolean
	loadedOnce: boolean
	keyword: string
	keywordDraft: string
	favoriteState: KyzzFavoriteQuestionState
	expandedQuestionIds: number[]
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'KyzzFavoritePage',
	data(): FavoritePageState {
		return {
			loading: false,
			loadedOnce: false,
			keyword: '',
			keywordDraft: '',
			favoriteState: createEmptyFavoriteQuestionState(),
			expandedQuestionIds: []
		}
	},
	computed: {
		emptyStateKind(): 'never' | 'filtered' {
			return this.keyword.trim() ? 'filtered' : 'never'
		},
		emptyStateIcon(): string {
			return this.emptyStateKind === 'filtered' ? 'help' : 'star-filled'
		},
		emptyStateTitle(): string {
			return this.emptyStateKind === 'filtered' ? '当前搜索下没有收藏题' : '还没有收藏题目'
		},
		emptyStateDescription(): string {
			return this.emptyStateKind === 'filtered'
				? '试试换个关键词，或者清空搜索看看全部收藏。'
				: '刷题时点击题目右上角的星标，就可以把重点题收进这里。'
		},
		emptyStateActionText(): string {
			return this.emptyStateKind === 'filtered' ? '清空搜索' : '去刷题'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		difficultyLabel,
		difficultyTagClass,
		questionTypeLabel,
		formatFavoriteQuestionTime,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadFavoriteQuestions()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '收藏加载失败'),
					icon: 'none'
				})
			}
		},
		async loadFavoriteQuestions(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getFavoriteQuestions({
					keyword: this.keyword.trim() || undefined
				})
				this.favoriteState = normalizeFavoriteQuestionState(result)
				const activeQuestionIds = new Set(this.favoriteState.records.map((item) => item.questionId))
				this.expandedQuestionIds = this.expandedQuestionIds.filter((questionId) => activeQuestionIds.has(questionId))
				this.loadedOnce = true
			} catch (error) {
				this.loadedOnce = true
				uni.showToast({
					title: resolveErrorMessage(error, '收藏加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		handleSearchConfirm(event: SearchConfirmEvent): void {
			const value = (event?.detail?.value || event?.value || this.keywordDraft || '').trim()
			this.keyword = value
			this.keywordDraft = value
			this.loadFavoriteQuestions().catch(() => {})
		},
		handleSearchClear(): void {
			if (!this.keyword && !this.keywordDraft) {
				return
			}
			this.keyword = ''
			this.keywordDraft = ''
			this.loadFavoriteQuestions().catch(() => {})
		},
		shouldShowStemToggle(record: KyzzFavoriteQuestionViewRecord): boolean {
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
		async handleUnfavorite(record: KyzzFavoriteQuestionViewRecord): Promise<void> {
			try {
				await unfavoriteQuestion(record.questionId)
				this.favoriteState = {
					totalCount: Math.max(this.favoriteState.totalCount - 1, 0),
					records: this.favoriteState.records.filter((item) => item.questionId !== record.questionId)
				}
				uni.showToast({
					title: '已取消收藏',
					icon: 'none'
				})
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '取消收藏失败'),
					icon: 'none'
				})
			}
		},
		handleRetry(record: KyzzFavoriteQuestionViewRecord): void {
			openPracticeTab({
				bankId: record.bankId,
				questionId: record.questionId,
				sourceType: 'favorite',
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
				this.keyword = ''
				this.keywordDraft = ''
				this.loadFavoriteQuestions().catch(() => {})
				return
			}
			uni.switchTab({
				url: '/pages/kyzz/practice/index'
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.favorite-page {
	min-height: 100vh;
	padding: 28rpx 26rpx calc(env(safe-area-inset-bottom) + 40rpx);
	box-sizing: border-box;
	background: radial-gradient(circle at top, rgba(255, 249, 246, 0.98) 0%, rgba(244, 247, 251, 0.97) 44%, rgba(231, 238, 247, 0.95) 100%);
}

.favorite-page__toolbar {
	padding: 2rpx 8rpx 0;
}

.favorite-page__search-shell {
	margin-top: 0;
}

.favorite-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	height: 82rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: inset 0 0 0 1rpx rgba(225, 232, 241, 0.98);
}

.favorite-page__search-input {
	flex: 1;
	min-width: 0;
	height: 100%;
	font-size: 26rpx;
	color: #2d3645;
}

.favorite-page__search-placeholder {
	font-size: 24rpx;
	color: #a9b2bf;
}

.favorite-page__search-clear {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: rgba(210, 218, 229, 0.92);
}

.favorite-page__search-clear-text {
	font-size: 28rpx;
	line-height: 1;
	color: #ffffff;
}

.favorite-page__state-card,
.favorite-page__empty {
	margin-top: 22rpx;
	padding: 34rpx 28rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 20rpx 38rpx rgba(43, 52, 55, 0.06);
}

.favorite-page__state-title,
.favorite-page__empty-title {
	display: block;
	font-size: 32rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2c3443;
}

.favorite-page__state-desc,
.favorite-page__empty-desc {
	display: block;
	margin-top: 14rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #778395;
}

.favorite-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 22rpx;
}

.favorite-page__card {
	padding: 28rpx 24rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.92);
	box-shadow: 0 20rpx 38rpx rgba(43, 52, 55, 0.05);
}

.favorite-page__card-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18rpx;
}

.favorite-page__card-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.favorite-page__tag,
.favorite-page__favorite-button {
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

.favorite-page__tag {
	background: rgba(242, 246, 251, 0.98);
	color: #5d6779;
}

.favorite-page__tag.is-simple {
	background: rgba(225, 236, 226, 0.98);
	color: #466354;
}

.favorite-page__tag.is-medium {
	background: rgba(235, 240, 247, 0.98);
	color: #4f6078;
}

.favorite-page__tag.is-hard {
	background: rgba(245, 235, 226, 0.98);
	color: #8d5d47;
}

.favorite-page__tag.is-sprint {
	background: rgba(247, 226, 224, 0.98);
	color: #965251;
}

.favorite-page__tag--bank {
	max-width: 100%;
	background: linear-gradient(135deg, rgba(232, 239, 251, 0.98) 0%, rgba(242, 247, 255, 0.98) 100%);
	color: #4a6287;
}

.favorite-page__favorite-button {
	gap: 6rpx;
	margin: 0;
	background: rgba(255, 244, 229, 0.98);
	color: #8d5d47;
}

.favorite-page__favorite-button::after {
	border: 0;
}

.favorite-page__favorite-button-text {
	font-size: 20rpx;
	font-weight: 700;
}

.favorite-page__stem-wrap {
	position: relative;
	margin-top: 18rpx;
}

.favorite-page__stem {
	display: block;
	font-size: 28rpx;
	line-height: 1.7;
	font-weight: 600;
	color: #27313f;
}

.favorite-page__stem.is-collapsed {
	display: -webkit-box;
	overflow: hidden;
	text-overflow: ellipsis;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	padding-right: 50rpx;
}

.favorite-page__stem-toggle {
	position: absolute;
	right: 0;
	bottom: 3rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 42rpx;
	height: 36rpx;
	border-radius: 999rpx;
	background: rgba(243, 247, 252, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(227, 233, 241, 0.96);
}

.favorite-page__meta-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
	margin-top: 16rpx;
}

.favorite-page__meta-pill {
	display: inline-flex;
	min-width: 0;
	align-items: center;
	gap: 8rpx;
	min-height: 44rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(243, 247, 252, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(227, 233, 241, 0.96);
}

.favorite-page__meta-pill-text {
	font-size: 22rpx;
	line-height: 1.6;
	color: #7c8796;
}

.favorite-page__retry-button,
.favorite-page__empty-button {
	margin: 0;
	padding: 0 20rpx;
	height: 54rpx;
	line-height: 54rpx;
	border-radius: 18rpx;
	font-size: 22rpx;
	font-weight: 600;
}

.favorite-page__retry-button {
	flex-shrink: 0;
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
	box-shadow: 0 12rpx 22rpx rgba(84, 94, 118, 0.14);
}

.favorite-page__retry-button::after,
.favorite-page__empty-button::after {
	border: 0;
}

.favorite-page__empty {
	text-align: center;
}

.favorite-page__empty-icon-shell {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 132rpx;
	height: 132rpx;
	border-radius: 36rpx;
	background: linear-gradient(180deg, rgba(255, 242, 238, 0.98) 0%, rgba(247, 225, 218, 0.96) 100%);
	box-shadow: 0 24rpx 44rpx rgba(84, 94, 118, 0.08), inset 0 0 0 1rpx rgba(239, 210, 198, 0.88);
}

.favorite-page__empty-icon-core {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 86rpx;
	height: 86rpx;
	border-radius: 28rpx;
	background: linear-gradient(135deg, rgba(255, 233, 226, 0.98) 0%, rgba(255, 248, 245, 0.98) 100%);
}

.favorite-page__empty-button {
	margin-top: 24rpx;
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
}
</style>
