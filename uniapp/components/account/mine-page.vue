<template>
	<!-- AI 索引: 小程序跨业务我的页公共组件，业务专项入口通过 special 插槽注入。 -->
	<page-shell root-class="mine-page">
		<view class="mine-page__frame">
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
				<view class="mine-page__menu-section">
					<text class="mine-page__menu-section-title">通用</text>
					<view class="mine-page__menu-section-list">
						<view
							v-for="item in commonMenuItems"
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
				</view>

				<view v-if="hasSpecialSlot" class="mine-page__menu-section">
					<text class="mine-page__menu-section-title">专项</text>
					<view class="mine-page__menu-section-list">
						<slot name="special" />
					</view>
				</view>
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

		<view v-if="serviceVisible" class="mine-page__service-mask" @tap="closeServicePopup">
			<view class="mine-page__service-dialog" @tap.stop>
				<view class="mine-page__service-header">
					<view>
						<text class="mine-page__service-title">联系客服</text>
						<text class="mine-page__service-subtitle">加入 QQ 交流群，反馈问题或获取更新通知</text>
					</view>
					<view class="mine-page__service-close" @tap="closeServicePopup">
						<uni-icons type="closeempty" size="24" color="#7b8494" />
					</view>
				</view>
				<view class="mine-page__service-card">
					<view>
						<text class="mine-page__service-label">QQ 交流群一群</text>
						<text class="mine-page__service-number">{{ serviceGroupNumber }}</text>
					</view>
					<button class="mine-page__service-copy" @tap="copyServiceGroupNumber">复制</button>
				</view>
			</view>
		</view>

		<template #tabbar>
			<slot name="tabbar" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth, getSessionSnapshot, setCurrentUser, subscribeSession } from '@/shared/session/session'
import { getVipStatus, redeemVipKey, type VipInfo } from '@/shared/api/vip'
import {
	resolveAvatarRemoteUrl,
	resolveAvatarUserKey,
	resolveUserDisplayAvatarUrl,
	syncUserAvatarCache
} from '@/shared/media/avatar-cache'

interface MineUserView {
	name: string
	avatarUrl: string
	isVip: boolean
	vipType: string
	vipTitle: string
	vipDescription: string
}

interface MineMenuItem {
	key: string
	text: string
	icon: string
}

interface VipFeatureItem {
	key: string
	text: string
	icon: string
}

interface SessionUserLike {
	nickname?: string
	vipInfo?: VipInfo | null
	[key: string]: unknown
}

interface SessionSnapshotLike {
	currentUser?: SessionUserLike | null
	[key: string]: unknown
}

const VIP_TYPE_TEXT = {
	month: '月卡会员',
	year: '年卡会员',
	lifetime: '永久会员'
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	return error instanceof Error && error.message ? error.message : fallback
}

