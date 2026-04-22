<template>
  <PageContainer :title="`${projectName} · 项目工作区`" description="当前项目已经完成上下文切换，后续业务管理页面将在该工作域下扩展。">
    <div class="project-placeholder admin-card">
      <div class="project-placeholder__code">{{ projectCode }}</div>
      <div class="project-placeholder__title">{{ projectName }}</div>
      <p class="project-placeholder__desc">
        这里将继续挂载题库、内容、分类、题目、配置等项目级管理页面。请求层已默认带上当前项目上下文。
      </p>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'

const route = useRoute()
const sessionStore = useAdminSessionStore()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const projectName = computed(() => {
  return sessionStore.availableProjects.find((project) => project.code === projectCode.value)?.name || projectCode.value
})
</script>

<style scoped lang="scss">
.project-placeholder {
  padding: 30px;
}

.project-placeholder__code {
  color: var(--admin-primary);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.project-placeholder__title {
  margin-top: 14px;
  font-size: 32px;
  font-weight: 700;
}

.project-placeholder__desc {
  margin: 16px 0 0;
  max-width: 760px;
  color: var(--admin-text-soft);
  line-height: 1.9;
}
</style>
