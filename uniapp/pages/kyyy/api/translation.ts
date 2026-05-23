/**
 * @file KyyyTranslationApi
 * @project pipker-do
 * @module 考研英语 / 小程序翻译知识库
 * @description 封装翻译知识库首页总览、分页列表与详情接口请求。
 * @logic 1. 拉取翻译知识库分面与推荐真题；2. 按方向、模式、年份和关键词分页查询；3. 读取单题详情与分段译文。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/translation/types
 * @index_tags 考研英语, 翻译接口, 知识库, 列表, 详情
 * @author holic512
 */
import request from '@/shared/network/request'
import type {
	KyyyTranslationDetail,
	KyyyTranslationListQuery,
	KyyyTranslationListResponse,
	KyyyTranslationOverview
} from '@/pages/kyyy/translation/types'

type QueryValue = string | number | null | undefined

function compactQuery(query: Record<string, QueryValue>): Record<string, string | number> {
	const result: Record<string, string | number> = {}
	Object.keys(query).forEach((key) => {
		const value = query[key]
		if (value !== undefined && value !== null && value !== '') {
			result[key] = value
		}
	})
	return result
}

export function getTranslationOverview(): Promise<KyyyTranslationOverview> {
	return request({
		url: '/api/kyyy/translation/overview',
		method: 'GET'
	})
}

export function getTranslationPassages(query: KyyyTranslationListQuery): Promise<KyyyTranslationListResponse> {
	return request({
		url: '/api/kyyy/translation/passages',
		method: 'GET',
		data: compactQuery({
			examDirection: query.examDirection || null,
			translationMode: query.translationMode || null,
			sourceYear: query.sourceYear || null,
			keyword: query.keyword || null,
			pageNo: query.pageNo,
			pageSize: query.pageSize
		})
	})
}

export function getTranslationDetail(passageId: number): Promise<KyyyTranslationDetail> {
	return request({
		url: `/api/kyyy/translation/passages/${passageId}`,
		method: 'GET'
	})
}
