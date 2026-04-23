<template>
	<view v-if="question && currentBank" class="practice-question-panel">
		<view class="practice-question-panel__summary-bar">
			<view class="practice-question-panel__summary-main">
				<text class="practice-question-panel__summary-index">第 {{ progress.currentQuestionIndex }} / {{ progress.totalQuestionCount }} 题</text>
				<text class="practice-question-panel__summary-bank">{{ currentBank.bankName }}</text>
			</view>
			<view class="practice-question-panel__switcher" @tap="$emit('open-switcher')">
				<text class="practice-question-panel__switcher-text">切换题库</text>
				<uni-icons type="right" size="12" color="#49566e" />
			</view>
		</view>

		<view class="practice-question-panel__summary-sub">
			<text class="practice-question-panel__summary-status">{{ currentBank.resumeLabel }}</text>
			<text class="practice-question-panel__summary-progress">{{ formatProgress(currentBank.currentProgress) }}</text>
		</view>

		<view class="practice-question-panel__summary-track">
			<view class="practice-question-panel__summary-value" :style="{ width: `${progressPercent(currentBank.currentProgress)}%` }"></view>
		</view>

		<view class="practice-question-panel__meta-row">
			<text class="practice-question-panel__meta-badge">{{ questionTypeLabel(question.questionType) }}</text>
			<text class="practice-question-panel__meta-badge" :class="difficultyTagClass(question.difficultyLevel)">{{ difficultyLabel(question.difficultyLevel) }}</text>
			<text v-if="question.score" class="practice-question-panel__meta-badge">{{ question.score }} 分</text>
			<text v-if="question.yearNo" class="practice-question-panel__meta-badge">{{ question.yearNo }} 年</text>
		</view>

		<view class="practice-question-panel__stem-shell">
			<text v-if="question.sourceName" class="practice-question-panel__stem-source">{{ question.sourceName }}</text>
			<view class="practice-question-panel__stem">{{ question.stem }}</view>
		</view>

		<view v-if="question.questionType !== 'short'" class="practice-question-panel__options">
			<view
				v-for="option in question.options"
				:key="option.optionKey"
				class="practice-question-panel__option"
				:class="optionCardClass(option.optionKey)"
				@tap="$emit('select-option', option.optionKey)"
			>
				<view class="practice-question-panel__option-key">{{ option.optionKey }}</view>
				<text class="practice-question-panel__option-content">{{ option.optionContent }}</text>
			</view>
		</view>

		<view v-else class="practice-question-panel__essay-shell">
			<textarea
				:value="answerText"
				class="practice-question-panel__essay-input"
				placeholder="先写下你的理解，再查看标准答案。"
				maxlength="-1"
				:auto-height="true"
				:disabled="Boolean(reviewResult)"
				@input="handleAnswerInput"
			/>
			<text class="practice-question-panel__essay-hint">简答题会在查看答案后，由你自己判断“答对了”还是“需要巩固”。</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type {
	KyzzPracticeBankViewRecord,
	KyzzPracticeQuestionView,
	KyzzPracticeReviewViewResult,
	KyzzPracticeSessionProgress
} from '@/pages/kyzz/practice/types'
import { difficultyLabel, difficultyTagClass, formatProgress, progressPercent, questionTypeLabel } from '@/pages/kyzz/practice/view'

// AI 索引: KYZZ 刷题页题面组件。

