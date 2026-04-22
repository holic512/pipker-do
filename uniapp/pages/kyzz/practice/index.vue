<template>
	<page-shell
		current="practice"
		root-class="tab-page"
		content-class="tab-page-shell-content"
	>
		<view class="tab-page__hero">
			<text class="tab-page__title">练习</text>
			<text class="tab-page__subtitle">中间按钮已作为普通 tab 页接入，并保持凸起主入口视觉。</text>
		</view>

		<view class="tab-page__placeholder">
			<text class="tab-page__placeholder-text">练习页占位内容</text>
		</view>
	</page-shell>
</template>

<script>
export default {
	name: 'PracticePage'
}
</script>

<style lang="scss">
.tab-page {
	height: 100vh;
	box-sizing: border-box;
	background: linear-gradient(180deg, #eef2f8 0%, #f8f9fc 100%);
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.tab-page__hero {
	margin-top: 28rpx;
	padding: 42rpx 34rpx;
	background: linear-gradient(135deg, #59647f 0%, #7783a2 100%);
	border-radius: 30rpx;
}

.tab-page__title {
	display: block;
	font-size: 40rpx;
	font-weight: 700;
	color: #ffffff;
}

.tab-page__subtitle {
	display: block;
	margin-top: 14rpx;
	font-size: 26rpx;
	line-height: 1.7;
	color: rgba(255, 255, 255, 0.8);
}

.tab-page__placeholder {
	flex: 1;
	margin-top: 28rpx;
	min-height: 360rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 34rpx rgba(80, 88, 110, 0.08);
	display: flex;
	align-items: center;
	justify-content: center;
}

.tab-page__placeholder-text {
	font-size: 30rpx;
	font-weight: 600;
	color: #5e6882;
}
</style>
