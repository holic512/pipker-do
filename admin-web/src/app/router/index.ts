import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/modules/system/pages/LoginPage.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/',
      component: () => import('@/app/layout/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/workspace'
        },
        {
          path: '/workspace',
          name: 'workspace',
          component: () => import('@/modules/system/pages/WorkspacePage.vue')
        },
        {
          path: '/system/profile',
          name: 'system-profile',
          component: () => import('@/modules/system/pages/AdminProfilePage.vue')
        },
        {
          path: '/system/admin-roles',
          name: 'system-admin-roles',
          component: () => import('@/modules/system/pages/AdminRoleManagementPage.vue'),
          meta: { roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN'] }
        },
        {
          path: '/system/admins',
          name: 'system-admins',
          component: () => import('@/modules/system/pages/AdminManagementPage.vue'),
          meta: { roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN'] }
        },
        {
          path: '/system/users',
          name: 'system-users',
          component: () => import('@/modules/system/pages/UserManagementPage.vue'),
          meta: { roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN'] }
        },
        {
          path: '/project/:projectCode/overview',
          name: 'project-overview',
          component: () => import('@/modules/system/pages/ProjectPlaceholderPage.vue')
        },
        {
          path: '/project/:projectCode/content',
          name: 'project-content',
          component: () => import('@/modules/system/pages/ProjectPlaceholderPage.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/modules/system/pages/NotFoundPage.vue')
    }
  ]
})

export default router
