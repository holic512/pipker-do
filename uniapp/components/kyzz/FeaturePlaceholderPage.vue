<template>
	<view class="feature-placeholder-page">
		<view class="feature-placeholder-page__inner">
			<view class="feature-placeholder-page__hero">
				<view class="feature-placeholder-page__icon-shell" :style="{ background: iconShellBackground }">
					<view class="feature-placeholder-page__icon-core" :style="{ background: iconCoreBackground }">
						<uni-icons :type="icon" size="34" :color="iconColor" />
					</view>
				</view>

				<text class="feature-placeholder-page__title">{{ title }}</text>
				<text class="feature-placeholder-page__desc">{{ description }}</text>
				<text v-if="tip" class="feature-placeholder-page__tip">{{ tip }}</text>
			</view>

			<view class="feature-placeholder-page__actions">
				<button class="feature-placeholder-page__primary" @tap="$emit('primary')">{{ primaryText }}</button>
				<button class="feature-placeholder-page__secondary" @tap="$emit('secondary')">{{ secondaryText }}</button>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

// AI 索引: KYZZ 小程序业务占位页通用组件。

export default defineComponent({
	name: 'FeaturePlaceholderPage',
	props: {
		title: {
			type: String,
			required: true
		},
		description: {
			type: String,
			required: true
		},
		tip: {
			type: String,
			default: ''
		},
		icon: {
			type: String,
			default: 'info'
		},
		iconColor: {
			type: String,
			default: '#5a6681'
		},
		iconShellBackground: {
			type: String,
			default: 'linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(237, 242, 249, 0.96) 100%)'
		},
		iconCoreBackground: {
			type: String,
			default: 'linear-gradient(135deg, rgba(222, 231, 245, 0.98) 0%, rgba(246, 249, 253, 0.98) 100%)'
		},
		primaryText: {
			type: String,
			default: '去学习'
		},
		secondaryText: {
			type: String,
			default: '返回上一页'
		}
	},
	emits: ['primary', 'secondary']
})
</script>

<style lang="scss" scoped>
.feature-placeholder-page {
	min-height: 100vh;
	padding: 44rpx 32rpx calc(env(safe-area-inset-bottom) + 52rpx);
	box-sizing: border-box;
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.99) 0%, rgba(243, 247, 252, 0.97) 46%, rgba(234, 240, 247, 0.94) 100%);
}

.feature-placeholder-page__inner {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	min-height: calc(100vh - 88rpx - env(safe-area-inset-bottom));
}

.feature-placeholder-page__hero {
	margin-top: 64rpx;
	padding: 42rpx 34rpx;
	border-radius: 34rpx;
	background: rgba(255, 255, 255, 0.88);
	box-shadow: 0 24rpx 48rpx rgba(43, 52, 55, 0.07);
	text-align: center;
}

.feature-placeholder-page__icon-shell {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 164rpx;
	height: 164rpx;
	border-radius: 44rpx;
	box-shadow:
		0 26rpx 48rpx rgba(84, 94, 118, 0.08),
		inset 0 0 0 1rpx rgba(222, 229, 239, 0.88);
}

.feature-placeholder-page__icon-core {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 112rpx;
	height: 112rpx;
	border-radius: 32rpx;
	box-shadow: inset 0 0 0 1rpx rgba(231, 236, 244, 0.96);
}

.feature-placeholder-page__title {
	display: block;
	margin-top: 30rpx;
	font-size: 40rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2b3443;
}

.feature-placeholder-page__desc {
	display: block;
	margin-top: 18rpx;
	font-size: 26rpx;
	line-height: 1.8;
	color: #6f7c90;
}

.feature-placeholder-page__tip {
	display: block;
	margin-top: 18rpx;
	font-size: 22rpx;
	line-height: 1.7;
	color: #8a94a4;
}

.feature-placeholder-page__actions {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 32rpx;
}

.feature-placeholder-page__primary,
.feature-placeholder-page__secondary {
	margin: 0;
	height: 88rpx;
	line-height: 88rpx;
	border-radius: 28rpx;
	font-size: 26rpx;
	font-weight: 600;
}

.feature-placeholder-page__primary::after,
.feature-placeholder-page__secondary::after {
	border: 0;
}

.feature-placeholder-page__primary {
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
	box-shadow: 0 18rpx 32rpx rgba(84, 94, 118, 0.2);
}

.feature-placeholder-page__secondary {
	background: rgba(255, 255, 255, 0.9);
	color: #566378;
	box-shadow: inset 0 0 0 1rpx rgba(215, 223, 235, 0.96);
}
</style>
