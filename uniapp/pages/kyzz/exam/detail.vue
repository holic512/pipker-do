<template>
	<!-- AI 索引: KYZZ VIP 考试只读试卷详情页 -->
	<view class="exam-detail">
		<view class="exam-detail__content">
			<view class="exam-detail__inner">
				<view class="exam-detail__header">
				<view class="exam-detail__header-main">
					<text class="exam-detail__title">{{ summaryText }}</text>
					<text class="exam-detail__subtitle">
						{{ statusText }} · {{ formatScore(summary?.totalScore) }} 分 · {{ answeredCount }}/{{ totalQuestionCount }} 题
					</text>
				</view>
				</view>

			<view v-if="questions.length" class="exam-detail__progress-panel">
				<view class="exam-detail__progress-row">
					<view class="exam-detail__progress-bar">
						<view
							class="exam-detail__progress-fill"
							:class="{ 'is-complete': isAnswerProgressComplete }"
							:style="answerProgressStyle"
						></view>
					</view>
					<text class="exam-detail__progress-percent" :class="{ 'is-complete': isAnswerProgressComplete }">
						{{ answerProgressPercent }}%
					</text>
					<button class="exam-detail__switch-button" aria-label="选择题目" @tap="openQuestionPicker">
						<uni-icons type="bottom" size="12" :color="isAnswerProgressComplete ? '#2f9f62' : '#59677c'" />
					</button>
				</view>
			</view>

			<view v-if="currentQuestion" class="exam-detail__question-card">
				<view class="exam-detail__question-head">
					<view>
						<text class="exam-detail__question-kicker">{{ questionTypeLabel(currentQuestion.questionType) }}</text>
						<text class="exam-detail__question-title">第 {{ currentIndex + 1 }} 题</text>
					</view>
					<text class="exam-detail__score">{{ formatScore(currentQuestion.score) }} 分</text>
				</view>

				<text class="exam-detail__stem">{{ currentQuestion.stem }}</text>

				<view v-if="isChoiceQuestion(currentQuestion)" class="exam-detail__options">
					<view
						v-for="option in currentQuestion.options"
						:key="option.optionKey"
						class="exam-detail__option"
						:class="{ 'is-selected': currentQuestion.selectedOptionKeys.includes(option.optionKey) }"
					>
						<view class="exam-detail__option-key">
							<text>{{ option.optionKey }}</text>
						</view>
						<text class="exam-detail__option-text">{{ option.optionContent }}</text>
					</view>
				</view>

				<view class="exam-detail__answer">
					<text class="exam-detail__answer-label">我的答案</text>
					<text class="exam-detail__answer-content">{{ answerDisplay(currentQuestion) }}</text>
				</view>
			</view>

			<view v-else-if="!loading" class="exam-detail__empty">
				<text>没有可查看的题目</text>
			</view>

			<view class="exam-detail__footer">
				<button class="exam-detail__footer-button" :disabled="currentIndex <= 0" @tap="goPrevious">
					上一题
				</button>
				<button class="exam-detail__footer-button" :disabled="currentIndex >= questions.length - 1" @tap="goNext">
					下一题
				</button>
			</view>

			<uni-popup
				ref="questionPickerPopup"
				type="bottom"
				background-color="#ffffff"
				border-radius="28rpx 28rpx 0 0"
				:is-mask-click="true"
			>
				<view class="exam-detail__picker">
					<view class="exam-detail__picker-head">
						<view>
							<text class="exam-detail__picker-title">题目目录</text>
							<text class="exam-detail__picker-subtitle">已答 {{ answeredCount }}/{{ totalQuestionCount }} 题</text>
						</view>
						<button class="exam-detail__picker-close" @tap="closeQuestionPicker">收起</button>
					</view>
					<scroll-view class="exam-detail__picker-scroll" scroll-y>
						<view class="exam-detail__picker-grid">
							<view
								v-for="(item, index) in questions"
								:key="item.questionId"
								class="exam-detail__picker-item"
								:class="{
									'is-active': index === currentIndex,
									'is-answered': isAnswered(item)
								}"
								@tap="selectQuestion(index)"
							>
								<text>{{ index + 1 }}</text>
							</view>
						</view>
					</scroll-view>
				</view>
			</uni-popup>

			<view v-if="loading" class="exam-detail__loading">
				<view class="exam-detail__loading-card">
					<view class="exam-detail__spinner"></view>
					<text>正在加载试卷详情</text>
				</view>
			</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getExamDetail } from '@/pages/kyzz/api/exam'
import type { KyzzExamDetailResponse, KyzzExamQuestion, KyzzExamSummary, UniPopupRef } from '@/pages/kyzz/exam/types'

