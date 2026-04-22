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
          path: '/project/:projectCode/question-banks',
          name: 'project-question-banks',
          component: () => import('@/modules/kyzz/pages/QuestionBankManagementPage.vue')
        },
        {
          path: '/project/:projectCode/questions',
          name: 'project-questions',
          component: () => import('@/modules/kyzz/pages/QuestionManagementPage.vue')
        },
        {
          path: '/project/:projectCode/question-tags',
          name: 'project-question-tags',
          component: () => import('@/modules/kyzz/pages/QuestionTagManagementPage.vue')
        },
        {
          path: '/project/:projectCode/question-bank-categories',
          name: 'project-question-bank-categories',
          component: () => import('@/modules/kyzz/pages/QuestionBankCategoryManagementPage.vue')
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
