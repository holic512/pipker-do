<template>
	<!-- AI 索引: 我的题库页 -->
	<page-shell
		current="question-bank"
		root-class="question-bank-page"
		content-class="question-bank-page__content"
	>
		<view class="question-bank-page__hero">
			<text class="question-bank-page__title">我的题库</text>
			<text class="question-bank-page__desc">把常刷题库收进个人清单，回到这里就能继续上次的进度。</text>
		</view>

		<view class="question-bank-page__search-shell">
			<view class="question-bank-page__search-box">
				<uni-icons type="search" size="18" color="#99a5b5" />
				<input
					v-model="keyword"
					class="question-bank-page__search-input"
					placeholder="搜索题库名称、副标题、分类"
					placeholder-class="question-bank-page__search-placeholder"
					confirm-type="search"
					@confirm="handleSearchConfirm"
				/>
				<view v-if="keyword" class="question-bank-page__search-clear" @tap="handleSearchClear">
					<text class="question-bank-page__search-clear-text">×</text>
				</view>
			</view>
		</view>

		<view v-if="loading && !loadedOnce" class="question-bank-page__state-card">
			<text class="question-bank-page__state-title">正在整理你的题库...</text>
			<text class="question-bank-page__state-desc">稍等一下，马上带出最近的学习进度。</text>
		</view>

		<view v-else-if="displayedBanks.length" class="question-bank-page__list">
			<view
				v-for="item in displayedBanks"
				:key="item.id"
				class="question-bank-page__card"
				@tap="handleBankTap(item)"
			>
				<view class="question-bank-page__card-main">
					<view class="question-bank-page__cover-stack">
						<view
							class="question-bank-page__cover-ring"
							:class="coverRingClass(item)"
							:style="coverRingStyle(item)"
						>
							<view class="question-bank-page__cover-shell">
								<image
									v-if="item.coverUrl"
									class="question-bank-page__cover"
									:src="item.coverUrl"
									mode="aspectFill"
								/>
								<view v-else class="question-bank-page__cover question-bank-page__cover--fallback">
									<text class="question-bank-page__cover-initial">{{ buildCoverInitial(item.bankName) }}</text>
									<view class="question-bank-page__cover-grid">
										<view class="question-bank-page__cover-dot"></view>
										<view class="question-bank-page__cover-dot"></view>
										<view class="question-bank-page__cover-dot"></view>
										<view class="question-bank-page__cover-dot"></view>
									</view>
								</view>
							</view>
						</view>
						<text class="question-bank-page__cover-progress">{{ formatProgress(item.currentProgress) }}</text>
					</view>

					<view class="question-bank-page__card-body">
						<view class="question-bank-page__card-head">
							<view class="question-bank-page__title-wrap">
								<text class="question-bank-page__card-title">{{ item.bankName }}</text>
								<text v-if="item.subtitle" class="question-bank-page__card-subtitle">{{ item.subtitle }}</text>
							</view>
							<view class="question-bank-page__stage-badge" :class="stageClass(item)">
								{{ stageText(item) }}
							</view>
						</view>

						<view class="question-bank-page__meta-row">
							<text class="question-bank-page__meta-pill">{{ item.categoryName || '未分类' }}</text>
							<text class="question-bank-page__meta-pill" :class="difficultyTagClass(item.difficultyLevel)">{{ difficultyLabel(item.difficultyLevel) }}</text>
							<text class="question-bank-page__meta-pill">{{ item.questionCount }} 题</text>
						</view>

						<view class="question-bank-page__info-row">
							<text class="question-bank-page__info-text">最近练习：{{ formatLastPractice(item.lastPracticeAt) }}</text>
							<text class="question-bank-page__info-text">已做 {{ item.studiedCount }} / {{ item.questionCount }}</text>
							<text class="question-bank-page__info-text">上次入口：{{ practiceResumeLabel(item) }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view v-else class="question-bank-page__empty">
			<view class="question-bank-page__empty-ornament">
				<view class="question-bank-page__empty-ring"></view>
				<view class="question-bank-page__empty-dot"></view>
			</view>
			<text class="question-bank-page__empty-title">{{ keyword.trim() ? '没有找到匹配题库' : '还没有加入题库' }}</text>
			<text class="question-bank-page__empty-desc">
				{{ keyword.trim() ? '换个关键词再试试，或者去公共题库看看有没有合适的内容。' : '先去公共题库挑几套常刷题库，之后这里会自动保存你的学习进度。' }}
			</text>
			<view class="question-bank-page__empty-actions">
				<button
					v-if="keyword.trim()"
					class="question-bank-page__ghost-button"
					@tap="resetKeyword"
				>
					清空搜索
				</button>
				<button class="question-bank-page__primary-button" @tap="goPublicBanks">
					去添加题库
				</button>
			</view>
		</view>

		<view class="question-bank-page__add-card" @tap="goPublicBanks">
			<view class="question-bank-page__add-mark">+</view>
			<view class="question-bank-page__add-copy">
				<text class="question-bank-page__add-title">添加新题库</text>
				<text class="question-bank-page__add-desc">进入完整公共题库列表，按已选 / 未选和分类筛选。</text>
			</view>
			<uni-icons type="right" size="18" color="#7e8798" />
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getMineQuestionBanks } from '@/pages/kyzz/api/question-bank'
import { getPracticeDashboard } from '@/pages/kyzz/api/practice'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'
import type {
	KyzzQuestionBankMineDashboardState,
	KyzzQuestionBankMineRecordResponse,
	KyzzQuestionBankMineViewRecord,
	SearchConfirmEvent
} from '@/pages/kyzz/question-bank/types'
import type { KyzzPracticeDashboardState, KyzzPracticeBankViewRecord } from '@/pages/kyzz/practice/types'
import { createEmptyPracticeDashboard, normalizePracticeDashboard } from '@/pages/kyzz/practice/view'

