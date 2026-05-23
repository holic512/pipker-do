<!--
@file KyyyTranslationListPage
@project pipker-do
@module 考研英语 / 小程序翻译知识库
@description 展示翻译知识库列表页，支持方向、模式、年份、关键词筛选与分页加载。
@logic 1. 解析入口筛选参数；2. 维护年份和关键词过滤；3. 触底加载更多并跳转详情页。
@dependencies API: @/pages/kyyy/api/translation, Types: @/pages/kyyy/translation/types, View: @/pages/kyyy/translation/view
@index_tags 考研英语, 翻译列表, 知识库, 年份筛选, 真题
@author holic512
-->
<template>
	<view class="kyyy-translation-list">
		<view class="kyyy-translation-list__hero">
			<text class="kyyy-translation-list__title">{{ headerTitle }}</text>
			<text class="kyyy-translation-list__desc">按年份和关键词继续筛选历年翻译真题。</text>
		</view>

		<view class="kyyy-translation-list__toolbar">
			<input
				v-model="listState.keyword"
				class="kyyy-translation-list__search"
				placeholder="搜索标题 / 原文 / 标签"
				confirm-type="search"
				@confirm="handleSearchConfirm"
			/>
			<view class="kyyy-translation-list__search-action" @tap="handleSearchConfirm">查询</view>
		</view>

		<scroll-view scroll-x class="kyyy-translation-list__year-scroll" show-scrollbar="false">
			<view class="kyyy-translation-list__year-row">
				<view
					class="kyyy-translation-list__year-chip"
					:class="{ 'is-active': !listState.sourceYear }"
					@tap="handleSelectYear(null)"
				>
					全部
				</view>
				<view
					v-for="year in yearOptions"
					:key="year"
					class="kyyy-translation-list__year-chip"
					:class="{ 'is-active': listState.sourceYear === year }"
					@tap="handleSelectYear(year)"
				>
					{{ year }}
				</view>
			</view>
		</scroll-view>

		<view v-if="listState.loading && !listState.loaded" class="kyyy-translation-list__state">
			<text>正在加载翻译列表...</text>
		</view>
		<view v-else-if="listState.records.length" class="kyyy-translation-list__records">
			<view
				v-for="item in listState.records"
				:key="item.id"
				class="kyyy-translation-list__card"
				@tap="openDetail(item.id)"
			>
				<view class="kyyy-translation-list__card-head">
					<text class="kyyy-translation-list__card-year">{{ item.sourceYear }}</text>
					<text class="kyyy-translation-list__card-pill">{{ resolveExamDirectionLabel(item.examDirection) }}</text>
					<text class="kyyy-translation-list__card-pill">{{ resolveTranslationModeLabel(item.translationMode) }}</text>
				</view>
				<text class="kyyy-translation-list__card-title">{{ item.sourceTitle }}</text>
				<text class="kyyy-translation-list__card-meta">{{ buildTranslationMetaText(item) }}</text>
				<view v-if="item.knowledgeTags.length" class="kyyy-translation-list__tag-row">
					<text
						v-for="tag in item.knowledgeTags"
						:key="`${item.id}-${tag}`"
						class="kyyy-translation-list__tag"
					>
						{{ tag }}
					</text>
				</view>
			</view>
			<view class="kyyy-translation-list__more-state">
				<text>{{ listState.loading ? '正在加载更多...' : listState.hasMore ? '上拉继续加载' : '已经到底了' }}</text>
			</view>
		</view>
		<view v-else class="kyyy-translation-list__state">
			<text>当前筛选下没有翻译内容</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getTranslationOverview, getTranslationPassages } from '@/pages/kyyy/api/translation'
import type { KyyyTranslationListState } from '@/pages/kyyy/translation/types'
import {
	buildTranslationListTitle,
	buildTranslationMetaText,
	createEmptyListState,
	normalizeOverviewState,
	normalizeTranslationListResponse,
	resolveExamDirectionLabel,
	resolveTranslationModeLabel
} from '@/pages/kyyy/translation/view'

interface TranslationListPageState {
	examDirection: string
	translationMode: string
	listState: KyyyTranslationListState
	yearOptions: number[]
}

function normalizeQueryText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

