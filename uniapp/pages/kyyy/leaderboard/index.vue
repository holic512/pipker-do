<template>
	<kyyy-entry-placeholder-page
		title="排行榜"
		description="这里会展示英语业务的阶段排行、学习量变化和个人位置，方便查看当前节奏。"
		notice-text="排行榜入口已经补齐。后续会接入英语学习榜单、周期切换和个人排名焦点。"
		icon="medal-filled"
		icon-color="#476455"
		icon-background="linear-gradient(135deg, rgba(226, 239, 232, 0.98), rgba(202, 224, 210, 0.95))"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyEntryPlaceholderPage from '@/components/kyyy/entry-placeholder-page.vue'

export default defineComponent({
	name: 'KyyyLeaderboardPage',
	components: {
		KyyyEntryPlaceholderPage
	}
})
</script>
