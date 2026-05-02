<template>
	<!-- AI 索引: KYYY 项目入口页 -->
	<page-shell
		root-class="kyyy-home theme-page"
		content-style="padding: 0 24rpx 40rpx;"
	>
		<view class="kyyy-home__ambient">
			<view class="kyyy-home__glow kyyy-home__glow--primary"></view>
			<view class="kyyy-home__glow kyyy-home__glow--secondary"></view>
		</view>

		<view class="kyyy-home__inner">
			<view class="kyyy-home__hero">
				<text class="kyyy-home__eyebrow">GRADUATE ENGLISH</text>
				<text class="kyyy-home__title">考研英语</text>
				<text class="kyyy-home__desc">
					英语项目入口已接入顶部抽屉。后续阅读、词汇、真题与模考能力都可以从这里继续扩展。
				</text>

				<view class="kyyy-home__hero-meta">
					<view class="kyyy-home__hero-chip">
						<uni-icons type="calendar" size="14" color="#eef4ff" />
						<text>入口已就绪</text>
					</view>
					<view class="kyyy-home__hero-chip">
						<uni-icons type="compose" size="14" color="#eef4ff" />
						<text>支持持续扩展</text>
					</view>
				</view>
			</view>

			<view class="kyyy-home__section">
				<view class="kyyy-home__section-head">
					<text class="kyyy-home__section-overline">MODULES</text>
					<text class="kyyy-home__section-title">项目方向</text>
				</view>

				<view class="kyyy-home__module-list">
					<view
						v-for="item in modules"
						:key="item.key"
						class="kyyy-home__module-card"
					>
						<view class="kyyy-home__module-icon" :style="{ background: item.iconBg }">
							<uni-icons :type="item.icon" size="20" :color="item.iconColor" />
						</view>
						<view class="kyyy-home__module-copy">
							<text class="kyyy-home__module-title">{{ item.title }}</text>
							<text class="kyyy-home__module-desc">{{ item.description }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="kyyy-home__notice">
				<text class="kyyy-home__notice-title">当前说明</text>
				<text class="kyyy-home__notice-text">
					现在可以通过左上角按钮在考研政治和考研英语之间切换。英语页先作为项目入口，后续业务页面可继续落到
					pages/kyyy 与 components/kyyy。
				</text>
			</view>
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

interface KyyyModuleCard {
	key: string
	title: string
	description: string
	icon: string
	iconBg: string
	iconColor: string
}

interface KyyyHomeState {
	modules: KyyyModuleCard[]
}

export default defineComponent({
	name: 'KyyyHomePage',
	data(): KyyyHomeState {
		return {
			modules: [
				{
					key: 'reading',
					title: '阅读理解',
					description: '为真题精读、题型拆解和长文复盘预留入口。',
					icon: 'compose',
					iconBg: 'linear-gradient(135deg, rgba(215, 226, 255, 0.95), rgba(188, 205, 232, 0.95))',
					iconColor: '#46536a'
				},
				{
					key: 'vocabulary',
					title: '词汇积累',
					description: '用于单词计划、熟词僻义与高频短语整理。',
					icon: 'star-filled',
					iconBg: 'linear-gradient(135deg, rgba(248, 236, 219, 0.98), rgba(232, 214, 193, 0.98))',
					iconColor: '#8a5f2d'
				},
				{
					key: 'mock-exam',
					title: '真题模考',
					description: '后续会承接套卷训练、阶段计时与成绩跟踪。',
					icon: 'calendar',
					iconBg: 'linear-gradient(135deg, rgba(224, 239, 231, 0.98), rgba(201, 224, 209, 0.98))',
					iconColor: '#466352'
				}
			]
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-home {
	position: relative;
	overflow: hidden;
}

.kyyy-home__ambient {
	position: absolute;
	inset: 0;
	pointer-events: none;
	overflow: hidden;
}

.kyyy-home__glow {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	opacity: 0.9;
}

.kyyy-home__glow--primary {
	top: 72rpx;
	right: -100rpx;
	width: 340rpx;
	height: 340rpx;
	background: radial-gradient(circle, rgba(215, 226, 255, 0.95) 0%, rgba(215, 226, 255, 0) 72%);
}

.kyyy-home__glow--secondary {
	top: 420rpx;
	left: -120rpx;
	width: 280rpx;
	height: 280rpx;
	background: radial-gradient(circle, rgba(228, 236, 242, 0.88) 0%, rgba(228, 236, 242, 0) 72%);
}

.kyyy-home__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 28rpx;
	padding-top: 8rpx;
}

.kyyy-home__hero {
	position: relative;
	overflow: hidden;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 40rpx 36rpx;
	border-radius: 36rpx;
	background: linear-gradient(135deg, #50607a 0%, #6f85a2 100%);
	box-shadow: 0 24rpx 48rpx rgba(43, 52, 55, 0.14);
}

.kyyy-home__hero::after {
	content: '';
	position: absolute;
	top: -64rpx;
	right: -48rpx;
	width: 220rpx;
	height: 220rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(255, 255, 255, 0.28) 0%, rgba(255, 255, 255, 0) 72%);
}

.kyyy-home__eyebrow {
	position: relative;
	z-index: 1;
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.22em;
	color: rgba(246, 249, 255, 0.88);
}

.kyyy-home__title {
	position: relative;
	z-index: 1;
	font-size: 56rpx;
	line-height: 1.18;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-home__desc {
	position: relative;
	z-index: 1;
	font-size: 26rpx;
	line-height: 1.68;
	color: rgba(241, 245, 255, 0.88);
}

.kyyy-home__hero-meta {
	position: relative;
	z-index: 1;
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
}

.kyyy-home__hero-chip {
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

.kyyy-home__section {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-home__section-head {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	padding: 0 8rpx;
}

.kyyy-home__section-overline {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.18em;
	color: #99a3b1;
}

.kyyy-home__section-title {
	font-size: 34rpx;
	line-height: 1.28;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-home__module-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-home__module-card {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 26rpx 24rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.94);
	border: 1rpx solid rgba(215, 226, 255, 0.9);
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.07);
}

.kyyy-home__module-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 88rpx;
	height: 88rpx;
	border-radius: 24rpx;
	flex-shrink: 0;
}

.kyyy-home__module-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
}

.kyyy-home__module-title {
	font-size: 30rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-home__module-desc {
	font-size: 24rpx;
	line-height: 1.65;
	color: #69737d;
}

.kyyy-home__notice {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	padding: 30rpx 28rpx;
	border-radius: 30rpx;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(240, 244, 247, 0.96));
	border: 1rpx solid rgba(222, 229, 236, 0.9);
}

.kyyy-home__notice-title {
	font-size: 28rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #35404b;
}

.kyyy-home__notice-text {
	font-size: 24rpx;
	line-height: 1.72;
	color: #69727b;
}
</style>
