<template>
	<view class="practice-state-notice" :class="variantClass">
		<text class="practice-state-notice__title">{{ notice.title }}</text>
		<text class="practice-state-notice__desc">{{ notice.description }}</text>

		<view v-if="notice.pills && notice.pills.length" class="practice-state-notice__pill-row">
			<text
				v-for="item in notice.pills"
				:key="item"
				class="practice-state-notice__pill"
			>
				{{ item }}
			</text>
		</view>

		<view v-if="notice.primaryText || notice.secondaryText" class="practice-state-notice__actions">
			<button
				v-if="notice.secondaryText"
				class="practice-state-notice__ghost-button"
				@tap="$emit('secondary')"
			>
				{{ notice.secondaryText }}
			</button>
			<button
				v-if="notice.primaryText"
				class="practice-state-notice__primary-button"
				@tap="$emit('primary')"
			>
				{{ notice.primaryText }}
			</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeNoticeViewModel } from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 刷题页状态提醒组件。

export default defineComponent({
	name: 'PracticeStateNotice',
	props: {
		notice: {
			type: Object as PropType<KyzzPracticeNoticeViewModel>,
			required: true
		}
	},
	emits: ['primary', 'secondary'],
	computed: {
		variantClass(): string {
			if (this.notice.variant === 'accent') {
				return 'practice-state-notice--accent'
			}
			if (this.notice.variant === 'success') {
				return 'practice-state-notice--success'
			}
			return ''
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-state-notice {
	padding: 38rpx 32rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.97);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.05);
	border: 1rpx solid rgba(216, 224, 235, 0.9);
}

.practice-state-notice--accent {
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(241, 245, 251, 0.96) 100%);
}

.practice-state-notice--success {
	background: linear-gradient(135deg, rgba(248, 252, 249, 0.99) 0%, rgba(236, 245, 240, 0.97) 100%);
}

.practice-state-notice__title {
	display: block;
	font-size: 34rpx;
	line-height: 1.25;
	font-weight: 700;
	color: #25303d;
}

.practice-state-notice__desc {
	display: block;
	margin-top: 16rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #677487;
}

.practice-state-notice__pill-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 22rpx;
}

.practice-state-notice__pill {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 44rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: rgba(234, 239, 247, 0.96);
	font-size: 21rpx;
	line-height: 1;
	color: #49596f;
}

.practice-state-notice__actions {
	display: flex;
	gap: 16rpx;
	margin-top: 28rpx;
}

.practice-state-notice__primary-button,
.practice-state-notice__ghost-button {
	flex: 1;
	margin: 0;
	padding: 0 28rpx;
	height: 78rpx;
	line-height: 78rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 600;
}

.practice-state-notice__primary-button::after,
.practice-state-notice__ghost-button::after {
	border: 0;
}

.practice-state-notice__primary-button {
	background: linear-gradient(135deg, #4f5b71 0%, #73819b 100%);
	color: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(79, 91, 113, 0.2);
}

.practice-state-notice__ghost-button {
	background: #edf1f6;
	color: #4d5a70;
}
</style>
