// AI 索引: KYZZ 小程序错题本类型约束。

import type { KyzzPracticeQuestionType } from '@/pages/kyzz/practice/types'

export type KyzzWrongQuestionStatus = 'all' | 'active' | 'mastered'

export interface KyzzWrongQuestionSummaryResponse {
	totalCount: number | string | null
	activeCount: number | string | null
	masteredCount: number | string | null
	totalWrongTimes: number | string | null
}

export interface KyzzWrongQuestionSummaryState {
	totalCount: number
	activeCount: number
	masteredCount: number
	totalWrongTimes: number
}

export interface KyzzWrongQuestionRecordResponse {
	questionId: number
	bankId: number
	bankName: string
	questionType: KyzzPracticeQuestionType
	stem: string
	difficultyLevel: number | string | null
	wrongCount: number | string | null
	lastWrongAt: string | null
	isMastered: boolean | number | string | null
	masteredAt: string | null
}

export interface KyzzWrongQuestionViewRecord extends Omit<KyzzWrongQuestionRecordResponse, 'difficultyLevel' | 'wrongCount' | 'isMastered'> {
	difficultyLevel: number
	wrongCount: number
	isMastered: boolean
}

export interface KyzzWrongQuestionResponse {
	summary: KyzzWrongQuestionSummaryResponse
	records: KyzzWrongQuestionRecordResponse[]
}

export interface KyzzWrongQuestionDashboardState {
	summary: KyzzWrongQuestionSummaryState
	records: KyzzWrongQuestionViewRecord[]
}

export interface SearchConfirmEvent {
	value?: string
}
