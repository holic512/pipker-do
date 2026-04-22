const TOKEN_KEY = 'PIPKER_TOKEN'
const USER_KEY = 'PIPKER_CURRENT_USER'

export function getToken() {
	return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
	uni.setStorageSync(TOKEN_KEY, token || '')
}

export function clearToken() {
	uni.removeStorageSync(TOKEN_KEY)
}

export function getCachedUser() {
	return uni.getStorageSync(USER_KEY) || null
}

export function setCachedUser(user) {
	if (user) {
		uni.setStorageSync(USER_KEY, user)
		return
	}
	uni.removeStorageSync(USER_KEY)
}

export function clearCachedUser() {
	uni.removeStorageSync(USER_KEY)
}
