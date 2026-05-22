/**
 * @file KyyyReadingView
 * @project pipker-do
 * @module 考研英语 / 小程序阅读做题
 * @description 负责阅读会话响应的前端归一化与默认状态生成。
 * @logic 1. 规范会话状态与题目结构；2. 归一化篇章、进度与结果汇总；3. 提供页面安全默认值。
 * @dependencies Types: @/pages/kyyy/reading/types
 * @index_tags 考研英语, 阅读归一化, 会话状态, 页面视图
 * @author holic512
 */

import type {
	KyyyReadingAnnotationResponse,
	KyyyReadingAnnotationState,
	KyyyReadingOptionResponse,
	KyyyReadingOptionState,
	KyyyReadingPassageResponse,
	KyyyReadingPassageState,
	KyyyReadingProgressResponse,
	KyyyReadingProgressState,
	KyyyReadingQuestionResponse,
	KyyyReadingQuestionState,
	KyyyReadingSessionResponse,
	KyyyReadingSessionState,
	KyyyReadingSessionStatus,
	KyyyReadingSummaryResponse,
	KyyyReadingSummaryState
} from '@/pages/kyyy/reading/types'
import { tokenizeReadingText } from '@/pages/kyyy/reading/annotation'

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

function normalizeStatus(value: unknown): KyyyReadingSessionStatus {
	const text = normalizeText(value)
	if (text === 'active' || text === 'submitted' || text === 'empty') {
		return text
	}
	return 'empty'
}

function normalizeAnswerKey(value: unknown): string {
	const text = normalizeText(value).toUpperCase()
	return ['A', 'B', 'C', 'D'].includes(text) ? text : ''
}

function normalizeAnnotationContentType(value: unknown): 'passage_text' | 'question_stem' {
	return normalizeText(value) === 'question_stem' ? 'question_stem' : 'passage_text'
}

export function normalizeReadingAnnotation(result: KyyyReadingAnnotationResponse | null | undefined): KyyyReadingAnnotationState | null {
	const id = toNumber(result?.id)
	if (!id) {
		return null
	}
	const startOffset = Math.max(toNumber(result?.startOffset), 0)
	const endOffset = Math.max(toNumber(result?.endOffset), 0)
	if (endOffset <= startOffset) {
		return null
	}
	return {
		id,
		contentType: normalizeAnnotationContentType(result?.contentType),
		startOffset,
		endOffset,
		selectedText: normalizeText(result?.selectedText),
		noteContent: normalizeText(result?.noteContent)
	}
}

function normalizeAnnotations(result: KyyyReadingAnnotationResponse[] | null | undefined): KyyyReadingAnnotationState[] {
	return Array.isArray(result)
		? result.map((item) => normalizeReadingAnnotation(item)).filter((item): item is KyyyReadingAnnotationState => !!item)
		: []
}

function normalizeOptions(result: KyyyReadingOptionResponse[] | null | undefined): KyyyReadingOptionState[] {
	return Array.isArray(result)
		? result.map((item) => ({
			optionKey: normalizeAnswerKey(item?.optionKey),
			optionContent: normalizeText(item?.optionContent)
		})).filter((item) => !!item.optionKey && !!item.optionContent)
		: []
}

function normalizePassage(result: KyyyReadingPassageResponse | null | undefined): KyyyReadingPassageState | null {
	const id = result?.id === null || result?.id === undefined ? null : Number(result.id)
	if (!id || !Number.isFinite(id) || id <= 0) {
		return null
	}
	return {
		id,
		sourceYear: Math.max(toNumber(result?.sourceYear), 0),
		sourceName: normalizeText(result?.sourceName),
		passageNo: Math.max(toNumber(result?.passageNo), 0),
		title: normalizeText(result?.title),
		passageText: normalizeText(result?.passageText),
		examDirection: normalizeText(result?.examDirection),
		examDirectionLabel: normalizeText(result?.examDirectionLabel),
		annotations: normalizeAnnotations(result?.annotations),
		tokens: tokenizeReadingText(normalizeText(result?.passageText))
	}
}

function normalizeProgress(result: KyyyReadingProgressResponse | null | undefined): KyyyReadingProgressState {
	const totalQuestions = Math.max(toNumber(result?.totalQuestions), 0)
	const answeredCount = Math.max(toNumber(result?.answeredCount), 0)
	const unresolvedUnanswered = Math.max(toNumber(result?.unansweredCount), 0)
	return {
		totalQuestions,
		answeredCount,
		unansweredCount: Math.max(totalQuestions - answeredCount, unresolvedUnanswered, 0),
		submitted: Boolean(result?.submitted)
	}
}

function normalizeQuestions(result: KyyyReadingQuestionResponse[] | null | undefined): KyyyReadingQuestionState[] {
	return Array.isArray(result)
		? result.map((item, index) => {
			const questionId = toNumber(item?.questionId)
			if (!questionId) {
				return null
			}
			return {
				questionId,
				questionNo: Math.max(toNumber(item?.questionNo, index + 1), 1),
				stem: normalizeText(item?.stem),
				stemTokens: tokenizeReadingText(normalizeText(item?.stem)),
				annotations: normalizeAnnotations(item?.annotations),
				options: normalizeOptions(item?.options),
				selectedOptionKey: normalizeAnswerKey(item?.selectedOptionKey),
				answerStatus: normalizeText(item?.answerStatus),
				isCorrect: typeof item?.isCorrect === 'boolean' ? item.isCorrect : null,
				correctAnswer: normalizeAnswerKey(item?.correctAnswer),
				analysis: normalizeText(item?.analysis)
			}
		}).filter((item): item is KyyyReadingQuestionState => !!item)
		: []
}

function normalizeSummary(result: KyyyReadingSummaryResponse | null | undefined): KyyyReadingSummaryState | null {
	if (!result) {
		return null
	}
	return {
		correctCount: Math.max(toNumber(result.correctCount), 0),
		wrongCount: Math.max(toNumber(result.wrongCount), 0),
		accuracyRate: Math.max(toNumber(result.accuracyRate), 0),
		submittedAt: normalizeText(result.submittedAt)
	}
}

export function createEmptyReadingSession(): KyyyReadingSessionState {
	return {
		sessionId: null,
		status: 'empty',
		passage: null,
		progress: {
			totalQuestions: 0,
			answeredCount: 0,
			unansweredCount: 0,
			submitted: false
		},
		questions: [],
		summary: null,
		loaded: false,
		loading: false,
		submitting: false
	}
}

export function normalizeReadingSession(result: KyyyReadingSessionResponse | null | undefined): KyyyReadingSessionState {
	const status = normalizeStatus(result?.status)
	const questions = normalizeQuestions(result?.questions)
	const progress = normalizeProgress(result?.progress)
	return {
		sessionId: result?.sessionId === null || result?.sessionId === undefined ? null : Math.max(toNumber(result.sessionId), 0) || null,
		status,
		passage: normalizePassage(result?.passage),
		progress: {
			...progress,
			totalQuestions: Math.max(progress.totalQuestions, questions.length),
			unansweredCount: Math.max(Math.max(progress.totalQuestions, questions.length) - progress.answeredCount, progress.unansweredCount, 0),
			submitted: progress.submitted || status === 'submitted'
		},
		questions,
		summary: normalizeSummary(result?.summary),
		loaded: true,
		loading: false,
		submitting: false
	}
}
