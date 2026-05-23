<!--
@file KyyyFavoriteWordPage
@project pipker-do
@module 考研英语 / 单词收藏
@description 展示用户在首页查词场景收藏的单词列表，支持搜索、取消收藏和查看详情。
@logic 1. 加载当前用户收藏单词；2. 支持关键词搜索与空态展示；3. 支持取消收藏和跳转查词详情。
@dependencies API: @/pages/kyyy/api/favorite-word, View: @/pages/kyyy/favorite-word/view, Shared: @/shared/session/session
@index_tags 考研英语, 单词收藏, 收藏列表, 查词详情
@author holic512
-->
<template>
	<view class="favorite-word-page">
		<view class="favorite-word-page__toolbar">
			<view class="favorite-word-page__search-shell">
				<view class="favorite-word-page__search-box">
					<uni-icons type="search" size="18" color="#99a5b5" />
					<input
						v-model="keywordDraft"
						class="favorite-word-page__search-input"
						placeholder="搜索单词或释义"
						placeholder-class="favorite-word-page__search-placeholder"
						confirm-type="search"
						@confirm="handleSearchConfirm"
					/>
					<view v-if="keywordDraft" class="favorite-word-page__search-clear" @tap="handleSearchClear">
						<text class="favorite-word-page__search-clear-text">×</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="loading && !loadedOnce" class="favorite-word-page__state-card">
			<text class="favorite-word-page__state-title">正在整理你的单词收藏...</text>
			<text class="favorite-word-page__state-desc">常查常忘的单词，先收进这里会更好回看。</text>
		</view>

		<view v-else-if="favoriteState.records.length" class="favorite-word-page__list">
			<view
				v-for="item in favoriteState.records"
				:key="item.wordId"
				class="favorite-word-page__card"
				@tap="handleOpenWord(item)"
			>
				<view class="favorite-word-page__card-head">
					<view class="favorite-word-page__word-block">
						<text class="favorite-word-page__word">{{ item.wordText }}</text>
						<text v-if="resolveFavoriteWordPhonetic(item)" class="favorite-word-page__phonetic">{{ resolveFavoriteWordPhonetic(item) }}</text>
					</view>
					<button class="favorite-word-page__favorite-button" @tap.stop="handleUnfavorite(item)">
						<uni-icons type="star-filled" size="13" color="#a66a3f" />
						<text class="favorite-word-page__favorite-button-text">已收藏</text>
					</button>
				</view>

				<view class="favorite-word-page__meaning-row">
					<text v-if="item.partOfSpeech" class="favorite-word-page__part-of-speech">{{ item.partOfSpeech }}</text>
					<text class="favorite-word-page__meaning">{{ item.meaningCn || '释义暂缺' }}</text>
				</view>

				<view class="favorite-word-page__meta-row">
					<view class="favorite-word-page__meta-pill">
						<uni-icons type="calendar" size="14" color="#7b8798" />
						<text class="favorite-word-page__meta-pill-text">收藏于 {{ formatFavoriteWordTime(item.favoriteAt) }}</text>
					</view>
					<view class="favorite-word-page__detail-link">
						<text class="favorite-word-page__detail-link-text">查看详情</text>
						<uni-icons type="right" size="12" color="#8592a4" />
					</view>
				</view>
			</view>
		</view>

		<view v-else class="favorite-word-page__empty">
			<view class="favorite-word-page__empty-icon-shell">
				<view class="favorite-word-page__empty-icon-core">
					<uni-icons :type="emptyStateIcon" size="32" color="#7d5a4f" />
				</view>
			</view>
			<text class="favorite-word-page__empty-title">{{ emptyStateTitle }}</text>
			<text class="favorite-word-page__empty-desc">{{ emptyStateDescription }}</text>
			<button class="favorite-word-page__empty-button" @tap="handleEmptyAction">{{ emptyStateActionText }}</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getFavoriteWords, unfavoriteWord } from '@/pages/kyyy/api/favorite-word'
import type {
	KyyyFavoriteWordState,
	KyyyFavoriteWordViewRecord,
	SearchConfirmEvent
} from '@/pages/kyyy/favorite-word/types'
import {
	createEmptyFavoriteWordState,
	formatFavoriteWordTime,
	normalizeFavoriteWordState
} from '@/pages/kyyy/favorite-word/view'

