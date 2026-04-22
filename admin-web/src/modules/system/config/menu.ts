import type { Component } from 'vue'
import {
  DataAnalysis,
  Document,
  FolderOpened,
  House,
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
}

export interface MenuSection {
  key: 'workspace' | 'system' | 'project'
  label: string
  icon: Component
  items: MenuItem[]
}

export function buildAdminMenus(projectCode: string | null): MenuSection[] {
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
          key: 'system-users',
          label: '用户管理',
          route: '/system/users',
          icon: User
        },
        {
          key: 'system-admins',
          label: '管理员管理',
          route: '/system/admins',
          icon: UserFilled
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
  ]
}
