import request from '@/shared/network/request'
import type { KyzzLeaderboardResponse, KyzzLeaderboardScope } from '@/pages/kyzz/leaderboard/types'

// AI 索引: KYZZ 小程序排行榜 API。

type RequestParamValue = string | number | boolean | null | undefined

export interface KyzzLeaderboardQuery {
	scope?: KyzzLeaderboardScope
	limit?: number
}

function compactQuery(params: KyzzLeaderboardQuery): Partial<KyzzLeaderboardQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyzzLeaderboardQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null) {
			query[key] = value
		}
	})
	return query as Partial<KyzzLeaderboardQuery>
}

export function getLeaderboard(params: KyzzLeaderboardQuery = {}): Promise<KyzzLeaderboardResponse> {
	return request({
		url: '/api/kyzz/leaderboard',
		method: 'GET',
		data: compactQuery(params)
	})
}
