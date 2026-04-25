// AI 索引: 小程序通用用户 API。

import request, { uploadFile } from '@/shared/network/request'

export type UserProfilePayload = Record<string, unknown>

export interface UploadAvatarResponse {
	url: string
	storageKey: string
}

export function getCurrentUser<TUser = unknown>(): Promise<TUser> {
	return request({
		url: '/api/user/me',
		method: 'GET'
	})
}

export function updateProfile<TUser = unknown>(data: UserProfilePayload): Promise<TUser> {
	return request({
		url: '/api/user/profile',
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function acceptUserAgreement<TUser = unknown>(): Promise<TUser> {
	return request({
		url: '/api/user/agreement/accept',
		method: 'PUT'
	})
}

export function uploadAvatar(filePath: string): Promise<UploadAvatarResponse> {
	return uploadFile<UploadAvatarResponse>({
		url: '/api/files/avatar',
		filePath,
		name: 'file'
	})
}
