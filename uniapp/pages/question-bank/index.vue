<template>
	<page-shell
		current="question-bank"
		root-class="question-bank-page"
		content-class="question-bank-page__content"
	>
		<view class="question-bank-page__search-row">
			<view class="question-bank-page__search">
				<uni-icons type="search" size="17" color="#8b93a6" />
				<text class="question-bank-page__search-text">搜索题库、知识点...</text>
			</view>

			<view class="question-bank-page__filter">
				<view class="question-bank-page__filter-line question-bank-page__filter-line--top"></view>
				<view class="question-bank-page__filter-line question-bank-page__filter-line--bottom"></view>
			</view>
		</view>

		<text class="question-bank-page__title">我的题库</text>

		<view class="question-bank-page__list">
			<view
				v-for="item in banks"
				:key="item.id"
				class="question-bank-page__card"
			>
				<view class="question-bank-page__card-main">
					<view
						class="question-bank-page__icon-box"
						:class="item.iconClass"
					>
						<view v-if="item.iconType === 'bookmark'" class="icon-bookmark">
							<view class="icon-bookmark__cut"></view>
						</view>
						<text v-else-if="item.iconType === 'translate'" class="icon-text icon-text--translate">文A</text>
						<view v-else class="icon-grid">
							<view class="icon-grid__row">
								<view class="icon-grid__dot"></view>
								<view class="icon-grid__dot"></view>
							</view>
							<view class="icon-grid__row">
								<view class="icon-grid__dot"></view>
								<view class="icon-grid__dot"></view>
							</view>
						</view>
					</view>

					<view class="question-bank-page__body">
						<view class="question-bank-page__head">
							<view class="question-bank-page__head-copy">
								<text class="question-bank-page__name">{{ item.name }}</text>
								<view class="question-bank-page__meta-row">
									<view class="question-bank-page__stars">
										<uni-icons
											v-for="star in 5"
											:key="`${item.id}-${star}`"
											type="star-filled"
											size="10"
											:color="star <= item.starCount ? '#F5B638' : '#E8EBF2'"
										/>
									</view>
									<text class="question-bank-page__hint">{{ item.hint }}</text>
								</view>
								<text class="question-bank-page__count">≡ 共 {{ item.count }}</text>
							</view>

							<view v-if="item.badge" class="question-bank-page__badge">{{ item.badge }}</view>
							<view v-else class="question-bank-page__more">
								<text class="question-bank-page__more-dot"></text>
								<text class="question-bank-page__more-dot"></text>
								<text class="question-bank-page__more-dot"></text>
							</view>
						</view>

						<view class="question-bank-page__progress-row">
							<view class="question-bank-page__progress-track">
								<view
									class="question-bank-page__progress-value"
									:style="{ width: `${item.progress}%` }"
								></view>
							</view>
							<text class="question-bank-page__progress-text">{{ item.progress }}%</text>
						</view>
					</view>
				</view>
			</view>

			<view class="question-bank-page__add-card">
				<view class="question-bank-page__add-circle">+</view>
				<text class="question-bank-page__add-text">添加新题库</text>
			</view>
		</view>
	</page-shell>
</template>

<script>
export default {
	name: 'QuestionBankPage',
	data() {
		return {
			banks: [
				{
					id: 'history',
					name: '考研政治历年真题',
					hint: '推荐度极高',
					count: '1200 题',
					progress: 45,
					starCount: 5,
					badge: '正在刷题',
					iconType: 'bookmark',
					iconClass: 'question-bank-page__icon-box--blue'
				},
				{
					id: 'english',
					name: '英语核心词汇练练',
					hint: '重点钻句',
					count: '5500 题',
					progress: 82,
					starCount: 4,
					badge: '',
					iconType: 'translate',
					iconClass: 'question-bank-page__icon-box--sky'
				},
				{
					id: 'math',
					name: '数学一模拟冲刺卷',
					hint: '阶段提升',
					count: '20 套（440 题）',
					progress: 15,
					starCount: 0,
					badge: '',
					iconType: 'grid',
					iconClass: 'question-bank-page__icon-box--purple'
				}
			]
		}
	}
}
</script>

<style lang="scss">
.question-bank-page {
	min-height: 100vh;
	background: #f6f7fb;
	box-sizing: border-box;
}

.question-bank-page__content {
	padding: 10rpx 14rpx calc(env(safe-area-inset-bottom) + 168rpx);
	box-sizing: border-box;
}

