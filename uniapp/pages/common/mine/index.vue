<template>
	<page-shell
		current="mine"
		root-class="mine-page"
		content-class="mine-page__content"
	>
		<view class="mine-page__profile">
			<view class="mine-page__avatar" @tap="goProfile">
				<image
					v-if="user.avatarUrl"
					class="mine-page__avatar-image"
					:src="user.avatarUrl"
					mode="aspectFill"
				/>
				<view v-else>
					<view class="mine-page__avatar-glow"></view>
					<view class="mine-page__avatar-head"></view>
					<view class="mine-page__avatar-body"></view>
				</view>
			</view>

			<view class="mine-page__profile-main">
				<text class="mine-page__name">{{ user.name }}</text>
				<view class="mine-page__profile-link" @tap="goProfile">
					<text class="mine-page__profile-link-text">更新资料</text>
					<uni-icons type="right" size="15" color="#7f8697" />
				</view>
			</view>
		</view>

		<view class="mine-page__vip-card" :class="{ 'mine-page__vip-card--inactive': !user.isVip }">
			<view class="mine-page__vip-header">
				<view>
					<text class="mine-page__vip-title">{{ user.vipTitle }}</text>
					<text class="mine-page__vip-expire">{{ user.vipDescription }}</text>
				</view>
				<view class="mine-page__vip-badge" :class="{ 'mine-page__vip-badge--active': user.isVip }">
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

		<view v-if="redeemVisible" class="mine-page__redeem-mask" @tap="closeRedeemPopup">
			<view class="mine-page__redeem-dialog" @tap.stop>
				<view class="mine-page__redeem-header">
					<view>
						<text class="mine-page__redeem-title">兑换会员</text>
						<text class="mine-page__redeem-subtitle">请输入后台生成的兑换 Key</text>
					</view>
					<view class="mine-page__redeem-close" @tap="closeRedeemPopup">
						<uni-icons type="closeempty" size="24" color="#7b8494" />
					</view>
				</view>
				<input
					v-model="redeemForm.key"
					class="mine-page__redeem-input"
					maxlength="40"
					placeholder="例如 VIPXXXXXXXXXXXX"
					placeholder-class="mine-page__redeem-placeholder"
					:auto-focus="redeemVisible"
					@input="handleRedeemInput"
				/>
				<text class="mine-page__redeem-tip">兑换成功后，会员将自动按当前到期时间顺延。</text>
				<button class="mine-page__redeem-submit" :disabled="redeeming" @tap="submitRedeem">
					{{ redeeming ? '兑换中...' : '立即兑换' }}
				</button>
			</view>
		</view>
	</page-shell>
</template>

<script>
import { bootstrapAuth, getSessionSnapshot, setCurrentUser, subscribeSession } from '@/shared/session/session'
import { getVipStatus, redeemVipKey } from '@/shared/api/vip'

const VIP_TYPE_TEXT = {
	month: '月卡会员',
	year: '年卡会员',
	lifetime: '永久会员'
}

