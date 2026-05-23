/**
 * @file KyyyLeaderboardView
 * @project pipker-do
 * @module 考研英语 / 小程序排行榜
 * @description 提供英语排行榜页面的数据归一化和展示格式化能力。
 * @logic 1. 将接口中的混合数值类型统一转成前端可计算状态；2. 规范空榜默认摘要；3. 提供正确率和活跃时间格式化方法。
 * @dependencies Types: @/pages/kyyy/leaderboard/types
 * @index_tags 考研英语, 排行榜视图, 数据归一化, 展示辅助
 * @author holic512
 */
import type {
	KyyyLeaderboardDashboardState,
	KyyyLeaderboardRecordResponse,
	KyyyLeaderboardRecordState,
	KyyyLeaderboardResponse,
	KyyyLeaderboardScope,
	KyyyLeaderboardSummaryResponse,
	KyyyLeaderboardSummaryState
} from '@/pages/kyyy/leaderboard/types'

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

export function createEmptyLeaderboardState(scope: KyyyLeaderboardScope = 'daily'): KyyyLeaderboardDashboardState {
	return {
		summary: {
			scope,
			periodLabel: '',
			periodStart: null,
			periodEnd: null,
			participantCount: 0,
			generatedAt: null,
			ruleDescription: '排序规则：综合学习量优先，其次综合正确率，再比最近活跃时间；学习量=完成背词卡片数+已作答阅读题数。'
		},
		myRecord: null,
		records: []
	}
}

export function normalizeLeaderboardSummary(
	summary: KyyyLeaderboardSummaryResponse | null | undefined,
	fallbackScope: KyyyLeaderboardScope
): KyyyLeaderboardSummaryState {
	const responseScope = summary?.scope
	const scope = isSupportedScope(responseScope) ? responseScope : fallbackScope
	return {
		scope,
		periodLabel: summary?.periodLabel || '',
		periodStart: summary?.periodStart || null,
		periodEnd: summary?.periodEnd || null,
		participantCount: Math.max(toNumber(summary?.participantCount), 0),
		generatedAt: summary?.generatedAt || null,
		ruleDescription: summary?.ruleDescription || '排序规则：综合学习量优先，其次综合正确率，再比最近活跃时间；学习量=完成背词卡片数+已作答阅读题数。'
	}
}

export function normalizeLeaderboardRecord(record: KyyyLeaderboardRecordResponse): KyyyLeaderboardRecordState {
	return {
		...record,
		rankNo: Math.max(toNumber(record.rankNo), 0),
		studyCount: Math.max(toNumber(record.studyCount), 0),
		correctCount: Math.max(toNumber(record.correctCount), 0),
		accuracyRate: normalizeAccuracyRate(record.accuracyRate),
		isMe: toBoolean(record.isMe)
	}
}

export function normalizeLeaderboardDashboard(
	result: KyyyLeaderboardResponse | null | undefined,
	fallbackScope: KyyyLeaderboardScope
): KyyyLeaderboardDashboardState {
	const responseRecords = Array.isArray(result?.records) ? result.records : []
	return {
		summary: normalizeLeaderboardSummary(result?.summary, fallbackScope),
		myRecord: result?.myRecord ? normalizeLeaderboardRecord(result.myRecord) : null,
		records: responseRecords.map((item) => normalizeLeaderboardRecord(item))
	}
}

export function formatAccuracyRate(value: number): string {
	return `${normalizeAccuracyRate(value).toFixed(2)}%`
}

export function formatLeaderboardTime(value: string | null): string {
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

function isSupportedScope(scope: string | null | undefined): scope is KyyyLeaderboardScope {
	return scope === 'daily' || scope === 'weekly' || scope === 'total'
}

function normalizeAccuracyRate(value: unknown): number {
	const rate = toNumber(value)
	if (rate <= 0) {
		return 0
	}
	if (rate >= 100) {
		return 100
	}
	return Math.round(rate * 100) / 100
}

function pad(value: number): string {
	return String(value).padStart(2, '0')
}
