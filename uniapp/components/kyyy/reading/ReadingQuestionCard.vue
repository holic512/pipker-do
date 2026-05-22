<!--
@file ReadingQuestionCard
@project pipker-do
@module 考研英语 / 阅读做题
@description 渲染单道阅读题卡片，承载题干 token、选项与交卷结果。
@logic 1. 展示题号与题干交互区；2. 展示选项与选中状态；3. 交卷后展示结果、标准答案与解析。
@dependencies Component: ReadingTokenFlow, Types: @/pages/kyyy/reading/types
@index_tags 考研英语, 题目卡片, 题干标注, 阅读选项
@author holic512
-->
<template>
	<view class="reading-question-card">
		<view class="reading-question-card__head">
			<text class="reading-question-card__no">第 {{ displayNo }} 题</text>
			<text v-if="saving" class="reading-question-card__saving">保存中</text>
		</view>

		<reading-token-flow
			:tokens="question.stemTokens"
			:token-key-prefix="`question-${question.questionId}`"
			:token-class-resolver="tokenClassResolver"
			@token-longpress="$emit('token-longpress', question.questionId, $event.token, $event.event)"
			@token-tap="$emit('token-tap', question.questionId, $event.token, $event.event)"
		/>

		<view class="reading-question-card__option-list">
			<view
				v-for="option in question.options"
				:key="`${question.questionId}-${option.optionKey}`"
				class="reading-question-card__option"
				:class="resolveOptionClass(option.optionKey)"
				@tap="$emit('select-option', question.questionId, option.optionKey)"
			>
				<view class="reading-question-card__option-key">
					<text>{{ option.optionKey }}</text>
				</view>
				<text class="reading-question-card__option-text">{{ option.optionContent }}</text>
			</view>
		</view>

		<view v-if="submitted" class="reading-question-card__result">
			<text class="reading-question-card__result-line">{{ resultText }}</text>
			<text class="reading-question-card__result-line">{{ userAnswerText }}</text>
			<text class="reading-question-card__result-line">标准答案：{{ question.correctAnswer || '待确认' }}</text>
			<text v-if="question.analysis" class="reading-question-card__analysis">{{ question.analysis }}</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import ReadingTokenFlow from '@/components/kyyy/reading/ReadingTokenFlow.vue'
import type { KyyyReadingQuestionState, KyyyReadingTextToken } from '@/pages/kyyy/reading/types'

type TokenClassResolver = (token: KyyyReadingTextToken) => Record<string, boolean>
type OptionClassResolver = (optionKey: string) => Record<string, boolean>

export default defineComponent({
	name: 'ReadingQuestionCard',
	components: {
		ReadingTokenFlow
	},
	props: {
		question: {
			type: Object as PropType<KyyyReadingQuestionState>,
			required: true
		},
		displayNo: {
			type: Number,
			default: 1
		},
		saving: {
			type: Boolean,
			default: false
		},
		submitted: {
			type: Boolean,
			default: false
		},
		tokenClassResolver: {
			type: Function as PropType<TokenClassResolver>,
			default: null
		},
		optionClassResolver: {
			type: Function as PropType<OptionClassResolver>,
			default: null
		}
	},
	emits: ['token-longpress', 'token-tap', 'select-option'],
	computed: {
		resultText(): string {
			if (!this.question.selectedOptionKey) {
				return '结果：未作答'
			}
			return `结果：${this.question.isCorrect ? '答对' : '答错'}`
		},
		userAnswerText(): string {
			return `你的答案：${this.question.selectedOptionKey || '未作答'}`
		}
	},
	methods: {
		resolveOptionClass(optionKey: string): Record<string, boolean> {
			if (typeof this.optionClassResolver === 'function') {
				return this.optionClassResolver(optionKey)
			}
			return {}
		}
	}
})
</script>

<style lang="scss" scoped>
.reading-question-card {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 32rpx 28rpx;
	background: rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(210, 218, 226, 0.92);
	border-radius: 28rpx;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.08);
}

.reading-question-card__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.reading-question-card__no,
.reading-question-card__saving,
.reading-question-card__result-line,
.reading-question-card__analysis {
	font-size: 24rpx;
	line-height: 1.7;
}

.reading-question-card__no {
	color: #667483;
}

.reading-question-card__saving {
	color: #7a8794;
}

.reading-question-card__option-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.reading-question-card__option {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 24rpx 22rpx;
	border-radius: 22rpx;
	border: 1rpx solid #dde4ea;
	background: #fbfcfd;
	transition: all 0.2s ease;
}

.reading-question-card__option.is-selected {
	border-color: #6d839b;
	background: #eef3f8;
}

.reading-question-card__option.is-correct {
	border-color: #74a987;
	background: #edf7f0;
}

.reading-question-card__option.is-wrong {
	border-color: #c48d86;
	background: #fbf1ef;
}

.reading-question-card__option.is-disabled {
	opacity: 0.95;
}

.reading-question-card__option-key {
	flex-shrink: 0;
	width: 46rpx;
	height: 46rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: #e8edf2;
	font-size: 24rpx;
	font-weight: 600;
	color: #445367;
}

.reading-question-card__option-text {
	flex: 1;
	font-size: 26rpx;
	line-height: 1.25;
	color: #314255;
}

.reading-question-card__result {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	padding-top: 8rpx;
	border-top: 1rpx solid #edf1f4;
}

.reading-question-card__result-line {
	color: #667483;
}

.reading-question-card__analysis {
	color: #4d5d70;
	white-space: pre-wrap;
}
</style>
