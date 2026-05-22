<!--
@file ReadingTokenFlow
@project pipker-do
@module 考研英语 / 阅读标注
@description 统一渲染阅读正文或题干的 token 流，并透出长按与点击事件。
@logic 1. 按 token 顺序输出文本片段；2. 通过外部 class resolver 控制高亮态；3. 统一向上分发长按和点击事件。
@dependencies Types: @/pages/kyyy/reading/types
@index_tags 考研英语, token流, 阅读标注, 文本高亮
@author holic512
-->
<template>
	<view class="reading-token-flow">
		<text
			v-for="token in tokens"
			:key="`${tokenKeyPrefix}-${token.key}`"
			class="reading-token-flow__token"
			:class="resolveTokenClass(token)"
			@longpress="handleTokenLongPress(token, $event)"
			@tap="handleTokenTap(token, $event)"
		>{{ token.text }}</text>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyyyReadingTextToken } from '@/pages/kyyy/reading/types'

type TokenClassResolver = (token: KyyyReadingTextToken) => Record<string, boolean>

export default defineComponent({
	name: 'ReadingTokenFlow',
	props: {
		tokens: {
			type: Array as PropType<KyyyReadingTextToken[]>,
			default: () => []
		},
		tokenKeyPrefix: {
			type: String,
			default: 'token'
		},
		tokenClassResolver: {
			type: Function as PropType<TokenClassResolver>,
			default: null
		}
	},
	emits: ['token-longpress', 'token-tap'],
	methods: {
		resolveTokenClass(token: KyyyReadingTextToken): Record<string, boolean> {
			if (typeof this.tokenClassResolver === 'function') {
				return this.tokenClassResolver(token)
			}
			return {}
		},
		handleTokenLongPress(token: KyyyReadingTextToken, event: unknown): void {
			this.$emit('token-longpress', {
				token,
				event
			})
		},
		handleTokenTap(token: KyyyReadingTextToken, event: unknown): void {
			this.$emit('token-tap', {
				token,
				event
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.reading-token-flow {
	white-space: pre-wrap;
	word-break: break-word;
}

.reading-token-flow__token {
	font-size: 28rpx;
	line-height: 1.25;
	color: #314255;
	border-radius: 8rpx;
	padding: 0 2rpx;
	transition: background-color 0.18s ease, color 0.18s ease;
}

.reading-token-flow__token.is-mark {
	background: rgba(248, 229, 96, 0.72);
}

.reading-token-flow__token.is-explanation {
	background: rgba(164, 223, 183, 0.82);
	color: #1f4d2f;
}

.reading-token-flow__token.is-preview {
	background: rgba(186, 204, 238, 0.68);
}

.reading-token-flow__token.is-selectable:active {
	opacity: 0.82;
}
</style>
