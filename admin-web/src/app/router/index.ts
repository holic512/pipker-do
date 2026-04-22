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
          path: '/system/admins',
          name: 'system-admins',
          component: () => import('@/modules/system/pages/SystemPlaceholderPage.vue')
        },
        {
          path: '/system/users',
          name: 'system-users',
          component: () => import('@/modules/system/pages/UserManagementPage.vue')
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
