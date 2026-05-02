<template>
	<!-- AI 索引: KYYY 我的页，复用公共我的页且专项插槽留空。 -->
	<mine-page
		ref="minePage"
		profile-url="/pages/kyyy/profile/edit"
	>
		<template #tabbar>
			<kyyy-tabbar current="mine" />
		</template>
	</mine-page>
</template>

<script>
import MinePage from '@/components/account/mine-page.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'

export default {
	name: 'KyyyMinePage',
	components: {
		MinePage,
		KyyyTabbar
	},
	onShow() {
		const minePage = this.$refs.minePage
		if (minePage && typeof minePage.refreshOnShow === 'function') {
			minePage.refreshOnShow()
		}
	}
}
</script>
