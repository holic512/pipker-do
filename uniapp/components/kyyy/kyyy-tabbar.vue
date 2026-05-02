<template>
	<!-- AI 索引: KYYY 小程序底部导航。 -->
	<project-tabbar
		:current="current"
		:items="mergedItems"
		@select="handleTabClick"
	/>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import ProjectTabbar from '@/components/project-tabbar/project-tabbar.vue'
import { createKyyyTabbarItems, type KyyyTabbarItem } from '@/pages/kyyy/navigation/tabbar'

export default defineComponent({
	name: 'KyyyTabbar',
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
		mergedItems(): KyyyTabbarItem[] {
			return createKyyyTabbarItems().map((item) => ({
				...item,
				size: item.size || 24,
				activeColor: item.activeColor || '#46536a',
				color: item.color || '#6b7884'
			}))
		}
	},
	methods: {
		handleTabClick(item: KyyyTabbarItem): void {
			if (!item || !item.pagePath) {
				return
			}

			if (item.navigationType === 'switchTab') {
				uni.switchTab({
					url: item.pagePath,
					fail: (error) => {
						console.warn('[kyyy-tabbar] switchTab failed.', error)
					}
				})
				return
			}

			if (item.navigationType === 'navigateTo') {
				uni.navigateTo({
					url: item.pagePath,
					fail: (error) => {
						console.warn('[kyyy-tabbar] navigateTo failed.', error)
					}
				})
				return
			}

			uni.reLaunch({
				url: item.pagePath,
				fail: (error) => {
					console.warn('[kyyy-tabbar] reLaunch failed.', error)
				}
			})
		}
	}
})
</script>
