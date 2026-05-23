/**
 * @file KyyyLeaderboardApi
 * @project pipker-do
 * @module 考研英语 / 小程序排行榜
 * @description 封装英语综合排行榜用户侧接口请求。
 * @logic 1. 接收榜单周期和条数；2. 过滤空查询参数；3. 请求英语排行榜接口并返回同构响应。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/leaderboard/types
 * @index_tags 考研英语, 排行榜API, 小程序请求, 综合榜
 * @author holic512
 */
import request from '@/shared/network/request'
import type { KyyyLeaderboardResponse, KyyyLeaderboardScope } from '@/pages/kyyy/leaderboard/types'

type RequestParamValue = string | number | boolean | null | undefined

export interface KyyyLeaderboardQuery {
	scope?: KyyyLeaderboardScope
	limit?: number
}

function compactQuery(params: KyyyLeaderboardQuery): Partial<KyyyLeaderboardQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyyyLeaderboardQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null) {
			query[key] = value
		}
	})
	return query as Partial<KyyyLeaderboardQuery>
}

export function getLeaderboard(params: KyyyLeaderboardQuery = {}): Promise<KyyyLeaderboardResponse> {
	return request({
		url: '/api/kyyy/leaderboard',
		method: 'GET',
		data: compactQuery(params)
	})
}
