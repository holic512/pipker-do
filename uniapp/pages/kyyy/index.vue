<template>
	<kyyy-feature-shell-page
		current="home"
		title="考研英语首页"
		description="英语项目先按首页、作文、练习、翻译、我的五段结构落位，后续阅读、词汇、真题与模考能力都在这个骨架上继续扩展。"
		hero-icon="home-filled"
		:hero-chips="heroChips"
		:cards="cards"
		notice-text="当前已完成英语项目的独立下菜单和页面骨架。后面继续补业务时，直接在 pages/kyyy 与 components/kyyy 下按模块展开即可。"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyFeatureShellPage from '@/components/kyyy/feature-shell-page.vue'

export default defineComponent({
	name: 'KyyyHomePage',
	components: {
		KyyyFeatureShellPage
	},
	data() {
		return {
			heroChips: [
				{
					key: 'ready',
					text: '首页已就绪',
					icon: 'calendar'
				},
				{
					key: 'tabbar',
					text: '5 项导航独立',
					icon: 'checkbox-filled'
				}
			],
			cards: [
				{
					key: 'composition',
					title: '作文',
					description: '承接大小作文模板、主题素材与段落表达积累。',
					icon: 'compose',
					iconBg: 'linear-gradient(135deg, rgba(215, 226, 255, 0.95), rgba(188, 205, 232, 0.95))',
					iconColor: '#46536a'
				},
				{
					key: 'practice',
					title: '练习',
					description: '用于词汇、阅读和阶段训练的日常刷题入口。',
					icon: 'checkbox-filled',
					iconBg: 'linear-gradient(135deg, rgba(248, 236, 219, 0.98), rgba(232, 214, 193, 0.98))',
					iconColor: '#8a5f2d'
				},
				{
					key: 'translation',
					title: '翻译',
					description: '预留长难句拆解、段落翻译与表达对照训练。',
					icon: 'paperplane-filled',
					iconBg: 'linear-gradient(135deg, rgba(224, 239, 231, 0.98), rgba(201, 224, 209, 0.98))',
					iconColor: '#466352'
				}
			]
		}
	}
})
</script>
