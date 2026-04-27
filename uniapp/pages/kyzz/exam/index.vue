<template>
	<!-- AI 索引: KYZZ VIP 考试入口页 -->
	<page-shell
		current="exam"
		root-class="exam-page"
		content-class="exam-page__content"
	>
		<view class="exam-page__inner">
			<view class="exam-page__header">
				<view>
					<text class="exam-page__eyebrow">VIP EXAM</text>
					<text class="exam-page__title">模拟考试</text>
					<text class="exam-page__subtitle">按考研政治题型比例组卷，独立计时、独立保存考试记录。</text>
				</view>
				<view class="exam-page__vip-badge" :class="{ 'is-active': isVip }">
					<uni-icons type="star-filled" size="17" :color="isVip ? '#fff7e5' : '#8792a4'" />
					<text>{{ isVip ? 'VIP' : 'LOCKED' }}</text>
				</view>
			</view>

			<view v-if="ongoingExam" class="exam-page__ongoing">
				<view>
					<text class="exam-page__section-kicker">正在进行</text>
					<text class="exam-page__ongoing-title">{{ ongoingExam.examTypeLabel }}</text>
					<text class="exam-page__ongoing-desc">
						已答 {{ formatCount(ongoingExam.answeredCount) }}/{{ formatCount(ongoingExam.totalQuestionCount) }} 题 · 剩余 {{ formatDuration(ongoingExam.remainingSeconds) }}
					</text>
				</view>
				<button class="exam-page__small-button" @tap="openSession(ongoingExam.sessionId)">继续考试</button>
			</view>

			<view class="exam-page__panel" :class="{ 'is-locked': !isVip }">
				<view class="exam-page__section-head">
					<view>
						<text class="exam-page__section-kicker">考试配置</text>
						<text class="exam-page__section-title">选择测验类型</text>
					</view>
				</view>

				<view class="exam-page__preset-grid">
					<view
						v-for="preset in presets"
						:key="preset.examType"
						class="exam-page__preset"
						:class="{ 'is-active': form.examType === preset.examType }"
						@tap="selectPreset(preset)"
					>
						<view class="exam-page__preset-top">
							<text class="exam-page__preset-title">{{ preset.title }}</text>
							<text class="exam-page__preset-time">{{ formatMinutes(preset.defaultDurationMinutes) }}</text>
						</view>
						<text class="exam-page__preset-desc">{{ preset.description }}</text>
						<view class="exam-page__preset-meta">
							<text v-if="formatCount(preset.singleCount) > 0">单选 {{ formatCount(preset.singleCount) }}</text>
							<text v-if="formatCount(preset.multipleCount) > 0">多选 {{ formatCount(preset.multipleCount) }}</text>
							<text v-if="formatCount(preset.shortCount) > 0">简答 {{ formatCount(preset.shortCount) }}</text>
						</view>
					</view>
				</view>

				<view class="exam-page__config-block">
					<text class="exam-page__config-label">难度</text>
					<view class="exam-page__chips">
						<view
							v-for="option in difficultyOptions"
							:key="option.difficultyMode"
							class="exam-page__chip"
							:class="{ 'is-active': form.difficultyMode === option.difficultyMode }"
							@tap="form.difficultyMode = option.difficultyMode"
						>
							<text>{{ option.title }}</text>
						</view>
					</view>
				</view>

				<view class="exam-page__config-block">
					<text class="exam-page__config-label">考试时长</text>
					<view class="exam-page__duration-row">
						<input
							v-model="durationInput"
							class="exam-page__duration-input"
							type="number"
							maxlength="3"
							placeholder="分钟"
							placeholder-class="exam-page__input-placeholder"
						/>
						<text class="exam-page__duration-unit">分钟</text>
					</view>
				</view>

				<button class="exam-page__primary-button" :disabled="starting || !isVip" @tap="handleStartExam">
					{{ starting ? '组卷中...' : '开始考试' }}
				</button>
			</view>

			<view class="exam-page__panel">
				<view class="exam-page__section-head">
					<view>
						<text class="exam-page__section-kicker">考试历史</text>
						<text class="exam-page__section-title">最近试卷</text>
					</view>
					<button class="exam-page__text-button" @tap="loadHistory">刷新</button>
				</view>

				<view v-if="history.length" class="exam-page__history">
					<view
						v-for="record in history"
						:key="record.sessionId"
						class="exam-page__history-item"
						@tap="openSession(record.sessionId)"
					>
						<view>
							<text class="exam-page__history-title">{{ record.examTypeLabel }}</text>
							<text class="exam-page__history-desc">
								{{ record.difficultyLabel }} · {{ record.statusLabel }} · {{ record.startedAt || '未记录时间' }}
							</text>
						</view>
						<view class="exam-page__history-score">
							<text>{{ formatCount(record.answeredCount) }}/{{ formatCount(record.totalQuestionCount) }}</text>
							<uni-icons type="right" size="15" color="#9aa4b5" />
						</view>
					</view>
				</view>
				<view v-else class="exam-page__empty">
					<text>{{ historyLoading ? '正在同步历史...' : '还没有考试记录' }}</text>
				</view>
			</view>

			<view v-if="loading" class="exam-page__loading-mask">
				<view class="exam-page__loading-card">
					<view class="exam-page__spinner"></view>
					<text>正在加载考试入口</text>
				</view>
			</view>

			<view v-if="!loading && !isVip" class="exam-page__vip-mask">
				<view class="exam-page__vip-dialog">
					<view class="exam-page__vip-icon">
						<uni-icons type="locked-filled" size="25" color="#fff7e5" />
					</view>
					<text class="exam-page__vip-dialog-title">VIP 专属考试</text>
					<text class="exam-page__vip-dialog-desc">开通或兑换会员后，可以生成模拟试卷、续考并查看考试历史。</text>
					<button class="exam-page__vip-dialog-button" @tap="goMine">去激活兑换码</button>
				</view>
			</view>
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getExamEntry, getExamHistory, startExam } from '@/pages/kyzz/api/exam'
import type {
	KyzzExamDifficultyMode,
	KyzzExamDifficultyOption,
	KyzzExamEntryResponse,
	KyzzExamPreset,
	KyzzExamSummary,
	KyzzExamType
} from '@/pages/kyzz/exam/types'

