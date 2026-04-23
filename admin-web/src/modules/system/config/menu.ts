import type { Component } from 'vue'
import {
  DataAnalysis,
  Document,
  FolderOpened,
  House,
  Lock,
  Medal,
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
        ...(currentProjectCode === 'kyzz'
          ? [
              {
                key: 'project-kyzz-question-banks',
                label: '题库管理',
                route: `/project/${currentProjectCode}/question-banks`,
                icon: Document,
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-questions',
                label: '题目管理',
                route: `/project/${currentProjectCode}/questions`,
                icon: Document,
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-question-tags',
                label: '标签管理',
                route: `/project/${currentProjectCode}/question-tags`,
                icon: Document,
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-question-bank-categories',
                label: '题库分类',
                route: `/project/${currentProjectCode}/question-bank-categories`,
                icon: Document,
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-user-question-banks',
                label: '用户题库选择',
                route: `/project/${currentProjectCode}/user-question-banks`,
                icon: Document,
                projectScoped: true
              } satisfies MenuItem
            ]
          : [])
      ]
    }
  ].map((section) => ({
    ...section,
    items: section.items.filter((item) => canAccess(item.roles, currentRoles))
  })).filter((section) => section.items.length > 0)
}