const DIFFICULTY_MAP: Record<number, string> = {
	1: '简单',
	2: '中等',
	3: '困难',
	4: '冲刺'
}

interface LoadMineOptions {
	silent?: boolean
	fromPullDownRefresh?: boolean
}

interface MinePageState {
	loading: boolean
	loadedOnce: boolean
	keyword: string
	dashboard: KyzzQuestionBankMineDashboardState
	practiceDashboard: KyzzPracticeDashboardState
}

function createEmptyMineDashboard(): KyzzQuestionBankMineDashboardState {
	return {
		summary: {
			selectedCount: 0,
			inProgressCount: 0,
			completedCount: 0
		},
		records: []
	}
}

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'QuestionBankPage',
	data(): MinePageState {
		return {
			loading: false,
			loadedOnce: false,
			keyword: '',
			dashboard: createEmptyMineDashboard(),
			practiceDashboard: createEmptyPracticeDashboard()
		}
	},
	computed: {
		displayedBanks(): KyzzQuestionBankMineViewRecord[] {
			const keyword = this.keyword.trim().toLowerCase()
			if (!keyword) {
				return this.dashboard.records
			}
			return this.dashboard.records.filter((item) => {
				return [item.bankName, item.subtitle, item.categoryName]
					.filter((field): field is string => Boolean(field))
					.some((field) => field.toLowerCase().includes(keyword))
			})
		},
		practiceBankMap(): Record<number, KyzzPracticeBankViewRecord> {
			return this.practiceDashboard.records.reduce<Record<number, KyzzPracticeBankViewRecord>>((result, item) => {
				result[item.bankId] = item
				return result
			}, {})
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	onPullDownRefresh() {
		this.bootstrapAndLoad({ fromPullDownRefresh: true })
	},
	methods: {
		async bootstrapAndLoad(options: LoadMineOptions = {}): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadMineQuestionBanks({ silent: true })
				await this.loadPracticeDashboard()
			} catch (error) {
				if (!options.silent) {
					uni.showToast({
						title: resolveErrorMessage(error, '题库加载失败'),
						icon: 'none'
					})
				}
			} finally {
				if (options.fromPullDownRefresh) {
					uni.stopPullDownRefresh()
				}
			}
		},
		async loadMineQuestionBanks(options: Pick<LoadMineOptions, 'silent'> = {}): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getMineQuestionBanks()
				this.dashboard = {
					summary: result?.summary ?? createEmptyMineDashboard().summary,
					records: Array.isArray(result?.records)
						? result.records.map((record) => this.normalizeRecord(record))
						: []
				}
				this.loadedOnce = true
				if (!options.silent) {
					uni.showToast({
						title: '题库已刷新',
						icon: 'none'
					})
				}
			} catch (error) {
				this.loadedOnce = true
				throw error
			} finally {
				this.loading = false
			}
		},
		async loadPracticeDashboard(): Promise<void> {
			const result = await getPracticeDashboard()
			this.practiceDashboard = normalizePracticeDashboard(result)
		},
		normalizeRecord(record: KyzzQuestionBankMineRecordResponse): KyzzQuestionBankMineViewRecord {
			return {
				...record,
				questionCount: toNumber(record.questionCount),
				totalScore: toNumber(record.totalScore),
				ratingCount: toNumber(record.ratingCount),
				studyUserCount: toNumber(record.studyUserCount),
				currentProgress: toNumber(record.currentProgress),
				studiedCount: toNumber(record.studiedCount),
				correctCount: toNumber(record.correctCount),
				wrongCount: toNumber(record.wrongCount)
			}
		},
		difficultyLabel(level: number): string {
			return DIFFICULTY_MAP[level] || `L${level || 0}`
		},
		difficultyTagClass(level: number): string {
			if (level === 1) {
				return 'question-bank-page__meta-pill--simple'
			}
			if (level === 2) {
				return 'question-bank-page__meta-pill--medium'
			}
			if (level === 3) {
				return 'question-bank-page__meta-pill--hard'
			}
			if (level === 4) {
				return 'question-bank-page__meta-pill--sprint'
			}
			return ''
		},
		progressPercent(item: KyzzQuestionBankMineViewRecord): number {
			const value = toNumber(item.currentProgress)
			if (value <= 0) {
				return 0
			}
			if (value >= 100) {
				return 100
			}
			return value
		},
		coverRingClass(item: KyzzQuestionBankMineViewRecord): string {
			const progress = this.progressPercent(item)
			if (progress >= 100 && item.questionCount > 0) {
				return 'question-bank-page__cover-ring--done'
			}
			if (progress > 0) {
				return 'question-bank-page__cover-ring--pulse'
			}
			return 'question-bank-page__cover-ring--idle'
		},
		coverRingStyle(item: KyzzQuestionBankMineViewRecord): Record<string, string> {
			const progress = this.progressPercent(item)
			const progressDegree = `${Math.max(0, Math.min(360, progress * 3.6))}deg`
			const activeColor = progress >= 100 && item.questionCount > 0
				? '#8eb89a'
				: '#7a889f'
			return {
				background: `conic-gradient(${activeColor} 0deg, ${activeColor} ${progressDegree}, rgba(224, 230, 240, 0.88) ${progressDegree}, rgba(224, 230, 240, 0.88) 360deg)`
			}
		},
		formatProgress(value: number): string {
			const progress = toNumber(value)
			const normalized = Math.round(progress * 10) / 10
			const display = Number.isInteger(normalized) ? normalized : normalized.toFixed(1)
			return `${display}%`
		},
		formatLastPractice(value: string | null): string {
			if (!value) {
				return '暂未开始'
			}
			const normalized = value.replace(/-/g, '/')
			const practiceDate = new Date(normalized)
			if (Number.isNaN(practiceDate.getTime())) {
				return value.slice(0, 16).replace('T', ' ')
			}
			const today = new Date()
			const oneDay = 24 * 60 * 60 * 1000
			const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime()
			const practiceStart = new Date(practiceDate.getFullYear(), practiceDate.getMonth(), practiceDate.getDate()).getTime()
			const diffDays = Math.floor((todayStart - practiceStart) / oneDay)
			if (diffDays === 0) {
				return '今天'
			}
			if (diffDays === 1) {
				return '昨天'
			}
			if (diffDays > 1 && diffDays < 7) {
				return `${diffDays} 天前`
			}
			return `${practiceDate.getFullYear()}-${this.pad(practiceDate.getMonth() + 1)}-${this.pad(practiceDate.getDate())}`
		},
		pad(value: number): string {
			return String(value).padStart(2, '0')
		},
		stageText(item: KyzzQuestionBankMineViewRecord): string {
			const practiceRecord = this.practiceBankMap[item.id]
			if (practiceRecord && practiceRecord.resumeLabel) {
				return practiceRecord.resumeLabel
			}
			const progress = toNumber(item.currentProgress)
			if (progress >= 100 && item.questionCount > 0) {
				return '已完成'
			}
			if (progress > 0) {
				return '进行中'
			}
			return '待开始'
		},
		stageClass(item: KyzzQuestionBankMineViewRecord): string {
			const practiceRecord = this.practiceBankMap[item.id]
			if (practiceRecord) {
				if (practiceRecord.resumeStatus === 'completed') {
					return 'question-bank-page__stage-badge--done'
				}
				if (practiceRecord.resumeStatus === 'in_progress') {
					return 'question-bank-page__stage-badge--active'
				}
				return 'question-bank-page__stage-badge--idle'
			}
			const progress = toNumber(item.currentProgress)
			if (progress >= 100 && item.questionCount > 0) {
				return 'question-bank-page__stage-badge--done'
			}
			if (progress > 0) {
				return 'question-bank-page__stage-badge--active'
			}
			return 'question-bank-page__stage-badge--idle'
		},
		practiceResumeLabel(item: KyzzQuestionBankMineViewRecord): string {
			return this.practiceBankMap[item.id]?.resumeLabel || this.stageText(item)
		},
		buildCoverInitial(name: string): string {
			if (!name) {
				return '题'
			}
			return name.trim().slice(0, 1).toUpperCase()
		},
		handleSearchConfirm(event: SearchConfirmEvent): void {
			this.keyword = (event?.detail?.value || event?.value || this.keyword || '').trim()
		},
		handleSearchClear(): void {
			this.keyword = ''
		},
		resetKeyword(): void {
			this.keyword = ''
		},
		goPublicBanks(): void {
			uni.navigateTo({
				url: '/pages/kyzz/question-bank/public'
			})
		},
		handleBankTap(item: KyzzQuestionBankMineViewRecord): void {
			openPracticeTab({ bankId: item.id }).catch(() => {})
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.question-bank-page {
	min-height: 100vh;
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.98) 0%, rgba(245, 247, 250, 0.96) 42%, rgba(236, 241, 246, 0.94) 100%);
	box-sizing: border-box;
}

