// AI 索引: KYYY 小程序词库页面类型。

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
