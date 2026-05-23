<!--
@file KyyyHomePage
@project pipker-do
@module 考研英语 / 小程序首页
@description 展示考研英语首页搜索、沉浸式查词、单词详情、每日一词、学习入口与专项入口。
@logic 1. 加载首页仪表盘与每日一词；2. 维护每日一词轮播展示；3. 处理聚焦查词动画、结果查询与详情展示；4. 处理学习入口和专项入口跳转。
@dependencies API: @/pages/kyyy/api/home, Component: PageShell, Component: KyyyTabbar
@index_tags 考研英语, 每日一词, 首页, 查词, 词库, 小程序
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-home-page theme-page"
		content-style="padding: 0 24rpx 24rpx;"
	>
		<view class="kyyy-home-page__ambient kyyy-home-page__ambient--left"></view>
		<view class="kyyy-home-page__ambient kyyy-home-page__ambient--right"></view>

		<view
			class="kyyy-home-page__inner"
			:class="{ 'kyyy-home-page__inner--search-active': searchSceneVisible }"
		>
			<view class="kyyy-home-page__search-box" @tap="openSearchScene">
				<view class="kyyy-home-page__search-leading">
					<uni-icons type="search" size="21" color="#7d889c" />
				</view>
				<input
					v-model="keyword"
					class="kyyy-home-page__search-input"
					placeholder="搜索单词 / 查词"
					placeholder-class="kyyy-home-page__search-placeholder"
					confirm-type="search"
					@focus="handleSearchFocus"
					@input="handleSearchInput"
					@confirm="handleSearchConfirm"
				/>
				<view v-if="keyword" class="kyyy-home-page__search-clear" @tap.stop="handleSearchClear">
					<text class="kyyy-home-page__search-clear-text">×</text>
				</view>
				<view v-else class="kyyy-home-page__search-scan" @tap.stop="openSearchScene">
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
								<text class="kyyy-home-page__daily-word" :style="resolveDailyWordWordStyle(item)">{{ item.wordText }}</text>
								<view class="kyyy-home-page__daily-primary">
									<text v-if="item.partOfSpeech" class="kyyy-home-page__daily-pos">{{ item.partOfSpeech }}</text>
									<text class="kyyy-home-page__daily-primary-text">{{ resolveDailyWordMeaning(item) }}</text>
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
					<text class="kyyy-home-page__entry-bank">{{ resolveDefaultWordBankText() }}</text>
					<text class="kyyy-home-page__entry-count">待学 {{ dashboard.studyCount }} 单词</text>
				</view>
				<view class="kyyy-home-page__entry-card kyyy-home-page__entry-card--review" @tap="openPracticeMode('review')">
					<text class="kyyy-home-page__entry-label">复习</text>
					<text class="kyyy-home-page__entry-bank">{{ resolveDefaultWordBankText() }}</text>
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

		<view
			v-if="searchSceneRendered"
			class="kyyy-home-page__search-stage"
			:class="{ 'is-visible': searchSceneVisible }"
			@tap="handleSearchBlankTap"
		>
			<view class="kyyy-home-page__search-dock" :style="searchDockStyle" @tap.stop>
				<view class="kyyy-home-page__search-box kyyy-home-page__search-box--floating">
					<view class="kyyy-home-page__search-leading">
						<uni-icons type="search" size="20" color="#60708a" />
					</view>
					<input
						v-model="keyword"
						:focus="searchInputFocused"
						class="kyyy-home-page__search-input"
						placeholder="搜索单词 / 查词"
						placeholder-class="kyyy-home-page__search-placeholder"
						confirm-type="search"
						@focus="handleSearchFocus"
						@input="handleSearchInput"
						@confirm="handleSearchConfirm"
					/>
					<view v-if="keyword" class="kyyy-home-page__search-clear" @tap.stop="handleSearchClear">
						<text class="kyyy-home-page__search-clear-text">×</text>
					</view>
					<view v-else class="kyyy-home-page__search-scan">
						<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--lt"></view>
						<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--rt"></view>
						<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--lb"></view>
						<view class="kyyy-home-page__search-scan-corner kyyy-home-page__search-scan-corner--rb"></view>
					</view>
				</view>
			</view>

			<scroll-view
				scroll-y
				class="kyyy-home-page__search-results"
				:style="searchResultPanelStyle"
				@tap="handleSearchBlankTap"
			>
				<view class="kyyy-home-page__search-results-inner">
					<view v-if="wordDetailVisible" class="kyyy-home-page__word-detail" @tap.stop>
						<view class="kyyy-home-page__word-detail-toolbar">
							<view class="kyyy-home-page__word-detail-back" @tap.stop="closeWordDetail">
								<uni-icons type="left" size="17" color="#657386" />
								<text>返回</text>
							</view>
							<text v-if="wordDetailLoading" class="kyyy-home-page__word-detail-status">加载中</text>
						</view>

						<view class="kyyy-home-page__word-detail-head">
						<view class="kyyy-home-page__word-detail-title-row">
							<text class="kyyy-home-page__word-detail-word" :style="resolveWordDetailWordStyle()">{{ resolveWordDetailWordText() }}</text>
							<view class="kyyy-home-page__word-detail-star" @tap.stop="handleWordDetailFavoriteToggle">
								<uni-icons :type="resolveFavoriteIcon(resolveWordDetailIsFavorite())" size="28" :color="resolveFavoriteColor(resolveWordDetailIsFavorite())" />
							</view>
						</view>
							<view v-if="resolveWordDetailPhonetic()" class="kyyy-home-page__word-detail-pron">
								<view class="kyyy-home-page__word-detail-pron-badge">
									<text>美</text>
									<uni-icons type="sound" size="12" color="#77818d" />
								</view>
								<text class="kyyy-home-page__word-detail-phonetic">{{ resolveWordDetailPhonetic() }}</text>
							</view>
							<view class="kyyy-home-page__word-detail-meaning">
								<text v-if="resolveWordDetailPartOfSpeech()" class="kyyy-home-page__word-detail-pos">{{ resolveWordDetailPartOfSpeech() }}</text>
								<text class="kyyy-home-page__word-detail-meaning-text">{{ resolveWordDetailMeaning() }}</text>
							</view>
						</view>

						<view class="kyyy-home-page__word-detail-example">
							<view class="kyyy-home-page__word-detail-section-title">
								<view class="kyyy-home-page__word-detail-section-mark"></view>
								<text>例句</text>
							</view>
							<view v-if="wordDetailFailed" class="kyyy-home-page__word-detail-message">
								<text>详情加载失败，已保留列表释义</text>
							</view>
							<view v-else class="kyyy-home-page__word-detail-example-body">
								<view
									v-for="(example, exampleIndex) in resolveWordDetailExamples()"
									:key="`${example.id || example.exampleSentence}-${exampleIndex}`"
									class="kyyy-home-page__word-detail-example-item"
								>
									<text class="kyyy-home-page__word-detail-sentence">{{ example.exampleSentence }}</text>
									<text v-if="example.exampleTranslation" class="kyyy-home-page__word-detail-translation">{{ example.exampleTranslation }}</text>
								</view>
								<view v-if="!resolveWordDetailExamples().length" class="kyyy-home-page__word-detail-example-item">
									<text class="kyyy-home-page__word-detail-sentence">{{ resolveWordDetailExampleSentence() }}</text>
									<text class="kyyy-home-page__word-detail-translation">{{ resolveWordDetailExampleTranslation() }}</text>
								</view>
							</view>
						</view>
					</view>
					<view v-else-if="!normalizedSearchKeyword" class="kyyy-home-page__search-state">
						<text class="kyyy-home-page__search-state-title">输入单词</text>
						<text class="kyyy-home-page__search-state-desc">结果会在下方即时出现</text>
					</view>
					<view v-else-if="searchLoading" class="kyyy-home-page__search-state">
						<view class="kyyy-home-page__search-loader"></view>
						<text class="kyyy-home-page__search-state-title">正在查词</text>
						<text class="kyyy-home-page__search-state-desc">匹配词库里的单词释义</text>
					</view>
					<view v-else-if="searchFailed" class="kyyy-home-page__search-state">
						<text class="kyyy-home-page__search-state-title">查询失败</text>
						<text class="kyyy-home-page__search-state-desc">稍后重新输入试试</text>
					</view>
					<view v-else-if="searchResults.length" class="kyyy-home-page__search-list">
						<view
							v-for="(item, index) in searchResults"
							:key="resolveSearchResultKey(item, index)"
							class="kyyy-home-page__search-item"
							:style="{ animationDelay: `${index * 38}ms` }"
							@tap.stop="openWordDetail(item)"
						>
							<text class="kyyy-home-page__search-item-word">{{ item.wordText }}</text>
							<text class="kyyy-home-page__search-item-summary">{{ resolveSearchSummary(item) }}</text>
							<view class="kyyy-home-page__search-item-star" @tap.stop="handleSearchResultFavoriteToggle(item)">
								<uni-icons :type="resolveFavoriteIcon(item.isFavorite)" size="22" :color="resolveFavoriteColor(item.isFavorite)" />
							</view>
						</view>
					</view>
					<view v-else-if="searchLoaded" class="kyyy-home-page__search-state kyyy-home-page__search-state--empty">
						<text class="kyyy-home-page__search-state-title">没有找到结果</text>
						<text class="kyyy-home-page__search-state-desc">点击空白处返回首页</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<template #tabbar>
			<view
				class="kyyy-home-page__tabbar-wrap"
				:class="{ 'kyyy-home-page__tabbar-wrap--hidden': searchSceneVisible }"
			>
				<kyyy-tabbar current="home" />
			</view>
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { favoriteWord, unfavoriteWord } from '@/pages/kyyy/api/favorite-word'
import { getHomeDailyWords, getHomeDashboard, getHomeWordDetail, searchHomeWords } from '@/pages/kyyy/api/home'
import { cacheDailyWords, readCachedDailyWords } from '@/pages/kyyy/home/daily-word'
import {
	SEARCH_DEBOUNCE_DELAY,
	SEARCH_SCENE_ANIMATION_DURATION,
	createWordDetailFallback,
	normalizeSearchKeyword,
	normalizeWordDetailResult,
	normalizeWordSearchResult,
	resolveSearchMeaning as resolveSearchMeaningText,
	resolveSearchSummary as resolveSearchSummaryText,
	resolveWordDetailExamples as resolveWordDetailExamplesValue,
	resolveWordDetailExampleSentence as resolveWordDetailExampleSentenceText,
	resolveWordDetailExampleTranslation as resolveWordDetailExampleTranslationText,
	resolveWordDetailMeaning as resolveWordDetailMeaningText,
	resolveWordDetailPartOfSpeech as resolveWordDetailPartOfSpeechText,
	resolveWordDetailPhonetic as resolveWordDetailPhoneticText,
	resolveWordDetailWordStyle as resolveWordDetailWordStyleValue,
	resolveWordDetailWordText as resolveWordDetailWordTextValue
} from '@/pages/kyyy/home/search'
import type { KyyyHomeWordDetailState, KyyyHomeWordSearchState, KyyyWordExampleState } from '@/pages/kyyy/home/search'
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

