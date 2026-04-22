import type { Component } from 'vue'
import {
  DataAnalysis,
  Document,
  FolderOpened,
  House,
  Lock,
  Medal,
  Notebook,
  Setting,
  User,
  UserFilled
} from '@element-plus/icons-vue'

export interface MenuItem {
  key: string
  label: string
  route: string
  icon: Component
  projectScoped?: boolean
  roles?: string[]
}

export interface MenuSection {
  key: 'workspace' | 'system' | 'project'
  label: string
  icon: Component
  items: MenuItem[]
}

function canAccess(allowedRoles: string[] | undefined, currentRoles: string[]): boolean {
  if (!allowedRoles?.length) {
    return true
  }
  return allowedRoles.some((role) => currentRoles.includes(role))
}

export function buildAdminMenus(projectCode: string | null, currentRoles: string[] = []): MenuSection[] {
  const currentProjectCode = projectCode || 'kyzz'

  return [
    {
      key: 'workspace',
      label: '工作台',
      icon: House,
      items: [
        {
          key: 'workspace',
          label: '工作台',
          route: '/workspace',
          icon: DataAnalysis
        }
      ]
    },
    {
      key: 'system',
      label: '通用管理',
      icon: Setting,
      items: [
        {
          key: 'system-profile',
          label: '个人资料',
          route: '/system/profile',
          icon: Lock
        },
        {
          key: 'system-users',
          label: '用户管理',
          route: '/system/users',
          icon: User,
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-admins',
          label: '管理员管理',
          route: '/system/admins',
          icon: UserFilled,
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-admin-roles',
          label: '角色管理',
          route: '/system/admin-roles',
          icon: Medal,
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        }
      ]
    },
    {
      key: 'project',
      label: '业务管理',
      icon: FolderOpened,
      items: [
        {
          key: 'project-overview',
          label: '项目概览',
          route: `/project/${currentProjectCode}/overview`,
          icon: Notebook,
          projectScoped: true
        },
        {
          key: 'project-content',
          label: '业务内容',
          route: `/project/${currentProjectCode}/content`,
          icon: Document,
          projectScoped: true
        }
      ]
    }
  ].map((section) => ({
    ...section,
    items: section.items.filter((item) => canAccess(item.roles, currentRoles))
  })).filter((section) => section.items.length > 0)
}
