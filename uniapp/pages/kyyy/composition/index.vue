<!--
@file KyyyCompositionHomePage
@project pipker-do
@module 考研英语 / 小程序作文知识库
@description 展示作文知识库首页筛选、列表入口与近年真题推荐，作为作文模块统一浏览入口。
@logic 1. 加载作文总览分面；2. 用方向、年份、大小作文与类别下拉筛选；3. 提供列表入口与真题详情跳转。
@dependencies API: @/pages/kyyy/api/writing, Types: @/pages/kyyy/composition/types, View: @/pages/kyyy/composition/view, Component: PageShell, Component: KyyyTabbar
@index_tags 考研英语, 作文首页, 知识库, 筛选, 真题
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-composition-home theme-page"
		content-style="padding: 0 24rpx 176rpx;"
	>
		<view class="kyyy-composition-home__inner">
			<view class="kyyy-composition-home__section">
				<text class="kyyy-composition-home__section-title">英语作文知识库</text>
				<view class="kyyy-composition-home__toolbar">
					<view class="kyyy-composition-home__toolbar-row">
						<picker
							mode="selector"
							:range="examDirectionOptions"
							range-key="label"
							:value="selectedExamDirectionIndex"
							@change="handleExamDirectionPickerChange"
						>
							<view class="kyyy-composition-home__select">
								<text class="kyyy-composition-home__select-label">方向</text>
								<text class="kyyy-composition-home__select-value">{{ selectedExamDirectionLabel }}</text>
							</view>
						</picker>
						<picker
							mode="selector"
							:range="yearOptions"
							range-key="label"
							:value="selectedYearIndex"
							@change="handleYearPickerChange"
						>
							<view class="kyyy-composition-home__select">
								<text class="kyyy-composition-home__select-label">年份</text>
								<text class="kyyy-composition-home__select-value">{{ selectedYearLabel }}</text>
							</view>
						</picker>
					</view>
					<view class="kyyy-composition-home__toolbar-row">
						<picker
							mode="selector"
							:range="essaySectionOptions"
							range-key="label"
							:value="selectedEssaySectionIndex"
							@change="handleEssaySectionPickerChange"
						>
							<view class="kyyy-composition-home__select">
								<text class="kyyy-composition-home__select-label">分区</text>
								<text class="kyyy-composition-home__select-value">{{ selectedSectionLabel }}</text>
							</view>
						</picker>
						<picker
							mode="selector"
							:range="promptCategoryOptions"
							range-key="label"
							:value="selectedPromptCategoryIndex"
							@change="handlePromptCategoryPickerChange"
						>
							<view class="kyyy-composition-home__select">
								<text class="kyyy-composition-home__select-label">类别</text>
								<text class="kyyy-composition-home__select-value">{{ selectedPromptCategoryLabel }}</text>
							</view>
						</picker>
					</view>
				</view>
				<view class="kyyy-composition-home__summary-row">
					<view class="kyyy-composition-home__summary-copy">
						<text class="kyyy-composition-home__summary-title">{{ currentSelectionTitle }}</text>
					</view>
					<view class="kyyy-composition-home__entry-button" @tap="openSectionList">进入列表</view>
				</view>
			</view>

			<view class="kyyy-composition-home__section">
				<view v-if="loading && !overviewState.loaded" class="kyyy-composition-home__state-card">
					<text class="kyyy-composition-home__state-text">正在整理作文知识库...</text>
				</view>
				<view v-else-if="overviewState.featuredRecords.length" class="kyyy-composition-home__records-panel">
					<view class="kyyy-composition-home__record-list">
						<view
							v-for="item in overviewState.featuredRecords"
							:key="item.id"
							class="kyyy-composition-home__record-card"
							@tap="openDetail(item.id)"
						>
							<text class="kyyy-composition-home__record-title">{{ item.sourceTitle }}</text>
							<text class="kyyy-composition-home__record-meta">{{ buildEssayMetaText(item) }}</text>
						</view>
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
	resolveExamDirectionLabel,
	resolvePromptCategoryLabel
} from '@/pages/kyyy/composition/view'