const DAILY_WORD_SLIDE_COUNT = 3
const MAX_DAILY_WORD_LENGTH_FOR_LARGE = 7
const MAX_DAILY_WORD_LENGTH_FOR_MEDIUM = 9
const MAX_DAILY_WORD_LENGTH_FOR_SMALL = 11
const MAX_DAILY_WORD_MEANING_LENGTH = 18
const FALLBACK_DAILY_WORD = createHelloDailyWord()

function trimDisplayText(value: string, maxLength: number): string {
	const normalized = (value || '').trim().replace(/\s+/g, ' ')
	if (!normalized) {
		return ''
	}
	if (normalized.length <= maxLength) {
		return normalized
	}
	const preferredCut = normalized.lastIndexOf(' ', Math.max(maxLength - 1, 0))
	if (preferredCut >= Math.max(Math.floor(maxLength / 2), 4)) {
		return `${normalized.slice(0, preferredCut).trim()}…`
	}
	return `${normalized.slice(0, Math.max(maxLength - 1, 0)).trim()}…`
}

function resolveDailyWordWordStyle(word: KyyyHomeDailyWordState): Record<string, string> {
	const length = (word.wordText || '').trim().length
	if (length > MAX_DAILY_WORD_LENGTH_FOR_SMALL) {
		return {
			fontSize: '64rpx',
			lineHeight: '1.02',
			letterSpacing: '-0.02em'
		}
	}
	if (length > MAX_DAILY_WORD_LENGTH_FOR_MEDIUM) {
		return {
			fontSize: '74rpx',
			lineHeight: '0.98',
			letterSpacing: '-0.03em'
		}
	}
	if (length > MAX_DAILY_WORD_LENGTH_FOR_LARGE) {
		return {
			fontSize: '86rpx',
			lineHeight: '0.96',
			letterSpacing: '-0.04em'
		}
	}
	return {}
}

