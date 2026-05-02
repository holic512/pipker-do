<template>
	<kyyy-entry-placeholder-page
		title="错词本"
		description="这里会集中承接英语学习里记忆反复出错、容易混淆和需要回看的单词。"
		notice-text="错词本入口已经补齐。后续会把错词收集、复习排序和掌握状态接到这里。"
		icon="help-filled"
		icon-color="#49627c"
		icon-background="linear-gradient(135deg, rgba(225, 233, 247, 0.98), rgba(199, 212, 234, 0.95))"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyEntryPlaceholderPage from '@/components/kyyy/entry-placeholder-page.vue'

export default defineComponent({
	name: 'KyyyWrongWordPage',
	components: {
		KyyyEntryPlaceholderPage
	}
})
</script>
