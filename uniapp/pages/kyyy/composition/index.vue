<!--
@file KyyyCompositionHomePage
@project pipker-do
@module 考研英语 / 小程序作文知识库
@description 展示作文知识库首页分面、题型入口与近年真题推荐，作为作文模块统一浏览入口。
@logic 1. 加载作文总览分面；2. 维护英一英二与大小作文切换；3. 提供题型列表、总入口与真题详情跳转。
@dependencies API: @/pages/kyyy/api/writing, Types: @/pages/kyyy/composition/types, View: @/pages/kyyy/composition/view, Component: PageShell, Component: KyyyTabbar
@index_tags 考研英语, 作文首页, 知识库, 真题, 小程序
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-composition-home theme-page"
		content-style="padding: 0 24rpx 176rpx;"
	>
		<view class="kyyy-composition-home__ambient kyyy-composition-home__ambient--left"></view>
		<view class="kyyy-composition-home__ambient kyyy-composition-home__ambient--right"></view>

		<view class="kyyy-composition-home__inner">
			<view class="kyyy-composition-home__hero">
				<text class="kyyy-composition-home__eyebrow">WRITING LIBRARY</text>
				<text class="kyyy-composition-home__title">英语作文知识库</text>
				<text class="kyyy-composition-home__desc">按考试方向、大小作文与题型组织历年真题，先查题目，再看范文与中英翻译。</text>
				<view class="kyyy-composition-home__hero-meta">
					<text class="kyyy-composition-home__meta-pill">年份 {{ overviewState.recentYears.length }}</text>
					<text class="kyyy-composition-home__meta-pill">真题 {{ totalCount }}</text>
				</view>
			</view>

			<view class="kyyy-composition-home__section">
				<view class="kyyy-composition-home__section-head">
					<text class="kyyy-composition-home__section-overline">DIRECTION</text>
					<text class="kyyy-composition-home__section-title">考试方向</text>
				</view>
				<view class="kyyy-composition-home__exam-grid">
					<view
						v-for="item in overviewState.examDirections"
						:key="item.code"
						class="kyyy-composition-home__exam-card"
						:class="{ 'is-active': selectedExamDirection === item.code }"
						@tap="handleSelectExamDirection(item.code)"
					>
						<text class="kyyy-composition-home__exam-label">{{ item.label }}</text>
						<text class="kyyy-composition-home__exam-count">{{ item.count }} 篇</text>
					</view>
				</view>
			</view>

			<view class="kyyy-composition-home__section">
				<view class="kyyy-composition-home__section-head">
					<text class="kyyy-composition-home__section-overline">SECTION</text>
					<text class="kyyy-composition-home__section-title">作文分区</text>
				</view>
				<view class="kyyy-composition-home__chip-row">
					<view
						v-for="item in overviewState.essaySections"
						:key="item.code"
						class="kyyy-composition-home__chip"
						:class="{ 'is-active': selectedEssaySection === item.code }"
						@tap="handleSelectEssaySection(item.code)"
					>
						<text>{{ item.label }}</text>
						<text class="kyyy-composition-home__chip-count">{{ item.count }}</text>
					</view>
				</view>
				<view class="kyyy-composition-home__entry-panel">
					<view class="kyyy-composition-home__entry-copy">
						<text class="kyyy-composition-home__entry-title">{{ selectedSectionLabel }}</text>
						<text class="kyyy-composition-home__entry-desc">先看题型，再按年份逐篇进入真题与范文详情。</text>
					</view>
					<view class="kyyy-composition-home__entry-button" @tap="openSectionList">进入列表</view>
				</view>
			</view>

			<view class="kyyy-composition-home__section">
				<view class="kyyy-composition-home__section-head">
					<text class="kyyy-composition-home__section-overline">CATEGORY</text>
					<text class="kyyy-composition-home__section-title">题型分类</text>
				</view>
				<view v-if="currentPromptCategories.length" class="kyyy-composition-home__category-grid">
					<view
						v-for="item in currentPromptCategories"
						:key="`${item.essaySection}-${item.code}`"
						class="kyyy-composition-home__category-card"
						@tap="openPromptCategoryList(item.code)"
					>
						<text class="kyyy-composition-home__category-title">{{ item.label }}</text>
						<text class="kyyy-composition-home__category-meta">{{ selectedSectionLabel }} · {{ item.count }} 篇</text>
					</view>
				</view>
				<view v-else class="kyyy-composition-home__state-card">
					<text class="kyyy-composition-home__state-text">当前分类还没有可展示题型</text>
				</view>
			</view>

			<view class="kyyy-composition-home__section">
				<view class="kyyy-composition-home__section-head">
					<text class="kyyy-composition-home__section-overline">RECENT</text>
					<text class="kyyy-composition-home__section-title">近年真题</text>
				</view>
				<view v-if="loading && !overviewState.loaded" class="kyyy-composition-home__state-card">
					<text class="kyyy-composition-home__state-text">正在整理作文知识库...</text>
				</view>
				<view v-else-if="overviewState.featuredRecords.length" class="kyyy-composition-home__record-list">
					<view
						v-for="item in overviewState.featuredRecords"
						:key="item.id"
						class="kyyy-composition-home__record-card"
						@tap="openDetail(item.id)"
					>
						<view class="kyyy-composition-home__record-head">
							<text class="kyyy-composition-home__record-year">{{ item.sourceYear }}</text>
							<text class="kyyy-composition-home__record-pill">{{ resolveExamDirectionLabel(item.examDirection) }}</text>
							<text class="kyyy-composition-home__record-pill">{{ resolveEssaySectionLabel(item.essaySection) }}</text>
						</view>
						<text class="kyyy-composition-home__record-title">{{ item.sourceTitle }}</text>
						<text class="kyyy-composition-home__record-meta">{{ buildEssayMetaText(item) }}</text>
					</view>
				</view>
				<view v-else class="kyyy-composition-home__state-card">
					<text class="kyyy-composition-home__state-text">当前还没有推荐真题</text>
				</view>
			</view>
		</view>

		<template #tabbar>
			<kyyy-tabbar current="composition" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWritingOverview } from '@/pages/kyyy/api/writing'
