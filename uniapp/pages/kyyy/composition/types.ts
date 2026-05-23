/**
 * @file KyyyCompositionTypes
 * @project pipker-do
 * @module 考研英语 / 小程序作文知识库
 * @description 定义作文知识库首页、列表与详情页所需的接口结构和页面状态类型。
 * @logic 1. 约束总览分面和推荐卡片；2. 约束分页列表与筛选参数；3. 约束详情与页面展示状态。
 * @dependencies API: @/pages/kyyy/api/writing
 * @index_tags 考研英语, 作文类型, 知识库, 列表, 详情
 * @author holic512
 */

export type KyyyWritingExamDirection = 'english_one' | 'english_two'
export type KyyyWritingEssaySection = 'small' | 'big'
export type KyyyWritingPromptCategory =
	| 'email'
	| 'letter'
	| 'notice'
	| 'memo'
	| 'report'
	| 'practical_writing'
	| 'chart'
	| 'picture'
	| 'essay'

export interface KyyyWritingFacet {
	code?: string | null
	label?: string | null
	count?: number | string | null
	essaySection?: string | null
}

export interface KyyyWritingEssayCard {
	id?: number | string | null
	writingCode?: string | null
	sourceTitle?: string | null
	examDirection?: string | null
	sourceYear?: number | string | null
	essaySection?: string | null
	promptCategory?: string | null
	scoreValue?: number | string | null
	wordLimitMin?: number | string | null
	wordLimitMax?: number | string | null
	knowledgeTags?: string[] | null
}

export interface KyyyWritingOverview {
	examDirections?: KyyyWritingFacet[] | null
	essaySections?: KyyyWritingFacet[] | null
	promptCategories?: KyyyWritingFacet[] | null
	recentYears?: Array<number | string | null> | null
	featuredRecords?: KyyyWritingEssayCard[] | null
}

export interface KyyyWritingEssayListQuery {
	examDirection?: string | null
	essaySection?: string | null
	promptCategory?: string | null
	sourceYear?: number | null
	keyword?: string | null
	pageNo: number
	pageSize: number
}

export interface KyyyWritingEssayListResponse {
	records?: KyyyWritingEssayCard[] | null
	pageNo?: number | string | null
	pageSize?: number | string | null
	hasMore?: boolean | null
	total?: number | string | null
}

export interface KyyyWritingEssayDetail extends KyyyWritingEssayCard {
	promptContent?: string | null
	promptTranslation?: string | null
	sampleContent?: string | null
	sampleTranslation?: string | null
	sourcePath?: string | null
}

export interface KyyyWritingFacetState {
	code: string
	label: string
	count: number
	essaySection: string
}

export interface KyyyWritingEssayCardState {
	id: number
	writingCode: string
	sourceTitle: string
	examDirection: KyyyWritingExamDirection | ''
	sourceYear: number
	essaySection: KyyyWritingEssaySection | ''
	promptCategory: string
	scoreValue: number
	wordLimitMin: number | null
	wordLimitMax: number | null
	knowledgeTags: string[]
}

export interface KyyyWritingOverviewState {
	examDirections: KyyyWritingFacetState[]
	essaySections: KyyyWritingFacetState[]
	promptCategories: KyyyWritingFacetState[]
	recentYears: number[]
	featuredRecords: KyyyWritingEssayCardState[]
	loaded: boolean
}

export interface KyyyWritingEssayListState {
	records: KyyyWritingEssayCardState[]
	pageNo: number
	pageSize: number
	total: number
	hasMore: boolean
	loading: boolean
	loaded: boolean
	keyword: string
	sourceYear: number | null
}

export interface KyyyWritingEssayDetailState extends KyyyWritingEssayCardState {
	promptContent: string
	promptTranslation: string
	sampleContent: string
	sampleTranslation: string
	sourcePath: string
}
