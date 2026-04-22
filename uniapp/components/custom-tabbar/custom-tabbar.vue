<template>
	<view class="custom-tabbar">
		<!-- 占位，防止页面内容被底部 tabbar 遮挡 -->
		<view class="custom-tabbar__placeholder"></view>

		<view class="custom-tabbar__inner">
			<view class="custom-tabbar__panel">
				<view
					v-for="item in mergedItems"
					:key="item.key"
					class="tabbar-item"
					:class="{ 'is-active': isActive(item) }"
					@click="handleTabClick(item)"
				>
					<view class="icon-box">
						<uni-icons
							:type="resolveIcon(item)"
							:size="item.size || 24"
							:color="resolveColor(item)"
						/>
					</view>
					<text class="tabbar-label">{{ item.text }}</text>
				</view>
			</view>

			<!-- 底部安全区 -->
			<view class="safe-area-inset"></view>
		</view>
	</view>
</template>

<script>
const DEFAULT_ITEMS = [
	{
		key: 'study',
		text: '学习',
		pagePath: '/pages/kyzz/study/index',
		icon: 'home',
		activeIcon: 'home-filled'
	},
	{
		key: 'question-bank',
		text: '题库',
		pagePath: '/pages/kyzz/question-bank/index',
		icon: 'compose',
		activeIcon: 'compose'
	},
	{
		key: 'practice',
		text: '练习',
		pagePath: '/pages/kyzz/practice/index',
		icon: 'plusempty',
		activeIcon: 'plusempty'
	},
	{
		key: 'exam',
		text: '考试',
		pagePath: '/pages/kyzz/exam/index',
		icon: 'medal',
		activeIcon: 'medal-filled'
	},
	{
		key: 'mine',
		text: '我的',
		pagePath: '/pages/common/mine/index',
		icon: 'person',
		activeIcon: 'person-filled'
	}
];

export default {
	name: 'CustomTabbar',
	props: {
		current: {
			type: String,
			default: ''
		},
		items: {
			type: Array,
			default: () => DEFAULT_ITEMS
		}
	},
	computed: {
		mergedItems() {
			const sourceItems = this.items && this.items.length ? this.items : DEFAULT_ITEMS;
			return sourceItems.map(item => ({
				...item,
				size: item.size || 24,
				activeColor: item.activeColor || '#2f3648',
				color: item.color || '#bcc5d4'
			}));
		}
	},
	mounted() {
		uni.hideTabBar({ animation: false }).catch(() => {});
	},
	methods: {
		isActive(item) {
			return item.key === this.current;
		},
		resolveIcon(item) {
			return this.isActive(item) ? (item.activeIcon || item.icon) : item.icon;
		},
		resolveColor(item) {
			return this.isActive(item) ? item.activeColor : item.color;
		},
		handleTabClick(item) {
			if (!item || !item.pagePath || this.isActive(item)) return;

			uni.vibrateShort({ type: 'light' }).catch(() => {});

			uni.switchTab({
				url: item.pagePath,
				fail: (error) => {
					console.warn('[custom-tabbar] switchTab failed, check pages.json tabBar config.', error);
				}
			});
		}
	}
};
</script>

<style lang="scss" scoped>
$color-bg: #ffffff;
$color-text-normal: #bcc5d4;
$color-text-active: #2f3648;

.custom-tabbar {
	position: relative;
	z-index: 999;

	&__placeholder {
		height: 104rpx;
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
		box-sizing: content-box;
	}

	&__inner {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 999;
		background: $color-bg;
		border-radius: 36rpx 36rpx 0 0;
		box-shadow: 0 -2rpx 16rpx rgba(43, 52, 55, 0.04);
	}

	&__panel {
		display: flex;
		align-items: flex-end;
		height: 104rpx;
		width: 100%;
		background: transparent;
	}
}

.safe-area-inset {
	background: $color-bg;
	height: constant(safe-area-inset-bottom);
	height: env(safe-area-inset-bottom);
}

.tabbar-item {
	flex: 1;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 10rpx;
	padding-bottom: 10rpx;
	box-sizing: border-box;

	&:active {
		opacity: 0.85;
	}

	.icon-box {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 50rpx;
		height: 50rpx;
		margin-bottom: 6rpx;
	}

	.tabbar-label {
		font-size: 20rpx;
		font-weight: 500;
		line-height: 1;
		color: $color-text-normal;
		transition: color 0.2s;
	}

	&.is-active {
		.tabbar-label {
			color: $color-text-active;
			font-weight: 700;
		}
	}
}
</style>