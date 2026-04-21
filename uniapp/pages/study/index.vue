<template>
	<page-shell
		current="study"
		root-class="study-page theme-page"
		content-style="padding: 0 28rpx 240rpx;"
	>
		<view class="study-page__quote theme-float-shadow">
			<view class="study-page__quote-overlay"></view>
			<text class="study-page__quote-label">每日金句</text>
			<text class="study-page__quote-text">"业精于勤，荒于嬉； 行成于思，毁于随。"</text>
			<view class="study-page__quote-dots">
				<view class="study-page__quote-dot is-active"></view>
				<view class="study-page__quote-dot"></view>
				<view class="study-page__quote-dot"></view>
			</view>
		</view>

		<view class="study-page__quick-actions">
			<view
				v-for="item in quickActions"
				:key="item.key"
				class="study-page__quick-card"
			>
				<view class="study-page__quick-icon" :class="{ 'is-active': item.active }">
					<uni-icons :type="item.icon" size="24" :color="item.active ? '#545e76' : '#687077'" />
				</view>
				<text class="study-page__quick-label">{{ item.text }}</text>
			</view>
		</view>

		<view class="study-page__research">
			<view class="study-page__section-head">
				<text class="study-page__section-title">近期研究</text>
				<text class="study-page__section-link">查看全部</text>
			</view>

			<view class="study-page__research-list">
				<view
					v-for="item in researchList"
					:key="item.key"
					class="study-page__research-item"
				>
					<view class="study-page__research-cover-wrap">
						<view class="study-page__research-track"></view>
						<view class="study-page__research-cover">
							<uni-icons :type="item.icon" size="28" color="#98a1a7" />
						</view>
					</view>

					<view class="study-page__research-content">
						<view class="study-page__research-title-row">
							<text class="study-page__research-title">{{ item.title }}</text>
							<text
								v-if="item.badge"
								class="study-page__research-badge"
								:class="item.badgeClass"
							>{{ item.badge }}</text>
						</view>
						<text class="study-page__research-desc">{{ item.desc }}</text>
						<view class="study-page__research-progress">
							<view class="study-page__research-progress-bar">
								<view class="study-page__research-progress-fill" :style="{ width: item.progress + '%' }"></view>
							</view>
							<text class="study-page__research-progress-text">{{ item.progress }}%</text>
						</view>
					</view>
				</view>
			</view>
		</view>

	</page-shell>
</template>

<script>
export default {
	name: 'StudyPage',
	data() {
		return {
			quickActions: [
				{ key: 'favorite', text: '收藏', icon: 'star', active: true },
				{ key: 'wrong', text: '错题库', icon: 'wallet', active: false },
				{ key: 'note', text: '笔记', icon: 'compose', active: false },
				{ key: 'rank', text: '排行榜', icon: 'bars', active: false }
			],
			researchList: [
				{
					key: 'mao',
					title: '毛泽东思想概论',
					badge: '高频',
					badgeClass: 'is-warm',
					desc: '第四章：社会主义建设道路初步探索',
					progress: 75,
					icon: 'wallet'
				},
				{
					key: 'english',
					title: '英语核心词汇',
					badge: '必考',
					badgeClass: 'is-cool',
					desc: 'Unit 12: Advanced Academic Vocabulary',
					progress: 33,
					icon: 'compose'
				},
				{
					key: 'math',
					title: '高等数学（上）',
					badge: '',
					badgeClass: '',
					desc: '第三章：微积分基础应用与推导',
					progress: 15,
					icon: 'flag'
				}
			]
		}
	}
}
</script>

<style lang="scss">
@import '@/uni.scss';

.study-page {
	min-height: 100vh;
	background: $pipker-surface;
	box-sizing: border-box;
}

.study-page__quote {
	position: relative;
	overflow: hidden;
	margin-top: 20rpx;
	padding: 78rpx 44rpx 52rpx;
	min-height: 270rpx;
	border-radius: 22rpx;
	background: linear-gradient(135deg, rgba(84, 94, 118, 0.72) 0%, rgba(215, 226, 255, 0.78) 100%);
}

.study-page__quote-overlay {
	position: absolute;
	right: 24rpx;
	bottom: 0;
	width: 320rpx;
	height: 190rpx;
	background:
		radial-gradient(circle at 68% 54%, rgba(255, 255, 255, 0.16) 0, rgba(255, 255, 255, 0.16) 22rpx, transparent 24rpx),
		linear-gradient(0deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05)),
		linear-gradient(90deg, transparent 0 48%, rgba(255, 255, 255, 0.1) 48% 52%, transparent 52%);
	opacity: 0.7;
}

