<template>
  <PageContainer title="个人资料" description="维护管理员展示信息、默认工作项目与登录密码，日常使用更顺手。">
    <template #actions>
      <el-radio-group v-model="activeTab" size="large">
        <el-radio-button label="profile">资料设置</el-radio-button>
        <el-radio-button label="password">修改密码</el-radio-button>
      </el-radio-group>
    </template>

    <section class="profile-page">
      <article class="profile-page__hero admin-card">
        <div class="profile-page__identity">
          <el-avatar :size="56" class="profile-page__avatar">
            {{ profileInitial }}
          </el-avatar>
          <div>
            <h2 class="profile-page__name">{{ sessionStore.adminUser?.displayName || '管理员' }}</h2>
            <p class="profile-page__role">{{ roleText }}</p>
            <p class="profile-page__hint">登录账号 {{ sessionStore.adminUser?.username || '-' }}，账号名当前暂不支持修改。</p>
          </div>
        </div>
        <div class="profile-page__metrics">
          <div class="profile-page__metric">
            <span class="profile-page__metric-label">当前项目</span>
            <strong>{{ sessionStore.currentProject?.name || '未设置' }}</strong>
          </div>
          <div class="profile-page__metric">
            <span class="profile-page__metric-label">默认项目</span>
            <strong>{{ defaultProjectName }}</strong>
          </div>
          <div class="profile-page__metric">
            <span class="profile-page__metric-label">最近登录</span>
            <strong>{{ lastLoginText }}</strong>
          </div>
        </div>
      </article>

      <el-alert
        v-if="activeTab === 'profile' && hasProfileChanges"
        type="info"
        :closable="false"
        show-icon
        title="你有未保存的资料修改，保存后新的昵称和默认项目会立刻生效。"
      />

      <el-alert
        v-if="activeTab === 'password'"
        type="warning"
        :closable="false"
        show-icon
        title="建议密码同时包含大写字母、小写字母、数字和符号，长度保持在 8 到 32 位。"
      />

      <div v-show="activeTab === 'profile'" class="profile-page__grid">
        <section class="profile-page__panel admin-card">
          <div class="profile-page__panel-head">
            <div>
              <h3>基础资料</h3>
              <p>展示名称会同步到后台头部与工作台，默认项目会影响登录后的第一工作域。</p>
            </div>
            <el-tag type="info" effect="plain">仅修改当前管理员</el-tag>
          </div>

          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top" class="profile-page__form">
            <el-form-item label="显示名称" prop="displayName">
              <el-input
                v-model="profileForm.displayName"
                maxlength="50"
                show-word-limit
                placeholder="例如：教务后台管理员"
              />
            </el-form-item>

            <el-form-item label="登录账号">
              <el-input :model-value="sessionStore.adminUser?.username || ''" disabled />
            </el-form-item>

            <el-form-item label="默认项目" prop="defaultProjectCode">
              <el-select v-model="profileForm.defaultProjectCode" placeholder="请选择默认项目">
                <el-option
                  v-for="project in sessionStore.availableProjects"
                  :key="project.code"
                  :label="project.name"
                  :value="project.code"
                />
              </el-select>
            </el-form-item>

            <div class="profile-page__actions">
              <el-button @click="resetProfileForm">重置</el-button>
              <el-button type="primary" :loading="savingProfile" @click="submitProfileForm">保存资料</el-button>
            </div>
          </el-form>
        </section>

        <section class="profile-page__panel admin-card">
          <div class="profile-page__panel-head">
            <div>
              <h3>使用建议</h3>
              <p>首版资料中心先聚焦常用项，避免后台账号维护过重。</p>
            </div>
          </div>

          <ul class="profile-page__tips">
            <li>显示名称建议使用真实职责或岗位名称，方便多人协作时快速识别。</li>
            <li>默认项目建议设置为你最常处理的业务域，登录后能少一次切换。</li>
            <li>如果你管理多个项目，右上角工作域切换不会受到这次保存影响。</li>
          </ul>
        </section>
      </div>

      <div v-show="activeTab === 'password'" class="profile-page__grid">
        <section class="profile-page__panel admin-card">
          <div class="profile-page__panel-head">
            <div>
              <h3>修改密码</h3>
              <p>修改后下次登录直接使用新密码，当前会话保持有效。</p>
            </div>
            <el-tag :type="passwordStrength.type" effect="light">{{ passwordStrength.label }}</el-tag>
          </div>

          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" class="profile-page__form">
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>

            <div class="profile-page__strength">
              <div class="profile-page__strength-bar">
                <span :style="{ width: `${passwordStrength.percent}%` }"></span>
              </div>
              <p>{{ passwordStrength.message }}</p>
            </div>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>

            <div class="profile-page__actions">
              <el-button @click="resetPasswordForm">清空</el-button>
              <el-button type="primary" :loading="savingPassword" @click="submitPasswordForm">更新密码</el-button>
            </div>
          </el-form>
        </section>

        <section class="profile-page__panel admin-card">
          <div class="profile-page__panel-head">
            <div>
              <h3>安全检查</h3>
              <p>提交前会自动检查常见风险，避免改完后不好记也不安全。</p>
            </div>
          </div>

          <ul class="profile-page__tips">
            <li :class="{ 'is-pass': passwordChecks.length }">长度 8 到 32 位</li>
            <li :class="{ 'is-pass': passwordChecks.upper }">包含大写字母</li>
            <li :class="{ 'is-pass': passwordChecks.lower }">包含小写字母</li>
            <li :class="{ 'is-pass': passwordChecks.number }">包含数字</li>
            <li :class="{ 'is-pass': passwordChecks.symbol }">包含符号</li>
            <li :class="{ 'is-pass': passwordChecks.confirmed }">两次输入保持一致</li>
          </ul>
        </section>
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { updateAdminPassword, updateAdminProfile } from '@/modules/system/api/auth'
import { useAdminSessionStore } from '@/modules/system/store/session'

