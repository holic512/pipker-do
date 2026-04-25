<template>
	<view v-if="question && currentBank" class="practice-question-panel">
		<view class="practice-question-panel__summary-bar">
			<view class="practice-question-panel__summary-main">
				<view class="practice-question-panel__summary-heading">
					<text class="practice-question-panel__summary-index">第 {{ progress.currentQuestionIndex }} / {{ progress.totalQuestionCount }} 题</text>
					<text v-if="isSourcePractice && sourceTitle" class="practice-question-panel__summary-source-tag">{{ sourceTitle }}</text>
					<text v-if="currentBank.bankName" class="practice-question-panel__summary-bank-tag">{{ currentBank.bankName }}</text>
				</view>
			</view>
			<view class="practice-question-panel__summary-actions">
				<button
					class="practice-question-panel__icon-button"
					@tap="$emit('open-settings')"
				>
					<uni-icons type="gear" size="18" color="#667286" />
				</button>

				<button
					class="practice-question-panel__icon-button practice-question-panel__favorite-button"
					:class="{ 'is-active': isFavorite }"
					@tap="$emit('toggle-favorite')"
				>
					<uni-icons :type="isFavorite ? 'star-filled' : 'star'" size="18" :color="isFavorite ? '#bd7a36' : '#667286'" />
				</button>

				<view v-if="!isSourcePractice" class="practice-question-panel__switcher" @tap="$emit('open-switcher')">
					<text class="practice-question-panel__switcher-text">切换题库</text>
					<uni-icons type="right" size="12" color="#49566e" />
				</view>
			</view>
		</view>

		<view class="practice-question-panel__meta-row">
			<text class="practice-question-panel__meta-badge practice-question-panel__meta-badge--type" :class="questionTypeTagClass(question.questionType)">{{ questionTypeLabel(question.questionType) }}</text>
			<text class="practice-question-panel__meta-badge practice-question-panel__meta-badge--difficulty" :class="difficultyTagClass(question.difficultyLevel)">{{ difficultyLabel(question.difficultyLevel) }}</text>
			<text v-if="question.score" class="practice-question-panel__meta-badge practice-question-panel__meta-badge--score">{{ question.score }} 分</text>
			<text v-if="question.yearNo" class="practice-question-panel__meta-badge practice-question-panel__meta-badge--year">{{ question.yearNo }} 年</text>
		</view>

		<view class="practice-question-panel__stem-shell">
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
	KyzzPracticeQuestionType,
	KyzzPracticeReviewViewResult,
	KyzzPracticeSessionProgress
} from '@/pages/kyzz/practice/types'
import { difficultyLabel, difficultyTagClass, questionTypeLabel } from '@/pages/kyzz/practice/view'

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
		sourceTitle: {
			type: String,
			default: ''
		},
		isSourcePractice: {
			type: Boolean,
			default: false
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
		},
		isFavorite: {
			type: Boolean,
			default: false
		}
	},
	emits: ['select-option', 'change-answer-text', 'open-switcher', 'toggle-favorite', 'open-settings'],
	methods: {
		difficultyLabel,
		difficultyTagClass,
		questionTypeLabel,
		questionTypeTagClass(questionType: KyzzPracticeQuestionType): string {
			if (questionType === 'single') {
				return 'is-single'
			}
			if (questionType === 'multiple') {
				return 'is-multiple'
			}
			if (questionType === 'short') {
				return 'is-short'
			}
			return ''
		},
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
	flex: 1;
}

.practice-question-panel__summary-actions {
	display: inline-flex;
	align-items: center;
	gap: 12rpx;
	flex-shrink: 0;
}

.practice-question-panel__summary-heading {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 10rpx 14rpx;
	min-width: 0;
}

.practice-question-panel__summary-index {
	font-size: 34rpx;
	line-height: 1.2;
	font-weight: 800;
	color: #1e2734;
}

.practice-question-panel__summary-source-tag,
.practice-question-panel__summary-bank-tag {
	display: inline-flex;
	align-items: center;
	max-width: 100%;
	min-height: 42rpx;
	padding: 0 16rpx;
	box-sizing: border-box;
	border-radius: 999rpx;
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 700;
	color: #345176;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.practice-question-panel__summary-source-tag {
	background: #e9f5ed;
	box-shadow: inset 0 0 0 1rpx #bdd8c7;
	color: #37614a;
}

