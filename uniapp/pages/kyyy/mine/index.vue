<!--
@file KyyyMinePage
@project pipker-do
@module 考研英语 / 小程序我的页
@description 复用通用我的页框架，承载英语业务的词库入口与刷题设置专项入口。
@logic 1. 复用公共资料、会员与通用菜单；2. 通过 special 插槽注入英语专项按钮；3. 维护刷题设置弹窗与词库页跳转。
@dependencies Component: @/components/account/mine-page.vue, Component: @/components/kyyy/kyyy-tabbar.vue, Component: @/components/kyyy/practice/PracticeSettingsPopup.vue
@index_tags 考研英语, 我的页, 词库, 刷题设置, 专项入口
@author holic512
-->
<template>
	<!-- AI 索引: KYYY 我的页，复用公共我的页并注入英语专项设置。 -->
	<view>
		<mine-page
			ref="minePage"
			profile-url="/pages/kyyy/profile/edit"
		>
			<template #special>
				<view class="kyyy-mine__special-actions">
					<view class="mine-page__menu-item kyyy-mine__special-action" @tap="openWordBankPage">
						<view class="mine-page__menu-icon">
							<uni-icons type="list" size="18" color="#64748b" />
						</view>
						<text class="mine-page__menu-text">词库</text>
						<text class="kyyy-mine__menu-value">{{ practiceSettings.defaultWordBankName || '未选择' }}</text>
						<uni-icons type="right" size="16" color="#c3cad7" />
					</view>
					<view class="mine-page__menu-item kyyy-mine__special-action" @tap="openPracticeSettingsPopup">
						<view class="mine-page__menu-icon">
							<uni-icons type="gear" size="18" color="#64748b" />
						</view>
						<text class="mine-page__menu-text">刷题设置</text>
						<text class="kyyy-mine__menu-value">{{ practiceSettings.examDirectionLabel }}</text>
						<uni-icons type="right" size="16" color="#c3cad7" />
					</view>
				</view>
			</template>

			<template #tabbar>
				<kyyy-tabbar v-if="!practiceSettingsPopupVisible" current="mine" />
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
				:exam-direction="practiceSettings.examDirection"
				:exam-direction-options="practiceSettings.examDirectionOptions"
				:syncing="practiceSettings.syncing"
				@close="closePracticeSettingsPopup"
				@change-exam-direction="handlePracticeExamDirectionChange"
			/>
		</uni-popup>
	</view>
</template>

<script>
import MinePage from '@/components/account/mine-page.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import PracticeSettingsPopup from '@/components/kyyy/practice/PracticeSettingsPopup.vue'
import {
	cachePracticeSettings,
	loadPracticeSettingsWithFallback,
	readCachedPracticeSettings,
	syncPracticeSettings
} from '@/pages/kyyy/practice/settings'
import { resolveExamDirectionLabel } from '@/pages/kyyy/practice/view'

export default {
	name: 'KyyyMinePage',
	components: {
		MinePage,
		KyyyTabbar,
		PracticeSettingsPopup
	},
	data() {
		return {
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
			console.warn('[kyyy-mine] refresh practice settings failed', error)
		})
	},
	methods: {
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
		openWordBankPage() {
			uni.reLaunch({
				url: '/pages/kyyy/word-bank/index',
				fail: (error) => {
					console.warn('[kyyy-mine] open word bank failed', error)
					uni.showToast({
						title: '打开词库失败',
						icon: 'none'
					})
				}
			})
		},
		openPracticeSettingsPopup() {
			this.practiceSettingsPopupVisible = true
			this.refreshPracticeSettings().catch((error) => {
				console.warn('[kyyy-mine] refresh practice settings failed', error)
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
		async handlePracticeExamDirectionChange(value) {
			const previousSettings = { ...this.practiceSettings }
			const examDirection = value
			this.practiceSettings = {
				...this.practiceSettings,
				examDirection,
				examDirectionLabel: resolveExamDirectionLabel(examDirection, this.practiceSettings.examDirectionOptions),
				loaded: true,
				syncing: true
			}
			cachePracticeSettings(this.practiceSettings)
			try {
				this.practiceSettings = await syncPracticeSettings({
					examDirection
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
		}
	}
}
</script>

<style lang="scss" scoped>
.kyyy-mine__special-actions {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding-top: 6rpx;
}

.kyyy-mine__special-action {
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

.kyyy-mine__menu-value {
	margin-left: auto;
	margin-right: 12rpx;
	font-size: 24rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #64748b;
}
</style>