interface ExamFormState {
	examType: KyzzExamType
	difficultyMode: KyzzExamDifficultyMode
	durationMinutes: number
}

interface ExamIndexState {
	loading: boolean
	starting: boolean
	historyLoading: boolean
	entry: KyzzExamEntryResponse | null
	history: KyzzExamSummary[]
	form: ExamFormState
	durationInput: string
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
	name: 'ExamPage',
	components: {
		PageShell
	},
	data(): ExamIndexState {
		return {
			loading: false,
			starting: false,
			historyLoading: false,
			entry: null,
			history: [],
			form: {
				examType: 'full',
				difficultyMode: 'balanced',
				durationMinutes: 180
			},
			durationInput: '180'
		}
	},
	computed: {
		isVip(): boolean {
			const vipInfo = this.entry?.vipInfo
			return !!(vipInfo && (vipInfo.isVip || vipInfo.vip))
		},
		ongoingExam(): KyzzExamSummary | null {
			return this.entry?.ongoingExam || null
		},
		presets(): KyzzExamPreset[] {
			return this.entry?.presets || []
		},
		difficultyOptions(): KyzzExamDifficultyOption[] {
			return this.entry?.difficultyOptions || []
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		async bootstrapAndLoad(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				await bootstrapAuth({ silent: true })
				const entry = await getExamEntry()
				this.entry = entry
				this.applyDefaultPreset()
				if (this.isVip) {
					await this.loadHistory()
				}
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '考试入口加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		applyDefaultPreset(): void {
			if (!this.presets.length) {
				return
			}
			const preset = this.presets.find((item) => item.examType === this.form.examType) || this.presets[0]
			this.form.examType = preset.examType
			this.form.durationMinutes = toNumber(preset.defaultDurationMinutes, 180)
			this.durationInput = String(this.form.durationMinutes)
		},
		async loadHistory(): Promise<void> {
			if (!this.isVip || this.historyLoading) {
				return
			}
			this.historyLoading = true
			try {
				this.history = await getExamHistory(20)
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '考试历史加载失败'),
					icon: 'none'
				})
			} finally {
				this.historyLoading = false
			}
		},
		selectPreset(preset: KyzzExamPreset): void {
			this.form.examType = preset.examType
			this.form.durationMinutes = toNumber(preset.defaultDurationMinutes, this.form.durationMinutes)
			this.durationInput = String(this.form.durationMinutes)
		},
		async handleStartExam(): Promise<void> {
			if (!this.isVip || this.starting) {
				return
			}
			const durationMinutes = Math.max(10, Math.min(toNumber(this.durationInput, this.form.durationMinutes), 240))
			this.starting = true
			uni.showLoading({ title: '正在组卷...' })
			try {
				const detail = await startExam({
					examType: this.form.examType,
					difficultyMode: this.form.difficultyMode,
					durationMinutes
				})
				uni.hideLoading()
				this.openSession(detail.summary.sessionId)
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: resolveErrorMessage(error, '组卷失败'),
					icon: 'none'
				})
				this.bootstrapAndLoad()
			} finally {
				this.starting = false
			}
		},
		openSession(sessionId: number): void {
			if (!sessionId) {
				return
			}
			uni.navigateTo({
				url: `/pages/kyzz/exam/session?sessionId=${sessionId}`
			})
		},
		goMine(): void {
			uni.switchTab({
				url: '/pages/common/mine/index'
			})
		},
		formatCount(value: unknown): number {
			return toNumber(value)
		},
		formatMinutes(value: unknown): string {
			return `${toNumber(value)} 分钟`
		},
		formatDuration(value: unknown): string {
			const seconds = Math.max(0, toNumber(value))
			const minutes = Math.floor(seconds / 60)
			const restSeconds = seconds % 60
			return `${minutes}:${String(restSeconds).padStart(2, '0')}`
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.exam-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #f6f8fb 0%, #edf2f7 100%);
	box-sizing: border-box;
	overflow: hidden;
}

