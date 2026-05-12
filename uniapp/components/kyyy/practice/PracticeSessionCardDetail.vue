<!--
@file PracticeSessionCardDetail
@project pipker-do
@module 考研英语 / 背词卡片详情
@description 渲染背词卡片释义区域，在未揭晓时显示骨架，占位后切换为完整释义详情。
@logic 1. 未揭晓时输出骨架块；2. 揭晓后展示词性、释义、多例句和相关词；3. 保持详情区样式和骨架动画收敛。
@dependencies Types: @/pages/kyyy/practice/types
@index_tags 考研英语, 卡片详情, 骨架屏, 单词释义
@author holic512
-->
<template>
	<view v-if="!revealed" class="practice-session-card-detail practice-session-card-detail--skeleton">
		<view class="practice-session-card-detail__skeleton-line practice-session-card-detail__skeleton-line--meaning"></view>
		<view class="practice-session-card-detail__skeleton-line practice-session-card-detail__skeleton-line--meaning-short"></view>
	</view>

	<view v-else class="practice-session-card-detail">
		<view class="practice-session-card-detail__meaning-row">
			<text v-if="card.partOfSpeech" class="practice-session-card-detail__part-of-speech">
				{{ card.partOfSpeech }}
			</text>
			<text class="practice-session-card-detail__meaning">{{ card.meaningCn || '释义暂缺' }}</text>
		</view>

		<view class="practice-session-card-detail__block practice-session-card-detail__block--example">
			<view class="practice-session-card-detail__title">
				<view class="practice-session-card-detail__mark"></view>
				<text>例句</text>
			</view>
			<view class="practice-session-card-detail__divider practice-session-card-detail__divider--example"></view>
			<view v-if="card.examples.length" class="practice-session-card-detail__example-list">
				<view
					v-for="(example, exampleIndex) in card.examples"
					:key="`${example.id || example.exampleSentence}-${exampleIndex}`"
					class="practice-session-card-detail__example-item"
				>
					<text class="practice-session-card-detail__text">
						{{ example.exampleSentence }}
					</text>
					<text v-if="example.exampleTranslation" class="practice-session-card-detail__subtext">
						{{ example.exampleTranslation }}
					</text>
				</view>
			</view>
			<view v-else class="practice-session-card-detail__example-item practice-session-card-detail__example-item--fallback">
				<text class="practice-session-card-detail__text">
					{{ card.exampleSentence || '当前单词暂未补充例句。' }}
				</text>
				<text v-if="card.exampleTranslation" class="practice-session-card-detail__subtext">
					{{ card.exampleTranslation }}
				</text>
			</view>
		</view>

		<view v-if="card.relatedWords.length" class="practice-session-card-detail__block practice-session-card-detail__block--related">
			<view class="practice-session-card-detail__title">
				<view class="practice-session-card-detail__mark"></view>
				<text>相关词</text>
			</view>
			<view class="practice-session-card-detail__divider practice-session-card-detail__divider--related"></view>
			<view class="practice-session-card-detail__related-list">
				<view
					v-for="item in card.relatedWords"
					:key="`${item.id || item.relatedWordText}-${item.relationType}`"
					class="practice-session-card-detail__related-item"
				>
					<text class="practice-session-card-detail__related-word">{{ item.relatedWordText }}</text>
					<text class="practice-session-card-detail__related-meaning">{{ item.meaningCn }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyyyPracticeCardState } from '@/pages/kyyy/practice/types'

export default defineComponent({
	name: 'PracticeSessionCardDetail',
	props: {
		card: {
			type: Object as PropType<KyyyPracticeCardState>,
			required: true
		},
		revealed: {
			type: Boolean,
			default: false
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-session-card-detail {
	position: relative;
	z-index: 1;
	margin-top: 44rpx;
	padding: 0 4rpx;
}

.practice-session-card-detail--skeleton {
	overflow: visible;
}

.practice-session-card-detail__skeleton-line,
.practice-session-card-detail__skeleton-chip {
	background: rgba(217, 224, 234, 0.96);
}

.practice-session-card-detail__skeleton-line {
	border-radius: 999rpx;
}

.practice-session-card-detail__skeleton-line--meaning {
	width: 68%;
	height: 30rpx;
}

.practice-session-card-detail__skeleton-line--meaning-short {
	width: 42%;
	height: 30rpx;
	margin-top: 16rpx;
}

.practice-session-card-detail__skeleton-line--text {
	width: 92%;
	height: 24rpx;
	margin-top: 16rpx;
}

.practice-session-card-detail__skeleton-line--text-short {
	width: 64%;
	height: 24rpx;
	margin-top: 14rpx;
}

.practice-session-card-detail__skeleton-chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-top: 16rpx;
}

.practice-session-card-detail__skeleton-chip {
	width: 126rpx;
	height: 56rpx;
	border-radius: 18rpx;
}

.practice-session-card-detail__skeleton-chip--wide {
	width: 168rpx;
}

.practice-session-card-detail__meaning-row {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
}

.practice-session-card-detail__part-of-speech {
	margin-top: 2rpx;
	font-size: 24rpx;
	line-height: 1.5;
	font-weight: 700;
	color: #4c5c72;
	white-space: nowrap;
}

.practice-session-card-detail__meaning {
	flex: 1;
	font-size: 31rpx;
	line-height: 1.64;
	font-weight: 700;
	color: #1f2b39;
}

.practice-session-card-detail__block {
	margin-top: 28rpx;
}

.practice-session-card-detail__divider {
	width: 100%;
	height: 3rpx;
	margin-top: 12rpx;
	border-radius: 999rpx;
	background: rgba(224, 231, 240, 0.82);
	overflow: hidden;
}

.practice-session-card-detail__divider::after {
	content: '';
	display: block;
	height: 100%;
	width: 132rpx;
	border-radius: inherit;
}

.practice-session-card-detail__divider--example::after {
	background: linear-gradient(90deg, rgba(98, 124, 168, 0.95), rgba(98, 124, 168, 0.14));
}

.practice-session-card-detail__divider--related::after {
	background: linear-gradient(90deg, rgba(191, 148, 77, 0.95), rgba(191, 148, 77, 0.14));
}

.practice-session-card-detail__title {
	display: flex;
	align-items: center;
	gap: 10rpx;
	font-size: 24rpx;
	font-weight: 700;
	color: #334256;
}

.practice-session-card-detail__mark {
	width: 10rpx;
	height: 10rpx;
	border-radius: 999rpx;
	background: #596d87;
}

.practice-session-card-detail__example-list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 14rpx;
}

.practice-session-card-detail__example-item {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.practice-session-card-detail__example-item--fallback {
	margin-top: 14rpx;
}

.practice-session-card-detail__text {
	display: block;
	font-size: 27rpx;
	line-height: 1.78;
	color: #24303f;
}

.practice-session-card-detail__subtext {
	display: block;
	font-size: 24rpx;
	line-height: 1.7;
	color: #566579;
}

.practice-session-card-detail__related-list {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-top: 16rpx;
}

.practice-session-card-detail__related-item {
	min-width: 0;
}

.practice-session-card-detail__related-word {
	display: block;
	font-size: 26rpx;
	line-height: 1.4;
	font-weight: 700;
	color: #293444;
}

.practice-session-card-detail__related-meaning {
	display: block;
	margin-top: 6rpx;
	font-size: 23rpx;
	line-height: 1.56;
	color: #566579;
}
</style>
