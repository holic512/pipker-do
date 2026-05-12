<!--
@file PracticeSessionCard
@project pipker-do
@module 考研英语 / 背词会话卡片
@description 组装背词页当前单词卡片，协调头部进度、星级状态与详情区域。
@logic 1. 透传当前单词与会话展示参数；2. 组合头部和详情两个子组件；3. 保持页面层只关注会话状态流转。
@dependencies Component: PracticeSessionCardHeader, Component: PracticeSessionCardDetail, Types: @/pages/kyyy/practice/types
@index_tags 考研英语, 背词卡片, 组件组装, 会话展示
@author holic512
-->
<template>
	<view class="practice-session-card">
		<practice-session-card-header
			:card="card"
			:progress-text="progressText"
			:progress-percent="progressPercent"
			:current-source-label="currentSourceLabel"
			:show-source-star-badge="showSourceStarBadge"
			:round-star-slots="roundStarSlots"
			:display-round-star-count="displayRoundStarCount"
			:round-stars-state-class="roundStarsStateClass"
		/>
		<practice-session-card-detail :card="card" :revealed="revealed" />
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import PracticeSessionCardHeader from '@/components/kyyy/practice/PracticeSessionCardHeader.vue'
import PracticeSessionCardDetail from '@/components/kyyy/practice/PracticeSessionCardDetail.vue'
import type { KyyyPracticeCardState } from '@/pages/kyyy/practice/types'

export default defineComponent({
	name: 'PracticeSessionCard',
	components: {
		PracticeSessionCardHeader,
		PracticeSessionCardDetail
	},
	props: {
		card: {
			type: Object as PropType<KyyyPracticeCardState>,
			required: true
		},
		revealed: {
			type: Boolean,
			default: false
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
	}
})
</script>

<style lang="scss" scoped>
.practice-session-card {
	position: relative;
	margin-top: 20rpx;
	padding: 0;
	background: transparent;
	box-shadow: none;
	border-radius: 0;
	overflow: visible;
}
</style>
