<template>
	<!-- AI 索引: 小程序关于我们通用说明页。 -->
	<view class="about-page theme-page">
		<scroll-view scroll-y class="about-page__scroll">
			<view class="about-page__hero">
				<text class="about-page__eyebrow">ABOUT PIPKER-DO</text>
				<text class="about-page__title">关于我们</text>
				<text class="about-page__summary">
					pipker-do 是一个面向学习场景的效率工具实践，当前重点服务考研政治刷题、复盘和学习管理。
				</text>
			</view>

			<view class="about-page__card">
				<view class="about-page__section">
					<text class="about-page__section-title">项目初衷</text>
					<text class="about-page__paragraph">
						这个项目希望把备考中重复、零散、容易被忽略的学习流程整理成更稳定的工具：能记录进度，能沉淀错题，能减少重复操作，也能让刷题和复盘变得更清晰。
					</text>
					<text class="about-page__paragraph">
						我更看重工具的真实可用性。它应该解决具体问题，减少重复劳动，并让原本繁琐的事情变得简单一些。
					</text>
				</view>

				<view class="about-page__section">
					<text class="about-page__section-title">关于作者</text>
					<text class="about-page__paragraph">
						你好，我是 holic512，一名程序员。平时关注自动化、AI 应用、开发效率和内容工具，也喜欢把日常工作中可复用的经验整理成系统。
					</text>
					<text class="about-page__paragraph">
						我会持续做一些和 AI Agent、自动化脚本、任务管理、公众号内容生产、Java/Spring 开发相关的实践，并探索如何用技术提升学习与工作的效率。
					</text>
				</view>

				<view class="about-page__section">
					<text class="about-page__section-title">开源主页</text>
					<view class="about-page__github">
						<text class="about-page__github-url">{{ githubUrl }}</text>
						<button class="about-page__github-copy" @tap="copyGithubUrl">复制</button>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

const GITHUB_URL = 'https://github.com/holic512'

export default defineComponent({
	name: 'AboutPage',
	data() {
		return {
			githubUrl: GITHUB_URL
		}
	},
	methods: {
		copyGithubUrl() {
			uni.setClipboardData({
				data: this.githubUrl,
				success: () => {
					uni.showToast({
						title: '链接已复制',
						icon: 'none'
					})
				}
			})
		}
	}
})
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.about-page {
	position: relative;
	min-height: 100vh;
	background: linear-gradient(180deg, #fafbfc 0%, #f3f5f7 100%);
}

.about-page__scroll {
	height: 100vh;
}

.about-page__hero {
	padding: 38rpx 32rpx 26rpx;
}

.about-page__eyebrow {
	display: block;
	font-size: 22rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.12em;
	color: $uni-secondary-color;
}

.about-page__title {
	display: block;
	margin-top: 12rpx;
	font-family: $heading-font-family;
	font-size: 48rpx;
	line-height: 1.18;
	font-weight: 700;
	color: $uni-main-color;
}

.about-page__summary {
	display: block;
	margin-top: 18rpx;
	font-size: 27rpx;
	line-height: 1.72;
	color: $uni-base-color;
}

.about-page__card {
	margin: 0 24rpx 36rpx;
	padding: 30rpx 28rpx;
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 38rpx rgba(176, 185, 198, 0.12);
}

.about-page__section + .about-page__section {
	margin-top: 34rpx;
	padding-top: 30rpx;
	border-top: 1rpx solid rgba(226, 232, 240, 0.86);
}

.about-page__section-title {
	display: block;
	font-size: 30rpx;
	line-height: 1.35;
	font-weight: 700;
	color: $uni-main-color;
}

.about-page__paragraph {
	display: block;
	margin-top: 18rpx;
	font-size: 26rpx;
	line-height: 1.82;
	color: $uni-base-color;
}

.about-page__github {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
	margin-top: 20rpx;
	padding: 22rpx 22rpx;
	border-radius: 18rpx;
	background: #f6f8fb;
}

.about-page__github-url {
	flex: 1;
	font-size: 25rpx;
	line-height: 1.45;
	font-weight: 700;
	color: #1f2937;
	word-break: break-all;
}

.about-page__github-copy {
	margin: 0;
	width: 108rpx;
	height: 62rpx;
	border-radius: 999rpx;
	background: #20272e;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 700;
	line-height: 62rpx;
	flex-shrink: 0;
}

.about-page__github-copy::after {
	border: 0;
}
</style>
