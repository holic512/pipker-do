<template>
	<kyyy-entry-placeholder-page
		title="AI练习"
		description="这里会承接英语专项里的智能练习、个性化训练与后续 AI 辅助学习能力。"
		notice-text="AI练习入口已经补齐。后续会按英语业务节奏继续补接智能练习和个性化生成能力。"
		icon="chatboxes-filled"
		icon-color="#5a5f7f"
		icon-background="linear-gradient(135deg, rgba(231, 233, 246, 0.98), rgba(212, 217, 238, 0.95))"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyEntryPlaceholderPage from '@/components/kyyy/entry-placeholder-page.vue'

export default defineComponent({
	name: 'KyyyAiPracticePage',
	components: {
		KyyyEntryPlaceholderPage
	}
})
</script>