interface ExamDetailState {
	sessionId: number
	loading: boolean
	detail: KyzzExamDetailResponse | null
	currentIndex: number
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
	name: 'ExamDetailPage',
	data(): ExamDetailState {
		return {
			sessionId: 0,
			loading: false,
			detail: null,
			currentIndex: 0
		}
	},
	computed: {
		summary(): KyzzExamSummary | null {
			return this.detail?.summary || null
		},
		questions(): KyzzExamQuestion[] {
			return this.detail?.questions || []
		},
		currentQuestion(): KyzzExamQuestion | null {
			return this.questions[this.currentIndex] || null
		},
		summaryText(): string {
			return this.summary?.examTypeLabel || '试卷详情'
		},
		statusText(): string {
			return this.summary?.statusLabel || '加载中'
		},
		answeredCount(): number {
			return toNumber(this.summary?.answeredCount)
		},
		totalQuestionCount(): number {
			return toNumber(this.summary?.totalQuestionCount)
		},
		answerProgressPercent(): number {
			const total = Math.max(1, this.totalQuestionCount)
			const answered = Math.min(total, Math.max(0, this.answeredCount))
			return Math.round((answered / total) * 100)
		},
		isAnswerProgressComplete(): boolean {
			return this.totalQuestionCount > 0 && this.answerProgressPercent >= 100
		},
		answerProgressStyle(): string {
			return `width: ${this.answerProgressPercent}%`
		}
	},
	onLoad(query: Record<string, string | undefined>) {
		this.sessionId = toNumber(query.sessionId)
		this.loadDetail()
	},
	methods: {
		async loadDetail(): Promise<void> {
			if (!this.sessionId || this.loading) {
				return
			}
			this.loading = true
			try {
				await bootstrapAuth({ silent: true })
				this.detail = await getExamDetail(this.sessionId)
				if (this.currentIndex >= this.questions.length) {
					this.currentIndex = 0
				}
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '试卷详情加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		isChoiceQuestion(question: KyzzExamQuestion): boolean {
			return question.questionType === 'single' || question.questionType === 'multiple'
		},
		isAnswered(question: KyzzExamQuestion | null): boolean {
			return !!question && toNumber(question.answerStatus) === 1
		},
		questionTypeLabel(questionType: string): string {
			if (questionType === 'single') return '单选题'
			if (questionType === 'multiple') return '多选题'
			if (questionType === 'short') return '简答题'
			return '题目'
		},
		answerDisplay(question: KyzzExamQuestion): string {
			if (this.isChoiceQuestion(question)) {
				return question.selectedOptionKeys.length ? question.selectedOptionKeys.join('、') : '未作答'
			}
			return question.answerText && question.answerText.trim() ? question.answerText : '未作答'
		},
		formatScore(value: unknown): number {
			return toNumber(value)
		},
		openQuestionPicker(): void {
			;(this.$refs.questionPickerPopup as UniPopupRef | undefined)?.open()
		},
		closeQuestionPicker(): void {
			;(this.$refs.questionPickerPopup as UniPopupRef | undefined)?.close()
		},
		selectQuestion(index: number): void {
			if (index < 0 || index >= this.questions.length) {
				return
			}
			this.currentIndex = index
			this.closeQuestionPicker()
		},
		goPrevious(): void {
			if (this.currentIndex > 0) {
				this.currentIndex -= 1
			}
		},
		goNext(): void {
			if (this.currentIndex < this.questions.length - 1) {
				this.currentIndex += 1
			}
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.exam-detail {
	min-height: 100vh;
	background: #f4f7fb;
	box-sizing: border-box;
}

.exam-detail__content {
	padding: 0 28rpx calc(env(safe-area-inset-bottom) + 120rpx);
}

.exam-detail__inner {
	position: relative;
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	padding-top: 24rpx;
}

.exam-detail__header,
.exam-detail__question-card,
.exam-detail__empty {
	border: 1rpx solid rgba(177, 187, 202, 0.45);
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 32rpx rgba(47, 58, 78, 0.08);
	box-sizing: border-box;
}

.exam-detail__header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18rpx;
	padding: 28rpx;
}

.exam-detail__header-main {
	flex: 1;
	min-width: 0;
}

.exam-detail__title {
	display: block;
	font-size: 34rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #2f3747;
}

.exam-detail__subtitle {
	display: block;
	margin-top: 10rpx;
	font-size: 23rpx;
	line-height: 1.45;
	color: #728096;
}

.exam-detail__switch-button,
.exam-detail__picker-close,
.exam-detail__footer-button {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
	margin: 0;
	padding: 0;
	border: 0;
	text-align: center;
	line-height: 1.2;
}

.exam-detail__switch-button,
.exam-detail__picker-close {
	flex: 0 0 auto;
	border-radius: 999rpx;
	background: #eef3f8;
	color: #4f5d73;
	font-size: 23rpx;
	font-weight: 800;
}

.exam-detail__switch-button {
	width: 44rpx;
	height: 44rpx;
	padding: 0;
}

.exam-detail__switch-button::after,
.exam-detail__picker-close::after,
.exam-detail__footer-button::after {
	border: 0;
}

.exam-detail__progress-panel {
	padding: 0 4rpx;
	box-sizing: border-box;
}

.exam-detail__progress-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.exam-detail__progress-bar {
	flex: 1;
	min-width: 0;
	height: 8rpx;
	overflow: hidden;
	border-radius: 999rpx;
	background: #e8eef6;
}

.exam-detail__progress-fill {
	height: 100%;
	border-radius: 999rpx;
	background: #2f8cff;
	transition: width 0.2s ease;
}

.exam-detail__progress-fill.is-complete {
	background: #2f9f62;
}

.exam-detail__progress-percent {
	flex: 0 0 auto;
	min-width: 58rpx;
	font-size: 22rpx;
	line-height: 1;
	color: #667386;
	text-align: right;
}

.exam-detail__progress-percent.is-complete {
	color: #2f9f62;
	font-weight: 800;
}

.exam-detail__question-card {
	padding: 30rpx 28rpx;
}

.exam-detail__question-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 24rpx;
}

.exam-detail__question-kicker {
	display: block;
	font-size: 22rpx;
	font-weight: 800;
	color: #728096;
}

.exam-detail__question-title {
	display: block;
	margin-top: 8rpx;
	font-size: 34rpx;
	font-weight: 800;
	color: #2f3747;
}

.exam-detail__score {
	flex: 0 0 auto;
	padding: 10rpx 14rpx;
	border-radius: 999rpx;
	background: #eef3f8;
	font-size: 23rpx;
	font-weight: 800;
	color: #59677c;
}

.exam-detail__stem {
	display: block;
	font-size: 30rpx;
	line-height: 1.8;
	color: #2f3747;
	white-space: pre-wrap;
}

.exam-detail__options {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 28rpx;
}

.exam-detail__option {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 22rpx;
	border: 2rpx solid #e2e8f0;
	border-radius: 18rpx;
	background: #f9fbfd;
	box-sizing: border-box;
}

.exam-detail__option.is-selected {
	border-color: #4f5d73;
	background: #eef3f8;
}

.exam-detail__option-key {
	display: flex;
	align-items: center;
	justify-content: center;
	flex: 0 0 auto;
	width: 46rpx;
	height: 46rpx;
	border-radius: 50%;
	background: #dfe7f0;
	font-size: 24rpx;
	font-weight: 800;
	color: #4c5a6e;
}

.exam-detail__option.is-selected .exam-detail__option-key {
	background: #344052;
	color: #ffffff;
}

.exam-detail__option-text {
	flex: 1;
	font-size: 27rpx;
	line-height: 1.65;
	color: #3d4658;
}

.exam-detail__answer {
	margin-top: 28rpx;
	padding: 22rpx;
	border-radius: 18rpx;
	background: #f7f9fc;
	box-sizing: border-box;
}

.exam-detail__answer-label {
	display: block;
	font-size: 22rpx;
	font-weight: 800;
	color: #728096;
}

.exam-detail__answer-content {
	display: block;
	margin-top: 10rpx;
	font-size: 27rpx;
	line-height: 1.65;
	color: #303849;
	white-space: pre-wrap;
}

.exam-detail__footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 10;
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 14rpx;
	padding: 18rpx 28rpx calc(env(safe-area-inset-bottom) + 18rpx);
	background: rgba(244, 247, 251, 0.96);
	box-shadow: 0 -12rpx 28rpx rgba(46, 56, 74, 0.08);
	box-sizing: border-box;
}

.exam-detail__footer-button {
	height: 76rpx;
	border-radius: 16rpx;
	background: #ffffff;
	color: #4f5d73;
	font-size: 26rpx;
	font-weight: 800;
}

.exam-detail__footer-button[disabled] {
	background: #c6ced9;
	color: #f2f5f8;
}

.exam-detail__picker {
	padding: 28rpx 24rpx calc(env(safe-area-inset-bottom) + 32rpx);
	box-sizing: border-box;
}

.exam-detail__picker-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
}

