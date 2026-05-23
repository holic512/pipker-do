<!--
@file KyyyCompositionListPage
@project pipker-do
@module 考研英语 / 小程序作文知识库
@description 展示作文知识库列表页，支持题型入口、年份过滤、关键词搜索与分页加载。
@logic 1. 解析入口筛选参数；2. 维护年份和关键词过滤；3. 触底加载更多并跳转详情页。
@dependencies API: @/pages/kyyy/api/writing, Types: @/pages/kyyy/composition/types, View: @/pages/kyyy/composition/view
@index_tags 考研英语, 作文列表, 知识库, 年份筛选, 真题
@author holic512
-->
<template>
	<view class="kyyy-composition-list">
		<view class="kyyy-composition-list__hero">
			<text class="kyyy-composition-list__eyebrow">WRITING LIST</text>
			<text class="kyyy-composition-list__title">{{ headerTitle }}</text>
			<text class="kyyy-composition-list__desc">从当前分类继续按年份和关键词筛选历年真题。</text>
		</view>

		<view class="kyyy-composition-list__toolbar">
			<input
				v-model="listState.keyword"
				class="kyyy-composition-list__search"
				placeholder="搜索标题 / 题目 / 标签"
				confirm-type="search"
				@confirm="handleSearchConfirm"
			/>
			<view class="kyyy-composition-list__search-action" @tap="handleSearchConfirm">查询</view>
		</view>

		<scroll-view scroll-x class="kyyy-composition-list__year-scroll" show-scrollbar="false">
			<view class="kyyy-composition-list__year-row">
				<view
					class="kyyy-composition-list__year-chip"
					:class="{ 'is-active': !listState.sourceYear }"
					@tap="handleSelectYear(null)"
				>
					全部
				</view>
				<view
					v-for="year in yearOptions"
					:key="year"
					class="kyyy-composition-list__year-chip"
					:class="{ 'is-active': listState.sourceYear === year }"
					@tap="handleSelectYear(year)"
				>
					{{ year }}
				</view>
			</view>
		</scroll-view>

		<view v-if="listState.loading && !listState.loaded" class="kyyy-composition-list__state">
			<text>正在加载作文列表...</text>
		</view>
		<view v-else-if="listState.records.length" class="kyyy-composition-list__records">
			<view
				v-for="item in listState.records"
				:key="item.id"
				class="kyyy-composition-list__card"
				@tap="openDetail(item.id)"
			>
				<view class="kyyy-composition-list__card-head">
					<text class="kyyy-composition-list__card-year">{{ item.sourceYear }}</text>
					<text class="kyyy-composition-list__card-pill">{{ resolveExamDirectionLabel(item.examDirection) }}</text>
					<text class="kyyy-composition-list__card-pill">{{ resolveEssaySectionLabel(item.essaySection) }}</text>
					<text class="kyyy-composition-list__card-pill">{{ resolvePromptCategoryLabel(item.promptCategory) }}</text>
				</view>
				<text class="kyyy-composition-list__card-title">{{ item.sourceTitle }}</text>
				<text class="kyyy-composition-list__card-meta">{{ buildEssayMetaText(item) }}</text>
				<view v-if="item.knowledgeTags.length" class="kyyy-composition-list__tag-row">
					<text
						v-for="tag in item.knowledgeTags"
						:key="`${item.id}-${tag}`"
						class="kyyy-composition-list__tag"
					>
						{{ tag }}
					</text>
				</view>
			</view>
			<view class="kyyy-composition-list__more-state">
				<text>{{ listState.loading ? '正在加载更多...' : listState.hasMore ? '上拉继续加载' : '已经到底了' }}</text>
			</view>
		</view>
		<view v-else class="kyyy-composition-list__state">
			<text>当前筛选下没有作文内容</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWritingEssays, getWritingOverview } from '@/pages/kyyy/api/writing'
import type { KyyyWritingEssayListState } from '@/pages/kyyy/composition/types'
import {
	buildEssayMetaText,
	buildListHeaderTitle,
	createEmptyListState,
	normalizeEssayListResponse,
	normalizeOverviewState,
	resolveEssaySectionLabel,
	resolveExamDirectionLabel,
	resolvePromptCategoryLabel
} from '@/pages/kyyy/composition/view'

interface CompositionListPageState {
	examDirection: string
	essaySection: string
	promptCategory: string
	listState: KyyyWritingEssayListState
	yearOptions: number[]
}

function normalizeQueryText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

