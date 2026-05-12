/**
 * @file KyyyPracticeApi
 * @project pipker-do
 * @module 考研英语 / 小程序背词页
 * @description 封装背词设置、学习会话读取与学习反馈提交接口。
 * @logic 1. 读取背词设置；2. 读取或重建学习会话；3. 提交当前单词学习反馈。
 * @dependencies Shared: @/shared/network/request, Types: @/pages/kyyy/practice/types
 * @index_tags 考研英语, 背词接口, 学习会话, 反馈提交
 * @author holic512
 */

import request from '@/shared/network/request'
import type {
	KyyyPracticeFeedbackRequest,
	KyyyPracticeSessionMode,
	KyyyPracticeSessionResponse,
	KyyyPracticeSettingRequest,
	KyyyPracticeSettingResponse
} from '@/pages/kyyy/practice/types'

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

export function getPracticeSession(mode: KyyyPracticeSessionMode, freshAttempt = false): Promise<KyyyPracticeSessionResponse> {
	return request({
		url: '/api/kyyy/practice/session',
		method: 'GET',
		data: {
			mode,
			freshAttempt: freshAttempt ? 'true' : 'false'
		}
	})
}

export function submitPracticeFeedback(sessionId: number, data: KyyyPracticeFeedbackRequest): Promise<KyyyPracticeSessionResponse> {
	return request({
		url: `/api/kyyy/practice/session/${sessionId}/feedback`,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}
