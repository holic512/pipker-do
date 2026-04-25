// AI 索引: 小程序用户协议本地接受状态缓存，减少启动时重复依赖后端协议状态判断。

export const CURRENT_AGREEMENT_VERSION = '2026-04-25'

const STORAGE_KEY = 'APP_USER_AGREEMENT_ACCEPTANCE'

interface AgreementAcceptanceCache {
	version: string
	acceptedAt: string
	userId: string
}

function isRecord(value: unknown): value is Record<string, unknown> {
	return !!value && typeof value === 'object' && !Array.isArray(value)
}

export function resolveAgreementUserId(user: unknown): string {
	if (!isRecord(user)) {
		return ''
	}
	const id = user.id
	return id === undefined || id === null ? '' : String(id)
}

export function hasCachedAgreementAcceptance(user: unknown): boolean {
	const userId = resolveAgreementUserId(user)
	if (!userId) {
		return false
	}
	const cache = uni.getStorageSync(STORAGE_KEY)
	return isRecord(cache)
		&& cache.version === CURRENT_AGREEMENT_VERSION
		&& cache.userId === userId
		&& typeof cache.acceptedAt === 'string'
		&& !!cache.acceptedAt
}

export function cacheAgreementAcceptance(user: unknown, acceptedAt?: string): void {
	const userId = resolveAgreementUserId(user)
	if (!userId) {
		return
	}
	const cache: AgreementAcceptanceCache = {
		version: CURRENT_AGREEMENT_VERSION,
		acceptedAt: acceptedAt || new Date().toISOString(),
		userId
	}
	uni.setStorageSync(STORAGE_KEY, cache)
}

export function clearAgreementAcceptanceCache(): void {
	uni.removeStorageSync(STORAGE_KEY)
}
