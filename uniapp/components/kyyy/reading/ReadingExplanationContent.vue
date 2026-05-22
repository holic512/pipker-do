<!--
@file ReadingExplanationContent
@project pipker-do
@module 考研英语 / 阅读解释
@description 渲染中心解释弹窗的内容区，承载选中文本、解释录入和查看状态。
@logic 1. 根据 mode 切换输入态与查看态；2. 输出选中文本和解释内容；3. 向上分发关闭、保存和输入更新事件。
@dependencies None
@index_tags 考研英语, 解释弹窗, 中心弹窗, 阅读批注
@author holic512
-->
<template>
	<view class="reading-explanation-content">
		<text class="reading-explanation-content__title">{{ title }}</text>
		<view class="reading-explanation-content__selection">
			<text class="reading-explanation-content__selection-text">{{ selectedText }}</text>
		</view>

		<textarea
			v-if="mode !== 'view'"
			:value="noteContent"
			class="reading-explanation-content__textarea"
			maxlength="200"
			placeholder="写下这段内容的解释。"
			placeholder-class="reading-explanation-content__placeholder"
			:disabled="saving"
			@input="handleInput"
		/>

		<view v-else class="reading-explanation-content__body">
			<text class="reading-explanation-content__body-text">{{ bodyText }}</text>
		</view>

		<view class="reading-explanation-content__meta">
			<text>{{ metaText }}</text>
		</view>

		<view class="reading-explanation-content__actions">
			<view class="reading-explanation-content__button reading-explanation-content__button--ghost" @tap="$emit('close')">
				<text>{{ mode === 'view' ? '关闭' : '取消' }}</text>
			</view>
			<view
				v-if="mode !== 'view'"
				class="reading-explanation-content__button"
				@tap="$emit('save')"
			>
				<text>{{ saving ? '保存中...' : '保存解释' }}</text>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

interface TextareaInputEvent {
	detail?: {
		value?: string
	}
}

export default defineComponent({
	name: 'ReadingExplanationContent',
	props: {
		title: {
			type: String,
			default: '解释'
		},
		mode: {
			type: String,
			default: 'view'
		},
		selectedText: {
			type: String,
			default: ''
		},
		noteContent: {
			type: String,
			default: ''
		},
		saving: {
			type: Boolean,
			default: false
		},
		bodyText: {
			type: String,
			default: ''
		},
		metaText: {
			type: String,
			default: ''
		}
	},
	emits: ['close', 'save', 'update:note-content'],
	methods: {
		handleInput(event: TextareaInputEvent): void {
			this.$emit('update:note-content', event?.detail?.value || '')
		}
	}
})
</script>

<style lang="scss" scoped>
.reading-explanation-content {
	width: 600rpx;
	padding: 28rpx 24rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.reading-explanation-content__title {
	font-size: 32rpx;
	font-weight: 700;
	line-height: 1.5;
	color: #253445;
}

.reading-explanation-content__selection,
.reading-explanation-content__body {
	padding: 20rpx 22rpx;
	border-radius: 20rpx;
	background: #f6f8fb;
	border: 1rpx solid #e3e9ef;
}

.reading-explanation-content__selection-text,
.reading-explanation-content__body-text {
	font-size: 26rpx;
	line-height: 1.5;
	color: #314255;
	white-space: pre-wrap;
}

.reading-explanation-content__textarea {
	width: 100%;
	min-height: 180rpx;
	padding: 22rpx;
	border-radius: 20rpx;
	background: #fbfcfd;
	border: 1rpx solid #dde4ea;
	box-sizing: border-box;
	font-size: 26rpx;
	line-height: 1.5;
	color: #314255;
}

.reading-explanation-content__placeholder {
	color: #98a3b0;
}

.reading-explanation-content__meta {
	font-size: 24rpx;
	line-height: 1.7;
	color: #667483;
}

.reading-explanation-content__actions {
	display: flex;
	gap: 20rpx;
	margin-top: 24rpx;
}

.reading-explanation-content__button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 64rpx;
	padding: 0 28rpx;
	border-radius: 16rpx;
	background: #4b617a;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
}

.reading-explanation-content__button--ghost {
	background: #eef2f5;
	color: #46566a;
}
</style>
