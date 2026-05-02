<template>
	<!-- AI 索引: KYYY 英语首页。 -->
	<page-shell
		root-class="kyyy-home-page theme-page"
		content-style="padding: 0 24rpx 24rpx;"
	>
		<view class="kyyy-home-page__ambient kyyy-home-page__ambient--left"></view>
		<view class="kyyy-home-page__ambient kyyy-home-page__ambient--right"></view>

		<view class="kyyy-home-page__inner">
			<view class="kyyy-home-page__search-box">
				<uni-icons type="search" size="18" color="#758193" />
				<input
					v-model="keyword"
					class="kyyy-home-page__search-input"
					placeholder="搜索单词（查词功能开发中）"
					placeholder-class="kyyy-home-page__search-placeholder"
					confirm-type="search"
					@confirm="handleSearchConfirm"
				/>
				<view v-if="keyword" class="kyyy-home-page__search-clear" @tap="handleSearchClear">
					<text class="kyyy-home-page__search-clear-text">×</text>
				</view>
			</view>

			<view class="kyyy-home-page__daily-wording">
				<view class="kyyy-home-page__daily-word-row">
					<text class="kyyy-home-page__daily-word">{{ dailyWord.wordText }}</text>
					<text v-if="dailyWordPhonetic" class="kyyy-home-page__daily-phonetic">{{ dailyWordPhonetic }}</text>
				</view>
				<view class="kyyy-home-page__daily-meaning-list">
					<text
						v-for="(line, index) in dailyWordMeaningLines"
						:key="`${dailyWord.wordText}-${index}`"
						class="kyyy-home-page__daily-meaning-item"
					>
						{{ line }}
					</text>
				</view>
			</view>

			<view class="kyyy-home-page__entry-grid">
				<view class="kyyy-home-page__entry-card kyyy-home-page__entry-card--study" @tap="openPracticeMode('study')">
					<text class="kyyy-home-page__entry-label">学习</text>
					<text class="kyyy-home-page__entry-count">待学 {{ dashboard.studyCount }} 单词</text>
				</view>
				<view class="kyyy-home-page__entry-card kyyy-home-page__entry-card--review" @tap="openPracticeMode('review')">
					<text class="kyyy-home-page__entry-label">复习</text>
					<text class="kyyy-home-page__entry-count">待复习 {{ dashboard.reviewCount }} 单词</text>
				</view>
			</view>

			<view class="kyyy-home-page__shortcut-section">
				<view class="kyyy-home-page__shortcut-title-row">
					<view class="kyyy-home-page__shortcut-title-mark">
						<view class="kyyy-home-page__shortcut-title-bar kyyy-home-page__shortcut-title-bar--left"></view>
						<view class="kyyy-home-page__shortcut-title-bar kyyy-home-page__shortcut-title-bar--right"></view>
					</view>
					<text class="kyyy-home-page__shortcut-title">专项入口</text>
				</view>
				<view class="kyyy-home-page__shortcut-grid">
					<view
						v-for="item in shortcutItems"
						:key="item.key"
						class="kyyy-home-page__shortcut-item"
						@tap="openShortcut(item)"
					>
						<view class="kyyy-home-page__shortcut-icon" :style="{ background: item.iconBackground }">
							<uni-icons :type="item.icon" size="18" :color="item.iconColor" />
						</view>
						<view class="kyyy-home-page__shortcut-copy">
							<text class="kyyy-home-page__shortcut-label">{{ item.title }}</text>
							<text class="kyyy-home-page__shortcut-desc">{{ item.description }}</text>
						</view>
						<view class="kyyy-home-page__shortcut-arrow">
							<uni-icons type="right" size="13" color="#8e98aa" />
						</view>
					</view>
				</view>
			</view>
		</view>

		<template #tabbar>
			<kyyy-tabbar current="home" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getHomeDailyWord, getHomeDashboard } from '@/pages/kyyy/api/home'
import { cacheTodayDailyWord, readTodayCachedDailyWord } from '@/pages/kyyy/home/daily-word'
import type {
	KyyyHomeDashboardState,
	KyyyHomeDailyWordState,
	KyyyHomeShortcutItem,
	KyyyPracticeEntryMode
} from '@/pages/kyyy/home/types'
import {
	createEmptyHomeDashboard,
	createHelloDailyWord,
	createPendingDailyWord,
	normalizeDailyWord,
	normalizeHomeDashboard
} from '@/pages/kyyy/home/view'

const MAX_DAILY_WORD_MEANING_LINES = 3
const FALLBACK_DAILY_WORD = createHelloDailyWord()

function trimDailyWordLines(lines: string[]): string[] {
	return lines
		.map((line) => line.trim())
		.filter(Boolean)
		.slice(0, MAX_DAILY_WORD_MEANING_LINES)
}

