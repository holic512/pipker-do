// AI 索引: 小程序统一请求与上传封装。

import env from '@/shared/config/env'
import { getSessionSnapshot, reloginAfterUnauthorized } from '@/shared/session/session'
import {
	buildAntiCrawlerHeaders,
	buildRiskMessage,
	getActiveCooldown,
	resolveRiskPayload,
	saveRiskCooldown,
	type AntiCrawlerCooldownState,
	type AntiCrawlerResponsePayload
} from '@/shared/network/anti-crawler'

const GET_IN_FLIGHT_WINDOW = 500

type RequestMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'HEAD'

type ResponseLike = {
	statusCode: number
	data: unknown
	[key: string]: unknown
}

interface ErrorPayloadShape {
	retryAfterSeconds?: number
	ruleCode?: string | null
}

interface ApiEnvelope {
	code?: number
	message?: unknown
	data?: unknown
}

export interface RequestRuntimeError extends Error {
	code: string | number
	response: unknown
	data: unknown
	retryAfterSeconds: number
	ruleCode: string | null
}

export interface RequestOptions<TData = unknown> {
	url: string
	method?: RequestMethod
	data?: TData
	header?: Record<string, string>
	timeout?: number
	auth?: boolean
}

export interface UploadFileOptions<TFormData = Record<string, unknown>> {
	url: string
	filePath: string
	name?: string
	formData?: TFormData
	header?: Record<string, string>
	auth?: boolean
}

const inflightGetRequests = new Map<string, { createdAt: number; promise: Promise<unknown> }>()

function parseResponseBody(data: unknown): unknown {
	if (typeof data === 'string') {
		try {
			return JSON.parse(data)
		} catch (error) {
			return data
		}
	}
	return data
}

function resolveRetryAfterSeconds(data: ErrorPayloadShape | null): number {
	if (!data || data.retryAfterSeconds === undefined || data.retryAfterSeconds === null) {
		return 0
	}
	const retryAfterSeconds = Number(data.retryAfterSeconds)
	return Number.isFinite(retryAfterSeconds) ? retryAfterSeconds : 0
}

function createError(
	message: string,
	code: string | number,
	response: unknown,
	data: AntiCrawlerResponsePayload | AntiCrawlerCooldownState | null
): RequestRuntimeError {
	const error = new Error(message || '请求失败') as RequestRuntimeError
	error.code = code
	error.response = response
	error.data = data || null
	error.retryAfterSeconds = resolveRetryAfterSeconds(data)
	error.ruleCode = data && data.ruleCode ? data.ruleCode : null
	return error
}

function isManagedRequestError(error: unknown): error is RequestRuntimeError {
	return error instanceof Error && Object.prototype.hasOwnProperty.call(error as unknown as Record<string, unknown>, 'code')
}

function asApiEnvelope(payload: unknown): ApiEnvelope | null {
	if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
		return null
	}
	return payload as ApiEnvelope
}

function stableSerialize(value: unknown): string {
	if (value === null || value === undefined) {
		return ''
	}
	if (Array.isArray(value)) {
		return `[${value.map((item) => stableSerialize(item)).join(',')}]`
	}
	if (typeof value === 'object') {
		const objectValue = value as Record<string, unknown>
		return `{${Object.keys(objectValue).sort().map((key) => `${key}:${stableSerialize(objectValue[key])}`).join(',')}}`
	}
	return String(value)
}

function buildRequestKey(options: RequestOptions): string {
	const method = String(options.method || 'GET').toUpperCase()
	return `${method}::${options.url}::${stableSerialize(options.data)}`
}

async function sendRequest(options: RequestOptions): Promise<ResponseLike> {
	const activeCooldown = getActiveCooldown(options.url)
	if (activeCooldown) {
		throw createError(buildRiskMessage(activeCooldown), 429, null, activeCooldown)
	}
	const session = getSessionSnapshot()
	const header = buildAntiCrawlerHeaders(options.header || {})

	if (options.auth !== false && session.token) {
		header.Authorization = `Bearer ${session.token}`
	}

	return new Promise((resolve, reject) => {
		uni.request({
			url: `${env.apiBaseUrl}${options.url}`,
			method: options.method || 'GET',
			data: options.data,
			header,
			timeout: options.timeout || env.timeout,
			success: (response: ResponseLike) => resolve(response),
			fail: reject
		})
	})
}

async function executeRequest<T = unknown>(options: RequestOptions, hasRetried = false): Promise<T> {
	const requestExecutor = () => executeRequestInternal<T>(options, hasRetried)
	if (String(options.method || 'GET').toUpperCase() !== 'GET') {
		return requestExecutor()
	}
	const requestKey = buildRequestKey(options)
	const currentEntry = inflightGetRequests.get(requestKey)
	if (currentEntry && Date.now() - currentEntry.createdAt <= GET_IN_FLIGHT_WINDOW) {
		return currentEntry.promise as Promise<T>
	}
	const promise = requestExecutor().finally(() => {
		if (inflightGetRequests.get(requestKey)?.promise === promise) {
			inflightGetRequests.delete(requestKey)
		}
	})
	inflightGetRequests.set(requestKey, {
		createdAt: Date.now(),
		promise
	})
	return promise as Promise<T>
}

