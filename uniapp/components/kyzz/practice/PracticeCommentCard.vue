<template>
	<view class="practice-comment-card">
		<view v-if="comment.author.avatarUrl" class="practice-comment-card__avatar-shell">
			<image class="practice-comment-card__avatar" :src="comment.author.avatarUrl" mode="aspectFill" />
		</view>
		<view v-else class="practice-comment-card__avatar-shell practice-comment-card__avatar-shell--fallback">
			<text class="practice-comment-card__avatar-fallback">{{ authorInitial }}</text>
		</view>

		<view class="practice-comment-card__main">
			<view class="practice-comment-card__meta">
				<view class="practice-comment-card__name-row">
					<text class="practice-comment-card__name">{{ authorName }}</text>
					<text v-if="comment.isMine" class="practice-comment-card__mine">我</text>
				</view>
				<text class="practice-comment-card__time">{{ comment.createdAtLabel }}</text>
			</view>

			<text class="practice-comment-card__content">{{ comment.content }}</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeCommentItem } from '@/pages/kyzz/practice/types'
import { buildCommentAuthorInitial, resolveCommentAuthorName } from '@/pages/kyzz/practice/view'

// AI 索引: KYZZ 刷题页评论卡片组件。

export default defineComponent({
	name: 'PracticeCommentCard',
	props: {
		comment: {
			type: Object as PropType<KyzzPracticeCommentItem>,
			required: true
		}
	},
	computed: {
		authorName(): string {
			return resolveCommentAuthorName(this.comment.author.nickname)
		},
		authorInitial(): string {
			return buildCommentAuthorInitial(this.authorName)
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-comment-card {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 24rpx 0;
}

.practice-comment-card + .practice-comment-card {
	border-top: 1rpx solid rgba(220, 227, 237, 0.84);
}

.practice-comment-card__avatar-shell {
	width: 68rpx;
	height: 68rpx;
	border-radius: 50%;
	overflow: hidden;
	flex-shrink: 0;
	background: rgba(226, 234, 243, 0.98);
	box-shadow: inset 0 0 0 1rpx rgba(206, 216, 229, 0.84);
}

.practice-comment-card__avatar-shell--fallback {
	display: flex;
	align-items: center;
	justify-content: center;
}

.practice-comment-card__avatar {
	width: 100%;
	height: 100%;
}

.practice-comment-card__avatar-fallback {
	font-size: 24rpx;
	font-weight: 700;
	color: #4f6078;
}

.practice-comment-card__main {
	flex: 1;
	min-width: 0;
}

.practice-comment-card__meta {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.practice-comment-card__name-row {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	min-width: 0;
}

.practice-comment-card__name {
	font-size: 25rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #29323f;
}

.practice-comment-card__mine {
	padding: 4rpx 10rpx;
	border-radius: 999rpx;
	background: rgba(223, 232, 247, 0.96);
	font-size: 18rpx;
	line-height: 1;
	font-weight: 600;
	color: #526885;
}

.practice-comment-card__time {
	font-size: 21rpx;
	line-height: 1.2;
	color: #8a94a2;
	flex-shrink: 0;
}

.practice-comment-card__content {
	display: block;
	margin-top: 12rpx;
	font-size: 25rpx;
	line-height: 1.8;
	color: #35404d;
	white-space: pre-wrap;
	word-break: break-word;
}
</style>
