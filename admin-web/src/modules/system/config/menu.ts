import type { Component } from 'vue'
import {
  Collection,
  DataAnalysis,
  EditPen,
  Guide,
  House,
  Lock,
  Management,
  Medal,
  PriceTag,
  Setting,
  Ticket,
  User,
  UserFilled
} from '@element-plus/icons-vue'

export interface MenuItem {
  key: string
  label: string
  route: string
  icon: Component
  iconTone?: string
  projectScoped?: boolean
  roles?: string[]
}

export interface MenuSection {
  key: 'workspace' | 'system' | 'project'
  label: string
  icon: Component
  iconTone?: string
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
      iconTone: '#3b82f6',
      items: [
        {
          key: 'workspace',
          label: '工作台',
          route: '/workspace',
          icon: DataAnalysis,
          iconTone: '#3b82f6'
        }
      ]
    },
    {
      key: 'system',
      label: '通用管理',
      icon: Setting,
      iconTone: '#64748b',
      items: [
        {
          key: 'system-profile',
          label: '个人资料',
          route: '/system/profile',
          icon: Lock,
          iconTone: '#0f766e'
        },
        {
          key: 'system-users',
          label: '用户VIP',
          route: '/system/users',
          icon: User,
          iconTone: '#2563eb',
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-vip-card-groups',
          label: '会员卡组',
          route: '/system/vip-card-groups',
          icon: Ticket,
          iconTone: '#0891b2',
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-vip-card-keys',
          label: '兑换Key',
          route: '/system/vip-card-keys',
          icon: PriceTag,
          iconTone: '#db2777',
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-admins',
          label: '管理员管理',
          route: '/system/admins',
          icon: UserFilled,
          iconTone: '#7c3aed',
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        },
        {
          key: 'system-admin-roles',
          label: '角色管理',
          route: '/system/admin-roles',
          icon: Medal,
          iconTone: '#d97706',
          roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN']
        }
      ]
    },
    {
      key: 'project',
      label: '业务管理',
      icon: Management,
      iconTone: '#475569',
      items: [
        ...(currentProjectCode === 'kyzz'
          ? [
              {
                key: 'project-kyzz-question-banks',
                label: '题库管理',
                route: `/project/${currentProjectCode}/question-banks`,
                icon: Collection,
                iconTone: '#2563eb',
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-questions',
                label: '题目管理',
                route: `/project/${currentProjectCode}/questions`,
                icon: EditPen,
                iconTone: '#16a34a',
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-question-tags',
                label: '标签管理',
                route: `/project/${currentProjectCode}/question-tags`,
                icon: PriceTag,
                iconTone: '#db2777',
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-question-bank-categories',
                label: '题库分类',
                route: `/project/${currentProjectCode}/question-bank-categories`,
                icon: Guide,
                iconTone: '#ea580c',
                projectScoped: true
              } satisfies MenuItem,
              {
                key: 'project-kyzz-user-question-banks',
                label: '用户题库选择',
                route: `/project/${currentProjectCode}/user-question-banks`,
                icon: Ticket,
                iconTone: '#0891b2',
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
