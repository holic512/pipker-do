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

		<view v-if="syncing || syncErrorMessage" class="practice-review-panel__sync" :class="{ 'is-error': syncErrorMessage }">
			<text class="practice-review-panel__sync-text">
				{{ syncErrorMessage || '正在同步作答记录...' }}
			</text>
			<button
				v-if="syncErrorMessage"
				class="practice-review-panel__sync-button"
				@tap="$emit('retry-review-sync')"
			>
				重试同步
			</button>
		</view>

		<view v-if="showWrongBookHint" class="practice-review-panel__wrong-book">
			<view class="practice-review-panel__wrong-book-copy">
				<text class="practice-review-panel__wrong-book-title">已收进错题本</text>
				<text class="practice-review-panel__wrong-book-desc">之后可以回到错题本集中巩固，重新过一遍薄弱题。</text>
			</view>
			<button class="practice-review-panel__wrong-book-button" @tap="$emit('open-wrong-book')">查看错题本</button>
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
		},
		showWrongBookHint: {
			type: Boolean,
			default: false
		},
		syncing: {
			type: Boolean,
			default: false
		},
		syncErrorMessage: {
			type: String,
			default: ''
		}
	},
	emits: ['open-wrong-book', 'retry-review-sync']
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

.practice-review-panel__wrong-book {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
	margin-top: 22rpx;
	padding: 22rpx 20rpx;
	border-radius: 24rpx;
	background: linear-gradient(135deg, rgba(252, 239, 237, 0.96) 0%, rgba(255, 247, 246, 0.98) 100%);
}

.practice-review-panel__sync {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
	margin-top: 22rpx;
	padding: 18rpx 20rpx;
	border-radius: 22rpx;
	background: rgba(238, 243, 249, 0.96);
	border: 1rpx solid rgba(202, 214, 229, 0.9);
}

.practice-review-panel__sync.is-error {
	background: rgba(255, 241, 237, 0.98);
	border-color: rgba(226, 180, 168, 0.95);
}

.practice-review-panel__sync-text {
	flex: 1;
	min-width: 0;
	font-size: 22rpx;
	line-height: 1.5;
	color: #607083;
}

.practice-review-panel__sync.is-error .practice-review-panel__sync-text {
	color: #985241;
}

.practice-review-panel__sync-button {
	flex-shrink: 0;
	margin: 0;
	padding: 0 22rpx;
	height: 58rpx;
	line-height: 58rpx;
	border-radius: 18rpx;
	background: #fff7f4;
	color: #8b4a39;
	font-size: 21rpx;
	font-weight: 700;
	box-shadow: inset 0 0 0 1rpx #e2b4a8;
}

.practice-review-panel__sync-button::after {
	border: 0;
}

.practice-review-panel__wrong-book-copy {
	flex: 1;
	min-width: 0;
}

.practice-review-panel__wrong-book-title {
	display: block;
	font-size: 24rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #8f4d4b;
}

.practice-review-panel__wrong-book-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 21rpx;
	line-height: 1.6;
	color: #9b6a68;
}

.practice-review-panel__wrong-book-button {
	margin: 0;
	padding: 0 24rpx;
	height: 64rpx;
	line-height: 64rpx;
	border-radius: 20rpx;
	background: rgba(255, 255, 255, 0.92);
	color: #934b48;
	font-size: 22rpx;
	font-weight: 600;
	box-shadow: inset 0 0 0 1rpx rgba(235, 205, 202, 0.96);
}

.practice-review-panel__wrong-book-button::after {
	border: 0;
}
</style>
