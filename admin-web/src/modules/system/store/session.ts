import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { AdminCurrentUser, AdminProject } from '@/shared/types/admin'
import { clearAdminToken, clearStoredProjectCode, getAdminToken, getStoredProjectCode, setAdminToken, setStoredProjectCode } from '@/shared/utils/storage'
import { fetchAdminMe, fetchAdminProjects, loginByPassword, logoutAdmin } from '@/modules/system/api/auth'

function resolveProjectCode(admin: AdminCurrentUser | null, projects: AdminProject[], storedCode: string): string | null {
  if (storedCode && projects.some((project) => project.code === storedCode)) {
    return storedCode
  }
  if (admin?.defaultProjectCode && projects.some((project) => project.code === admin.defaultProjectCode)) {
    return admin.defaultProjectCode
  }
  return projects[0]?.code ?? null
}

export const useAdminSessionStore = defineStore('admin-session', () => {
  const token = ref<string>(getAdminToken())
  const adminUser = ref<AdminCurrentUser | null>(null)
  const availableProjects = ref<AdminProject[]>([])
  const currentProjectCode = ref<string | null>(getStoredProjectCode() || null)
  const initialized = ref(false)
  const initializingPromise = ref<Promise<void> | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const currentProject = computed(() => {
    return availableProjects.value.find((project) => project.code === currentProjectCode.value) || null
  })

  function applySession(nextToken: string, nextAdmin: AdminCurrentUser, projects: AdminProject[]) {
    token.value = nextToken
    adminUser.value = nextAdmin
    availableProjects.value = projects
    currentProjectCode.value = resolveProjectCode(nextAdmin, projects, getStoredProjectCode())
    setAdminToken(nextToken)
    if (currentProjectCode.value) {
      setStoredProjectCode(currentProjectCode.value)
    } else {
      clearStoredProjectCode()
    }
  }

  function resetSession() {
    token.value = ''
    adminUser.value = null
    availableProjects.value = []
    currentProjectCode.value = null
    initialized.value = false
    clearAdminToken()
    clearStoredProjectCode()
  }

  async function initializeSession(force = false) {
    if (!token.value) {
      resetSession()
      return
    }
    if (initialized.value && !force) {
      return
    }
    if (initializingPromise.value && !force) {
      return initializingPromise.value
    }

    initializingPromise.value = (async () => {
      const [me, projects] = await Promise.all([fetchAdminMe(), fetchAdminProjects()])
      adminUser.value = me
      availableProjects.value = projects
      currentProjectCode.value = resolveProjectCode(me, projects, getStoredProjectCode())
      initialized.value = true
      if (currentProjectCode.value) {
        setStoredProjectCode(currentProjectCode.value)
      }
    })().finally(() => {
      initializingPromise.value = null
    })

    return initializingPromise.value
  }

  async function login(payload: { username: string; password: string }) {
    const result = await loginByPassword(payload)
    const projects = result.admin.projects.length ? result.admin.projects : await fetchAdminProjects()
    applySession(result.token, result.admin, projects)
    initialized.value = true
  }

  async function logout() {
    try {
      if (token.value) {
        await logoutAdmin()
      }
    } finally {
      resetSession()
    }
  }

  function switchProject(projectCode: string) {
    if (!availableProjects.value.some((project) => project.code === projectCode)) {
      return
    }
    currentProjectCode.value = projectCode
    setStoredProjectCode(projectCode)
  }

  async function refreshCurrentUser() {
    await initializeSession(true)
  }

  return {
    token,
    adminUser,
    availableProjects,
    currentProjectCode,
    currentProject,
    initialized,
    isAuthenticated,
    initializeSession,
    refreshCurrentUser,
    login,
    logout,
    switchProject,
    resetSession
  }
})
