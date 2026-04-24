// AI 索引: KYZZ 小程序收藏题目类型约束。

import type { KyzzPracticeQuestionType } from '@/pages/kyzz/practice/types'

export interface KyzzFavoriteQuestionRecordResponse {
	questionId: number
	bankId: number
	bankName: string
	questionType: KyzzPracticeQuestionType
	stem: string
	difficultyLevel: number | string | null
	favoriteAt: string | null
}

export interface KyzzFavoriteQuestionViewRecord extends Omit<KyzzFavoriteQuestionRecordResponse, 'difficultyLevel'> {
	difficultyLevel: number
}

export interface KyzzFavoriteQuestionResponse {
	totalCount: number | string | null
	records: KyzzFavoriteQuestionRecordResponse[]
}

export interface KyzzFavoriteQuestionState {
	totalCount: number
	records: KyzzFavoriteQuestionViewRecord[]
}

export interface KyzzFavoriteQuestionToggleResponse {
	questionId: number
	isFavorite: boolean
}

export interface SearchConfirmEvent {
	value?: string
	detail?: {
		value?: string | null
	}
}
