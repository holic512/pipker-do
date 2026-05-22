/**
 * @file KyyyReadingApi
 * @project pipker-do
 * @module 考研英语 / 小程序阅读做题
 * @description 封装阅读会话整篇加载、单题存稿与交卷提交请求。
 * @logic 1. 请求当前阅读会话；2. 保存单题答案草稿；3. 提交整篇并回显结果。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/reading/types
 * @index_tags 考研英语, 阅读接口, 整篇作答, 交卷提交
 * @author holic512
 */
import request from '@/shared/network/request'
import type {
	KyyyReadingAnswerDraftRequest,
	KyyyReadingAnnotationCreateRequest,
	KyyyReadingAnnotationResponse,
	KyyyReadingAnnotationUpdateRequest,
	KyyyReadingSessionResponse
} from '@/pages/kyyy/reading/types'

type QueryValue = string | number | boolean | null | undefined

function compactQuery(query: Record<string, QueryValue>): Record<string, string | number | boolean> {
	const result: Record<string, string | number | boolean> = {}
	Object.keys(query).forEach((key) => {
		const value = query[key]
		if (value !== undefined && value !== null) {
			result[key] = value
		}
	})
	return result
}

export function getReadingSession(passageId?: number | null, freshAttempt = false): Promise<KyyyReadingSessionResponse> {
	return request({
		url: '/api/kyyy/reading/session',
		method: 'GET',
		data: compactQuery({
			passageId,
			freshAttempt: freshAttempt ? 'true' : 'false'
		})
	})
}

export function saveReadingAnswer(
	sessionId: number,
	questionId: number,
	data: KyyyReadingAnswerDraftRequest
): Promise<KyyyReadingSessionResponse> {
	return request({
		url: `/api/kyyy/reading/session/${sessionId}/answers/${questionId}`,
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function submitReadingSession(sessionId: number): Promise<KyyyReadingSessionResponse> {
	return request({
		url: `/api/kyyy/reading/session/${sessionId}/submit`,
		method: 'POST'
	})
}

export function createReadingAnnotation(data: KyyyReadingAnnotationCreateRequest): Promise<KyyyReadingAnnotationResponse> {
	return request({
		url: '/api/kyyy/reading/annotations',
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function updateReadingAnnotation(
	annotationId: number,
	data: KyyyReadingAnnotationUpdateRequest
): Promise<KyyyReadingAnnotationResponse> {
	return request({
		url: `/api/kyyy/reading/annotations/${annotationId}`,
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function deleteReadingAnnotation(annotationId: number): Promise<void> {
	return request({
		url: `/api/kyyy/reading/annotations/${annotationId}`,
		method: 'DELETE'
	})
}
