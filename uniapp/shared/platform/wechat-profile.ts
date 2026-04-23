// AI 索引: 小程序微信原生资料能力适配。

export interface WechatProfileEvent {
	detail?: {
		avatarUrl?: string
		tempFilePath?: string
		value?: string
	} | null
}

export function supportsWechatNativeProfile(): boolean {
	// #ifdef MP-WEIXIN
	return true
	// #endif

	// #ifndef MP-WEIXIN
	return false
	// #endif
}

export function resolveWechatAvatarPath(event: WechatProfileEvent | null | undefined): string {
	if (!event || !event.detail) {
		return ''
	}
	return event.detail.avatarUrl || event.detail.tempFilePath || ''
}

export function resolveWechatNickname(event: WechatProfileEvent | null | undefined): string {
	if (!event || !event.detail) {
		return ''
	}
	return (event.detail.value || '').trim()
}
