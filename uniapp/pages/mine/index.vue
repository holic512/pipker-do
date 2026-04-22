<template>
	<page-shell
		current="mine"
		root-class="mine-page"
		content-class="mine-page__content"
	>
		<view class="mine-page__profile">
			<view class="mine-page__avatar">
				<view class="mine-page__avatar-glow"></view>
				<view class="mine-page__avatar-head"></view>
				<view class="mine-page__avatar-body"></view>
			</view>

			<view class="mine-page__profile-main">
				<text class="mine-page__name">{{ user.name }}</text>
				<view class="mine-page__profile-link" @tap="handleAction('profile')">
					<text class="mine-page__profile-link-text">更新资料</text>
					<uni-icons type="right" size="15" color="#7f8697" />
				</view>
			</view>
		</view>

		<view class="mine-page__vip-card">
			<view class="mine-page__vip-header">
				<view>
					<text class="mine-page__vip-title">VIP会员</text>
					<text class="mine-page__vip-expire">到期时间: {{ user.vipExpireAt }}</text>
				</view>
				<view class="mine-page__vip-badge">
					<uni-icons type="star-filled" size="15" color="#dfe7ff" />
				</view>
			</view>

			<view class="mine-page__vip-features">
				<view
					v-for="feature in vipFeatures"
					:key="feature.key"
					class="mine-page__vip-feature"
				>
					<view class="mine-page__vip-feature-icon">
						<uni-icons :type="feature.icon" size="18" color="#f5f7ff" />
					</view>
					<text class="mine-page__vip-feature-text">{{ feature.text }}</text>
				</view>
			</view>
		</view>

		<view class="mine-page__menu">
			<view
				v-for="item in menuItems"
				:key="item.key"
				class="mine-page__menu-item"
				@tap="handleAction(item.key)"
			>
				<view class="mine-page__menu-icon">
					<uni-icons :type="item.icon" size="18" color="#64748b" />
				</view>
				<text class="mine-page__menu-text">{{ item.text }}</text>
				<uni-icons type="right" size="16" color="#c3cad7" />
			</view>
		</view>
	</page-shell>
</template>

<script>
export default {
	name: 'MinePage',
	data() {
		return {
			user: {
				name: '学者 Pipker',
				vipExpireAt: '2025-12-31'
			},
			vipFeatures: [
				{ key: 'unlimited', text: '无限提问', icon: 'loop' },
				{ key: 'priority', text: '优先处理', icon: 'headphones' },
				{ key: 'report', text: '深度报告', icon: 'paperclip' }
			],
			menuItems: [
				{ key: 'redeem', text: '激活兑换码', icon: 'scan' },
				{ key: 'setting', text: '刷题设置', icon: 'gear' },
				{ key: 'service', text: '联系客服', icon: 'headphones' },
				{ key: 'feedback', text: '意见反馈', icon: 'chatboxes' },
				{ key: 'about', text: '关于我们', icon: 'info' }
			]
		};
	},
	methods: {
		handleAction(key) {
			const actionTextMap = {
				profile: '更新资料',
				redeem: '激活兑换码',
				setting: '刷题设置',
				service: '联系客服',
				feedback: '意见反馈',
				about: '关于我们'
			};

			uni.showToast({
				title: `${actionTextMap[key] || '功能'}开发中`,
				icon: 'none'
			});
		}
	}
};
</script>

<style lang="scss">
.mine-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #fafbfc 0%, #f3f5f7 100%);
}

.mine-page__content {
	padding: 18rpx 18rpx calc(env(safe-area-inset-bottom) + 176rpx);
}

.mine-page__profile {
	display: flex;
	align-items: center;
	padding: 8rpx 0 0;
}

.mine-page__avatar {
	position: relative;
	width: 96rpx;
	height: 96rpx;
	border-radius: 20rpx;
	background: linear-gradient(145deg, #162028 0%, #212b34 100%);
	box-shadow: 0 18rpx 32rpx rgba(33, 43, 52, 0.12);
	overflow: hidden;
	flex-shrink: 0;
}

.mine-page__avatar-glow {
	position: absolute;
	left: 18rpx;
	top: 22rpx;
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: rgba(73, 94, 117, 0.24);
}

.mine-page__avatar-head {
	position: absolute;
	left: 34rpx;
	top: 16rpx;
	width: 30rpx;
	height: 30rpx;
	border-radius: 50%;
	background: #f0b17b;
	box-shadow: -8rpx 4rpx 0 0 #2b2521 inset;
}

.mine-page__avatar-body {
	position: absolute;
	left: 28rpx;
	top: 40rpx;
	width: 40rpx;
	height: 36rpx;
	border-radius: 20rpx 20rpx 18rpx 18rpx;
	background: #ffd0ab;
	transform: rotate(-36deg);
}

.mine-page__profile-main {
	margin-left: 26rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.mine-page__name {
	font-size: 32rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2f343a;
}

.mine-page__profile-link {
	display: inline-flex;
	align-items: center;
	margin-top: 12rpx;
}

.mine-page__profile-link-text {
	font-size: 22rpx;
	line-height: 1;
	color: #7b8494;
	margin-right: 8rpx;
}

.mine-page__vip-card {
	margin-top: 34rpx;
	padding: 34rpx 30rpx 30rpx;
	border-radius: 16rpx;
	background: linear-gradient(115deg, #353c42 0%, #232b30 100%);
	box-shadow: 0 20rpx 44rpx rgba(37, 43, 48, 0.16);
}

.mine-page__vip-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
}

.mine-page__vip-title {
	display: block;
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #ffffff;
}

.mine-page__vip-expire {
	display: block;
	margin-top: 14rpx;
	font-size: 21rpx;
	line-height: 1.3;
	color: rgba(235, 239, 245, 0.5);
}

.mine-page__vip-badge {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 34rpx;
	height: 40rpx;
	border-radius: 12rpx 12rpx 8rpx 8rpx;
	background: linear-gradient(180deg, rgba(111, 123, 143, 0.6) 0%, rgba(82, 92, 108, 0.3) 100%);
}

.mine-page__vip-features {
	display: flex;
	align-items: flex-start;
	margin-top: 38rpx;
}

.mine-page__vip-feature {
	width: 132rpx;
	display: flex;
	flex-direction: column;
	align-items: flex-start;
}

.mine-page__vip-feature-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 46rpx;
	height: 46rpx;
	border-radius: 14rpx;
	background: rgba(255, 255, 255, 0.1);
	box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.04);
}

.mine-page__vip-feature-text {
	margin-top: 14rpx;
	font-size: 20rpx;
	line-height: 1.2;
	color: rgba(245, 247, 255, 0.92);
}

.mine-page__menu {
	margin-top: 30rpx;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.mine-page__menu-item {
	display: flex;
	align-items: center;
	min-height: 68rpx;
	padding: 18rpx 22rpx 18rpx 18rpx;
	border-radius: 14rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 32rpx rgba(176, 185, 198, 0.12);
}

.mine-page__menu-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 42rpx;
	height: 42rpx;
	border-radius: 8rpx;
	background: #eef2f5;
	flex-shrink: 0;
}

.mine-page__menu-text {
	flex: 1;
	margin-left: 16rpx;
	font-size: 26rpx;
	line-height: 1.2;
	font-weight: 600;
	color: #2f343a;
}
</style>
