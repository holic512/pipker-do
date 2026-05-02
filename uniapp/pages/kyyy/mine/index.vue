<template>
	<!-- AI 索引: KYYY 我的页骨架 -->
	<page-shell
		root-class="kyyy-mine theme-page"
		content-style="padding: 0 24rpx 40rpx;"
	>
		<view class="kyyy-mine__hero">
			<view class="kyyy-mine__avatar" @tap="goProfile">
				<image
					v-if="user.avatarUrl"
					class="kyyy-mine__avatar-image"
					:src="user.avatarUrl"
					mode="aspectFill"
				/>
				<view v-else class="kyyy-mine__avatar-fallback">
					<uni-icons type="person-filled" size="34" color="#94a3b8" />
				</view>
			</view>

			<view class="kyyy-mine__hero-copy">
				<text class="kyyy-mine__eyebrow">ENGLISH ACCOUNT</text>
				<text class="kyyy-mine__name">{{ user.name }}</text>
				<text class="kyyy-mine__vip">{{ user.vipDescription }}</text>
			</view>

			<view class="kyyy-mine__status" :class="{ 'is-active': user.isVip }">
				<uni-icons :type="user.isVip ? 'star-filled' : 'star'" size="16" :color="user.isVip ? '#fff7e5' : '#6a7688'" />
				<text>{{ user.isVip ? 'VIP' : 'NORMAL' }}</text>
			</view>
		</view>

		<view class="kyyy-mine__section">
			<view class="kyyy-mine__section-head">
				<text class="kyyy-mine__section-overline">ACCOUNT</text>
				<text class="kyyy-mine__section-title">个人中心</text>
			</view>

			<view class="kyyy-mine__action-list">
				<view class="kyyy-mine__action-card" @tap="goProfile">
					<view class="kyyy-mine__action-icon">
						<uni-icons type="person" size="18" color="#4f6078" />
					</view>
					<view class="kyyy-mine__action-copy">
						<text class="kyyy-mine__action-title">更新资料</text>
						<text class="kyyy-mine__action-desc">沿用统一资料能力，后续可扩展英语专属字段。</text>
					</view>
					<uni-icons type="right" size="16" color="#9aa4b3" />
				</view>

				<view class="kyyy-mine__action-card" @tap="openAgreement">
					<view class="kyyy-mine__action-icon">
						<uni-icons type="paperclip" size="18" color="#4f6078" />
					</view>
					<view class="kyyy-mine__action-copy">
						<text class="kyyy-mine__action-title">用户协议</text>
						<text class="kyyy-mine__action-desc">协议仍是全局页，但入口和页面组织保持项目独立。</text>
					</view>
					<uni-icons type="right" size="16" color="#9aa4b3" />
				</view>
			</view>
		</view>

		<view class="kyyy-mine__notice">
			<text class="kyyy-mine__notice-title">骨架说明</text>
			<text class="kyyy-mine__notice-text">
				英语项目的我的页已经独立建好。后续词汇成长、阅读记录、真题订阅、兑换与客服入口可以继续在这里按英语业务扩展。
			</text>
		</view>

		<template #tabbar>
			<kyyy-tabbar current="mine" />
		</template>
	</page-shell>
</template>

<script>
import { bootstrapAuth, getSessionSnapshot, setCurrentUser, subscribeSession } from '@/shared/session/session'
import { getVipStatus } from '@/shared/api/vip'
import { resolveUserDisplayAvatarUrl } from '@/shared/media/avatar-cache'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'

