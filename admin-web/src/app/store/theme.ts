import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { AdminThemeMode } from '@/shared/utils/storage'
import { getStoredThemeMode, setStoredThemeMode } from '@/shared/utils/storage'

type ResolvedTheme = 'light' | 'dark'

function resolveTheme(mode: AdminThemeMode, mediaQuery: MediaQueryList | null): ResolvedTheme {
  if (mode === 'system') {
    return mediaQuery?.matches ? 'dark' : 'light'
  }
  return mode
}

function applyTheme(theme: ResolvedTheme) {
  document.documentElement.dataset.theme = theme
  document.documentElement.style.colorScheme = theme
}

export const useAdminThemeStore = defineStore('admin-theme', () => {
  const mode = ref<AdminThemeMode>(getStoredThemeMode())
  const resolvedTheme = ref<ResolvedTheme>('light')
  const initialized = ref(false)
  let mediaQuery: MediaQueryList | null = null

  const isDark = computed(() => resolvedTheme.value === 'dark')

  function syncTheme() {
    resolvedTheme.value = resolveTheme(mode.value, mediaQuery)
    applyTheme(resolvedTheme.value)
  }

  function initializeTheme() {
    if (initialized.value) {
      syncTheme()
      return
    }

    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    syncTheme()
    mediaQuery.addEventListener('change', () => {
      if (mode.value === 'system') {
        syncTheme()
      }
    })
    initialized.value = true
  }

  function setThemeMode(nextMode: AdminThemeMode) {
    mode.value = nextMode
    setStoredThemeMode(nextMode)
    syncTheme()
  }

  return {
    mode,
    resolvedTheme,
    isDark,
    initializeTheme,
    setThemeMode
  }
})