function resolveDailyWordMeaning(word: KyyyHomeDailyWordState): string {
	const meaning = trimDisplayText((word.meaningCn || '').trim(), MAX_DAILY_WORD_MEANING_LENGTH)
	if (meaning) {
		return meaning
	}
	return word.wordText === FALLBACK_DAILY_WORD.wordText ? FALLBACK_DAILY_WORD.meaningCn : '释义暂缺'
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

function hasValidDailyWordText(item: unknown): item is { wordText: string } {
	const wordText = (item as { wordText?: unknown } | null)?.wordText
	return !!item
		&& typeof item === 'object'
		&& !Array.isArray(item)
		&& typeof wordText === 'string'
		&& wordText.trim().length > 0
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

interface HomePageState {
	keyword: string
	dashboard: KyyyHomeDashboardState
	dailyWords: KyyyHomeDailyWordState[]
	dailyWordCurrentIndex: number
	loadingDashboard: boolean
	loadingDailyWord: boolean
	searchSceneRendered: boolean
	searchSceneVisible: boolean
	searchInputFocused: boolean
	searchResults: KyyyHomeWordSearchState[]
	searchLoading: boolean
	searchLoaded: boolean
	searchFailed: boolean
	searchRequestSeq: number
	searchDebounceTimer: ReturnType<typeof setTimeout> | null
	searchCloseTimer: ReturnType<typeof setTimeout> | null
	wordDetailVisible: boolean
	selectedSearchWord: KyyyHomeWordSearchState | null
	wordDetail: KyyyHomeWordDetailState | null
	wordDetailLoading: boolean
	wordDetailLoaded: boolean
	wordDetailFailed: boolean
	wordDetailRequestSeq: number
	searchDockTop: number
	searchDockHeight: number
	searchDockLeft: number
	searchDockRight: number
	shortcutItems: KyyyHomeShortcutItem[]
	pendingSearchKeyword: string
	pendingWordId: number | null
	favoriteSubmittingWordIds: number[]
}

interface HomePageLoadQuery {
	searchKeyword?: string | null
	wordId?: string | number | null
}

function toPositiveNumber(value: unknown): number | null {
	if (value === null || value === undefined || value === '') {
		return null
	}
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function encodeWordNavigationKeyword(value: string): string {
	return (value || '').trim()
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
			searchSceneRendered: false,
			searchSceneVisible: false,
			searchInputFocused: false,
			searchResults: [],
			searchLoading: false,
			searchLoaded: false,
			searchFailed: false,
			searchRequestSeq: 0,
			searchDebounceTimer: null,
			searchCloseTimer: null,
			wordDetailVisible: false,
			selectedSearchWord: null,
			wordDetail: null,
			wordDetailLoading: false,
			wordDetailLoaded: false,
			wordDetailFailed: false,
			wordDetailRequestSeq: 0,
			searchDockTop: 28,
			searchDockHeight: 40,
			searchDockLeft: 12,
			searchDockRight: 104,
			pendingSearchKeyword: '',
			pendingWordId: null,
			favoriteSubmittingWordIds: [],
			shortcutItems: [
				{
					key: 'favorite-word',
					title: '单词收藏',
					description: '查词重点回看',
					icon: 'help-filled',
					iconColor: '#49627c',
					iconBackground: 'linear-gradient(135deg, rgba(225, 233, 247, 0.98), rgba(199, 212, 234, 0.95))',
					path: '/pages/kyyy/favorite-word/index',
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
					key: 'reading',
					title: '阅读',
					description: '整篇阅读作答',
					icon: 'compose',
					iconColor: '#5a5f7f',
					iconBackground: 'linear-gradient(135deg, rgba(231, 233, 246, 0.98), rgba(212, 217, 238, 0.95))',
					path: '/pages/kyyy/reading/index',
					navigationType: 'navigateTo'
				}
			]
		}
	},
	computed: {
		normalizedSearchKeyword(): string {
			return normalizeSearchKeyword(this.keyword)
		},
		searchDockStyle(): Record<string, string> {
			return {
				top: `${this.searchDockTop}px`,
				left: `${this.searchDockLeft}px`,
				right: `${this.searchDockRight}px`,
				height: `${this.searchDockHeight}px`
			}
		},
		searchResultPanelStyle(): Record<string, string> {
			return {
				paddingTop: `${this.searchDockTop + this.searchDockHeight + 18}px`
			}
		}
	},
	created() {
		this.syncSearchLayoutMetrics()
	},
	onLoad(query?: HomePageLoadQuery) {
		this.pendingSearchKeyword = decodeURIComponent(String(query?.searchKeyword || '')).trim()
		this.pendingWordId = toPositiveNumber(query?.wordId)
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	beforeUnmount() {
		this.clearSearchTimers()
	},
	methods: {
		handleSearchConfirm(): void {
			this.openSearchScene()
			this.resetWordDetail()
			this.loadSearchResults(this.normalizedSearchKeyword)
		},
		handleSearchFocus(): void {
			this.openSearchScene()
		},
		handleSearchInput(event?: { detail?: { value?: string } }): void {
			if (typeof event?.detail?.value === 'string') {
				this.keyword = event.detail.value
			}
			if (!this.searchSceneRendered) {
				this.openSearchScene()
			}
			this.resetWordDetail()
			this.scheduleSearchResults()
		},
		openSearchScene(): void {
			this.clearSearchCloseTimer()
			if (!this.searchSceneRendered) {
				this.searchSceneRendered = true
				this.searchSceneVisible = false
			}
			this.$nextTick(() => {
				setTimeout(() => {
					if (!this.searchSceneRendered) {
						return
					}
					this.searchSceneVisible = true
					this.searchInputFocused = true
					if (this.normalizedSearchKeyword && !this.searchLoading && !this.searchLoaded) {
						this.scheduleSearchResults()
					}
				}, 16)
			})
		},
		closeSearchScene(): void {
			this.clearSearchTimers()
			this.resetWordDetail()
			this.searchRequestSeq += 1
			this.searchInputFocused = false
			this.searchSceneVisible = false
			this.searchLoading = false
			this.searchCloseTimer = setTimeout(() => {
				this.searchSceneRendered = false
				this.searchResults = []
				this.searchLoaded = false
				this.searchFailed = false
				this.keyword = ''
				this.searchCloseTimer = null
			}, SEARCH_SCENE_ANIMATION_DURATION)
		},
		handleSearchBlankTap(): void {
			this.closeSearchScene()
		},
		handleSearchClear(): void {
			this.keyword = ''
			this.searchResults = []
			this.searchLoaded = false
			this.searchFailed = false
			this.searchLoading = false
			this.searchRequestSeq += 1
			this.resetWordDetail()
			this.clearSearchDebounceTimer()
			if (this.searchSceneRendered) {
				this.searchInputFocused = true
			}
		},
		scheduleSearchResults(): void {
			this.clearSearchDebounceTimer()
			const keyword = this.normalizedSearchKeyword
			this.searchFailed = false
			if (!keyword) {
				this.searchResults = []
				this.searchLoading = false
				this.searchLoaded = false
				this.searchRequestSeq += 1
				return
			}
			this.searchLoading = true
			this.searchDebounceTimer = setTimeout(() => {
				this.loadSearchResults(keyword)
			}, SEARCH_DEBOUNCE_DELAY)
		},
		async loadSearchResults(keyword?: string): Promise<void> {
			const normalizedKeyword = normalizeSearchKeyword(keyword ?? this.normalizedSearchKeyword)
			this.clearSearchDebounceTimer()
			if (!normalizedKeyword) {
				this.searchResults = []
				this.searchLoading = false
				this.searchLoaded = false
				this.searchFailed = false
				return
			}
			const requestSeq = this.searchRequestSeq + 1
			this.searchRequestSeq = requestSeq
			this.searchLoading = true
			this.searchFailed = false
			try {
				const response = await searchHomeWords(normalizedKeyword)
				if (requestSeq !== this.searchRequestSeq) {
					return
				}
				this.searchResults = (Array.isArray(response) ? response : [])
					.map((item) => normalizeWordSearchResult(item))
					.filter((item) => !!item.wordText)
				this.searchLoaded = true
				this.tryOpenPendingWordDetail()
			} catch (error) {
				if (requestSeq !== this.searchRequestSeq) {
					return
				}
				console.warn('[kyyy-home] search words failed', error)
				this.searchResults = []
				this.searchLoaded = true
				this.searchFailed = true
			} finally {
				if (requestSeq === this.searchRequestSeq) {
					this.searchLoading = false
				}
			}
		},
		openWordDetail(item: KyyyHomeWordSearchState): void {
			this.selectedSearchWord = item
			this.wordDetail = createWordDetailFallback(item)
			this.wordDetailVisible = true
			this.wordDetailLoaded = false
			this.wordDetailFailed = false
			this.wordDetailLoading = false
			this.searchInputFocused = false
			if (!item.wordId) {
				this.wordDetailLoaded = true
				return
			}
			this.loadWordDetail(item.wordId, item)
		},
		async loadWordDetail(wordId: number, fallback: KyyyHomeWordSearchState): Promise<void> {
			const requestSeq = this.wordDetailRequestSeq + 1
			this.wordDetailRequestSeq = requestSeq
			this.wordDetailLoading = true
			this.wordDetailFailed = false
			try {
				const response = await getHomeWordDetail(wordId)
				if (requestSeq !== this.wordDetailRequestSeq) {
					return
				}
				this.wordDetail = normalizeWordDetailResult(response, fallback)
				this.wordDetailLoaded = true
				this.wordDetailFailed = !this.wordDetail
				if (!this.wordDetail) {
					this.wordDetail = createWordDetailFallback(fallback)
				}
			} catch (error) {
				if (requestSeq !== this.wordDetailRequestSeq) {
					return
				}
				console.warn('[kyyy-home] load word detail failed', error)
				this.wordDetail = createWordDetailFallback(fallback)
				this.wordDetailLoaded = true
				this.wordDetailFailed = true
			} finally {
				if (requestSeq === this.wordDetailRequestSeq) {
					this.wordDetailLoading = false
				}
			}
		},
		closeWordDetail(): void {
			this.resetWordDetail()
			if (this.searchSceneRendered) {
				this.searchInputFocused = true
			}
		},
		resetWordDetail(): void {
			this.wordDetailRequestSeq += 1
			this.wordDetailVisible = false
			this.wordDetailLoading = false
			this.wordDetailLoaded = false
			this.wordDetailFailed = false
			this.wordDetail = null
			this.selectedSearchWord = null
		},
		syncSearchLayoutMetrics(): void {
			const systemInfo = uni.getSystemInfoSync()
			const statusBarHeight = Number(systemInfo.statusBarHeight || 20)
			const windowWidth = Number(systemInfo.windowWidth || 375)
			this.searchDockTop = statusBarHeight + 6
			this.searchDockHeight = 40
			this.searchDockLeft = 12
			this.searchDockRight = 104
			if (typeof uni.getMenuButtonBoundingClientRect !== 'function') {
				return
			}
			try {
				const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
				if (!menuButtonInfo || !menuButtonInfo.height) {
					return
				}
				const dockHeight = Math.max(Number(menuButtonInfo.height), 32)
				const menuTop = Number(menuButtonInfo.top || statusBarHeight + 6)
				const menuLeft = Number(menuButtonInfo.left || 0)
				const menuRight = Number(menuButtonInfo.right || windowWidth - 10)
				const menuRightGap = Math.max(windowWidth - menuRight, 8)
				this.searchDockTop = menuTop
				this.searchDockHeight = dockHeight
				this.searchDockLeft = menuRightGap
				this.searchDockRight = menuLeft > 0 ? Math.max(windowWidth - menuLeft + menuRightGap, 96) : 104
			} catch (error) {
				console.warn('[kyyy-home] resolve search layout failed', error)
			}
		},
		handleDailyWordChange(event: { detail?: { current?: number } } | undefined): void {
			const nextIndex = Number(event?.detail?.current ?? 0)
			this.dailyWordCurrentIndex = Number.isFinite(nextIndex) ? Math.max(nextIndex, 0) : 0
		},
		resolveDailyWordWordStyle(word: KyyyHomeDailyWordState): Record<string, string> {
			return resolveDailyWordWordStyle(word)
		},
		resolveDailyWordMeaning(word: KyyyHomeDailyWordState): string {
			return resolveDailyWordMeaning(word)
		},
		resolveDefaultWordBankText(): string {
			return this.dashboard.defaultWordBankName ? `默认 ${this.dashboard.defaultWordBankName}` : '先选词库'
		},
		resolveSearchResultKey(item: KyyyHomeWordSearchState, index: number): string {
			return item.wordId ? `word-${item.wordId}` : `${item.wordText}-${index}`
		},
		resolveSearchMeaning(item: KyyyHomeWordSearchState): string {
			return resolveSearchMeaningText(item)
		},
		resolveSearchSummary(item: KyyyHomeWordSearchState): string {
			return resolveSearchSummaryText(item)
		},
		resolveFavoriteIcon(isFavorite: boolean): string {
			return isFavorite ? 'star-filled' : 'star'
		},
		resolveFavoriteColor(isFavorite: boolean): string {
			return isFavorite ? '#b06c3d' : '#7b858d'
		},
		resolveWordDetailSource(): KyyyHomeWordDetailState | KyyyHomeWordSearchState | null {
			return this.wordDetail || this.selectedSearchWord
		},
		resolveWordDetailIsFavorite(): boolean {
			return Boolean(this.resolveWordDetailSource()?.isFavorite)
		},
		resolveWordDetailWordText(): string {
			return resolveWordDetailWordTextValue(this.resolveWordDetailSource())
		},
		resolveWordDetailWordStyle(): Record<string, string> {
			return resolveWordDetailWordStyleValue(this.resolveWordDetailWordText())
		},
		resolveWordDetailPhonetic(): string {
			return resolveWordDetailPhoneticText(this.resolveWordDetailSource())
		},
		resolveWordDetailPartOfSpeech(): string {
			return resolveWordDetailPartOfSpeechText(this.resolveWordDetailSource())
		},
		resolveWordDetailMeaning(): string {
			return resolveWordDetailMeaningText(this.resolveWordDetailSource())
		},
		resolveWordDetailExampleSentence(): string {
			return resolveWordDetailExampleSentenceText(this.wordDetail)
		},
		resolveWordDetailExampleTranslation(): string {
			return resolveWordDetailExampleTranslationText(this.wordDetail)
		},
		resolveWordDetailExamples(): KyyyWordExampleState[] {
			return resolveWordDetailExamplesValue(this.wordDetail)
		},
		isFavoriteSubmitting(wordId: number | null): boolean {
			return wordId ? this.favoriteSubmittingWordIds.includes(wordId) : false
		},
		async handleSearchResultFavoriteToggle(item: KyyyHomeWordSearchState): Promise<void> {
			await this.toggleWordFavorite(item)
		},
		async handleWordDetailFavoriteToggle(): Promise<void> {
			const source = this.resolveWordDetailSource()
			if (!source) {
				return
			}
			await this.toggleWordFavorite(source)
		},
		async toggleWordFavorite(source: KyyyHomeWordSearchState | KyyyHomeWordDetailState): Promise<void> {
			const wordId = source.wordId
			if (!wordId || this.isFavoriteSubmitting(wordId)) {
				return
			}
			this.favoriteSubmittingWordIds = [...this.favoriteSubmittingWordIds, wordId]
			const willFavorite = !source.isFavorite
			try {
				const result = willFavorite ? await favoriteWord(wordId) : await unfavoriteWord(wordId)
				this.applyFavoriteStateToWord(wordId, Boolean(result.isFavorite))
				uni.showToast({
					title: result.isFavorite ? '已收藏' : '已取消收藏',
					icon: 'none'
				})
			} catch (error) {
				console.warn('[kyyy-home] toggle favorite failed', error)
				uni.showToast({
					title: resolveErrorMessage(error, willFavorite ? '收藏失败' : '取消收藏失败'),
					icon: 'none'
				})
			} finally {
				this.favoriteSubmittingWordIds = this.favoriteSubmittingWordIds.filter((item) => item !== wordId)
			}
		},
		applyFavoriteStateToWord(wordId: number, isFavorite: boolean): void {
			this.searchResults = this.searchResults.map((item) => item.wordId === wordId ? {
				...item,
				isFavorite
			} : item)
			if (this.selectedSearchWord?.wordId === wordId) {
				this.selectedSearchWord = {
					...this.selectedSearchWord,
					isFavorite
				}
			}
			if (this.wordDetail?.wordId === wordId) {
				this.wordDetail = {
					...this.wordDetail,
					isFavorite
				}
			}
		},
		tryOpenPendingWordDetail(): void {
			if (!this.pendingWordId || !this.pendingSearchKeyword || this.wordDetailVisible || !this.searchLoaded || this.searchLoading) {
				return
			}
			const matched = this.searchResults.find((item) => item.wordId === this.pendingWordId)
			if (!matched) {
				return
			}
			const targetWordId = this.pendingWordId
			this.pendingSearchKeyword = ''
			this.pendingWordId = null
			this.openWordDetail(matched)
			if (matched.wordId !== targetWordId) {
				this.pendingWordId = targetWordId
			}
		},
		consumePendingSearchRoute(): void {
			if (!this.pendingSearchKeyword) {
				return
			}
			this.keyword = encodeWordNavigationKeyword(this.pendingSearchKeyword)
			this.pendingSearchKeyword = this.keyword
			this.openSearchScene()
			this.resetWordDetail()
			this.loadSearchResults(this.keyword).catch(() => {})
		},
		clearSearchDebounceTimer(): void {
			if (this.searchDebounceTimer) {
				clearTimeout(this.searchDebounceTimer)
				this.searchDebounceTimer = null
			}
		},
		clearSearchCloseTimer(): void {
			if (this.searchCloseTimer) {
				clearTimeout(this.searchCloseTimer)
				this.searchCloseTimer = null
			}
		},
		clearSearchTimers(): void {
			this.clearSearchDebounceTimer()
			this.clearSearchCloseTimer()
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
			this.consumePendingSearchRoute()
		},
		applyCachedDailyWord(): void {
			const cachedDailyWords = readCachedDailyWords()
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
			if (this.loadingDailyWord) {
				return
			}
			this.loadingDailyWord = true
			const cachedDailyWords = readCachedDailyWords()
			try {
				const responseWords = (await getHomeDailyWords())
					.filter(hasValidDailyWordText)
					.map((item) => normalizeDailyWord(item))
				if (responseWords.length > 0) {
					const words = ensureDailyWordSlides(responseWords)
					this.dailyWords = words
					this.dailyWordCurrentIndex = 0
					cacheDailyWords(words)
					return
				}
				if (!cachedDailyWords?.length) {
					this.dailyWords = ensureDailyWordSlides([createHelloDailyWord()])
					this.dailyWordCurrentIndex = 0
				}
			} catch (error) {
				console.warn('[kyyy-home] load daily word failed', error)
				if (!cachedDailyWords?.length) {
					this.dailyWords = ensureDailyWordSlides([createHelloDailyWord()])
					this.dailyWordCurrentIndex = 0
				}
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
@import '@/pages/kyyy/home/index.scss';
</style>
