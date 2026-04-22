import request, { uploadFile } from '@/utils/request'

export function getCurrentUser() {
	return request({
		url: '/api/user/me',
		method: 'GET'
	})
}

export function updateProfile(data) {
	return request({
		url: '/api/user/profile',
		method: 'PUT',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function uploadAvatar(filePath) {
	return uploadFile({
		url: '/api/files/avatar',
		filePath,
		name: 'file'
	})
}
