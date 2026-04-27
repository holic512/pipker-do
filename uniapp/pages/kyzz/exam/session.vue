<template>
	<!-- AI 索引: KYZZ VIP 独立考试答题页 -->
	<view class="exam-session">
		<view class="exam-session__content">
			<view class="exam-session__inner">
			<view class="exam-session__topbar">
				<view class="exam-session__top-main">
					<text class="exam-session__type">{{ summaryText }}</text>
					<text class="exam-session__meta">
						{{ answeredCount }}/{{ totalQuestionCount }} 题 · {{ statusText }}
					</text>
				</view>
				<view class="exam-session__timer" :class="{ 'is-danger': remainingSeconds <= 300 }">
					<uni-icons type="calendar" size="16" :color="remainingSeconds <= 300 ? '#b46a67' : '#4d5a70'" />
					<text>{{ remainingText }}</text>
				</view>
			</view>

			<view v-if="questions.length" class="exam-session__progress-panel">
				<view class="exam-session__progress-row">
					<view class="exam-session__progress-bar">
						<view
							class="exam-session__progress-fill"
							:class="{ 'is-complete': isAnswerProgressComplete }"
							:style="answerProgressStyle"
						></view>
					</view>
					<text class="exam-session__progress-percent" :class="{ 'is-complete': isAnswerProgressComplete }">
						{{ answerProgressPercent }}%
					</text>
					<button class="exam-session__switch-button" aria-label="选择题目" @tap="openQuestionPicker">
						<uni-icons type="bottom" size="12" :color="isAnswerProgressComplete ? '#2f9f62' : '#59677c'" />
					</button>
				</view>
			</view>

			<uni-popup
				ref="questionPickerPopup"
				type="bottom"
				background-color="#ffffff"
				border-radius="28rpx 28rpx 0 0"
				:is-mask-click="true"
			>
				<view class="exam-session__picker">
					<view class="exam-session__picker-head">
						<view>
							<text class="exam-session__picker-title">选择题目</text>
							<text class="exam-session__picker-subtitle">已答 {{ answeredCount }}/{{ totalQuestionCount }} 题</text>
						</view>
						<button class="exam-session__picker-close" @tap="closeQuestionPicker">收起</button>
					</view>
					<view class="exam-session__picker-legend">
						<text>当前题</text>
						<text>已答</text>
						<text>未答</text>
					</view>
					<scroll-view class="exam-session__picker-scroll" scroll-y>
						<view class="exam-session__picker-grid">
							<view
								v-for="(item, index) in questions"
								:key="item.questionId"
								class="exam-session__picker-item"
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

			<view v-if="currentQuestion" class="exam-session__question-card">
				<view class="exam-session__question-head">
					<view>
						<text class="exam-session__question-kicker">{{ questionTypeLabel(currentQuestion.questionType) }}</text>
						<text class="exam-session__question-title">第 {{ currentIndex + 1 }} 题</text>
					</view>
					<text class="exam-session__score">{{ formatScore(currentQuestion.score) }} 分</text>
				</view>

				<text class="exam-session__stem">{{ currentQuestion.stem }}</text>

				<view v-if="isChoiceQuestion(currentQuestion)" class="exam-session__options">
					<view
						v-for="option in currentQuestion.options"
						:key="option.optionKey"
						class="exam-session__option"
						:class="{ 'is-selected': currentQuestion.selectedOptionKeys.includes(option.optionKey) }"
						@tap="toggleOption(option.optionKey)"
					>
						<view class="exam-session__option-key">
							<text>{{ option.optionKey }}</text>
						</view>
						<text class="exam-session__option-text">{{ option.optionContent }}</text>
					</view>
				</view>

				<view v-else class="exam-session__short-answer">
					<textarea
						v-model="answerDraft"
						class="exam-session__textarea"
						maxlength="3000"
						placeholder="在这里作答，暂不自动阅卷。"
						placeholder-class="exam-session__placeholder"
						:disabled="!canAnswer"
						@blur="saveCurrentAnswer"
					/>
					<button class="exam-session__save-button" :disabled="saving || !canAnswer" @tap="saveCurrentAnswer">
						{{ saving ? '保存中...' : '保存本题' }}
					</button>
				</view>
			</view>

			<view v-else-if="!loading" class="exam-session__empty">
				<text>没有可作答的题目</text>
			</view>

			<view class="exam-session__footer" :class="{ 'without-submit': !canSubmit }">
				<button class="exam-session__footer-button" :disabled="currentIndex <= 0" @tap="goPrevious">
					上一题
				</button>
				<button class="exam-session__footer-button" :disabled="currentIndex >= questions.length - 1" @tap="goNext">
					下一题
				</button>
				<button v-if="canSubmit" class="exam-session__submit-button" :disabled="submitting || !detail" @tap="confirmSubmit">
					{{ submitting ? '交卷中...' : '交卷' }}
				</button>
			</view>

			<view v-if="loading" class="exam-session__loading">
				<view class="exam-session__loading-card">
					<view class="exam-session__spinner"></view>
					<text>正在进入考试</text>
				</view>
			</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getExamDetail, saveExamAnswer, submitExam } from '@/pages/kyzz/api/exam'