export default defineComponent({
	name: 'KyyyCompositionListPage',
	data(): CompositionListPageState {
		return {
			examDirection: 'english_one',
			essaySection: 'small',
			promptCategory: '',
			listState: createEmptyListState(10),
			yearOptions: []
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.examDirection = normalizeQueryText(query?.examDirection) || 'english_one'
		this.essaySection = normalizeQueryText(query?.essaySection) || 'small'
		this.promptCategory = normalizeQueryText(query?.promptCategory)
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
			return buildListHeaderTitle(this.examDirection, this.essaySection, this.promptCategory)
		}
	},
	methods: {
		resolveExamDirectionLabel,
		resolveEssaySectionLabel,
		resolvePromptCategoryLabel,
		buildEssayMetaText,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-composition-list] bootstrap auth failed', error)
			}
			await Promise.all([this.loadOverviewYears(), this.loadList(1, false)])
		},
		async loadOverviewYears(): Promise<void> {
			try {
				const overview = normalizeOverviewState(await getWritingOverview())
				this.yearOptions = overview.recentYears
			} catch (error) {
				console.warn('[kyyy-composition-list] load years failed', error)
				this.yearOptions = []
			}
		},
		async loadList(pageNo: number, append: boolean): Promise<void> {
			if (this.listState.loading) {
				return
			}
			this.listState.loading = true
			try {
				const result = normalizeEssayListResponse(await getWritingEssays({
					examDirection: this.examDirection,
					essaySection: this.essaySection,
					promptCategory: this.promptCategory || null,
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
				console.warn('[kyyy-composition-list] load list failed', error)
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
					title: '作文列表加载失败',
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
		openDetail(essayId: number): void {
			if (!essayId) {
				return
			}
			uni.navigateTo({
				url: `/pages/kyyy/composition/detail?essayId=${essayId}`
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-composition-list {
	min-height: 100vh;
	padding: 24rpx;
	background: linear-gradient(180deg, #f6f8fb 0%, #eef2f6 100%);
}

.kyyy-composition-list__hero,
.kyyy-composition-list__toolbar,
.kyyy-composition-list__card,
.kyyy-composition-list__state,
.kyyy-composition-list__more-state {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-composition-list__hero {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 32rpx;
	margin-bottom: 20rpx;
	background: linear-gradient(140deg, #5a647c 0%, #8292ad 100%);
}

.kyyy-composition-list__eyebrow {
	font-size: 18rpx;
	letter-spacing: 0.22em;
	font-weight: 700;
	color: rgba(245, 248, 255, 0.86);
}

.kyyy-composition-list__title {
	font-size: 36rpx;
	line-height: 1.28;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-composition-list__desc {
	font-size: 24rpx;
	line-height: 1.6;
	color: rgba(241, 245, 252, 0.88);
}

.kyyy-composition-list__toolbar {
	display: flex;
	gap: 16rpx;
	align-items: center;
	padding: 20rpx;
	margin-bottom: 18rpx;
}

.kyyy-composition-list__search {
	flex: 1;
	height: 72rpx;
	padding: 0 22rpx;
	border-radius: 20rpx;
	background: #f3f6fa;
	font-size: 26rpx;
	color: #2a3138;
}

.kyyy-composition-list__search-action {
	padding: 16rpx 24rpx;
	border-radius: 999rpx;
	background: #55637b;
	font-size: 24rpx;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-composition-list__year-scroll {
	margin-bottom: 18rpx;
	white-space: nowrap;
}

.kyyy-composition-list__year-row {
	display: inline-flex;
	gap: 14rpx;
	padding-right: 12rpx;
}

.kyyy-composition-list__year-chip {
	padding: 14rpx 24rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.94);
	font-size: 24rpx;
	color: #607080;
	box-shadow: 0 12rpx 28rpx rgba(43, 52, 55, 0.05);
}

.kyyy-composition-list__year-chip.is-active {
	background: linear-gradient(135deg, rgba(224, 233, 248, 0.98), rgba(206, 218, 239, 0.95));
	color: #324150;
}

.kyyy-composition-list__records {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-composition-list__card {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	padding: 26rpx;
}

.kyyy-composition-list__card-head {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
}

.kyyy-composition-list__card-year {
	font-size: 30rpx;
	font-weight: 700;
	color: #2f4050;
}

.kyyy-composition-list__card-pill,
.kyyy-composition-list__tag {
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(235, 240, 246, 0.98);
	font-size: 20rpx;
	color: #617282;
}

.kyyy-composition-list__card-title {
	font-size: 28rpx;
	line-height: 1.46;
	font-weight: 700;
	color: #263036;
}

.kyyy-composition-list__card-meta {
	font-size: 24rpx;
	line-height: 1.6;
	color: #697480;
}

.kyyy-composition-list__tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.kyyy-composition-list__state,
.kyyy-composition-list__more-state {
	padding: 28rpx;
	text-align: center;
	font-size: 24rpx;
	color: #697480;
}
</style>
