/**
 * @file KyyyReadingTypes
 * @project pipker-do
 * @module 考研英语 / 小程序阅读做题
 * @description 定义阅读做题接口结构、页面状态与单题草稿提交类型。
 * @logic 1. 约束整篇阅读会话响应；2. 约束题目、选项、结果汇总结构；3. 约束单题答案草稿请求。
 * @dependencies API: @/pages/kyyy/api/reading
 * @index_tags 考研英语, 阅读类型, 会话响应, 单题草稿
 * @author holic512
 */

export type KyyyReadingSessionStatus = 'active' | 'submitted' | 'empty'
export type KyyyReadingAnnotationContentType = 'passage_text' | 'question_stem'
export type KyyyReadingTextTokenKind = 'word' | 'space' | 'punct' | 'newline'

export interface KyyyReadingPassageResponse {
	id?: number | string | null
	sourceYear?: number | string | null
	sourceName?: string | null
	passageNo?: number | string | null
	title?: string | null
	passageText?: string | null
	examDirection?: string | null
	examDirectionLabel?: string | null
	annotations?: KyyyReadingAnnotationResponse[] | null
}

export interface KyyyReadingProgressResponse {
	totalQuestions?: number | string | null
	answeredCount?: number | string | null
	unansweredCount?: number | string | null
	submitted?: boolean | null
}

export interface KyyyReadingOptionResponse {
	optionKey?: string | null
	optionContent?: string | null
}

export interface KyyyReadingQuestionResponse {
	questionId?: number | string | null
	questionNo?: number | string | null
	stem?: string | null
	options?: KyyyReadingOptionResponse[] | null
	selectedOptionKey?: string | null
	answerStatus?: string | null
	isCorrect?: boolean | null
	correctAnswer?: string | null
	analysis?: string | null
	annotations?: KyyyReadingAnnotationResponse[] | null
}

export interface KyyyReadingAnnotationResponse {
	id?: number | string | null
	contentType?: KyyyReadingAnnotationContentType | string | null
	startOffset?: number | string | null
	endOffset?: number | string | null
	selectedText?: string | null
	noteContent?: string | null
}

export interface KyyyReadingSummaryResponse {
	correctCount?: number | string | null
	wrongCount?: number | string | null
	accuracyRate?: number | string | null
	submittedAt?: string | null
}

export interface KyyyReadingSessionResponse {
	sessionId?: number | string | null
	status?: string | null
	passage?: KyyyReadingPassageResponse | null
	progress?: KyyyReadingProgressResponse | null
	questions?: KyyyReadingQuestionResponse[] | null
	summary?: KyyyReadingSummaryResponse | null
}

export interface KyyyReadingAnswerDraftRequest {
	answerContent: string
	usedSeconds: number
}

export interface KyyyReadingAnnotationCreateRequest {
	passageId: number
	questionId?: number | null
	contentType: KyyyReadingAnnotationContentType
	startOffset: number
	endOffset: number
	selectedText: string
	noteContent: string
}

export interface KyyyReadingAnnotationUpdateRequest {
	noteContent: string
}

export interface KyyyReadingTextToken {
	key: string
	text: string
	startOffset: number
	endOffset: number
	kind: KyyyReadingTextTokenKind
}

export interface KyyyReadingPassageState {
	id: number | null
	sourceYear: number
	sourceName: string
	passageNo: number
	title: string
	passageText: string
	examDirection: string
	examDirectionLabel: string
	annotations: KyyyReadingAnnotationState[]
	tokens: KyyyReadingTextToken[]
}

export interface KyyyReadingProgressState {
	totalQuestions: number
	answeredCount: number
	unansweredCount: number
	submitted: boolean
}

export interface KyyyReadingOptionState {
	optionKey: string
	optionContent: string
}

export interface KyyyReadingQuestionState {
	questionId: number
	questionNo: number
	stem: string
	stemTokens: KyyyReadingTextToken[]
	annotations: KyyyReadingAnnotationState[]
	options: KyyyReadingOptionState[]
	selectedOptionKey: string
	answerStatus: string
	isCorrect: boolean | null
	correctAnswer: string
	analysis: string
}

export interface KyyyReadingAnnotationState {
	id: number
	contentType: KyyyReadingAnnotationContentType
	startOffset: number
	endOffset: number
	selectedText: string
	noteContent: string
}

export interface KyyyReadingSummaryState {
	correctCount: number
	wrongCount: number
	accuracyRate: number
	submittedAt: string
}

export interface KyyyReadingSessionState {
	sessionId: number | null
	status: KyyyReadingSessionStatus
	passage: KyyyReadingPassageState | null
	progress: KyyyReadingProgressState
	questions: KyyyReadingQuestionState[]
	summary: KyyyReadingSummaryState | null
	loaded: boolean
	loading: boolean
	submitting: boolean
}

export interface KyyyReadingSelectionTarget {
	contentType: KyyyReadingAnnotationContentType
	questionId: number | null
}

export interface KyyyReadingPoint {
	x: number
	y: number
}

export interface KyyyReadingActionMenuState {
	visible: boolean
	mode: 'selection' | 'existing'
	left: number
	top: number
}

export interface KyyyReadingExplanationState {
	visible: boolean
	mode: 'create' | 'edit' | 'view'
	annotationId: number | null
	contentType: KyyyReadingAnnotationContentType
	questionId: number | null
	startOffset: number
	endOffset: number
	selectedText: string
	noteContent: string
	saving: boolean
}

export interface KyyyReadingTextTouchLikeEvent {
	detail?: {
		x?: number
		y?: number
	}
	changedTouches?: Array<{
		x?: number
		y?: number
	}>
}

export interface KyyyReadingPopupRef {
	open: () => void
	close: () => void
}

export interface KyyyReadingPageState {
	sessionState: KyyyReadingSessionState
	passageId: number | null
	targetQuestionId: number | null
	locatingQuestionId: number | null
	savingQuestionIds: Record<number, boolean>
	questionUsedSeconds: Record<number, number>
	questionTouchedAt: Record<number, number>
	sessionToken: string
	selectionMode: boolean
	selectionTarget: KyyyReadingSelectionTarget | null
	selectionAnchorToken: KyyyReadingTextToken | null
	previewRange: import('@/pages/kyyy/reading/annotation').KyyyReadingRangeState | null
	actionMenu: KyyyReadingActionMenuState
	activeAnnotation: KyyyReadingAnnotationState | null
	activeAnnotationTarget: KyyyReadingSelectionTarget | null
	explanationState: KyyyReadingExplanationState
}

export function createDefaultReadingExplanationState(): KyyyReadingExplanationState {
	return {
		visible: false,
		mode: 'create',
		annotationId: null,
		contentType: 'passage_text',
		questionId: null,
		startOffset: 0,
		endOffset: 0,
		selectedText: '',
		noteContent: '',
		saving: false
	}
}
