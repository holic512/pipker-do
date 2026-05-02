<!-- AI 索引: KYZZ 小程序底部导航。 -->
<template>
	<view class="kyzz-tabbar">
		<view class="kyzz-tabbar__placeholder"></view>

		<view class="kyzz-tabbar__inner">
			<view class="kyzz-tabbar__panel">
				<view
					v-for="item in mergedItems"
					:key="item.key"
					class="kyzz-tabbar__item"
					:class="{ 'is-active': isActive(item) }"
					@click="handleTabClick(item)"
				>
					<view class="kyzz-tabbar__icon-box">
						<uni-icons
							:type="resolveIcon(item)"
							:size="item.size || 24"
							:color="resolveColor(item)"
						/>
					</view>
					<text class="kyzz-tabbar__label" :style="{ color: resolveColor(item) }">{{ item.text }}</text>
				</view>
			</view>

			<view class="kyzz-tabbar__safe-area"></view>
		</view>
	</view>
</template>

<script>
import { createKyzzTabbarItems } from '@/pages/kyzz/navigation/tabbar'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'

export default {
	name: 'KyzzTabbar',
	props: {
		current: {
			type: String,
			default: ''
		}
	},
	computed: {
		mergedItems() {
			return createKyzzTabbarItems().map((item) => ({
				...item,
				size: item.size || 24,
				activeColor: item.activeColor || '#46536a',
				color: item.color || '#6b7884'
			}))
		}
	},
	mounted() {
		uni.hideTabBar({ animation: false }).catch(() => {})
	},
	methods: {
		isActive(item) {
			return item.key === this.current
		},
		resolveIcon(item) {
			return item.icon
		},
		resolveColor(item) {
			return this.isActive(item) ? item.activeColor : item.color
		},
		handleTabClick(item) {
			if (!item || !item.pagePath || this.isActive(item)) {
				return
			}

			uni.vibrateShort({ type: 'light' }).catch(() => {})

			if (item.key === 'practice') {
				openPracticeTab().catch((error) => {
					console.warn('[kyzz-tabbar] switch practice tab failed.', error)
				})
				return
			}

			uni.switchTab({
				url: item.pagePath,
				fail: (error) => {
					console.warn('[kyzz-tabbar] switchTab failed, check pages.json tabBar config.', error)
				}
			})
		}
	}
}
</script>

<style lang="scss" scoped>
$color-bg: #ffffff;
$color-text-normal: #6b7884;

.kyzz-tabbar {
	position: relative;
	z-index: 999;
}

.kyzz-tabbar__placeholder {
	height: 104rpx;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
	box-sizing: content-box;
}

.kyzz-tabbar__inner {
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

.kyzz-tabbar__panel {
	display: flex;
	align-items: flex-end;
	height: 104rpx;
	width: 100%;
	background: transparent;
}

.kyzz-tabbar__safe-area {
	background: $color-bg;
	height: constant(safe-area-inset-bottom);
	height: env(safe-area-inset-bottom);
}

.kyzz-tabbar__item {
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
}

.kyzz-tabbar__icon-box {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 50rpx;
	height: 50rpx;
	margin-bottom: 6rpx;
}

.kyzz-tabbar__label {
	font-size: 20rpx;
	font-weight: 500;
	line-height: 1;
	color: $color-text-normal;
	transition: color 0.2s;
}

.kyzz-tabbar__item.is-active .kyzz-tabbar__label {
	font-weight: 600;
}
</style>
