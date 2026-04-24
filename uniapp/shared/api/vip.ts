// AI 索引: 小程序用户端 VIP 状态与兑换码 API。

import request from '@/shared/network/request'

export interface VipRedeemPayload {
	key: string
}

export interface VipInfo {
	isVip?: boolean
	vip?: boolean
	vipType: string | null
	expireAt: string | null
}

export interface VipRedeemRecord {
	id: number
	userId: number
	vipType: string
	vipStatus: number
	sourceType: string
	sourceRefId: number | null
	amount: number
	startTime: string
	endTime: string
	invalidReason: string | null
	invalidAt: string | null
	invalidBy: number | null
	createdAt: string
	updatedAt: string
}

export function redeemVipKey(data: VipRedeemPayload): Promise<VipRedeemRecord> {
	return request<VipRedeemRecord>({
		url: '/api/user/vip/redeem',
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function getVipStatus(): Promise<VipInfo> {
	return request<VipInfo>({
		url: '/api/user/vip/status',
		method: 'GET'
	})
}
