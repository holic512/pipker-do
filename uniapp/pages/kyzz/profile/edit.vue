<template>
	<profile-editor-form ref="profileEditorForm" />
</template>

<script>
import ProfileEditorForm from '@/components/account/profile-editor-form.vue'

export default {
	name: 'KyzzProfileEditPage',
	components: {
		ProfileEditorForm
	},
	onLoad() {
		this.refreshProfileForm()
	},
	onShow() {
		this.refreshProfileForm()
	},
	methods: {
		refreshProfileForm() {
			this.$nextTick(() => {
				const form = this.$refs.profileEditorForm
				if (form && typeof form.refreshProfileForm === 'function') {
					form.refreshProfileForm()
				}
			})
		}
	}
}
</script>
