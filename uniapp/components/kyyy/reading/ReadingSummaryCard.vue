<!--
@file ReadingSummaryCard
@project pipker-do
@module 考研英语 / 阅读做题
@description 展示阅读交卷后的答题汇总卡片。
@logic 1. 展示答对数量；2. 展示答错数量；3. 展示正确率。
@dependencies None
@index_tags 考研英语, 阅读汇总, 正确率, 结果卡片
@author holic512
-->
<template>
	<view class="reading-summary-card">
		<view class="reading-summary-card__item">
			<text class="reading-summary-card__value">{{ correctCount }}</text>
			<text class="reading-summary-card__label">答对</text>
		</view>
		<view class="reading-summary-card__item">
			<text class="reading-summary-card__value">{{ wrongCount }}</text>
			<text class="reading-summary-card__label">答错</text>
		</view>
		<view class="reading-summary-card__item">
			<text class="reading-summary-card__value">{{ accuracyText }}</text>
			<text class="reading-summary-card__label">正确率</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'ReadingSummaryCard',
	props: {
		correctCount: {
			type: Number,
			default: 0
		},
		wrongCount: {
			type: Number,
			default: 0
		},
		accuracyText: {
			type: String,
			default: '0%'
		}
	}
})
</script>

<style lang="scss" scoped>
.reading-summary-card {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 16rpx;
	padding: 24rpx 20rpx;
	background: rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(210, 218, 226, 0.92);
	border-radius: 28rpx;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.08);
}

.reading-summary-card__item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	padding: 20rpx 0;
	border-radius: 20rpx;
	background: #f7f9fb;
}

.reading-summary-card__value {
	font-size: 34rpx;
	font-weight: 700;
	color: #314255;
}

.reading-summary-card__label {
	font-size: 22rpx;
	color: #6c7886;
}
</style>
