// AI 索引: KYZZ 小程序刷题域类型约束。

export type KyzzPracticeResumeStatus = 'not_started' | 'in_progress' | 'completed'
export type KyzzPracticeQuestionType = 'single' | 'multiple' | 'short'
export type KyzzPracticeNoticeVariant = 'default' | 'accent' | 'success'
export type KyzzPracticeEmptyState = 'no_bank' | 'no_question'
export type KyzzPracticeSourceType = 'bank' | 'wrong_book' | 'favorite'
export type KyzzPracticeSourceStatus = 'all' | 'active' | 'mastered'

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
	isFavorite: boolean | number | string | null
	options: KyzzPracticeQuestionOptionResponse[]
}

export interface KyzzPracticeQuestionView extends Omit<KyzzPracticeQuestionResponse, 'score' | 'sortNo' | 'yearNo' | 'isFavorite'> {
	score: number
	sortNo: number
	yearNo: number | null
	isFavorite: boolean
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
	sourceType: KyzzPracticeSourceType | string | null
	sourceTitle: string | null
}

export interface KyzzPracticeSessionState {
	activeBank: KyzzPracticeBankViewRecord | null
	switchableBanks: KyzzPracticeBankViewRecord[]
	progress: KyzzPracticeSessionProgress
	question: KyzzPracticeQuestionView | null
	previousQuestionId: number | null
	previousQuestionIndex: number
	reviewResult: KyzzPracticeReviewViewResult | null
	sourceType: KyzzPracticeSourceType
	sourceTitle: string
}

export interface KyzzPracticeSessionQuery {
	bankId?: number | null
	questionId?: number | null
	freshAttempt?: boolean | null
	sourceType?: KyzzPracticeSourceType | null
	sourceStatus?: KyzzPracticeSourceStatus | null
	keyword?: string | null
}

export interface KyzzPracticeLaunchTarget {
	bankId?: number | null
	questionId?: number | null
	freshAttempt?: boolean | null
	sourceType?: KyzzPracticeSourceType | null
	sourceStatus?: KyzzPracticeSourceStatus | null
	keyword?: string | null
}

export interface KyzzPracticeReviewRequest {
	bankId: number
	selectedOptionKeys?: string[]
	answerText?: string
	usedSeconds: number
	sourceType?: KyzzPracticeSourceType | null
	sourceStatus?: KyzzPracticeSourceStatus | null
	keyword?: string | null
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
	sourceType: KyzzPracticeSourceType | string | null
	sourceTitle: string | null
	completedSource: boolean | null
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

export interface KyzzPracticeCommentAuthor {
	id: number
	nickname: string
	avatarUrl: string | null
}

export interface KyzzPracticeCommentItem {
	commentId: number
	questionId: number
	content: string
	createdAt: string | null
	createdAtLabel?: string
	likeCount: number
	replyCount: number
	author: KyzzPracticeCommentAuthor
	isMine: boolean
}

export interface KyzzPracticeCommentPageResponse {
	records: KyzzPracticeCommentItem[]
	pageNo: number | string | null
	pageSize: number | string | null
	hasMore: boolean
	total: number | string | null
}

export interface KyzzPracticeCommentQuery {
	pageNo: number
	pageSize: number
}

export interface KyzzPracticeCommentCreateRequest {
	content: string
}

export interface KyzzPracticeCommentState {
	questionId: number | null
	records: KyzzPracticeCommentItem[]
	pageNo: number
	pageSize: number
	total: number
	hasMore: boolean
	loading: boolean
	loadingMore: boolean
	submitting: boolean
	initialized: boolean
	errorMessage: string
	composerContent: string
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
