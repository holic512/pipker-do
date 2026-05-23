/**
 * @file KyyyWrongQuestionView
 * @project pipker-do
 * @module 考研英语 / 小程序阅读错题本
 * @description 负责阅读错题本响应归一化、时间格式化和来源文案拼装。
 * @logic 1. 归一化汇总与记录结构；2. 输出安全默认值；3. 统一列表显示时间和来源信息。
 * @dependencies Types: @/pages/kyyy/wrong-question/types
 * @index_tags 考研英语, 错题本视图, 阅读错题, 时间格式化
 * @author holic512
 */
import type {
	KyyyWrongQuestionDashboardState,
	KyyyWrongQuestionRecordResponse,
	KyyyWrongQuestionResponse,
	KyyyWrongQuestionSummaryResponse,
	KyyyWrongQuestionSummaryState,
	KyyyWrongQuestionViewRecord
} from '@/pages/kyyy/wrong-question/types'

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

export function createEmptyWrongQuestionDashboard(): KyyyWrongQuestionDashboardState {
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

export function normalizeWrongQuestionSummary(summary: KyyyWrongQuestionSummaryResponse | null | undefined): KyyyWrongQuestionSummaryState {
	return {
		totalCount: toNumber(summary?.totalCount),
		activeCount: toNumber(summary?.activeCount),
		masteredCount: toNumber(summary?.masteredCount),
		totalWrongTimes: toNumber(summary?.totalWrongTimes)
	}
}

export function normalizeWrongQuestionRecord(record: KyyyWrongQuestionRecordResponse): KyyyWrongQuestionViewRecord {
	return {
		...record,
		sourceYear: Math.max(toNumber(record.sourceYear), 0),
		passageNo: Math.max(toNumber(record.passageNo), 0),
		wrongCount: Math.max(toNumber(record.wrongCount), 0),
		isMastered: toBoolean(record.isMastered)
	}
}

export function normalizeWrongQuestionDashboard(result: KyyyWrongQuestionResponse | null | undefined): KyyyWrongQuestionDashboardState {
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

export function buildWrongQuestionSourceText(record: KyyyWrongQuestionViewRecord): string {
	const parts = [
		record.examDirectionLabel || '',
		record.sourceYear ? `${record.sourceYear}` : '',
		record.sourceName || '',
		record.passageNo ? `Text ${record.passageNo}` : ''
	].filter(Boolean)
	return parts.join(' · ')
}

function pad(value: number): string {
	return String(value).padStart(2, '0')
}
