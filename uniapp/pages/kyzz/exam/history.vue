<template>
	<!-- AI 索引: KYZZ VIP 考试历史页 -->
	<view class="exam-history">
		<view class="exam-history__content">
			<view class="exam-history__inner">
				<view class="exam-history__toolbar">
					<text class="exam-history__toolbar-meta">最近 {{ records.length }} 次考试</text>
					<button class="exam-history__refresh" :disabled="loading" @tap="loadHistory">
						<uni-icons type="refreshempty" size="15" color="#4f5d73" />
						<text>刷新</text>
					</button>
				</view>

			<view v-if="records.length" class="exam-history__list">
				<view
					v-for="record in records"
					:key="record.sessionId"
					class="exam-history__item"
					@tap="openDetail(record.sessionId)"
				>
					<view class="exam-history__item-main">
						<view class="exam-history__item-head">
							<text class="exam-history__item-title">{{ record.examTypeLabel }}</text>
							<text class="exam-history__status" :class="statusClass(record.status)">{{ record.statusLabel }}</text>
						</view>
						<text class="exam-history__item-desc">
							{{ record.examNo }} · {{ record.difficultyLabel }}
						</text>
						<view class="exam-history__metrics">
							<view class="exam-history__metric">
								<text class="exam-history__metric-label">用时</text>
								<text class="exam-history__metric-value">{{ formatUsedDuration(record) }}</text>
							</view>
							<view class="exam-history__metric">
								<text class="exam-history__metric-label">成绩</text>
								<text class="exam-history__metric-value">{{ formatScore(record) }}</text>
							</view>
							<view class="exam-history__metric">
								<text class="exam-history__metric-label">完成度</text>
								<text class="exam-history__metric-value">{{ formatProgress(record) }}</text>
							</view>
						</view>
						<view class="exam-history__progress">
							<view class="exam-history__progress-bar">
								<view class="exam-history__progress-fill" :style="progressStyle(record)"></view>
							</view>
							<text>{{ formatCount(record.answeredCount) }}/{{ formatCount(record.totalQuestionCount) }}</text>
						</view>
						<view class="exam-history__time-row">
							<text>开始 {{ record.startedAt || '未记录' }}</text>
							<text>{{ finishTimeLabel(record) }}</text>
						</view>
					</view>
					<uni-icons type="right" size="16" color="#9aa4b5" />
				</view>
			</view>

			<view v-else class="exam-history__empty">
				<view class="exam-history__empty-icon">
					<uni-icons type="calendar" size="30" color="#6a778a" />
				</view>
				<text class="exam-history__empty-title">{{ loading ? '正在同步历史' : '还没有考试记录' }}</text>
				<text class="exam-history__empty-desc">完成一次模拟考试后，试卷会显示在这里。</text>
			</view>

			<view v-if="loading" class="exam-history__loading-mask">
				<view class="exam-history__loading-card">
					<view class="exam-history__spinner"></view>
					<text>正在加载考试历史</text>
				</view>
			</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getExamHistory } from '@/pages/kyzz/api/exam'
import type { KyzzExamSummary } from '@/pages/kyzz/exam/types'

