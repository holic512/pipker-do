<template>
	<!-- AI 索引: 小程序页面壳与冷启动覆盖层挂载点。 -->
	<view class="page-shell" :class="rootClass" :style="rootStyle">
		<custom-navbar @menu-click="$emit('menu-click')" />
		<view class="page-shell__content" :class="contentClass" :style="contentStyle">
			<slot />
		</view>
		<custom-tabbar
			v-if="showTabbar"
			:current="current"
			:items="items"
		/>
		<launch-overlay />
	</view>
</template>

<script>
export default {
	name: 'PageShell',
	props: {
		current: {
			type: String,
			default: ''
		},
		items: {
			type: Array,
			default: null
		},
		showTabbar: {
			type: Boolean,
			default: true
		},
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
		}
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