.exam-page__content {
	padding: 0 28rpx calc(env(safe-area-inset-bottom) + 168rpx);
}

.exam-page__inner {
	position: relative;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 28rpx;
}

.exam-page__header,
.exam-page__panel,
.exam-page__ongoing {
	border: 1rpx solid rgba(177, 187, 202, 0.45);
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.95);
	box-shadow: 0 16rpx 32rpx rgba(47, 58, 78, 0.08);
	box-sizing: border-box;
}

.exam-page__header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	padding: 34rpx 30rpx;
}

.exam-page__eyebrow,
.exam-page__section-kicker {
	display: block;
	font-size: 21rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #7a8495;
}

.exam-page__title {
	display: block;
	margin-top: 12rpx;
	font-size: 42rpx;
	line-height: 1.2;
	font-weight: 800;
	color: #2d3545;
}

.exam-page__subtitle {
	display: block;
	margin-top: 12rpx;
	max-width: 500rpx;
	font-size: 25rpx;
	line-height: 1.65;
	color: #6f7a8c;
}

.exam-page__vip-badge {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 16rpx;
	border-radius: 999rpx;
	background: #eef2f6;
	color: #687486;
	font-size: 20rpx;
	font-weight: 800;
}

.exam-page__vip-badge.is-active {
	background: #3f4859;
	color: #fff7e5;
}

.exam-page__ongoing {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 26rpx 28rpx;
	background: #30394a;
	border-color: rgba(255, 255, 255, 0.12);
}

.exam-page__ongoing .exam-page__section-kicker {
	color: rgba(236, 241, 248, 0.68);
}

.exam-page__ongoing-title {
	display: block;
	margin-top: 8rpx;
	font-size: 30rpx;
	font-weight: 800;
	color: #ffffff;
}

.exam-page__ongoing-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	color: rgba(236, 241, 248, 0.72);
}

.exam-page__small-button,
.exam-page__primary-button,
.exam-page__vip-dialog-button {
	margin: 0;
	border: 0;
	border-radius: 16rpx;
	background: #4f5c72;
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 700;
	line-height: 1;
}

.exam-page__small-button {
	flex: 0 0 auto;
	padding: 22rpx 24rpx;
	background: #fff7e5;
	color: #3b4656;
}

.exam-page__panel {
	position: relative;
	padding: 28rpx;
}

.exam-page__panel.is-locked {
	filter: saturate(0.72);
}

.exam-page__section-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 22rpx;
}

.exam-page__section-title {
	display: block;
	margin-top: 8rpx;
	font-size: 31rpx;
	font-weight: 800;
	color: #2f3747;
}

.exam-page__text-button {
	margin: 0;
	padding: 0;
	border: 0;
	background: transparent;
	color: #59677c;
	font-size: 24rpx;
	line-height: 1;
}