async function executeRequestInternal<T = unknown>(options: RequestOptions, hasRetried = false): Promise<T> {
	let response
	try {
		response = await sendRequest(options)
	} catch (error) {
		if (isManagedRequestError(error)) {
			throw error
		}
		throw createError('网络异常，请检查服务是否可用', 'NETWORK_ERROR', error, null)
	}

	const payload = parseResponseBody(response.data)
	const apiEnvelope = asApiEnvelope(payload)
	const businessCode = apiEnvelope?.code ?? null
	const riskPayload = resolveRiskPayload(response.statusCode, payload)
	const isUnauthorized = response.statusCode === 401 || businessCode === 401

	if (riskPayload) {
		saveRiskCooldown(options.url, riskPayload)
		throw createError(
			String(apiEnvelope?.message || '') || buildRiskMessage(riskPayload),
			businessCode || response.statusCode,
			response,
			riskPayload
		)
	}

	if (isUnauthorized && options.auth !== false && !hasRetried) {
		try {
			await reloginAfterUnauthorized()
			return executeRequestInternal<T>(options, true)
		} catch (error) {
			throw createError('登录已失效，请稍后重试', 401, response, null)
		}
	}

	if (response.statusCode < 200 || response.statusCode >= 300) {
		throw createError(
			String(apiEnvelope?.message || '') || '请求失败',
			response.statusCode,
			response,
			null
		)
	}

	if (apiEnvelope && Object.prototype.hasOwnProperty.call(apiEnvelope, 'code')) {
		if (apiEnvelope.code !== 0) {
			throw createError(String(apiEnvelope.message || '请求失败'), apiEnvelope.code ?? 'REQUEST_FAILED', response, null)
		}
		return apiEnvelope.data as T
	}

	return payload as T
}

export default function request<T = unknown>(options: RequestOptions): Promise<T> {
	return executeRequest<T>(options)
}

export async function uploadFile<T = unknown>(options: UploadFileOptions): Promise<T> {
	const doUpload = (): Promise<ResponseLike> => {
		const activeCooldown = getActiveCooldown(options.url)
		if (activeCooldown) {
			return Promise.reject(createError(buildRiskMessage(activeCooldown), 429, null, activeCooldown))
		}
		const session = getSessionSnapshot()
		const header = buildAntiCrawlerHeaders(options.header || {})

		if (options.auth !== false && session.token) {
			header.Authorization = `Bearer ${session.token}`
		}

		return new Promise((resolve, reject) => {
			uni.uploadFile({
				url: `${env.apiBaseUrl}${options.url}`,
				filePath: options.filePath,
				name: options.name || 'file',
				formData: options.formData || {},
				header,
				success: (uploadResponse: ResponseLike) => resolve(uploadResponse),
				fail: reject
			})
		})
	}

	let response: ResponseLike
	try {
		response = await doUpload()
	} catch (error) {
		if (isManagedRequestError(error)) {
			throw error
		}
		throw createError('上传失败，请检查网络连接', 'UPLOAD_NETWORK_ERROR', error, null)
	}

	const payload = parseResponseBody(response.data)
	const uploadEnvelope = asApiEnvelope(payload)
	const riskPayload = resolveRiskPayload(response.statusCode, payload)
	if (riskPayload) {
		saveRiskCooldown(options.url, riskPayload)
		throw createError(
			String(uploadEnvelope?.message || '') || buildRiskMessage(riskPayload),
			uploadEnvelope?.code || response.statusCode,
			response,
			riskPayload
		)
	}
	if ((response.statusCode === 401 || uploadEnvelope?.code === 401) && options.auth !== false) {
		await reloginAfterUnauthorized()
		response = await doUpload()
	}

	const finalPayload = parseResponseBody(response.data)
	const finalEnvelope = asApiEnvelope(finalPayload)
	const finalRiskPayload = resolveRiskPayload(response.statusCode, finalPayload)
	if (finalRiskPayload) {
		saveRiskCooldown(options.url, finalRiskPayload)
		throw createError(
			String(finalEnvelope?.message || '') || buildRiskMessage(finalRiskPayload),
			finalEnvelope?.code || response.statusCode,
			response,
			finalRiskPayload
		)
	}
	if (response.statusCode < 200 || response.statusCode >= 300 || !finalEnvelope || finalEnvelope.code !== 0) {
		throw createError(
			String(finalEnvelope?.message || '') || '上传失败',
			response.statusCode || finalEnvelope?.code || 'UPLOAD_FAILED',
			response,
			null
		)
	}
	return finalEnvelope.data as T
}