import type { KyzzExamDetailResponse, KyzzExamQuestion, KyzzExamSummary, UniPopupRef } from '@/pages/kyzz/exam/types'

interface ExamSessionState {
	sessionId: number
	loading: boolean
	saving: boolean
	submitting: boolean
	detail: KyzzExamDetailResponse | null
	currentIndex: number
	answerDraft: string
	timer: number | null
	remainingSeconds: number
	questionStartedAt: number
	timeUpHandled: boolean
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
	name: 'ExamSessionPage',
	data(): ExamSessionState {
		return {
			sessionId: 0,
			loading: false,
			saving: false,
			submitting: false,
			detail: null,
			currentIndex: 0,
			answerDraft: '',
			timer: null,
			remainingSeconds: 0,
			questionStartedAt: Date.now(),
			timeUpHandled: false
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
		canAnswer(): boolean {
			return !!(this.detail?.canAnswer && this.remainingSeconds > 0)
		},
		canSubmit(): boolean {
			return !!(this.detail?.canSubmit && this.detail?.summary?.status === 'in_progress')
		},
		summaryText(): string {
			return this.summary?.examTypeLabel || '模拟考试'
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
		},
		remainingText(): string {
			const seconds = Math.max(0, this.remainingSeconds)
			const hours = Math.floor(seconds / 3600)
			const minutes = Math.floor((seconds % 3600) / 60)
			const restSeconds = seconds % 60
			if (hours > 0) {
				return `${hours}:${String(minutes).padStart(2, '0')}:${String(restSeconds).padStart(2, '0')}`
			}
			return `${minutes}:${String(restSeconds).padStart(2, '0')}`
		}
	},
	watch: {
		currentIndex() {
			this.syncAnswerDraft()
			this.questionStartedAt = Date.now()
		}
	},
	onLoad(query: Record<string, string | undefined>) {
		this.sessionId = toNumber(query.sessionId)
		this.loadDetail()
	},
	onUnload() {
		this.clearTimer()
	},
	methods: {
		async loadDetail(): Promise<void> {
			if (!this.sessionId || this.loading) {
				return
			}
			this.loading = true
			try {
				await bootstrapAuth({ silent: true })
				const detail = await getExamDetail(this.sessionId)
				this.detail = detail
				this.remainingSeconds = toNumber(detail.summary.remainingSeconds)
				if (this.currentIndex >= this.questions.length) {
					this.currentIndex = 0
				}
				this.syncAnswerDraft()
				this.startTimer()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '考试加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		startTimer(): void {
			this.clearTimer()
			this.timer = setInterval(() => {
				if (this.remainingSeconds > 0) {
					this.remainingSeconds -= 1
					return
				}
				this.handleTimeUp()
			}, 1000)
		},
		clearTimer(): void {
			if (this.timer) {
				clearInterval(this.timer)
				this.timer = null
			}
		},
		async handleTimeUp(): Promise<void> {
			if (this.timeUpHandled || !this.detail || this.detail.summary.status !== 'in_progress') {
				return
			}
			this.timeUpHandled = true
			uni.showToast({
				title: '时间到，正在交卷',
				icon: 'none'
			})
			await this.submitCurrentExam()
		},
		syncAnswerDraft(): void {
			const question = this.currentQuestion
			this.answerDraft = question && question.questionType === 'short' ? (question.answerText || '') : ''
		},
		isChoiceQuestion(question: KyzzExamQuestion): boolean {
			return question.questionType === 'single' || question.questionType === 'multiple'
		},
		isAnswered(question: KyzzExamQuestion): boolean {
			return toNumber(question.answerStatus) === 1
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
			if (index !== this.currentIndex) {
				this.saveCurrentAnswer()
				this.currentIndex = index
			}
			this.closeQuestionPicker()
		},
		questionTypeLabel(questionType: string): string {
			if (questionType === 'single') return '单选题'
			if (questionType === 'multiple') return '多选题'
			if (questionType === 'short') return '简答题'
			return '题目'
		},
		formatScore(value: unknown): number {
			return toNumber(value)
		},
		toggleOption(optionKey: string): void {
			const question = this.currentQuestion
			if (!question || !this.canAnswer || !this.isChoiceQuestion(question)) {
				return
			}
			if (question.questionType === 'single') {
				question.selectedOptionKeys = question.selectedOptionKeys.includes(optionKey) ? [] : [optionKey]
			} else {
				const selected = new Set(question.selectedOptionKeys)
				if (selected.has(optionKey)) {
					selected.delete(optionKey)
				} else {
					selected.add(optionKey)
				}
				question.selectedOptionKeys = Array.from(selected).sort()
			}
			this.saveCurrentAnswer()
		},
		async saveCurrentAnswer(): Promise<void> {
			const question = this.currentQuestion
			if (!question || !this.canAnswer || this.saving) {
				return
			}
			this.saving = true
			try {
				const usedSeconds = Math.max(0, Math.round((Date.now() - this.questionStartedAt) / 1000))
				const result = await saveExamAnswer(this.sessionId, question.questionId, {
					selectedOptionKeys: this.isChoiceQuestion(question) ? question.selectedOptionKeys : undefined,
					answerText: question.questionType === 'short' ? this.answerDraft : undefined,
					usedSeconds
				})
				if (question.questionType === 'short') {
					question.answerText = this.answerDraft
				}
				question.answerStatus = this.resolveQuestionAnswered(question) ? 1 : 0
				if (this.detail?.summary) {
					this.detail.summary.answeredCount = result.answeredCount
				}
				this.questionStartedAt = Date.now()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '答案保存失败'),
					icon: 'none'
				})
			} finally {
				this.saving = false
			}
		},
		resolveQuestionAnswered(question: KyzzExamQuestion): boolean {
			if (this.isChoiceQuestion(question)) {
				return question.selectedOptionKeys.length > 0
			}
			return !!this.answerDraft.trim()
		},
		goPrevious(): void {
			if (this.currentIndex > 0) {
				this.saveCurrentAnswer()
				this.currentIndex -= 1
			}
		},
		goNext(): void {
			if (this.currentIndex < this.questions.length - 1) {
				this.saveCurrentAnswer()
				this.currentIndex += 1
			}
		},
		confirmSubmit(): void {
			if (!this.detail || !this.canSubmit || this.submitting) {
				return
			}
			uni.showModal({
				title: '确认交卷',
				content: `当前已答 ${this.answeredCount}/${this.totalQuestionCount} 题，交卷后本次考试将结束。`,
				confirmText: '交卷',
				cancelText: '继续作答',
				confirmColor: '#344052',
				success: (result: { confirm?: boolean }) => {
					if (result.confirm) {
						this.submitCurrentExam()
					}
				}
			})
		},
		async submitCurrentExam(): Promise<void> {
			if (!this.sessionId || this.submitting) {
				return
			}
			this.submitting = true
			try {
				if (this.currentQuestion && this.canAnswer) {
					await this.saveCurrentAnswer()
				}
				const summary = await submitExam(this.sessionId)
				if (this.detail) {
					this.detail.summary = summary
					this.detail.canAnswer = false
					this.detail.canSubmit = false
				}
				this.remainingSeconds = 0
				this.clearTimer()
				uni.showToast({
					title: '已交卷，正在阅卷',
					icon: 'none'
				})
				setTimeout(() => {
					uni.redirectTo({
						url: `/pages/kyzz/exam/detail?sessionId=${this.sessionId}`
					})
				}, 700)
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '交卷失败'),
					icon: 'none'
				})
			} finally {
				this.submitting = false
			}
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.exam-session {
	min-height: 100vh;
	background: #f4f7fb;
	box-sizing: border-box;
}

