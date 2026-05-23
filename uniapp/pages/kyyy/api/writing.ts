/**
 * @file KyyyWritingApi
 * @project pipker-do
 * @module 考研英语 / 小程序作文知识库
 * @description 封装作文知识库首页总览、分页列表与详情接口请求。
 * @logic 1. 拉取首页分面与推荐真题；2. 按分类和关键词分页查询；3. 读取单篇作文详情。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/composition/types
 * @index_tags 考研英语, 作文接口, 知识库, 列表, 详情
 * @author holic512
 */
import request from '@/shared/network/request'
import type {
	KyyyWritingEssayDetail,
	KyyyWritingEssayListQuery,
	KyyyWritingEssayListResponse,
	KyyyWritingOverview
} from '@/pages/kyyy/composition/types'

function compactQuery(query: Record<string, string | number | null | undefined>): Record<string, string | number> {
	const result: Record<string, string | number> = {}
	Object.keys(query).forEach((key) => {
		const value = query[key]
		if (value !== undefined && value !== null && value !== '') {
			result[key] = value
		}
	})
	return result
}

export function getWritingOverview(): Promise<KyyyWritingOverview> {
	return request({
		url: '/api/kyyy/writing/overview',
		method: 'GET'
	})
}

export function getWritingEssays(query: KyyyWritingEssayListQuery): Promise<KyyyWritingEssayListResponse> {
	return request({
		url: '/api/kyyy/writing/essays',
		method: 'GET',
		data: compactQuery({
			examDirection: query.examDirection || null,
			essaySection: query.essaySection || null,
			promptCategory: query.promptCategory || null,
			sourceYear: query.sourceYear || null,
			keyword: query.keyword || null,
			pageNo: query.pageNo,
			pageSize: query.pageSize
		})
	})
}

export function getWritingEssayDetail(essayId: number): Promise<KyyyWritingEssayDetail> {
	return request({
		url: `/api/kyyy/writing/essays/${essayId}`,
		method: 'GET'
	})
}
