<template>
  <PageContainer title="工作台" description="统一查看当前后台工作域、权限范围和后续业务入口。">
    <div class="workspace-grid">
      <section class="workspace-grid__hero admin-card">
        <div class="workspace-grid__hero-mark">当前项目</div>
        <h2>{{ currentProject?.name || '未选择项目' }}</h2>
        <p>{{ summaryText }}</p>
      </section>

      <section class="workspace-grid__card admin-card">
        <div class="workspace-grid__label">管理员</div>
        <div class="workspace-grid__value">{{ sessionStore.adminUser?.displayName || '未登录' }}</div>
        <div class="workspace-grid__hint">{{ sessionStore.adminUser?.roles?.join(' / ') || '暂无角色' }}</div>
      </section>

      <section class="workspace-grid__card admin-card">
        <div class="workspace-grid__label">项目权限</div>
        <div class="workspace-grid__value">{{ sessionStore.availableProjects.length }}</div>
        <div class="workspace-grid__hint">已接入项目数量</div>
      </section>

      <section class="workspace-grid__card admin-card">
        <div class="workspace-grid__label">后续扩展</div>
        <div class="workspace-grid__value">KYZZ / KYSX</div>
        <div class="workspace-grid__hint">统一壳下按项目扩业务页</div>
      </section>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'

const sessionStore = useAdminSessionStore()

const currentProject = computed(() => sessionStore.currentProject)
const summaryText = computed(() => {
  if (!currentProject.value) {
    return '请先选择一个项目工作域，再进入对应的业务模块。'
  }
  return `当前工作域已切换到 ${currentProject.value.name}，左侧菜单与后续模块都会跟随当前项目变化。`
})
</script>

<style scoped lang="scss">
.workspace-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.workspace-grid__hero {
  grid-column: span 3;
  padding: 30px;
  background:
    linear-gradient(135deg, rgba(84, 94, 118, 0.96), rgba(120, 132, 159, 0.88)),
    #545e76;
  color: white;
}

.workspace-grid__hero-mark {
  font-size: 13px;
  letter-spacing: 0.14em;
  opacity: 0.78;
}

.workspace-grid__hero h2 {
  margin: 14px 0 10px;
  font-size: 32px;
}

.workspace-grid__hero p {
  margin: 0;
  max-width: 780px;
  line-height: 1.8;
}

.workspace-grid__card {
  padding: 24px;
}

.workspace-grid__label {
  color: var(--admin-text-soft);
  font-size: 14px;
}

.workspace-grid__value {
  margin-top: 14px;
  font-size: 28px;
  font-weight: 700;
}

.workspace-grid__hint {
  margin-top: 12px;
  color: var(--admin-text-soft);
  font-size: 14px;
}
</style>
