import request from '@/shared/network/request'

export function logout() {
	return request({
		url: '/api/auth/logout',
		method: 'POST'
	})
}
