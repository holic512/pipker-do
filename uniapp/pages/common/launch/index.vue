<template>
	<!-- AI 索引: 小程序独立冷启动页，避免 tabBar 首页参与首帧渲染。 -->
	<view class="launch-page theme-page">
		<launch-overlay />
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {
	hasCompletedLaunchBootstrap,
	startLaunchBootstrap,
	subscribeLaunchState,
	type LaunchSnapshot
} from '@/shared/launch'

type LaunchUnsubscribe = (() => boolean) | null

const HOME_TAB_PATH = '/pages/kyzz/study/index'

export default defineComponent({
	name: 'LaunchPage',
	data() {
		return {
			navigated: false,
			unsubscribeLaunch: null as LaunchUnsubscribe
		}
	},
	onLoad() {
		this.unsubscribeLaunch = subscribeLaunchState((_snapshot: LaunchSnapshot) => {
			if (hasCompletedLaunchBootstrap()) {
				this.enterHomeTab()
			}
		})
		this.startLaunch()
	},
	onShow() {
		if (hasCompletedLaunchBootstrap()) {
			this.enterHomeTab()
		}
	},
	beforeUnmount() {
		this.disposeLaunchSubscription()
	},
	methods: {
		async startLaunch(): Promise<void> {
			try {
				await startLaunchBootstrap()
				this.enterHomeTab()
			} catch (error) {
				console.warn('[launch-page] bootstrap failed, waiting for retry', error)
			}
		},
		enterHomeTab(): void {
			if (this.navigated) {
				return
			}
			this.navigated = true
			this.disposeLaunchSubscription()
			uni.switchTab({
				url: HOME_TAB_PATH,
				fail: (error: unknown) => {
					this.navigated = false
					console.warn('[launch-page] switch home tab failed', error)
				}
			})
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
</style>
