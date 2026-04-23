// AI 索引: KYZZ 小程序排行榜类型约束。

export type KyzzLeaderboardScope = 'daily' | 'weekly' | 'total'

export interface KyzzLeaderboardSummaryResponse {
	scope: KyzzLeaderboardScope
	periodLabel: string
	periodStart: string | null
	periodEnd: string | null
	participantCount: number | string | null
	generatedAt: string | null
	ruleDescription: string | null
}

export interface KyzzLeaderboardRecordResponse {
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

export interface KyzzLeaderboardResponse {
	summary: KyzzLeaderboardSummaryResponse | null
	myRecord: KyzzLeaderboardRecordResponse | null
	records: KyzzLeaderboardRecordResponse[]
}

export interface KyzzLeaderboardSummaryState {
	scope: KyzzLeaderboardScope
	periodLabel: string
	periodStart: string | null
	periodEnd: string | null
	participantCount: number
	generatedAt: string | null
	ruleDescription: string
}

export interface KyzzLeaderboardRecordState
	extends Omit<KyzzLeaderboardRecordResponse, 'rankNo' | 'studyCount' | 'correctCount' | 'accuracyRate' | 'isMe'> {
	rankNo: number
	studyCount: number
	correctCount: number
	accuracyRate: number
	isMe: boolean
}

export interface KyzzLeaderboardDashboardState {
	summary: KyzzLeaderboardSummaryState
	myRecord: KyzzLeaderboardRecordState | null
	records: KyzzLeaderboardRecordState[]
}
