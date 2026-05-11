/**
 * @file KyyyHomeApi
 * @project pipker-do
 * @module 考研英语 / 小程序首页
 * @description 封装首页仪表盘、每日一词与查词接口请求。
 * @logic 1. 调用首页 dashboard；2. 拉取每日一词轮播；3. 按关键词请求首页查词结果。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/home/types
 * @index_tags 考研英语, 首页接口, 每日一词, 查词
 * @author holic512
 */
import request from '@/shared/network/request'
import type { KyyyHomeDashboardResponse, KyyyHomeDailyWordResponse } from '@/pages/kyyy/home/types'

export interface KyyyHomeWordSearchResponse {
	wordId?: number | string | null
	wordText?: string | null
	phoneticUs?: string | null
	phoneticUk?: string | null
	partOfSpeech?: string | null
	meaningCn?: string | null
}

export function getHomeDashboard(): Promise<KyyyHomeDashboardResponse> {
	return request({
		url: '/api/kyyy/home/dashboard',
		method: 'GET'
	})
}

export function getHomeDailyWords(): Promise<KyyyHomeDailyWordResponse[]> {
	return request({
		url: '/api/kyyy/home/daily-words',
		method: 'GET'
	})
}

export function searchHomeWords(keyword: string): Promise<KyyyHomeWordSearchResponse[]> {
	return request({
		url: '/api/kyyy/home/word-search',
		method: 'GET',
		data: {
			keyword
		}
	})
}
