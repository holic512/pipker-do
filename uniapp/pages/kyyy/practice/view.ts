/**
 * @file KyyyPracticeView
 * @project pipker-do
 * @module 考研英语 / 小程序背词页
 * @description 负责背词设置与学习会话响应的前端归一化处理。
 * @logic 1. 规范考试方向与会话模式；2. 将接口响应转换成页面安全状态；3. 生成默认空态与学习卡片状态。
 * @dependencies Types: @/pages/kyyy/practice/types
 * @index_tags 考研英语, 视图归一化, 背词会话, 页面状态
 * @author holic512
 */

import type {
	KyyyExamDirection,
	KyyyPracticeCardResponse,
	KyyyPracticeCardState,
	KyyyPracticeRelatedWordResponse,
	KyyyPracticeRelatedWordState,
	KyyyPracticeSessionBankResponse,
	KyyyPracticeSessionBankState,
	KyyyPracticeSessionCompletionResponse,
	KyyyPracticeSessionCompletionState,
	KyyyPracticeSessionEmptyState,
	KyyyPracticeSessionEmptyStateResponse,
	KyyyPracticeSessionMode,
	KyyyPracticeSessionProgressResponse,
	KyyyPracticeSessionProgressState,
	KyyyPracticeSessionResponse,
	KyyyPracticeSessionState,
	KyyyPracticeSessionStatus,
	KyyyPracticeSettingOption,
	KyyyPracticeSettingResponse,
	KyyyPracticeSettingState,
	KyyyPracticeWordExampleResponse,
	KyyyPracticeWordExampleState,
	KyyyPracticeWordSourceBankResponse,
	KyyyPracticeWordSourceBankState
} from '@/pages/kyyy/practice/types'

export const DEFAULT_KYYY_EXAM_DIRECTION: KyyyExamDirection = 'english_one'

export const DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS: KyyyPracticeSettingOption[] = [
	{ value: 'english_one', label: '英一' },
	{ value: 'english_two', label: '英二' }
]

export const DEFAULT_KYYY_PRACTICE_MODE: KyyyPracticeSessionMode = 'study'

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

export function normalizeExamDirection(value: unknown): KyyyExamDirection {
	if (typeof value !== 'string') {
		return DEFAULT_KYYY_EXAM_DIRECTION
	}
	const normalized = value.trim().toLowerCase().replace(/-/g, '_')
	if (['english_two', 'two', 'english2', 'english_2', 'english_ii', '英二', '英语二'].includes(normalized)) {
		return 'english_two'
	}
	return DEFAULT_KYYY_EXAM_DIRECTION
}

export function resolveExamDirectionLabel(value: KyyyExamDirection, options = DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS): string {
	return options.find((item) => item.value === value)?.label || (value === 'english_two' ? '英二' : '英一')
}

export function createDefaultPracticeSettings(): KyyyPracticeSettingState {
	return {
		examDirection: DEFAULT_KYYY_EXAM_DIRECTION,
		examDirectionLabel: resolveExamDirectionLabel(DEFAULT_KYYY_EXAM_DIRECTION),
		defaultWordBankId: null,
		defaultWordBankName: '',
		examDirectionOptions: DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS,
		loaded: false,
		syncing: false
	}
}

export function normalizePracticeSettings(result: KyyyPracticeSettingResponse | null | undefined): KyyyPracticeSettingState {
	const resultOptions = Array.isArray(result?.examDirectionOptions) ? result.examDirectionOptions : []
	const options = resultOptions.length
		? resultOptions.map((item) => ({
			value: normalizeExamDirection(item.value),
			label: item.label || resolveExamDirectionLabel(normalizeExamDirection(item.value))
		}))
		: DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS
	const examDirection = normalizeExamDirection(result?.examDirection)
	const defaultWordBankId = result?.defaultWordBankId === null || result?.defaultWordBankId === undefined
		? null
		: Number(result.defaultWordBankId)
	return {
		examDirection,
		examDirectionLabel: result?.examDirectionLabel || resolveExamDirectionLabel(examDirection, options),
		defaultWordBankId: defaultWordBankId !== null && Number.isFinite(defaultWordBankId) && defaultWordBankId > 0 ? defaultWordBankId : null,
		defaultWordBankName: typeof result?.defaultWordBankName === 'string' ? result.defaultWordBankName.trim() : '',
		examDirectionOptions: options,
		loaded: true,
		syncing: false
	}
}

