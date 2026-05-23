/**
 * @file KyyyFavoriteWordApi
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 封装用户单词收藏列表、收藏与取消收藏请求。
 * @logic 1. 查询单词收藏列表；2. 收藏单词；3. 取消收藏并复用统一请求层鉴权。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/favorite-word/types
 * @index_tags 考研英语, 单词收藏, 收藏接口, 小程序API
 * @author holic512
 */
import request from '@/shared/network/request'
import type {
	KyyyFavoriteWordResponse,
	KyyyFavoriteWordToggleResponse
} from '@/pages/kyyy/favorite-word/types'

type RequestParamValue = string | number | boolean | null | undefined

export interface KyyyFavoriteWordQuery {
	keyword?: string
}

function compactQuery(params: KyyyFavoriteWordQuery): Partial<KyyyFavoriteWordQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyyyFavoriteWordQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null && value !== '') {
			query[key] = value
		}
	})
	return query as Partial<KyyyFavoriteWordQuery>
}

export function getFavoriteWords(params: KyyyFavoriteWordQuery = {}): Promise<KyyyFavoriteWordResponse> {
	return request({
		url: '/api/kyyy/favorite-words',
		method: 'GET',
		data: compactQuery(params)
	})
}

export function favoriteWord(wordId: number): Promise<KyyyFavoriteWordToggleResponse> {
	return request({
		url: `/api/kyyy/favorite-words/${wordId}`,
		method: 'PUT'
	})
}

export function unfavoriteWord(wordId: number): Promise<KyyyFavoriteWordToggleResponse> {
	return request({
		url: `/api/kyyy/favorite-words/${wordId}`,
		method: 'DELETE'
	})
}