export default defineComponent({
	name: 'PracticeQuestionPanel',
	props: {
		currentBank: {
			type: Object as PropType<KyzzPracticeBankViewRecord | null>,
			default: null
		},
		progress: {
			type: Object as PropType<KyzzPracticeSessionProgress>,
			required: true
		},
		question: {
			type: Object as PropType<KyzzPracticeQuestionView | null>,
			default: null
		},
		selectedOptionKeys: {
			type: Array as PropType<string[]>,
			default: () => []
		},
		answerText: {
			type: String,
			default: ''
		},
		reviewResult: {
			type: Object as PropType<KyzzPracticeReviewViewResult | null>,
			default: null
		}
	},
	emits: ['select-option', 'change-answer-text', 'open-switcher'],
	methods: {
		difficultyLabel,
		difficultyTagClass,
		formatProgress,
		progressPercent,
		questionTypeLabel,
		optionCardClass(optionKey: string): string {
			const selected = this.selectedOptionKeys.includes(optionKey)
			if (!this.reviewResult) {
				return selected ? 'practice-question-panel__option--selected' : ''
			}
			const isCorrect = this.reviewResult.correctOptionKeys.includes(optionKey)
			if (isCorrect) {
				return 'practice-question-panel__option--correct'
			}
			if (selected) {
				return 'practice-question-panel__option--wrong'
			}
			return ''
		},
		handleAnswerInput(event: { detail?: { value?: string } }): void {
			this.$emit('change-answer-text', event?.detail?.value || '')
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-question-panel__summary-bar {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 16rpx;
}

.practice-question-panel__summary-main {
	min-width: 0;
	display: flex;
	flex-direction: column;
}

.practice-question-panel__summary-index {
	font-size: 34rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #242d3a;
}

.practice-question-panel__summary-bank {
	margin-top: 10rpx;
	font-size: 24rpx;
	line-height: 1.5;
	color: #4d5a70;
	font-weight: 600;
}

.practice-question-panel__switcher {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 0 18rpx;
	height: 58rpx;
	border-radius: 999rpx;
	background: rgba(240, 244, 249, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(202, 212, 228, 0.82);
	flex-shrink: 0;
}

.practice-question-panel__switcher-text {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 600;
	color: #4a566c;
}

.practice-question-panel__summary-sub {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	margin-top: 16rpx;
}

.practice-question-panel__summary-status {
	font-size: 22rpx;
	line-height: 1.5;
	color: #6e798c;
}

.practice-question-panel__summary-progress {
	font-size: 24rpx;
	line-height: 1;
	font-weight: 700;
	color: #415069;
}

.practice-question-panel__summary-track {
	overflow: hidden;
	margin-top: 14rpx;
	height: 12rpx;
	border-radius: 999rpx;
	background: rgba(208, 217, 231, 0.9);
}

.practice-question-panel__summary-value {
	height: 100%;
	border-radius: inherit;
	background: linear-gradient(90deg, #4b576d 0%, #74839d 100%);
}

.practice-question-panel__meta-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-top: 18rpx;
}

.practice-question-panel__meta-badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 42rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(220, 227, 237, 0.92);
	font-size: 20rpx;
	line-height: 1;
	color: #5d6777;
}

.practice-question-panel__meta-badge.is-simple {
	background: rgba(224, 236, 225, 0.98);
	color: #486655;
}

.practice-question-panel__meta-badge.is-medium {
	background: rgba(220, 229, 252, 0.96);
	color: #405775;
}

.practice-question-panel__meta-badge.is-hard {
	background: rgba(244, 230, 213, 0.98);
	color: #8b6335;
}

.practice-question-panel__meta-badge.is-sprint {
	background: rgba(246, 225, 223, 0.98);
	color: #9e4e4b;
}

.practice-question-panel__stem-shell,
.practice-question-panel__essay-shell {
	margin-top: 18rpx;
	padding: 28rpx 26rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.97);
	box-shadow: 0 18rpx 36rpx rgba(50, 60, 72, 0.05);
	border: 1rpx solid rgba(217, 225, 236, 0.9);
}

.practice-question-panel__stem-source {
	display: block;
	font-size: 21rpx;
	line-height: 1.4;
	color: #748094;
}

.practice-question-panel__stem {
	margin-top: 18rpx;
	font-size: 31rpx;
	line-height: 1.8;
	font-weight: 600;
	color: #26303d;
	white-space: pre-line;
}

.practice-question-panel__options {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	margin-top: 18rpx;
}

.practice-question-panel__option {
	display: flex;
	align-items: flex-start;
	gap: 16rpx;
	padding: 24rpx 22rpx;
	border-radius: 26rpx;
	background: rgba(255, 255, 255, 0.98);
	box-shadow: 0 14rpx 30rpx rgba(50, 60, 72, 0.04);
	border: 1rpx solid rgba(218, 225, 235, 0.88);
	transition: all 0.18s ease;
}

.practice-question-panel__option--selected {
	background: linear-gradient(135deg, rgba(238, 243, 250, 0.99) 0%, rgba(226, 232, 243, 0.99) 100%);
	box-shadow: 0 18rpx 32rpx rgba(70, 82, 106, 0.08);
	border-color: rgba(177, 190, 211, 0.9);
}

.practice-question-panel__option--correct {
	background: linear-gradient(135deg, rgba(233, 245, 236, 0.99) 0%, rgba(220, 238, 227, 0.99) 100%);
	border-color: rgba(178, 207, 186, 0.9);
}

.practice-question-panel__option--wrong {
	background: linear-gradient(135deg, rgba(248, 236, 235, 0.99) 0%, rgba(244, 226, 224, 0.99) 100%);
	border-color: rgba(223, 186, 182, 0.9);
}

.practice-question-panel__option-key {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 48rpx;
	height: 48rpx;
	border-radius: 16rpx;
	background: rgba(83, 96, 119, 0.14);
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #49566f;
	flex-shrink: 0;
}

.practice-question-panel__option-content {
	flex: 1;
	min-width: 0;
	font-size: 27rpx;
	line-height: 1.8;
	color: #2b3443;
}

.practice-question-panel__essay-input {
	width: 100%;
	min-height: 240rpx;
	font-size: 28rpx;
	line-height: 1.8;
	color: #2a3342;
}

.practice-question-panel__essay-hint {
	display: block;
	margin-top: 18rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #6f7b8d;
}
</style>
