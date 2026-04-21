<template>
	<view class="custom-navbar" :style="placeholderStyle">
		<view class="custom-navbar__inner" :style="navbarStyle">
			<view class="custom-navbar__bar" :style="barStyle">
			<view
				class="custom-navbar__side custom-navbar__side--left"
				:style="sideStyle"
				@tap="handleMenuTap"
			>
				<uni-icons type="bars" :size="iconSize" color="#1f2937" />
			</view>
			<view class="custom-navbar__center"></view>
			<view class="custom-navbar__side" :style="sideStyle"></view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CustomNavbar',
	data() {
		return {
			statusBarHeight: 20,
			horizontalPadding: '32rpx',
			buttonSize: 32,
			menuButtonInfo: {
				top: 0,
				height: 32,
				width: 88,
				right: 10
			},
			windowWidth: 375
		};
	},
	computed: {
		placeholderStyle() {
			return {
				height: `${this.statusBarHeight + this.barHeight}px`
			};
		},
		navbarStyle() {
			return {
				width: '100%',
				paddingTop: `${this.statusBarHeight}px`,
				background: 'transparent'
			};
		},
		barHeight() {
			const verticalGap = Math.max(this.menuButtonInfo.top - this.statusBarHeight, 0);
			return this.menuButtonInfo.height + verticalGap * 2;
		},
		barStyle() {
			return {
				width: '100%',
				height: `${this.barHeight}px`,
				paddingLeft: this.horizontalPadding,
				paddingRight: this.horizontalPadding
			};
		},
		sideStyle() {
			return {
				width: `${this.buttonSize}px`,
				height: `${this.buttonSize}px`
			};
		},
		iconSize() {
			return 20;
		}
	},
	created() {
		const systemInfo = uni.getSystemInfoSync();
		this.statusBarHeight = systemInfo.statusBarHeight || 20;
		this.windowWidth = systemInfo.windowWidth || 375;
		if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
			const menuButtonInfo = uni.getMenuButtonBoundingClientRect();
			if (menuButtonInfo && menuButtonInfo.height) {
				this.menuButtonInfo = {
					top: menuButtonInfo.top || this.statusBarHeight,
					height: menuButtonInfo.height,
					width: menuButtonInfo.width,
					right: this.windowWidth - (menuButtonInfo.right || this.windowWidth - 10)
				};
			}
		}
	},
	methods: {
		handleMenuTap() {
			this.$emit('menu-click');
		}
	}
};
</script>

<style lang="scss" scoped>
.custom-navbar {
	position: relative;
	z-index: 20;
}

.custom-navbar__inner {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 1000;
}

.custom-navbar__bar {
	display: grid;
	grid-template-columns: auto 1fr auto;
	align-items: center;
	box-sizing: border-box;
}

.custom-navbar__side {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
}

.custom-navbar__side--left {
	border-radius: 999rpx;
}

.custom-navbar__center {
	display: flex;
	align-items: center;
	justify-content: center;
}
</style>