export default defineComponent({
	name: 'AccountMinePage',
	props: {
		profileUrl: {
			type: String,
			default: '/pages/kyzz/profile/edit'
		},
		serviceGroupNumber: {
			type: String,
			default: '1098353564'
		}
	},
	data() {
		return {
			user: {
				name: '微信用户',
				avatarUrl: '',
				isVip: false,
				vipType: '',
				vipTitle: 'VIP会员',
				vipDescription: '未开通，兑换后可解锁完整会员权益'
			} as MineUserView,
			redeemVisible: false,
			serviceVisible: false,
			redeeming: false,
			refreshingUser: false,
			redeemForm: {
				key: ''
			},
			unsubscribeSession: null as null | (() => void),
			avatarRefreshKey: '',
			vipFeatures: [
				{ key: 'unlimited', text: '无限提问', icon: 'loop' },
				{ key: 'priority', text: '优先处理', icon: 'headphones' },
				{ key: 'report', text: '深度报告', icon: 'paperclip' }
			] as VipFeatureItem[],
			commonMenuItems: [
				{ key: 'redeem', text: '激活兑换码', icon: 'scan' },
				{ key: 'service', text: '联系客服', icon: 'headphones' },
				{ key: 'agreement', text: '用户协议', icon: 'paperclip' },
				{ key: 'about', text: '关于我们', icon: 'info' }
			] as MineMenuItem[]
		}
	},
	computed: {
		hasSpecialSlot(): boolean {
			return Boolean(this.$slots.special)
		}
	},
	mounted() {
		this.syncUser(getSessionSnapshot())
		this.unsubscribeSession = subscribeSession((snapshot: unknown) => {
			this.syncUser(snapshot)
		})
		this.refreshOnShow()
	},
	beforeUnmount() {
		if (this.unsubscribeSession) {
			this.unsubscribeSession()
			this.unsubscribeSession = null
		}
	},
	methods: {
		async refreshOnShow(): Promise<void> {
			if (this.refreshingUser) {
				return
			}
			this.refreshingUser = true
			try {
				await bootstrapAuth({ silent: true })
				await this.refreshVipStatus({ silent: true })
			} catch (error) {
				console.warn('[mine-page] bootstrap or vip refresh failed', error)
			} finally {
				this.refreshingUser = false
			}
		},
		syncUser(snapshot: unknown): void {
			const currentSnapshot = snapshot as SessionSnapshotLike | null
			const currentUser = currentSnapshot && currentSnapshot.currentUser ? currentSnapshot.currentUser : null
			const vipInfo = currentUser && currentUser.vipInfo ? currentUser.vipInfo : null
			const isVip = Boolean(vipInfo && (vipInfo.isVip || vipInfo.vip))
			const vipType = vipInfo && vipInfo.vipType ? vipInfo.vipType : ''
			const expireAt = vipInfo && vipInfo.expireAt ? vipInfo.expireAt : ''
			const avatarRemoteUrl = resolveAvatarRemoteUrl(currentUser)
			const avatarUserKey = resolveAvatarUserKey(currentUser)
			const avatarRefreshKey = `${avatarUserKey}::${avatarRemoteUrl}`
			this.avatarRefreshKey = avatarRefreshKey
			this.user = {
				name: currentUser && currentUser.nickname ? currentUser.nickname : '微信用户',
				avatarUrl: resolveUserDisplayAvatarUrl(currentUser),
				isVip,
				vipType,
				vipTitle: isVip ? (VIP_TYPE_TEXT[vipType as keyof typeof VIP_TYPE_TEXT] || 'VIP会员') : 'VIP会员',
				vipDescription: isVip
					? `有效期至 ${expireAt || '长期有效'}`
					: '未开通，兑换后可解锁完整会员权益'
			}
			this.refreshAvatarCache(currentUser, avatarRefreshKey)
		},
		async refreshAvatarCache(user: unknown, expectedRefreshKey: string): Promise<void> {
			try {
				const localPath = await syncUserAvatarCache(user)
				const currentSnapshot = getSessionSnapshot() as unknown as SessionSnapshotLike
				const currentUser = currentSnapshot.currentUser || null
				const currentRemoteUrl = resolveAvatarRemoteUrl(currentUser)
				const currentRefreshKey = `${resolveAvatarUserKey(currentUser)}::${currentRemoteUrl}`
				if (
					localPath
					&& expectedRefreshKey
					&& this.avatarRefreshKey === expectedRefreshKey
					&& currentRefreshKey === expectedRefreshKey
					&& this.user.avatarUrl !== localPath
				) {
					this.user = {
						...this.user,
						avatarUrl: localPath
					}
				}
			} catch (error) {
				console.warn('[avatar] cache refresh failed', error)
			}
		},
		goProfile(): void {
			if (!this.profileUrl) {
				return
			}
			uni.navigateTo({
				url: this.profileUrl
			})
		},
		handleAction(key: string): void {
			if (key === 'redeem') {
				this.openRedeemPopup()
				return
			}
			if (key === 'agreement') {
				uni.navigateTo({
					url: '/pages/common/agreement/index'
				})
				return
			}
			if (key === 'service') {
				this.openServicePopup()
				return
			}
			if (key === 'about') {
				uni.navigateTo({
					url: '/pages/common/about/index'
				})
				return
			}

			const actionTextMap: Record<string, string> = {
				profile: '更新资料',
				redeem: '激活兑换码',
				agreement: '用户协议',
				service: '联系客服',
				about: '关于我们'
			}

			uni.showToast({
				title: `${actionTextMap[key] || '功能'}开发中`,
				icon: 'none'
			})
		},
		openRedeemPopup(): void {
			this.redeemVisible = true
			this.refreshVipStatus({ silent: true }).catch((error: unknown) => {
				console.warn('[mine-page] refresh vip status failed', error)
			})
		},
		closeRedeemPopup(): void {
			if (this.redeeming) return
			this.redeemVisible = false
		},
		openServicePopup(): void {
			this.serviceVisible = true
		},
		closeServicePopup(): void {
			this.serviceVisible = false
		},
		copyServiceGroupNumber(): void {
			uni.setClipboardData({
				data: this.serviceGroupNumber,
				success: () => {
					uni.showToast({
						title: '群号已复制',
						icon: 'none'
					})
				}
			})
		},
		handleRedeemInput(event: { detail?: { value?: string } }): void {
			const value = event && event.detail ? event.detail.value : this.redeemForm.key
			this.redeemForm.key = String(value || '').toUpperCase().replace(/\s+/g, '')
		},
		async submitRedeem(): Promise<void> {
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
				await this.refreshVipStatus({ silent: true }).catch((error: unknown) => {
					console.warn('[mine-page] refresh vip status after redeem failed', error)
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
					title: resolveErrorMessage(error, '兑换失败'),
					icon: 'none'
				})
			} finally {
				this.redeeming = false
			}
		},
		async refreshVipStatus(options: { silent?: boolean } = {}): Promise<VipInfo> {
			try {
				const vipInfo = await getVipStatus()
				const snapshot = getSessionSnapshot() as unknown as SessionSnapshotLike
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
						title: resolveErrorMessage(error, '会员状态刷新失败'),
						icon: 'none'
					})
				}
				throw error
			}
		}
	}
})
</script>

