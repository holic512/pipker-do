<template>
	<feature-placeholder-page
		title="笔记"
		description="笔记区会保留你的重点整理、易混点和回顾摘要，后续会接到题目场景里。"
		tip="这次先把访问入口留出来，方便你从首页快速进入。"
		icon="compose"
		icon-color="#4e6574"
		icon-shell-background="linear-gradient(180deg, rgba(236, 246, 250, 0.98) 0%, rgba(214, 232, 240, 0.96) 100%)"
		icon-core-background="linear-gradient(135deg, rgba(228, 241, 246, 0.98) 0%, rgba(248, 252, 253, 0.98) 100%)"
		@primary="goStudy"
		@secondary="goBack"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FeaturePlaceholderPage from '@/components/kyzz/FeaturePlaceholderPage.vue'

// AI 索引: KYZZ 小程序笔记占位页。

export default defineComponent({
	name: 'KyzzNotePage',
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