export default {
	name: 'MinePage',
	data() {
		return {
			user: {
				name: '微信用户',
				avatarUrl: '',
				isVip: false,
				vipType: '',
				vipTitle: 'VIP会员',
				vipDescription: '未开通，兑换后可解锁完整会员权益'
			},
			redeemVisible: false,
			redeeming: false,
			redeemForm: {
				key: ''
			},
			unsubscribeSession: null,
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
	onLoad() {
		this.syncUser(getSessionSnapshot())
		this.unsubscribeSession = subscribeSession((snapshot) => {
			this.syncUser(snapshot)
		})
	},
	onShow() {
		bootstrapAuth({ silent: true }).then(() => {
			this.refreshVipStatus({ silent: true }).catch((error) => {
				console.warn('[mine] refresh vip status failed', error)
			})
		}).catch((error) => {
			console.warn('[mine] bootstrap failed', error)
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
			const vipType = vipInfo && vipInfo.vipType ? vipInfo.vipType : ''
			const expireAt = vipInfo && vipInfo.expireAt ? vipInfo.expireAt : ''
			this.user = {
				name: currentUser && currentUser.nickname ? currentUser.nickname : '微信用户',
				avatarUrl: currentUser && currentUser.avatarUrl ? currentUser.avatarUrl : '',
				isVip,
				vipType,
				vipTitle: isVip ? (VIP_TYPE_TEXT[vipType] || 'VIP会员') : 'VIP会员',
				vipDescription: isVip
					? `有效期至 ${expireAt || '长期有效'}`
					: '未开通，兑换后可解锁完整会员权益'
			}
		},
		goProfile() {
			uni.navigateTo({
				url: '/pages/common/profile/edit'
			})
		},
		handleAction(key) {
			if (key === 'redeem') {
				this.openRedeemPopup()
				return
			}
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
		},
		openRedeemPopup() {
			this.redeemVisible = true
			this.refreshVipStatus({ silent: true }).catch((error) => {
				console.warn('[mine] refresh vip status failed', error)
			})
		},
		closeRedeemPopup() {
			if (this.redeeming) return
			this.redeemVisible = false
		},
		handleRedeemInput(event) {
			const value = event && event.detail ? event.detail.value : this.redeemForm.key
			this.redeemForm.key = String(value || '').toUpperCase().replace(/\s+/g, '')
		},
		async submitRedeem() {
			if (this.redeeming) return
			const key = String(this.redeemForm.key || '').trim().toUpperCase()
			if (!key) {
				uni.showToast({
					title: '请输入兑换码',
					icon: 'none'
				})
				return
			}

			this.redeeming = true
			uni.showLoading({ title: '兑换中...' })
			try {
				const record = await redeemVipKey({ key })
				await this.refreshVipStatus({ silent: true }).catch((error) => {
					console.warn('[mine] refresh vip status after redeem failed', error)
				})
				this.redeemVisible = false
				this.redeemForm.key = ''
				uni.hideLoading()
				uni.showToast({
					title: record && record.endTime ? `兑换成功，有效期至 ${record.endTime.slice(0, 10)}` : '兑换成功',
					icon: 'none',
					duration: 2200
				})
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: error.message || '兑换失败',
					icon: 'none'
				})
			} finally {
				this.redeeming = false
			}
		},
		async refreshVipStatus(options = {}) {
			try {
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
				return vipInfo
			} catch (error) {
				if (!options.silent) {
					uni.showToast({
						title: error.message || '会员状态刷新失败',
						icon: 'none'
					})
				}
				throw error
			}
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

.mine-page__avatar-image {
	width: 100%;
	height: 100%;
	display: block;
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

.mine-page__vip-card--inactive {
	background: linear-gradient(115deg, #64748b 0%, #334155 100%);
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

.mine-page__vip-badge--active {
	background: linear-gradient(180deg, rgba(238, 196, 110, 0.88) 0%, rgba(196, 137, 54, 0.66) 100%);
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

.mine-page__redeem-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 10000;
	display: flex;
	align-items: flex-end;
	justify-content: center;
	padding: 32rpx 24rpx calc(env(safe-area-inset-bottom) + 32rpx);
	background: rgba(15, 23, 42, 0.42);
}

.mine-page__redeem-dialog {
	width: 100%;
	padding: 34rpx 30rpx 30rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-shadow: 0 24rpx 70rpx rgba(15, 23, 42, 0.2);
}

.mine-page__redeem-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
}

.mine-page__redeem-title {
	display: block;
	font-size: 32rpx;
	font-weight: 800;
	color: #1f2937;
}

.mine-page__redeem-subtitle {
	display: block;
	margin-top: 10rpx;
	font-size: 23rpx;
	color: #7b8494;
}

.mine-page__redeem-close {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 54rpx;
	height: 54rpx;
	border-radius: 50%;
	background: #f4f6f9;
}

.mine-page__redeem-input {
	margin-top: 30rpx;
	height: 92rpx;
	padding: 0 28rpx;
	border-radius: 18rpx;
	background: #f6f8fb;
	font-size: 28rpx;
	font-weight: 700;
	letter-spacing: 1rpx;
	color: #1f2937;
}

.mine-page__redeem-placeholder {
	font-weight: 400;
	letter-spacing: 0;
	color: #a8b0bd;
}

.mine-page__redeem-tip {
	display: block;
	margin-top: 16rpx;
	font-size: 22rpx;
	line-height: 1.5;
	color: #7b8494;
}

.mine-page__redeem-submit {
	margin-top: 30rpx;
	height: 84rpx;
	border-radius: 999rpx;
	background: #20272e;
	color: #ffffff;
	font-size: 28rpx;
	font-weight: 700;
	line-height: 84rpx;
}

.mine-page__redeem-submit[disabled] {
	opacity: 0.72;
}
</style>
