<template>
	<view class="practice-comment-list">
		<view class="practice-comment-list__head">
			<text class="practice-comment-list__title">评论</text>
		</view>

		<view v-if="loading" class="practice-comment-list__state">
			<text class="practice-comment-list__state-title">正在加载评论...</text>
			<text class="practice-comment-list__state-desc">稍等一下，这里会把大家的思路整理出来。</text>
		</view>

		<view v-else-if="errorMessage && !records.length" class="practice-comment-list__state">
			<text class="practice-comment-list__state-title">评论暂时没加载出来</text>
			<text class="practice-comment-list__state-desc">{{ errorMessage }}</text>
			<button class="practice-comment-list__retry practice-comment-list__retry--state" @tap="$emit('retry')">重新加载</button>
		</view>

		<view v-else-if="!records.length" class="practice-comment-list__state">
			<text class="practice-comment-list__state-title">还没有人开口</text>
			<text class="practice-comment-list__state-desc">你可以先说说自己的理解，给后来的人一点参考。</text>
		</view>

		<scroll-view
			v-else
			scroll-y
			class="practice-comment-list__scroll"
			:lower-threshold="80"
			@scrolltolower="$emit('load-more')"
		>
			<practice-comment-card
				v-for="item in records"
				:key="item.commentId"
				:comment="item"
			/>

			<view class="practice-comment-list__footer">
				<text v-if="loadingMore" class="practice-comment-list__footer-text">正在加载更多评论...</text>
				<button
					v-else-if="errorMessage"
					class="practice-comment-list__retry"
					@tap="$emit('retry')"
				>
					加载失败，点我重试
				</button>
				<text v-else-if="hasMore" class="practice-comment-list__footer-text">继续下滑，看看更多评论</text>
				<text v-else class="practice-comment-list__footer-text">已经到底了，先消化一下这道题</text>
			</view>
		</scroll-view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeCommentItem } from '@/pages/kyzz/practice/types'
import PracticeCommentCard from '@/components/kyzz/practice/PracticeCommentCard.vue'

// AI 索引: KYZZ 刷题页评论列表组件。

export default defineComponent({
	name: 'PracticeCommentList',
	components: {
		PracticeCommentCard
	},
	props: {
		records: {
			type: Array as PropType<KyzzPracticeCommentItem[]>,
			default: () => []
		},
		total: {
			type: Number,
			default: 0
		},
		loading: {
			type: Boolean,
			default: false
		},
		loadingMore: {
			type: Boolean,
			default: false
		},
		hasMore: {
			type: Boolean,
			default: false
		},
		errorMessage: {
			type: String,
			default: ''
		}
	},
	emits: ['load-more', 'retry']
})
</script>

<style lang="scss" scoped>
.practice-comment-list {
	margin-top: 18rpx;
	padding: 28rpx 26rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.97);
	box-shadow: 0 18rpx 36rpx rgba(50, 60, 72, 0.05);
	border: 1rpx solid rgba(217, 225, 236, 0.92);
}

.practice-comment-list__title {
	display: block;
	font-size: 30rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #25303d;
}

.practice-comment-list__state {
	padding: 32rpx 6rpx 12rpx;
}

.practice-comment-list__state-title {
	display: block;
	font-size: 26rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2d3844;
}

.practice-comment-list__state-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 23rpx;
	line-height: 1.7;
	color: #7c8796;
}

.practice-comment-list__scroll {
	max-height: 760rpx;
	margin-top: 20rpx;
}

.practice-comment-list__footer {
	padding: 20rpx 0 6rpx;
	text-align: center;
}

.practice-comment-list__footer-text {
	font-size: 22rpx;
	line-height: 1.5;
	color: #8b95a4;
}

.practice-comment-list__retry {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	margin: 0 auto;
	padding: 0 22rpx;
	height: 58rpx;
	line-height: 58rpx;
	border-radius: 999rpx;
	background: rgba(239, 244, 249, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(209, 219, 232, 0.86);
	font-size: 22rpx;
	font-weight: 600;
	color: #4d627d;
}

.practice-comment-list__retry--state {
	margin-top: 18rpx;
}

.practice-comment-list__retry::after {
	border: 0;
}
</style>