interface CompositionHomePageState {
	loading: boolean
	overviewState: KyyyWritingOverviewState
	selectedExamDirection: string
	selectedEssaySection: KyyyWritingEssaySection
	selectedPromptCategory: string
	selectedSourceYear: number | null
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
			selectedEssaySection: 'small',
			selectedPromptCategory: '',
			selectedSourceYear: null
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		examDirectionOptions(): Array<{ code: string; label: string }> {
			return this.overviewState.examDirections.map((item) => ({ code: item.code, label: item.label }))
		},
		essaySectionOptions(): Array<{ code: string; label: string }> {
			return this.overviewState.essaySections.map((item) => ({ code: item.code, label: item.label }))
		},
		promptCategoryOptions(): Array<{ code: string; label: string }> {
			return [{ code: '', label: '全部类别' }].concat(
				this.overviewState.promptCategories
					.filter((item) => item.essaySection === this.selectedEssaySection)
					.map((item) => ({ code: item.code, label: item.label }))
			)
		},
		yearOptions(): Array<{ value: number | null; label: string }> {
			return [{ value: null, label: '全部年份' }].concat(
				this.overviewState.recentYears.map((year) => ({ value: year, label: `${year}` }))
			)
		},
		selectedExamDirectionIndex(): number {
			const index = this.examDirectionOptions.findIndex((item) => item.code === this.selectedExamDirection)
			return index >= 0 ? index : 0
		},
		selectedEssaySectionIndex(): number {
			const index = this.essaySectionOptions.findIndex((item) => item.code === this.selectedEssaySection)
			return index >= 0 ? index : 0
		},
		selectedPromptCategoryIndex(): number {
			const index = this.promptCategoryOptions.findIndex((item) => item.code === this.selectedPromptCategory)
			return index >= 0 ? index : 0
		},
		selectedYearIndex(): number {
			const index = this.yearOptions.findIndex((item) => item.value === this.selectedSourceYear)
			return index >= 0 ? index : 0
		},
		selectedExamDirectionLabel(): string {
			return resolveExamDirectionLabel(this.selectedExamDirection)
		},
		selectedSectionLabel(): string {
			return resolveEssaySectionLabel(this.selectedEssaySection)
		},
		selectedPromptCategoryLabel(): string {
			return this.selectedPromptCategory ? resolvePromptCategoryLabel(this.selectedPromptCategory) : '全部类别'
		},
		selectedYearLabel(): string {
			return this.selectedSourceYear ? `${this.selectedSourceYear}` : '全部年份'
		},
		currentSelectionTitle(): string {
			const parts = [resolveExamDirectionLabel(this.selectedExamDirection), resolveEssaySectionLabel(this.selectedEssaySection)]
			if (this.selectedPromptCategory) {
				parts.push(resolvePromptCategoryLabel(this.selectedPromptCategory))
			}
			if (this.selectedSourceYear) {
				parts.push(`${this.selectedSourceYear}`)
			}
			return parts.join(' · ')
		}
	},
	methods: {
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
		handleEssaySectionChange(code: string): void {
			if (code === 'small' || code === 'big') {
				this.selectedEssaySection = code
				this.selectedPromptCategory = ''
			}
		},
		handleExamDirectionPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.examDirectionOptions[index]
			if (option?.code) {
				this.selectedExamDirection = option.code
			}
		},
		handleEssaySectionPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.essaySectionOptions[index]
			this.handleEssaySectionChange(option?.code || '')
		},
		handlePromptCategoryPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.promptCategoryOptions[index]
			this.selectedPromptCategory = option?.code || ''
		},
		handleYearPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.yearOptions[index]
			this.selectedSourceYear = typeof option?.value === 'number' ? option.value : null
		},
		openSectionList(): void {
			const query = [
				`examDirection=${encodeURIComponent(this.selectedExamDirection)}`,
				`essaySection=${encodeURIComponent(this.selectedEssaySection)}`
			]
			if (this.selectedPromptCategory) {
				query.push(`promptCategory=${encodeURIComponent(this.selectedPromptCategory)}`)
			}
			if (this.selectedSourceYear) {
				query.push(`sourceYear=${this.selectedSourceYear}`)
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
.kyyy-composition-home__inner {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding-top: 6rpx;
}

.kyyy-composition-home__section,
.kyyy-composition-home__state-card,
.kyyy-composition-home__record-card {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-composition-home__section {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 22rpx;
}

.kyyy-composition-home__section-title {
	font-size: 32rpx;
	line-height: 1.32;
	font-weight: 700;
	color: #273138;
}

.kyyy-composition-home__toolbar {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.kyyy-composition-home__toolbar-row {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 10rpx;
}

.kyyy-composition-home__select {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding: 18rpx 20rpx;
	border-radius: 18rpx;
	background: #f4f7fa;
}

.kyyy-composition-home__select-label {
	font-size: 24rpx;
	color: #6b7785;
}

.kyyy-composition-home__select-value,
.kyyy-composition-home__summary-title,
.kyyy-composition-home__record-title {
	font-size: 26rpx;
	font-weight: 700;
	color: #24313c;
}

.kyyy-composition-home__summary-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding-top: 2rpx;
}

.kyyy-composition-home__summary-copy {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	flex: 1;
	min-width: 0;
}

.kyyy-composition-home__entry-button {
	flex-shrink: 0;
	padding: 14rpx 20rpx;
	border-radius: 18rpx;
	background: #59647d;
	font-size: 24rpx;
	font-weight: 600;
	color: #ffffff;
}

.kyyy-composition-home__records-panel {
	padding: 6rpx;
	border-radius: 20rpx;
	background: #f6f8fb;
}

.kyyy-composition-home__record-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.kyyy-composition-home__record-card {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	padding: 18rpx 18rpx 16rpx;
	box-shadow: none;
}

.kyyy-composition-home__record-meta,
.kyyy-composition-home__state-text {
	font-size: 24rpx;
	line-height: 1.6;
	color: #687480;
}

.kyyy-composition-home__state-card {
	padding: 32rpx;
	text-align: center;
}
</style>
