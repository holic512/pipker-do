import { ADMIN_PROJECT_KEY, ADMIN_THEME_MODE_KEY, ADMIN_TOKEN_KEY } from '@/shared/constants/storage'

export function getAdminToken(): string {
  return localStorage.getItem(ADMIN_TOKEN_KEY) || ''
}

export function setAdminToken(token: string): void {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

export function clearAdminToken(): void {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

export function getStoredProjectCode(): string {
  return localStorage.getItem(ADMIN_PROJECT_KEY) || ''
}

export function setStoredProjectCode(projectCode: string): void {
  localStorage.setItem(ADMIN_PROJECT_KEY, projectCode)
}

export function clearStoredProjectCode(): void {
  localStorage.removeItem(ADMIN_PROJECT_KEY)
}

export type AdminThemeMode = 'light' | 'dark' | 'system'

export function getStoredThemeMode(): AdminThemeMode {
  const storedMode = localStorage.getItem(ADMIN_THEME_MODE_KEY)
  if (storedMode === 'light' || storedMode === 'dark' || storedMode === 'system') {
    return storedMode
  }
  return 'system'
}

export function setStoredThemeMode(mode: AdminThemeMode): void {
  localStorage.setItem(ADMIN_THEME_MODE_KEY, mode)
}
