<!--
@file PracticeSessionFooterActions
@project pipker-do
@module 考研英语 / 背词底部操作
@description 渲染背词页底部提示、评分按钮与确认操作按钮。
@logic 1. 未揭晓时提供认识程度选择；2. 揭晓后根据状态显示下一词和记错了动作；3. 固定在底部承载会话交互。
@dependencies Types: @/pages/kyyy/practice/types
@index_tags 考研英语, 背词底栏, 评分按钮, 会话确认
@author holic512
-->
<template>
	<view class="practice-session-footer">
		<text class="practice-session-footer__hint">{{ footerHintText }}</text>
		<view v-if="!revealed" class="practice-session-footer__actions">
			<view
				v-for="item in ratingItems"
				:key="item.value"
				class="practice-session-footer__rate-button"
				@tap="$emit('select-rating', item.value)"
			>
				<text class="practice-session-footer__rate-text">{{ item.label }}</text>
				<view class="practice-session-footer__rate-accent" :class="item.accentClass"></view>
			</view>
		</view>
		<view v-else class="practice-session-footer__confirm" :class="{ 'is-single': !showMistakeAction }">
			<view class="practice-session-footer__confirm-button" @tap="$emit('next')">
				<text>下一词</text>
			</view>
			<view
				v-if="showMistakeAction"
				class="practice-session-footer__confirm-button practice-session-footer__confirm-button--ghost"
				@tap="$emit('mistake')"
			>
				<text>记错了</text>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { KyyyPracticeRating } from '@/pages/kyyy/practice/types'

interface PracticeFooterRatingItem {
	value: KyyyPracticeRating
	label: string
	accentClass: string
}

export default defineComponent({
	name: 'PracticeSessionFooterActions',
	props: {
		revealed: {
			type: Boolean,
			default: false
		},
		footerHintText: {
			type: String,
			default: ''
		},
		showMistakeAction: {
			type: Boolean,
			default: false
		}
	},
	emits: ['select-rating', 'next', 'mistake'],
	computed: {
		ratingItems(): PracticeFooterRatingItem[] {
			return [
				{ value: 'know', label: '认识', accentClass: 'is-know' },
				{ value: 'fuzzy', label: '模糊', accentClass: 'is-fuzzy' },
				{ value: 'unknown', label: '不认识', accentClass: 'is-unknown' }
			]
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-session-footer {
	position: fixed;
	left: 24rpx;
	right: 24rpx;
	bottom: calc(env(safe-area-inset-bottom) + 20rpx);
	padding: 0;
	background: transparent;
	box-shadow: none;
	backdrop-filter: none;
}

.practice-session-footer__hint {
	display: block;
	font-size: 24rpx;
	line-height: 1.6;
	text-align: center;
	color: #445266;
}

.practice-session-footer__actions {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 18rpx;
}

.practice-session-footer__rate-button {
	height: 102rpx;
	border-radius: 30rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	background: transparent;
	box-shadow: none;
}

.practice-session-footer__rate-text {
	font-size: 34rpx;
	line-height: 1.08;
	font-weight: 720;
	letter-spacing: 0.04em;
	color: #111111;
}

.practice-session-footer__rate-accent {
	width: 28rpx;
	height: 6rpx;
	border-radius: 999rpx;
}

.practice-session-footer__rate-accent.is-know {
	background: #18b0a1;
}

.practice-session-footer__rate-accent.is-fuzzy {
	background: #f2be2d;
}

.practice-session-footer__rate-accent.is-unknown {
	background: #ea4a52;
}

.practice-session-footer__confirm {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 18rpx;
}

.practice-session-footer__confirm.is-single {
	grid-template-columns: minmax(0, 1fr);
}

.practice-session-footer__confirm-button {
	height: 88rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #1f2833;
	box-shadow: 0 14rpx 28rpx rgba(44, 57, 74, 0.16);
	font-size: 28rpx;
	font-weight: 700;
	color: #ffffff;
}

.practice-session-footer__confirm-button--ghost {
	background: rgba(255, 255, 255, 0.86);
	box-shadow: inset 0 0 0 1rpx rgba(202, 212, 223, 0.9);
	color: #26313f;
}
</style>
