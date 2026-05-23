/**
 * @file KyyyLeaderboardTypes
 * @project pipker-do
 * @module 考研英语 / 小程序排行榜
 * @description 定义英语排行榜接口响应与页面状态类型。
 * @logic 1. 约束榜单周期和摘要结构；2. 约束榜单记录与我的排名数据；3. 区分接口原始类型和页面归一化状态。
 * @dependencies API: @/pages/kyyy/api/leaderboard
 * @index_tags 考研英语, 排行榜类型, TypeScript, 页面状态
 * @author holic512
 */
export type KyyyLeaderboardScope = 'daily' | 'weekly' | 'total'

export interface KyyyLeaderboardSummaryResponse {
	scope: KyyyLeaderboardScope
	periodLabel: string
	periodStart: string | null
	periodEnd: string | null
	participantCount: number | string | null
	generatedAt: string | null
	ruleDescription: string | null
}

export interface KyyyLeaderboardRecordResponse {
	rankNo: number | string | null
	userId: number
	nickname: string
	avatarUrl: string | null
	studyCount: number | string | null
	correctCount: number | string | null
	accuracyRate: number | string | null
	lastPracticeAt: string | null
	isMe: boolean | number | string | null
}

export interface KyyyLeaderboardResponse {
	summary: KyyyLeaderboardSummaryResponse | null
	myRecord: KyyyLeaderboardRecordResponse | null
	records: KyyyLeaderboardRecordResponse[]
}

export interface KyyyLeaderboardSummaryState {
	scope: KyyyLeaderboardScope
	periodLabel: string
	periodStart: string | null
	periodEnd: string | null
	participantCount: number
	generatedAt: string | null
	ruleDescription: string
}

export interface KyyyLeaderboardRecordState
	extends Omit<KyyyLeaderboardRecordResponse, 'rankNo' | 'studyCount' | 'correctCount' | 'accuracyRate' | 'isMe'> {
	rankNo: number
	studyCount: number
	correctCount: number
	accuracyRate: number
	isMe: boolean
}

export interface KyyyLeaderboardDashboardState {
	summary: KyyyLeaderboardSummaryState
	myRecord: KyyyLeaderboardRecordState | null
	records: KyyyLeaderboardRecordState[]
}
