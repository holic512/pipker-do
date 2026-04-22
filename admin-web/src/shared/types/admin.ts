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
  lastLoginAt: string | null
}

export interface AdminRoleSummary {
  id: number
  roleCode: string
  roleName: string
  status: number
  adminCount: number
  description: string
  capabilities: string[]
  protectedRole: boolean
  createdAt: string
  updatedAt: string
}

export interface AdminAssignedRole {
  id: number
  roleCode: string
  roleName: string
  status: number
  protectedRole: boolean
}

export interface AdminManagedUser {
  id: number
  username: string
  displayName: string
  status: number
  defaultProjectCode: string | null
  defaultProjectName: string | null
  roles: AdminAssignedRole[]
  projects: AdminProject[]
  protectedAccount: boolean
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
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
