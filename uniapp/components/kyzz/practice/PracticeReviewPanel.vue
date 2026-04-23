<template>
	<view v-if="reviewResult" class="practice-review-panel">
		<view class="practice-review-panel__head">
			<text class="practice-review-panel__title">查看答案</text>
			<text v-if="awaitingSelfJudgement" class="practice-review-panel__badge practice-review-panel__badge--pending">等待自判</text>
			<text v-else class="practice-review-panel__badge" :class="reviewResult.isCorrect ? 'practice-review-panel__badge--correct' : 'practice-review-panel__badge--wrong'">
				{{ reviewResult.isCorrect ? '回答正确' : '需要巩固' }}
			</text>
		</view>

		<view class="practice-review-panel__block">
			<text class="practice-review-panel__label">你的答案</text>
			<text class="practice-review-panel__value">{{ userAnswerDisplay }}</text>
		</view>

		<view class="practice-review-panel__block">
			<text class="practice-review-panel__label">标准答案</text>
			<text class="practice-review-panel__value">{{ standardAnswerDisplay }}</text>
		</view>

		<view v-if="reviewResult.analysis" class="practice-review-panel__block">
			<text class="practice-review-panel__label">解析</text>
			<text class="practice-review-panel__value">{{ reviewResult.analysis }}</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeReviewViewResult } from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 刷题页答案解析组件。

export default defineComponent({
	name: 'PracticeReviewPanel',
	props: {
		reviewResult: {
			type: Object as PropType<KyzzPracticeReviewViewResult | null>,
			default: null
		},
		awaitingSelfJudgement: {
			type: Boolean,
			default: false
		},
		userAnswerDisplay: {
			type: String,
			default: ''
		},
		standardAnswerDisplay: {
			type: String,
			default: ''
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-review-panel {
	margin-top: 18rpx;
	padding: 28rpx 26rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.97);
	box-shadow: 0 18rpx 36rpx rgba(50, 60, 72, 0.05);
	border: 1rpx solid rgba(217, 225, 236, 0.92);
}

.practice-review-panel__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.practice-review-panel__title {
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #25303d;
}

.practice-review-panel__badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 42rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 600;
}

.practice-review-panel__badge--correct {
	background: rgba(225, 236, 226, 0.98);
	color: #476453;
}

.practice-review-panel__badge--wrong {
	background: rgba(247, 226, 224, 0.98);
	color: #984c49;
}

.practice-review-panel__badge--pending {
	background: rgba(236, 240, 245, 0.98);
	color: #5f6c80;
}

.practice-review-panel__block {
	margin-top: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid rgba(216, 224, 235, 0.92);
}

.practice-review-panel__label {
	display: block;
	font-size: 22rpx;
	line-height: 1.4;
	color: #6c788a;
}

.practice-review-panel__value {
	display: block;
	margin-top: 14rpx;
	font-size: 26rpx;
	line-height: 1.8;
	color: #283240;
	white-space: pre-line;
}
</style>
