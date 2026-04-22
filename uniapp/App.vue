<script>
	import { bootstrapAuth } from '@/shared/session/session'

	export default {
		onLaunch: function() {
			bootstrapAuth().catch((error) => {
				console.warn('[auth] bootstrap onLaunch failed', error)
			})
		},
		onShow: function() {
			bootstrapAuth().catch((error) => {
				console.warn('[auth] bootstrap onShow failed', error)
			})
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	@import '@/uni.scss';
	@import '@/uni_modules/uni-scss/index.scss';
	/* #ifndef APP-NVUE */
	@import '@/static/customicons.css';

	page,
	body,
	uni-page-body {
		background-color: $pipker-surface;
		color: $uni-main-color;
		font-family: $body-font-family;
		line-height: 1.6;
	}

	page {
		--pipker-color-primary: #{$uni-primary};
		--pipker-color-primary-container: #{$pipker-primary-container};
		--pipker-color-surface: #{$pipker-surface};
		--pipker-color-surface-low: #{$pipker-surface-low};
		--pipker-color-surface-container: #{$pipker-surface-container};
		--pipker-color-surface-card: #{$pipker-surface-card};
		--pipker-color-on-surface: #{$uni-main-color};
		--pipker-color-body: #{$uni-base-color};
		--pipker-color-muted: #{$uni-secondary-color};
		--pipker-color-ghost: #{$pipker-outline-ghost};
		--pipker-shadow-ambient: #{$pipker-shadow-ambient};
		--pipker-shadow-float: #{$pipker-shadow-float};
		--pipker-gradient-primary: #{$pipker-gradient-primary};
	}

	view,
	text,
	button,
	input,
	textarea,
	scroll-view,
	swiper,
	swiper-item,
	image {
		box-sizing: border-box;
	}

	button,
	input,
	textarea {
		font-family: $body-font-family;
	}

	button::after {
		border: 0;
	}

	.theme-page {
		min-height: 100vh;
		background: $pipker-surface;
		color: $uni-main-color;
	}

	.theme-page--sectioned {
		background: $pipker-surface-low;
	}

	.theme-shell {
		padding-left: 32rpx;
		padding-right: 32rpx;
	}

	.theme-surface {
		background: $pipker-surface;
	}

	.theme-surface-low {
		background: $pipker-surface-low;
	}

	.theme-surface-container {
		background: $pipker-surface-container;
	}

	.theme-card,
	.theme-block {
		padding: 32rpx;
		border-radius: 24rpx;
		background: $pipker-surface-card;
		border: 0;
		box-shadow: none;
	}

	.theme-card--soft {
		background: $pipker-surface-low;
	}

	.theme-card--tonal {
		background: $pipker-surface-container;
	}

	.theme-floating,
	.theme-glass {
		background: $pipker-glass-surface;
		-webkit-backdrop-filter: blur(20px);
		backdrop-filter: blur(20px);
		box-shadow: $pipker-shadow-ambient;
	}

	.theme-ambient-shadow {
		box-shadow: $pipker-shadow-ambient;
	}

	.theme-float-shadow {
		box-shadow: $pipker-shadow-float;
	}

	.theme-ghost-outline {
		box-shadow: inset 0 0 0 1px $pipker-outline-ghost;
	}

	.theme-title {
		font-family: $heading-font-family;
		font-size: 52rpx;
		line-height: 1.18;
		font-weight: 700;
		letter-spacing: -0.02em;
		color: $uni-main-color;
	}

	.theme-heading {
		font-family: $heading-font-family;
		font-size: 40rpx;
		line-height: 1.24;
		font-weight: 700;
		letter-spacing: -0.02em;
		color: $uni-main-color;
	}

	.theme-subheading {
		font-size: 30rpx;
		line-height: 1.45;
		font-weight: 600;
		color: $uni-main-color;
	}

	.theme-body {
		font-size: 28rpx;
		line-height: 1.72;
		font-weight: 400;
		color: $uni-base-color;
	}

	.theme-caption {
		font-size: 24rpx;
		line-height: 1.6;
		font-weight: 500;
		color: $uni-secondary-color;
	}

	.theme-overline {
		font-size: 22rpx;
		line-height: 1.4;
		font-weight: 600;
		letter-spacing: 0.08em;
		text-transform: uppercase;
		color: $uni-secondary-color;
	}

	.theme-link {
		display: inline;
		color: $uni-primary;
		text-decoration: none;
		box-shadow: inset 0 -8rpx 0 rgba(215, 226, 255, 0.9);
	}

	.theme-link--plain {
		box-shadow: none;
	}

	.theme-stack-sm .theme-stack-item + .theme-stack-item {
		margin-top: 16rpx;
	}

	.theme-stack-md .theme-stack-item + .theme-stack-item {
		margin-top: 24rpx;
	}

	.theme-stack-lg .theme-stack-item + .theme-stack-item {
		margin-top: 32rpx;
	}

	.theme-no-line-list .theme-stack-item + .theme-stack-item {
		margin-top: 24rpx;
	}

	.theme-meta {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 12rpx;
		color: $uni-secondary-color;
		font-size: 24rpx;
		line-height: 1.5;
	}

	.theme-meta-dot {
		width: 8rpx;
		height: 8rpx;
		border-radius: 50%;
		background: $pipker-surface-variant;
	}

	.theme-progress {
		position: relative;
		width: 100%;
		height: 8rpx;
		border-radius: 999rpx;
		background: $pipker-primary-container;
		overflow: hidden;
	}

	.theme-progress__fill {
		height: 100%;
		border-radius: inherit;
		background: $uni-primary;
	}

	.theme-progress--vertical {
		width: 8rpx;
		height: 140rpx;
		background: $pipker-primary-container;
	}

	.theme-progress--vertical .theme-progress__fill {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
	}

	.theme-input {
		padding: 24rpx 28rpx;
		border: 0;
		border-radius: 20rpx;
		background: $pipker-surface-low;
		box-shadow: inset 0 -4rpx 0 rgba(84, 94, 118, 0.14);
		color: $uni-main-color;
	}

	.theme-input--ghost {
		background: transparent;
		box-shadow: inset 0 -4rpx 0 $uni-primary;
		border-radius: 0;
		padding-left: 0;
		padding-right: 0;
	}

	.theme-gradient-primary {
		background-image: $pipker-gradient-primary;
	}

	.theme-muted {
		color: $uni-secondary-color;
	}

	.theme-body-color {
		color: $uni-base-color;
	}

	/* #endif */
	.example-info {
		font-size: 14px;
		color: #333;
		padding: 10px;
	}
</style>
