<template>
	<feature-placeholder-page
		title="收藏"
		description="收藏功能入口先放好，后面会补成你常用题目或内容的快速回看区。"
		tip="这次先保证从首页可以顺畅跳转，不会落到空白页。"
		icon="star-filled"
		icon-color="#7d5a4f"
		icon-shell-background="linear-gradient(180deg, rgba(255, 242, 238, 0.98) 0%, rgba(247, 225, 218, 0.96) 100%)"
		icon-core-background="linear-gradient(135deg, rgba(255, 233, 226, 0.98) 0%, rgba(255, 248, 245, 0.98) 100%)"
		@primary="goStudy"
		@secondary="goBack"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FeaturePlaceholderPage from '@/components/kyzz/FeaturePlaceholderPage.vue'

// AI 索引: KYZZ 小程序收藏占位页。

export default defineComponent({
	name: 'KyzzFavoritePage',
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