function toSourceYear(value: unknown): number | null {
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

export default defineComponent({
	name: 'KyyyTranslationListPage',
	data(): TranslationListPageState {
		return {
			examDirection: '',
			translationMode: '',
			listState: createEmptyListState(10),
			yearOptions: []
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.examDirection = normalizeQueryText(query?.examDirection)
		this.translationMode = normalizeQueryText(query?.translationMode)
		this.listState.sourceYear = toSourceYear(query?.sourceYear)
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	onReachBottom() {
		if (this.listState.hasMore && !this.listState.loading) {
			this.loadList(this.listState.pageNo + 1, true)
		}
	},
	computed: {
		headerTitle(): string {
			return buildTranslationListTitle(this.examDirection, this.translationMode)
		}
	},
	methods: {
		resolveExamDirectionLabel,
		resolveTranslationModeLabel,
		buildTranslationMetaText,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-translation-list] bootstrap auth failed', error)
			}
			await Promise.all([this.loadOverviewYears(), this.loadList(1, false)])
		},
		async loadOverviewYears(): Promise<void> {
			try {
				const overview = normalizeOverviewState(await getTranslationOverview())
				this.yearOptions = overview.recentYears
			} catch (error) {
				console.warn('[kyyy-translation-list] load years failed', error)
				this.yearOptions = []
			}
		},
		async loadList(pageNo: number, append: boolean): Promise<void> {
			if (this.listState.loading) {
				return
			}
			this.listState.loading = true
			try {
				const result = normalizeTranslationListResponse(await getTranslationPassages({
					examDirection: this.examDirection || null,
					translationMode: this.translationMode || null,
					sourceYear: this.listState.sourceYear,
					keyword: this.listState.keyword || null,
					pageNo,
					pageSize: this.listState.pageSize
				}))
				this.listState = {
					...this.listState,
					records: append ? [...this.listState.records, ...result.records] : result.records,
					pageNo: result.pageNo,
					pageSize: result.pageSize,
					total: result.total,
					hasMore: result.hasMore,
					loaded: result.loaded
				}
			} catch (error) {
				console.warn('[kyyy-translation-list] load list failed', error)
				if (!append) {
					this.listState = {
						...this.listState,
						records: [],
						total: 0,
						hasMore: false,
						loaded: true
					}
				}
				uni.showToast({
					title: '翻译列表加载失败',
					icon: 'none'
				})
			} finally {
				this.listState.loading = false
			}
		},
		handleSelectYear(year: number | null): void {
			this.listState.sourceYear = year
			this.loadList(1, false)
		},
		handleSearchConfirm(): void {
			this.listState.keyword = this.listState.keyword.trim()
			this.loadList(1, false)
		},
		openDetail(passageId: number): void {
			if (!passageId) {
				return
			}
			uni.navigateTo({
				url: `/pages/kyyy/translation/detail?passageId=${passageId}`
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-translation-list {
	min-height: 100vh;
	padding: 24rpx;
	background: linear-gradient(180deg, #f6f8fb 0%, #eef2f6 100%);
}

.kyyy-translation-list__hero,
.kyyy-translation-list__toolbar,
.kyyy-translation-list__card,
.kyyy-translation-list__state,
.kyyy-translation-list__more-state {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-translation-list__hero {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 32rpx;
	margin-bottom: 20rpx;
	background: linear-gradient(140deg, #556070 0%, #7d8c9f 100%);
}

.kyyy-translation-list__title {
	font-size: 38rpx;
	line-height: 1.28;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-translation-list__desc {
	font-size: 24rpx;
	line-height: 1.7;
	color: rgba(242, 246, 252, 0.88);
}

.kyyy-translation-list__toolbar {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 20rpx;
	margin-bottom: 18rpx;
}

.kyyy-translation-list__search {
	flex: 1;
	min-width: 0;
	height: 76rpx;
	padding: 0 22rpx;
	border-radius: 20rpx;
	background: #f3f6fa;
	font-size: 24rpx;
	color: #24313c;
}

.kyyy-translation-list__search-action {
	flex-shrink: 0;
	padding: 16rpx 24rpx;
	border-radius: 20rpx;
	background: #4f6375;
	font-size: 24rpx;
	font-weight: 600;
	color: #ffffff;
}

.kyyy-translation-list__year-scroll {
	margin-bottom: 18rpx;
	white-space: nowrap;
}

.kyyy-translation-list__year-row,
.kyyy-translation-list__card-head,
.kyyy-translation-list__tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	align-items: center;
}

.kyyy-translation-list__year-chip,
.kyyy-translation-list__card-pill,
.kyyy-translation-list__tag {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
}

.kyyy-translation-list__year-chip {
	background: #eef2f6;
	color: #34414d;
}

.kyyy-translation-list__year-chip.is-active {
	background: #4f6375;
	color: #ffffff;
}

.kyyy-translation-list__records {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-translation-list__card {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 24rpx;
}

.kyyy-translation-list__card-year {
	font-size: 30rpx;
	font-weight: 700;
	color: #23303b;
}

.kyyy-translation-list__card-pill,
.kyyy-translation-list__tag {
	background: #eef2f7;
	color: #566574;
}

.kyyy-translation-list__card-title {
	font-size: 30rpx;
	line-height: 1.46;
	font-weight: 700;
	color: #25323d;
}

.kyyy-translation-list__card-meta,
.kyyy-translation-list__state,
.kyyy-translation-list__more-state {
	font-size: 24rpx;
	line-height: 1.7;
	color: #667380;
}

.kyyy-translation-list__state,
.kyyy-translation-list__more-state {
	padding: 30rpx;
	text-align: center;
}
</style>