.exam-page__preset-grid {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.exam-page__preset {
	padding: 22rpx;
	border: 2rpx solid #e3e9f0;
	border-radius: 18rpx;
	background: #f9fbfd;
	box-sizing: border-box;
}

.exam-page__preset.is-active {
	border-color: #59677e;
	background: #eef3f8;
}

.exam-page__preset-top,
.exam-page__history-item,
.exam-page__duration-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.exam-page__preset-title,
.exam-page__history-title {
	font-size: 28rpx;
	font-weight: 800;
	color: #303849;
}

.exam-page__preset-time {
	font-size: 22rpx;
	font-weight: 700;
	color: #68758a;
}

.exam-page__preset-desc {
	display: block;
	margin-top: 12rpx;
	font-size: 24rpx;
	line-height: 1.55;
	color: #6f7b8d;
}

.exam-page__preset-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 16rpx;
}

.exam-page__preset-meta text {
	padding: 7rpx 12rpx;
	border-radius: 999rpx;
	background: #e8eef5;
	font-size: 21rpx;
	color: #596779;
}

.exam-page__config-block {
	margin-top: 26rpx;
}

.exam-page__config-label {
	display: block;
	margin-bottom: 14rpx;
	font-size: 24rpx;
	font-weight: 800;
	color: #3d4658;
}

.exam-page__chips {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
}

.exam-page__chip {
	padding: 15rpx 20rpx;
	border: 1rpx solid #dce4ed;
	border-radius: 999rpx;
	background: #ffffff;
	font-size: 24rpx;
	color: #596679;
}

.exam-page__chip.is-active {
	border-color: #4f5d73;
	background: #4f5d73;
	color: #ffffff;
}

.exam-page__duration-row {
	justify-content: flex-start;
	gap: 16rpx;
	width: 280rpx;
	padding: 0 18rpx;
	border: 1rpx solid #dce4ed;
	border-radius: 16rpx;
	background: #ffffff;
}

.exam-page__duration-input {
	width: 140rpx;
	height: 76rpx;
	font-size: 30rpx;
	font-weight: 800;
	color: #2f3747;
}

.exam-page__duration-unit {
	font-size: 24rpx;
	color: #718096;
}

.exam-page__input-placeholder {
	color: #b2bccb;
}

.exam-page__primary-button {
	width: 100%;
	height: 88rpx;
	margin-top: 30rpx;
	background: #344052;
}

.exam-page__primary-button[disabled] {
	background: #b8c0cd;
	color: #eef2f7;
}

.exam-page__history {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.exam-page__history-item {
	padding: 20rpx 0;
	border-bottom: 1rpx solid #edf1f5;
}

.exam-page__history-item:last-child {
	border-bottom: 0;
}

.exam-page__history-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	color: #7b8698;
}

.exam-page__history-score {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 24rpx;
	font-weight: 800;
	color: #4a5668;
}

.exam-page__empty {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 130rpx;
	border-radius: 18rpx;
	background: #f7f9fc;
	color: #7b8698;
	font-size: 25rpx;
}

.exam-page__loading-mask,
.exam-page__vip-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	z-index: 20;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(42, 50, 63, 0.34);
	padding: 44rpx;
	box-sizing: border-box;
}

.exam-page__loading-card,
.exam-page__vip-dialog {
	width: 100%;
	max-width: 560rpx;
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 24rpx 56rpx rgba(36, 45, 61, 0.22);
	box-sizing: border-box;
}

.exam-page__loading-card {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 18rpx;
	padding: 34rpx;
	font-size: 26rpx;
	color: #3f4a5c;
}

.exam-page__spinner {
	width: 34rpx;
	height: 34rpx;
	border: 4rpx solid #d7dee8;
	border-top-color: #4f5d73;
	border-radius: 50%;
	animation: exam-spin 0.8s linear infinite;
}

.exam-page__vip-dialog {
	padding: 42rpx 36rpx;
	text-align: center;
}

.exam-page__vip-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 76rpx;
	height: 76rpx;
	margin: 0 auto 22rpx;
	border-radius: 20rpx;
	background: #3f4859;
}

.exam-page__vip-dialog-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.3;
	font-weight: 800;
	color: #2f3747;
}

.exam-page__vip-dialog-desc {
	display: block;
	margin-top: 14rpx;
	font-size: 25rpx;
	line-height: 1.65;
	color: #6f7b8d;
}

.exam-page__vip-dialog-button {
	width: 100%;
	height: 84rpx;
	margin-top: 28rpx;
	background: #344052;
}

@keyframes exam-spin {
	to {
		transform: rotate(360deg);
	}
}
</style>
