<!--
@file KyzzMinePage
@project pipker-do
@module 考研政治 / 小程序我的页
@description 复用通用我的页框架，承载政治业务的刷题设置、重置进度和意见反馈专项入口。
@logic 1. 复用公共资料、会员与通用菜单；2. 通过 special 插槽注入政治专项按钮；3. 维护刷题设置弹窗和重置进度交互。
@dependencies Component: @/components/account/mine-page.vue, Component: @/components/kyzz/kyzz-tabbar.vue, Component: @/components/kyzz/practice/PracticeSettingsPopup.vue, API: @/pages/kyzz/api/practice
@index_tags 考研政治, 我的页, 刷题设置, 重置进度, 专项入口
@author holic512
-->
<template>
	<!-- AI 索引: KYZZ 我的页，复用公共我的页并注入政治专项入口。 -->
	<view>
		<mine-page
			ref="minePage"
			profile-url="/pages/kyzz/profile/edit"
		>
			<template #special>
				<view class="kyzz-mine__special-actions">
					<view class="mine-page__menu-item kyzz-mine__special-action" @tap="openPracticeSettingsPopup">
						<view class="mine-page__menu-icon">
							<uni-icons type="gear" size="18" color="#64748b" />
						</view>
						<text class="mine-page__menu-text">刷题设置</text>
						<uni-icons type="right" size="16" color="#c3cad7" />
					</view>

					<view class="mine-page__menu-item kyzz-mine__special-action" @tap="confirmAndResetPracticeProgress">
						<view class="mine-page__menu-icon">
							<uni-icons type="refreshempty" size="18" color="#64748b" />
						</view>
						<text class="mine-page__menu-text">重置刷题进度</text>
						<uni-icons type="right" size="16" color="#c3cad7" />
					</view>

					<view class="mine-page__menu-item kyzz-mine__special-action" @tap="showFeedbackPlaceholder">
						<view class="mine-page__menu-icon">
							<uni-icons type="chatboxes" size="18" color="#64748b" />
						</view>
						<text class="mine-page__menu-text">意见反馈</text>
						<uni-icons type="right" size="16" color="#c3cad7" />
					</view>
				</view>
			</template>

			<template #tabbar>
				<kyzz-tabbar v-if="!practiceSettingsPopupVisible" current="mine" />
			</template>
		</mine-page>

		<uni-popup
			ref="practiceSettingsPopup"
			type="bottom"
			background-color="#ffffff"
			border-radius="28rpx 28rpx 0 0"
			:is-mask-click="true"
			@change="handlePracticeSettingsPopupChange"
		>
			<practice-settings-popup
				:auto-jump-on-correct="practiceSettings.autoJumpOnCorrect"
				:bank-practice-choice-only="practiceSettings.bankPracticeChoiceOnly"
				:syncing="practiceSettings.syncing"
				@close="closePracticeSettingsPopup"
				@change-auto-jump="handlePracticeAutoJumpChange"
				@change-bank-practice-choice-only="handlePracticeBankChoiceOnlyChange"
			/>
		</uni-popup>
	</view>
</template>

<script>
import MinePage from '@/components/account/mine-page.vue'
import KyzzTabbar from '@/components/kyzz/kyzz-tabbar.vue'
import PracticeSettingsPopup from '@/components/kyzz/practice/PracticeSettingsPopup.vue'
import { invalidateKyzzPreload } from '@/shared/preload/kyzz'
import { resetPracticeProgress } from '@/pages/kyzz/api/practice'
import {
	cachePracticeSettings,
	loadPracticeSettingsWithFallback,
	readCachedPracticeSettings,
	syncPracticeSettings
} from '@/pages/kyzz/practice/settings'

