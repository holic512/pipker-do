<!--
@file PracticeSessionNavbar
@project pipker-do
@module 考研英语 / 练习导航
@description 为英语背词页提供独立顶部导航，不复用项目切换按钮。
@logic 1. 读取安全区与胶囊尺寸生成顶部占位；2. 提供轻量退出入口并为内容区让出空间；3. 通过事件把退出行为交给页面处理。
@dependencies Component: uni-icons
@index_tags 考研英语, 独立导航, 背词页, 顶部栏
@author holic512
-->
<template>
	<view class="practice-session-navbar" :style="placeholderStyle">
		<view class="practice-session-navbar__inner" :style="navbarStyle">
			<view class="practice-session-navbar__bar" :style="barStyle">
				<view class="practice-session-navbar__action" @tap="$emit('exit')">
					<uni-icons type="left" size="18" color="#607085" />
					<text>退出</text>
				</view>
				<view class="practice-session-navbar__spacer"></view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'PracticeSessionNavbar',
	emits: ['exit'],
	data() {
		return {
			statusBarHeight: 20,
			horizontalPadding: '24rpx',
			menuButtonInfo: {
				top: 0,
				height: 32
			}
		}
	},
	computed: {
		placeholderStyle(): Record<string, string> {
			return {
				height: `${this.statusBarHeight + this.barHeight}px`
			}
		},
		navbarStyle(): Record<string, string> {
			return {
				width: '100%',
				paddingTop: `${this.statusBarHeight}px`
			}
		},
		barHeight(): number {
			const verticalGap = Math.max(this.menuButtonInfo.top - this.statusBarHeight, 0)
			return this.menuButtonInfo.height + verticalGap * 2
		},
		barStyle(): Record<string, string> {
			return {
				height: `${this.barHeight}px`,
				paddingLeft: this.horizontalPadding,
				paddingRight: this.horizontalPadding
			}
		}
	},
	created() {
		const systemInfo = uni.getSystemInfoSync()
		this.statusBarHeight = Number(systemInfo.statusBarHeight || 20)
		if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
			try {
				const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
				if (menuButtonInfo && menuButtonInfo.height) {
					this.menuButtonInfo = {
						top: Number(menuButtonInfo.top || this.statusBarHeight),
						height: Number(menuButtonInfo.height || 32)
					}
				}
			} catch (error) {
				console.warn('[practice-session-navbar] resolve menu button failed', error)
			}
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-session-navbar {
	position: relative;
	z-index: 20;
}

.practice-session-navbar__inner {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 20;
	background: transparent;
}

.practice-session-navbar__bar {
	display: flex;
	align-items: center;
	box-sizing: border-box;
}

.practice-session-navbar__action {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
	font-size: 24rpx;
	font-weight: 600;
	color: #5f6f84;
}

.practice-session-navbar__action--right {
	justify-content: flex-end;
}

.practice-session-navbar__spacer {
	flex: 1;
}
</style>
