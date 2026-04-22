import http from '@/shared/http/client'
import type { AdminCurrentUser, AdminLoginData, AdminProject } from '@/shared/types/admin'

export interface AdminLoginRequest {
  username: string
  password: string
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
