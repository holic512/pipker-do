<template>
	<!-- AI 索引: KYYY 业务占位页共享骨架。 -->
	<page-shell
		root-class="kyyy-feature-shell theme-page"
		content-style="padding: 0 24rpx 40rpx;"
	>
		<view class="kyyy-feature-shell__ambient">
			<view class="kyyy-feature-shell__glow kyyy-feature-shell__glow--primary"></view>
			<view class="kyyy-feature-shell__glow kyyy-feature-shell__glow--secondary"></view>
		</view>

		<view class="kyyy-feature-shell__inner">
			<view class="kyyy-feature-shell__hero" :style="{ background: heroBackground }">
				<view class="kyyy-feature-shell__hero-orb" :style="{ background: heroOrbBackground }"></view>
				<view class="kyyy-feature-shell__hero-icon-shell" :style="{ background: heroIconShellBackground }">
					<view class="kyyy-feature-shell__hero-icon-core" :style="{ background: heroIconCoreBackground }">
						<uni-icons :type="heroIcon" size="24" :color="heroIconColor" />
					</view>
				</view>
				<text class="kyyy-feature-shell__eyebrow">{{ eyebrow }}</text>
				<text class="kyyy-feature-shell__title">{{ title }}</text>
				<text class="kyyy-feature-shell__desc">{{ description }}</text>

				<view class="kyyy-feature-shell__hero-meta">
					<view
						v-for="chip in heroChips"
						:key="chip.key"
						class="kyyy-feature-shell__hero-chip"
					>
						<uni-icons :type="chip.icon" size="14" color="#eef4ff" />
						<text>{{ chip.text }}</text>
					</view>
				</view>
			</view>

			<view class="kyyy-feature-shell__section">
				<view class="kyyy-feature-shell__section-head">
					<text class="kyyy-feature-shell__section-overline">{{ sectionOverline }}</text>
					<text class="kyyy-feature-shell__section-title">{{ sectionTitle }}</text>
				</view>

				<view class="kyyy-feature-shell__module-list">
					<view
						v-for="item in cards"
						:key="item.key"
						class="kyyy-feature-shell__module-card"
					>
						<view class="kyyy-feature-shell__module-icon" :style="{ background: item.iconBg }">
							<uni-icons :type="item.icon" size="20" :color="item.iconColor" />
						</view>
						<view class="kyyy-feature-shell__module-copy">
							<text class="kyyy-feature-shell__module-title">{{ item.title }}</text>
							<text class="kyyy-feature-shell__module-desc">{{ item.description }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="kyyy-feature-shell__notice">
				<text class="kyyy-feature-shell__notice-title">{{ noticeTitle }}</text>
				<text class="kyyy-feature-shell__notice-text">{{ noticeText }}</text>
			</view>
		</view>

		<template #tabbar>
			<kyyy-tabbar :current="current" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'

interface KyyyFeatureChip {
	key: string
	text: string
	icon: string
}

interface KyyyFeatureCard {
	key: string
	title: string
	description: string
	icon: string
	iconBg: string
	iconColor: string
}

export default defineComponent({
	name: 'KyyyFeatureShellPage',
	components: {
		KyyyTabbar
	},
	props: {
		current: {
			type: String,
			required: true
		},
		eyebrow: {
			type: String,
			default: 'GRADUATE ENGLISH'
		},
		title: {
			type: String,
			required: true
		},
		description: {
			type: String,
			required: true
		},
		heroIcon: {
			type: String,
			default: 'home-filled'
		},
		heroBackground: {
			type: String,
			default: 'linear-gradient(135deg, #50607a 0%, #6f85a2 100%)'
		},
		heroOrbBackground: {
			type: String,
			default: 'radial-gradient(circle, rgba(255, 255, 255, 0.24) 0%, rgba(255, 255, 255, 0) 72%)'
		},
		heroIconShellBackground: {
			type: String,
			default: 'rgba(255, 255, 255, 0.14)'
		},
		heroIconCoreBackground: {
			type: String,
			default: 'rgba(255, 255, 255, 0.9)'
		},
		heroIconColor: {
			type: String,
			default: '#4b5a72'
		},
		heroChips: {
			type: Array as PropType<KyyyFeatureChip[]>,
			default: () => []
		},
		sectionOverline: {
			type: String,
			default: 'MODULES'
		},
		sectionTitle: {
			type: String,
			default: '项目方向'
		},
		cards: {
			type: Array as PropType<KyyyFeatureCard[]>,
			default: () => []
		},
		noticeTitle: {
			type: String,
			default: '当前说明'
		},
		noticeText: {
			type: String,
			default: ''
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-feature-shell {
	position: relative;
	overflow: hidden;
}

.kyyy-feature-shell__ambient {
	position: absolute;
	inset: 0;
	pointer-events: none;
	overflow: hidden;
}

.kyyy-feature-shell__glow {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	opacity: 0.9;
}

.kyyy-feature-shell__glow--primary {
	top: 72rpx;
	right: -100rpx;
	width: 340rpx;
	height: 340rpx;
	background: radial-gradient(circle, rgba(215, 226, 255, 0.95) 0%, rgba(215, 226, 255, 0) 72%);
}

.kyyy-feature-shell__glow--secondary {
	top: 420rpx;
	left: -120rpx;
	width: 280rpx;
	height: 280rpx;
	background: radial-gradient(circle, rgba(228, 236, 242, 0.88) 0%, rgba(228, 236, 242, 0) 72%);
}

.kyyy-feature-shell__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 28rpx;
	padding-top: 8rpx;
}

.kyyy-feature-shell__hero {
	position: relative;
	overflow: hidden;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 40rpx 36rpx;
	border-radius: 36rpx;
	box-shadow: 0 24rpx 48rpx rgba(43, 52, 55, 0.14);
}

.kyyy-feature-shell__hero-orb {
	position: absolute;
	top: -64rpx;
	right: -48rpx;
	width: 220rpx;
	height: 220rpx;
	border-radius: 50%;
}

.kyyy-feature-shell__hero-icon-shell {
	position: relative;
	z-index: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 96rpx;
	height: 96rpx;
	border-radius: 28rpx;
	backdrop-filter: blur(10rpx);
}

.kyyy-feature-shell__hero-icon-core {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 62rpx;
	height: 62rpx;
	border-radius: 20rpx;
	box-shadow: inset 0 0 0 1rpx rgba(226, 234, 245, 0.88);
}

.kyyy-feature-shell__eyebrow {
	position: relative;
	z-index: 1;
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.22em;
	color: rgba(246, 249, 255, 0.88);
}

.kyyy-feature-shell__title {
	position: relative;
	z-index: 1;
	font-size: 54rpx;
	line-height: 1.18;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-feature-shell__desc {
	position: relative;
	z-index: 1;
	font-size: 26rpx;
	line-height: 1.68;
	color: rgba(241, 245, 255, 0.88);
}

.kyyy-feature-shell__hero-meta {
	position: relative;
	z-index: 1;
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
}

.kyyy-feature-shell__hero-chip {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.14);
	font-size: 22rpx;
	line-height: 1.3;
	font-weight: 600;
	color: #f4f7ff;
}

.kyyy-feature-shell__section {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-feature-shell__section-head {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	padding: 0 8rpx;
}

.kyyy-feature-shell__section-overline {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.18em;
	color: #99a3b1;
}

.kyyy-feature-shell__section-title {
	font-size: 34rpx;
	line-height: 1.28;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-feature-shell__module-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-feature-shell__module-card {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 24rpx 24rpx 24rpx 22rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.92);
	box-shadow: 0 18rpx 38rpx rgba(43, 52, 55, 0.07);
}

.kyyy-feature-shell__module-icon {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 80rpx;
	height: 80rpx;
	border-radius: 24rpx;
	flex-shrink: 0;
}

.kyyy-feature-shell__module-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.kyyy-feature-shell__module-title {
	font-size: 30rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-feature-shell__module-desc {
	font-size: 24rpx;
	line-height: 1.65;
	color: #6d7782;
}

.kyyy-feature-shell__notice {
	padding: 30rpx 28rpx;
	border-radius: 28rpx;
	background: rgba(243, 247, 251, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(224, 231, 238, 0.9);
}

.kyyy-feature-shell__notice-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.35;
	font-weight: 700;
	color: #32404d;
}

.kyyy-feature-shell__notice-text {
	display: block;
	margin-top: 12rpx;
	font-size: 24rpx;
	line-height: 1.72;
	color: #6c7783;
}
</style>
