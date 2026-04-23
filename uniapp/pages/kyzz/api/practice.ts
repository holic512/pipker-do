import request from '@/shared/network/request'
import type {
	KyzzPracticeDashboardResponse,
	KyzzPracticeReviewRequest,
	KyzzPracticeReviewResponse,
	KyzzPracticeSelfJudgementRequest,
	KyzzPracticeSessionQuery,
	KyzzPracticeSessionResponse
} from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序刷题业务 API。

type QueryValue = string | number | boolean | null | undefined

function compactQuery(query: KyzzPracticeSessionQuery): Partial<KyzzPracticeSessionQuery> {
	const result: Record<string, QueryValue> = {}
	;(Object.keys(query) as Array<keyof KyzzPracticeSessionQuery>).forEach((key) => {
		const value = query[key]
		if (value !== undefined && value !== null) {
			result[key] = value
		}
	})
	return result as Partial<KyzzPracticeSessionQuery>
}

export function getPracticeDashboard(): Promise<KyzzPracticeDashboardResponse> {
	return request({
		url: '/api/kyzz/practice/dashboard',
		method: 'GET'
	})
}

export function getPracticeSession(params: KyzzPracticeSessionQuery = {}): Promise<KyzzPracticeSessionResponse> {
	return request({
		url: '/api/kyzz/practice/session',
		method: 'GET',
		data: compactQuery(params)
	})
}

export function reviewPracticeQuestion(questionId: number, data: KyzzPracticeReviewRequest): Promise<KyzzPracticeReviewResponse> {
	return request({
		url: `/api/kyzz/practice/questions/${questionId}/review`,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function selfJudgePracticeQuestion(questionId: number, data: KyzzPracticeSelfJudgementRequest): Promise<KyzzPracticeReviewResponse> {
	return request({
		url: `/api/kyzz/practice/questions/${questionId}/self-judgement`,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}
