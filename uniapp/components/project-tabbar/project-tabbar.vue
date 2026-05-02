<template>
	<!-- AI 索引: 小程序项目级底部导航共享底座。 -->
	<view class="project-tabbar">
		<view class="project-tabbar__placeholder"></view>

		<view class="project-tabbar__inner">
			<view class="project-tabbar__panel">
				<view
					v-for="item in items"
					:key="item.key"
					class="project-tabbar__item"
					:class="{ 'is-active': isActive(item) }"
					@click="handleTabClick(item)"
				>
					<view class="project-tabbar__icon-box">
						<uni-icons
							:type="resolveIcon(item)"
							:size="resolveSize(item)"
							:color="resolveColor(item)"
						/>
					</view>
					<text class="project-tabbar__label" :style="{ color: resolveColor(item) }">{{ item.text }}</text>
				</view>
			</view>

			<view class="project-tabbar__safe-area"></view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'

interface ProjectTabbarItem {
	key: string
	text: string
	pagePath: string
	icon: string
	activeIcon?: string
	size?: number
	activeColor?: string
	color?: string
}

export default defineComponent({
	name: 'ProjectTabbar',
	props: {
		current: {
			type: String,
			default: ''
		},
		items: {
			type: Array as PropType<ProjectTabbarItem[]>,
			default: () => []
		}
	},
	emits: ['select'],
	mounted() {
		uni.hideTabBar({ animation: false }).catch(() => {})
	},
	methods: {
		isActive(item: ProjectTabbarItem): boolean {
			return item.key === this.current
		},
		resolveIcon(item: ProjectTabbarItem): string {
			if (this.isActive(item) && item.activeIcon) {
				return item.activeIcon
			}
			return item.icon
		},
		resolveColor(item: ProjectTabbarItem): string {
			return this.isActive(item) ? (item.activeColor || '#46536a') : (item.color || '#6b7884')
		},
		resolveSize(item: ProjectTabbarItem): number {
			return item.size || 24
		},
		handleTabClick(item: ProjectTabbarItem): void {
			if (!item || !item.pagePath || this.isActive(item)) {
				return
			}

			uni.vibrateShort({ type: 'light' }).catch(() => {})
			this.$emit('select', item)
		}
	}
})
</script>

<style lang="scss" scoped>
$color-bg: #ffffff;
$color-text-normal: #6b7884;

.project-tabbar {
	position: relative;
	z-index: 999;
}

.project-tabbar__placeholder {
	height: 104rpx;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
	box-sizing: content-box;
}

.project-tabbar__inner {
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

.project-tabbar__panel {
	display: flex;
	align-items: flex-end;
	height: 104rpx;
	width: 100%;
	background: transparent;
}

.project-tabbar__safe-area {
	background: $color-bg;
	height: constant(safe-area-inset-bottom);
	height: env(safe-area-inset-bottom);
}

.project-tabbar__item {
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

.project-tabbar__icon-box {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 50rpx;
	height: 50rpx;
	margin-bottom: 6rpx;
}

.project-tabbar__label {
	font-size: 20rpx;
	font-weight: 500;
	line-height: 1;
	color: $color-text-normal;
	transition: color 0.2s;
}

.project-tabbar__item.is-active .project-tabbar__label {
	font-weight: 600;
}
</style>
