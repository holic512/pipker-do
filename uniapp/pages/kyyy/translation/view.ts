/**
 * @file KyyyTranslationView
 * @project pipker-do
 * @module 考研英语 / 小程序翻译知识库
 * @description 统一处理翻译知识库接口数据归一化、标签映射与展示文案生成。
 * @logic 1. 将接口返回转成稳定页面状态；2. 提供英一英二和翻译模式标签映射；3. 生成列表与详情页展示文案。
 * @dependencies Types: @/pages/kyyy/translation/types
 * @index_tags 考研英语, 翻译视图, 映射, 文案, 知识库
 * @author holic512
 */
import type {
	KyyyTranslationCard,
	KyyyTranslationCardState,
	KyyyTranslationDetail,
	KyyyTranslationDetailState,
	KyyyTranslationFacet,
	KyyyTranslationFacetState,
	KyyyTranslationListResponse,
	KyyyTranslationListState,
	KyyyTranslationOverview,
	KyyyTranslationOverviewState,
	KyyyTranslationSegment,
	KyyyTranslationSegmentState
} from '@/pages/kyyy/translation/types'

export const KYYY_TRANSLATION_EXAM_DIRECTION_LABELS: Record<string, string> = {
	english_one: '英一',
	english_two: '英二'
}

export const KYYY_TRANSLATION_MODE_LABELS: Record<string, string> = {
	segmented: '划线句翻译',
	passage: '全文翻译'
}

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

function normalizeTags(value: unknown): string[] {
	return Array.isArray(value)
		? value.map((item) => normalizeText(item)).filter(Boolean)
		: []
}

export function resolveExamDirectionLabel(value: string): string {
	return KYYY_TRANSLATION_EXAM_DIRECTION_LABELS[value] || '翻译'
}

export function resolveTranslationModeLabel(value: string): string {
	return KYYY_TRANSLATION_MODE_LABELS[value] || '翻译'
}

export function createEmptyOverviewState(): KyyyTranslationOverviewState {
	return {
		examDirections: [],
		translationModes: [],
		recentYears: [],
		featuredRecords: [],
		loaded: false
	}
}

export function createEmptyListState(pageSize = 10): KyyyTranslationListState {
	return {
		records: [],
		pageNo: 1,
		pageSize,
		total: 0,
		hasMore: false,
		loading: false,
		loaded: false,
		keyword: '',
		sourceYear: null
	}
}

export function normalizeFacetState(facet: KyyyTranslationFacet | null | undefined): KyyyTranslationFacetState {
	return {
		code: normalizeText(facet?.code),
		label: normalizeText(facet?.label),
		count: Math.max(toNumber(facet?.count), 0)
	}
}

export function normalizeTranslationCard(record: KyyyTranslationCard | null | undefined): KyyyTranslationCardState {
	return {
		id: Math.max(toNumber(record?.id), 0),
		translationCode: normalizeText(record?.translationCode),
		sourceTitle: normalizeText(record?.sourceTitle),
		examDirection: normalizeText(record?.examDirection) as 'english_one' | 'english_two' | '',
		sourceYear: Math.max(toNumber(record?.sourceYear), 0),
		translationMode: normalizeText(record?.translationMode) as 'segmented' | 'passage' | '',
		scoreValue: Math.max(toNumber(record?.scoreValue), 0),
		segmentCount: Math.max(toNumber(record?.segmentCount), 0),
		knowledgeTags: normalizeTags(record?.knowledgeTags)
	}
}

export function normalizeOverviewState(result: KyyyTranslationOverview | null | undefined): KyyyTranslationOverviewState {
	return {
		examDirections: Array.isArray(result?.examDirections)
			? result.examDirections.map((item) => normalizeFacetState(item)).filter((item) => item.code)
			: [],
		translationModes: Array.isArray(result?.translationModes)
			? result.translationModes.map((item) => normalizeFacetState(item)).filter((item) => item.code)
			: [],
		recentYears: Array.isArray(result?.recentYears)
			? result.recentYears.map((item) => Math.max(toNumber(item), 0)).filter((item) => item > 0)
			: [],
		featuredRecords: Array.isArray(result?.featuredRecords)
			? result.featuredRecords.map((item) => normalizeTranslationCard(item)).filter((item) => item.id > 0)
			: [],
		loaded: true
	}
}

export function normalizeTranslationListResponse(result: KyyyTranslationListResponse | null | undefined): Pick<KyyyTranslationListState, 'records' | 'pageNo' | 'pageSize' | 'total' | 'hasMore' | 'loaded'> {
	return {
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizeTranslationCard(item)).filter((item) => item.id > 0)
			: [],
		pageNo: Math.max(toNumber(result?.pageNo, 1), 1),
		pageSize: Math.max(toNumber(result?.pageSize, 10), 1),
		total: Math.max(toNumber(result?.total), 0),
		hasMore: Boolean(result?.hasMore),
		loaded: true
	}
}

export function normalizeTranslationSegment(segment: KyyyTranslationSegment | null | undefined): KyyyTranslationSegmentState {
	return {
		segmentNo: Math.max(toNumber(segment?.segmentNo), 0),
		sourceText: normalizeText(segment?.sourceText),
		translatedText: normalizeText(segment?.translatedText)
	}
}

export function normalizeTranslationDetail(result: KyyyTranslationDetail | null | undefined): KyyyTranslationDetailState {
	const card = normalizeTranslationCard(result)
	return {
		...card,
		promptInstruction: normalizeText(result?.promptInstruction),
		promptContent: normalizeText(result?.promptContent),
		promptTranslation: normalizeText(result?.promptTranslation),
		referenceTranslation: normalizeText(result?.referenceTranslation),
		referenceNote: normalizeText(result?.referenceNote),
		segments: Array.isArray(result?.segments)
			? result.segments.map((item) => normalizeTranslationSegment(item)).filter((item) => item.segmentNo > 0 || item.sourceText || item.translatedText)
			: [],
		sourcePath: normalizeText(result?.sourcePath),
		sourcePromptRef: normalizeText(result?.sourcePromptRef),
		sourceAnswerRef: normalizeText(result?.sourceAnswerRef)
	}
}

export function buildTranslationMetaText(record: KyyyTranslationCardState): string {
	const parts = [
		resolveExamDirectionLabel(record.examDirection),
		resolveTranslationModeLabel(record.translationMode)
	]
	if (record.segmentCount > 0) {
		parts.push(`${record.segmentCount}段`)
	}
	if (record.scoreValue > 0) {
		parts.push(`${record.scoreValue}分`)
	}
	return parts.filter(Boolean).join(' / ')
}

export function buildTranslationListTitle(examDirection: string, translationMode: string): string {
	const parts: string[] = []
	if (examDirection) {
		parts.push(resolveExamDirectionLabel(examDirection))
	}
	if (translationMode) {
		parts.push(resolveTranslationModeLabel(translationMode))
	}
	return parts.length ? parts.join(' · ') : '翻译列表'
}

export function splitParagraphs(value: string): string[] {
	return normalizeText(value)
		.split(/\n+/)
		.map((item) => item.trim())
		.filter(Boolean)
}