.exam-detail__picker-title {
	display: block;
	font-size: 31rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #303849;
}

.exam-detail__picker-subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	color: #738097;
}

.exam-detail__picker-close {
	height: 56rpx;
	padding: 0 18rpx;
}

.exam-detail__picker-scroll {
	max-height: 520rpx;
	margin-top: 24rpx;
}

.exam-detail__picker-grid {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	gap: 14rpx;
	padding-bottom: 6rpx;
}

.exam-detail__picker-item {
	display: flex;
	align-items: center;
	justify-content: center;
	aspect-ratio: 1;
	border: 1rpx solid #dce4ed;
	border-radius: 16rpx;
	background: #f9fbfd;
	font-size: 25rpx;
	font-weight: 800;
	color: #667386;
	box-sizing: border-box;
}

.exam-detail__picker-item.is-answered {
	background: #e7eee8;
	border-color: #b9cbbd;
	color: #4f7258;
}

.exam-detail__picker-item.is-active {
	background: #344052;
	border-color: #344052;
	color: #ffffff;
}

.exam-detail__empty {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 260rpx;
	color: #728096;
	font-size: 26rpx;
}

.exam-detail__loading {
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

.exam-detail__loading-card {
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

.exam-detail__spinner {
	width: 34rpx;
	height: 34rpx;
	border: 4rpx solid #d7dee8;
	border-top-color: #4f5d73;
	border-radius: 50%;
	animation: exam-detail-spin 0.8s linear infinite;
}

@keyframes exam-detail-spin {
	to {
		transform: rotate(360deg);
	}
}
</style>
