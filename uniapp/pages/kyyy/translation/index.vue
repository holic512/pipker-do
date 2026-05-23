<!--
@file KyyyTranslationHomePage
@project pipker-do
@module 考研英语 / 小程序翻译知识库
@description 展示翻译知识库首页分面、年份入口与近年真题推荐，作为翻译模块统一浏览入口。
@logic 1. 加载翻译总览分面；2. 维护英一英二与模式筛选；3. 提供列表入口与详情跳转。
@dependencies API: @/pages/kyyy/api/translation, Types: @/pages/kyyy/translation/types, View: @/pages/kyyy/translation/view, Component: PageShell, Component: KyyyTabbar
@index_tags 考研英语, 翻译首页, 知识库, 真题, 小程序
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-translation-home theme-page"
		content-style="padding: 0 24rpx 176rpx;"
	>
		<view class="kyyy-translation-home__inner">
			<view class="kyyy-translation-home__hero">
				<text class="kyyy-translation-home__title">英语翻译知识库</text>
				<text class="kyyy-translation-home__desc">按英一、英二和翻译模式整理历年真题，先看原文，再看分段译文和参考译文。</text>
				<view class="kyyy-translation-home__hero-meta">
					<text class="kyyy-translation-home__meta-pill">年份 {{ overviewState.recentYears.length }}</text>
					<text class="kyyy-translation-home__meta-pill">真题 {{ totalCount }}</text>
				</view>
			</view>

			<view class="kyyy-translation-home__section">
				<text class="kyyy-translation-home__section-title">考试方向</text>
				<view class="kyyy-translation-home__card-grid">
					<view
						v-for="item in overviewState.examDirections"
						:key="item.code"
						class="kyyy-translation-home__facet-card"
						:class="{ 'is-active': selectedExamDirection === item.code }"
						@tap="handleSelectExamDirection(item.code)"
					>
						<text class="kyyy-translation-home__facet-label">{{ item.label }}</text>
						<text class="kyyy-translation-home__facet-count">{{ item.count }} 篇</text>
					</view>
				</view>
			</view>

			<view class="kyyy-translation-home__section">
				<text class="kyyy-translation-home__section-title">翻译模式</text>
				<view class="kyyy-translation-home__chip-row">
					<view
						v-for="item in overviewState.translationModes"
						:key="item.code"
						class="kyyy-translation-home__chip"
						:class="{ 'is-active': selectedTranslationMode === item.code }"
						@tap="handleSelectTranslationMode(item.code)"
					>
						<text>{{ item.label }}</text>
						<text class="kyyy-translation-home__chip-count">{{ item.count }}</text>
					</view>
				</view>
				<view class="kyyy-translation-home__entry-panel">
					<view class="kyyy-translation-home__entry-copy">
						<text class="kyyy-translation-home__entry-title">{{ currentSelectionTitle }}</text>
						<text class="kyyy-translation-home__entry-desc">进入列表后可继续按年份和关键词检索。</text>
					</view>
					<view class="kyyy-translation-home__entry-button" @tap="openList()">进入列表</view>
				</view>
			</view>

			<view class="kyyy-translation-home__section">
				<text class="kyyy-translation-home__section-title">年份</text>
				<scroll-view scroll-x class="kyyy-translation-home__year-scroll" show-scrollbar="false">
					<view class="kyyy-translation-home__year-row">
						<view
							v-for="year in overviewState.recentYears"
							:key="year"
							class="kyyy-translation-home__year-chip"
							@tap="openList(year)"
						>
							{{ year }}
						</view>
					</view>
				</scroll-view>
			</view>

			<view class="kyyy-translation-home__section">
				<text class="kyyy-translation-home__section-title">近年真题</text>
				<view v-if="loading && !overviewState.loaded" class="kyyy-translation-home__state-card">
					<text class="kyyy-translation-home__state-text">正在整理翻译知识库...</text>
				</view>
				<view v-else-if="overviewState.featuredRecords.length" class="kyyy-translation-home__record-list">
					<view
						v-for="item in overviewState.featuredRecords"
						:key="item.id"
						class="kyyy-translation-home__record-card"
						@tap="openDetail(item.id)"
					>
						<view class="kyyy-translation-home__record-head">
							<text class="kyyy-translation-home__record-year">{{ item.sourceYear }}</text>
							<text class="kyyy-translation-home__record-pill">{{ resolveExamDirectionLabel(item.examDirection) }}</text>
							<text class="kyyy-translation-home__record-pill">{{ resolveTranslationModeLabel(item.translationMode) }}</text>
						</view>
						<text class="kyyy-translation-home__record-title">{{ item.sourceTitle }}</text>
						<text class="kyyy-translation-home__record-meta">{{ buildTranslationMetaText(item) }}</text>
					</view>
				</view>
				<view v-else class="kyyy-translation-home__state-card">
					<text class="kyyy-translation-home__state-text">当前还没有推荐真题</text>
				</view>
			</view>
		</view>

		<template #tabbar>
			<kyyy-tabbar current="translation" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getTranslationOverview } from '@/pages/kyyy/api/translation'
