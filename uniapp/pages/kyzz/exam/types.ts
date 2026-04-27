// AI 索引: KYZZ 小程序 VIP 考试类型约束。

import type { VipInfo } from '@/shared/api/vip'

export type KyzzExamType = 'choice' | 'short' | 'full'
export type KyzzExamDifficultyMode = 'balanced' | 'easy' | 'normal' | 'hard'
export type KyzzExamStatus = 'in_progress' | 'submitted' | 'expired'
export type KyzzExamQuestionType = 'single' | 'multiple' | 'short'

export interface KyzzExamPreset {
	examType: KyzzExamType
	title: string
	description: string
	defaultDurationMinutes: number | string | null
	singleCount: number | string | null
	multipleCount: number | string | null
	shortCount: number | string | null
}

export interface KyzzExamDifficultyOption {
	difficultyMode: KyzzExamDifficultyMode
	title: string
	description: string
}

export interface KyzzExamSummary {
	sessionId: number
	examNo: string
	examType: KyzzExamType | string
	examTypeLabel: string
	difficultyMode: KyzzExamDifficultyMode | string
	difficultyLabel: string
	durationMinutes: number | string | null
	totalQuestionCount: number | string | null
	answeredCount: number | string | null
	totalScore: number | string | null
	status: KyzzExamStatus | string
	statusLabel: string
	startedAt: string | null
	deadlineAt: string | null
	submittedAt: string | null
	remainingSeconds: number | string | null
}

export interface KyzzExamEntryResponse {
	vipInfo: VipInfo
	ongoingExam: KyzzExamSummary | null
	presets: KyzzExamPreset[]
	difficultyOptions: KyzzExamDifficultyOption[]
}

export interface KyzzExamStartRequest {
	examType: KyzzExamType
	difficultyMode: KyzzExamDifficultyMode
	durationMinutes: number
}

export interface KyzzExamQuestionOption {
	optionKey: string
	optionContent: string
}

export interface KyzzExamQuestion {
	id: number
	questionId: number
	bankId: number
	questionType: KyzzExamQuestionType | string
	questionOrder: number | string | null
	stem: string
	difficultyLevel: number | string | null
	score: number | string | null
	sourceName: string | null
	yearNo: number | string | null
	options: KyzzExamQuestionOption[]
	selectedOptionKeys: string[]
	answerText: string | null
	answerStatus: number | string | null
}

export interface KyzzExamDetailResponse {
	summary: KyzzExamSummary
	questions: KyzzExamQuestion[]
	canAnswer: boolean
	canSubmit: boolean
}

export interface KyzzExamAnswerSaveRequest {
	selectedOptionKeys?: string[]
	answerText?: string
	usedSeconds: number
}

export interface KyzzExamAnswerSaveResponse {
	sessionId: number
	questionId: number
	answeredCount: number | string | null
	totalQuestionCount: number | string | null
}

export interface UniPopupRef {
	open: () => void
	close: () => void
}
