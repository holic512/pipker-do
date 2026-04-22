<template>
  <div class="not-found">
    <div class="not-found__panel admin-card">
      <div class="not-found__code">404</div>
      <h1>页面不存在</h1>
      <p>当前访问的后台页面未找到，请返回工作台继续操作。</p>
      <el-button type="primary" @click="router.replace('/workspace')">返回工作台</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.not-found {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.not-found__panel {
  width: min(100%, 520px);
  text-align: center;
  padding: 40px 32px;
}

.not-found__code {
  color: var(--admin-primary);
  font-size: 54px;
  font-weight: 700;
}

.not-found__panel h1 {
  margin: 12px 0 0;
}

.not-found__panel p {
  margin: 14px 0 26px;
  color: var(--admin-text-soft);
}
</style>
