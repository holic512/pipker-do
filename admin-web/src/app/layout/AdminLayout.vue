<template>
  <el-container class="admin-layout" :class="{ 'is-collapsed': isCollapse }">
    <!-- 左侧侧边栏 (动态宽度) -->
    <el-aside class="admin-layout__sidebar" :class="{ 'is-collapsed': isCollapse }">
      <!-- 品牌 Logo 区域 -->
      <div class="admin-layout__brand">
        <div class="admin-layout__brand-mark">P</div>
        <!-- 折叠时隐藏文字 -->
        <div v-show="!isCollapse" class="admin-layout__brand-info">
          <div class="admin-layout__brand-title">Pipker Admin</div>
          <div class="admin-layout__brand-subtitle">后台统一工作台</div>
        </div>
      </div>

      <!-- 搜索区域 (折叠时隐藏) -->
      <div v-show="!isCollapse" class="admin-layout__sidebar-search">
        <el-input
            v-model="menuKeyword"
            placeholder="搜索菜单..."
            clearable
            :prefix-icon="Search"
        />
      </div>

      <!-- 菜单区域 -->
      <el-scrollbar class="admin-layout__menu-wrap">
        <el-menu
            v-if="!isCollapse"
            class="admin-layout__menu"
            :default-active="activeMenuPath"
            :default-openeds="defaultOpenSections"
            :router="true"
        >
          <el-sub-menu v-for="section in visibleMenuSections" :key="section.key" :index="section.key">
            <template #title>
              <el-icon class="admin-layout__menu-icon" :style="buildMenuIconStyle(section.iconTone)">
                <component :is="section.icon" />
              </el-icon>
              <span>{{ section.label }}</span>
            </template>
            <el-menu-item v-for="item in section.items" :key="item.key" :index="item.route">
              <el-icon class="admin-layout__menu-icon" :style="buildMenuIconStyle(item.iconTone)">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>

        <div v-else class="admin-layout__collapsed-menu">
          <div
              v-for="section in visibleMenuSections"
              :key="section.key"
              class="admin-layout__collapsed-section"
          >
            <el-tooltip :content="section.label" placement="right">
              <div class="admin-layout__collapsed-section-mark" :style="buildMenuIconStyle(section.iconTone)">
                <el-icon size="14">
                  <component :is="section.icon" />
                </el-icon>
              </div>
            </el-tooltip>

            <el-tooltip
                v-for="item in section.items"
                :key="item.key"
                :content="item.label"
                placement="right"
            >
              <button
                  type="button"
                  class="admin-layout__collapsed-item"
                  :class="{ 'is-active': activeMenuPath === item.route }"
                  :style="buildMenuIconStyle(item.iconTone)"
                  @click="router.push(item.route)"
              >
                <el-icon size="16">
                  <component :is="item.icon" />
                </el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>

        <div v-if="!visibleMenuSections.length && !isCollapse" class="admin-layout__menu-empty">
          <el-empty description="没有匹配的菜单项" :image-size="60" />
        </div>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="admin-layout__main">
      <!-- 顶部导航条 -->
      <el-header class="admin-layout__header" height="60px">
        <div class="admin-layout__header-left">
          <!-- 菜单折叠/展开按钮 -->
          <div class="admin-action-btn" @click="toggleCollapse" title="切换菜单">
            <el-icon size="20">
              <component :is="isCollapse ? Expand : Fold" />
            </el-icon>
          </div>

          <el-divider direction="vertical" class="header-divider" />

          <div class="admin-layout__header-context">
            <span class="admin-layout__header-title">工作域</span>
            <el-divider direction="vertical" />
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
        </div>

        <div class="admin-layout__header-right">
          <!-- 主题切换 -->
          <el-dropdown trigger="click" @command="handleThemeChange">
            <div class="admin-action-btn" aria-label="主题切换">
              <el-icon size="18">
                <Monitor v-if="themeMode === 'system'" />
                <Sunny v-else-if="themeMode === 'light'" />
                <Moon v-else />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="system" :disabled="themeMode === 'system'">跟随系统</el-dropdown-item>
                <el-dropdown-item command="light" :disabled="themeMode === 'light'">浅色主题</el-dropdown-item>
                <el-dropdown-item command="dark" :disabled="themeMode === 'dark'">深色主题</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-divider direction="vertical" class="header-divider" />

          <!-- 用户信息与操作 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="admin-user-profile">
              <el-avatar :size="32" class="admin-user-avatar">
                {{ sessionStore.adminUser?.displayName?.charAt(0)?.toUpperCase() || 'A' }}
              </el-avatar>
              <div class="admin-user-info">
                <span class="admin-user-name">{{ sessionStore.adminUser?.displayName || '管理员' }}</span>
                <span class="admin-user-role">{{ adminRoles }}</span>
              </div>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided class="text-danger">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="admin-layout__content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { Monitor, Moon, Search, Sunny, ArrowDown, Fold, Expand } from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { buildAdminMenus } from '@/modules/system/config/menu'
import { useAdminThemeStore } from '@/app/store/theme'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { AdminThemeMode } from '@/shared/utils/storage'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()
const themeStore = useAdminThemeStore()

const menuKeyword = ref('')
const isCollapse = ref(false) // 菜单折叠状态

// 切换折叠状态
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function buildMenuIconStyle(iconTone?: string) {
  return iconTone ? { '--menu-icon-tone': iconTone } : undefined
}

const projectCode = computed({
  get: () => sessionStore.currentProjectCode || '',
  set: (value: string) => sessionStore.switchProject(value)
})

const themeMode = computed({
  get: () => themeStore.mode,
  set: (value: AdminThemeMode) => themeStore.setThemeMode(value)
})

const menuSections = computed(() => buildAdminMenus(sessionStore.currentProjectCode, sessionStore.adminUser?.roles || []))
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

// 折叠状态下默认不展开所有菜单，展开时再恢复
const defaultOpenSections = computed(() => {
  return isCollapse.value ? [] : visibleMenuSections.value.map((section) => section.key)
})

async function handleProjectChange(value: string) {
  sessionStore.switchProject(value)
  if (value === 'kyzz') {
    await router.push(`/project/${value}/question-banks`)
  } else {
    await router.push('/workspace')
  }
}

function handleThemeChange(mode: AdminThemeMode) {
  themeMode.value = mode
}

function handleUserCommand(command: string) {
  if (command === 'profile') {
    router.push('/system/profile')
    return
  }
  if (command === 'password') {
    router.push('/system/profile?tab=password')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前登录状态吗？', '退出提示', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      handleLogout()
    }).catch(() => {})
  }
}

async function handleLogout() {
  await sessionStore.logout()
  ElMessage.success('已退出后台登录')
  await router.replace('/login')
}
</script>
