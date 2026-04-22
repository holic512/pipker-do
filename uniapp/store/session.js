import env from '@/config/env'
import { clearCachedUser, clearToken, getCachedUser, getToken, setCachedUser, setToken } from '@/utils/auth'

const listeners = new Set()

const state = {
	token: getToken(),
	currentUser: getCachedUser(),
	bootstrappingPromise: null
}

function notify() {
	const snapshot = getSessionSnapshot()
	listeners.forEach((listener) => {
		try {
			listener(snapshot)
		} catch (error) {
			console.warn('[session] listener error', error)
		}
	})
}

function requestByUni(options) {
	return new Promise((resolve, reject) => {
		uni.request({
			timeout: env.timeout,
			...options,
			success: (response) => resolve(response),
			fail: (error) => reject(error)
		})
	})
}

function parseBody(data) {
	if (typeof data === 'string') {
		try {
			return JSON.parse(data)
		} catch (error) {
			return data
		}
	}
	return data
}

function resolveMessage(payload, fallback) {
	if (payload && typeof payload === 'object' && payload.message) {
		return payload.message
	}
	return fallback
}

function setSession(token, user) {
	state.token = token || ''
	state.currentUser = user || null
	if (state.token) {
		setToken(state.token)
	} else {
		clearToken()
	}
	setCachedUser(state.currentUser)
	notify()
}

export function getSessionSnapshot() {
	return {
		token: state.token,
		currentUser: state.currentUser,
		isAuthenticated: !!state.token
	}
}

export function subscribeSession(listener) {
	listeners.add(listener)
	return () => listeners.delete(listener)
}

export function clearSession(options = {}) {
	state.token = ''
	state.currentUser = null
	clearToken()
	clearCachedUser()
	if (options.notify !== false) {
		notify()
	}
}

export function setCurrentUser(user) {
	setSession(state.token, user)
}

export async function fetchCurrentUser() {
	if (!state.token) {
		throw new Error('missing token')
	}

	const response = await requestByUni({
		url: `${env.apiBaseUrl}/api/user/me`,
		method: 'GET',
		header: {
			Authorization: `Bearer ${state.token}`
		}
	})

	const payload = parseBody(response.data)
	if (response.statusCode === 401 || !payload || payload.code === 401) {
		clearSession({ notify: false })
		throw new Error(resolveMessage(payload, '登录态已失效'))
	}
	if (response.statusCode < 200 || response.statusCode >= 300 || payload.code !== 0) {
		throw new Error(resolveMessage(payload, '获取用户信息失败'))
	}

	setSession(state.token, payload.data)
	return payload.data
}

function uniLogin() {
	return new Promise((resolve, reject) => {
		uni.login({
			provider: 'weixin',
			success: (result) => {
				if (!result.code) {
					reject(new Error('未获取到微信登录 code'))
					return
				}
				resolve(result.code)
			},
			fail: reject
		})
	})
}

async function performWechatSilentLogin() {
	const code = await uniLogin()
	const response = await requestByUni({
		url: env.loginUrl,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data: { code }
	})
	const payload = parseBody(response.data)
	if (response.statusCode < 200 || response.statusCode >= 300 || !payload || payload.code !== 0) {
		throw new Error(resolveMessage(payload, '静默登录失败'))
	}
	setSession(payload.data.token, payload.data.user)
	return payload.data.user
}

export async function bootstrapAuth(options = {}) {
	if (state.bootstrappingPromise && !options.force) {
		return state.bootstrappingPromise
	}

	state.bootstrappingPromise = (async () => {
		if (state.token && !options.force) {
			try {
				return await fetchCurrentUser()
			} catch (error) {
				console.warn('[session] refresh current user failed', error)
			}
		}

		clearSession({ notify: false })
		return performWechatSilentLogin()
	})()
		.catch((error) => {
			if (!options.silent) {
				uni.showToast({
					title: error.message || '登录失败',
					icon: 'none'
				})
			}
			throw error
		})
		.finally(() => {
			state.bootstrappingPromise = null
		})

	return state.bootstrappingPromise
}

export async function reloginAfterUnauthorized() {
	clearSession({ notify: false })
	return bootstrapAuth({ force: true, silent: true })
}
