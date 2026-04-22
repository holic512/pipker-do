import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'
import { useAdminSessionStore } from '@/modules/system/store/session'

function resolveProjectEntry(projectCode: string | null): { path: string } {
  if (projectCode === 'kyzz') {
    return { path: `/project/${projectCode}/question-bank-categories` }
  }
  return { path: '/workspace' }
}

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
      return resolveProjectEntry(sessionStore.currentProjectCode)
    }

    const requiredRoles = Array.isArray(to.meta.roles) ? to.meta.roles.map(String) : []
    if (requiredRoles.length > 0) {
      const currentRoles = sessionStore.adminUser?.roles || []
      const hasRequiredRole = requiredRoles.some((role) => currentRoles.includes(role))
      if (!hasRequiredRole) {
        return { path: '/workspace' }
      }
    }

    if (to.params.projectCode) {
      const projectCode = String(to.params.projectCode)
      if (!sessionStore.availableProjects.some((project) => project.code === projectCode)) {
        if (sessionStore.currentProjectCode) {
          return resolveProjectEntry(sessionStore.currentProjectCode)
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
