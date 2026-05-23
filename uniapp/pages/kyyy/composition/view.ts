/**
 * @file KyyyCompositionView
 * @project pipker-do
 * @module 考研英语 / 小程序作文知识库
 * @description 统一处理作文知识库接口数据归一化、标签映射与展示文案生成。
 * @logic 1. 将接口返回转成稳定页面状态；2. 提供英一英二、大小作文和题型标签映射；3. 生成列表与详情页展示文案。
 * @dependencies Types: @/pages/kyyy/composition/types
 * @index_tags 考研英语, 作文视图, 映射, 文案, 知识库
 * @author holic512
 */
import type {
	KyyyWritingEssayCard,
	KyyyWritingEssayCardState,
	KyyyWritingEssayDetail,
	KyyyWritingEssayDetailState,
	KyyyWritingEssayListResponse,
	KyyyWritingEssayListState,
	KyyyWritingExamDirection,
	KyyyWritingFacet,
	KyyyWritingFacetState,
	KyyyWritingOverview,
	KyyyWritingOverviewState
} from '@/pages/kyyy/composition/types'

export const KYYY_WRITING_EXAM_DIRECTION_LABELS: Record<string, string> = {
	english_one: '英一',
	english_two: '英二'
}

export const KYYY_WRITING_ESSAY_SECTION_LABELS: Record<string, string> = {
	small: '小作文',
	big: '大作文'
}

export const KYYY_WRITING_PROMPT_CATEGORY_LABELS: Record<string, string> = {
	email: 'Email',
	letter: 'Letter',
	notice: 'Notice',
	memo: 'Memo',
	report: 'Report',
	practical_writing: '应用文',
	chart: 'Chart',
	picture: 'Picture',
	essay: 'Essay'
}

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function toNullableNumber(value: unknown): number | null {
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? parsed : null
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
	return KYYY_WRITING_EXAM_DIRECTION_LABELS[value] || '作文'
}

export function resolveEssaySectionLabel(value: string): string {
	return KYYY_WRITING_ESSAY_SECTION_LABELS[value] || '作文'
}

export function resolvePromptCategoryLabel(value: string): string {
	return KYYY_WRITING_PROMPT_CATEGORY_LABELS[value] || normalizeText(value)
}

export function createEmptyOverviewState(): KyyyWritingOverviewState {
	return {
		examDirections: [],
		essaySections: [],
		promptCategories: [],
		recentYears: [],
		featuredRecords: [],
		loaded: false
	}
}

export function createEmptyListState(pageSize = 10): KyyyWritingEssayListState {
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

export function normalizeFacetState(facet: KyyyWritingFacet | null | undefined): KyyyWritingFacetState {
	return {
		code: normalizeText(facet?.code),
		label: normalizeText(facet?.label),
		count: Math.max(toNumber(facet?.count), 0),
		essaySection: normalizeText(facet?.essaySection)
	}
}

export function normalizeEssayCard(record: KyyyWritingEssayCard | null | undefined): KyyyWritingEssayCardState {
	return {
		id: Math.max(toNumber(record?.id), 0),
		writingCode: normalizeText(record?.writingCode),
		sourceTitle: normalizeText(record?.sourceTitle),
		examDirection: normalizeText(record?.examDirection) as KyyyWritingExamDirection | '',
		sourceYear: Math.max(toNumber(record?.sourceYear), 0),
		essaySection: normalizeText(record?.essaySection) as 'small' | 'big' | '',
		promptCategory: normalizeText(record?.promptCategory),
		scoreValue: Math.max(toNumber(record?.scoreValue), 0),
		wordLimitMin: toNullableNumber(record?.wordLimitMin),
		wordLimitMax: toNullableNumber(record?.wordLimitMax),
		knowledgeTags: normalizeTags(record?.knowledgeTags)
	}
}

export function normalizeOverviewState(result: KyyyWritingOverview | null | undefined): KyyyWritingOverviewState {
	return {
		examDirections: Array.isArray(result?.examDirections)
			? result.examDirections.map((item) => normalizeFacetState(item)).filter((item) => item.code)
			: [],
		essaySections: Array.isArray(result?.essaySections)
			? result.essaySections.map((item) => normalizeFacetState(item)).filter((item) => item.code)
			: [],
		promptCategories: Array.isArray(result?.promptCategories)
			? result.promptCategories.map((item) => normalizeFacetState(item)).filter((item) => item.code)
			: [],
		recentYears: Array.isArray(result?.recentYears)
			? result.recentYears.map((item) => Math.max(toNumber(item), 0)).filter((item) => item > 0)
			: [],
		featuredRecords: Array.isArray(result?.featuredRecords)
			? result.featuredRecords.map((item) => normalizeEssayCard(item)).filter((item) => item.id > 0)
			: [],
		loaded: true
	}
}

export function normalizeEssayListResponse(result: KyyyWritingEssayListResponse | null | undefined): Pick<KyyyWritingEssayListState, 'records' | 'pageNo' | 'pageSize' | 'total' | 'hasMore' | 'loaded'> {
	return {
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizeEssayCard(item)).filter((item) => item.id > 0)
			: [],
		pageNo: Math.max(toNumber(result?.pageNo, 1), 1),
		pageSize: Math.max(toNumber(result?.pageSize, 10), 1),
		total: Math.max(toNumber(result?.total), 0),
		hasMore: Boolean(result?.hasMore),
		loaded: true
	}
}

export function normalizeEssayDetail(result: KyyyWritingEssayDetail | null | undefined): KyyyWritingEssayDetailState {
	const card = normalizeEssayCard(result)
	return {
		...card,
		promptContent: normalizeText(result?.promptContent),
		promptTranslation: normalizeText(result?.promptTranslation),
		sampleContent: normalizeText(result?.sampleContent),
		sampleTranslation: normalizeText(result?.sampleTranslation),
		sourcePath: normalizeText(result?.sourcePath)
	}
}

export function buildListHeaderTitle(examDirection: string, essaySection: string, promptCategory: string): string {
	const parts = [resolveExamDirectionLabel(examDirection), resolveEssaySectionLabel(essaySection)]
	if (promptCategory) {
		parts.push(resolvePromptCategoryLabel(promptCategory))
	}
	return parts.filter(Boolean).join(' · ')
}

export function buildEssayMetaText(record: KyyyWritingEssayCardState): string {
	const summaryParts: string[] = [
		resolveExamDirectionLabel(record.examDirection),
		resolveEssaySectionLabel(record.essaySection),
		resolvePromptCategoryLabel(record.promptCategory)
	]
	if (record.wordLimitMin && record.wordLimitMax) {
		summaryParts.push(`${record.wordLimitMin}-${record.wordLimitMax}词`)
	} else if (record.wordLimitMin) {
		summaryParts.push(`约${record.wordLimitMin}词`)
	}
	if (record.scoreValue > 0) {
		summaryParts.push(`${record.scoreValue}分`)
	}
	return summaryParts.filter(Boolean).join(' / ')
}

export function splitParagraphs(value: string): string[] {
	return normalizeText(value)
		.split(/\n+/)
		.map((item) => item.trim())
		.filter(Boolean)
}
