/**
 * @file KyyyTranslationTypes
 * @project pipker-do
 * @module 考研英语 / 小程序翻译知识库
 * @description 定义翻译知识库首页、列表与详情页所需的接口结构和页面状态类型。
 * @logic 1. 约束分面和推荐卡片；2. 约束分页查询参数与列表结果；3. 约束详情原文、译文和分段结构。
 * @dependencies API: @/pages/kyyy/api/translation
 * @index_tags 考研英语, 翻译类型, 知识库, 列表, 详情
 * @author holic512
 */

export type KyyyTranslationExamDirection = 'english_one' | 'english_two'
export type KyyyTranslationMode = 'segmented' | 'passage'

export interface KyyyTranslationFacet {
	code?: string | null
	label?: string | null
	count?: number | string | null
}

export interface KyyyTranslationCard {
	id?: number | string | null
	translationCode?: string | null
	sourceTitle?: string | null
	examDirection?: string | null
	sourceYear?: number | string | null
	translationMode?: string | null
	scoreValue?: number | string | null
	segmentCount?: number | string | null
	knowledgeTags?: string[] | null
}

export interface KyyyTranslationOverview {
	examDirections?: KyyyTranslationFacet[] | null
	translationModes?: KyyyTranslationFacet[] | null
	recentYears?: Array<number | string | null> | null
	featuredRecords?: KyyyTranslationCard[] | null
}

export interface KyyyTranslationListQuery {
	examDirection?: string | null
	translationMode?: string | null
	sourceYear?: number | null
	keyword?: string | null
	pageNo: number
	pageSize: number
}

export interface KyyyTranslationListResponse {
	records?: KyyyTranslationCard[] | null
	pageNo?: number | string | null
	pageSize?: number | string | null
	hasMore?: boolean | null
	total?: number | string | null
}

export interface KyyyTranslationSegment {
	segmentNo?: number | string | null
	sourceText?: string | null
	translatedText?: string | null
}

export interface KyyyTranslationDetail extends KyyyTranslationCard {
	promptInstruction?: string | null
	promptContent?: string | null
	promptTranslation?: string | null
	referenceTranslation?: string | null
	referenceNote?: string | null
	segments?: KyyyTranslationSegment[] | null
	sourcePath?: string | null
	sourcePromptRef?: string | null
	sourceAnswerRef?: string | null
}

export interface KyyyTranslationFacetState {
	code: string
	label: string
	count: number
}

export interface KyyyTranslationCardState {
	id: number
	translationCode: string
	sourceTitle: string
	examDirection: KyyyTranslationExamDirection | ''
	sourceYear: number
	translationMode: KyyyTranslationMode | ''
	scoreValue: number
	segmentCount: number
	knowledgeTags: string[]
}

export interface KyyyTranslationOverviewState {
	examDirections: KyyyTranslationFacetState[]
	translationModes: KyyyTranslationFacetState[]
	recentYears: number[]
	featuredRecords: KyyyTranslationCardState[]
	loaded: boolean
}

export interface KyyyTranslationListState {
	records: KyyyTranslationCardState[]
	pageNo: number
	pageSize: number
	total: number
	hasMore: boolean
	loading: boolean
	loaded: boolean
	keyword: string
	sourceYear: number | null
}

export interface KyyyTranslationSegmentState {
	segmentNo: number
	sourceText: string
	translatedText: string
}

export interface KyyyTranslationDetailState extends KyyyTranslationCardState {
	promptInstruction: string
	promptContent: string
	promptTranslation: string
	referenceTranslation: string
	referenceNote: string
	segments: KyyyTranslationSegmentState[]
	sourcePath: string
	sourcePromptRef: string
	sourceAnswerRef: string
}
