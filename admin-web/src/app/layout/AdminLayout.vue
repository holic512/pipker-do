<template>
  <div class="admin-layout">
    <aside class="admin-layout__sidebar">
      <div class="admin-layout__brand">
        <div class="admin-layout__brand-mark">P</div>
        <div>
          <div class="admin-layout__brand-title">Pipker Admin</div>
          <div class="admin-layout__brand-subtitle">后台统一工作台</div>
        </div>
      </div>

      <div class="admin-layout__sidebar-search">
        <el-input
          v-model="menuKeyword"
          placeholder="搜索菜单"
          clearable
          :prefix-icon="Search"
        />
      </div>

      <el-menu
        class="admin-layout__menu"
        :default-active="activeMenuPath"
        :default-openeds="defaultOpenSections"
        :router="true"
        :collapse-transition="false"
      >
        <el-sub-menu v-for="section in visibleMenuSections" :key="section.key" :index="section.key">
          <template #title>
            <span>{{ section.label }}</span>
          </template>
          <el-menu-item v-for="item in section.items" :key="item.key" :index="item.route">
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div v-if="!visibleMenuSections.length" class="admin-layout__menu-empty">
        没有匹配的菜单项
      </div>
    </aside>

    <div class="admin-layout__main">
      <header class="admin-layout__header">
        <div class="admin-layout__header-left">
          <div>
            <div class="admin-layout__header-title">后台管理中心</div>
            <div class="admin-layout__header-tip">统一切换项目工作域，保持管理体验一致</div>
          </div>
          <el-select
            v-model="projectCode"
            class="admin-layout__project-switch"
            placeholder="选择项目"
            @change="handleProjectChange"
          >
            <el-option
              v-for="project in sessionStore.availableProjects"
              :key="project.code"
              :label="project.name"
              :value="project.code"
            />
          </el-select>
        </div>

        <div class="admin-layout__header-right">
          <div class="admin-layout__theme-switch" aria-label="主题切换">
            <el-tooltip content="跟随系统" placement="bottom">
              <el-button
                class="admin-layout__theme-button"
                :type="themeMode === 'system' ? 'primary' : 'default'"
                plain
                circle
                @click="themeMode = 'system'"
              >
                <el-icon><Monitor /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="浅色主题" placement="bottom">
              <el-button
                class="admin-layout__theme-button"
                :type="themeMode === 'light' ? 'primary' : 'default'"
                plain
                circle
                @click="themeMode = 'light'"
              >
                <el-icon><Sunny /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="深色主题" placement="bottom">
              <el-button
                class="admin-layout__theme-button"
                :type="themeMode === 'dark' ? 'primary' : 'default'"
                plain
                circle
                @click="themeMode = 'dark'"
              >
                <el-icon><Moon /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          <div class="admin-layout__admin">
            <div class="admin-layout__admin-name">{{ sessionStore.adminUser?.displayName || '管理员' }}</div>
            <div class="admin-layout__admin-role">{{ adminRoles }}</div>
          </div>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="admin-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Monitor, Moon, Search, Sunny } from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { buildAdminMenus } from '@/modules/system/config/menu'
import { useAdminThemeStore } from '@/app/store/theme'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { AdminThemeMode } from '@/shared/utils/storage'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()
const themeStore = useAdminThemeStore()
const menuKeyword = ref('')

const projectCode = computed({
  get: () => sessionStore.currentProjectCode || '',
  set: (value: string) => sessionStore.switchProject(value)
})

const themeMode = computed({
  get: () => themeStore.mode,
  set: (value: AdminThemeMode) => themeStore.setThemeMode(value)
})
const menuSections = computed(() => buildAdminMenus(sessionStore.currentProjectCode))
const activeMenuPath = computed(() => route.path)
const adminRoles = computed(() => (sessionStore.adminUser?.roles || []).join(' / ') || '管理员')
const visibleMenuSections = computed(() => {
  const keyword = menuKeyword.value.trim().toLocaleLowerCase()
  if (!keyword) {
    return menuSections.value
  }

  return menuSections.value
    .map((section) => {
      const sectionMatched = section.label.toLocaleLowerCase().includes(keyword)
      const matchedItems = section.items.filter((item) => item.label.toLocaleLowerCase().includes(keyword))
      if (sectionMatched) {
        return {
          ...section,
          items: section.items
        }
      }

      return matchedItems.length > 0 ? { ...section, items: matchedItems } : null
    })
    .filter((section): section is NonNullable<typeof section> => !!section)
})
const defaultOpenSections = computed(() => visibleMenuSections.value.map((section) => section.key))

async function handleProjectChange(value: string) {
  sessionStore.switchProject(value)
  if (route.name === 'project-content') {
    await router.push(`/project/${value}/content`)
    return
  }
  await router.push(`/project/${value}/overview`)
}

async function handleLogout() {
  await sessionStore.logout()
  ElMessage.success('已退出后台登录')
  await router.replace('/login')
}
</script>
