<!--
@file PracticeSessionCardHeader
@project pipker-do
@module 考研英语 / 背词卡片头部
@description 渲染背词会话卡片的环状进度、星级状态、单词主体和音标。
@logic 1. 展示当前进度和百分比；2. 根据评分状态驱动星徽和轮次动效；3. 展示单词和音标信息。
@dependencies Types: @/pages/kyyy/practice/types, Component: uni-icons
@index_tags 考研英语, 卡片头部, 环状进度, 星级状态
@author holic512
-->
<template>
	<view class="practice-session-card-header">
		<view class="practice-session-card-header__top">
			<view class="practice-session-card-header__progress">
				<view class="practice-session-card-header__progress-ring" :style="progressRingStyle">
					<view class="practice-session-card-header__progress-core"></view>
				</view>
				<view class="practice-session-card-header__progress-copy">
					<text class="practice-session-card-header__progress-percent">{{ progressPercent }}%</text>
					<text class="practice-session-card-header__progress-text">{{ progressText }}</text>
				</view>
			</view>

			<view class="practice-session-card-header__meta">
				<view v-if="!showSourceStarBadge" class="practice-session-card-header__tag">{{ currentSourceLabel }}</view>

				<view class="practice-session-card-header__round-stars" :class="roundStarsStateClass">
					<view
						v-for="slot in roundStarSlots"
						:key="`round-star-${slot}`"
						class="practice-session-card-header__round-star"
						:class="{ 'is-filled': slot < displayRoundStarCount }"
					>
						<uni-icons
							:type="slot < displayRoundStarCount ? 'star-filled' : 'star'"
							size="12"
							:color="slot < displayRoundStarCount ? '#d3a14d' : '#c4ceda'"
						/>
					</view>
				</view>
			</view>
		</view>

		<view class="practice-session-card-header__head">
			<text class="practice-session-card-header__word">{{ card.wordText }}</text>
			<view v-if="phoneticText" class="practice-session-card-header__phonetic-row">
				<view class="practice-session-card-header__phonetic-pill">
					<text>美</text>
					<uni-icons type="sound" size="12" color="#7d8898" />
				</view>
				<text class="practice-session-card-header__phonetic">{{ phoneticText }}</text>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyyyPracticeCardState } from '@/pages/kyyy/practice/types'

