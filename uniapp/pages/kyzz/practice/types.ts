// AI 索引: KYZZ 小程序刷题域类型约束。

export type KyzzPracticeResumeStatus = 'not_started' | 'in_progress' | 'completed'
export type KyzzPracticeQuestionType = 'single' | 'multiple' | 'short'
export type KyzzPracticeNoticeVariant = 'default' | 'accent' | 'success'
export type KyzzPracticeEmptyState = 'no_bank' | 'no_question'

export interface KyzzPracticeBankRecordResponse {
	bankId: number
	bankName: string
	coverUrl: string | null
	categoryName: string | null
	difficultyLevel: number
	questionCount: number | string | null
	currentProgress: number | string | null
	studiedCount: number | string | null
	wrongCount: number | string | null
	lastPracticeAt: string | null
	resumeStatus: KyzzPracticeResumeStatus
	resumeLabel: string
	resumeQuestionId: number | null
	resumeQuestionIndex: number | string | null
	joinedAt: string | null
}

export interface KyzzPracticeBankViewRecord extends Omit<
	KyzzPracticeBankRecordResponse,
	'questionCount' | 'currentProgress' | 'studiedCount' | 'wrongCount' | 'resumeQuestionIndex'
> {
	questionCount: number
	currentProgress: number
	studiedCount: number
	wrongCount: number
	resumeQuestionIndex: number
}

export interface KyzzPracticeDashboardResponse {
	recommendedBankId: number | null
	recommendedReason: string | null
	records: KyzzPracticeBankRecordResponse[]
}

export interface KyzzPracticeDashboardState {
	recommendedBankId: number | null
	recommendedReason: string
	records: KyzzPracticeBankViewRecord[]
}

export interface KyzzPracticeQuestionOptionResponse {
	optionKey: string
	optionContent: string
}

export interface KyzzPracticeQuestionResponse {
	id: number
	questionType: KyzzPracticeQuestionType
	stem: string
	difficultyLevel: number
	score: number | string | null
	sortNo: number | string | null
	sourceName: string | null
	yearNo: number | string | null
	options: KyzzPracticeQuestionOptionResponse[]
}

export interface KyzzPracticeQuestionView extends Omit<KyzzPracticeQuestionResponse, 'score' | 'sortNo' | 'yearNo'> {
	score: number
	sortNo: number
	yearNo: number | null
}

export interface KyzzPracticeSessionProgressResponse {
	currentQuestionIndex: number | string | null
	totalQuestionCount: number | string | null
}

export interface KyzzPracticeSessionProgress {
	currentQuestionIndex: number
	totalQuestionCount: number
}

export interface KyzzPracticeSessionResponse {
	activeBank: KyzzPracticeBankRecordResponse | null
	switchableBanks: KyzzPracticeBankRecordResponse[]
	progress: KyzzPracticeSessionProgressResponse
	question: KyzzPracticeQuestionResponse | null
	previousQuestionId: number | null
	previousQuestionIndex: number | string | null
	reviewResult: KyzzPracticeReviewResponse | null
}

export interface KyzzPracticeSessionState {
	activeBank: KyzzPracticeBankViewRecord | null
	switchableBanks: KyzzPracticeBankViewRecord[]
	progress: KyzzPracticeSessionProgress
	question: KyzzPracticeQuestionView | null
	previousQuestionId: number | null
	previousQuestionIndex: number
	reviewResult: KyzzPracticeReviewViewResult | null
}

export interface KyzzPracticeSessionQuery {
	bankId?: number | null
	questionId?: number | null
	freshAttempt?: boolean | null
}

export interface KyzzPracticeLaunchTarget {
	bankId?: number | null
	questionId?: number | null
	freshAttempt?: boolean | null
}

export interface KyzzPracticeReviewRequest {
	bankId: number
	selectedOptionKeys?: string[]
	answerText?: string
	usedSeconds: number
}

export interface KyzzPracticeSelfJudgementRequest extends KyzzPracticeReviewRequest {
	selfJudgedCorrect: boolean
}

export interface KyzzPracticeReviewResponse {
	questionId: number
	bankId: number
	questionType: KyzzPracticeQuestionType
	submittedOptionKeys: string[]
	submittedAnswerText: string | null
	requiresSelfJudgement: boolean
	isCorrect: boolean | null
	correctOptionKeys: string[]
	answerText: string | null
	analysis: string | null
	updatedBank: KyzzPracticeBankRecordResponse | null
	nextQuestionId: number | null
	nextQuestionIndex: number | string | null
	completedBank: boolean
}

export interface KyzzPracticeReviewViewResult extends Omit<KyzzPracticeReviewResponse, 'updatedBank' | 'nextQuestionIndex'> {
	updatedBank: KyzzPracticeBankViewRecord | null
	nextQuestionIndex: number
}

export interface KyzzPracticeAnswerDraftState {
	selectedOptionKeys: string[]
	answerText: string
	questionStartedAt: number
}

export interface KyzzPracticeReviewState {
	result: KyzzPracticeReviewViewResult | null
}

export interface KyzzPracticeUiState {
	loading: boolean
	loadedOnce: boolean
	submitting: boolean
	emptyState: KyzzPracticeEmptyState | null
}

export interface KyzzPracticeNoticeViewModel {
	title: string
	description: string
	variant: KyzzPracticeNoticeVariant
	pills?: string[]
	primaryText?: string
	secondaryText?: string
}

export interface UniPopupRef {
	open: () => void
	close: () => void
}
