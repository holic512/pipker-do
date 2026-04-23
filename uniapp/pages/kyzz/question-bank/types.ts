// AI 索引: KYZZ 小程序题库页类型约束。

export type KyzzQuestionBankSelectionStatus = 'all' | 'selected' | 'unselected'

export interface KyzzQuestionBankMineSummary {
	selectedCount: number
	inProgressCount: number
	completedCount: number
}

export interface KyzzQuestionBankMineRecordResponse {
	id: number
	bankCode: string
	bankName: string
	subtitle: string | null
	coverUrl: string | null
	categoryId: number | null
	categoryName: string | null
	difficultyLevel: number
	questionCount: number | string | null
	totalScore: number | string | null
	ratingCount: number | string | null
	studyUserCount: number | string | null
	currentProgress: number | string | null
	studiedCount: number | string | null
	correctCount: number | string | null
	wrongCount: number | string | null
	lastPracticeAt: string | null
	joinSource: string | null
	joinedAt: string | null
}

export interface KyzzQuestionBankMineViewRecord extends Omit<
	KyzzQuestionBankMineRecordResponse,
	'questionCount' | 'totalScore' | 'ratingCount' | 'studyUserCount' | 'currentProgress' | 'studiedCount' | 'correctCount' | 'wrongCount'
> {
	questionCount: number
	totalScore: number
	ratingCount: number
	studyUserCount: number
	currentProgress: number
	studiedCount: number
	correctCount: number
	wrongCount: number
}

export interface KyzzQuestionBankMineResponse {
	summary: KyzzQuestionBankMineSummary
	records: KyzzQuestionBankMineRecordResponse[]
}

export interface KyzzQuestionBankMineDashboardState {
	summary: KyzzQuestionBankMineSummary
	records: KyzzQuestionBankMineViewRecord[]
}

export interface KyzzQuestionBankPublicCategoryResponse {
	id: number
	categoryName: string
	categoryLevel: number
}

export interface KyzzQuestionBankPublicRecordResponse {
	id: number
	bankCode: string
	bankName: string
	subtitle: string | null
	coverUrl: string | null
	categoryId: number | null
	categoryName: string | null
	difficultyLevel: number
	questionCount: number | string | null
	totalScore: number | string | null
	ratingCount: number | string | null
	studyUserCount: number | string | null
	sortNo: number | string | null
	currentProgress: number | string | null
	studiedCount: number | string | null
	correctCount: number | string | null
	wrongCount: number | string | null
	lastPracticeAt: string | null
	joinSource: string | null
	joinedAt: string | null
	selected: boolean
}

export interface KyzzQuestionBankPublicViewRecord extends Omit<
	KyzzQuestionBankPublicRecordResponse,
	'questionCount' | 'totalScore' | 'ratingCount' | 'studyUserCount' | 'sortNo' | 'currentProgress' | 'studiedCount' | 'correctCount' | 'wrongCount'
> {
	questionCount: number
	totalScore: number
	ratingCount: number
	studyUserCount: number
	sortNo: number
	currentProgress: number
	studiedCount: number
	correctCount: number
	wrongCount: number
}

export interface KyzzQuestionBankPublicResponse {
	summary: {
		totalCount: number
		selectedCount: number
		unselectedCount: number
	}
	categories: KyzzQuestionBankPublicCategoryResponse[]
	records: KyzzQuestionBankPublicRecordResponse[]
}

export interface KyzzQuestionBankPublicFilters {
	keyword: string
	categoryId: number | null
	difficultyLevel: number | null
	selectionStatus: KyzzQuestionBankSelectionStatus
}

export interface KyzzQuestionBankPublicDraftFilters {
	categoryId: number | null
	difficultyLevel: number | null
}

export interface SearchConfirmEvent {
	value?: string
	detail?: {
		value?: string | null
	}
}

export interface SegmentedControlClickEvent {
	currentIndex: number
}

export interface UniPopupRef {
	open: () => void
	close: () => void
}
