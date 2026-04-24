<template>
	<!-- AI 索引: 小程序冷启动品牌覆盖层。 -->
	<view
		v-if="snapshot.visible"
		class="launch-overlay"
		:class="overlayClass"
	>
		<view class="launch-overlay__ambient launch-overlay__ambient--left"></view>
		<view class="launch-overlay__ambient launch-overlay__ambient--right"></view>
		<view class="launch-overlay__grain"></view>

		<view class="launch-overlay__content">
			<view class="launch-overlay__logo-shell">
				<view class="launch-overlay__logo-glow"></view>
				<image class="launch-overlay__logo" src="/static/launch-logo.png" mode="aspectFit" />
			</view>

			<view class="launch-overlay__copy">
				<text class="launch-overlay__brand">PIPKER</text>
				<text class="launch-overlay__headline">正在准备你的学习空间</text>
				<text class="launch-overlay__step">{{ resolvedStepText }}</text>
				<text v-if="snapshot.status === 'error' && snapshot.errorMessage" class="launch-overlay__error">
					{{ snapshot.errorMessage }}
				</text>
			</view>

			<view class="launch-overlay__meter">
				<view class="launch-overlay__meter-track">
					<view class="launch-overlay__meter-fill"></view>
				</view>
			</view>

			<button
				v-if="snapshot.status === 'error'"
				class="launch-overlay__retry"
				:disabled="retrying"
				@tap="handleRetry"
			>
				{{ retrying ? '重新连接中...' : '重新尝试' }}
			</button>
		</view>

		<view v-if="snapshot.menuItems.length" class="launch-overlay__menu-preview">
			<text
				v-for="item in snapshot.menuItems"
				:key="item.key"
				class="launch-overlay__menu-item"
			>
				{{ item.text }}
			</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {
	getLaunchSnapshot,
	retryLaunchBootstrap,
	subscribeLaunchState,
	type LaunchSnapshot
} from '@/shared/launch'

type LaunchUnsubscribe = (() => boolean) | null

export default defineComponent({
	name: 'LaunchOverlay',
	data() {
		return {
			snapshot: getLaunchSnapshot() as LaunchSnapshot,
			retrying: false,
			unsubscribeLaunch: null as LaunchUnsubscribe
		}
	},
	computed: {
		overlayClass(): string {
			if (this.snapshot.status === 'ready') {
				return 'is-ready'
			}
			if (this.snapshot.status === 'error') {
				return 'is-error'
			}
			return ''
		},
		resolvedStepText(): string {
			if (this.snapshot.status === 'ready') {
				return '首页即将展开'
			}
			if (this.snapshot.status === 'error') {
				return '当前网络暂不可用'
			}
			return this.snapshot.stepText || '正在连接'
		}
	},
	mounted() {
		this.unsubscribeLaunch = subscribeLaunchState((snapshot) => {
			this.snapshot = snapshot
			if (snapshot.status !== 'error') {
				this.retrying = false
			}
		})
	},
	beforeUnmount() {
		if (this.unsubscribeLaunch) {
			this.unsubscribeLaunch()
			this.unsubscribeLaunch = null
		}
	},
	methods: {
		async handleRetry(): Promise<void> {
			if (this.retrying) {
				return
			}
			this.retrying = true
			try {
				await retryLaunchBootstrap()
			} catch (error) {
				console.warn('[launch] retry failed', error)
				this.retrying = false
			}
		}
	}
})
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.launch-overlay {
	position: fixed;
	inset: 0;
	z-index: 5000;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	padding: calc(env(safe-area-inset-top) + 88rpx) 56rpx calc(env(safe-area-inset-bottom) + 72rpx);
	background:
		linear-gradient(180deg, rgba(248, 249, 250, 0.98) 0%, rgba(241, 244, 246, 0.98) 100%);
	opacity: 1;
	transform: scale(1);
	transition: opacity 0.22s ease, transform 0.22s ease;
	overflow: hidden;
}

.launch-overlay.is-ready {
	opacity: 0;
	transform: scale(1.02);
}

.launch-overlay__ambient,
.launch-overlay__grain {
	position: absolute;
	pointer-events: none;
}