import type { KyyyWritingEssaySection, KyyyWritingOverviewState } from '@/pages/kyyy/composition/types'
import {
	buildEssayMetaText,
	createEmptyOverviewState,
	normalizeOverviewState,
	resolveEssaySectionLabel,
	resolveExamDirectionLabel
} from '@/pages/kyyy/composition/view'

interface CompositionHomePageState {
	loading: boolean
	overviewState: KyyyWritingOverviewState
	selectedExamDirection: string
	selectedEssaySection: KyyyWritingEssaySection
}

export default defineComponent({
	name: 'KyyyCompositionHomePage',
	components: {
		PageShell,
		KyyyTabbar
	},
	data(): CompositionHomePageState {
		return {
			loading: false,
			overviewState: createEmptyOverviewState(),
			selectedExamDirection: 'english_one',
			selectedEssaySection: 'small'
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		totalCount(): number {
			return this.overviewState.examDirections.reduce((sum, item) => sum + item.count, 0)
		},
		selectedSectionLabel(): string {
			return resolveEssaySectionLabel(this.selectedEssaySection)
		},
		currentPromptCategories() {
			return this.overviewState.promptCategories.filter((item) => item.essaySection === this.selectedEssaySection)
		}
	},
	methods: {
		resolveExamDirectionLabel,
		resolveEssaySectionLabel,
		buildEssayMetaText,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-composition-home] bootstrap auth failed', error)
			}
			await this.loadOverview()
		},
		async loadOverview(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getWritingOverview()
				this.overviewState = normalizeOverviewState(result)
				if (!this.overviewState.examDirections.find((item) => item.code === this.selectedExamDirection)) {
					this.selectedExamDirection = this.overviewState.examDirections[0]?.code || 'english_one'
				}
				if (!this.overviewState.essaySections.find((item) => item.code === this.selectedEssaySection)) {
					this.selectedEssaySection = (this.overviewState.essaySections[0]?.code as KyyyWritingEssaySection) || 'small'
				}
			} catch (error) {
				console.warn('[kyyy-composition-home] load overview failed', error)
				this.overviewState = {
					...this.overviewState,
					loaded: true
				}
				uni.showToast({
					title: '作文库加载失败',
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
		handleSelectEssaySection(code: string): void {
			if (code === 'small' || code === 'big') {
				this.selectedEssaySection = code
			}
		},
		openSectionList(): void {
			this.openListByFilters(this.selectedEssaySection)
		},
		openPromptCategoryList(promptCategory: string): void {
			this.openListByFilters(this.selectedEssaySection, promptCategory)
		},
		openListByFilters(essaySection: string, promptCategory = ''): void {
			const query = [
				`examDirection=${encodeURIComponent(this.selectedExamDirection)}`,
				`essaySection=${encodeURIComponent(essaySection)}`
			]
			if (promptCategory) {
				query.push(`promptCategory=${encodeURIComponent(promptCategory)}`)
			}
			uni.navigateTo({
				url: `/pages/kyyy/composition/list?${query.join('&')}`
			})
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
.kyyy-composition-home {
	position: relative;
	overflow: hidden;
}

.kyyy-composition-home__ambient {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	opacity: 0.92;
}

.kyyy-composition-home__ambient--left {
	top: 96rpx;
	left: -110rpx;
	width: 300rpx;
	height: 300rpx;
	background: radial-gradient(circle, rgba(212, 224, 250, 0.88) 0%, rgba(212, 224, 250, 0) 72%);
}

.kyyy-composition-home__ambient--right {
	right: -96rpx;
	bottom: 280rpx;
	width: 280rpx;
	height: 280rpx;
	background: radial-gradient(circle, rgba(238, 227, 216, 0.82) 0%, rgba(238, 227, 216, 0) 72%);
}

.kyyy-composition-home__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 10rpx;
}

.kyyy-composition-home__hero,
.kyyy-composition-home__section,
.kyyy-composition-home__state-card,
.kyyy-composition-home__record-card,
.kyyy-composition-home__entry-panel {
	border-radius: 32rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-composition-home__hero {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 36rpx 34rpx;
	background: linear-gradient(140deg, #59647e 0%, #8392ae 100%);
	box-shadow: 0 24rpx 48rpx rgba(60, 71, 94, 0.24);
}

.kyyy-composition-home__eyebrow {
	font-size: 18rpx;
	letter-spacing: 0.24em;
	font-weight: 700;
	color: rgba(244, 247, 255, 0.88);
}

.kyyy-composition-home__title {
	font-size: 42rpx;
	line-height: 1.22;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-composition-home__desc {
	font-size: 26rpx;
	line-height: 1.72;
	color: rgba(241, 245, 253, 0.88);
}

.kyyy-composition-home__hero-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.kyyy-composition-home__meta-pill {
	padding: 10rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.14);
	font-size: 22rpx;
	color: #f4f7ff;
}

.kyyy-composition-home__section {
	padding: 30rpx;
}

.kyyy-composition-home__section-head {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	margin-bottom: 20rpx;
}

.kyyy-composition-home__section-overline {
	font-size: 18rpx;
	letter-spacing: 0.2em;
	font-weight: 700;
	color: #8a96a6;
}

.kyyy-composition-home__section-title {
	font-size: 32rpx;
	line-height: 1.32;
	font-weight: 700;
	color: #273138;
}

.kyyy-composition-home__exam-grid,
.kyyy-composition-home__category-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 18rpx;
}

.kyyy-composition-home__exam-card,
.kyyy-composition-home__category-card {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 24rpx;
	border-radius: 24rpx;
	background: linear-gradient(180deg, rgba(248, 250, 253, 0.98), rgba(237, 242, 248, 0.9));
}

.kyyy-composition-home__exam-card.is-active {
	background: linear-gradient(140deg, rgba(225, 234, 247, 0.98), rgba(203, 216, 238, 0.96));
	box-shadow: inset 0 0 0 2rpx rgba(127, 147, 177, 0.38);
}

.kyyy-composition-home__exam-label,
.kyyy-composition-home__category-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #24303a;
}

.kyyy-composition-home__exam-count,
.kyyy-composition-home__category-meta,
.kyyy-composition-home__record-meta,
.kyyy-composition-home__state-text {
	font-size: 24rpx;
	line-height: 1.6;
	color: #687480;
}

.kyyy-composition-home__chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.kyyy-composition-home__chip {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	padding: 16rpx 22rpx;
	border-radius: 999rpx;
	background: rgba(241, 245, 249, 0.98);
	font-size: 24rpx;
	color: #566372;
}

.kyyy-composition-home__chip.is-active {
	background: linear-gradient(135deg, rgba(224, 233, 248, 0.98), rgba(206, 218, 239, 0.95));
	color: #304150;
}

.kyyy-composition-home__chip-count {
	opacity: 0.8;
}

.kyyy-composition-home__entry-panel {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
	margin-top: 20rpx;
	padding: 24rpx 26rpx;
	background: linear-gradient(135deg, rgba(246, 248, 251, 0.98), rgba(234, 239, 245, 0.92));
}

.kyyy-composition-home__entry-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
}

.kyyy-composition-home__entry-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #24303a;
}

.kyyy-composition-home__entry-desc {
	font-size: 24rpx;
	line-height: 1.6;
	color: #6c7884;
}

.kyyy-composition-home__entry-button {
	flex-shrink: 0;
	padding: 16rpx 24rpx;
	border-radius: 999rpx;
	background: #55637b;
	font-size: 24rpx;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-composition-home__record-list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-composition-home__record-card {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	padding: 24rpx 26rpx;
}

.kyyy-composition-home__record-head {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
}

.kyyy-composition-home__record-year {
	font-size: 30rpx;
	font-weight: 700;
	color: #304150;
}

.kyyy-composition-home__record-pill {
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(232, 238, 245, 0.95);
	font-size: 20rpx;
	color: #607081;
}

.kyyy-composition-home__record-title {
	font-size: 28rpx;
	line-height: 1.45;
	font-weight: 700;
	color: #263036;
}

.kyyy-composition-home__state-card {
	padding: 28rpx;
	text-align: center;
}
</style>