function formatDailyWordPhonetic(word: KyyyHomeDailyWordState): string {
	const phonetic = (word.phoneticUk || word.phoneticUs || '').trim()
	if (!phonetic) {
		return ''
	}
	return phonetic.startsWith('[') ? phonetic : `[${phonetic}]`
}

function buildDailyWordMeaningLines(word: KyyyHomeDailyWordState): string[] {
	const meaningText = (word.meaningCn || '').trim()
	if (!meaningText) {
		if (word.wordText === FALLBACK_DAILY_WORD.wordText) {
			return trimDailyWordLines(FALLBACK_DAILY_WORD.meaningCn.split(/\n+/))
		}
		return [word.partOfSpeech ? `${word.partOfSpeech} 释义暂缺` : '释义暂缺']
	}

	const newlineLines = trimDailyWordLines(meaningText.split(/\n+/))
	if (newlineLines.length > 1) {
		return newlineLines
	}

	const numberedLines = trimDailyWordLines(meaningText.match(/\d+\.\s*.*?(?=(?:\d+\.\s*)|$)/g) || [])
	if (numberedLines.length > 1) {
		return numberedLines
	}

	const segmentedLines = trimDailyWordLines(meaningText.split(/[；;]\s*/))
	if (segmentedLines.length > 1) {
		return segmentedLines.map((line, index) => {
			const prefix = word.partOfSpeech ? `${word.partOfSpeech} ` : ''
			return `${index + 1}. ${prefix}${line}`.trim()
		})
	}

	const singleLine = `${word.partOfSpeech ? `${word.partOfSpeech} ` : ''}${meaningText}`.trim()
	return [singleLine || '释义暂缺']
}

interface HomePageState {
	keyword: string
	dashboard: KyyyHomeDashboardState
	dailyWord: KyyyHomeDailyWordState
	loadingDashboard: boolean
	loadingDailyWord: boolean
	shortcutItems: KyyyHomeShortcutItem[]
}

