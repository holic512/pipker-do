// AI 索引: 小程序登录态管理与静默登录。

import env from '@/shared/config/env'
import { clearCachedUser, clearToken, getCachedUser, getToken, setCachedUser, setToken } from '@/shared/auth/storage'
import {
	buildAntiCrawlerHeaders,
	buildRiskMessage,
	getActiveCooldown,
	resolveRiskPayload,
	saveRiskCooldown,
	type AntiCrawlerCooldownState,
	type AntiCrawlerResponsePayload
} from '@/shared/network/anti-crawler'

interface SessionState {
	token: string
	currentUser: unknown | null
	bootstrappingPromise: Promise<unknown> | null
}

type ResponseLike = {
	statusCode: number
	data: unknown
}

export interface SessionSnapshot<TUser = unknown> {
	token: string
	currentUser: TUser | null
	isAuthenticated: boolean
}

export interface BootstrapAuthOptions {
	force?: boolean
	silent?: boolean
}

interface SessionRequestError extends Error {
	code: string | number
	response: unknown
	data: unknown
}

interface ApiEnvelope {
	code?: number
	message?: unknown
	data?: unknown
}

type SessionListener = (snapshot: SessionSnapshot) => void

const listeners = new Set<SessionListener>()

const state: SessionState = {
	token: getToken(),
	currentUser: getCachedUser(),
	bootstrappingPromise: null
}

function notify(): void {
	const snapshot = getSessionSnapshot()
	listeners.forEach((listener) => {
		try {
			listener(snapshot)
		} catch (error) {
			console.warn('[session] listener error', error)
		}
	})
}

function createRequestError(
	message: string,
	code: string | number,
	response: unknown,
	data: AntiCrawlerResponsePayload | AntiCrawlerCooldownState | null
): SessionRequestError {
	const error = new Error(message || '请求失败') as SessionRequestError
	error.code = code
	error.response = response
	error.data = data || null
	return error
}

function requestByUni(options: {
	url: string
	method?: string
	header?: Record<string, string>
	data?: unknown
}): Promise<ResponseLike> {
	const activeCooldown = getActiveCooldown(options.url)
	if (activeCooldown) {
		return Promise.reject(createRequestError(buildRiskMessage(activeCooldown), 429, null, activeCooldown))
	}
	return new Promise((resolve, reject) => {
		uni.request({
			timeout: env.timeout,
			...options,
			header: buildAntiCrawlerHeaders(options.header || {}),
			success: (response: ResponseLike) => resolve(response),
			fail: (error: unknown) => reject(error)
		})
	})
}

function parseBody(data: unknown): unknown {
	if (typeof data === 'string') {
		try {
			return JSON.parse(data)
		} catch (error) {
			return data
		}
	}
	return data
}

function asApiEnvelope(payload: unknown): ApiEnvelope | null {
	if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
		return null
	}
	return payload as ApiEnvelope
}

function resolveMessage(payload: unknown, fallback: string): string {
	const apiEnvelope = asApiEnvelope(payload)
	if (apiEnvelope?.message) {
		return String(apiEnvelope.message)
	}
	return fallback
}

function setSession(token: string, user: unknown): void {
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

export function getSessionSnapshot<TUser = unknown>(): SessionSnapshot<TUser> {
	return {
		token: state.token,
		currentUser: state.currentUser as TUser | null,
		isAuthenticated: !!state.token
	}
}

export function subscribeSession(listener: SessionListener): () => boolean {
	listeners.add(listener)
	return () => listeners.delete(listener)
}

export function clearSession(options: { notify?: boolean } = {}): void {
	state.token = ''
	state.currentUser = null
	clearToken()
	clearCachedUser()
	if (options.notify !== false) {
		notify()
	}
}

export function setCurrentUser(user: unknown): void {
	setSession(state.token, user)
}

export async function fetchCurrentUser<TUser = unknown>(): Promise<TUser> {
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
	const apiEnvelope = asApiEnvelope(payload)
	const riskPayload = resolveRiskPayload(response.statusCode, payload)
	if (riskPayload) {
		saveRiskCooldown('/api/user/me', riskPayload)
		throw createRequestError(resolveMessage(payload, buildRiskMessage(riskPayload)), apiEnvelope?.code || response.statusCode, response, riskPayload)
	}
	if (response.statusCode === 401 || !apiEnvelope || apiEnvelope.code === 401) {
		clearSession({ notify: false })
		throw new Error(resolveMessage(payload, '登录态已失效'))
	}
	if (response.statusCode < 200 || response.statusCode >= 300 || apiEnvelope.code !== 0) {
		throw new Error(resolveMessage(payload, '获取用户信息失败'))
	}

	setSession(state.token, apiEnvelope.data)
	return apiEnvelope.data as TUser
}

function uniLogin(): Promise<string> {
	return new Promise((resolve, reject) => {
		uni.login({
			provider: 'weixin',
			success: (result: { code?: string }) => {
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

async function performWechatSilentLogin<TUser = unknown>(): Promise<TUser> {
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
	const apiEnvelope = asApiEnvelope(payload)
	const riskPayload = resolveRiskPayload(response.statusCode, payload)
	if (riskPayload) {
		saveRiskCooldown(env.loginUrl, riskPayload)
		throw createRequestError(resolveMessage(payload, buildRiskMessage(riskPayload)), apiEnvelope?.code || response.statusCode, response, riskPayload)
	}
	if (response.statusCode < 200 || response.statusCode >= 300 || !apiEnvelope || apiEnvelope.code !== 0) {
		throw new Error(resolveMessage(payload, '静默登录失败'))
	}
	const loginData = (apiEnvelope.data || {}) as { token?: string; user?: unknown }
	setSession(loginData.token || '', loginData.user)
	return loginData.user as TUser
}

export async function bootstrapAuth<TUser = unknown>(options: BootstrapAuthOptions = {}): Promise<TUser> {
	if (state.bootstrappingPromise && !options.force) {
		return state.bootstrappingPromise as Promise<TUser>
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
		return performWechatSilentLogin<TUser>()
	})()
		.catch((error: unknown) => {
			if (!options.silent) {
				const errorMessage = error instanceof Error ? error.message : '登录失败'
				uni.showToast({
					title: errorMessage || '登录失败',
					icon: 'none'
				})
			}
			throw error
		})
		.finally(() => {
			state.bootstrappingPromise = null
		})

	return state.bootstrappingPromise as Promise<TUser>
}

export async function reloginAfterUnauthorized<TUser = unknown>(): Promise<TUser> {
	clearSession({ notify: false })
	return bootstrapAuth<TUser>({ force: true, silent: true })
}
