import http from '@/shared/http/client'
import type { AdminRoleSummary } from '@/shared/types/admin'

export interface AdminRoleQuery {
  keyword?: string
  status?: number
}

export interface AdminRoleUpsertRequest {
  roleCode: string
  roleName: string
  status: number
}

export function fetchAdminRoles(params: AdminRoleQuery = {}): Promise<AdminRoleSummary[]> {
  return http.get('/admin/roles', { params })
}

export function createAdminRole(data: AdminRoleUpsertRequest): Promise<AdminRoleSummary> {
  return http.post('/admin/roles', data)
}

export function updateAdminRole(roleId: number, data: AdminRoleUpsertRequest): Promise<AdminRoleSummary> {
  return http.put(`/admin/roles/${roleId}`, data)
}

export function updateAdminRoleStatus(roleId: number, status: number): Promise<AdminRoleSummary> {
  return http.put(`/admin/roles/${roleId}/status`, { status })
}
