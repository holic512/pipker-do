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
			<view class="kyyy-translation-home__section">
				<text class="kyyy-translation-home__section-title">英语翻译知识库</text>
				<view class="kyyy-translation-home__toolbar">
					<view class="kyyy-translation-home__toolbar-row">
						<picker
							mode="selector"
							:range="examDirectionOptions"
							range-key="label"
							:value="selectedExamDirectionIndex"
							@change="handleExamDirectionPickerChange"
						>
							<view class="kyyy-translation-home__select kyyy-translation-home__select--half">
								<text class="kyyy-translation-home__select-label">方向</text>
								<text class="kyyy-translation-home__select-value">{{ selectedExamDirectionLabel }}</text>
							</view>
						</picker>
						<picker
							mode="selector"
							:range="yearOptions"
							range-key="label"
							:value="selectedYearIndex"
							@change="handleYearPickerChange"
						>
							<view class="kyyy-translation-home__select kyyy-translation-home__select--half">
								<text class="kyyy-translation-home__select-label">年份</text>
								<text class="kyyy-translation-home__select-value">{{ selectedYearLabel }}</text>
							</view>
						</picker>
					</view>
					<picker
						mode="selector"
						:range="translationModeOptions"
						range-key="label"
						:value="selectedTranslationModeIndex"
						@change="handleTranslationModePickerChange"
					>
						<view class="kyyy-translation-home__select">
							<text class="kyyy-translation-home__select-label">类别</text>
							<text class="kyyy-translation-home__select-value">{{ selectedTranslationModeLabel }}</text>
						</view>
					</picker>
				</view>
				<view class="kyyy-translation-home__summary-row">
					<view class="kyyy-translation-home__summary-copy">
						<text class="kyyy-translation-home__summary-title">{{ currentSelectionTitle }}</text>
					</view>
					<view class="kyyy-translation-home__entry-button" @tap="openList()">进入列表</view>
				</view>
			</view>

			<view class="kyyy-translation-home__section">
				<view v-if="loading && !overviewState.loaded" class="kyyy-translation-home__state-card">
					<text class="kyyy-translation-home__state-text">正在整理翻译知识库...</text>
				</view>
				<view v-else-if="overviewState.featuredRecords.length" class="kyyy-translation-home__records-panel">
					<view class="kyyy-translation-home__record-list">
					<view
						v-for="item in overviewState.featuredRecords"
						:key="item.id"
						class="kyyy-translation-home__record-card"
						@tap="openDetail(item.id)"
					>
						<text class="kyyy-translation-home__record-title">{{ item.sourceTitle }}</text>
						<text class="kyyy-translation-home__record-meta">{{ buildTranslationMetaText(item) }}</text>
					</view>
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
	selectedSourceYear: number | null
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
			selectedTranslationMode: 'segmented',
			selectedSourceYear: null
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		totalCount(): number {
			return this.overviewState.examDirections.reduce((sum, item) => sum + item.count, 0)
		},
		examDirectionOptions(): Array<{ code: string; label: string }> {
			return this.overviewState.examDirections.map((item) => ({
				code: item.code,
				label: item.label
			}))
		},
		translationModeOptions(): Array<{ code: string; label: string }> {
			return this.overviewState.translationModes.map((item) => ({
				code: item.code,
				label: item.label
			}))
		},
		yearOptions(): Array<{ value: number | null; label: string }> {
			return [
				{
					value: null,
					label: '全部年份'
				},
				...this.overviewState.recentYears.map((year) => ({
					value: year,
					label: `${year}`
				}))
			]
		},
		selectedExamDirectionIndex(): number {
			const index = this.examDirectionOptions.findIndex((item) => item.code === this.selectedExamDirection)
			return index >= 0 ? index : 0
		},
		selectedTranslationModeIndex(): number {
			const index = this.translationModeOptions.findIndex((item) => item.code === this.selectedTranslationMode)
			return index >= 0 ? index : 0
		},
		selectedYearIndex(): number {
			const index = this.yearOptions.findIndex((item) => item.value === this.selectedSourceYear)
			return index >= 0 ? index : 0
		},
		selectedExamDirectionLabel(): string {
			return resolveExamDirectionLabel(this.selectedExamDirection)
		},
		selectedTranslationModeLabel(): string {
			return resolveTranslationModeLabel(this.selectedTranslationMode)
		},
		selectedYearLabel(): string {
			return this.selectedSourceYear ? `${this.selectedSourceYear}` : '全部年份'
		},
		currentSelectionTitle(): string {
			const parts = [
				resolveExamDirectionLabel(this.selectedExamDirection),
				resolveTranslationModeLabel(this.selectedTranslationMode)
			]
			if (this.selectedSourceYear) {
				parts.push(`${this.selectedSourceYear}`)
			}
			return parts.join(' · ')
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
		handleExamDirectionPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.examDirectionOptions[index]
			if (option?.code) {
				this.handleSelectExamDirection(option.code)
			}
		},
		handleTranslationModePickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.translationModeOptions[index]
			if (option?.code === 'segmented' || option?.code === 'passage') {
				this.handleSelectTranslationMode(option.code)
			}
		},
		handleYearPickerChange(event: { detail?: { value?: number | string } }): void {
			const index = Number(event.detail?.value)
			const option = this.yearOptions[index]
			this.selectedSourceYear = typeof option?.value === 'number' ? option.value : null
		},
		openList(sourceYear?: number): void {
			const query = [
				`examDirection=${encodeURIComponent(this.selectedExamDirection)}`,
				`translationMode=${encodeURIComponent(this.selectedTranslationMode)}`
			]
			const targetYear = typeof sourceYear === 'number' ? sourceYear : this.selectedSourceYear
			if (targetYear) {
				query.push(`sourceYear=${targetYear}`)
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
	gap: 16rpx;
	padding-top: 6rpx;
}

.kyyy-translation-home__section,
.kyyy-translation-home__state-card,
.kyyy-translation-home__record-card {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-translation-home__record-head {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	align-items: center;
}

.kyyy-translation-home__record-pill {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
}

.kyyy-translation-home__section {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 22rpx;
}

.kyyy-translation-home__section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #23303b;
}
.kyyy-translation-home__record-meta,
.kyyy-translation-home__state-text {
	font-size: 24rpx;
	line-height: 1.7;
	color: #667380;
}

.kyyy-translation-home__toolbar {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.kyyy-translation-home__toolbar-row {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 10rpx;
}

.kyyy-translation-home__select {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding: 18rpx 20rpx;
	border-radius: 18rpx;
	background: #f4f7fa;
}

.kyyy-translation-home__select--half {
	width: 100%;
}

.kyyy-translation-home__select-label {
	font-size: 24rpx;
	color: #6b7785;
}

.kyyy-translation-home__select-value,
.kyyy-translation-home__summary-title {
	font-size: 26rpx;
	font-weight: 700;
	color: #24313c;
}

.kyyy-translation-home__summary-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding-top: 2rpx;
}

.kyyy-translation-home__summary-copy {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	flex: 1;
	min-width: 0;
}

.kyyy-translation-home__entry-button {
	flex-shrink: 0;
	padding: 14rpx 20rpx;
	border-radius: 18rpx;
	background: #4f6375;
	font-size: 24rpx;
	font-weight: 600;
	color: #ffffff;
}

.kyyy-translation-home__record-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.kyyy-translation-home__records-panel {
	padding: 6rpx;
	border-radius: 20rpx;
	background: #f6f8fb;
}

.kyyy-translation-home__record-card {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	padding: 18rpx 18rpx 16rpx;
	box-shadow: none;
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