export default defineComponent({
	name: 'PracticeSessionCardHeader',
	props: {
		card: {
			type: Object as PropType<KyyyPracticeCardState>,
			required: true
		},
		progressText: {
			type: String,
			default: '0/20'
		},
		progressPercent: {
			type: Number,
			default: 0
		},
		currentSourceLabel: {
			type: String,
			default: ''
		},
		showSourceStarBadge: {
			type: Boolean,
			default: false
		},
		roundStarSlots: {
			type: Array as PropType<number[]>,
			default: () => [0, 1, 2]
		},
		displayRoundStarCount: {
			type: Number,
			default: 1
		},
		roundStarsStateClass: {
			type: String,
			default: ''
		}
	},
	computed: {
		progressRingStyle(): Record<string, string> {
			const degree = Math.round((Math.min(Math.max(this.progressPercent, 0), 100) / 100) * 360)
			return {
				background: `conic-gradient(#7d95c7 0deg ${degree}deg, rgba(213, 222, 235, 0.62) ${degree}deg 360deg)`
			}
		},
		phoneticText(): string {
			return this.card.phoneticUs || this.card.phoneticUk || ''
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-session-card-header {
	position: relative;
}

.practice-session-card-header__top,
.practice-session-card-header__head {
	position: relative;
	z-index: 1;
}

.practice-session-card-header__top {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 14rpx;
	padding: 0 4rpx;
}

.practice-session-card-header__progress {
	display: flex;
	align-items: center;
	gap: 10rpx;
	flex-shrink: 0;
}

.practice-session-card-header__progress-ring {
	width: 56rpx;
	height: 56rpx;
	padding: 4rpx;
	border-radius: 999rpx;
	box-sizing: border-box;
}

.practice-session-card-header__progress-core {
	width: 100%;
	height: 100%;
	border-radius: 999rpx;
	background: #ffffff;
}

.practice-session-card-header__progress-copy {
	height: 56rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 8rpx;
	box-sizing: border-box;
}

.practice-session-card-header__progress-percent {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 800;
	color: #263649;
}

.practice-session-card-header__progress-text {
	font-size: 18rpx;
	line-height: 1;
	font-weight: 700;
	color: #4d5b6f;
	letter-spacing: 0.03em;
}

.practice-session-card-header__meta {
	display: inline-flex;
	align-items: center;
	justify-content: flex-end;
	gap: 10rpx;
	flex-wrap: wrap;
	margin-left: auto;
	padding-top: 0;
}

.practice-session-card-header__tag {
	display: inline-flex;
	align-items: center;
	height: 40rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(88, 112, 141, 0.12);
	font-size: 18rpx;
	font-weight: 700;
	color: #334154;
}

.practice-session-card-header__round-stars {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	min-height: 40rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.84);
	box-shadow: inset 0 0 0 1rpx rgba(223, 230, 239, 0.96);
}

.practice-session-card-header__round-star {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	transform-origin: center;
}

.practice-session-card-header__round-stars.is-success {
	animation: kyyy-practice-stars-rise 0.56s ease;
}

.practice-session-card-header__round-stars.is-success .practice-session-card-header__round-star.is-filled:nth-child(2),
.practice-session-card-header__round-stars.is-success .practice-session-card-header__round-star.is-filled:nth-child(3) {
	animation: kyyy-practice-stars-pop 0.5s ease;
}

.practice-session-card-header__round-stars.is-failure {
	animation: kyyy-practice-star-fail 0.42s ease;
}

.practice-session-card-header__round-stars.is-failure .practice-session-card-header__round-star {
	opacity: 0.7;
}

.practice-session-card-header__round-stars.is-fuzzy {
	animation: kyyy-practice-star-soft 0.56s ease;
}

.practice-session-card-header__head {
	margin-top: 12rpx;
	padding: 0 4rpx;
}

.practice-session-card-header__word {
	display: block;
	font-size: 88rpx;
	line-height: 1.04;
	font-weight: 820;
	letter-spacing: -0.05em;
	color: #151d28;
	word-break: break-word;
}

.practice-session-card-header__phonetic-row {
	margin-top: 20rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.practice-session-card-header__phonetic-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	height: 46rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.84);
	box-shadow: inset 0 0 0 1rpx rgba(220, 227, 236, 0.92);
	font-size: 20rpx;
	font-weight: 700;
	color: #4f5d71;
}

.practice-session-card-header__phonetic {
	font-size: 38rpx;
	line-height: 1.3;
	color: #46566d;
}

@keyframes kyyy-practice-star-fail {
	0%,
	100% {
		transform: translateX(0);
	}
	25% {
		transform: translateX(-7rpx);
	}
	50% {
		transform: translateX(7rpx);
	}
	75% {
		transform: translateX(-4rpx);
	}
}

@keyframes kyyy-practice-star-soft {
	0% {
		transform: scale(0.96);
		opacity: 0.86;
	}
	100% {
		transform: scale(1);
		opacity: 1;
	}
}

@keyframes kyyy-practice-stars-rise {
	0% {
		transform: translateY(8rpx);
		opacity: 0.76;
	}
	100% {
		transform: translateY(0);
		opacity: 1;
	}
}

@keyframes kyyy-practice-stars-pop {
	0% {
		transform: scale(0.68);
		opacity: 0.3;
	}
	65% {
		transform: scale(1.18);
		opacity: 1;
	}
	100% {
		transform: scale(1);
	}
}
</style>