.exam-session__content {
	padding: 0 28rpx calc(env(safe-area-inset-bottom) + 120rpx);
}

.exam-session__inner {
	position: relative;
	padding-top: 24rpx;
}

.exam-session__topbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 26rpx 28rpx;
	border: 1rpx solid rgba(177, 187, 202, 0.45);
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 32rpx rgba(47, 58, 78, 0.08);
}

.exam-session__top-main {
	min-width: 0;
}

.exam-session__type {
	display: block;
	font-size: 31rpx;
	font-weight: 800;
	color: #2f3747;
}

.exam-session__meta {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	color: #728096;
}

.exam-session__timer {
	display: flex;
	align-items: center;
	gap: 8rpx;
	flex: 0 0 auto;
	padding: 14rpx 16rpx;
	border-radius: 999rpx;
	background: #eef3f8;
	font-size: 25rpx;
	font-weight: 800;
	color: #4d5a70;
}

.exam-session__timer.is-danger {
	background: #f5e5e4;
	color: #b46a67;
}

.exam-session__progress-panel {
	margin-top: 18rpx;
	padding: 0 4rpx;
	box-sizing: border-box;
}

.exam-session__progress-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.exam-session__switch-button,
.exam-session__picker-close {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
	margin: 0;
	padding: 0;
	border: 0;
	border-radius: 999rpx;
	background: #eef3f8;
	color: #4f5d73;
	font-weight: 800;
	line-height: 1.2;
	text-align: center;
}

