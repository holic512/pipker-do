/**
 * @file KyyyWordBankTypes
 * @project pipker-do
 * @module 考研英语 / 小程序词库页
 * @description 定义词库列表、选择状态与学习入口底部动作的前端类型。
 * @logic 1. 约束词库列表接口结构；2. 约束页面内的词库记录状态；3. 约束学习/复习模式下的底部 CTA 状态。
 * @dependencies API: @/pages/kyyy/api/word-bank
 * @index_tags 考研英语, 词库类型, 默认词库, 学习入口
 * @author holic512
 */

export type KyyyWordBankEntryMode = 'default' | 'study' | 'review'

export interface KyyyWordBankSummaryResponse {
	totalCount?: number | string | null
	selectedCount?: number | string | null
	defaultWordBankId?: number | string | null
}

export interface KyyyWordBankRecordResponse {
	id?: number | string | null
	bankCode?: string | null
	bankName?: string | null
	subtitle?: string | null
	description?: string | null
	wordCount?: number | string | null
	studyUserCount?: number | string | null
	sortNo?: number | string | null
	joinSource?: string | null
	joinedAt?: string | null
	lastPracticeAt?: string | null
	selected?: boolean | null
	isDefault?: boolean | null
}

export interface KyyyWordBankListResponse {
	summary?: KyyyWordBankSummaryResponse | null
	records?: KyyyWordBankRecordResponse[] | null
}

export interface KyyyWordBankSelectionRequest {
	selected?: boolean
}

export interface KyyyWordBankSummaryState {
	totalCount: number
	selectedCount: number
	defaultWordBankId: number | null
}

export interface KyyyWordBankRecordState {
	id: number
	bankCode: string
	bankName: string
	subtitle: string
	description: string
	wordCount: number
	studyUserCount: number
	sortNo: number
	joinSource: string
	joinedAt: string
	lastPracticeAt: string
	selected: boolean
	isDefault: boolean
}

export interface KyyyWordBankListState {
	summary: KyyyWordBankSummaryState
	records: KyyyWordBankRecordState[]
	loaded: boolean
}

export interface KyyyWordBankFooterActionState {
	visible: boolean
	text: string
	mode: KyyyWordBankEntryMode
	disabled: boolean
}