import type { KyyyTranslationMode, KyyyTranslationOverviewState } from '@/pages/kyyy/translation/types'
import {
	buildTranslationMetaText,
	createEmptyOverviewState,
	normalizeOverviewState,
	resolveExamDirectionLabel,
	resolveTranslationModeLabel
} from '@/pages/kyyy/translation/view'

interface TranslationHomePageState {
	loading: boolean
	overviewState: KyyyTranslationOverviewState
	selectedExamDirection: string
	selectedTranslationMode: KyyyTranslationMode
}

export default defineComponent({
	name: 'KyyyTranslationHomePage',
	components: {
		PageShell,
		KyyyTabbar
	},
	data(): TranslationHomePageState {
		return {
			loading: false,
			overviewState: createEmptyOverviewState(),
			selectedExamDirection: 'english_one',
			selectedTranslationMode: 'segmented'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		totalCount(): number {
			return this.overviewState.examDirections.reduce((sum, item) => sum + item.count, 0)
		},
		currentSelectionTitle(): string {
			return `${resolveExamDirectionLabel(this.selectedExamDirection)} · ${resolveTranslationModeLabel(this.selectedTranslationMode)}`
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
				console.warn('[kyyy-translation-home] bootstrap auth failed', error)
			}
			await this.loadOverview()
		},
		async loadOverview(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getTranslationOverview()
				this.overviewState = normalizeOverviewState(result)
				if (!this.overviewState.examDirections.find((item) => item.code === this.selectedExamDirection)) {
					this.selectedExamDirection = this.overviewState.examDirections[0]?.code || 'english_one'
				}
				if (!this.overviewState.translationModes.find((item) => item.code === this.selectedTranslationMode)) {
					this.selectedTranslationMode = (this.overviewState.translationModes[0]?.code as KyyyTranslationMode) || 'segmented'
				}
			} catch (error) {
				console.warn('[kyyy-translation-home] load overview failed', error)
				this.overviewState = {
					...this.overviewState,
					loaded: true
				}
				uni.showToast({
					title: '翻译库加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		handleSelectExamDirection(code: string): void {
			if (!code) {
				return
			}
			this.selectedExamDirection = code
		},
		handleSelectTranslationMode(code: string): void {
			if (code === 'segmented' || code === 'passage') {
				this.selectedTranslationMode = code
			}
		},
		openList(sourceYear?: number): void {
			const query = [
				`examDirection=${encodeURIComponent(this.selectedExamDirection)}`,
				`translationMode=${encodeURIComponent(this.selectedTranslationMode)}`
			]
			if (sourceYear) {
				query.push(`sourceYear=${sourceYear}`)
			}
			uni.navigateTo({
				url: `/pages/kyyy/translation/list?${query.join('&')}`
			})
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
.kyyy-translation-home__inner {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 10rpx;
}

.kyyy-translation-home__hero,
.kyyy-translation-home__section,
.kyyy-translation-home__state-card,
.kyyy-translation-home__record-card,
.kyyy-translation-home__entry-panel,
.kyyy-translation-home__facet-card {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-translation-home__hero {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 36rpx 34rpx;
	background: linear-gradient(140deg, #556070 0%, #7d8c9f 100%);
	box-shadow: 0 24rpx 48rpx rgba(60, 71, 94, 0.22);
}

.kyyy-translation-home__title {
	font-size: 42rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-translation-home__desc {
	font-size: 26rpx;
	line-height: 1.7;
	color: rgba(242, 246, 252, 0.9);
}

.kyyy-translation-home__hero-meta,
.kyyy-translation-home__record-head,
.kyyy-translation-home__year-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	align-items: center;
}

.kyyy-translation-home__meta-pill,
.kyyy-translation-home__record-pill,
.kyyy-translation-home__year-chip {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
}

.kyyy-translation-home__meta-pill {
	background: rgba(255, 255, 255, 0.16);
	color: #f3f7ff;
}

.kyyy-translation-home__section {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	padding: 28rpx;
}

.kyyy-translation-home__section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #23303b;
}

.kyyy-translation-home__card-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
}

.kyyy-translation-home__facet-card {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	padding: 24rpx;
	border: 2rpx solid transparent;
}

.kyyy-translation-home__facet-card.is-active,
.kyyy-translation-home__chip.is-active {
	border-color: rgba(81, 99, 124, 0.28);
	background: rgba(236, 241, 247, 0.98);
}

.kyyy-translation-home__facet-label {
	font-size: 28rpx;
	font-weight: 700;
	color: #24313c;
}

.kyyy-translation-home__facet-count {
	font-size: 22rpx;
	color: #637180;
}

.kyyy-translation-home__chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
}

.kyyy-translation-home__chip {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	padding: 14rpx 18rpx;
	border-radius: 999rpx;
	background: #f3f6fa;
	border: 2rpx solid transparent;
	font-size: 24rpx;
	color: #31404d;
}

.kyyy-translation-home__chip-count {
	font-size: 22rpx;
	color: #6d7986;
}

.kyyy-translation-home__entry-panel {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
	padding: 24rpx;
	background: linear-gradient(180deg, #ffffff 0%, #f6f8fb 100%);
}

.kyyy-translation-home__entry-copy {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	flex: 1;
	min-width: 0;
}

.kyyy-translation-home__entry-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #24313c;
}

.kyyy-translation-home__entry-desc,
.kyyy-translation-home__record-meta,
.kyyy-translation-home__state-text {
	font-size: 24rpx;
	line-height: 1.7;
	color: #667380;
}

.kyyy-translation-home__entry-button {
	flex-shrink: 0;
	padding: 16rpx 24rpx;
	border-radius: 20rpx;
	background: #4f6375;
	font-size: 24rpx;
	font-weight: 600;
	color: #ffffff;
}

.kyyy-translation-home__year-scroll {
	white-space: nowrap;
}

.kyyy-translation-home__year-chip {
	background: #eef2f6;
	color: #34414d;
}

.kyyy-translation-home__record-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-translation-home__record-card {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 24rpx;
}

.kyyy-translation-home__record-year {
	font-size: 30rpx;
	font-weight: 700;
	color: #23303b;
}

.kyyy-translation-home__record-pill {
	background: #eef2f7;
	color: #526171;
}

.kyyy-translation-home__record-title {
	font-size: 30rpx;
	line-height: 1.45;
	font-weight: 700;
	color: #25323d;
}

.kyyy-translation-home__state-card {
	padding: 30rpx;
	text-align: center;
}
</style>
