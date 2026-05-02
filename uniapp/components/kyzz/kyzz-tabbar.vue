<template>
	<!-- AI 索引: KYZZ 小程序底部导航。 -->
	<project-tabbar
		:current="current"
		:items="mergedItems"
		@select="handleTabClick"
	/>
</template>

<script>
import ProjectTabbar from '@/components/project-tabbar/project-tabbar.vue'
import { createKyzzTabbarItems } from '@/pages/kyzz/navigation/tabbar'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'

export default {
	name: 'KyzzTabbar',
	components: {
		ProjectTabbar
	},
	props: {
		current: {
			type: String,
			default: ''
		}
	},
	computed: {
		mergedItems() {
			return createKyzzTabbarItems().map((item) => ({
				...item,
				size: item.size || 24,
				activeColor: item.activeColor || '#46536a',
				color: item.color || '#6b7884'
			}))
		}
	},
	methods: {
		handleTabClick(item) {
			if (!item || !item.pagePath) {
				return
			}

			if (item.key === 'practice') {
				openPracticeTab().catch((error) => {
					console.warn('[kyzz-tabbar] switch practice tab failed.', error)
				})
				return
			}

			uni.switchTab({
				url: item.pagePath,
				fail: (error) => {
					console.warn('[kyzz-tabbar] switchTab failed, check pages.json tabBar config.', error)
				}
			})
		}
	}
}
</script>