.launch-overlay__ambient {
	border-radius: 999rpx;
	filter: blur(50px);
	opacity: 0.72;
	animation: launch-ambient-float 8s ease-in-out infinite alternate;
}

.launch-overlay__ambient--left {
	top: 12%;
	left: -12%;
	width: 360rpx;
	height: 360rpx;
	background: rgba(215, 226, 255, 0.9);
}

.launch-overlay__ambient--right {
	right: -18%;
	bottom: 18%;
	width: 420rpx;
	height: 420rpx;
	background: rgba(227, 233, 236, 0.95);
	animation-duration: 10s;
}

.launch-overlay__grain {
	inset: 0;
	background-image: radial-gradient(rgba(43, 52, 55, 0.06) 0.9px, transparent 0.9px);
	background-size: 18rpx 18rpx;
	opacity: 0.2;
}

.launch-overlay__content {
	position: relative;
	z-index: 1;
	display: flex;
	flex: 1;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.launch-overlay__logo-shell {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 320rpx;
	height: 320rpx;
	margin-bottom: 72rpx;
	animation: launch-logo-enter 1.1s ease both;
}

.launch-overlay__logo-glow {
	position: absolute;
	inset: 36rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.82);
	box-shadow:
		0 32rpx 80rpx rgba(43, 52, 55, 0.08),
		inset 0 0 0 1px rgba(171, 179, 183, 0.12);
}

.launch-overlay__logo {
	position: relative;
	width: 248rpx;
	height: 248rpx;
}

.launch-overlay__copy {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
	text-align: center;
}

.launch-overlay__brand {
	font-size: 24rpx;
	line-height: 1.2;
	font-weight: 700;
	letter-spacing: 0.32em;
	text-indent: 0.32em;
	color: $uni-secondary-color;
}

.launch-overlay__headline {
	font-family: $heading-font-family;
	font-size: 52rpx;
	line-height: 1.18;
	font-weight: 700;
	letter-spacing: -0.03em;
	color: $uni-main-color;
}

.launch-overlay__step,
.launch-overlay__error {
	max-width: 460rpx;
	font-size: 26rpx;
	line-height: 1.7;
	font-weight: 500;
	color: $uni-base-color;
}

.launch-overlay__error {
	color: $uni-error;
}

.launch-overlay__meter {
	width: 100%;
	max-width: 340rpx;
	margin-top: 40rpx;
}

.launch-overlay__meter-track {
	position: relative;
	height: 8rpx;
	border-radius: 999rpx;
	overflow: hidden;
	background: rgba(171, 179, 183, 0.22);
}

.launch-overlay__meter-fill {
	position: absolute;
	top: 0;
	left: 0;
	bottom: 0;
	width: 46%;
	border-radius: inherit;
	background: linear-gradient(90deg, rgba(84, 94, 118, 0.2) 0%, rgba(84, 94, 118, 0.9) 100%);
	animation: launch-meter-slide 1.25s ease-in-out infinite;
}

.launch-overlay__retry {
	margin-top: 36rpx;
	padding: 0 44rpx;
	height: 88rpx;
	border-radius: 999rpx;
	background: $uni-main-color;
	color: $uni-white;
	font-size: 26rpx;
	line-height: 88rpx;
	font-weight: 600;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.12);
}

.launch-overlay__retry::after {
	border: 0;
}

.launch-overlay__retry[disabled] {
	opacity: 0.75;
}

.launch-overlay__menu-preview {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
	gap: 14rpx;
}

.launch-overlay__menu-item {
	padding: 12rpx 20rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.58);
	box-shadow: inset 0 0 0 1px rgba(171, 179, 183, 0.14);
	font-size: 22rpx;
	line-height: 1.2;
	font-weight: 500;
	color: $uni-secondary-color;
}

@keyframes launch-logo-enter {
	0% {
		opacity: 0;
		transform: scale(0.94);
	}

	100% {
		opacity: 1;
		transform: scale(1);
	}
}

@keyframes launch-ambient-float {
	0% {
		transform: translate3d(0, 0, 0) scale(1);
	}

	100% {
		transform: translate3d(24rpx, -30rpx, 0) scale(1.08);
	}
}

@keyframes launch-meter-slide {
	0% {
		transform: translateX(-100%);
	}

	100% {
		transform: translateX(320%);
	}
}
</style>
