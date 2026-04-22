import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'
import { useAdminSessionStore } from '@/modules/system/store/session'

export function installRouterGuards(router: Router, pinia: Pinia) {
  router.beforeEach(async (to) => {
    const sessionStore = useAdminSessionStore(pinia)

    if (sessionStore.token && !sessionStore.initialized) {
      try {
        await sessionStore.initializeSession()
      } catch (error) {
        sessionStore.resetSession()
      }
    }

    if (to.meta.requiresAuth && !sessionStore.isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    if (to.meta.guestOnly && sessionStore.isAuthenticated) {
      return sessionStore.currentProjectCode
        ? { path: `/project/${sessionStore.currentProjectCode}/overview` }
        : { path: '/workspace' }
    }

    if (to.params.projectCode) {
      const projectCode = String(to.params.projectCode)
      if (!sessionStore.availableProjects.some((project) => project.code === projectCode)) {
        if (sessionStore.currentProjectCode) {
          return { path: `/project/${sessionStore.currentProjectCode}/overview` }
        }
        return { path: '/workspace' }
      }
      if (projectCode !== sessionStore.currentProjectCode) {
        sessionStore.switchProject(projectCode)
      }
    }

    return true
  })
}
