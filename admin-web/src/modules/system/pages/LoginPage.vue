<template>
  <div class="login-page">
    <section class="login-page__hero">
      <div class="login-page__brand" aria-label="Pipker Admin">
        <span class="login-page__brand-mark">P</span>
        <span class="login-page__brand-text">PIPKER ADMIN</span>
      </div>
      <h1 class="login-page__title">统一后台工作台</h1>
      <p class="login-page__description">
        面向多项目运营、内容维护与用户管理的后台系统，支持在统一工作台中切换不同业务域。
      </p>
      <div class="login-page__features" aria-label="后台能力">
        <div class="login-page__feature">
          <span>01</span>
          <strong>项目化管理</strong>
          <p>按项目上下文进入业务后台</p>
        </div>
        <div class="login-page__feature">
          <span>02</span>
          <strong>权限化入口</strong>
          <p>使用管理员账号安全登录</p>
        </div>
      </div>
    </section>

    <section class="login-page__panel admin-card">
      <div class="login-page__panel-head">
        <h2>管理员登录</h2>
        <p>请输入后台账号与密码进入工作台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="账号" prop="username">
          <el-input
            v-model.trim="form.username"
            autocomplete="username"
            clearable
            placeholder="请输入管理员账号"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            autocomplete="current-password"
            type="password"
            placeholder="请输入密码"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
          class="login-page__submit"
          native-type="submit"
          type="primary"
          size="large"
          :loading="submitting"
        >
          登录后台
        </el-button>
      </el-form>
      <p class="login-page__tip">账号由系统管理员分配，如无法登录请联系平台负责人。</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAdminSessionStore } from '@/modules/system/store/session'

const router = useRouter()
const route = useRoute()
const sessionStore = useAdminSessionStore()
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function resolveProjectEntry(projectCode: string | null) {
  if (projectCode === 'kyzz') {
    return `/project/${projectCode}/question-banks`
  }
  return '/workspace'
}

async function handleLogin() {
  if (submitting.value) {
    return
  }

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
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
  align-items: center;
  padding: 40px;
  gap: 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 18%, rgba(215, 226, 255, 0.9), transparent 28%),
    radial-gradient(circle at 84% 82%, rgba(238, 242, 243, 0.95), transparent 30%),
    linear-gradient(160deg, var(--admin-bg) 0%, var(--admin-surface-soft) 100%);
}

.login-page__hero,
.login-page__panel {
  position: relative;
  z-index: 1;
}

.login-page__hero {
  min-height: 560px;
  padding: 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-page__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
}

.login-page__brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: var(--admin-primary);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 14px 30px -18px var(--admin-primary);
}

.login-page__brand-text {
  color: var(--admin-primary);
  font-weight: 700;
  letter-spacing: 0.16em;
  font-size: 13px;
}

.login-page__title {
  margin: 28px 0 0;
  max-width: 660px;
  font-size: clamp(42px, 5vw, 64px);
  line-height: 1.08;
  letter-spacing: -0.04em;
  color: var(--admin-text);
}

.login-page__description {
  max-width: 620px;
  margin: 22px 0 0;
  font-size: 18px;
  line-height: 1.8;
  color: var(--admin-text-soft);
}

.login-page__features {
  margin-top: 40px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 220px));
  gap: 16px;
}

.login-page__feature {
  padding: 18px 20px;
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-md);
  background: color-mix(in srgb, var(--admin-surface) 86%, transparent);
  box-shadow: var(--admin-shadow);
}

.login-page__feature span {
  color: var(--admin-primary);
  font-size: 13px;
  font-weight: 800;
}

.login-page__feature strong {
  display: block;
  margin-top: 10px;
  color: var(--admin-text);
  font-size: 16px;
}

.login-page__feature p {
  margin: 8px 0 0;
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.6;
}

.login-page__panel {
  justify-self: center;
  width: min(100%, 460px);
  padding: 36px 34px 30px;
  max-width: 520px;
  border-radius: var(--admin-radius-lg);
  box-shadow: var(--admin-shadow);
}

.login-page__panel-head h2 {
  margin: 0;
  font-size: 28px;
}

.login-page__panel-head p {
  margin: 10px 0 24px;
  color: var(--admin-text-soft);
}

.login-page__panel :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--admin-text);
}

.login-page__submit {
  width: 100%;
  margin-top: 8px;
  font-weight: 700;
}

.login-page__tip {
  margin: 18px 0 0;
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
  text-align: center;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 28px;
    overflow: auto;
  }

  .login-page__hero {
    min-height: auto;
    padding: 24px 4px 0;
  }

  .login-page__panel {
    justify-self: stretch;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 20px;
    gap: 24px;
  }

  .login-page__description {
    font-size: 16px;
  }

  .login-page__features {
    grid-template-columns: 1fr;
    margin-top: 28px;
  }

  .login-page__panel {
    padding: 28px 22px 24px;
    border-radius: var(--admin-radius-md);
  }
}
</style>
