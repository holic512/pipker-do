<template>
	<kyyy-feature-shell-page
		current="practice"
		title="英语练习"
		description="练习模块先给词汇、阅读、复盘做统一落位，后续真题刷题、阶段训练和错题回看都可以继续接进来。"
		hero-icon="checkbox-filled"
		hero-background="linear-gradient(135deg, #4f6178 0%, #758aa4 100%)"
		:hero-chips="heroChips"
		section-title="练习方向"
		:cards="cards"
		notice-text="这里会承接英语项目的日常训练入口。等题目结构和接口明确后，再按题型细分到独立页面即可。"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyFeatureShellPage from '@/components/kyyy/feature-shell-page.vue'

export default defineComponent({
	name: 'KyyyPracticePage',
	components: {
		KyyyFeatureShellPage
	},
	data() {
		return {
			heroChips: [
				{
					key: 'word',
					text: '词汇快刷',
					icon: 'checkbox-filled'
				},
				{
					key: 'reading',
					text: '阅读定时训练',
					icon: 'calendar'
				}
			],
			cards: [
				{
					key: 'vocabulary',
					title: '单词训练',
					description: '后续接入单词计划、熟词僻义与高频短语记忆。',
					icon: 'checkbox-filled',
					iconBg: 'linear-gradient(135deg, rgba(219, 228, 247, 0.98), rgba(191, 205, 231, 0.95))',
					iconColor: '#4f5f79'
				},
				{
					key: 'reading',
					title: '阅读练习',
					description: '预留篇章阅读、定位技巧和题干拆解的练习位。',
					icon: 'compose',
					iconBg: 'linear-gradient(135deg, rgba(243, 234, 218, 0.98), rgba(229, 214, 193, 0.95))',
					iconColor: '#8b6334'
				},
				{
					key: 'review',
					title: '复盘回看',
					description: '后续把错题、阶段反馈和能力短板分析集中到这里。',
					icon: 'refresh',
					iconBg: 'linear-gradient(135deg, rgba(225, 239, 232, 0.98), rgba(202, 224, 210, 0.95))',
					iconColor: '#466352'
				}
			]
		}
	}
})
</script>
