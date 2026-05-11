/**
 * @file KyyyHomeSearchView
 * @project pipker-do
 * @module 考研英语 / 首页查词
 * @description 整理首页沉浸式查词列表与单词详情的展示态、文案和样式辅助。
 * @logic 1. 标准化查词关键词与接口结果；2. 生成列表摘要与详情兜底态；3. 计算详情词头响应式字号。
 * @dependencies API Types: @/pages/kyyy/api/home
 * @index_tags 考研英语, 首页查词, 单词详情, 展示态
 * @author holic512
 */
import type { KyyyHomeWordDetailResponse, KyyyHomeWordSearchResponse } from '@/pages/kyyy/api/home'

export const SEARCH_DEBOUNCE_DELAY = 260
export const SEARCH_SCENE_ANIMATION_DURATION = 320

const MAX_SEARCH_MEANING_LENGTH = 42

export interface KyyyHomeWordSearchState {
	wordId: number | null
	wordText: string
	phoneticUs: string
	phoneticUk: string
	partOfSpeech: string
	meaningCn: string
}

export interface KyyyHomeWordDetailState extends KyyyHomeWordSearchState {
	exampleSentence: string
	exampleTranslation: string
}

function trimDisplayText(value: string, maxLength: number): string {
	const normalized = (value || '').trim().replace(/\s+/g, ' ')
	if (!normalized) {
		return ''
	}
	if (normalized.length <= maxLength) {
		return normalized
	}
	const preferredCut = normalized.lastIndexOf(' ', Math.max(maxLength - 1, 0))
	if (preferredCut >= Math.max(Math.floor(maxLength / 2), 4)) {
		return `${normalized.slice(0, preferredCut).trim()}…`
	}
	return `${normalized.slice(0, Math.max(maxLength - 1, 0)).trim()}…`
}

function toNullableNumber(value: unknown): number | null {
	if (value === null || value === undefined || value === '') {
		return null
	}
	const numberValue = Number(value)
	return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : null
}

function normalizeSearchText(value: unknown): string {
	return typeof value === 'string' ? value.trim().replace(/\s+/g, ' ') : ''
}

export function normalizeSearchKeyword(value: string): string {
	return (value || '').trim().replace(/\s+/g, ' ')
}

export function normalizeWordSearchResult(item: KyyyHomeWordSearchResponse): KyyyHomeWordSearchState {
	return {
		wordId: toNullableNumber(item.wordId),
		wordText: normalizeSearchText(item.wordText),
		phoneticUs: normalizeSearchText(item.phoneticUs),
		phoneticUk: normalizeSearchText(item.phoneticUk),
		partOfSpeech: normalizeSearchText(item.partOfSpeech),
		meaningCn: normalizeSearchText(item.meaningCn)
	}
}

export function createWordDetailFallback(item: KyyyHomeWordSearchState): KyyyHomeWordDetailState {
	return {
		...item,
		exampleSentence: '',
		exampleTranslation: ''
	}
}

export function normalizeWordDetailResult(
	item: KyyyHomeWordDetailResponse | null | undefined,
	fallback: KyyyHomeWordSearchState | null
): KyyyHomeWordDetailState | null {
	const fallbackDetail = fallback ? createWordDetailFallback(fallback) : null
	if (!item) {
		return fallbackDetail
	}
	const wordText = normalizeSearchText(item.wordText) || fallbackDetail?.wordText || ''
	if (!wordText) {
		return fallbackDetail
	}
	return {
		wordId: toNullableNumber(item.wordId) || fallbackDetail?.wordId || null,
		wordText,
		phoneticUs: normalizeSearchText(item.phoneticUs) || fallbackDetail?.phoneticUs || '',
		phoneticUk: normalizeSearchText(item.phoneticUk) || fallbackDetail?.phoneticUk || '',
		partOfSpeech: normalizeSearchText(item.partOfSpeech) || fallbackDetail?.partOfSpeech || '',
		meaningCn: normalizeSearchText(item.meaningCn) || fallbackDetail?.meaningCn || '',
		exampleSentence: normalizeSearchText(item.exampleSentence),
		exampleTranslation: normalizeSearchText(item.exampleTranslation)
	}
}

export function resolveSearchMeaning(item: KyyyHomeWordSearchState): string {
	const meaning = trimDisplayText(item.meaningCn, MAX_SEARCH_MEANING_LENGTH)
	return meaning || '释义暂缺'
}

export function resolveSearchSummary(item: KyyyHomeWordSearchState): string {
	const meaning = resolveSearchMeaning(item)
	return item.partOfSpeech ? `${item.partOfSpeech} ${meaning}` : meaning
}

export function resolveWordDetailWordText(detail: KyyyHomeWordDetailState | KyyyHomeWordSearchState | null): string {
	return detail?.wordText || '单词详情'
}

export function resolveWordDetailWordStyle(wordText: string): Record<string, string> {
	const length = wordText.length
	if (length > 14) {
		return {
			fontSize: '54rpx',
			lineHeight: '1.08'
		}
	}
	if (length > 10) {
		return {
			fontSize: '64rpx',
			lineHeight: '1.06'
		}
	}
	return {}
}

export function resolveWordDetailPhonetic(detail: KyyyHomeWordDetailState | KyyyHomeWordSearchState | null): string {
	return detail?.phoneticUs || detail?.phoneticUk || ''
}

export function resolveWordDetailPartOfSpeech(detail: KyyyHomeWordDetailState | KyyyHomeWordSearchState | null): string {
	return detail?.partOfSpeech || ''
}

export function resolveWordDetailMeaning(detail: KyyyHomeWordDetailState | KyyyHomeWordSearchState | null): string {
	return detail?.meaningCn || '释义暂缺'
}

export function resolveWordDetailExampleSentence(detail: KyyyHomeWordDetailState | null): string {
	return detail?.exampleSentence || '暂无例句'
}

export function resolveWordDetailExampleTranslation(detail: KyyyHomeWordDetailState | null): string {
	return detail?.exampleTranslation || '翻译待补充'
}
