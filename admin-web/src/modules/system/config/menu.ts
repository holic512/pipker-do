import type { RouteLocationRaw } from 'vue-router'

export interface MenuItem {
  key: string
  label: string
  route: RouteLocationRaw
  section: 'workspace' | 'system' | 'project'
}

export function buildAdminMenus(projectCode: string | null): MenuItem[] {
  const currentProjectCode = projectCode || 'kyzz'

  return [
    {
      key: 'workspace',
      label: '工作台',
      route: '/workspace',
      section: 'workspace'
    },
    {
      key: 'system-admins',
      label: '管理员与权限',
      route: '/system/admins',
      section: 'system'
    },
    {
      key: 'project-overview',
      label: '项目首页',
      route: `/project/${currentProjectCode}/overview`,
      section: 'project'
    },
    {
      key: 'project-placeholder',
      label: '业务管理',
      route: `/project/${currentProjectCode}/content`,
      section: 'project'
    }
  ]
}