<style lang="scss">
.mine-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #fafbfc 0%, #f3f5f7 100%);
}

.mine-page__frame {
	padding: 18rpx 18rpx calc(env(safe-area-inset-bottom) + 176rpx);
	box-sizing: border-box;
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
	gap: 26rpx;
}

.mine-page__menu-section {
	display: flex;
	flex-direction: column;
}

.mine-page__menu-section-title {
	margin: 0 0 12rpx 6rpx;
	font-size: 21rpx;
	line-height: 1.2;
	font-weight: 600;
	color: #98a1af;
}

.mine-page__menu-section-list {
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

.mine-page__service-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 10000;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 32rpx 24rpx;
	background: rgba(15, 23, 42, 0.42);
}

.mine-page__service-dialog {
	width: 100%;
	padding: 34rpx 30rpx 30rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-shadow: 0 24rpx 70rpx rgba(15, 23, 42, 0.2);
}

.mine-page__service-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 24rpx;
}

.mine-page__service-title {
	display: block;
	font-size: 32rpx;
	font-weight: 800;
	color: #1f2937;
}

.mine-page__service-subtitle {
	display: block;
	margin-top: 10rpx;
	font-size: 23rpx;
	line-height: 1.5;
	color: #7b8494;
}

.mine-page__service-close {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 54rpx;
	height: 54rpx;
	border-radius: 50%;
	background: #f4f6f9;
	flex-shrink: 0;
}

.mine-page__service-card {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 22rpx;
	margin-top: 30rpx;
	padding: 24rpx;
	border-radius: 18rpx;
	background: #f6f8fb;
}

.mine-page__service-label {
	display: block;
	font-size: 23rpx;
	line-height: 1.4;
	color: #7b8494;
}

.mine-page__service-number {
	display: block;
	margin-top: 8rpx;
	font-size: 34rpx;
	line-height: 1.2;
	font-weight: 800;
	color: #1f2937;
}

.mine-page__service-copy {
	margin: 0;
	width: 116rpx;
	height: 64rpx;
	border-radius: 999rpx;
	background: #20272e;
	color: #ffffff;
	font-size: 25rpx;
	font-weight: 700;
	line-height: 64rpx;
	flex-shrink: 0;
}

.mine-page__service-copy::after {
	border: 0;
}
</style>
