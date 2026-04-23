import type {
	KyzzWrongQuestionDashboardState,
	KyzzWrongQuestionRecordResponse,
	KyzzWrongQuestionResponse,
	KyzzWrongQuestionSummaryResponse,
	KyzzWrongQuestionSummaryState,
	KyzzWrongQuestionViewRecord
} from '@/pages/kyzz/wrong-book/types'

// AI 索引: KYZZ 小程序错题本数据整理与展示辅助。

export function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

export function toBoolean(value: unknown, fallback = false): boolean {
	if (value === true || value === false) {
		return value
	}
	if (value === 1 || value === '1' || value === 'true') {
		return true
	}
	if (value === 0 || value === '0' || value === 'false') {
		return false
	}
	return fallback
}

export function createEmptyWrongQuestionDashboard(): KyzzWrongQuestionDashboardState {
	return {
		summary: {
			totalCount: 0,
			activeCount: 0,
			masteredCount: 0,
			totalWrongTimes: 0
		},
		records: []
	}
}

export function normalizeWrongQuestionSummary(summary: KyzzWrongQuestionSummaryResponse | null | undefined): KyzzWrongQuestionSummaryState {
	return {
		totalCount: toNumber(summary?.totalCount),
		activeCount: toNumber(summary?.activeCount),
		masteredCount: toNumber(summary?.masteredCount),
		totalWrongTimes: toNumber(summary?.totalWrongTimes)
	}
}

export function normalizeWrongQuestionRecord(record: KyzzWrongQuestionRecordResponse): KyzzWrongQuestionViewRecord {
	return {
		...record,
		difficultyLevel: toNumber(record.difficultyLevel),
		wrongCount: toNumber(record.wrongCount),
		isMastered: toBoolean(record.isMastered)
	}
}

export function normalizeWrongQuestionDashboard(result: KyzzWrongQuestionResponse | null | undefined): KyzzWrongQuestionDashboardState {
	return {
		summary: normalizeWrongQuestionSummary(result?.summary),
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizeWrongQuestionRecord(item))
			: []
	}
}

export function formatWrongQuestionTime(value: string | null): string {
	if (!value) {
		return '暂无记录'
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