export default defineComponent({
	name: 'KyyyHomePage',
	components: {
		PageShell,
		KyyyTabbar
	},
	data(): HomePageState {
		return {
			keyword: '',
			dashboard: createEmptyHomeDashboard(),
			dailyWord: createPendingDailyWord(),
			loadingDashboard: false,
			loadingDailyWord: false,
			shortcutItems: [
				{
					key: 'wrong-word',
					title: '错词本',
					description: '易混词回看',
					icon: 'help-filled',
					iconColor: '#49627c',
					iconBackground: 'linear-gradient(135deg, rgba(225, 233, 247, 0.98), rgba(199, 212, 234, 0.95))',
					path: '/pages/kyyy/wrong-word/index',
					navigationType: 'navigateTo'
				},
				{
					key: 'wrong-question',
					title: '错题本',
					description: '阅读错题沉淀',
					icon: 'compose',
					iconColor: '#8b6235',
					iconBackground: 'linear-gradient(135deg, rgba(247, 236, 220, 0.98), rgba(232, 215, 194, 0.95))',
					path: '/pages/kyyy/wrong-question/index',
					navigationType: 'navigateTo'
				},
				{
					key: 'leaderboard',
					title: '排行榜',
					description: '查看阶段位置',
					icon: 'medal-filled',
					iconColor: '#476455',
					iconBackground: 'linear-gradient(135deg, rgba(226, 239, 232, 0.98), rgba(202, 224, 210, 0.95))',
					path: '/pages/kyyy/leaderboard/index',
					navigationType: 'navigateTo'
				},
				{
					key: 'ai-practice',
					title: 'AI练习',
					description: '智能练习位',
					icon: 'chatboxes-filled',
					iconColor: '#5a5f7f',
					iconBackground: 'linear-gradient(135deg, rgba(231, 233, 246, 0.98), rgba(212, 217, 238, 0.95))',
					path: '/pages/kyyy/ai-practice/index',
					navigationType: 'navigateTo'
				}
			]
		}
	},
	computed: {
		dailyWordPhonetic(): string {
			return formatDailyWordPhonetic(this.dailyWord)
		},
		dailyWordMeaningLines(): string[] {
			return buildDailyWordMeaningLines(this.dailyWord)
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		handleSearchConfirm(): void {
			uni.showToast({
				title: '查词功能开发中',
				icon: 'none'
			})
		},
		handleSearchClear(): void {
			this.keyword = ''
		},
		async bootstrapAndLoad(): Promise<void> {
			this.applyCachedDailyWord()
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-home] bootstrap auth failed', error)
			}
			await Promise.allSettled([
				this.loadDashboard(),
				this.loadDailyWord()
			])
		},
		applyCachedDailyWord(): void {
			const cachedDailyWord = readTodayCachedDailyWord()
			if (cachedDailyWord) {
				this.dailyWord = cachedDailyWord
			}
		},
		async loadDashboard(): Promise<void> {
			if (this.loadingDashboard) {
				return
			}
			this.loadingDashboard = true
			try {
				this.dashboard = normalizeHomeDashboard(await getHomeDashboard())
			} catch (error) {
				console.warn('[kyyy-home] load dashboard failed', error)
				this.dashboard = {
					...this.dashboard,
					loaded: true
				}
			} finally {
				this.loadingDashboard = false
			}
		},
		async loadDailyWord(): Promise<void> {
			if (readTodayCachedDailyWord() || this.loadingDailyWord) {
				return
			}
			this.loadingDailyWord = true
			try {
				const word = normalizeDailyWord(await getHomeDailyWord())
				this.dailyWord = word
				cacheTodayDailyWord(word)
			} catch (error) {
				console.warn('[kyyy-home] load daily word failed', error)
				const fallbackWord = createHelloDailyWord()
				this.dailyWord = fallbackWord
				cacheTodayDailyWord(fallbackWord)
			} finally {
				this.loadingDailyWord = false
			}
		},
		openPracticeMode(mode: KyyyPracticeEntryMode): void {
			uni.reLaunch({
				url: `/pages/kyyy/practice/index?mode=${mode}`,
				fail: (error: unknown) => {
					console.warn('[kyyy-home] reLaunch practice failed', error)
					uni.showToast({
						title: '打开练习失败',
						icon: 'none'
					})
				}
			})
		},
		openShortcut(item: KyyyHomeShortcutItem): void {
			if (item.navigationType === 'reLaunch') {
				uni.reLaunch({
					url: item.path
				})
				return
			}
			uni.navigateTo({
				url: item.path,
				fail: (error: unknown) => {
					console.warn('[kyyy-home] open shortcut failed', error)
					uni.showToast({
						title: `打开${item.title}失败`,
						icon: 'none'
					})
				}
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-home-page {
	position: relative;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.kyyy-home-page__ambient {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	pointer-events: none;
}

.kyyy-home-page__ambient--left {
	top: 92rpx;
	left: -92rpx;
	width: 260rpx;
	height: 260rpx;
	background: radial-gradient(circle, rgba(215, 226, 255, 0.88) 0%, rgba(215, 226, 255, 0) 72%);
}

.kyyy-home-page__ambient--right {
	top: 430rpx;
	right: -96rpx;
	width: 300rpx;
	height: 300rpx;
	background: radial-gradient(circle, rgba(244, 233, 219, 0.84) 0%, rgba(244, 233, 219, 0) 72%);
}

.kyyy-home-page__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 24rpx;
	min-height: 0;
	padding-top: 16rpx;
}

.kyyy-home-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	min-height: 78rpx;
	padding: 0 26rpx;
	border-radius: 30rpx;
	background: linear-gradient(180deg, rgba(247, 249, 252, 0.98) 0%, rgba(242, 245, 249, 0.96) 100%);
	box-shadow:
		0 12rpx 28rpx rgba(71, 86, 105, 0.05),
		inset 0 0 0 1rpx rgba(210, 219, 229, 0.82);
	box-sizing: border-box;
}

.kyyy-home-page__search-input {
	flex: 1;
	min-width: 0;
	font-size: 27rpx;
	line-height: 1.4;
	font-weight: 500;
	color: #2c3640;
}

.kyyy-home-page__search-placeholder {
	color: #96a2b0;
}

.kyyy-home-page__search-clear {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 34rpx;
	height: 34rpx;
	border-radius: 50%;
	background: rgba(219, 226, 233, 0.9);
	box-shadow: inset 0 0 0 1rpx rgba(205, 214, 224, 0.88);
}

.kyyy-home-page__search-clear-text {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #667588;
}

.kyyy-home-page__daily-wording {
	display: flex;
	flex: 1;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 14rpx;
	min-height: 0;
	margin-top: 68rpx;
	min-height: 520rpx;
	padding: 136rpx 16rpx 126rpx;
	box-sizing: border-box;
	text-align: center;
}

.kyyy-home-page__daily-word-row {
	display: flex;
	flex-wrap: wrap;
	align-items: baseline;
	justify-content: center;
	gap: 14rpx;
}

.kyyy-home-page__daily-word {
	font-size: 64rpx;
	line-height: 1.02;
	font-weight: 700;
	letter-spacing: -0.03em;
	color: #29343a;
}

.kyyy-home-page__daily-phonetic {
	font-size: 24rpx;
	line-height: 1.4;
	font-weight: 600;
	color: #72808e;
}

.kyyy-home-page__daily-meaning-list {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.kyyy-home-page__daily-meaning-item {
	max-width: 560rpx;
	font-size: 26rpx;
	line-height: 1.6;
	color: #556067;
}

.kyyy-home-page__entry-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 24rpx;
	margin-bottom: 10rpx;
	padding-bottom: 28rpx;
	border-bottom: 1rpx solid rgba(120, 132, 146, 0.18);
}

.kyyy-home-page__entry-card {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	padding: 22rpx 20rpx;
	border-radius: 24rpx;
	box-shadow: 0 14rpx 30rpx rgba(43, 52, 55, 0.07);
}

.kyyy-home-page__entry-card--study {
	background: linear-gradient(135deg, rgba(230, 238, 250, 0.98) 0%, rgba(212, 223, 242, 0.95) 100%);
}

.kyyy-home-page__entry-card--review {
	background: linear-gradient(135deg, rgba(249, 240, 225, 0.98) 0%, rgba(240, 226, 206, 0.95) 100%);
}

.kyyy-home-page__entry-label {
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #263036;
}

.kyyy-home-page__entry-count {
	font-size: 22rpx;
	line-height: 1.5;
	color: #556067;
}

.kyyy-home-page__shortcut-section {
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	padding-top: 8rpx;
}

.kyyy-home-page__shortcut-title-row {
	display: flex;
	align-items: center;
	gap: 14rpx;
}

.kyyy-home-page__shortcut-title-mark {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
}

.kyyy-home-page__shortcut-title-bar {
	width: 10rpx;
	border-radius: 999rpx;
	background: linear-gradient(180deg, #4f6587 0%, #95a8c4 100%);
}

.kyyy-home-page__shortcut-title-bar--left {
	height: 26rpx;
}

.kyyy-home-page__shortcut-title-bar--right {
	height: 20rpx;
}

.kyyy-home-page__shortcut-title {
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #243146;
}

.kyyy-home-page__shortcut-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 2rpx;
}

.kyyy-home-page__shortcut-item {
	position: relative;
	display: flex;
	align-items: center;
	min-height: 124rpx;
	padding: 22rpx 56rpx 22rpx 20rpx;
	border-radius: 28rpx;
	overflow: hidden;
	background: rgba(255, 255, 255, 0.94);
	box-shadow:
		0 14rpx 30rpx rgba(45, 58, 77, 0.055),
		inset 0 0 0 1rpx rgba(214, 222, 234, 0.95);
}

.kyyy-home-page__shortcut-item:active {
	transform: scale(0.98) translateY(2rpx);
	box-shadow:
		0 10rpx 24rpx rgba(43, 52, 55, 0.06),
		inset 0 0 0 1rpx rgba(203, 213, 228, 0.95);
}

.kyyy-home-page__shortcut-icon {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 58rpx;
	height: 58rpx;
	border-radius: 20rpx;
}

.kyyy-home-page__shortcut-copy {
	position: relative;
	z-index: 1;
	display: flex;
	flex: 1;
	flex-direction: column;
	min-width: 0;
	margin-left: 16rpx;
}

.kyyy-home-page__shortcut-label {
	font-size: 26rpx;
	line-height: 1.25;
	font-weight: 700;
	color: #273247;
}

.kyyy-home-page__shortcut-desc {
	margin-top: 8rpx;
	font-size: 20rpx;
	line-height: 1.25;
	font-weight: 500;
	color: #5f6d7f;
	white-space: nowrap;
}

.kyyy-home-page__shortcut-arrow {
	position: absolute;
	right: 18rpx;
	top: 50%;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 24rpx;
	height: 24rpx;
	transform: translateY(-50%);
}

@media screen and (max-width: 375px) {
	.kyyy-home-page__inner {
		gap: 22rpx;
	}

	.kyyy-home-page__search-box {
		min-height: 74rpx;
		padding: 0 22rpx;
	}

	.kyyy-home-page__search-input {
		font-size: 26rpx;
	}

	.kyyy-home-page__daily-wording {
		margin-top: 56rpx;
		min-height: 440rpx;
		padding: 110rpx 8rpx 100rpx;
	}

	.kyyy-home-page__daily-word {
		font-size: 58rpx;
	}

	.kyyy-home-page__daily-meaning-item {
		max-width: 100%;
		font-size: 24rpx;
	}

	.kyyy-home-page__entry-grid {
		gap: 14rpx;
	}

	.kyyy-home-page__entry-card {
		padding: 20rpx 18rpx;
	}

	.kyyy-home-page__shortcut-grid {
		gap: 14rpx;
	}

	.kyyy-home-page__shortcut-item {
		min-height: 116rpx;
		padding: 20rpx 50rpx 20rpx 18rpx;
	}
}
</style>
