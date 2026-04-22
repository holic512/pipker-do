import http from '@/shared/http/client'
import type { AdminCurrentUser, AdminLoginData, AdminProject } from '@/shared/types/admin'

export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminProfileUpdateRequest {
  displayName: string
  defaultProjectCode: string
}

export interface AdminPasswordUpdateRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export function loginByPassword(data: AdminLoginRequest): Promise<AdminLoginData> {
  return http.post('/admin/auth/login', data)
}

export function logoutAdmin(): Promise<void> {
  return http.post('/admin/auth/logout')
}

export function fetchAdminMe(): Promise<AdminCurrentUser> {
  return http.get('/admin/auth/me')
}

export function fetchAdminProjects(): Promise<AdminProject[]> {
  return http.get('/admin/auth/projects')
}

export function updateAdminProfile(data: AdminProfileUpdateRequest): Promise<AdminCurrentUser> {
  return http.put('/admin/auth/profile', data)
}

export function updateAdminPassword(data: AdminPasswordUpdateRequest): Promise<void> {
  return http.put('/admin/auth/password', data)
}