export default {
	name: 'KyzzMinePage',
	components: {
		MinePage,
		KyzzTabbar,
		PracticeSettingsPopup
	},
	data() {
		return {
			resettingProgress: false,
			practiceSettings: readCachedPracticeSettings(),
			practiceSettingsPopupVisible: false
		}
	},
	onShow() {
		const minePage = this.$refs.minePage
		if (minePage && typeof minePage.refreshOnShow === 'function') {
			minePage.refreshOnShow()
		}
		this.refreshPracticeSettings().catch((error) => {
			console.warn('[kyzz-mine] refresh practice settings failed', error)
		})
	},
	methods: {
		showResetPracticeProgressConfirm() {
			return new Promise((resolve) => {
				uni.showModal({
					title: '重置刷题进度',
					content: '将清空你的答题记录和错题本，题库、收藏和刷题设置会保留。确认重置后不可恢复。',
					confirmText: '确认重置',
					cancelText: '再想想',
					confirmColor: '#d9480f',
					success: (result) => resolve(!!result.confirm),
					fail: () => resolve(false)
				})
			})
		},
		async confirmAndResetPracticeProgress() {
			if (this.resettingProgress) return
			const confirmed = await this.showResetPracticeProgressConfirm()
			if (!confirmed) {
				return
			}

			this.resettingProgress = true
			uni.showLoading({ title: '重置中...' })
			try {
				await resetPracticeProgress()
				invalidateKyzzPreload()
				uni.hideLoading()
				uni.showToast({
					title: '刷题进度已重置',
					icon: 'none'
				})
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: error.message || '重置失败',
					icon: 'none'
				})
			} finally {
				this.resettingProgress = false
			}
		},
		async refreshPracticeSettings() {
			this.practiceSettings = {
				...this.practiceSettings,
				...readCachedPracticeSettings(),
				syncing: false
			}
			const settings = await loadPracticeSettingsWithFallback()
			if (this.practiceSettings.syncing) {
				return
			}
			this.practiceSettings = {
				...settings,
				syncing: false
			}
		},
		openPracticeSettingsPopup() {
			this.practiceSettingsPopupVisible = true
			this.refreshPracticeSettings().catch((error) => {
				console.warn('[kyzz-mine] refresh practice settings failed', error)
			})
			this.$refs.practiceSettingsPopup && this.$refs.practiceSettingsPopup.open()
		},
		closePracticeSettingsPopup() {
			this.practiceSettingsPopupVisible = false
			this.$refs.practiceSettingsPopup && this.$refs.practiceSettingsPopup.close()
		},
		handlePracticeSettingsPopupChange(event) {
			this.practiceSettingsPopupVisible = !!(event && event.show)
		},
		async handlePracticeAutoJumpChange(value) {
			const autoJumpOnCorrect = !!value
			this.practiceSettings = {
				...this.practiceSettings,
				autoJumpOnCorrect,
				loaded: true,
				syncing: true
			}
			cachePracticeSettings(this.practiceSettings)
			try {
				this.practiceSettings = await syncPracticeSettings({
					autoJumpOnCorrect
				})
			} catch (error) {
				this.practiceSettings = {
					...this.practiceSettings,
					loaded: true,
					syncing: false
				}
				uni.showToast({
					title: '设置已在本机生效',
					icon: 'none'
				})
			}
		},
		async handlePracticeBankChoiceOnlyChange(value) {
			const previousSettings = { ...this.practiceSettings }
			const bankPracticeChoiceOnly = !!value
			this.practiceSettings = {
				...this.practiceSettings,
				bankPracticeChoiceOnly,
				loaded: true,
				syncing: true
			}
			cachePracticeSettings(this.practiceSettings)
			invalidateKyzzPreload()
			try {
				this.practiceSettings = await syncPracticeSettings({
					bankPracticeChoiceOnly
				})
			} catch (error) {
				this.practiceSettings = {
					...previousSettings,
					loaded: true,
					syncing: false
				}
				cachePracticeSettings(this.practiceSettings)
				uni.showToast({
					title: '设置同步失败',
					icon: 'none'
				})
			}
		},
		showFeedbackPlaceholder() {
			uni.showToast({
				title: '意见反馈开发中',
				icon: 'none'
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.kyzz-mine__special-actions {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding-top: 6rpx;
}

.kyzz-mine__special-action {
	margin: 0;
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
