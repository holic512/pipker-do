import request from '@/shared/network/request'
import type {
	KyzzFavoriteQuestionResponse,
	KyzzFavoriteQuestionToggleResponse
} from '@/pages/kyzz/favorite/types'

// AI 索引: KYZZ 小程序收藏题目 API。

type RequestParamValue = string | number | boolean | null | undefined

export interface KyzzFavoriteQuestionQuery {
	keyword?: string
}

function compactQuery(params: KyzzFavoriteQuestionQuery): Partial<KyzzFavoriteQuestionQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyzzFavoriteQuestionQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null && value !== '') {
			query[key] = value
		}
	})
	return query as Partial<KyzzFavoriteQuestionQuery>
}

export function getFavoriteQuestions(params: KyzzFavoriteQuestionQuery = {}): Promise<KyzzFavoriteQuestionResponse> {
	return request({
		url: '/api/kyzz/favorite-questions',
		method: 'GET',
		data: compactQuery(params)
	})
}

export function favoriteQuestion(questionId: number): Promise<KyzzFavoriteQuestionToggleResponse> {
	return request({
		url: `/api/kyzz/favorite-questions/${questionId}`,
		method: 'PUT'
	})
}

export function unfavoriteQuestion(questionId: number): Promise<KyzzFavoriteQuestionToggleResponse> {
	return request({
		url: `/api/kyzz/favorite-questions/${questionId}`,
		method: 'DELETE'
	})
}
