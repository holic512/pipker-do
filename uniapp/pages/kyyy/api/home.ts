import request from '@/shared/network/request'
import type { KyyyHomeDashboardResponse, KyyyHomeDailyWordResponse } from '@/pages/kyyy/home/types'

// AI 索引: KYYY 小程序首页业务 API。

export function getHomeDashboard(): Promise<KyyyHomeDashboardResponse> {
	return request({
		url: '/api/kyyy/home/dashboard',
		method: 'GET'
	})
}

export function getHomeDailyWord(): Promise<KyyyHomeDailyWordResponse> {
	return request({
		url: '/api/kyyy/practice/next-word',
		method: 'GET'
	})
}
