<template>
	<kyyy-feature-shell-page
		current="translation"
		title="英语翻译"
		description="翻译模块先按长难句、段落翻译、表达对照三块搭骨架，后续可继续补接句法分析、得分表达和逐句精讲。"
		hero-icon="paperplane-filled"
		hero-background="linear-gradient(135deg, #4f6375 0%, #7f91a6 100%)"
		:hero-chips="heroChips"
		section-title="翻译方向"
		:cards="cards"
		notice-text="翻译页先保持轻骨架，重点是先把英语项目的一级入口和目录定住，后面再根据真题与题型继续细分。"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyFeatureShellPage from '@/components/kyyy/feature-shell-page.vue'

export default defineComponent({
	name: 'KyyyTranslationPage',
	components: {
		KyyyFeatureShellPage
	},
	data() {
		return {
			heroChips: [
				{
					key: 'sentence',
					text: '长难句拆解',
					icon: 'paperplane'
				},
				{
					key: 'exam',
					text: '真题段落翻译',
					icon: 'flag'
				}
			],
			cards: [
				{
					key: 'syntax',
					title: '句法分析',
					description: '后续承接主干提取、从句拆分与长难句通读训练。',
					icon: 'paperplane-filled',
					iconBg: 'linear-gradient(135deg, rgba(220, 229, 247, 0.98), rgba(192, 206, 231, 0.95))',
					iconColor: '#4e5f78'
				},
				{
					key: 'paragraph',
					title: '段落翻译',
					description: '预留真题段落逐句翻译、表达核对与错误定位入口。',
					icon: 'list',
					iconBg: 'linear-gradient(135deg, rgba(247, 236, 221, 0.98), rgba(232, 214, 194, 0.95))',
					iconColor: '#8a6030'
				},
				{
					key: 'expression',
					title: '得分表达',
					description: '把高频术语、书面表达和中英文对照短语收束到这里。',
					icon: 'chatboxes-filled',
					iconBg: 'linear-gradient(135deg, rgba(225, 239, 232, 0.98), rgba(202, 224, 210, 0.95))',
					iconColor: '#476454'
				}
			]
		}
	}
})
</script>
