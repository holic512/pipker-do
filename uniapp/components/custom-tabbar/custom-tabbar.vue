<!--底部导航-->
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
					<text class="tabbar-label" :style="{ color: resolveColor(item) }">{{ item.text }}</text>
				</view>
			</view>

			<!-- 底部安全区 -->
			<view class="safe-area-inset"></view>
		</view>
	</view>
</template>

<script>
// AI 索引: 小程序自定义底部导航与共享菜单配置。
import { createDefaultTabbarItems } from '@/shared/navigation/tabbar'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'

export default {
	name: 'CustomTabbar',
	props: {
		current: {
			type: String,
			default: ''
		},
		items: {
			type: Array,
			default: () => createDefaultTabbarItems()
		}
	},
	computed: {
		mergedItems() {
			const sourceItems = this.items && this.items.length ? this.items : createDefaultTabbarItems();
			return sourceItems.map(item => ({
				...item,
				size: item.size || 24,
				activeColor: item.activeColor || '#46536a',
				color: item.color || '#6b7884'
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
			return item.icon;
		},
		resolveColor(item) {
			return this.isActive(item) ? item.activeColor : item.color;
		},
		handleTabClick(item) {
			if (!item || !item.pagePath) return;
			if (this.isActive(item)) return;

			uni.vibrateShort({ type: 'light' }).catch(() => {});

			if (item.key === 'practice') {
				openPracticeTab().catch((error) => {
					console.warn('[custom-tabbar] switch practice tab failed.', error);
				});
				return;
			}

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
$color-text-normal: #6b7884;

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
		border-top: 1rpx solid #e0e6e9;
		box-shadow: 0 -4rpx 20rpx rgba(43, 52, 55, 0.06);
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
			font-weight: 600;
		}
	}
}
</style>
