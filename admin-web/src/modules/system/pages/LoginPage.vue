<template>
  <div class="login-page">
    <section class="login-page__hero">
      <div class="login-page__eyebrow">PIPKER ADMIN</div>
      <h1 class="login-page__title">统一后台工作台</h1>
      <p class="login-page__description">
        面向多项目运营与内容维护的管理前端。当前支持在同一 layout 中切换考研政治与考研数学工作域。
      </p>
      <div class="login-page__meta admin-card">
        <div>默认账号：<strong>admin</strong></div>
        <div>默认密码：<strong>Admin@123456</strong></div>
      </div>
    </section>

    <section class="login-page__panel admin-card">
      <div class="login-page__panel-head">
        <h2>管理员登录</h2>
        <p>使用独立后台账号进入工作台</p>
      </div>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入管理员账号" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
        </el-form-item>
        <el-button class="login-page__submit" type="primary" size="large" :loading="submitting" @click="handleLogin">
          登录后台
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminSessionStore } from '@/modules/system/store/session'

const router = useRouter()
const route = useRoute()
const sessionStore = useAdminSessionStore()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Admin@123456'
})

function resolveProjectEntry(projectCode: string | null) {
  if (projectCode === 'kyzz') {
    return `/project/${projectCode}/question-bank-categories`
  }
  return '/workspace'
}

async function handleLogin() {
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    await sessionStore.login(form)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirect) {
      await router.replace(redirect)
      return
    }
    await router.replace(resolveProjectEntry(sessionStore.currentProjectCode))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  padding: 36px;
  gap: 28px;
  background:
    radial-gradient(circle at left top, rgba(215, 226, 255, 0.95), transparent 30%),
    linear-gradient(160deg, #f7f9fb 0%, #eef2f4 100%);
}

.login-page__hero,
.login-page__panel {
  border-radius: 28px;
}

.login-page__hero {
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-page__eyebrow {
  color: var(--admin-primary);
  font-weight: 700;
  letter-spacing: 0.16em;
  font-size: 13px;
}

.login-page__title {
  margin: 18px 0 0;
  font-size: 54px;
  line-height: 1.08;
}

.login-page__description {
  max-width: 620px;
  margin: 20px 0 0;
  font-size: 18px;
  line-height: 1.8;
  color: var(--admin-text-soft);
}

.login-page__meta {
  margin-top: 28px;
  width: fit-content;
  padding: 20px 22px;
  display: grid;
  gap: 8px;
}

.login-page__panel {
  align-self: center;
  padding: 34px 32px;
  max-width: 520px;
}

.login-page__panel-head h2 {
  margin: 0;
  font-size: 28px;
}

.login-page__panel-head p {
  margin: 10px 0 24px;
  color: var(--admin-text-soft);
}

.login-page__submit {
  width: 100%;
  margin-top: 8px;
}
</style>