.exam-session__switch-button {
	flex: 0 0 auto;
	width: 44rpx;
	height: 44rpx;
	padding: 0;
}

.exam-session__switch-button::after,
.exam-session__picker-close::after {
	border: 0;
}

.exam-session__progress-bar {
	flex: 1;
	min-width: 0;
	height: 8rpx;
	overflow: hidden;
	border-radius: 999rpx;
	background: #e8eef6;
}

.exam-session__progress-fill {
	height: 100%;
	border-radius: 999rpx;
	background: #2f8cff;
	transition: width 0.2s ease;
}

.exam-session__progress-fill.is-complete {
	background: #2f9f62;
}

.exam-session__progress-percent {
	flex: 0 0 auto;
	min-width: 58rpx;
	font-size: 22rpx;
	line-height: 1;
	color: #667386;
	text-align: right;
}

.exam-session__progress-percent.is-complete {
	color: #2f9f62;
	font-weight: 800;
}

.exam-session__picker {
	padding: 28rpx 24rpx calc(env(safe-area-inset-bottom) + 32rpx);
	box-sizing: border-box;
}

.exam-session__picker-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
}

.exam-session__picker-title {
	display: block;
	font-size: 31rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #303849;
}

.exam-session__picker-subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	color: #738097;
}

.exam-session__picker-close {
	flex: 0 0 auto;
	height: 56rpx;
	padding: 0 18rpx;
	font-size: 23rpx;
}

.exam-session__picker-legend {
	display: flex;
	align-items: center;
	gap: 18rpx;
	margin-top: 24rpx;
	font-size: 22rpx;
	font-weight: 700;
	color: #7a8596;
}

.exam-session__picker-legend text::before {
	display: inline-block;
	width: 14rpx;
	height: 14rpx;
	margin-right: 8rpx;
	border-radius: 50%;
	background: #dce4ed;
	content: '';
}

.exam-session__picker-legend text:nth-child(1)::before {
	background: #344052;
}

.exam-session__picker-legend text:nth-child(2)::before {
	background: #4f7258;
}

.exam-session__picker-scroll {
	max-height: 520rpx;
	margin-top: 22rpx;
}

.exam-session__picker-grid {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	gap: 14rpx;
	padding-bottom: 6rpx;
}

