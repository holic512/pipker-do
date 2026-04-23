<template>
	<feature-placeholder-page
		title="排行榜"
		description="竞赛榜单会放在这里，后续会展示每日表现、连续上榜和冲榜提醒。"
		tip="这次先把入口和页面骨架准备好，保证你可以直接跳过来。"
		icon="medal-filled"
		icon-color="#6a5f45"
		icon-shell-background="linear-gradient(180deg, rgba(255, 249, 234, 0.98) 0%, rgba(246, 234, 203, 0.96) 100%)"
		icon-core-background="linear-gradient(135deg, rgba(255, 238, 196, 0.98) 0%, rgba(255, 250, 236, 0.98) 100%)"
		@primary="goStudy"
		@secondary="goBack"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FeaturePlaceholderPage from '@/components/kyzz/FeaturePlaceholderPage.vue'

// AI 索引: KYZZ 小程序排行榜占位页。

export default defineComponent({
	name: 'KyzzLeaderboardPage',
	components: {
		FeaturePlaceholderPage
	},
	methods: {
		goStudy(): void {
			uni.switchTab({
				url: '/pages/kyzz/study/index'
			})
		},
		goBack(): void {
			const currentPages = getCurrentPages()
			if (currentPages.length > 1) {
				uni.navigateBack()
				return
			}
			this.goStudy()
		}
	}
})
</script>
