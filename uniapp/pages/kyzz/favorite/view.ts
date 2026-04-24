import type {
	KyzzFavoriteQuestionRecordResponse,
	KyzzFavoriteQuestionResponse,
	KyzzFavoriteQuestionState,
	KyzzFavoriteQuestionViewRecord
} from '@/pages/kyzz/favorite/types'

// AI 索引: KYZZ 小程序收藏题目数据整理与展示辅助。

export function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

export function createEmptyFavoriteQuestionState(): KyzzFavoriteQuestionState {
	return {
		totalCount: 0,
		records: []
	}
}

export function normalizeFavoriteQuestionRecord(record: KyzzFavoriteQuestionRecordResponse): KyzzFavoriteQuestionViewRecord {
	return {
		...record,
		difficultyLevel: toNumber(record.difficultyLevel)
	}
}

export function normalizeFavoriteQuestionState(result: KyzzFavoriteQuestionResponse | null | undefined): KyzzFavoriteQuestionState {
	return {
		totalCount: toNumber(result?.totalCount),
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizeFavoriteQuestionRecord(item))
			: []
	}
}

export function formatFavoriteQuestionTime(value: string | null): string {
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