type ProfileTab = 'profile' | 'password'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()

const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = reactive({
  displayName: '',
  defaultProjectCode: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const activeTab = computed<ProfileTab>({
  get: () => (route.query.tab === 'password' ? 'password' : 'profile'),
  set: (value) => {
    router.replace({
      path: '/system/profile',
      query: value === 'password' ? { tab: 'password' } : undefined
    })
  }
})

const profileRules: FormRules<typeof profileForm> = {
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { min: 2, max: 50, message: '显示名称长度保持在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  defaultProjectCode: [{ required: true, message: '请选择默认项目', trigger: 'change' }]
}

const passwordRules: FormRules<typeof passwordForm> = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback()
          return
        }
        if (value.length < 8 || value.length > 32) {
          callback(new Error('密码长度需保持在 8 到 32 位'))
          return
        }
        if ((sessionStore.adminUser?.username || '') && value.toLowerCase().includes((sessionStore.adminUser?.username || '').toLowerCase())) {
          callback(new Error('新密码不能包含账号名'))
          return
        }
        if (passwordStrength.value.score < 3) {
          callback(new Error('请至少覆盖大写字母、小写字母、数字、符号中的 3 类'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const roleText = computed(() => sessionStore.adminUser?.roles?.join(' / ') || '管理员')
const profileInitial = computed(() => sessionStore.adminUser?.displayName?.charAt(0)?.toUpperCase() || 'A')
const defaultProjectName = computed(() => {
  const code = sessionStore.adminUser?.defaultProjectCode
  return sessionStore.availableProjects.find((project) => project.code === code)?.name || '未设置'
})
const lastLoginText = computed(() => {
  const raw = sessionStore.adminUser?.lastLoginAt
  if (!raw) {
    return '首次登录后展示'
  }
  return raw.replace('T', ' ').slice(0, 16)
})

const hasProfileChanges = computed(() => {
  return profileForm.displayName !== (sessionStore.adminUser?.displayName || '')
    || profileForm.defaultProjectCode !== (sessionStore.adminUser?.defaultProjectCode || sessionStore.availableProjects[0]?.code || '')
})

const passwordChecks = computed(() => {
  const value = passwordForm.newPassword
  return {
    length: value.length >= 8 && value.length <= 32,
    upper: /[A-Z]/.test(value),
    lower: /[a-z]/.test(value),
    number: /\d/.test(value),
    symbol: /[^A-Za-z0-9]/.test(value),
    confirmed: !!value && value === passwordForm.confirmPassword
  }
})

const passwordStrength = computed(() => {
  const checks = passwordChecks.value
  const score = [checks.upper, checks.lower, checks.number, checks.symbol].filter(Boolean).length
  if (!passwordForm.newPassword) {
    return {
      score: 0,
      percent: 0,
      type: 'info' as const,
      label: '待设置',
      message: '输入新密码后，这里会给出强度和结构提示。'
    }
  }
  if (score <= 1) {
    return {
      score,
      percent: 25,
      type: 'danger' as const,
      label: '偏弱',
      message: '建议补充更多字符类型，避免只用单一规则。'
    }
  }
  if (score === 2) {
    return {
      score,
      percent: 60,
      type: 'warning' as const,
      label: '一般',
      message: '再增加一种字符类型，安全性会明显更稳。'
    }
  }
  if (score === 3) {
    return {
      score,
      percent: 82,
      type: 'success' as const,
      label: '良好',
      message: '结构已经合格，建议不要和旧密码过于接近。'
    }
  }
  return {
    score,
    percent: 100,
    type: 'success' as const,
    label: '很强',
    message: '结构完整，记得保管好新密码。'
  }
})

function syncProfileForm() {
  profileForm.displayName = sessionStore.adminUser?.displayName || ''
  profileForm.defaultProjectCode = sessionStore.adminUser?.defaultProjectCode || sessionStore.availableProjects[0]?.code || ''
}

function resetProfileForm() {
  syncProfileForm()
  profileFormRef.value?.clearValidate()
}

function resetPasswordForm() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

async function submitProfileForm() {
  await profileFormRef.value?.validate()
  savingProfile.value = true
  try {
    await updateAdminProfile({
      displayName: profileForm.displayName.trim(),
      defaultProjectCode: profileForm.defaultProjectCode
    })
    await sessionStore.refreshCurrentUser()
    syncProfileForm()
    ElMessage.success('个人资料已更新')
  } finally {
    savingProfile.value = false
  }
}

async function submitPasswordForm() {
  await passwordFormRef.value?.validate()
  savingPassword.value = true
  try {
    await updateAdminPassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    resetPasswordForm()
    activeTab.value = 'profile'
    ElMessage.success('密码修改成功，请使用新密码进行后续登录')
  } finally {
    savingPassword.value = false
  }
}

watch(
  () => [sessionStore.adminUser?.displayName, sessionStore.adminUser?.defaultProjectCode, sessionStore.availableProjects.length],
  () => {
    syncProfileForm()
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-page__hero,
.profile-page__panel {
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-md);
  background: var(--admin-surface);
  box-shadow: var(--admin-shadow);
}

.profile-page__hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background:
    linear-gradient(135deg, rgba(84, 94, 118, 0.12), rgba(84, 94, 118, 0.02)),
    var(--admin-surface);
}

.profile-page__identity {
  display: flex;
  align-items: center;
  gap: 18px;
}

.profile-page__avatar {
  background: var(--admin-primary);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.profile-page__name {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
}

.profile-page__role,
.profile-page__hint,
.profile-page__panel-head p,
.profile-page__strength p {
  margin: 6px 0 0;
  color: var(--admin-text-soft);
}

.profile-page__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 12px;
  min-width: 360px;
}

.profile-page__metric {
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.54);
  border: 1px solid var(--admin-border);
}

.profile-page__metric-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--admin-text-soft);
}

.profile-page__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 1fr);
  gap: 20px;
}

