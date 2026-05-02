<template>
	<!-- AI 索引: KYYY 英语专项轻占位页。 -->
	<view class="kyyy-entry-placeholder theme-page">
		<view class="kyyy-entry-placeholder__ambient kyyy-entry-placeholder__ambient--left"></view>
		<view class="kyyy-entry-placeholder__ambient kyyy-entry-placeholder__ambient--right"></view>

		<view class="kyyy-entry-placeholder__inner">
			<view class="kyyy-entry-placeholder__hero">
				<view class="kyyy-entry-placeholder__icon-shell" :style="{ background: iconBackground }">
					<uni-icons :type="icon" size="24" :color="iconColor" />
				</view>
				<text class="kyyy-entry-placeholder__eyebrow">{{ eyebrow }}</text>
				<text class="kyyy-entry-placeholder__title">{{ title }}</text>
				<text class="kyyy-entry-placeholder__desc">{{ description }}</text>
			</view>

			<view class="kyyy-entry-placeholder__notice">
				<text class="kyyy-entry-placeholder__notice-title">功能建设中</text>
				<text class="kyyy-entry-placeholder__notice-text">{{ noticeText }}</text>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'KyyyEntryPlaceholderPage',
	props: {
		eyebrow: {
			type: String,
			default: 'GRADUATE ENGLISH'
		},
		title: {
			type: String,
			required: true
		},
		description: {
			type: String,
			required: true
		},
		noticeText: {
			type: String,
			default: '该页面入口已接通，后续会按英语业务节奏继续补充具体功能。'
		},
		icon: {
			type: String,
			default: 'compose'
		},
		iconColor: {
			type: String,
			default: '#4d5f79'
		},
		iconBackground: {
			type: String,
			default: 'linear-gradient(135deg, rgba(220, 230, 248, 0.98), rgba(194, 209, 233, 0.96))'
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-entry-placeholder {
	position: relative;
	min-height: 100vh;
	overflow: hidden;
	padding: 32rpx 24rpx 48rpx;
}

.kyyy-entry-placeholder__ambient {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	opacity: 0.95;
}

.kyyy-entry-placeholder__ambient--left {
	top: 120rpx;
	left: -96rpx;
	width: 240rpx;
	height: 240rpx;
	background: radial-gradient(circle, rgba(215, 226, 255, 0.82) 0%, rgba(215, 226, 255, 0) 74%);
}

.kyyy-entry-placeholder__ambient--right {
	right: -88rpx;
	bottom: 180rpx;
	width: 260rpx;
	height: 260rpx;
	background: radial-gradient(circle, rgba(244, 231, 218, 0.82) 0%, rgba(244, 231, 218, 0) 74%);
}

.kyyy-entry-placeholder__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 12rpx;
}

.kyyy-entry-placeholder__hero,
.kyyy-entry-placeholder__notice {
	padding: 32rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-entry-placeholder__hero {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-entry-placeholder__icon-shell {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 84rpx;
	height: 84rpx;
	border-radius: 24rpx;
}

.kyyy-entry-placeholder__eyebrow {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.22em;
	color: #7a8698;
}

.kyyy-entry-placeholder__title {
	font-size: 42rpx;
	line-height: 1.22;
	font-weight: 700;
	color: #263036;
}

.kyyy-entry-placeholder__desc,
.kyyy-entry-placeholder__notice-text {
	font-size: 26rpx;
	line-height: 1.7;
	color: #5f6970;
}

.kyyy-entry-placeholder__notice {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.kyyy-entry-placeholder__notice-title {
	font-size: 28rpx;
	line-height: 1.35;
	font-weight: 700;
	color: #2f3940;
}
</style>