.exam-session__picker-item {
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

.exam-session__picker-item.is-answered {
	background: #e7eee8;
	border-color: #b9cbbd;
	color: #4f7258;
}

.exam-session__picker-item.is-active {
	background: #344052;
	border-color: #344052;
	color: #ffffff;
}

.exam-session__question-card,
.exam-session__empty {
	margin-top: 22rpx;
	border: 1rpx solid rgba(177, 187, 202, 0.45);
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 32rpx rgba(47, 58, 78, 0.08);
}

.exam-session__question-card {
	padding: 30rpx 28rpx;
}

.exam-session__question-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.exam-session__question-kicker {
	display: block;
	font-size: 22rpx;
	font-weight: 800;
	color: #728096;
}

.exam-session__question-title {
	display: block;
	margin-top: 8rpx;
	font-size: 34rpx;
	font-weight: 800;
	color: #2f3747;
}

.exam-session__score {
	padding: 10rpx 14rpx;
	border-radius: 999rpx;
	background: #eef3f8;
	font-size: 23rpx;
	font-weight: 800;
	color: #59677c;
}

.exam-session__stem {
	display: block;
	font-size: 30rpx;
	line-height: 1.8;
	color: #2f3747;
	white-space: pre-wrap;
}

.exam-session__options {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 28rpx;
}

.exam-session__option {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 22rpx;
	border: 2rpx solid #e2e8f0;
	border-radius: 18rpx;
	background: #f9fbfd;
	box-sizing: border-box;
}

.exam-session__option.is-selected {
	border-color: #4f5d73;
	background: #eef3f8;
}

.exam-session__option-key {
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

.exam-session__option.is-selected .exam-session__option-key {
	background: #344052;
	color: #ffffff;
}

.exam-session__option-text {
	flex: 1;
	font-size: 27rpx;
	line-height: 1.65;
	color: #3d4658;
}

.exam-session__short-answer {
	margin-top: 28rpx;
}

.exam-session__textarea {
	width: 100%;
	min-height: 360rpx;
	padding: 22rpx;
	border: 1rpx solid #dce4ed;
	border-radius: 18rpx;
	background: #f9fbfd;
	font-size: 27rpx;
	line-height: 1.7;
	color: #2f3747;
	box-sizing: border-box;
}

.exam-session__placeholder {
	color: #a9b4c3;
}

.exam-session__save-button {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
	width: 100%;
	height: 82rpx;
	margin-top: 18rpx;
	padding: 0;
	border: 0;
	border-radius: 16rpx;
	background: #4f5d73;
	color: #ffffff;
	font-size: 27rpx;
	font-weight: 800;
	line-height: 1.2;
	text-align: center;
}

.exam-session__save-button::after,
.exam-session__footer-button::after,
.exam-session__submit-button::after {
	border: 0;
}

.exam-session__footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 10;
	display: grid;
	grid-template-columns: 1fr 1fr 1.2fr;
	gap: 14rpx;
	padding: 18rpx 28rpx calc(env(safe-area-inset-bottom) + 18rpx);
	background: rgba(244, 247, 251, 0.96);
	box-shadow: 0 -12rpx 28rpx rgba(46, 56, 74, 0.08);
	box-sizing: border-box;
}

.exam-session__footer.without-submit {
	grid-template-columns: 1fr 1fr;
}

.exam-session__footer-button,
.exam-session__submit-button {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
	height: 76rpx;
	margin: 0;
	padding: 0;
	border: 0;
	border-radius: 16rpx;
	font-size: 26rpx;
	font-weight: 800;
	line-height: 1.2;
	text-align: center;
}

.exam-session__footer-button {
	background: #ffffff;
	color: #4f5d73;
}

.exam-session__submit-button {
	background: #344052;
	color: #ffffff;
}

.exam-session__footer-button[disabled],
.exam-session__submit-button[disabled],
.exam-session__save-button[disabled] {
	background: #c6ced9;
	color: #f2f5f8;
}

.exam-session__empty {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 260rpx;
	color: #728096;
	font-size: 26rpx;
}

.exam-session__loading {
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

.exam-session__loading-card {
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

.exam-session__spinner {
	width: 34rpx;
	height: 34rpx;
	border: 4rpx solid #d7dee8;
	border-top-color: #4f5d73;
	border-radius: 50%;
	animation: exam-session-spin 0.8s linear infinite;
}

@keyframes exam-session-spin {
	to {
		transform: rotate(360deg);
	}
}
</style>
