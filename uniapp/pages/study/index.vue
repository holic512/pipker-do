<template>
	<page-shell
		current="study"
		root-class="study-page theme-page"
		content-style="padding: 0 32rpx 32rpx;"
	>
		<view class="study-page__inner">
			<!-- 顶部格言区域，按设计稿保留居中展示 -->
			<view class="study-page__motto">
				<text class="study-page__motto-text">“博观而约取，厚积而薄发”</text>
				<view class="study-page__motto-line"></view>
			</view>

			<!-- 装饰线框，用来还原截图中的轻几何背景 -->
			<view class="study-page__frame study-page__frame--left"></view>
			<view class="study-page__frame study-page__frame--right"></view>
			<view class="study-page__diamond"></view>

			<!-- 主学习卡片 -->
			<view class="study-page__hero-shell">
				<view class="study-page__hero">
					<text class="study-page__hero-en">{{ hero.englishTitle }}</text>
					<text class="study-page__hero-title">{{ hero.title }}</text>
					<view class="study-page__hero-divider"></view>
					<text class="study-page__hero-desc">{{ hero.questionBank }}</text>
					<text class="study-page__hero-desc">完成进度：{{ hero.progress }}%</text>
				</view>

				<!-- 7day 改为坚持天数标签 -->
				<view class="study-page__streak">
					<uni-icons type="fire-filled" size="14" color="#ffffff" />
					<text class="study-page__streak-text">{{ streakDays }} DAYS</text>
				</view>
			</view>

			<!-- 快捷功能入口 -->
			<view class="study-page__actions">
				<view
					v-for="item in quickActions"
					:key="item.key"
					class="study-page__action-item"
				>
					<view class="study-page__action-icon">
						<uni-icons :type="item.icon" size="30" color="#56627f" />
					</view>
					<text class="study-page__action-label">{{ item.text }}</text>
				</view>
			</view>

			<study-garden />
		</view>
	</page-shell>
</template>

<script>
export default {
	name: 'StudyPage',
	data() {
		return {
			// 坚持天数，对应截图里的 7 DAYS
			streakDays: 7,
			// 主卡片文案配置
			hero: {
				englishTitle: 'ACADEMIC QUEST',
				title: '开始刷题',
				questionBank: '当前题库：考研政治核心点',
				progress: 65
			},
			// 页面中部四个快捷入口
			quickActions: [
				{ key: 'favorite', text: '收藏', icon: 'star-filled' },
				{ key: 'wrong', text: '错题库', icon: 'paperplane-filled' },
				{ key: 'note', text: '笔记', icon: 'compose' },
				{ key: 'rank', text: '排行榜', icon: 'bars' }
			]
		}
	}
}
</script>

<style lang="scss">
@import '@/uni.scss';

.study-page {
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.98) 0%, rgba(244, 247, 252, 0.94) 48%, rgba(236, 241, 247, 0.92) 100%);
	box-sizing: border-box;
}

.study-page__inner {
	position: relative;
	padding-top: 96rpx;
	overflow: hidden;
}

/* 顶部格言 */
.study-page__motto {
	position: relative;
	z-index: 2;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.study-page__motto-text {
	font-size: 30rpx;
	line-height: 1.5;
	font-weight: 600;
	font-style: italic;
	letter-spacing: 0.08em;
	color: rgba(120, 127, 142, 0.88);
}

.study-page__motto-line {
	margin-top: 24rpx;
	width: 182rpx;
	height: 2rpx;
	background: rgba(178, 186, 201, 0.9);
}

/* 背景几何线框 */
.study-page__frame,
.study-page__diamond {
	position: absolute;
	pointer-events: none;
}

.study-page__frame {
	top: 132rpx;
	width: 260rpx;
	height: 260rpx;
	border-top: 2rpx solid rgba(197, 206, 220, 0.48);
}

.study-page__frame--left {
	left: -92rpx;
	transform: skewY(-35deg);
}

.study-page__frame--right {
	right: -92rpx;
	transform: skewY(35deg);
}

.study-page__diamond {
	left: 50%;
	top: 248rpx;
	width: 520rpx;
	height: 520rpx;
	border: 2rpx solid rgba(202, 211, 224, 0.32);
	transform: translateX(-50%) rotate(45deg);
}

/* 主卡片外层白色描边壳 */
.study-page__hero-shell {
	position: relative;
	z-index: 2;
	margin: 108rpx auto 0;
	width: 560rpx;
	padding: 18rpx;
	border-radius: 24rpx;
	background: rgba(248, 250, 253, 0.95);
	box-shadow:
		0 14rpx 40rpx rgba(147, 160, 182, 0.18),
		inset 0 0 0 2rpx rgba(196, 205, 220, 0.85);
}

.study-page__hero {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 40rpx 92rpx;
	border-radius: 20rpx;
	background: linear-gradient(180deg, #1f2633 0%, #283142 100%);
	box-shadow:
		inset 0 0 0 2rpx rgba(255, 255, 255, 0.06),
		0 10rpx 22rpx rgba(49, 58, 76, 0.18);
}

.study-page__hero-en {
	font-size: 24rpx;
	line-height: 1.4;
	letter-spacing: 0.22em;
	color: rgba(206, 212, 223, 0.6);
}

.study-page__hero-title {
	margin-top: 34rpx;
	font-size: 66rpx;
	line-height: 1.18;
	font-weight: 700;
	letter-spacing: 0.04em;
	color: #ffffff;
}

.study-page__hero-divider {
	margin: 40rpx 0 34rpx;
	width: 90rpx;
	height: 4rpx;
	border-radius: 999rpx;
	background: rgba(180, 191, 209, 0.62);
}

.study-page__hero-desc {
	display: block;
	margin-top: 12rpx;
	font-size: 27rpx;
	line-height: 1.7;
	color: rgba(226, 231, 239, 0.8);
}

.study-page__streak {
	position: absolute;
	right: 0;
	bottom: 34rpx;
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	padding: 10rpx 24rpx;
	border: 4rpx solid #ffffff;
	border-radius: 999rpx;
	background: linear-gradient(180deg, #6a738c 0%, #7a8399 100%);
	box-shadow: 0 10rpx 20rpx rgba(110, 121, 143, 0.24);
	transform: translateX(18rpx);
}

.study-page__streak-text {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	letter-spacing: 0.04em;
	color: #ffffff;
}

/* 四个快捷入口 */
.study-page__actions {
	position: relative;
	z-index: 2;
	display: grid;
	grid-template-columns: repeat(4, minmax(0, 1fr));
	gap: 28rpx;
	margin-top: 84rpx;
}

.study-page__action-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.study-page__action-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 104rpx;
	height: 104rpx;
	border-radius: 22rpx;
	background: rgba(255, 255, 255, 0.88);
	box-shadow: 0 14rpx 30rpx rgba(171, 180, 196, 0.2);
}

.study-page__action-label {
	margin-top: 22rpx;
	font-size: 27rpx;
	line-height: 1.4;
	color: #6d7383;
}

@media screen and (max-width: 375px) {
	.study-page__hero-shell {
		width: 100%;
	}

	.study-page__hero {
		padding-left: 28rpx;
		padding-right: 28rpx;
	}

	.study-page__hero-title {
		font-size: 58rpx;
	}

	.study-page__hero-desc {
		font-size: 24rpx;
	}

	.study-page__actions {
		gap: 18rpx;
	}

	.study-page__action-icon {
		width: 94rpx;
		height: 94rpx;
	}
}
</style>
