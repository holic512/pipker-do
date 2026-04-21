<template>
	<page-shell
		current="question-bank"
		root-class="tab-page"
		content-class="tab-page-shell-content"
	>
		<view class="tab-page__header">
			<text class="tab-page__title">题库</text>
			<text class="tab-page__subtitle">这里预留为题型分类、章节刷题和收藏错题入口。</text>
		</view>

		<view class="tab-page__placeholder">
			<text class="tab-page__placeholder-text">题库页占位内容</text>
		</view>
	</page-shell>
</template>

<script>
export default {
	name: 'QuestionBankPage'
}
</script>

<style lang="scss">
.tab-page {
	height: 100vh;
	box-sizing: border-box;
	background: #f5f7fb;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.tab-page__header {
	margin-top: 28rpx;
	padding: 36rpx 32rpx;
	background: #ffffff;
	border-radius: 28rpx;
	box-shadow: 0 14rpx 34rpx rgba(80, 88, 110, 0.08);
}

.tab-page__title {
	display: block;
	font-size: 40rpx;
	font-weight: 700;
	color: #2f3648;
}

.tab-page__subtitle {
	display: block;
	margin-top: 12rpx;
	font-size: 26rpx;
	line-height: 1.7;
	color: #7a8399;
}

.tab-page__placeholder {
	flex: 1;
	margin-top: 28rpx;
	min-height: 360rpx;
	border-radius: 28rpx;
	background: linear-gradient(135deg, #ffffff 0%, #eef2f8 100%);
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
