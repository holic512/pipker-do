import request from '@/shared/network/request'
import type {
	KyyyPracticeSettingRequest,
	KyyyPracticeSettingResponse
} from '@/pages/kyyy/practice/types'

// AI 索引: KYYY 小程序刷题业务 API。

export function getPracticeSettings(): Promise<KyyyPracticeSettingResponse> {
	return request({
		url: '/api/kyyy/practice/settings',
		method: 'GET'
	})
}

export function updatePracticeSettings(data: KyyyPracticeSettingRequest): Promise<KyyyPracticeSettingResponse> {
	return request({
		url: '/api/kyyy/practice/settings',
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}
