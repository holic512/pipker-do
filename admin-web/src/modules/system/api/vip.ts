import http from '@/shared/http/client'
import type {
  AdminVipRecord,
  AdminVipUser,
  VipCardGroup,
  VipCardKey,
  VipCardKeyBatch,
  VipCardKeyBatchCreateResponse,
  VipCardKeyBatchVoidResponse
} from '@/shared/types/admin'

export interface VipCardGroupPayload {
  groupName: string
  vipType: string
  durationDays: number
  status?: number
  remark?: string | null
}

export interface VipCardGroupQuery {
  keyword?: string
  status?: number
}

export interface VipCardKeyQuery {
  groupId?: number
  status?: number
  userId?: number
  keyword?: string
  batchNo?: string
}

export interface VipUserQuery {
  keyword?: string
  vipStatus?: number
}

export interface VipManualGrantPayload {
  vipType: string
  durationDays: number
  amount?: number
  remark?: string | null
}

export function fetchVipCardGroups(params: VipCardGroupQuery = {}) {
  return http.get<VipCardGroup[]>('/admin/vip/card-groups', { params })
}

export function createVipCardGroup(data: VipCardGroupPayload) {
  return http.post<VipCardGroup>('/admin/vip/card-groups', data)
}

export function updateVipCardGroup(groupId: number, data: VipCardGroupPayload) {
  return http.put<VipCardGroup>(`/admin/vip/card-groups/${groupId}`, data)
}

export function updateVipCardGroupStatus(groupId: number, status: number) {
  return http.put<VipCardGroup>(`/admin/vip/card-groups/${groupId}/status`, { status })
}

export function batchCreateVipCardKeys(groupId: number, count: number) {
  return http.post<VipCardKeyBatchCreateResponse>(`/admin/vip/card-groups/${groupId}/keys/batch`, { count })
}

export function fetchVipCardKeyBatches(groupId: number) {
  return http.get<VipCardKeyBatch[]>('/admin/vip/card-key-batches', { params: { groupId } })
}

export function voidVipCardKeyBatch(groupId: number, batchNo: string, reason: string) {
  return http.put<VipCardKeyBatchVoidResponse>(`/admin/vip/card-key-batches/${batchNo}/void`, { reason }, { params: { groupId } })
}

export function fetchVipCardKeys(params: VipCardKeyQuery = {}) {
  return http.get<VipCardKey[]>('/admin/vip/card-keys', { params })
}

export function voidVipCardKey(keyId: number, reason: string) {
  return http.put<VipCardKey>(`/admin/vip/card-keys/${keyId}/void`, { reason })
}

export function deleteVipCardKey(keyId: number) {
  return http.delete<void>(`/admin/vip/card-keys/${keyId}`)
}

export function fetchVipUsers(params: VipUserQuery = {}) {
  return http.get<AdminVipUser[]>('/admin/vip/users', { params })
}

export function fetchVipRecords(userId: number) {
  return http.get<AdminVipRecord[]>(`/admin/vip/users/${userId}/vip-records`)
}

export function grantVip(userId: number, data: VipManualGrantPayload) {
  return http.post<AdminVipRecord>(`/admin/vip/users/${userId}/vip`, data)
}

export function voidVipRecord(userId: number, vipId: number, reason: string) {
  return http.put<AdminVipRecord>(`/admin/vip/users/${userId}/vip/${vipId}/void`, { reason })
}
