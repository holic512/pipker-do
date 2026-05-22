<!--
@file ReadingActionMenu
@project pipker-do
@module 考研英语 / 阅读标注
@description 渲染正文或题干上的浮动操作框，承载标注与解释相关动作。
@logic 1. 根据 mode 切换按钮集；2. 固定输出上浮菜单样式；3. 将操作事件统一抛给页面状态机。
@dependencies None
@index_tags 考研英语, 浮动菜单, 标注操作, 阅读解释
@author holic512
-->
<template>
	<view class="reading-action-menu" :style="menuStyle">
		<template v-if="mode === 'selection'">
			<view class="reading-action-menu__button" @tap="$emit('create-mark')">
				<text>标注</text>
			</view>
			<view class="reading-action-menu__button reading-action-menu__button--secondary" @tap="$emit('create-explanation')">
				<text>解释</text>
			</view>
			<view class="reading-action-menu__button reading-action-menu__button--ghost" @tap="$emit('cancel-selection')">
				<text>取消选中</text>
			</view>
		</template>
		<template v-else>
			<view class="reading-action-menu__button reading-action-menu__button--danger" @tap="$emit('remove-annotation')">
				<text>取消标注</text>
			</view>
			<view class="reading-action-menu__button reading-action-menu__button--secondary" @tap="$emit('view-explanation')">
				<text>查看解释</text>
			</view>
		</template>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'ReadingActionMenu',
	props: {
		mode: {
			type: String,
			default: 'selection'
		},
		menuStyle: {
			type: String,
			default: ''
		}
	},
	emits: ['create-mark', 'create-explanation', 'cancel-selection', 'remove-annotation', 'view-explanation']
})
</script>

<style lang="scss" scoped>
.reading-action-menu {
	position: fixed;
	z-index: 30;
	transform: translateX(-50%) translateY(-100%);
	display: flex;
	align-items: center;
	gap: 14rpx;
	padding: 12rpx;
	border-radius: 18rpx;
	background: rgba(33, 43, 55, 0.94);
	box-shadow: 0 20rpx 40rpx rgba(24, 31, 41, 0.2);
}

.reading-action-menu__button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 56rpx;
	padding: 0 24rpx;
	border-radius: 14rpx;
	background: #4b617a;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
}

.reading-action-menu__button--secondary {
	background: #eef2f5;
	color: #46566a;
}

.reading-action-menu__button--ghost {
	background: rgba(255, 255, 255, 0.12);
	color: #f1f4f8;
}

.reading-action-menu__button--danger {
	background: #f6e7e4;
	color: #9a4f48;
}
</style>