.study-page__quote-label,
.study-page__quote-text {
	position: relative;
	z-index: 1;
	display: block;
	color: #f8fbff;
}

.study-page__quote-label {
	font-size: 24rpx;
	line-height: 1.4;
	font-weight: 600;
	opacity: 0.92;
}

.study-page__quote-text {
	margin-top: 18rpx;
	max-width: 520rpx;
	font-family: $heading-font-family;
	font-size: 34rpx;
	line-height: 1.32;
	font-weight: 700;
	letter-spacing: -0.02em;
}

.study-page__quote-dots {
	position: absolute;
	right: 34rpx;
	bottom: 34rpx;
	z-index: 1;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.study-page__quote-dot {
	width: 18rpx;
	height: 18rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.35);
}

.study-page__quote-dot.is-active {
	background: rgba(255, 255, 255, 0.92);
}

.study-page__quick-actions {
	display: grid;
	grid-template-columns: repeat(4, minmax(0, 1fr));
	gap: 22rpx;
	margin-top: 60rpx;
}

.study-page__quick-card {
	display: flex;
	align-items: center;
	flex-direction: column;
	padding: 28rpx 12rpx 34rpx;
	border-radius: 18rpx;
	background: $pipker-surface-card;
}

.study-page__quick-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 68rpx;
	height: 68rpx;
	border-radius: 20rpx;
	background: $pipker-surface-high;
}

.study-page__quick-icon.is-active {
	background: $pipker-primary-container;
}

.study-page__quick-label {
	margin-top: 18rpx;
	font-size: 22rpx;
	line-height: 1.4;
	font-weight: 500;
	color: $uni-main-color;
}

.study-page__research {
	margin-top: 66rpx;
	padding: 36rpx 26rpx 18rpx;
	border-radius: 22rpx;
	background: $pipker-surface-card;
}

.study-page__section-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.study-page__section-title {
	font-family: $heading-font-family;
	font-size: 52rpx;
	line-height: 1.14;
	font-weight: 700;
	letter-spacing: -0.03em;
	color: $uni-main-color;
}

.study-page__section-link {
	font-size: 22rpx;
	line-height: 1.5;
	font-weight: 500;
	color: $uni-primary;
	opacity: 0.92;
}

.study-page__research-list {
	margin-top: 22rpx;
}

.study-page__research-item {
	display: flex;
	align-items: flex-start;
	gap: 22rpx;
	padding: 26rpx 6rpx;
}

.study-page__research-cover-wrap {
	position: relative;
	padding-left: 12rpx;
}

.study-page__research-track {
	position: absolute;
	left: 0;
	top: 4rpx;
	bottom: 4rpx;
	width: 5rpx;
	border-radius: 999rpx;
	background: rgba(84, 94, 118, 0.92);
}

.study-page__research-cover {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 66rpx;
	height: 66rpx;
	border-radius: 6rpx;
	background: $pipker-surface-low;
}

.study-page__research-content {
	flex: 1;
	min-width: 0;
}

.study-page__research-title-row {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 12rpx;
}

.study-page__research-title {
	font-size: 28rpx;
	line-height: 1.4;
	font-weight: 700;
	color: $uni-main-color;
}

.study-page__research-badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 600;
}

.study-page__research-badge.is-warm {
	background: #f7dede;
	color: #8f514d;
}

.study-page__research-badge.is-cool {
	background: #dfe8fb;
	color: #52657f;
}

.study-page__research-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: $uni-base-color;
}

.study-page__research-progress {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-top: 18rpx;
}

.study-page__research-progress-bar {
	position: relative;
	flex: 1;
	height: 6rpx;
	border-radius: 999rpx;
	background: #dde5ea;
	overflow: hidden;
}

.study-page__research-progress-fill {
	height: 100%;
	border-radius: inherit;
	background: $uni-primary;
}

.study-page__research-progress-text {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 500;
	color: #a3acb1;
}

.study-page__research-item + .study-page__research-item {
	margin-top: 4rpx;
}

@media screen and (max-width: 360px) {
	.study-page__quote-text {
		font-size: 30rpx;
	}

	.study-page__section-title {
		font-size: 44rpx;
	}
}
</style>
