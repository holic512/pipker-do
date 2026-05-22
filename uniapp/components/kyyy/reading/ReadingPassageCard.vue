<!--
@file ReadingPassageCard
@project pipker-do
@module 考研英语 / 阅读做题
@description 渲染阅读页正文卡片，承载篇章元信息、标题与正文 token 流。
@logic 1. 展示篇章来源与标题；2. 复用 token 组件渲染正文；3. 透出正文 token 交互事件。
@dependencies Component: ReadingTokenFlow, Types: @/pages/kyyy/reading/types
@index_tags 考研英语, 正文卡片, 阅读正文, token交互
@author holic512
-->
<template>
	<view class="reading-passage-card">
		<text class="reading-passage-card__meta">{{ passageMetaText }}</text>
		<text v-if="title" class="reading-passage-card__title">{{ title }}</text>
		<reading-token-flow
			:tokens="tokens"
			token-key-prefix="passage"
			:token-class-resolver="tokenClassResolver"
			@token-longpress="$emit('token-longpress', $event.token, $event.event)"
			@token-tap="$emit('token-tap', $event.token, $event.event)"
		/>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import ReadingTokenFlow from '@/components/kyyy/reading/ReadingTokenFlow.vue'
import type { KyyyReadingTextToken } from '@/pages/kyyy/reading/types'

type TokenClassResolver = (token: KyyyReadingTextToken) => Record<string, boolean>

export default defineComponent({
	name: 'ReadingPassageCard',
	components: {
		ReadingTokenFlow
	},
	props: {
		passageMetaText: {
			type: String,
			default: ''
		},
		title: {
			type: String,
			default: ''
		},
		tokens: {
			type: Array as PropType<KyyyReadingTextToken[]>,
			default: () => []
		},
		tokenClassResolver: {
			type: Function as PropType<TokenClassResolver>,
			default: null
		}
	},
	emits: ['token-longpress', 'token-tap']
})
</script>

<style lang="scss" scoped>
.reading-passage-card {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	padding: 32rpx 28rpx;
	background: rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(210, 218, 226, 0.92);
	border-radius: 28rpx;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.08);
}

.reading-passage-card__meta {
	font-size: 24rpx;
	line-height: 1.7;
	color: #7a8794;
}

.reading-passage-card__title {
	font-size: 32rpx;
	font-weight: 700;
	line-height: 1.5;
	color: #253445;
}
</style>
