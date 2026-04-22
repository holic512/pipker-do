import http from '@/shared/http/client'
import type { AdminManagedUser, AdminProject } from '@/shared/types/admin'

export interface AdminUserQuery {
  keyword?: string
  status?: number
  roleCode?: string
  projectCode?: string
}

export interface AdminUserCreateRequest {
  username: string
  displayName: string
  password: string
  confirmPassword: string
  status: number
  defaultProjectCode: string
  roleIds: number[]
  projectCodes: string[]
}

export interface AdminUserBasicUpdateRequest {
  displayName: string
  defaultProjectCode: string
}

export interface AdminUserRoleAssignmentRequest {
  roleIds: number[]
}

export interface AdminUserProjectAssignmentRequest {
  projectCodes: string[]
  defaultProjectCode: string
}

export interface AdminUserPasswordResetRequest {
  password: string
  confirmPassword: string
}

export function fetchAdminUsers(params: AdminUserQuery = {}): Promise<AdminManagedUser[]> {
  return http.get('/admin/users', { params })
}

export function fetchAdminUserDetail(userId: number): Promise<AdminManagedUser> {
  return http.get(`/admin/users/${userId}`)
}

export function fetchAdminProjectCatalog(): Promise<AdminProject[]> {
  return http.get('/admin/users/project-catalog')
}

export function createAdminUser(data: AdminUserCreateRequest): Promise<AdminManagedUser> {
  return http.post('/admin/users', data)
}

export function updateAdminUserBasicInfo(userId: number, data: AdminUserBasicUpdateRequest): Promise<AdminManagedUser> {
  return http.put(`/admin/users/${userId}`, data)
}

export function updateAdminUserStatus(userId: number, status: number): Promise<AdminManagedUser> {
  return http.put(`/admin/users/${userId}/status`, { status })
}

export function updateAdminUserRoles(userId: number, data: AdminUserRoleAssignmentRequest): Promise<AdminManagedUser> {
  return http.put(`/admin/users/${userId}/roles`, data)
}

export function updateAdminUserProjects(userId: number, data: AdminUserProjectAssignmentRequest): Promise<AdminManagedUser> {
  return http.put(`/admin/users/${userId}/projects`, data)
}

export function resetAdminUserPassword(userId: number, data: AdminUserPasswordResetRequest): Promise<void> {
  return http.put(`/admin/users/${userId}/password/reset`, data)
}
