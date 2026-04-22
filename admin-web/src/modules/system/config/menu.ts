export interface MenuItem {
  key: string
  label: string
  route: string
  projectScoped?: boolean
}

export interface MenuSection {
  key: 'workspace' | 'system' | 'project'
  label: string
  items: MenuItem[]
}

export function buildAdminMenus(projectCode: string | null): MenuSection[] {
  const currentProjectCode = projectCode || 'kyzz'

  return [
    {
      key: 'workspace',
      label: '工作台',
      items: [
        {
          key: 'workspace',
          label: '工作台',
          route: '/workspace'
        }
      ]
    },
    {
      key: 'system',
      label: '通用管理',
      items: [
        {
          key: 'system-users',
          label: '用户管理',
          route: '/system/users'
        },
        {
          key: 'system-admins',
          label: '管理员管理',
          route: '/system/admins'
        }
      ]
    },
    {
      key: 'project',
      label: '业务管理',
      items: [
        {
          key: 'project-overview',
          label: '项目概览',
          route: `/project/${currentProjectCode}/overview`,
          projectScoped: true
        },
        {
          key: 'project-content',
          label: '业务内容',
          route: `/project/${currentProjectCode}/content`,
          projectScoped: true
        }
      ]
    }
  ]
}