.question-bank-page__content {
	padding: 12rpx 24rpx calc(env(safe-area-inset-bottom) + 196rpx);
	box-sizing: border-box;
}

.question-bank-page__hero {
	padding: 12rpx 6rpx 0;
}

.question-bank-page__title {
	display: block;
	font-size: 54rpx;
	line-height: 1.08;
	font-family: $heading-font-family;
	font-weight: 700;
	color: #293241;
}

.question-bank-page__desc {
	display: block;
	margin-top: 16rpx;
	font-size: 26rpx;
	line-height: 1.7;
	color: #6f7a86;
}

.question-bank-page__search-shell {
	margin-top: 28rpx;
}

.question-bank-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	height: 82rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: rgba(244, 247, 251, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(225, 232, 241, 0.98);
}

.question-bank-page__search-input {
	flex: 1;
	min-width: 0;
	height: 100%;
	font-size: 26rpx;
	color: #2d3645;
}

.question-bank-page__search-placeholder {
	font-size: 24rpx;
	color: #a9b2bf;
}

.question-bank-page__search-clear {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: rgba(210, 218, 229, 0.92);
}

.question-bank-page__search-clear-text {
	font-size: 28rpx;
	line-height: 1;
	color: #ffffff;
}

.question-bank-page__state-card,
.question-bank-page__empty {
	margin-top: 26rpx;
	padding: 44rpx 34rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.92);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.06);
}

