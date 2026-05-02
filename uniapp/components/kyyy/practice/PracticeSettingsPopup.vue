<template>
	<view class="practice-settings-popup">
		<view class="practice-settings-popup__header">
			<view class="practice-settings-popup__title-wrap">
				<text class="practice-settings-popup__title">刷题设置</text>
				<text class="practice-settings-popup__subtitle">考试方向会同步到当前账号，并用于后续英语练习与真题筛选</text>
			</view>
			<button class="practice-settings-popup__close" @tap="$emit('close')">
				<uni-icons type="closeempty" size="22" color="#64748b" />
			</button>
		</view>

		<view class="practice-settings-popup__body">
			<view class="practice-settings-popup__copy">
				<text class="practice-settings-popup__item-title">考试方向</text>
				<text class="practice-settings-popup__item-desc">按你的目标考试选择英一或英二</text>
			</view>
			<view class="practice-settings-popup__segmented">
				<button
					v-for="item in normalizedOptions"
					:key="item.value"
					class="practice-settings-popup__segment"
					:class="{ 'practice-settings-popup__segment--active': item.value === examDirection }"
					:disabled="syncing"
					@tap="handleExamDirectionChange(item.value)"
				>
					<text>{{ item.label }}</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { KyyyExamDirection, KyyyPracticeSettingOption } from '@/pages/kyyy/practice/types'
import { DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS } from '@/pages/kyyy/practice/view'

// AI 索引: KYYY 刷题设置弹窗组件。

export default defineComponent({
	name: 'KyyyPracticeSettingsPopup',
	props: {
		examDirection: {
			type: String as PropType<KyyyExamDirection>,
			default: 'english_one'
		},
		examDirectionOptions: {
			type: Array as PropType<KyyyPracticeSettingOption[]>,
			default: () => DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS
		},
		syncing: {
			type: Boolean,
			default: false
		}
	},
	emits: ['close', 'change-exam-direction'],
	computed: {
		normalizedOptions(): KyyyPracticeSettingOption[] {
			return this.examDirectionOptions && this.examDirectionOptions.length
				? this.examDirectionOptions
				: DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS
		}
	},
	methods: {
		handleExamDirectionChange(value: KyyyExamDirection): void {
			if (this.syncing || value === this.examDirection) {
				return
			}
			this.$emit('change-exam-direction', value)
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-settings-popup {
	padding: 28rpx 28rpx calc(env(safe-area-inset-bottom) + 30rpx);
	background: #ffffff;
	box-sizing: border-box;
}

.practice-settings-popup__header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
}

.practice-settings-popup__title-wrap {
	min-width: 0;
	flex: 1;
}

.practice-settings-popup__title {
	display: block;
	font-size: 32rpx;
	line-height: 1.25;
	font-weight: 800;
	color: #1f2937;
}

.practice-settings-popup__subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	line-height: 1.45;
	color: #6b778a;
}

.practice-settings-popup__close {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 58rpx;
	height: 58rpx;
	margin: 0;
	padding: 0;
	border-radius: 999rpx;
	background: #f6f8fb;
	box-shadow: inset 0 0 0 1rpx #d1dae6;
	flex-shrink: 0;
}

.practice-settings-popup__close::after,
.practice-settings-popup__segment::after {
	border: 0;
}

.practice-settings-popup__body {
	margin-top: 30rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 28rpx;
}

.practice-settings-popup__copy {
	min-width: 0;
	flex: 1;
}

.practice-settings-popup__item-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.35;
	font-weight: 750;
	color: #263142;
}

.practice-settings-popup__item-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.45;
	color: #738096;
}

.practice-settings-popup__segmented {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 6rpx;
	border-radius: 999rpx;
	background: #f1f5f9;
	box-shadow: inset 0 0 0 1rpx #d8e0ea;
	flex-shrink: 0;
}

.practice-settings-popup__segment {
	display: flex;
	align-items: center;
	justify-content: center;
	min-width: 104rpx;
	height: 58rpx;
	margin: 0;
	padding: 0 20rpx;
	border-radius: 999rpx;
	background: transparent;
	color: #64748b;
	font-size: 25rpx;
	line-height: 1;
	font-weight: 700;
}

.practice-settings-popup__segment--active {
	background: #5f7493;
	color: #ffffff;
	box-shadow: 0 8rpx 18rpx rgba(65, 80, 101, 0.18);
}
</style>
