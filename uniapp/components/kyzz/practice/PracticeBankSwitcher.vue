<template>
	<view class="practice-bank-switcher">
		<view class="practice-bank-switcher__head">
			<text class="practice-bank-switcher__title">切换当前题库</text>
			<text class="practice-bank-switcher__close" @tap="$emit('close')">收起</text>
		</view>

		<view class="practice-bank-switcher__list">
			<view
				v-for="item in banks"
				:key="item.bankId"
				class="practice-bank-switcher__item"
				:class="{ 'practice-bank-switcher__item--current': currentBankId === item.bankId }"
				@tap="$emit('switch-bank', item)"
			>
				<view
					v-if="item.coverUrl"
					class="practice-bank-switcher__cover"
					:style="{ backgroundImage: `url(${item.coverUrl})` }"
				></view>
				<view v-else class="practice-bank-switcher__cover practice-bank-switcher__cover--fallback">
					{{ buildCoverInitial(item.bankName) }}
				</view>

				<view class="practice-bank-switcher__copy">
					<text class="practice-bank-switcher__name">{{ item.bankName }}</text>
					<text class="practice-bank-switcher__sub">{{ item.resumeLabel }} · {{ item.questionCount }} 题</text>
				</view>

				<view class="practice-bank-switcher__side">
					<text class="practice-bank-switcher__progress">{{ formatProgress(item.currentProgress) }}</text>
					<text class="practice-bank-switcher__date">{{ formatLastPractice(item.lastPracticeAt) }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyzzPracticeBankViewRecord } from '@/pages/kyzz/practice/types'
import { buildCoverInitial, formatLastPractice, formatProgress } from '@/pages/kyzz/practice/view'

// AI 索引: KYZZ 刷题页题库切换组件。

export default defineComponent({
	name: 'PracticeBankSwitcher',
	props: {
		banks: {
			type: Array as PropType<KyzzPracticeBankViewRecord[]>,
			default: () => []
		},
		currentBankId: {
			type: Number as PropType<number | null>,
			default: null
		}
	},
	emits: ['switch-bank', 'close'],
	methods: {
		buildCoverInitial,
		formatLastPractice,
		formatProgress
	}
})
</script>

<style lang="scss" scoped>
.practice-bank-switcher {
	padding: 28rpx 24rpx calc(env(safe-area-inset-bottom) + 24rpx);
}

.practice-bank-switcher__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
}

.practice-bank-switcher__title {
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2e3644;
}

.practice-bank-switcher__close {
	font-size: 22rpx;
	line-height: 1;
	color: #7b8594;
}

.practice-bank-switcher__list {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	margin-top: 24rpx;
	max-height: 65vh;
}

.practice-bank-switcher__item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 18rpx;
	border-radius: 24rpx;
	background: rgba(245, 247, 251, 0.9);
}

.practice-bank-switcher__item--current {
	background: rgba(235, 240, 247, 0.96);
	box-shadow: inset 0 0 0 2rpx rgba(101, 114, 138, 0.14);
}

.practice-bank-switcher__cover {
	width: 72rpx;
	height: 72rpx;
	border-radius: 20rpx;
	background-position: center;
	background-size: cover;
	flex-shrink: 0;
}

.practice-bank-switcher__cover--fallback {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(145deg, #eef3ff 0%, #dde7fb 100%);
	font-size: 28rpx;
	line-height: 1;
	font-weight: 700;
	color: #58657b;
}

.practice-bank-switcher__copy {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
}

.practice-bank-switcher__name {
	font-size: 26rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2d3644;
}

.practice-bank-switcher__sub {
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.5;
	color: #7b8694;
}

.practice-bank-switcher__side {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	flex-shrink: 0;
}

.practice-bank-switcher__progress {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #4f5d74;
}

.practice-bank-switcher__date {
	margin-top: 8rpx;
	font-size: 20rpx;
	line-height: 1.2;
	color: #8a93a1;
}
</style>
