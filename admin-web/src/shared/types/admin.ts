export interface AdminProject {
  code: string
  name: string
  enabled: boolean
}

export interface AdminCurrentUser {
  id: number
  username: string
  displayName: string
  roles: string[]
  projects: AdminProject[]
  defaultProjectCode: string | null
}

export interface AdminLoginData {
  token: string
  admin: AdminCurrentUser
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
  timestamp: string
}

export interface AdminSessionState {
  token: string
  adminUser: AdminCurrentUser | null
  currentProjectCode: string | null
  availableProjects: AdminProject[]
}
