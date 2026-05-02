<template>
	<kyyy-feature-shell-page
		current="composition"
		title="英语作文"
		description="作文模块先按大小作文、主题素材、句型表达三块落骨架，后续可以继续接入模板背诵、范文拆解和 AI 批改。"
		hero-icon="compose"
		hero-background="linear-gradient(135deg, #5b647d 0%, #8191ad 100%)"
		:hero-chips="heroChips"
		section-title="作文方向"
		:cards="cards"
		notice-text="这里先作为英语作文的统一入口页。后续新增小作文、大作文、热点主题或模板库时，可以直接拆到 pages/kyyy/composition 下继续细分。"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyFeatureShellPage from '@/components/kyyy/feature-shell-page.vue'

export default defineComponent({
	name: 'KyyyCompositionPage',
	components: {
		KyyyFeatureShellPage
	},
	data() {
		return {
			heroChips: [
				{
					key: 'template',
					text: '大小作文模板',
					icon: 'compose'
				},
				{
					key: 'material',
					text: '主题表达积累',
					icon: 'star-filled'
				}
			],
			cards: [
				{
					key: 'sentence',
					title: '高频句型',
					description: '沉淀开头、论证、结尾的稳定句型，后面可继续接模板库。',
					icon: 'compose',
					iconBg: 'linear-gradient(135deg, rgba(219, 228, 247, 0.98), rgba(191, 205, 231, 0.95))',
					iconColor: '#4f5f79'
				},
				{
					key: 'theme',
					title: '主题素材',
					description: '按教育、科技、文化、社会等主题整理例子与表达。',
					icon: 'flag-filled',
					iconBg: 'linear-gradient(135deg, rgba(248, 236, 220, 0.98), rgba(232, 214, 194, 0.95))',
					iconColor: '#8a612f'
				},
				{
					key: 'essay',
					title: '范文拆解',
					description: '预留优秀范文分析、段落结构总结和改写练习入口。',
					icon: 'list',
					iconBg: 'linear-gradient(135deg, rgba(226, 239, 231, 0.98), rgba(204, 224, 210, 0.95))',
					iconColor: '#466352'
				}
			]
		}
	}
})
</script>