interface FavoriteWordPageState {
	loading: boolean
	loadedOnce: boolean
	keyword: string
	keywordDraft: string
	favoriteState: KyyyFavoriteWordState
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'KyyyFavoriteWordPage',
	data(): FavoriteWordPageState {
		return {
			loading: false,
			loadedOnce: false,
			keyword: '',
			keywordDraft: '',
			favoriteState: createEmptyFavoriteWordState()
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
			return this.emptyStateKind === 'filtered' ? '当前搜索下没有收藏单词' : '还没有收藏单词'
		},
		emptyStateDescription(): string {
			return this.emptyStateKind === 'filtered'
				? '试试换个关键词，或者清空搜索看看全部收藏。'
				: '在首页查词时点击星标，就可以把重点单词收进这里。'
		},
		emptyStateActionText(): string {
			return this.emptyStateKind === 'filtered' ? '清空搜索' : '去查词'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		formatFavoriteWordTime,
		resolveFavoriteWordPhonetic(record: KyyyFavoriteWordViewRecord): string {
			return record.phoneticUs || record.phoneticUk || ''
		},
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadFavoriteWords()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '收藏加载失败'),
					icon: 'none'
				})
			}
		},
		async loadFavoriteWords(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getFavoriteWords({
					keyword: this.keyword.trim() || undefined
				})
				this.favoriteState = normalizeFavoriteWordState(result)
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
			this.loadFavoriteWords().catch(() => {})
		},
		handleSearchClear(): void {
			if (!this.keyword && !this.keywordDraft) {
				return
			}
			this.keyword = ''
			this.keywordDraft = ''
			this.loadFavoriteWords().catch(() => {})
		},
		async handleUnfavorite(record: KyyyFavoriteWordViewRecord): Promise<void> {
			try {
				await unfavoriteWord(record.wordId)
				this.favoriteState = {
					totalCount: Math.max(this.favoriteState.totalCount - 1, 0),
					records: this.favoriteState.records.filter((item) => item.wordId !== record.wordId)
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
		handleOpenWord(record: KyyyFavoriteWordViewRecord): void {
			uni.navigateTo({
				url: `/pages/kyyy/index?searchKeyword=${encodeURIComponent(record.wordText)}&wordId=${record.wordId}`,
				fail: () => {
					uni.showToast({
						title: '打开单词详情失败',
						icon: 'none'
					})
				}
			})
		},
		handleEmptyAction(): void {
			if (this.emptyStateKind === 'filtered') {
				this.keyword = ''
				this.keywordDraft = ''
				this.loadFavoriteWords().catch(() => {})
				return
			}
			uni.navigateTo({
				url: '/pages/kyyy/index',
				fail: () => {
					uni.showToast({
						title: '打开查词失败',
						icon: 'none'
					})
				}
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.favorite-word-page {
	min-height: 100vh;
	padding: 28rpx 26rpx calc(env(safe-area-inset-bottom) + 40rpx);
	box-sizing: border-box;
	background: linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
}

.favorite-word-page__toolbar {
	padding: 2rpx 8rpx 0;
}

.favorite-word-page__search-box {
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

.favorite-word-page__search-input {
	flex: 1;
	min-width: 0;
	height: 100%;
	font-size: 26rpx;
	color: #2d3645;
}

.favorite-word-page__search-placeholder {
	font-size: 24rpx;
	color: #a9b2bf;
}

.favorite-word-page__search-clear {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: #9aa7ba;
}

.favorite-word-page__search-clear-text {
	font-size: 28rpx;
	line-height: 1;
	color: #ffffff;
}

.favorite-word-page__state-card,
.favorite-word-page__empty {
	margin-top: 22rpx;
	padding: 34rpx 28rpx;
	border-radius: 30rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 34rpx rgba(45, 58, 77, 0.06);
	border: 1rpx solid #d6deea;
}

.favorite-word-page__state-title,
.favorite-word-page__empty-title {
	display: block;
	font-size: 32rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2c3443;
}

.favorite-word-page__state-desc,
.favorite-word-page__empty-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 25rpx;
	line-height: 1.7;
	color: #7d8794;
}

.favorite-word-page__list {
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	margin-top: 22rpx;
}

.favorite-word-page__card {
	padding: 28rpx;
	border-radius: 30rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 34rpx rgba(45, 58, 77, 0.06);
	border: 1rpx solid #d6deea;
}

.favorite-word-page__card-head,
.favorite-word-page__meta-row,
.favorite-word-page__meaning-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
}

.favorite-word-page__word-block {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.favorite-word-page__word {
	font-size: 36rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #253140;
	word-break: break-word;
}

.favorite-word-page__phonetic {
	font-size: 22rpx;
	line-height: 1.4;
	color: #8290a3;
}

.favorite-word-page__favorite-button,
.favorite-word-page__empty-button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 58rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	border: none;
	background: linear-gradient(135deg, rgba(252, 243, 229, 0.98), rgba(243, 225, 204, 0.95));
	color: #8c5e37;
}

.favorite-word-page__favorite-button::after,
.favorite-word-page__empty-button::after {
	border: none;
}

.favorite-word-page__favorite-button-text,
.favorite-word-page__empty-button {
	font-size: 24rpx;
	font-weight: 600;
}

.favorite-word-page__meaning-row {
	align-items: flex-start;
	margin-top: 18rpx;
}

.favorite-word-page__part-of-speech {
	flex: none;
	padding: 6rpx 14rpx;
	border-radius: 999rpx;
	background: #eef3fb;
	font-size: 20rpx;
	line-height: 1.2;
	color: #68778c;
}

.favorite-word-page__meaning {
	flex: 1;
	font-size: 26rpx;
	line-height: 1.7;
	color: #4d5968;
}

.favorite-word-page__meta-row {
	margin-top: 20rpx;
}

.favorite-word-page__meta-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
	padding: 12rpx 16rpx;
	border-radius: 999rpx;
	background: #f4f7fb;
}

.favorite-word-page__meta-pill-text,
.favorite-word-page__detail-link-text {
	font-size: 22rpx;
	line-height: 1.3;
	color: #738093;
}

.favorite-word-page__detail-link {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
}

.favorite-word-page__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
}

.favorite-word-page__empty-icon-shell {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	background: linear-gradient(180deg, rgba(244, 229, 214, 0.92), rgba(234, 214, 197, 0.92));
}

.favorite-word-page__empty-icon-core {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 94rpx;
	height: 94rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.82);
}

.favorite-word-page__empty-title {
	margin-top: 24rpx;
}

.favorite-word-page__empty-desc {
	margin-top: 12rpx;
}

.favorite-word-page__empty-button {
	margin-top: 26rpx;
	padding: 0 30rpx;
}
</style>