.profile-page__panel {
  padding: 24px;
}

.profile-page__panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.profile-page__panel-head h3 {
  margin: 0;
  font-size: 20px;
}

.profile-page__form :deep(.el-select),
.profile-page__form :deep(.el-input) {
  width: 100%;
}

.profile-page__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.profile-page__tips {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-page__tips li {
  position: relative;
  padding: 14px 16px 14px 40px;
  border-radius: 14px;
  background: var(--admin-surface-soft);
  color: var(--admin-text-soft);
}

.profile-page__tips li::before {
  content: '';
  position: absolute;
  left: 16px;
  top: 18px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c4ccd1;
}

.profile-page__tips li.is-pass {
  color: var(--admin-text);
}

.profile-page__tips li.is-pass::before {
  background: #67c23a;
}

.profile-page__strength {
  margin: -4px 0 20px;
}

.profile-page__strength-bar {
  overflow: hidden;
  height: 8px;
  border-radius: 999px;
  background: var(--admin-surface-soft);
}

.profile-page__strength-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #f56c6c, #e6a23c, #67c23a);
  transition: width 0.2s ease;
}

@media (max-width: 1080px) {
  .profile-page__hero,
  .profile-page__grid {
    grid-template-columns: 1fr;
  }

  .profile-page__hero {
    flex-direction: column;
  }

  .profile-page__metrics {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .profile-page__metrics {
    grid-template-columns: 1fr;
  }

  .profile-page__identity {
    align-items: flex-start;
  }

  .profile-page__actions,
  .profile-page__panel-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