export default {
	name: 'KyyyMinePage',
	components: {
		KyyyTabbar
	},
	data() {
		return {
			user: {
				name: '微信用户',
				avatarUrl: '',
				isVip: false,
				vipDescription: '未开通会员'
			},
			unsubscribeSession: null
		}
	},
	onLoad() {
		this.syncUser(getSessionSnapshot())
		this.unsubscribeSession = subscribeSession((snapshot) => {
			this.syncUser(snapshot)
		})
	},
	onShow() {
		bootstrapAuth({ silent: true }).then(() => {
			this.refreshVipStatus().catch((error) => {
				console.warn('[kyyy-mine] refresh vip status failed', error)
			})
		}).catch((error) => {
			console.warn('[kyyy-mine] bootstrap failed', error)
		})
	},
	onUnload() {
		if (this.unsubscribeSession) {
			this.unsubscribeSession()
			this.unsubscribeSession = null
		}
	},
	methods: {
		syncUser(snapshot) {
			const currentUser = snapshot && snapshot.currentUser ? snapshot.currentUser : null
			const vipInfo = currentUser && currentUser.vipInfo ? currentUser.vipInfo : null
			const isVip = !!(vipInfo && (vipInfo.isVip || vipInfo.vip))
			const expireAt = vipInfo && vipInfo.expireAt ? vipInfo.expireAt : ''
			this.user = {
				name: currentUser && currentUser.nickname ? currentUser.nickname : '微信用户',
				avatarUrl: resolveUserDisplayAvatarUrl(currentUser),
				isVip,
				vipDescription: isVip ? `有效期至 ${expireAt || '长期有效'}` : '未开通会员'
			}
		},
		goProfile() {
			uni.navigateTo({
				url: '/pages/kyyy/profile/edit'
			})
		},
		openAgreement() {
			uni.navigateTo({
				url: '/pages/common/agreement/index'
			})
		},
		async refreshVipStatus() {
			const vipInfo = await getVipStatus()
			const snapshot = getSessionSnapshot()
			const currentUser = snapshot && snapshot.currentUser ? snapshot.currentUser : {}
			const nextUser = {
				...currentUser,
				vipInfo
			}
			setCurrentUser(nextUser)
			this.syncUser({
				...snapshot,
				currentUser: nextUser
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.kyyy-mine {
	min-height: 100vh;
}

.kyyy-mine__hero {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx 6rpx 30rpx;
}

.kyyy-mine__avatar {
	width: 108rpx;
	height: 108rpx;
	border-radius: 26rpx;
	overflow: hidden;
	background: linear-gradient(145deg, #eff3f8 0%, #dfe7f1 100%);
	flex-shrink: 0;
}

.kyyy-mine__avatar-image,
.kyyy-mine__avatar-fallback {
	width: 100%;
	height: 100%;
}

.kyyy-mine__avatar-fallback {
	display: flex;
	align-items: center;
	justify-content: center;
}

.kyyy-mine__hero-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.kyyy-mine__eyebrow {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.18em;
	color: #98a2b2;
}

.kyyy-mine__name {
	font-size: 40rpx;
	line-height: 1.22;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-mine__vip {
	font-size: 24rpx;
	line-height: 1.6;
	color: #6d7782;
}

.kyyy-mine__status {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 18rpx;
	border-radius: 999rpx;
	background: #eef3f8;
	font-size: 20rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #6a7688;
	flex-shrink: 0;
}

.kyyy-mine__status.is-active {
	background: #43546e;
	color: #fff7e5;
}

.kyyy-mine__section {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-mine__section-head {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	padding: 0 8rpx;
}

.kyyy-mine__section-overline {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.18em;
	color: #99a3b1;
}

.kyyy-mine__section-title {
	font-size: 34rpx;
	line-height: 1.28;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-mine__action-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-mine__action-card {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 26rpx 24rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.94);
	border: 1rpx solid rgba(215, 226, 255, 0.9);
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.07);
}

.kyyy-mine__action-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 80rpx;
	height: 80rpx;
	border-radius: 22rpx;
	background: linear-gradient(135deg, #e7eef8 0%, #dae5f3 100%);
	flex-shrink: 0;
}

.kyyy-mine__action-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.kyyy-mine__action-title {
	font-size: 30rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2f3940;
}

.kyyy-mine__action-desc {
	font-size: 24rpx;
	line-height: 1.62;
	color: #69737d;
}

.kyyy-mine__notice {
	margin-top: 28rpx;
	padding: 30rpx 28rpx;
	border-radius: 30rpx;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(240, 244, 247, 0.96));
	border: 1rpx solid rgba(222, 229, 236, 0.9);
}

.kyyy-mine__notice-title {
	display: block;
	font-size: 28rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #35404b;
}

.kyyy-mine__notice-text {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	line-height: 1.72;
	color: #69727b;
}
</style>
