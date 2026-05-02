<template>
	<kyyy-entry-placeholder-page
		title="错题本"
		description="这里会承接英语阅读、翻译和阶段训练里做错过的题目，方便后续集中复盘。"
		notice-text="错题本入口已经补齐。后续会把英语专项错题、错因回顾和重练流程接到这里。"
		icon="compose"
		icon-color="#8b6235"
		icon-background="linear-gradient(135deg, rgba(247, 236, 220, 0.98), rgba(232, 215, 194, 0.95))"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyEntryPlaceholderPage from '@/components/kyyy/entry-placeholder-page.vue'

export default defineComponent({
	name: 'KyyyWrongQuestionPage',
	components: {
		KyyyEntryPlaceholderPage
	}
})
</script>
