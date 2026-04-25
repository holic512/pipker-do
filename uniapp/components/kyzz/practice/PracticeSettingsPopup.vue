<template>
	<view class="practice-settings-popup">
		<view class="practice-settings-popup__header">
			<view class="practice-settings-popup__title-wrap">
				<text class="practice-settings-popup__title">刷题设置</text>
				<text class="practice-settings-popup__subtitle">设置会同步到当前账号并立即生效</text>
			</view>
			<button class="practice-settings-popup__close" @tap="$emit('close')">
				<uni-icons type="closeempty" size="22" color="#64748b" />
			</button>
		</view>

		<view class="practice-settings-popup__body">
			<view class="practice-settings-popup__item">
				<view class="practice-settings-popup__item-copy">
					<text class="practice-settings-popup__item-title">答对后自动下一题</text>
					<text class="practice-settings-popup__item-desc">客观题答对后进入下一题，简答题自判答对后生效</text>
				</view>
				<switch
					:checked="autoJumpOnCorrect"
					:disabled="syncing"
					color="#5f7493"
					@change="handleAutoJumpChange"
				/>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

// AI 索引: KYZZ 刷题设置弹窗组件。

export default defineComponent({
	name: 'PracticeSettingsPopup',
	props: {
		autoJumpOnCorrect: {
			type: Boolean,
			default: true
		},
		syncing: {
			type: Boolean,
			default: false
		}
	},
	emits: ['close', 'change-auto-jump'],
	methods: {
		handleAutoJumpChange(event: { detail?: { value?: boolean } }): void {
			this.$emit('change-auto-jump', Boolean(event?.detail?.value))
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-settings-popup {
	padding: 28rpx 28rpx calc(env(safe-area-inset-bottom) + 30rpx);
	background: #ffffff;
	box-sizing: border-box;
}

.practice-settings-popup__header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
}

.practice-settings-popup__title-wrap {
	min-width: 0;
	flex: 1;
}

.practice-settings-popup__title {
	display: block;
	font-size: 32rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #1f2937;
}

.practice-settings-popup__subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	line-height: 1.45;
	color: #6b778a;
}

.practice-settings-popup__close {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 58rpx;
	height: 58rpx;
	margin: 0;
	padding: 0;
	border-radius: 999rpx;
	background: #f6f8fb;
	box-shadow: inset 0 0 0 1rpx #d1dae6;
	flex-shrink: 0;
}

.practice-settings-popup__close::after {
	border: 0;
}

.practice-settings-popup__body {
	margin-top: 28rpx;
}

.practice-settings-popup__item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 28rpx;
	padding: 26rpx 24rpx;
	border-radius: 26rpx;
	background: #f8fafc;
	box-shadow:
		0 10rpx 24rpx rgba(45, 58, 77, 0.04),
		inset 0 0 0 1rpx #d7e0ec;
}

.practice-settings-popup__item-copy {
	min-width: 0;
	flex: 1;
}

.practice-settings-popup__item-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.35;
	font-weight: 750;
	color: #263142;
}

.practice-settings-popup__item-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.45;
	color: #738096;
}
</style>