.question-bank-page__state-title,
.question-bank-page__empty-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.25;
	font-weight: 700;
	color: #2d3642;
}

.question-bank-page__state-desc,
.question-bank-page__empty-desc {
	display: block;
	margin-top: 16rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #7b8594;
}

.question-bank-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 26rpx;
}

.question-bank-page__card {
	padding: 18rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 16rpx 34rpx rgba(43, 52, 55, 0.06);
}

.question-bank-page__card-main {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
}

.question-bank-page__cover-stack {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
	width: 156rpx;
	flex-shrink: 0;
}

.question-bank-page__cover-ring {
	position: relative;
	width: 156rpx;
	height: 156rpx;
	padding: 8rpx;
	border-radius: 30rpx;
	box-shadow: 0 14rpx 28rpx rgba(112, 126, 151, 0.11);
}

.question-bank-page__cover-ring::before {
	content: '';
	position: absolute;
	inset: 5rpx;
	border-radius: 26rpx;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(245, 248, 252, 0.78) 100%);
}

.question-bank-page__cover-ring--pulse::after,
.question-bank-page__cover-ring--done::after {
	content: '';
	position: absolute;
	inset: -10rpx;
	border-radius: 40rpx;
	border: 2rpx solid rgba(122, 136, 159, 0.18);
	opacity: 0;
}

.question-bank-page__cover-ring--pulse::after {
	animation: question-bank-cover-pulse 2.2s ease-out infinite;
}

.question-bank-page__cover-ring--done::after {
	border-color: rgba(142, 184, 154, 0.22);
	animation: question-bank-cover-pulse 2.8s ease-out infinite;
}

.question-bank-page__cover-shell {
	position: relative;
	z-index: 1;
	width: 100%;
	height: 100%;
	border-radius: 22rpx;
	overflow: hidden;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.5);
}

