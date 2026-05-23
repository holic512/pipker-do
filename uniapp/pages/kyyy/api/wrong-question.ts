/**
 * @file KyyyWrongQuestionApi
 * @project pipker-do
 * @module 考研英语 / 小程序阅读错题本
 * @description 封装阅读错题本列表查询请求，供状态筛选与关键词搜索复用。
 * @logic 1. 组装错题本查询参数；2. 调用阅读错题本接口；3. 返回统一响应结构。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/wrong-question/types
 * @index_tags 考研英语, 错题本接口, 阅读错题, 列表查询
 * @author holic512
 */
import request from '@/shared/network/request'
import type { KyyyWrongQuestionResponse, KyyyWrongQuestionStatus } from '@/pages/kyyy/wrong-question/types'

type RequestParamValue = string | number | boolean | null | undefined

export interface KyyyWrongQuestionQuery {
	status?: KyyyWrongQuestionStatus
	keyword?: string
}

function compactQuery(params: KyyyWrongQuestionQuery): Partial<KyyyWrongQuestionQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyyyWrongQuestionQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null && value !== '') {
			query[key] = value
		}
	})
	return query as Partial<KyyyWrongQuestionQuery>
}

export function getWrongQuestions(params: KyyyWrongQuestionQuery = {}): Promise<KyyyWrongQuestionResponse> {
	return request({
		url: '/api/kyyy/wrong-questions',
		method: 'GET',
		data: compactQuery(params)
	})
}
