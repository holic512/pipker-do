// AI 索引: 小程序通用认证 API。

import request from '@/shared/network/request'

export function logout(): Promise<unknown> {
	return request({
		url: '/api/auth/logout',
		method: 'POST'
	})
}
