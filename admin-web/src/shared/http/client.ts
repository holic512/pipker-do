import axios from 'axios'
import router from '@/app/router'
import { getAdminToken } from '@/shared/utils/storage'
import { useAdminSessionStore } from '@/modules/system/store/session'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = getAdminToken()
  const sessionStore = useAdminSessionStore()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  if (sessionStore.currentProjectCode) {
    config.headers['X-Project-Code'] = sessionStore.currentProjectCode
  }

  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload === 'object' && 'code' in payload && payload.code !== 0) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload?.data ?? payload
  },
  async (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || '请求失败'

    if (status === 401) {
      const sessionStore = useAdminSessionStore()
      sessionStore.resetSession()
      if (router.currentRoute.value.path !== '/login') {
        await router.replace('/login')
      }
    }

    return Promise.reject(new Error(message))
  }
)

export default http
