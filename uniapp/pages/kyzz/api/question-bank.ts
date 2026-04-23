import request from '@/shared/network/request'
import type {
	KyzzQuestionBankMineResponse,
	KyzzQuestionBankPublicResponse,
	KyzzQuestionBankSelectionStatus,
	KyzzQuestionBankPublicRecordResponse
} from '@/pages/kyzz/question-bank/types'

// AI 索引: KYZZ 小程序题库业务 API。

type RequestParamValue = string | number | boolean | null | undefined

export interface KyzzQuestionBankPublicQuery {
	keyword?: string
	categoryId?: number | null
	difficultyLevel?: number | null
	selectionStatus?: KyzzQuestionBankSelectionStatus
}

function compactQuery(params: KyzzQuestionBankPublicQuery): Partial<KyzzQuestionBankPublicQuery> {
	const query: Record<string, RequestParamValue> = {}
	;(Object.keys(params) as Array<keyof KyzzQuestionBankPublicQuery>).forEach((key) => {
		const value = params[key]
		if (value !== undefined && value !== null && value !== '') {
			query[key] = value
		}
	})
	return query as Partial<KyzzQuestionBankPublicQuery>
}

export function getMineQuestionBanks(): Promise<KyzzQuestionBankMineResponse> {
	return request({
		url: '/api/kyzz/question-banks/mine',
		method: 'GET'
	})
}

export function getPublicQuestionBanks(params: KyzzQuestionBankPublicQuery = {}): Promise<KyzzQuestionBankPublicResponse> {
	return request({
		url: '/api/kyzz/question-banks/public',
		method: 'GET',
		data: compactQuery(params)
	})
}

export function updateQuestionBankSelection(bankId: number, selected: boolean): Promise<KyzzQuestionBankPublicRecordResponse> {
	return request({
		url: `/api/kyzz/question-banks/${bankId}/selection`,
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data: {
			selected
		}
	})
}
