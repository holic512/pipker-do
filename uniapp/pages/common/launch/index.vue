<template>
	<!-- AI 索引: 小程序独立冷启动页，避免 tabBar 首页参与首帧渲染，并承载首次用户协议弹窗。 -->
	<view class="launch-page theme-page">
		<launch-overlay />

		<view v-if="agreementDialogVisible" class="launch-page__agreement-mask">
			<view class="launch-page__agreement-dialog">
				<text class="launch-page__agreement-title">用户协议提示</text>
				<view class="launch-page__agreement-content">
					<text>使用 pipker-do（考研政治学习小程序）前，请阅读并同意</text>
					<text class="launch-page__agreement-link" @tap.stop="openAgreementPage">《用户协议》</text>
					<text>。我们会根据协议为你提供题库、练习、会员兑换等服务。</text>
				</view>
				<view class="launch-page__agreement-actions">
					<button class="launch-page__agreement-cancel" :disabled="agreementSubmitting" @tap="rejectAgreement">不同意</button>
					<button class="launch-page__agreement-confirm" :disabled="agreementSubmitting" @tap="confirmAgreement">
						{{ agreementSubmitting ? '提交中...' : '同意并继续' }}
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {
	getLaunchSnapshot,
	hasCompletedLaunchBootstrap,
	startLaunchBootstrap,
	subscribeLaunchState,
	type LaunchSnapshot
} from '@/shared/launch'
import { acceptUserAgreement } from '@/shared/api/user'
import { CURRENT_AGREEMENT_VERSION, cacheAgreementAcceptance, hasCachedAgreementAcceptance } from '@/shared/auth/agreement'
import { openMiniappProject } from '@/shared/project'
import { getSessionSnapshot, setCurrentUser } from '@/shared/session/session'

type LaunchUnsubscribe = (() => boolean) | null

type AgreementUser = {
	id?: string | number
	agreementVersion?: string
	agreementAccepted?: boolean
	agreementAcceptedAt?: string
}

const AGREEMENT_PAGE_PATH = '/pages/common/agreement/index'

function getCurrentAgreementUser(): AgreementUser | null {
	const snapshot = getSessionSnapshot<AgreementUser>()
	return snapshot.currentUser
}

function hasAcceptedAgreement(): boolean {
	const currentUser = getCurrentAgreementUser()
	return !currentUser
		|| (currentUser.agreementAccepted === true && currentUser.agreementVersion === CURRENT_AGREEMENT_VERSION)
		|| hasCachedAgreementAcceptance(currentUser)
}

export default defineComponent({
	name: 'LaunchPage',
	data() {
		return {
			navigated: false,
			agreementDialogVisible: false,
			agreementSubmitting: false,
			unsubscribeLaunch: null as LaunchUnsubscribe
		}
	},
	onLoad() {
		this.unsubscribeLaunch = subscribeLaunchState((_snapshot: LaunchSnapshot) => {
			if (hasCompletedLaunchBootstrap()) {
				this.enterNextStep()
			}
		})
		this.startLaunch()
	},
	onShow() {
		if (hasCompletedLaunchBootstrap() && !this.agreementDialogVisible) {
			this.enterNextStep()
		}
	},
	beforeUnmount() {
		this.disposeLaunchSubscription()
	},
	methods: {
		async startLaunch(): Promise<void> {
			try {
				await startLaunchBootstrap()
				this.enterNextStep()
			} catch (error) {
				console.warn('[launch-page] bootstrap failed, waiting for retry', error)
			}
		},
		enterNextStep(): void {
			if (this.navigated || this.agreementDialogVisible) {
				return
			}
			if (!hasAcceptedAgreement()) {
				this.agreementDialogVisible = true
				return
			}
			this.enterDefaultProject()
		},
		enterDefaultProject(): void {
			if (this.navigated) {
				return
			}
			this.navigated = true
			this.disposeLaunchSubscription()
			const target = getLaunchSnapshot().defaultProjectTarget
			openMiniappProject(target)
				.catch((error: unknown) => {
					this.navigated = false
					console.warn('[launch-page] open default project failed', error)
					uni.showToast({
						title: '打开默认项目失败',
						icon: 'none'
					})
				})
		},
		openAgreementPage(): void {
			uni.navigateTo({
				url: AGREEMENT_PAGE_PATH
			})
		},
		rejectAgreement(): void {
			uni.showToast({
				title: '需同意用户协议后继续使用',
				icon: 'none'
			})
		},
		async confirmAgreement(): Promise<void> {
			if (this.agreementSubmitting) {
				return
			}
			this.agreementSubmitting = true
			uni.showLoading({ title: '提交中...' })
			try {
				const currentUser = await acceptUserAgreement<AgreementUser>()
				setCurrentUser(currentUser)
				cacheAgreementAcceptance(currentUser, currentUser.agreementAcceptedAt)
				this.agreementDialogVisible = false
				uni.hideLoading()
				this.enterDefaultProject()
			} catch (error) {
				uni.hideLoading()
				const message = error instanceof Error && error.message ? error.message : '提交失败，请稍后重试'
				uni.showToast({
					title: message,
					icon: 'none'
				})
			} finally {
				this.agreementSubmitting = false
			}
		},
		disposeLaunchSubscription(): void {
			if (this.unsubscribeLaunch) {
				this.unsubscribeLaunch()
				this.unsubscribeLaunch = null
			}
		}
	}
})
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.launch-page {
	min-height: 100vh;
	background: $pipker-surface;
}

.launch-page__agreement-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 20000;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 48rpx;
	background: rgba(15, 23, 42, 0.28);
}

.launch-page__agreement-dialog {
	width: 100%;
	max-width: 620rpx;
	padding: 38rpx 34rpx 30rpx;
	border-radius: 26rpx;
	background: #ffffff;
	box-shadow: 0 24rpx 58rpx rgba(30, 41, 59, 0.18);
}

.launch-page__agreement-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.25;
	font-weight: 700;
	color: $uni-main-color;
	text-align: center;
}

.launch-page__agreement-content {
	margin-top: 24rpx;
	font-size: 27rpx;
	line-height: 1.78;
	color: $uni-base-color;
}

.launch-page__agreement-link {
	display: inline;
	color: $uni-primary;
	font-weight: 700;
}

.launch-page__agreement-actions {
	display: flex;
	align-items: center;
	gap: 18rpx;
	margin-top: 34rpx;
}

.launch-page__agreement-cancel,
.launch-page__agreement-confirm {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 78rpx;
	border-radius: 18rpx;
	font-size: 27rpx;
	font-weight: 700;
}

.launch-page__agreement-cancel {
	background: #f1f5f9;
	color: $uni-secondary-color;
}

.launch-page__agreement-confirm {
	background: $pipker-gradient-primary;
	color: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(96, 132, 214, 0.2);
}

.launch-page__agreement-cancel[disabled],
.launch-page__agreement-confirm[disabled] {
	opacity: 0.72;
}
</style>