.practice-question-panel__switcher {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 0 18rpx;
	height: 58rpx;
	border-radius: 999rpx;
	background: #f5f7fb;
	box-shadow:
		0 8rpx 18rpx rgba(54, 68, 90, 0.06),
		inset 0 0 0 1rpx #cbd5e4;
	flex-shrink: 0;
}

.practice-question-panel__switcher-text {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #33445d;
}

.practice-question-panel__icon-button {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 58rpx;
	height: 58rpx;
	margin: 0;
	padding: 0;
	border-radius: 999rpx;
	background: #f7f9fc;
	box-shadow:
		0 8rpx 18rpx rgba(54, 68, 90, 0.05),
		inset 0 0 0 1rpx #ccd6e5;
}

.practice-question-panel__icon-button::after {
	border: 0;
}

.practice-question-panel__favorite-button.is-active {
	background: #fff4df;
	box-shadow:
		0 10rpx 22rpx rgba(174, 104, 34, 0.14),
		inset 0 0 0 1rpx #e8c17f;
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
	background: #ffffff;
	box-shadow: inset 0 0 0 1rpx #cfd8e6;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 700;
	color: #39465a;
}

.practice-question-panel__meta-badge--type.is-single {
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
	color: #334f7c;
}

.practice-question-panel__meta-badge--type.is-multiple {
	background: #edf5ea;
	box-shadow: inset 0 0 0 1rpx #c2d9bc;
	color: #3d6237;
}

.practice-question-panel__meta-badge--type.is-short {
	background: #f4ecff;
	box-shadow: inset 0 0 0 1rpx #d5c2ec;
	color: #5b4678;
}

.practice-question-panel__meta-badge.is-simple {
	background: #e5f4e8;
	box-shadow: inset 0 0 0 1rpx #bcdabe;
	color: #315f42;
}

.practice-question-panel__meta-badge.is-medium {
	background: #e7efff;
	box-shadow: inset 0 0 0 1rpx #b9cae8;
	color: #314f79;
}

.practice-question-panel__meta-badge.is-hard {
	background: #fff0dc;
	box-shadow: inset 0 0 0 1rpx #e6c58e;
	color: #78511f;
}

.practice-question-panel__meta-badge.is-sprint {
	background: #fde8e5;
	box-shadow: inset 0 0 0 1rpx #e8b8b3;
	color: #87413e;
}

.practice-question-panel__meta-badge--score {
	background: #fff6df;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
	color: #735817;
}

.practice-question-panel__meta-badge--year {
	background: #eef2f6;
	box-shadow: inset 0 0 0 1rpx #c9d2dc;
	color: #3c4a58;
}

.practice-question-panel__stem-shell,
.practice-question-panel__essay-shell {
	margin-top: 18rpx;
	padding: 28rpx 26rpx;
	border-radius: 30rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(45, 58, 77, 0.06);
	border: 1rpx solid #d4deea;
}

.practice-question-panel__stem {
	font-size: 31rpx;
	line-height: 1.8;
	font-weight: 700;
	color: #1f2937;
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
	background: #ffffff;
	box-shadow: 0 12rpx 24rpx rgba(45, 58, 77, 0.045);
	border: 1rpx solid #d8e0eb;
	transition: all 0.18s ease;
}

.practice-question-panel__option--selected {
	background: #eef4ff;
	box-shadow: 0 16rpx 30rpx rgba(55, 84, 132, 0.1);
	border-color: #9fb7dd;
}

.practice-question-panel__option--correct {
	background: #e8f6eb;
	border-color: #97c8a4;
}

.practice-question-panel__option--wrong {
	background: #fdebea;
	border-color: #dc9b96;
}

.practice-question-panel__option-key {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 48rpx;
	height: 48rpx;
	border-radius: 16rpx;
	background: #e6ebf2;
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #334155;
	flex-shrink: 0;
}

.practice-question-panel__option--selected .practice-question-panel__option-key {
	background: #5472a5;
	color: #ffffff;
}

.practice-question-panel__option--correct .practice-question-panel__option-key {
	background: #4f7f58;
	color: #ffffff;
}

.practice-question-panel__option--wrong .practice-question-panel__option-key {
	background: #b45f5b;
	color: #ffffff;
}

.practice-question-panel__option-content {
	flex: 1;
	min-width: 0;
	font-size: 27rpx;
	line-height: 1.8;
	color: #253142;
}

.practice-question-panel__essay-input {
	width: 100%;
	min-height: 240rpx;
	font-size: 28rpx;
	line-height: 1.8;
	color: #1f2937;
}

.practice-question-panel__essay-hint {
	display: block;
	margin-top: 18rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #5f6d7f;
}
</style>
