/**
 * @file KyyyWrongQuestionTypes
 * @project pipker-do
 * @module 考研英语 / 小程序阅读错题本
 * @description 定义阅读错题本接口结构、页面状态和搜索事件类型。
 * @logic 1. 约束汇总与记录响应；2. 提供前端归一化后的展示类型；3. 统一状态筛选枚举。
 * @dependencies None
 * @index_tags 考研英语, 错题本类型, 阅读错题, 小程序状态
 * @author holic512
 */

export type KyyyWrongQuestionStatus = 'all' | 'active' | 'mastered'

export interface KyyyWrongQuestionSummaryResponse {
	totalCount: number | string | null
	activeCount: number | string | null
	masteredCount: number | string | null
	totalWrongTimes: number | string | null
}

export interface KyyyWrongQuestionSummaryState {
	totalCount: number
	activeCount: number
	masteredCount: number
	totalWrongTimes: number
}

export interface KyyyWrongQuestionRecordResponse {
	questionId: number
	passageId: number
	sourceYear: number | string | null
	sourceName: string
	passageNo: number | string | null
	examDirection: string
	examDirectionLabel: string
	stem: string
	wrongCount: number | string | null
	lastWrongAt: string | null
	isMastered: boolean | number | string | null
	masteredAt: string | null
}

export interface KyyyWrongQuestionViewRecord extends Omit<KyyyWrongQuestionRecordResponse, 'sourceYear' | 'passageNo' | 'wrongCount' | 'isMastered'> {
	sourceYear: number
	passageNo: number
	wrongCount: number
	isMastered: boolean
}

export interface KyyyWrongQuestionResponse {
	summary: KyyyWrongQuestionSummaryResponse
	records: KyyyWrongQuestionRecordResponse[]
}

export interface KyyyWrongQuestionDashboardState {
	summary: KyyyWrongQuestionSummaryState
	records: KyyyWrongQuestionViewRecord[]
}

export interface SearchConfirmEvent {
	value?: string
	detail?: {
		value?: string | null
	}
}
