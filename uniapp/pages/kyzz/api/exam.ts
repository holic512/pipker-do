// AI 索引: KYZZ 小程序 VIP 考试业务 API。

import request from '@/shared/network/request'
import type {
	KyzzExamAnswerSaveRequest,
	KyzzExamAnswerSaveResponse,
	KyzzExamDetailResponse,
	KyzzExamEntryResponse,
	KyzzExamStartRequest,
	KyzzExamSummary
} from '@/pages/kyzz/exam/types'

export function getExamEntry(): Promise<KyzzExamEntryResponse> {
	return request<KyzzExamEntryResponse>({
		url: '/api/kyzz/exam/entry',
		method: 'GET'
	})
}

export function getExamHistory(limit = 20): Promise<KyzzExamSummary[]> {
	return request<KyzzExamSummary[]>({
		url: '/api/kyzz/exam/history',
		method: 'GET',
		data: {
			limit
		}
	})
}

export function startExam(data: KyzzExamStartRequest): Promise<KyzzExamDetailResponse> {
	return request<KyzzExamDetailResponse>({
		url: '/api/kyzz/exam/sessions',
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function getExamDetail(sessionId: number): Promise<KyzzExamDetailResponse> {
	return request<KyzzExamDetailResponse>({
		url: `/api/kyzz/exam/sessions/${sessionId}`,
		method: 'GET'
	})
}

export function saveExamAnswer(
	sessionId: number,
	questionId: number,
	data: KyzzExamAnswerSaveRequest
): Promise<KyzzExamAnswerSaveResponse> {
	return request<KyzzExamAnswerSaveResponse>({
		url: `/api/kyzz/exam/sessions/${sessionId}/questions/${questionId}/answer`,
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function submitExam(sessionId: number): Promise<KyzzExamSummary> {
	return request<KyzzExamSummary>({
		url: `/api/kyzz/exam/sessions/${sessionId}/submit`,
		method: 'POST'
	})
}

export function retryExamGrading(sessionId: number): Promise<KyzzExamSummary> {
	return request<KyzzExamSummary>({
		url: `/api/kyzz/exam/sessions/${sessionId}/grading/retry`,
		method: 'POST'
	})
}
