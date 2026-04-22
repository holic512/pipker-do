import env from '@/shared/config/env'
import { getSessionSnapshot, reloginAfterUnauthorized } from '@/shared/session/session'

function parseResponseBody(data) {
	if (typeof data === 'string') {
		try {
			return JSON.parse(data)
		} catch (error) {
			return data
		}
	}
	return data
}

function createError(message, code, response) {
	const error = new Error(message || '请求失败')
	error.code = code
	error.response = response
	return error
}

async function sendRequest(options) {
	const session = getSessionSnapshot()
	const header = {
		...(options.header || {})
	}

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
			success: resolve,
			fail: reject
		})
	})
}

async function executeRequest(options, hasRetried = false) {
	let response
	try {
		response = await sendRequest(options)
	} catch (error) {
		throw createError('网络异常，请检查服务是否可用', 'NETWORK_ERROR', error)
	}

	const payload = parseResponseBody(response.data)
	const businessCode = payload && typeof payload === 'object' ? payload.code : null
	const isUnauthorized = response.statusCode === 401 || businessCode === 401

	if (isUnauthorized && options.auth !== false && !hasRetried) {
		try {
			await reloginAfterUnauthorized()
			return executeRequest(options, true)
		} catch (error) {
			throw createError('登录已失效，请稍后重试', 401, response)
		}
	}

	if (response.statusCode < 200 || response.statusCode >= 300) {
		throw createError((payload && payload.message) || '请求失败', response.statusCode, response)
	}

	if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
		if (payload.code !== 0) {
			throw createError(payload.message || '请求失败', payload.code, response)
		}
		return payload.data
	}

	return payload
}

export default function request(options) {
	return executeRequest(options)
}

export async function uploadFile(options) {
	const doUpload = () => {
		const session = getSessionSnapshot()
		const header = {
			...(options.header || {})
		}

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
			success: resolve,
			fail: reject
		})
		})
	}

	let response
	try {
		response = await doUpload()
	} catch (error) {
		throw createError('上传失败，请检查网络连接', 'UPLOAD_NETWORK_ERROR', error)
	}

	const payload = parseResponseBody(response.data)
	if ((response.statusCode === 401 || (payload && payload.code === 401)) && options.auth !== false) {
		await reloginAfterUnauthorized()
		response = await doUpload()
	}

	const finalPayload = parseResponseBody(response.data)
	if (response.statusCode < 200 || response.statusCode >= 300 || !finalPayload || finalPayload.code !== 0) {
		throw createError((finalPayload && finalPayload.message) || '上传失败', response.statusCode || finalPayload.code, response)
	}
	return finalPayload.data
}