export function normalizePracticeMode(value: unknown): KyyyPracticeSessionMode {
	return value === 'review' ? 'review' : DEFAULT_KYYY_PRACTICE_MODE
}

export function normalizePracticeStatus(value: unknown): KyyyPracticeSessionStatus {
	return ['active', 'empty', 'bank_required', 'completed'].includes(String(value || '').trim())
		? (String(value).trim() as KyyyPracticeSessionStatus)
		: 'empty'
}

function normalizeSessionBank(result: KyyyPracticeSessionBankResponse | null | undefined): KyyyPracticeSessionBankState | null {
	const id = result?.id === null || result?.id === undefined ? null : Number(result.id)
	if (!id || !Number.isFinite(id) || id <= 0) {
		return null
	}
	return {
		id,
		bankCode: normalizeText(result?.bankCode),
		bankName: normalizeText(result?.bankName),
		subtitle: normalizeText(result?.subtitle)
	}
}

function createDefaultProgressSummary(): KyyyPracticeSessionProgressState {
	return {
		totalCards: 20,
		completedCards: 0,
		remainingCards: 0,
		currentIndex: 0,
		knownCount: 0,
		fuzzyCount: 0,
		unknownCount: 0
	}
}

function normalizeProgressSummary(result: KyyyPracticeSessionProgressResponse | null | undefined): KyyyPracticeSessionProgressState {
	return {
		totalCards: Math.max(toNumber(result?.totalCards, 20), 0),
		completedCards: Math.max(toNumber(result?.completedCards), 0),
		remainingCards: Math.max(toNumber(result?.remainingCards), 0),
		currentIndex: Math.max(toNumber(result?.currentIndex), 0),
		knownCount: Math.max(toNumber(result?.knownCount), 0),
		fuzzyCount: Math.max(toNumber(result?.fuzzyCount), 0),
		unknownCount: Math.max(toNumber(result?.unknownCount), 0)
	}
}

function normalizeSourceBanks(result: KyyyPracticeWordSourceBankResponse[] | null | undefined): KyyyPracticeWordSourceBankState[] {
	return Array.isArray(result)
		? result.map((item) => ({
			id: item?.id === null || item?.id === undefined ? null : Math.max(toNumber(item.id), 0) || null,
			bankCode: normalizeText(item?.bankCode),
			bankName: normalizeText(item?.bankName)
		})).filter((item) => !!item.id)
		: []
}

function normalizeRelatedWords(result: KyyyPracticeRelatedWordResponse[] | null | undefined): KyyyPracticeRelatedWordState[] {
	return Array.isArray(result)
		? result.map((item) => ({
			id: item?.id === null || item?.id === undefined ? null : Math.max(toNumber(item.id), 0) || null,
			relatedWordId: item?.relatedWordId === null || item?.relatedWordId === undefined ? null : Math.max(toNumber(item.relatedWordId), 0) || null,
			relatedWordText: normalizeText(item?.relatedWordText),
			meaningCn: normalizeText(item?.meaningCn),
			relationType: normalizeText(item?.relationType)
		})).filter((item) => !!item.relatedWordText)
		: []
}

function normalizeWordExamples(result: KyyyPracticeWordExampleResponse[] | null | undefined,
	fallbackSentence: unknown,
	fallbackTranslation: unknown): KyyyPracticeWordExampleState[] {
	const examples = Array.isArray(result)
		? result.map((item) => ({
			id: item?.id === null || item?.id === undefined ? null : Math.max(toNumber(item.id), 0) || null,
			exampleSentence: normalizeText(item?.exampleSentence),
			exampleTranslation: normalizeText(item?.exampleTranslation)
		})).filter((item) => !!item.exampleSentence)
		: []
	if (examples.length) {
		return examples
	}
	const sentence = normalizeText(fallbackSentence)
	if (!sentence) {
		return []
	}
	return [{
		id: null,
		exampleSentence: sentence,
		exampleTranslation: normalizeText(fallbackTranslation)
	}]
}