.question-bank-page__cover {
	width: 100%;
	height: 100%;
	border-radius: 22rpx;
	background: linear-gradient(145deg, #eef3ff 0%, #dde7fb 100%);
	display: block;
	overflow: hidden;
}

.question-bank-page__cover--fallback {
	height: 100%;
	padding: 18rpx;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	box-shadow: inset 0 0 0 2rpx rgba(84, 94, 118, 0.08);
}

.question-bank-page__cover-initial {
	font-size: 46rpx;
	line-height: 1;
	font-family: $heading-font-family;
	font-weight: 700;
	color: #58657b;
}

.question-bank-page__cover-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 8rpx;
}

.question-bank-page__cover-dot {
	height: 16rpx;
	border-radius: 6rpx;
	background: rgba(84, 94, 118, 0.16);
}

.question-bank-page__cover-progress {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	letter-spacing: 0.02em;
	color: #526077;
}

.question-bank-page__card-body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
}

.question-bank-page__card-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
}

.question-bank-page__title-wrap {
	flex: 1;
	min-width: 0;
}

.question-bank-page__card-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2c3441;
}

.question-bank-page__card-subtitle {
	display: block;
	margin-top: 6rpx;
	font-size: 20rpx;
	line-height: 1.45;
	color: #7d8794;
}

.question-bank-page__stage-badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 40rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 600;
	white-space: nowrap;
}

.question-bank-page__stage-badge--active {
	background: rgba(215, 226, 255, 0.8);
	color: #516079;
}

.question-bank-page__stage-badge--done {
	background: rgba(221, 233, 222, 0.88);
	color: #557062;
}

.question-bank-page__stage-badge--idle {
	background: rgba(229, 234, 237, 0.9);
	color: #707a84;
}

.question-bank-page__meta-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-top: 12rpx;
}

.question-bank-page__meta-pill {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 40rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: #f3f6f9;
	font-size: 20rpx;
	line-height: 1;
	color: #707987;
}

.question-bank-page__meta-pill--simple {
	background: rgba(221, 233, 222, 0.92);
	color: #587062;
}

.question-bank-page__meta-pill--medium {
	background: rgba(215, 226, 255, 0.86);
	color: #516079;
}

.question-bank-page__meta-pill--hard {
	background: rgba(242, 231, 218, 0.94);
	color: #916d3f;
}

.question-bank-page__meta-pill--sprint {
	background: rgba(244, 226, 225, 0.96);
	color: #a35c59;
}

.question-bank-page__info-row {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-top: 14rpx;
}

.question-bank-page__info-text {
	font-size: 21rpx;
	line-height: 1.5;
	color: #7d8694;
}

.question-bank-page__empty {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
}

.question-bank-page__empty-ornament {
	position: relative;
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 12rpx;
}

.question-bank-page__empty-ring {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	border: 3rpx solid rgba(84, 94, 118, 0.14);
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(237, 242, 249, 0.82) 100%);
}

.question-bank-page__empty-dot {
	position: absolute;
	right: 12rpx;
	top: 12rpx;
	width: 22rpx;
	height: 22rpx;
	border-radius: 50%;
	background: rgba(84, 94, 118, 0.2);
}

.question-bank-page__empty-actions {
	display: flex;
	gap: 16rpx;
	margin-top: 26rpx;
}

.question-bank-page__primary-button,
.question-bank-page__ghost-button {
	margin: 0;
	padding: 0 28rpx;
	height: 78rpx;
	line-height: 78rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 600;
}

.question-bank-page__primary-button::after,
.question-bank-page__ghost-button::after {
	border: 0;
}

.question-bank-page__primary-button {
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(84, 94, 118, 0.2);
}

.question-bank-page__ghost-button {
	background: #eef2f6;
	color: #5b667c;
}

.question-bank-page__add-card {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-top: 24rpx;
	padding: 26rpx 24rpx;
	border-radius: 28rpx;
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(241, 245, 251, 0.92) 100%);
	box-shadow: 0 16rpx 32rpx rgba(43, 52, 55, 0.05);
}

.question-bank-page__add-mark {
	width: 72rpx;
	height: 72rpx;
	border-radius: 24rpx;
	background: rgba(84, 94, 118, 0.1);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	line-height: 1;
	color: #546078;
	flex-shrink: 0;
}

.question-bank-page__add-copy {
	flex: 1;
	min-width: 0;
}

.question-bank-page__add-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2f3744;
}

.question-bank-page__add-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #7a8391;
}

@keyframes question-bank-cover-pulse {
	0% {
		transform: scale(0.94);
		opacity: 0;
	}
	20% {
		opacity: 0.42;
	}
	100% {
		transform: scale(1.08);
		opacity: 0;
	}
}
</style>
