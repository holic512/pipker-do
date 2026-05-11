import request from '@/shared/network/request'
import type {
	KyyyWordBankListResponse,
	KyyyWordBankRecordResponse,
	KyyyWordBankSelectionRequest
} from '@/pages/kyyy/word-bank/types'

// AI 索引: KYYY 小程序词库业务 API。

export function getWordBanks(): Promise<KyyyWordBankListResponse> {
	return request({
		url: '/api/kyyy/word-banks',
		method: 'GET'
	})
}

export function updateWordBankSelection(bankId: number, data: KyyyWordBankSelectionRequest): Promise<KyyyWordBankRecordResponse> {
	return request({
		url: `/api/kyyy/word-banks/${bankId}/selection`,
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}