.question-bank-page__search-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.question-bank-page__search {
	flex: 1;
	height: 82rpx;
	padding: 0 22rpx;
	border: 2rpx solid #ebeef5;
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 6rpx 20rpx rgba(125, 136, 162, 0.08);
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.question-bank-page__search-text {
	font-size: 25rpx;
	color: #9ba3b3;
}

.question-bank-page__filter {
	position: relative;
	width: 64rpx;
	height: 64rpx;
	border-radius: 20rpx;
	background: #dfe6ff;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 18rpx rgba(126, 144, 202, 0.18);
}

.question-bank-page__filter-line {
	position: absolute;
	height: 4rpx;
	border-radius: 999rpx;
	background: #5f6f92;
}

.question-bank-page__filter-line::after {
	content: '';
	position: absolute;
	top: 50%;
	width: 10rpx;
	height: 10rpx;
	border-radius: 50%;
	background: #5f6f92;
	transform: translateY(-50%);
}

.question-bank-page__filter-line--top {
	width: 24rpx;
	transform: translateY(-8rpx);
}

.question-bank-page__filter-line--top::after {
	right: -2rpx;
}

.question-bank-page__filter-line--bottom {
	width: 18rpx;
	transform: translateY(8rpx);
}

.question-bank-page__filter-line--bottom::after {
	left: -2rpx;
}

.question-bank-page__title {
	display: block;
	margin-top: 34rpx;
	margin-bottom: 24rpx;
	font-size: 46rpx;
	font-weight: 700;
	color: #2c3240;
}

.question-bank-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.question-bank-page__card {
	padding: 18rpx;
	border-radius: 22rpx;
	background: #ffffff;
	box-shadow: 0 8rpx 24rpx rgba(114, 124, 148, 0.08);
}

.question-bank-page__card-main {
	display: flex;
	align-items: stretch;
	gap: 18rpx;
}

.question-bank-page__icon-box {
	flex-shrink: 0;
	width: 64rpx;
	height: 64rpx;
	border-radius: 14rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.question-bank-page__icon-box--blue {
	background: linear-gradient(180deg, #dbe4ff 0%, #cad6fb 100%);
}

.question-bank-page__icon-box--sky {
	background: linear-gradient(180deg, #d7e8ff 0%, #c8dcff 100%);
}

.question-bank-page__icon-box--purple {
	background: linear-gradient(180deg, #dfd6f4 0%, #cdc5ea 100%);
}

.icon-bookmark {
	position: relative;
	width: 22rpx;
	height: 28rpx;
	border-radius: 5rpx 5rpx 3rpx 3rpx;
	background: #697390;
}

.icon-bookmark__cut {
	position: absolute;
	left: 50%;
	bottom: 0;
	width: 10rpx;
	height: 10rpx;
	background: linear-gradient(135deg, transparent 0 50%, #dbe4ff 50% 100%);
	transform: translateX(-50%);
}

.icon-text {
	font-size: 24rpx;
	font-weight: 700;
	color: #63708c;
}

.icon-text--translate {
	letter-spacing: -0.04em;
}

.icon-grid {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.icon-grid__row {
	display: flex;
	gap: 4rpx;
}

.icon-grid__dot {
	width: 8rpx;
	height: 8rpx;
	border-radius: 2rpx;
	background: #6b718e;
}

.question-bank-page__body {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	min-width: 0;
}

.question-bank-page__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 12rpx;
}

.question-bank-page__head-copy {
	flex: 1;
	min-width: 0;
}

.question-bank-page__name {
	display: block;
	font-size: 30rpx;
	line-height: 1.34;
	font-weight: 700;
	color: #1f2533;
}

.question-bank-page__meta-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-top: 6rpx;
}

.question-bank-page__stars {
	display: inline-flex;
	align-items: center;
	gap: 2rpx;
}

.question-bank-page__hint {
	font-size: 20rpx;
	color: #7f8797;
}

.question-bank-page__count {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	color: #535c6f;
}

.question-bank-page__badge {
	flex-shrink: 0;
	padding: 6rpx 12rpx;
	border-radius: 10rpx;
	background: #5b6170;
	font-size: 18rpx;
	line-height: 1;
	font-weight: 600;
	color: #ffffff;
}

.question-bank-page__more {
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
	padding-top: 6rpx;
	width: 20rpx;
}

.question-bank-page__more-dot {
	width: 4rpx;
	height: 4rpx;
	border-radius: 50%;
	background: #7c8496;
}

.question-bank-page__progress-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-top: 22rpx;
}

.question-bank-page__progress-track {
	flex: 1;
	height: 8rpx;
	border-radius: 999rpx;
	background: #eceff6;
	overflow: hidden;
}

.question-bank-page__progress-value {
	height: 100%;
	border-radius: inherit;
	background: #5d6c89;
}

.question-bank-page__progress-text {
	flex-shrink: 0;
	font-size: 20rpx;
	font-weight: 600;
	color: #7a8293;
}

.question-bank-page__add-card {
	height: 128rpx;
	border: 2rpx dashed #dfe3ec;
	border-radius: 18rpx;
	background: rgba(255, 255, 255, 0.5);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
}

.question-bank-page__add-circle {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 34rpx;
	height: 34rpx;
	border: 3rpx solid #737b89;
	border-radius: 50%;
	font-size: 28rpx;
	line-height: 1;
	font-weight: 400;
	color: #737b89;
}

.question-bank-page__add-text {
	font-size: 22rpx;
	color: #5f6675;
}
</style>
