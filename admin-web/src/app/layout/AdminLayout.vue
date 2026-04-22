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

      <el-menu
        class="admin-layout__menu"
        :default-active="activeMenuPath"
        :router="true"
      >
        <el-menu-item v-for="item in menuItems" :key="item.key" :index="String(item.route)">
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin-layout__main">
      <header class="admin-layout__header admin-card">
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { buildAdminMenus } from '@/modules/system/config/menu'
import { useAdminSessionStore } from '@/modules/system/store/session'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()

const projectCode = computed({
  get: () => sessionStore.currentProjectCode || '',
  set: (value: string) => sessionStore.switchProject(value)
})

const menuItems = computed(() => buildAdminMenus(sessionStore.currentProjectCode))
const activeMenuPath = computed(() => route.path)
const adminRoles = computed(() => (sessionStore.adminUser?.roles || []).join(' / ') || '管理员')

async function handleProjectChange(value: string) {
  sessionStore.switchProject(value)
  await router.push(`/project/${value}/overview`)
}

async function handleLogout() {
  await sessionStore.logout()
  ElMessage.success('已退出后台登录')
  await router.replace('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background:
    radial-gradient(circle at top left, rgba(215, 226, 255, 0.7), transparent 28%),
    linear-gradient(180deg, #f7f9fa 0%, #f2f5f6 100%);
}

.admin-layout__sidebar {
  padding: 24px 20px;
  background: rgba(255, 255, 255, 0.76);
  border-right: 1px solid rgba(227, 233, 236, 0.9);
  backdrop-filter: blur(14px);
}

.admin-layout__brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 30px;
}

.admin-layout__brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  background: linear-gradient(135deg, #545e76 0%, #8694b6 100%);
}

.admin-layout__brand-title {
  font-size: 16px;
  font-weight: 700;
}

.admin-layout__brand-subtitle {
  margin-top: 4px;
  color: var(--admin-text-soft);
  font-size: 13px;
}

.admin-layout__menu {
  border-right: 0;
  background: transparent;
}

.admin-layout__main {
  padding: 22px;
}

.admin-layout__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  margin-bottom: 20px;
}

.admin-layout__header-left,
.admin-layout__header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.admin-layout__header-title {
  font-size: 20px;
  font-weight: 700;
}

.admin-layout__header-tip {
  margin-top: 4px;
  font-size: 13px;
  color: var(--admin-text-soft);
}

.admin-layout__project-switch {
  width: 180px;
}

.admin-layout__admin {
  text-align: right;
}

.admin-layout__admin-name {
  font-weight: 700;
}

.admin-layout__admin-role {
  margin-top: 4px;
  color: var(--admin-text-soft);
  font-size: 13px;
}

.admin-layout__content {
  min-width: 0;
}
</style>