interface ExamHistoryState {
	loading: boolean
	records: KyzzExamSummary[]
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
	name: 'ExamHistoryPage',
	data(): ExamHistoryState {
		return {
			loading: false,
			records: []
		}
	},
	onShow() {
		this.loadHistory()
	},
	methods: {
		async loadHistory(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				await bootstrapAuth({ silent: true })
				this.records = await getExamHistory(50)
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '考试历史加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		openDetail(sessionId: number): void {
			if (!sessionId) {
				return
			}
			uni.navigateTo({
				url: `/pages/kyzz/exam/detail?sessionId=${sessionId}`
			})
		},
		formatCount(value: unknown): number {
			return toNumber(value)
		},
		formatMinutes(value: unknown): string {
			return `${toNumber(value)} 分钟`
		},
		formatUsedDuration(record: KyzzExamSummary): string {
			if (record.status === 'in_progress') {
				const remaining = Math.max(0, toNumber(record.remainingSeconds))
				const totalSeconds = Math.max(0, toNumber(record.durationMinutes) * 60)
				return this.formatDurationSeconds(Math.max(0, totalSeconds - remaining))
			}
			if (!record.startedAt) {
				return '--'
			}
			const endText = record.submittedAt || record.deadlineAt
			if (!endText) {
				return this.formatMinutes(record.durationMinutes)
			}
			const startedAt = new Date(record.startedAt.replace(/-/g, '/')).getTime()
			const endedAt = new Date(endText.replace(/-/g, '/')).getTime()
			if (!Number.isFinite(startedAt) || !Number.isFinite(endedAt) || endedAt <= startedAt) {
				return this.formatMinutes(record.durationMinutes)
			}
			return this.formatDurationSeconds(Math.round((endedAt - startedAt) / 1000))
		},
		formatDurationSeconds(secondsValue: number): string {
			const seconds = Math.max(0, secondsValue)
			const hours = Math.floor(seconds / 3600)
			const minutes = Math.floor((seconds % 3600) / 60)
			if (hours > 0) {
				return `${hours}时${minutes}分`
			}
			return minutes > 0 ? `${minutes}分钟` : '不足1分钟'
		},
		formatScore(record: KyzzExamSummary): string {
			const total = toNumber(record.totalScore)
			if (record.status === 'in_progress') {
				return `进行中 / 满分 ${total} 分`
			}
			if (record.gradingStatus === 'graded') {
				return `${toNumber(record.earnedScore)} / ${total} 分`
			}
			if (record.gradingStatus === 'failed') {
				return `阅卷失败 / 满分 ${total} 分`
			}
			if (record.gradingStatus === 'grading') {
				return `阅卷中 / 满分 ${total} 分`
			}
			return `待阅卷 / 满分 ${total} 分`
		},
		formatProgress(record: KyzzExamSummary): string {
			const total = Math.max(1, toNumber(record.totalQuestionCount))
			const answered = Math.min(total, Math.max(0, toNumber(record.answeredCount)))
			return `${Math.round((answered / total) * 100)}%`
		},
		finishTimeLabel(record: KyzzExamSummary): string {
			if (record.submittedAt) {
				return `交卷 ${record.submittedAt}`
			}
			if (record.status === 'expired') {
				return `截止 ${record.deadlineAt || '未记录'}`
			}
			return `截止 ${record.deadlineAt || '未记录'}`
		},
		progressStyle(record: KyzzExamSummary): string {
			const total = Math.max(1, toNumber(record.totalQuestionCount))
			const answered = Math.min(total, Math.max(0, toNumber(record.answeredCount)))
			return `width: ${(answered / total) * 100}%`
		},
		statusClass(status: string): string {
			if (status === 'submitted') {
				return 'is-submitted'
			}
			if (status === 'expired') {
				return 'is-expired'
			}
			return 'is-progress'
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.exam-history {
	min-height: 100vh;
	background: linear-gradient(180deg, #f6f8fb 0%, #edf2f7 100%);
	box-sizing: border-box;
}

.exam-history__content {
	padding: 0 28rpx calc(env(safe-area-inset-bottom) + 42rpx);
}

.exam-history__inner {
	position: relative;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 28rpx;
}

.exam-history__toolbar,
.exam-history__item,
.exam-history__empty {
	border: 1rpx solid rgba(177, 187, 202, 0.45);
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 16rpx 32rpx rgba(47, 58, 78, 0.08);
	box-sizing: border-box;
}

.exam-history__toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
	padding: 22rpx 24rpx;
}

.exam-history__toolbar-meta {
	font-size: 25rpx;
	line-height: 1.3;
	font-weight: 800;
	color: #4f5d73;
}

.exam-history__refresh {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 6rpx;
	flex: 0 0 auto;
	height: 58rpx;
	margin: 0;
	padding: 0 16rpx;
	border: 0;
	border-radius: 999rpx;
	background: #eef3f8;
	color: #4f5d73;
	font-size: 22rpx;
	font-weight: 800;
	line-height: 1.2;
	box-sizing: border-box;
}

.exam-history__refresh::after {
	border: 0;
}

.exam-history__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.exam-history__item {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 24rpx;
}

.exam-history__item-main {
	flex: 1;
	min-width: 0;
}

.exam-history__item-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 14rpx;
}

.exam-history__item-title {
	font-size: 29rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #303849;
}

.exam-history__status {
	flex: 0 0 auto;
	padding: 7rpx 12rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 800;
}

.exam-history__status.is-progress {
	background: #e7edf6;
	color: #4f5d73;
}

.exam-history__status.is-submitted {
	background: #e7eee8;
	color: #4f7258;
}

.exam-history__status.is-expired {
	background: #f5e5e4;
	color: #b46a67;
}

.exam-history__item-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 23rpx;
	line-height: 1.45;
	color: #7a8596;
}

.exam-history__metrics {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 12rpx;
	margin-top: 18rpx;
}

.exam-history__metric {
	min-width: 0;
	padding: 16rpx 14rpx;
	border-radius: 16rpx;
	background: #f7f9fc;
	box-sizing: border-box;
}

.exam-history__metric-label {
	display: block;
	font-size: 20rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #8792a4;
}

.exam-history__metric-value {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	line-height: 1.32;
	font-weight: 800;
	color: #303849;
	word-break: break-all;
}

.exam-history__progress {
	display: flex;
	align-items: center;
	gap: 14rpx;
	margin-top: 18rpx;
	font-size: 22rpx;
	font-weight: 800;
	color: #5c687a;
}

.exam-history__progress-bar {
	flex: 1;
	height: 10rpx;
	overflow: hidden;
	border-radius: 999rpx;
	background: #edf2f7;
}

.exam-history__progress-fill {
	height: 100%;
	border-radius: 999rpx;
	background: #4f5d73;
}

.exam-history__time-row {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	margin-top: 14rpx;
	font-size: 21rpx;
	line-height: 1.4;
	color: #8792a4;
}

.exam-history__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	min-height: 360rpx;
	padding: 42rpx;
	text-align: center;
}

.exam-history__empty-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 84rpx;
	height: 84rpx;
	border-radius: 22rpx;
	background: #eef3f8;
}

.exam-history__empty-title {
	display: block;
	margin-top: 22rpx;
	font-size: 30rpx;
	font-weight: 800;
	color: #303849;
}

.exam-history__empty-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	line-height: 1.55;
	color: #7a8596;
}

.exam-history__loading-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	z-index: 20;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(42, 50, 63, 0.28);
}

.exam-history__loading-card {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 34rpx;
	border-radius: 22rpx;
	background: #ffffff;
	box-shadow: 0 24rpx 56rpx rgba(36, 45, 61, 0.2);
	color: #3f4a5c;
	font-size: 26rpx;
}

.exam-history__spinner {
	width: 34rpx;
	height: 34rpx;
	border: 4rpx solid #d7dee8;
	border-top-color: #4f5d73;
	border-radius: 50%;
	animation: exam-history-spin 0.8s linear infinite;
}

@keyframes exam-history-spin {
	to {
		transform: rotate(360deg);
	}
}
</style>