function normalizeCard(result: KyyyPracticeCardResponse | null | undefined): KyyyPracticeCardState | null {
	const wordId = result?.wordId === null || result?.wordId === undefined ? null : Number(result.wordId)
	if (!wordId || !Number.isFinite(wordId) || wordId <= 0) {
		return null
	}
	return {
		wordId,
		wordText: normalizeText(result?.wordText),
		phoneticUs: normalizeText(result?.phoneticUs),
		phoneticUk: normalizeText(result?.phoneticUk),
		partOfSpeech: normalizeText(result?.partOfSpeech),
		meaningCn: normalizeText(result?.meaningCn),
		exampleSentence: normalizeText(result?.exampleSentence),
		exampleTranslation: normalizeText(result?.exampleTranslation),
		examples: normalizeWordExamples(result?.examples, result?.exampleSentence, result?.exampleTranslation),
		difficultyLevel: Math.max(toNumber(result?.difficultyLevel, 1), 0),
		studyStatus: normalizeText(result?.studyStatus),
		masteryLevel: Math.max(toNumber(result?.masteryLevel), 0),
		memoryStage: Math.max(toNumber(result?.memoryStage), 0),
		learningStep: Math.max(toNumber(result?.learningStep), 0),
		reviewCount: Math.max(toNumber(result?.reviewCount), 0),
		correctCount: Math.max(toNumber(result?.correctCount), 0),
		wrongCount: Math.max(toNumber(result?.wrongCount), 0),
		lastResult: normalizeText(result?.lastResult),
		lastStudiedAt: normalizeText(result?.lastStudiedAt),
		nextReviewAt: normalizeText(result?.nextReviewAt),
		sourceType: normalizeText(result?.sourceType),
		roundNo: Math.max(toNumber(result?.roundNo, 1), 1),
		queueOrder: Math.max(toNumber(result?.queueOrder), 0),
		sourceBanks: normalizeSourceBanks(result?.sourceBanks),
		relatedWords: normalizeRelatedWords(result?.relatedWords)
	}
}

function normalizeEmptyState(result: KyyyPracticeSessionEmptyStateResponse | null | undefined): KyyyPracticeSessionEmptyState | null {
	if (!result) {
		return null
	}
	return {
		title: normalizeText(result.title) || '当前暂无可学习内容',
		description: normalizeText(result.description) || '稍后再回来看看。',
		actionText: normalizeText(result.actionText) || '返回首页',
		suggestedMode: normalizePracticeMode(result.suggestedMode)
	}
}

function normalizeCompletionSummary(result: KyyyPracticeSessionCompletionResponse | null | undefined): KyyyPracticeSessionCompletionState | null {
	if (!result) {
		return null
	}
	return {
		passedNewCount: Math.max(toNumber(result.passedNewCount), 0),
		dueSoonCount: Math.max(toNumber(result.dueSoonCount), 0),
		unknownCount: Math.max(toNumber(result.unknownCount), 0),
		primaryActionMode: normalizePracticeMode(result.primaryActionMode)
	}
}

export function createEmptyPracticeSession(mode: KyyyPracticeSessionMode = DEFAULT_KYYY_PRACTICE_MODE): KyyyPracticeSessionState {
	return {
		sessionId: null,
		mode,
		status: 'empty',
		bank: null,
		progressSummary: createDefaultProgressSummary(),
		currentCard: null,
		emptyState: null,
		completionSummary: null,
		loaded: false,
		loading: false,
		submitting: false,
		revealed: false
	}
}

export function normalizePracticeSession(result: KyyyPracticeSessionResponse | null | undefined,
	fallbackMode: KyyyPracticeSessionMode = DEFAULT_KYYY_PRACTICE_MODE): KyyyPracticeSessionState {
	const mode = normalizePracticeMode(result?.mode || fallbackMode)
	return {
		sessionId: result?.sessionId === null || result?.sessionId === undefined ? null : Math.max(toNumber(result.sessionId), 0) || null,
		mode,
		status: normalizePracticeStatus(result?.status),
		bank: normalizeSessionBank(result?.bank),
		progressSummary: normalizeProgressSummary(result?.progressSummary),
		currentCard: normalizeCard(result?.currentCard),
		emptyState: normalizeEmptyState(result?.emptyState),
		completionSummary: normalizeCompletionSummary(result?.completionSummary),
		loaded: true,
		loading: false,
		submitting: false,
		revealed: false
	}
}
