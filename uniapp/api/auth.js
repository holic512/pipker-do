import request from '@/utils/request'

export function logout() {
	return request({
		url: '/api/auth/logout',
		method: 'POST'
	})
}
