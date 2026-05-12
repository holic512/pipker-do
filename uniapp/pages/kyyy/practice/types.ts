/**
 * @file KyyyPracticeTypes
 * @project pipker-do
 * @module 考研英语 / 小程序背词页
 * @description 定义背词设置、学习会话、单词卡片与反馈请求的前端类型约束。
 * @logic 1. 约束设置接口结构；2. 约束学习会话响应与页面状态；3. 约束认识程度反馈参数。
 * @dependencies API: @/pages/kyyy/api/practice
 * @index_tags 考研英语, 小程序, 背词类型, 学习会话
 * @author holic512
 */

export type KyyyExamDirection = 'english_one' | 'english_two'

export type KyyyPracticeSessionMode = 'study' | 'review'

export type KyyyPracticeSessionStatus = 'active' | 'empty' | 'bank_required' | 'completed'

export type KyyyPracticeRating = 'know' | 'fuzzy' | 'unknown'

export interface KyyyPracticeSettingOption {
	value: KyyyExamDirection
	label: string
}

export interface KyyyPracticeSettingResponse {
	examDirection?: KyyyExamDirection | string | null
	examDirectionLabel?: string | null
	defaultWordBankId?: number | string | null
	defaultWordBankName?: string | null
	examDirectionOptions?: KyyyPracticeSettingOption[] | null
}

export interface KyyyPracticeSettingRequest {
	examDirection?: KyyyExamDirection
	defaultWordBankId?: number | null
}

export interface KyyyPracticeSettingState {
	examDirection: KyyyExamDirection
	examDirectionLabel: string
	defaultWordBankId: number | null
	defaultWordBankName: string
	examDirectionOptions: KyyyPracticeSettingOption[]
	loaded: boolean
	syncing: boolean
}

export interface KyyyPracticeSessionBankResponse {
	id?: number | string | null
	bankCode?: string | null
	bankName?: string | null
	subtitle?: string | null
}

export interface KyyyPracticeSessionProgressResponse {
	totalCards?: number | string | null
	completedCards?: number | string | null
	remainingCards?: number | string | null
	currentIndex?: number | string | null
	knownCount?: number | string | null
	fuzzyCount?: number | string | null
	unknownCount?: number | string | null
}

export interface KyyyPracticeRelatedWordResponse {
	id?: number | string | null
	relatedWordId?: number | string | null
	relatedWordText?: string | null
	meaningCn?: string | null
	relationType?: string | null
}

export interface KyyyPracticeWordExampleResponse {
	id?: number | string | null
	exampleSentence?: string | null
	exampleTranslation?: string | null
}

export interface KyyyPracticeWordSourceBankResponse {
	id?: number | string | null
	bankCode?: string | null
	bankName?: string | null
}

export interface KyyyPracticeCardResponse {
	wordId?: number | string | null
	wordText?: string | null
	phoneticUs?: string | null
	phoneticUk?: string | null
	partOfSpeech?: string | null
	meaningCn?: string | null
	exampleSentence?: string | null
	exampleTranslation?: string | null
	examples?: KyyyPracticeWordExampleResponse[] | null
	difficultyLevel?: number | string | null
	studyStatus?: string | null
	masteryLevel?: number | string | null
	memoryStage?: number | string | null
	learningStep?: number | string | null
	reviewCount?: number | string | null
	correctCount?: number | string | null
	wrongCount?: number | string | null
	lastResult?: string | null
	lastStudiedAt?: string | null
	nextReviewAt?: string | null
	sourceType?: string | null
	roundNo?: number | string | null
	queueOrder?: number | string | null
	sourceBanks?: KyyyPracticeWordSourceBankResponse[] | null
	relatedWords?: KyyyPracticeRelatedWordResponse[] | null
}

export interface KyyyPracticeSessionEmptyStateResponse {
	title?: string | null
	description?: string | null
	actionText?: string | null
	suggestedMode?: string | null
}

export interface KyyyPracticeSessionCompletionResponse {
	passedNewCount?: number | string | null
	dueSoonCount?: number | string | null
	unknownCount?: number | string | null
	primaryActionMode?: string | null
}

export interface KyyyPracticeSessionResponse {
	sessionId?: number | string | null
	mode?: string | null
	status?: string | null
	bank?: KyyyPracticeSessionBankResponse | null
	progressSummary?: KyyyPracticeSessionProgressResponse | null
	currentCard?: KyyyPracticeCardResponse | null
	emptyState?: KyyyPracticeSessionEmptyStateResponse | null
	completionSummary?: KyyyPracticeSessionCompletionResponse | null
}

export interface KyyyPracticeFeedbackRequest {
	wordId: number
	rating: KyyyPracticeRating
	revealed: boolean
	responseDurationMs: number
}

export interface KyyyPracticeSessionBankState {
	id: number | null
	bankCode: string
	bankName: string
	subtitle: string
}

export interface KyyyPracticeSessionProgressState {
	totalCards: number
	completedCards: number
	remainingCards: number
	currentIndex: number
	knownCount: number
	fuzzyCount: number
	unknownCount: number
}

export interface KyyyPracticeRelatedWordState {
	id: number | null
	relatedWordId: number | null
	relatedWordText: string
	meaningCn: string
	relationType: string
}

export interface KyyyPracticeWordExampleState {
	id: number | null
	exampleSentence: string
	exampleTranslation: string
}

export interface KyyyPracticeWordSourceBankState {
	id: number | null
	bankCode: string
	bankName: string
}

export interface KyyyPracticeCardState {
	wordId: number | null
	wordText: string
	phoneticUs: string
	phoneticUk: string
	partOfSpeech: string
	meaningCn: string
	exampleSentence: string
	exampleTranslation: string
	examples: KyyyPracticeWordExampleState[]
	difficultyLevel: number
	studyStatus: string
	masteryLevel: number
	memoryStage: number
	learningStep: number
	reviewCount: number
	correctCount: number
	wrongCount: number
	lastResult: string
	lastStudiedAt: string
	nextReviewAt: string
	sourceType: string
	roundNo: number
	queueOrder: number
	sourceBanks: KyyyPracticeWordSourceBankState[]
	relatedWords: KyyyPracticeRelatedWordState[]
}

export interface KyyyPracticeSessionEmptyState {
	title: string
	description: string
	actionText: string
	suggestedMode: KyyyPracticeSessionMode
}

export interface KyyyPracticeSessionCompletionState {
	passedNewCount: number
	dueSoonCount: number
	unknownCount: number
	primaryActionMode: KyyyPracticeSessionMode
}

export interface KyyyPracticeSessionState {
	sessionId: number | null
	mode: KyyyPracticeSessionMode
	status: KyyyPracticeSessionStatus
	bank: KyyyPracticeSessionBankState | null
	progressSummary: KyyyPracticeSessionProgressState
	currentCard: KyyyPracticeCardState | null
	emptyState: KyyyPracticeSessionEmptyState | null
	completionSummary: KyyyPracticeSessionCompletionState | null
	loaded: boolean
	loading: boolean
	submitting: boolean
	revealed: boolean
}
