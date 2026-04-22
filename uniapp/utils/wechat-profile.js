export function supportsWechatNativeProfile() {
	// #ifdef MP-WEIXIN
	return true
	// #endif

	// #ifndef MP-WEIXIN
	return false
	// #endif
}

export function resolveWechatAvatarPath(event) {
	if (!event || !event.detail) {
		return ''
	}
	return event.detail.avatarUrl || event.detail.tempFilePath || ''
}

export function resolveWechatNickname(event) {
	if (!event || !event.detail) {
		return ''
	}
	return (event.detail.value || '').trim()
}
