// AI 索引: 小程序登录态本地存储。

const TOKEN_KEY = 'PIPKER_TOKEN'
const USER_KEY = 'PIPKER_CURRENT_USER'

export function getToken(): string {
	return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
	uni.setStorageSync(TOKEN_KEY, token || '')
}

export function clearToken(): void {
	uni.removeStorageSync(TOKEN_KEY)
}

export function getCachedUser<TUser = unknown>(): TUser | null {
	const cachedUser = uni.getStorageSync(USER_KEY)
	return cachedUser ? (cachedUser as TUser) : null
}

export function setCachedUser(user: unknown): void {
	if (user) {
		uni.setStorageSync(USER_KEY, user)
		return
	}
	uni.removeStorageSync(USER_KEY)
}

export function clearCachedUser(): void {
	uni.removeStorageSync(USER_KEY)
}
