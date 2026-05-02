<template>
	<kyyy-feature-shell-page
		current="practice"
		:title="pageCopy.title"
		:description="pageCopy.description"
		hero-icon="checkbox-filled"
		hero-background="linear-gradient(135deg, #4f6178 0%, #758aa4 100%)"
		:hero-chips="pageCopy.heroChips"
		:section-title="pageCopy.sectionTitle"
		:cards="pageCopy.cards"
		:notice-text="pageCopy.noticeText"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KyyyFeatureShellPage from '@/components/kyyy/feature-shell-page.vue'
import type { KyyyPracticeEntryMode } from '@/pages/kyyy/home/types'

interface PracticePageCard {
	key: string
	title: string
	description: string
	icon: string
	iconBg: string
	iconColor: string
}

interface PracticePageChip {
	key: string
	text: string
	icon: string
}

interface PracticePageCopy {
	title: string
	description: string
	sectionTitle: string
	noticeText: string
	heroChips: PracticePageChip[]
	cards: PracticePageCard[]
}

export default defineComponent({
	name: 'KyyyPracticePage',
	components: {
		KyyyFeatureShellPage
	},
	data() {
		return {
			entryMode: 'default' as KyyyPracticeEntryMode
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.applyEntryMode(query?.mode)
	},
	computed: {
		pageCopy(): PracticePageCopy {
			if (this.entryMode === 'study') {
				return {
					title: '英语学习',
					description: '从待学单词开始进入英语学习流程，后续会在这里继续接入新词计划、阅读铺垫和阶段任务。',
					sectionTitle: '学习方向',
					noticeText: '当前先把“学习入口”独立出来，后面会继续补单词学习、阅读铺垫和每日计划。',
					heroChips: [
						{
							key: 'study-mode',
							text: '待学单词入口',
							icon: 'paperplane-filled'
						},
						{
							key: 'study-flow',
							text: '新词优先推进',
							icon: 'calendar'
						}
					],
					cards: [
						{
							key: 'new-word',
							title: '新词学习',
							description: '后续接入每日新词、释义记忆与例句理解。',
							icon: 'paperplane-filled',
							iconBg: 'linear-gradient(135deg, rgba(219, 228, 247, 0.98), rgba(191, 205, 231, 0.95))',
							iconColor: '#4f5f79'
						},
						{
							key: 'reading',
							title: '阅读铺垫',
							description: '把新词放进阅读语境，逐步接入篇章训练。',
							icon: 'compose',
							iconBg: 'linear-gradient(135deg, rgba(243, 234, 218, 0.98), rgba(229, 214, 193, 0.95))',
							iconColor: '#8b6334'
						},
						{
							key: 'plan',
							title: '学习计划',
							description: '按考试方向与节奏分配每日学习量。',
							icon: 'calendar',
							iconBg: 'linear-gradient(135deg, rgba(225, 239, 232, 0.98), rgba(202, 224, 210, 0.95))',
							iconColor: '#466352'
						}
					]
				}
			}
			if (this.entryMode === 'review') {
				return {
					title: '英语复习',
					description: '从待复习单词回到英语复习流程，后续会接入到期提醒、错词追踪和阶段复盘。',
					sectionTitle: '复习方向',
					noticeText: '当前先把“复习入口”独立出来，后面会继续补到期复习、错词追踪和记忆巩固。',
					heroChips: [
						{
							key: 'review-mode',
							text: '待复习单词入口',
							icon: 'reload'
						},
						{
							key: 'review-flow',
							text: '到期单词优先回看',
							icon: 'checkbox-filled'
						}
					],
					cards: [
						{
							key: 'due-review',
							title: '到期复习',
							description: '优先回看已经到期的单词与表达。',
							icon: 'reload',
							iconBg: 'linear-gradient(135deg, rgba(219, 228, 247, 0.98), rgba(191, 205, 231, 0.95))',
							iconColor: '#4f5f79'
						},
						{
							key: 'wrong-word',
							title: '错词回看',
							description: '把记忆反复出错的词集中拉出来继续巩固。',
							icon: 'help-filled',
							iconBg: 'linear-gradient(135deg, rgba(243, 234, 218, 0.98), rgba(229, 214, 193, 0.95))',
							iconColor: '#8b6334'
						},
						{
							key: 'feedback',
							title: '阶段复盘',
							description: '后续把复习节奏、遗忘点和回顾建议收束到这里。',
							icon: 'refresh',
							iconBg: 'linear-gradient(135deg, rgba(225, 239, 232, 0.98), rgba(202, 224, 210, 0.95))',
							iconColor: '#466352'
						}
					]
				}
			}
			return {
				title: '英语练习',
				description: '练习模块先给词汇、阅读、复盘做统一落位，后续真题刷题、阶段训练和错题回看都可以继续接进来。',
				sectionTitle: '练习方向',
				noticeText: '这里会承接英语项目的日常训练入口。等题目结构和接口明确后，再按题型细分到独立页面即可。',
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
	},
	methods: {
		applyEntryMode(mode?: string): void {
			if (mode === 'study' || mode === 'review') {
				this.entryMode = mode
				return
			}
			this.entryMode = 'default'
		}
	}
})
</script>
