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
				<view class="kyyy-home-page__search-leading">
					<uni-icons type="search" size="21" color="#7d889c" />
				</view>
				<input
					v-model="keyword"
					class="kyyy-home-page__search-input"
					placeholder="搜索单词 / 查词"
					placeholder-class="kyyy-home-page__search-placeholder"
					confirm-type="search"
					@confirm="handleSearchConfirm"
				/>
				<view v-if="keyword" class="kyyy-home-page__search-clear" @tap="handleSearchClear">
					<text class="kyyy-home-page__search-clear-text">×</text>
				</view>
				<view v-else class="kyyy-home-page__search-scan" @tap="handleSearchConfirm">
					<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--lt"></view>
					<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--rt"></view>
					<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--lb"></view>
					<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--rb"></view>
				</view>
			</view>

			<view class="kyyy-home-page__daily-section">
				<swiper
					class="kyyy-home-page__daily-swiper"
					:current="dailyWordCurrentIndex"
					:indicator-dots="false"
					:circular="false"
					@change="handleDailyWordChange"
				>
					<swiper-item
						v-for="(item, index) in dailyWords"
						:key="`${item.wordId ?? item.wordText}-${index}`"
					>
						<view class="kyyy-home-page__daily-card">
							<image
								class="kyyy-home-page__daily-bg-image"
								src="/static/kyyy/kyyxindex.png"
								mode="aspectFill"
							/>
							<view class="kyyy-home-page__daily-bg-mask"></view>
							<view class="kyyy-home-page__daily-copy">
								<view class="kyyy-home-page__daily-badge">
									<uni-icons type="star-filled" size="12" color="#ffffff" />
									<text>每日一词</text>
								</view>
								<text class="kyyy-home-page__daily-word">{{ item.wordText }}</text>
								<view v-if="resolveDailyWordPhonetic(item)" class="kyyy-home-page__daily-pronounce">
									<uni-icons type="sound" size="18" color="#3f69bf" />
									<text class="kyyy-home-page__daily-phonetic">{{ resolveDailyWordPhonetic(item) }}</text>
								</view>
								<view class="kyyy-home-page__daily-primary">
									<text v-if="resolveDailyWordPrimaryMeaning(item).partLabel" class="kyyy-home-page__daily-pos">
										{{ resolveDailyWordPrimaryMeaning(item).partLabel }}
									</text>
									<text class="kyyy-home-page__daily-primary-text">
										{{ resolveDailyWordPrimaryMeaning(item).meaningText }}
									</text>
								</view>
								<view class="kyyy-home-page__daily-divider"></view>
								<view class="kyyy-home-page__daily-example">
									<view class="kyyy-home-page__daily-example-icon">
										<uni-icons type="chatboxes-filled" size="15" color="#5e84dc" />
									</view>
									<text class="kyyy-home-page__daily-example-text">
										<text class="kyyy-home-page__daily-example-lead">{{ resolveDailyWordExample(item).lead }}</text>
										{{ resolveDailyWordExample(item).text }}
									</text>
								</view>
							</view>
							<view class="kyyy-home-page__daily-dots">
								<view
									v-for="(_, dotIndex) in dailyWords"
									:key="`${item.wordText}-dot-${dotIndex}`"
									class="kyyy-home-page__daily-dot"
									:class="{ 'kyyy-home-page__daily-dot--active': dotIndex === dailyWordCurrentIndex }"
								></view>
							</view>
						</view>
					</swiper-item>
				</swiper>
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
import { getHomeDailyWords, getHomeDashboard } from '@/pages/kyyy/api/home'
import { cacheTodayDailyWords, readTodayCachedDailyWords } from '@/pages/kyyy/home/daily-word'
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
const DAILY_WORD_SLIDE_COUNT = 3
const FALLBACK_DAILY_WORD = createHelloDailyWord()

function trimDailyWordLines(lines: string[]): string[] {
	return lines
		.map((line) => line.trim())
		.filter(Boolean)
		.slice(0, MAX_DAILY_WORD_MEANING_LINES)
}

