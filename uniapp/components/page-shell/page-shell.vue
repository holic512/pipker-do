<!--
@file PageShell
@project pipker-do
@module 小程序 / 页面壳
@description 提供页面内容承载、通用导航开关与冷启动覆盖层挂载能力。
@logic 1. 按需渲染通用导航栏；2. 承载页面内容与底部插槽；3. 在挂载时统一隐藏系统 tabBar。
@dependencies Component: CustomNavbar, Component: LaunchOverlay
@index_tags 小程序, 页面壳, 通用导航, 页面布局
@author holic512
-->
<template>
	<!-- AI 索引: 小程序页面壳与冷启动覆盖层挂载点。 -->
	<view class="page-shell" :class="rootClass" :style="rootStyle">
		<custom-navbar v-if="showNavbar" @menu-click="$emit('menu-click')">
			<slot name="navbar" />
		</custom-navbar>
		<view class="page-shell__content" :class="contentClass" :style="contentStyle">
			<slot />
		</view>
		<slot name="tabbar" />
		<launch-overlay />
	</view>
</template>

<script>
export default {
	name: 'PageShell',
	emits: ['menu-click'],
	props: {
		rootClass: {
			type: String,
			default: ''
		},
		contentClass: {
			type: String,
			default: ''
		},
		rootStyle: {
			type: [String, Object],
			default: ''
		},
		contentStyle: {
			type: [String, Object],
			default: ''
		},
		showNavbar: {
			type: Boolean,
			default: true
		}
	},
	mounted() {
		uni.hideTabBar({ animation: false }).catch(() => {})
	}
}
</script>

<style lang="scss" scoped>
.page-shell {
	min-height: 100vh;
	box-sizing: border-box;
}

.page-shell__content {
	display: flex;
	flex: 1;
	flex-direction: column;
	min-height: 0;
	box-sizing: border-box;
}

.tab-page-shell-content {
	padding: 0 32rpx calc(env(safe-area-inset-bottom) + 172rpx);
}
</style>
