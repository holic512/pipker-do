<template>
	<view class="practice-footer-actions">
		<view v-if="awaitingSelfJudgement" class="practice-footer-actions__judge-row">
			<button
				class="practice-footer-actions__judge-button practice-footer-actions__judge-button--ghost"
				:disabled="submitting"
				@tap="$emit('judge-wrong')"
			>
				需要巩固
			</button>
			<button
				class="practice-footer-actions__judge-button practice-footer-actions__judge-button--primary"
				:disabled="submitting"
				@tap="$emit('judge-correct')"
			>
				我答对了
			</button>
		</view>

		<view class="practice-footer-actions__buttons">
			<button
				class="practice-footer-actions__ghost-button"
				:disabled="!canGoPrevious || submitting"
				@tap="$emit('previous-question')"
			>
				上一题
			</button>

			<button
				class="practice-footer-actions__primary-button"
				:disabled="primaryDisabled"
				@tap="handlePrimaryTap"
			>
				{{ primaryButtonText }}
			</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeReviewViewResult } from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 刷题页页内操作组件。

export default defineComponent({
	name: 'PracticeFooterActions',
	props: {
		reviewResult: {
			type: Object as PropType<KyzzPracticeReviewViewResult | null>,
			default: null
		},
		awaitingSelfJudgement: {
			type: Boolean,
			default: false
		},
		canSubmit: {
			type: Boolean,
			default: false
		},
		submitting: {
			type: Boolean,
			default: false
		},
		submitButtonText: {
			type: String,
			default: '查看答案'
		},
		canGoPrevious: {
			type: Boolean,
			default: false
		},
		canGoNext: {
			type: Boolean,
			default: false
		},
		nextButtonText: {
			type: String,
			default: '下一题'
		}
	},
	emits: ['submit-review', 'judge-correct', 'judge-wrong', 'previous-question', 'next-question'],
	computed: {
		primaryDisabled(): boolean {
			if (this.submitting) {
				return true
			}
			if (this.reviewResult) {
				return !this.canGoNext || this.awaitingSelfJudgement
			}
			return !this.canSubmit
		},
		primaryButtonText(): string {
			if (this.reviewResult) {
				return this.nextButtonText
			}
			return this.submitButtonText
		}
	},
	methods: {
		handlePrimaryTap(): void {
			if (this.primaryDisabled) {
				return
			}
			if (this.reviewResult) {
				this.$emit('next-question')
				return
			}
			this.$emit('submit-review')
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-footer-actions {
	margin-top: 20rpx;
	padding: 20rpx 0 8rpx;
}

.practice-footer-actions__judge-row,
.practice-footer-actions__buttons {
	display: flex;
	gap: 14rpx;
}

.practice-footer-actions__judge-row {
	margin-bottom: 14rpx;
}

.practice-footer-actions__primary-button,
.practice-footer-actions__ghost-button,
.practice-footer-actions__judge-button {
	flex: 1;
	margin: 0;
	padding: 0 20rpx;
	height: 82rpx;
	line-height: 82rpx;
	border-radius: 26rpx;
	font-size: 24rpx;
	font-weight: 700;
}

.practice-footer-actions__primary-button::after,
.practice-footer-actions__ghost-button::after,
.practice-footer-actions__judge-button::after {
	border: 0;
}

.practice-footer-actions__primary-button {
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
	box-shadow: 0 16rpx 30rpx rgba(54, 75, 111, 0.22);
}

.practice-footer-actions__ghost-button {
	background: #f7f9fc;
	color: #34445e;
	box-shadow:
		0 8rpx 18rpx rgba(54, 68, 90, 0.05),
		inset 0 0 0 1rpx #cbd5e4;
}

.practice-footer-actions__ghost-button--active {
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
	box-shadow: 0 16rpx 30rpx rgba(54, 75, 111, 0.2);
}

.practice-footer-actions__judge-button--primary {
	background: linear-gradient(135deg, #3f754d 0%, #6f9477 100%);
	color: #ffffff;
	box-shadow: 0 16rpx 30rpx rgba(56, 111, 70, 0.18);
}

.practice-footer-actions__judge-button--ghost {
	background: #fff0e5;
	color: #8a4b2d;
	box-shadow:
		0 8rpx 18rpx rgba(126, 75, 43, 0.05),
		inset 0 0 0 1rpx #e5bea4;
}

.practice-footer-actions__primary-button[disabled],
.practice-footer-actions__ghost-button[disabled],
.practice-footer-actions__judge-button[disabled] {
	background: #d9e0ea;
	color: #7d8aa0;
	box-shadow: inset 0 0 0 1rpx #c4cedb;
	opacity: 1;
}
</style>