function stripMeaningOrderPrefix(line: string): string {
	return line.replace(/^\d+\.\s*/, '').trim()
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

function buildDailyWordPrimaryMeaning(word: KyyyHomeDailyWordState): { partLabel: string; meaningText: string } {
	const firstLine = stripMeaningOrderPrefix(buildDailyWordMeaningLines(word)[0] || '')
	if (!firstLine) {
		return {
			partLabel: '',
			meaningText: '释义暂缺'
		}
	}
	const explicitPart = (word.partOfSpeech || '').trim()
	if (explicitPart) {
		const stripped = firstLine.startsWith(explicitPart) ? firstLine.slice(explicitPart.length).trim() : firstLine
		return {
			partLabel: explicitPart,
			meaningText: stripped || firstLine
		}
	}
	const matchedPart = firstLine.match(/^([A-Za-z]+\.)\s*(.+)$/)
	if (matchedPart) {
		return {
			partLabel: matchedPart[1],
			meaningText: matchedPart[2]
		}
	}
	return {
		partLabel: '',
		meaningText: firstLine
	}
}

function buildDailyWordExample(word: KyyyHomeDailyWordState): { lead: string; text: string } {
	const exampleSentence = (word.exampleSentence || '').trim()
	const exampleTranslation = (word.exampleTranslation || '').trim()
	if (exampleSentence || exampleTranslation) {
		return {
			lead: exampleSentence || `${word.wordText}.`,
			text: exampleTranslation || '继续巩固这个单词的真实用法。'
		}
	}
	if (word.wordText.trim().toLowerCase() === FALLBACK_DAILY_WORD.wordText) {
		return {
			lead: 'Hello!',
			text: '很高兴见到你。'
		}
	}
	const secondaryLine = stripMeaningOrderPrefix(buildDailyWordMeaningLines(word)[1] || '')
	if (secondaryLine) {
		return {
			lead: `${word.wordText}.`,
			text: secondaryLine
		}
	}
	return {
		lead: `${word.wordText}.`,
		text: '继续学习这个单词，巩固它的常见用法。'
	}
}

function ensureDailyWordSlides(words: KyyyHomeDailyWordState[]): KyyyHomeDailyWordState[] {
	const normalizedWords = (Array.isArray(words) ? words : [])
		.map((item) => normalizeDailyWord(item))
		.filter((item) => !!item.wordText)
	if (normalizedWords.length === 0) {
		normalizedWords.push(FALLBACK_DAILY_WORD)
	}
	const slides = [...normalizedWords]
	while (slides.length < DAILY_WORD_SLIDE_COUNT) {
		slides.push(normalizeDailyWord(slides[slides.length % normalizedWords.length] || FALLBACK_DAILY_WORD))
	}
	return slides.slice(0, DAILY_WORD_SLIDE_COUNT)
}

interface HomePageState {
	keyword: string
	dashboard: KyyyHomeDashboardState
	dailyWords: KyyyHomeDailyWordState[]
	dailyWordCurrentIndex: number
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
			dailyWords: ensureDailyWordSlides([createPendingDailyWord()]),
			dailyWordCurrentIndex: 0,
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
		handleDailyWordChange(event: { detail?: { current?: number } } | undefined): void {
			const nextIndex = Number(event?.detail?.current ?? 0)
			this.dailyWordCurrentIndex = Number.isFinite(nextIndex) ? Math.max(nextIndex, 0) : 0
		},
		resolveDailyWordPhonetic(word: KyyyHomeDailyWordState): string {
			return formatDailyWordPhonetic(word)
		},
		resolveDailyWordPrimaryMeaning(word: KyyyHomeDailyWordState): { partLabel: string; meaningText: string } {
			return buildDailyWordPrimaryMeaning(word)
		},
		resolveDailyWordExample(word: KyyyHomeDailyWordState): { lead: string; text: string } {
			return buildDailyWordExample(word)
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
			const cachedDailyWords = readTodayCachedDailyWords()
			if (cachedDailyWords && cachedDailyWords.length > 0) {
				this.dailyWords = ensureDailyWordSlides(cachedDailyWords)
				this.dailyWordCurrentIndex = 0
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
			if (readTodayCachedDailyWords()?.length || this.loadingDailyWord) {
				return
			}
			this.loadingDailyWord = true
			try {
				const words = ensureDailyWordSlides(
					(await getHomeDailyWords()).map((item) => normalizeDailyWord(item))
				)
				this.dailyWords = words
				this.dailyWordCurrentIndex = 0
				cacheTodayDailyWords(words)
			} catch (error) {
				console.warn('[kyyy-home] load daily word failed', error)
				const fallbackWords = ensureDailyWordSlides([createHelloDailyWord()])
				this.dailyWords = fallbackWords
				this.dailyWordCurrentIndex = 0
				cacheTodayDailyWords(fallbackWords)
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
	gap: 18rpx;
	min-height: 92rpx;
	padding: 0 28rpx;
	border-radius: 34rpx;
	background: rgba(255, 255, 255, 0.96);
	box-shadow:
		0 18rpx 38rpx rgba(75, 90, 112, 0.06),
		inset 0 0 0 1rpx rgba(220, 227, 237, 0.88);
	box-sizing: border-box;
}

.kyyy-home-page__search-leading {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40rpx;
	height: 40rpx;
	flex: 0 0 40rpx;
}

.kyyy-home-page__search-input {
	flex: 1;
	min-width: 0;
	font-size: 31rpx;
	line-height: 1.4;
	font-weight: 500;
	color: #273247;
}

.kyyy-home-page__search-placeholder {
	color: #9aa6b6;
}

.kyyy-home-page__search-clear {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: rgba(219, 226, 233, 0.84);
	box-shadow: inset 0 0 0 1rpx rgba(205, 214, 224, 0.88);
}

.kyyy-home-page__search-clear-text {
	font-size: 24rpx;
	line-height: 1;
	font-weight: 700;
	color: #667588;
}

.kyyy-home-page__search-scan {
	position: relative;
	width: 40rpx;
	height: 40rpx;
	flex: 0 0 40rpx;
}

.kyyy-home-page__search-scan-corner {
	position: absolute;
	width: 14rpx;
	height: 14rpx;
	border-color: #7d889c;
	border-style: solid;
}

.kyyy-home-page__search-scan-corner--lt {
	top: 4rpx;
	left: 4rpx;
	border-width: 4rpx 0 0 4rpx;
	border-top-left-radius: 6rpx;
}

.kyyy-home-page__search-scan-corner--rt {
	top: 4rpx;
	right: 4rpx;
	border-width: 4rpx 4rpx 0 0;
	border-top-right-radius: 6rpx;
}

.kyyy-home-page__search-scan-corner--lb {
	left: 4rpx;
	bottom: 4rpx;
	border-width: 0 0 4rpx 4rpx;
	border-bottom-left-radius: 6rpx;
}

.kyyy-home-page__search-scan-corner--rb {
	right: 4rpx;
	bottom: 4rpx;
	border-width: 0 4rpx 4rpx 0;
	border-bottom-right-radius: 6rpx;
}

.kyyy-home-page__daily-section {
	width: 100%;
	margin-top: 24rpx;
}

.kyyy-home-page__daily-swiper {
	width: 100%;
	height: 520rpx;
}

.kyyy-home-page__daily-card {
	position: relative;
	display: flex;
	width: 100%;
	height: 100%;
	padding: 30rpx 32rpx 84rpx;
	border-radius: 36rpx;
	overflow: hidden;
	box-sizing: border-box;
	background: linear-gradient(135deg, rgba(250, 251, 255, 0.99) 0%, rgba(240, 246, 255, 0.98) 58%, rgba(234, 240, 252, 0.96) 100%);
	box-shadow:
		0 22rpx 48rpx rgba(62, 79, 104, 0.07),
		inset 0 0 0 1rpx rgba(208, 220, 239, 0.82);
}

.kyyy-home-page__daily-bg-image {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 100%;
}

.kyyy-home-page__daily-bg-mask {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	background: linear-gradient(90deg, rgba(249, 251, 255, 0.96) 0%, rgba(246, 249, 255, 0.9) 36%, rgba(241, 246, 255, 0.44) 72%, rgba(236, 242, 252, 0.16) 100%);
}

.kyyy-home-page__daily-copy {
	position: relative;
	z-index: 2;
	display: flex;
	flex-direction: column;
	width: calc(100% - 220rpx);
	min-width: 0;
	max-width: 420rpx;
}

.kyyy-home-page__daily-badge {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	align-self: flex-start;
	padding: 12rpx 20rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #9ab5ff 0%, #7e9cff 100%);
	box-shadow: 0 12rpx 22rpx rgba(91, 124, 206, 0.16);
	font-size: 24rpx;
	line-height: 1;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-home-page__daily-word {
	margin-top: 38rpx;
	font-family: 'Baskerville', 'Times New Roman', serif;
	font-size: 98rpx;
	line-height: 0.95;
	font-weight: 700;
	letter-spacing: -0.05em;
	color: #17315d;
	word-break: break-word;
}

.kyyy-home-page__daily-pronounce {
	display: inline-flex;
	align-items: center;
	gap: 12rpx;
	margin-top: 18rpx;
}

.kyyy-home-page__daily-phonetic {
	font-size: 38rpx;
	line-height: 1.2;
	font-weight: 500;
	color: #7481a4;
}

.kyyy-home-page__daily-primary {
	display: flex;
	flex-wrap: wrap;
	align-items: baseline;
	gap: 10rpx 14rpx;
	margin-top: 28rpx;
}

.kyyy-home-page__daily-pos {
	font-size: 28rpx;
	line-height: 1.35;
	font-weight: 700;
	color: #386af2;
}

.kyyy-home-page__daily-primary-text {
	font-size: 28rpx;
	line-height: 1.5;
	font-weight: 600;
	color: #273247;
}

.kyyy-home-page__daily-divider {
	width: min(100%, 360rpx);
	height: 2rpx;
	margin-top: 28rpx;
	background: linear-gradient(90deg, rgba(201, 210, 227, 0.8) 0%, rgba(201, 210, 227, 0.18) 100%);
}

.kyyy-home-page__daily-example {
	display: flex;
	align-items: flex-start;
	gap: 14rpx;
	margin-top: 28rpx;
	max-width: 380rpx;
}

.kyyy-home-page__daily-example-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 34rpx;
	height: 34rpx;
	flex: 0 0 34rpx;
	margin-top: 2rpx;
}

.kyyy-home-page__daily-example-text {
	font-size: 28rpx;
	line-height: 1.55;
	color: #4f5d72;
}

.kyyy-home-page__daily-example-lead {
	margin-right: 8rpx;
	font-weight: 700;
	color: #416fe0;
}

.kyyy-home-page__daily-dots {
	position: absolute;
	left: 50%;
	bottom: 24rpx;
	z-index: 2;
	display: flex;
	align-items: center;
	gap: 12rpx;
	transform: translateX(-50%);
}

.kyyy-home-page__daily-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	background: rgba(193, 202, 220, 0.9);
}

.kyyy-home-page__daily-dot--active {
	width: 24rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #4c7cf0 0%, #75a0ff 100%);
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
	position: relative;
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	gap: 10rpx;
	min-height: 122rpx;
	padding: 24rpx 20rpx 22rpx;
	border-radius: 28rpx;
	overflow: hidden;
	box-sizing: border-box;
	box-shadow: 0 16rpx 34rpx rgba(43, 52, 55, 0.06);
}

.kyyy-home-page__entry-card::before,
.kyyy-home-page__entry-card::after {
	content: '';
	position: absolute;
	border-radius: 999rpx;
	pointer-events: none;
}

.kyyy-home-page__entry-card::before {
	top: -40rpx;
	right: -52rpx;
	width: 190rpx;
	height: 190rpx;
	background: rgba(255, 255, 255, 0.28);
}

.kyyy-home-page__entry-card::after {
	left: -58rpx;
	bottom: -68rpx;
	width: 170rpx;
	height: 170rpx;
	background: rgba(255, 255, 255, 0.22);
}

.kyyy-home-page__entry-card--study {
	background:
		radial-gradient(circle at 22% 86%, rgba(255, 255, 255, 0.3) 0, rgba(255, 255, 255, 0.3) 72rpx, transparent 74rpx),
		linear-gradient(135deg, rgba(205, 240, 255, 0.96) 0%, rgba(197, 221, 255, 0.96) 46%, rgba(183, 202, 247, 0.94) 100%);
}

.kyyy-home-page__entry-card--review {
	background:
		radial-gradient(circle at 20% 84%, rgba(255, 255, 255, 0.34) 0, rgba(255, 255, 255, 0.34) 74rpx, transparent 76rpx),
		linear-gradient(135deg, rgba(255, 239, 219, 0.97) 0%, rgba(255, 226, 205, 0.96) 52%, rgba(248, 208, 190, 0.94) 100%);
}

.kyyy-home-page__entry-card--study .kyyy-home-page__entry-label,
.kyyy-home-page__entry-card--study .kyyy-home-page__entry-count,
.kyyy-home-page__entry-card--review .kyyy-home-page__entry-label,
.kyyy-home-page__entry-card--review .kyyy-home-page__entry-count {
	position: relative;
	z-index: 1;
}

.kyyy-home-page__entry-card--study .kyyy-home-page__entry-label::after,
.kyyy-home-page__entry-card--review .kyyy-home-page__entry-label::after {
	content: '';
	position: absolute;
	top: -30rpx;
	right: 0;
	width: 44rpx;
	height: 44rpx;
	border-radius: 12rpx;
	opacity: 0.68;
}

.kyyy-home-page__entry-card--study .kyyy-home-page__entry-label::after {
	background:
		linear-gradient(135deg, rgba(136, 170, 223, 0.3), rgba(136, 170, 223, 0.18)),
		linear-gradient(45deg, transparent 0 34%, rgba(103, 133, 184, 0.55) 35% 72%, transparent 73% 100%);
}

.kyyy-home-page__entry-card--review .kyyy-home-page__entry-label::after {
	background:
		linear-gradient(135deg, rgba(229, 177, 145, 0.28), rgba(229, 177, 145, 0.18)),
		repeating-linear-gradient(
			180deg,
			transparent 0 10rpx,
			rgba(173, 119, 87, 0.5) 10rpx 14rpx,
			transparent 14rpx 18rpx
		);
}

.kyyy-home-page__entry-label {
	position: relative;
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
		gap: 14rpx;
		min-height: 82rpx;
		padding: 0 22rpx;
	}

	.kyyy-home-page__search-leading {
		width: 36rpx;
		height: 36rpx;
		flex-basis: 36rpx;
	}

	.kyyy-home-page__search-input {
		font-size: 28rpx;
	}

	.kyyy-home-page__daily-card {
		padding: 26rpx 24rpx 72rpx;
	}

	.kyyy-home-page__daily-swiper {
		height: 472rpx;
	}

	.kyyy-home-page__daily-word {
		margin-top: 32rpx;
		font-size: 78rpx;
	}

	.kyyy-home-page__daily-copy {
		width: calc(100% - 170rpx);
		max-width: none;
	}

	.kyyy-home-page__daily-phonetic {
		font-size: 30rpx;
	}

	.kyyy-home-page__daily-primary-text,
	.kyyy-home-page__daily-example-text {
		font-size: 24rpx;
	}

	.kyyy-home-page__entry-grid {
		gap: 14rpx;
	}

	.kyyy-home-page__entry-card {
		min-height: 114rpx;
		padding: 20rpx 18rpx 18rpx;
	}

	.kyyy-home-page__entry-card::before {
		top: -34rpx;
		right: -44rpx;
		width: 168rpx;
		height: 168rpx;
	}

	.kyyy-home-page__entry-card::after {
		left: -48rpx;
		bottom: -56rpx;
		width: 150rpx;
		height: 150rpx;
	}

	.kyyy-home-page__entry-label::after {
		top: -26rpx;
		width: 40rpx;
		height: 40rpx;
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
