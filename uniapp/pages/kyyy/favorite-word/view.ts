/**
 * @file KyyyFavoriteWordView
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 整理单词收藏列表的归一化数据和时间展示文案。
 * @logic 1. 归一化收藏列表响应；2. 补齐展示字段空值；3. 统一收藏时间格式。
 * @dependencies Types: @/pages/kyyy/favorite-word/types
 * @index_tags 考研英语, 单词收藏, 展示态, 时间格式化
 * @author holic512
 */
import type {
	KyyyFavoriteWordRecordResponse,
	KyyyFavoriteWordResponse,
	KyyyFavoriteWordState,
	KyyyFavoriteWordViewRecord
} from '@/pages/kyyy/favorite-word/types'

export function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

export function createEmptyFavoriteWordState(): KyyyFavoriteWordState {
	return {
		totalCount: 0,
		records: []
	}
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim().replace(/\s+/g, ' ') : ''
}

export function normalizeFavoriteWordRecord(record: KyyyFavoriteWordRecordResponse): KyyyFavoriteWordViewRecord {
	return {
		...record,
		wordText: normalizeText(record.wordText),
		phoneticUs: normalizeText(record.phoneticUs),
		phoneticUk: normalizeText(record.phoneticUk),
		partOfSpeech: normalizeText(record.partOfSpeech),
		meaningCn: normalizeText(record.meaningCn)
	}
}

export function normalizeFavoriteWordState(result: KyyyFavoriteWordResponse | null | undefined): KyyyFavoriteWordState {
	return {
		totalCount: toNumber(result?.totalCount),
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizeFavoriteWordRecord(item)).filter((item) => !!item.wordText)
			: []
	}
}

export function formatFavoriteWordTime(value: string | null): string {
	if (!value) {
		return '刚刚收藏'
	}
	const normalized = value.replace(/-/g, '/')
	const targetDate = new Date(normalized)
	if (Number.isNaN(targetDate.getTime())) {
		return value.slice(0, 16).replace('T', ' ')
	}
	const now = new Date()
	const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
	const targetStart = new Date(targetDate.getFullYear(), targetDate.getMonth(), targetDate.getDate()).getTime()
	const diffDays = Math.floor((todayStart - targetStart) / (24 * 60 * 60 * 1000))
	const timeText = `${pad(targetDate.getHours())}:${pad(targetDate.getMinutes())}`
	if (diffDays === 0) {
		return `今天 ${timeText}`
	}
	if (diffDays === 1) {
		return `昨天 ${timeText}`
	}
	if (now.getFullYear() === targetDate.getFullYear()) {
		return `${pad(targetDate.getMonth() + 1)}-${pad(targetDate.getDate())} ${timeText}`
	}
	return `${targetDate.getFullYear()}-${pad(targetDate.getMonth() + 1)}-${pad(targetDate.getDate())} ${timeText}`
}

function pad(value: number): string {
	return String(value).padStart(2, '0')
}
