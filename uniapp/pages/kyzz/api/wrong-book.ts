import request from '@/shared/network/request'
import type { KyzzWrongQuestionResponse, KyzzWrongQuestionStatus } from '@/pages/kyzz/wrong-book/types'

// AI 索引: KYZZ 小程序错题本 API。

type RequestParamValue = string | number | boolean | null | undefined

export interface KyzzWrongQuestionQuery {
	status?: KyzzWrongQuestionStatus
	keyword?: string
}

function compactQuery(params: KyzzWrongQuestionQuery): Partial<KyzzWrongQuestionQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyzzWrongQuestionQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null && value !== '') {
			query[key] = value
		}
	})
	return query as Partial<KyzzWrongQuestionQuery>
}

export function getWrongQuestions(params: KyzzWrongQuestionQuery = {}): Promise<KyzzWrongQuestionResponse> {
	return request({
		url: '/api/kyzz/wrong-questions',
		method: 'GET',
		data: compactQuery(params)
	})
}
