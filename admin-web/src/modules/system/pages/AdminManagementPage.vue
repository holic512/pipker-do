<template>
  <PageContainer title="管理员管理" description="统一维护后台管理员账号、角色分配、项目授权与安全操作。">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增管理员</el-button>
    </template>

    <section class="admin-management-page">
      <article class="admin-management-page__hero admin-card">
        <div>
          <div class="admin-management-page__hero-mark">账号治理</div>
          <h2>先收口管理员边界，再放开业务权限</h2>
          <p>这页负责后台管理员账号的全生命周期管理。首版先聚焦账号、角色、项目和安全四个核心面，避免把权限体系做得过重。</p>
        </div>
        <div class="admin-management-page__hero-stats">
          <div class="admin-management-page__metric">
            <span>管理员总数</span>
            <strong>{{ adminStats.total }}</strong>
          </div>
          <div class="admin-management-page__metric">
            <span>启用账号</span>
            <strong>{{ adminStats.enabled }}</strong>
          </div>
          <div class="admin-management-page__metric">
            <span>受保护账号</span>
            <strong>{{ adminStats.protected }}</strong>
          </div>
          <div class="admin-management-page__metric">
            <span>项目授权数</span>
            <strong>{{ adminStats.projectAssignments }}</strong>
          </div>
        </div>
      </article>

      <section class="admin-management-page__filters admin-card">
        <el-input
          v-model="filters.keyword"
          class="admin-management-page__search"
          clearable
          placeholder="搜索账号或显示名称"
          @keyup.enter="loadAdmins"
          @clear="loadAdmins"
        />
        <el-select v-model="filters.status" clearable placeholder="全部状态" @change="loadAdmins">
          <el-option label="启用中" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
        <el-select v-model="filters.roleCode" clearable placeholder="全部角色" @change="loadAdmins">
          <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.roleCode" />
        </el-select>
        <el-select v-model="filters.projectCode" clearable placeholder="全部项目" @change="loadAdmins">
          <el-option v-for="project in projectOptions" :key="project.code" :label="project.name" :value="project.code" />
        </el-select>
        <el-button @click="loadAdmins">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </section>

      <section class="admin-management-page__table admin-card">
        <el-table v-loading="loading" :data="admins" stripe>
          <el-table-column label="管理员" min-width="260">
            <template #default="{ row }">
              <div class="admin-management-page__account">
                <div class="admin-management-page__account-head">
                  <strong>{{ row.displayName }}</strong>
                  <el-tag v-if="row.protectedAccount" type="danger" effect="light">受保护</el-tag>
                </div>
                <div class="admin-management-page__account-sub">{{ row.username }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="角色" min-width="280">
            <template #default="{ row }">
              <div class="admin-management-page__tag-group">
                <el-tag
                  v-for="role in row.roles"
                  :key="`${row.id}-${role.id}`"
                  :type="role.status === 1 ? 'primary' : 'info'"
                  effect="plain"
                  round
                >
                  {{ role.roleName }}
                </el-tag>
                <span v-if="!row.roles.length" class="admin-management-page__muted">未分配角色</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="项目权限" min-width="240">
            <template #default="{ row }">
              <div class="admin-management-page__tag-group">
                <el-tag v-for="project in row.projects" :key="`${row.id}-${project.code}`" effect="plain" round>
                  {{ project.name }}
                </el-tag>
                <span v-if="!row.projects.length" class="admin-management-page__muted">未配置项目</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="默认项目" width="130">
            <template #default="{ row }">
              {{ row.defaultProjectName || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
                {{ row.status === 1 ? '启用中' : '已停用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="最近登录" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.lastLoginAt, '暂无记录') }}
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="admin-management-page__actions">
                <el-button link type="primary" @click="openAdminDrawer(row.id)">详情</el-button>
                <el-button
                  link
                  :type="row.status === 1 ? 'warning' : 'success'"
                  :disabled="isStatusActionDisabled(row)"
                  :loading="togglingUserId === row.id"
                  @click="toggleUserStatus(row)"
                >
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </section>

    <el-dialog v-model="createDialogVisible" title="新增管理员" width="720px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <div class="admin-management-page__dialog-grid">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="createForm.username" maxlength="50" placeholder="例如 admin_kyzz" />
          </el-form-item>
          <el-form-item label="显示名称" prop="displayName">
            <el-input v-model="createForm.displayName" maxlength="50" placeholder="例如 考研政治运营管理员" />
          </el-form-item>
          <el-form-item label="初始密码" prop="password">
            <el-input v-model="createForm.password" type="password" show-password placeholder="请输入初始密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="createForm.confirmPassword" type="password" show-password placeholder="请再次输入初始密码" />
          </el-form-item>
        </div>

        <el-form-item label="账号状态" prop="status">
          <el-radio-group v-model="createForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="初始角色" prop="roleIds">
          <el-select v-model="createForm.roleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色">
            <el-option
              v-for="role in assignableRoleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目授权" prop="projectCodes">
          <el-select v-model="createForm.projectCodes" multiple collapse-tags collapse-tags-tooltip placeholder="请选择项目">
            <el-option
              v-for="project in projectOptions"
              :key="project.code"
              :label="project.name"
              :value="project.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="默认项目" prop="defaultProjectCode">
          <el-select v-model="createForm.defaultProjectCode" placeholder="请选择默认项目">
            <el-option
              v-for="project in createDefaultProjectOptions"
              :key="project.code"
              :label="project.name"
              :value="project.code"
            />
          </el-select>
        </el-form-item>

        <div class="admin-management-page__dialog-tip">
          新建管理员时建议同时完成角色和项目分配，否则账号启用后仍可能无法进入目标工作域。
        </div>
      </el-form>

      <template #footer>
        <div class="admin-management-page__dialog-actions">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="submitCreateForm">创建管理员</el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" size="760px" destroy-on-close>
      <template #header>
        <div v-if="selectedAdmin" class="admin-management-page__drawer-header">
          <div>
            <div class="admin-management-page__drawer-title">
              {{ selectedAdmin.displayName }}
              <el-tag v-if="selectedAdmin.protectedAccount" type="danger" effect="light">受保护账号</el-tag>
            </div>
            <div class="admin-management-page__drawer-subtitle">
              {{ selectedAdmin.username }} · {{ selectedAdmin.status === 1 ? '启用中' : '已停用' }}
            </div>
          </div>
        </div>
      </template>

      <div v-loading="detailLoading" class="admin-management-page__drawer-body">
        <el-alert
          v-if="selectedAdmin && isSensitiveReadonly"
          type="warning"
          :closable="false"
          show-icon
          title="当前账号带有受保护角色，只有超级管理员可以修改其角色、项目权限和安全配置。"
        />

        <el-tabs v-if="selectedAdmin" v-model="activeTab" class="admin-management-page__tabs">
          <el-tab-pane label="基本信息" name="basic">
            <section class="admin-management-page__tab-grid">
              <div class="admin-management-page__info-card admin-card">
                <div class="admin-management-page__info-label">登录账号</div>
                <div class="admin-management-page__info-value">{{ selectedAdmin.username }}</div>
                <div class="admin-management-page__info-hint">账号创建后不支持在首版后台中修改。</div>
              </div>
              <div class="admin-management-page__info-card admin-card">
                <div class="admin-management-page__info-label">创建时间</div>
                <div class="admin-management-page__info-value">{{ formatDateTime(selectedAdmin.createdAt) }}</div>
                <div class="admin-management-page__info-hint">最近更新：{{ formatDateTime(selectedAdmin.updatedAt) }}</div>
              </div>
              <div class="admin-management-page__info-card admin-card">
                <div class="admin-management-page__info-label">最近登录</div>
                <div class="admin-management-page__info-value">{{ formatDateTime(selectedAdmin.lastLoginAt, '暂无记录') }}</div>
                <div class="admin-management-page__info-hint">可用于判断账号活跃程度与风险排查。</div>
              </div>
            </section>

            <section class="admin-management-page__form-panel admin-card">
              <div class="admin-management-page__panel-head">
                <div>
                  <h3>基础资料</h3>
                  <p>这里维护管理员对外显示名称和默认进入项目，不处理密码与角色。</p>
                </div>
              </div>

              <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-position="top">
                <el-form-item label="显示名称" prop="displayName">
                  <el-input v-model="basicForm.displayName" maxlength="50" placeholder="请输入显示名称" />
                </el-form-item>

                <el-form-item label="默认项目" prop="defaultProjectCode">
                  <el-select v-model="basicForm.defaultProjectCode" placeholder="请选择默认项目">
                    <el-option
                      v-for="project in selectedAdmin.projects"
                      :key="project.code"
                      :label="project.name"
                      :value="project.code"
                    />
                  </el-select>
                </el-form-item>

                <div class="admin-management-page__footer-actions">
                  <el-button :disabled="isSensitiveReadonly" :loading="savingBasic" type="primary" @click="submitBasicForm">
                    保存基础资料
                  </el-button>
                </div>
              </el-form>
            </section>
          </el-tab-pane>

          <el-tab-pane label="角色分配" name="roles">
            <section class="admin-management-page__form-panel admin-card">
              <div class="admin-management-page__panel-head">
                <div>
                  <h3>角色选择</h3>
                  <p>角色决定管理员能做什么。当前系统采用角色能力与项目范围两层控制。</p>
                </div>
              </div>

              <div class="admin-management-page__card-grid">
                <label
                  v-for="role in roleSelectionOptions"
                  :key="role.id"
                  class="admin-management-page__select-card"
                  :class="{
                    'is-active': roleForm.roleIds.includes(role.id),
                    'is-disabled': isRoleOptionDisabled(role)
                  }"
                >
                  <el-checkbox
                    :model-value="roleForm.roleIds.includes(role.id)"
                    :disabled="isRoleOptionDisabled(role)"
                    @change="(checked) => toggleRoleSelection(role.id, checked)"
                  >
                    <span class="admin-management-page__select-title">{{ role.roleName }}</span>
                  </el-checkbox>
                  <div class="admin-management-page__select-subtitle">{{ role.roleCode }}</div>
                  <p>{{ role.description }}</p>
                </label>
              </div>

              <div class="admin-management-page__footer-actions">
                <el-button :disabled="isSensitiveReadonly" :loading="savingRoles" type="primary" @click="submitRoleAssignment">
                  保存角色分配
                </el-button>
              </div>
            </section>
          </el-tab-pane>

          <el-tab-pane label="项目权限" name="projects">
            <section class="admin-management-page__form-panel admin-card">
              <div class="admin-management-page__panel-head">
                <div>
                  <h3>项目授权</h3>
                  <p>项目权限决定管理员能在哪个业务域工作。至少保留一个项目，默认项目必须来自勾选列表。</p>
                </div>
              </div>

              <div class="admin-management-page__card-grid admin-management-page__card-grid--project">
                <label
                  v-for="project in projectOptions"
                  :key="project.code"
                  class="admin-management-page__select-card"
                  :class="{ 'is-active': projectForm.projectCodes.includes(project.code) }"
                >
                  <el-checkbox
                    :model-value="projectForm.projectCodes.includes(project.code)"
                    :disabled="isSensitiveReadonly"
                    @change="(checked) => toggleProjectSelection(project.code, checked)"
                  >
                    <span class="admin-management-page__select-title">{{ project.name }}</span>
                  </el-checkbox>
                  <div class="admin-management-page__select-subtitle">{{ project.code }}</div>
                </label>
              </div>

              <el-form label-position="top">
                <el-form-item label="默认项目">
                  <el-select v-model="projectForm.defaultProjectCode" placeholder="请选择默认项目">
                    <el-option
                      v-for="project in selectedProjectOptions"
                      :key="project.code"
                      :label="project.name"
                      :value="project.code"
                    />
                  </el-select>
                </el-form-item>
              </el-form>

              <div class="admin-management-page__footer-actions">
                <el-button :disabled="isSensitiveReadonly" :loading="savingProjects" type="primary" @click="submitProjectAssignment">
                  保存项目权限
                </el-button>
              </div>
            </section>
          </el-tab-pane>

          <el-tab-pane label="安全操作" name="security">
            <section class="admin-management-page__security-grid">
              <article class="admin-management-page__security-card admin-card">
                <div class="admin-management-page__panel-head">
                  <div>
                    <h3>账号状态</h3>
                    <p>启用后可正常登录，停用后立即失去后台访问权限。</p>
                  </div>
                </div>

                <div class="admin-management-page__status-row">
                  <el-tag :type="selectedAdmin.status === 1 ? 'success' : 'info'" effect="light">
                    {{ selectedAdmin.status === 1 ? '启用中' : '已停用' }}
                  </el-tag>
                  <el-button
                    :disabled="isStatusActionDisabled(selectedAdmin)"
                    :loading="togglingUserId === selectedAdmin.id"
                    :type="selectedAdmin.status === 1 ? 'warning' : 'success'"
                    plain
                    @click="toggleUserStatus(selectedAdmin)"
                  >
                    {{ selectedAdmin.status === 1 ? '停用账号' : '启用账号' }}
                  </el-button>
                </div>

                <div class="admin-management-page__status-tip">
                  当前账号最近登录时间：{{ formatDateTime(selectedAdmin.lastLoginAt, '暂无记录') }}
                </div>
              </article>

              <article class="admin-management-page__security-card admin-card">
                <div class="admin-management-page__panel-head">
                  <div>
                    <h3>重置密码</h3>
                    <p>这里用于后台管理员直接重设密码，不要求输入旧密码。建议用于交接、异常登录排查或初始化账号。</p>
                  </div>
                </div>

                <el-form ref="securityFormRef" :model="securityForm" :rules="securityRules" label-position="top">
                  <el-form-item label="新密码" prop="password">
                    <el-input v-model="securityForm.password" type="password" show-password placeholder="请输入新密码" />
                  </el-form-item>
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="securityForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                  </el-form-item>

                  <div class="admin-management-page__footer-actions">
                    <el-button :disabled="isSensitiveReadonly" :loading="savingSecurity" type="primary" @click="submitPasswordReset">
                      重置密码
                    </el-button>
                  </div>
                </el-form>
              </article>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { fetchAdminRoles } from '@/modules/system/api/role'
import {
  createAdminUser,
  fetchAdminProjectCatalog,
  fetchAdminUserDetail,
  fetchAdminUsers,
  resetAdminUserPassword,
  updateAdminUserBasicInfo,
  updateAdminUserProjects,
  updateAdminUserRoles,
  updateAdminUserStatus
} from '@/modules/system/api/admin-user'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { AdminManagedUser, AdminProject, AdminRoleSummary } from '@/shared/types/admin'

type AdminDrawerTab = 'basic' | 'roles' | 'projects' | 'security'

const sessionStore = useAdminSessionStore()

const loading = ref(false)
const detailLoading = ref(false)
const creating = ref(false)
const savingBasic = ref(false)
const savingRoles = ref(false)
const savingProjects = ref(false)
const savingSecurity = ref(false)
const togglingUserId = ref<number | null>(null)
const createDialogVisible = ref(false)
const drawerVisible = ref(false)
const activeTab = ref<AdminDrawerTab>('basic')

const createFormRef = ref<FormInstance>()
const basicFormRef = ref<FormInstance>()
const securityFormRef = ref<FormInstance>()

const admins = ref<AdminManagedUser[]>([])
const roleOptions = ref<AdminRoleSummary[]>([])
const projectOptions = ref<AdminProject[]>([])
const selectedAdmin = ref<AdminManagedUser | null>(null)

const filters = reactive<{
  keyword: string
  status: number | undefined
  roleCode: string
  projectCode: string
}>({
  keyword: '',
  status: undefined,
  roleCode: '',
  projectCode: ''
})

const createForm = reactive({
  username: '',
  displayName: '',
  password: '',
  confirmPassword: '',
  status: 1,
  roleIds: [] as number[],
  projectCodes: [] as string[],
  defaultProjectCode: ''
})

const basicForm = reactive({
  displayName: '',
  defaultProjectCode: ''
})

const roleForm = reactive({
  roleIds: [] as number[]
})

const projectForm = reactive({
  projectCodes: [] as string[],
  defaultProjectCode: ''
})

const securityForm = reactive({
  password: '',
  confirmPassword: ''
})

const currentAdminId = computed(() => sessionStore.adminUser?.id ?? null)
const currentRoleCodes = computed(() => sessionStore.adminUser?.roles || [])
const isSuperAdmin = computed(() => currentRoleCodes.value.includes('SUPER_ADMIN'))
const isSensitiveReadonly = computed(() => !!selectedAdmin.value?.protectedAccount && !isSuperAdmin.value)

const assignableRoleOptions = computed(() => {
  return roleOptions.value.filter((role) => role.status === 1 && (isSuperAdmin.value || !role.protectedRole))
})

const roleSelectionOptions = computed(() => {
  const selectedIds = new Set(roleForm.roleIds)
  return roleOptions.value.filter((role) => role.status === 1 || selectedIds.has(role.id))
})

const createDefaultProjectOptions = computed(() => projectOptions.value.filter((project) => createForm.projectCodes.includes(project.code)))
const selectedProjectOptions = computed(() => projectOptions.value.filter((project) => projectForm.projectCodes.includes(project.code)))

const adminStats = computed(() => ({
  total: admins.value.length,
  enabled: admins.value.filter((item) => item.status === 1).length,
  protected: admins.value.filter((item) => item.protectedAccount).length,
  projectAssignments: admins.value.reduce((sum, item) => sum + item.projects.length, 0)
}))

const createRules: FormRules<typeof createForm> = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!/^[a-zA-Z][a-zA-Z0-9_]{3,49}$/.test(value || '')) {
          callback(new Error('登录账号需为 4 到 50 位字母、数字或下划线，且以字母开头'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { min: 2, max: 50, message: '显示名称长度保持在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        validatePassword(value, createForm.username, callback)
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入初始密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== createForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个角色', trigger: 'change' }],
  projectCodes: [{ required: true, type: 'array', min: 1, message: '请至少选择一个项目', trigger: 'change' }],
  defaultProjectCode: [{ required: true, message: '请选择默认项目', trigger: 'change' }]
}

const basicRules: FormRules<typeof basicForm> = {
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { min: 2, max: 50, message: '显示名称长度保持在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  defaultProjectCode: [{ required: true, message: '请选择默认项目', trigger: 'change' }]
}

const securityRules: FormRules<typeof securityForm> = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        validatePassword(value, selectedAdmin.value?.username || '', callback)
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== securityForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function validatePassword(value: string, username: string, callback: (error?: Error) => void) {
  const password = value || ''
  if (password.length < 8 || password.length > 32) {
    callback(new Error('密码长度需保持在 8 到 32 位之间'))
    return
  }
  if (username && password.toLowerCase().includes(username.toLowerCase())) {
    callback(new Error('密码不能包含账号名'))
    return
  }

  const strength = [
    /[A-Z]/.test(password),
    /[a-z]/.test(password),
    /\d/.test(password),
    /[^A-Za-z0-9]/.test(password)
  ].filter(Boolean).length

  if (strength < 3) {
    callback(new Error('密码需至少包含大写字母、小写字母、数字、符号中的 3 类'))
    return
  }
  callback()
}

function formatDateTime(value: string | null | undefined, fallback = '-') {
  return value ? value.replace('T', ' ').slice(0, 16) : fallback
}

function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  filters.roleCode = ''
  filters.projectCode = ''
  loadAdmins()
}

function resetCreateForm() {
  createForm.username = ''
  createForm.displayName = ''
  createForm.password = ''
  createForm.confirmPassword = ''
  createForm.status = 1
  createForm.roleIds = []
  createForm.projectCodes = []
  createForm.defaultProjectCode = ''
  createFormRef.value?.clearValidate()
}

function syncDetailForms(admin: AdminManagedUser) {
  basicForm.displayName = admin.displayName
  basicForm.defaultProjectCode = admin.defaultProjectCode || admin.projects[0]?.code || ''
  roleForm.roleIds = admin.roles.map((role) => role.id)
  projectForm.projectCodes = admin.projects.map((project) => project.code)
  projectForm.defaultProjectCode = admin.defaultProjectCode || admin.projects[0]?.code || ''
  securityForm.password = ''
  securityForm.confirmPassword = ''
  basicFormRef.value?.clearValidate()
  securityFormRef.value?.clearValidate()
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

async function loadOptions() {
  const [roles, projects] = await Promise.all([
    fetchAdminRoles(),
    fetchAdminProjectCatalog()
  ])
  roleOptions.value = roles
  projectOptions.value = projects
}

async function loadAdmins() {
  loading.value = true
  try {
    admins.value = await fetchAdminUsers({
      keyword: filters.keyword.trim() || undefined,
      status: filters.status,
      roleCode: filters.roleCode || undefined,
      projectCode: filters.projectCode || undefined
    })
  } finally {
    loading.value = false
  }
}

async function openAdminDrawer(adminId: number, tab: AdminDrawerTab = 'basic') {
  drawerVisible.value = true
  detailLoading.value = true
  activeTab.value = tab
  try {
    selectedAdmin.value = await fetchAdminUserDetail(adminId)
    syncDetailForms(selectedAdmin.value)
  } finally {
    detailLoading.value = false
  }
}

async function refreshSelectedAdmin() {
  if (!selectedAdmin.value) {
    return
  }
  selectedAdmin.value = await fetchAdminUserDetail(selectedAdmin.value.id)
  syncDetailForms(selectedAdmin.value)
}

async function refreshListAfterMutation(focusAdminId?: number) {
  await loadAdmins()
  if (focusAdminId && drawerVisible.value && selectedAdmin.value?.id === focusAdminId) {
    await refreshSelectedAdmin()
  }
}

function isStatusActionDisabled(admin: AdminManagedUser) {
  if (admin.id === currentAdminId.value) {
    return true
  }
  if (admin.protectedAccount && !isSuperAdmin.value) {
    return true
  }
  return false
}

function isRoleOptionDisabled(role: AdminRoleSummary) {
  if (isSensitiveReadonly.value) {
    return true
  }
  if (role.status !== 1) {
    return true
  }
  if (role.protectedRole && !isSuperAdmin.value) {
    return true
  }
  return false
}

function toggleRoleSelection(roleId: number, checked: boolean | string | number) {
  const enabled = Boolean(checked)
  if (enabled) {
    roleForm.roleIds = Array.from(new Set([...roleForm.roleIds, roleId]))
    return
  }
  roleForm.roleIds = roleForm.roleIds.filter((id) => id !== roleId)
}

function toggleProjectSelection(projectCode: string, checked: boolean | string | number) {
  const enabled = Boolean(checked)
  if (enabled) {
    projectForm.projectCodes = Array.from(new Set([...projectForm.projectCodes, projectCode]))
    return
  }
  projectForm.projectCodes = projectForm.projectCodes.filter((code) => code !== projectCode)
}

async function submitCreateForm() {
  await createFormRef.value?.validate()
  creating.value = true
  try {
    const created = await createAdminUser({
      username: createForm.username.trim(),
      displayName: createForm.displayName.trim(),
      password: createForm.password,
      confirmPassword: createForm.confirmPassword,
      status: createForm.status,
      roleIds: createForm.roleIds,
      projectCodes: createForm.projectCodes,
      defaultProjectCode: createForm.defaultProjectCode
    })
    ElMessage.success('管理员已创建')
    createDialogVisible.value = false
    await loadAdmins()
    await openAdminDrawer(created.id)
  } finally {
    creating.value = false
  }
}

async function submitBasicForm() {
  if (!selectedAdmin.value) {
    return
  }
  await basicFormRef.value?.validate()
  savingBasic.value = true
  try {
    await updateAdminUserBasicInfo(selectedAdmin.value.id, {
      displayName: basicForm.displayName.trim(),
      defaultProjectCode: basicForm.defaultProjectCode
    })
    ElMessage.success('基础资料已更新')
    await refreshListAfterMutation(selectedAdmin.value.id)
  } finally {
    savingBasic.value = false
  }
}

async function submitRoleAssignment() {
  if (!selectedAdmin.value) {
    return
  }
  if (!roleForm.roleIds.length) {
    ElMessage.warning('请至少保留一个角色')
    return
  }
  savingRoles.value = true
  try {
    await updateAdminUserRoles(selectedAdmin.value.id, {
      roleIds: roleForm.roleIds
    })
    ElMessage.success('角色分配已更新')
    await refreshListAfterMutation(selectedAdmin.value.id)
  } finally {
    savingRoles.value = false
  }
}

async function submitProjectAssignment() {
  if (!selectedAdmin.value) {
    return
  }
  if (!projectForm.projectCodes.length) {
    ElMessage.warning('请至少保留一个项目')
    return
  }
  if (!projectForm.defaultProjectCode) {
    ElMessage.warning('请选择默认项目')
    return
  }
  savingProjects.value = true
  try {
    await updateAdminUserProjects(selectedAdmin.value.id, {
      projectCodes: projectForm.projectCodes,
      defaultProjectCode: projectForm.defaultProjectCode
    })
    ElMessage.success('项目权限已更新')
    await refreshListAfterMutation(selectedAdmin.value.id)
  } finally {
    savingProjects.value = false
  }
}

async function submitPasswordReset() {
  if (!selectedAdmin.value) {
    return
  }
  await securityFormRef.value?.validate()
  savingSecurity.value = true
  try {
    await resetAdminUserPassword(selectedAdmin.value.id, {
      password: securityForm.password,
      confirmPassword: securityForm.confirmPassword
    })
    securityForm.password = ''
    securityForm.confirmPassword = ''
    securityFormRef.value?.clearValidate()
    ElMessage.success('密码已重置')
  } finally {
    savingSecurity.value = false
  }
}

async function toggleUserStatus(admin: AdminManagedUser) {
  const nextStatus = admin.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    nextStatus === 1
      ? `确定启用管理员“${admin.displayName}”吗？`
      : `确定停用管理员“${admin.displayName}”吗？`,
    nextStatus === 1 ? '启用管理员' : '停用管理员',
    {
      confirmButtonText: nextStatus === 1 ? '确认启用' : '确认停用',
      cancelButtonText: '取消',
      type: nextStatus === 1 ? 'success' : 'warning'
    }
  )

  togglingUserId.value = admin.id
  try {
    await updateAdminUserStatus(admin.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '管理员已启用' : '管理员已停用')
    await refreshListAfterMutation(admin.id)
  } finally {
    togglingUserId.value = null
  }
}

watch(
  () => createForm.projectCodes.slice(),
  (projectCodes) => {
    if (!projectCodes.includes(createForm.defaultProjectCode)) {
      createForm.defaultProjectCode = projectCodes[0] || ''
    }
  }
)

watch(
  () => projectForm.projectCodes.slice(),
  (projectCodes) => {
    if (!projectCodes.includes(projectForm.defaultProjectCode)) {
      projectForm.defaultProjectCode = projectCodes[0] || ''
    }
  }
)

onMounted(async () => {
  await loadOptions()
  await loadAdmins()
})
</script>

<style scoped lang="scss">
.admin-management-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-management-page__hero,
.admin-management-page__filters,
.admin-management-page__table,
.admin-management-page__form-panel,
.admin-management-page__info-card,
.admin-management-page__security-card {
  border-radius: var(--admin-radius-md);
  border: 1px solid var(--admin-border);
  background: var(--admin-surface);
  box-shadow: var(--admin-shadow);
}

.admin-management-page__hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background:
    linear-gradient(135deg, rgba(84, 94, 118, 0.96), rgba(120, 132, 159, 0.88)),
    #545e76;
  color: #fff;
}

.admin-management-page__hero-mark {
  font-size: 13px;
  letter-spacing: 0.16em;
  opacity: 0.78;
}

.admin-management-page__hero h2 {
  margin: 12px 0 8px;
  font-size: 30px;
}

.admin-management-page__hero p {
  margin: 0;
  max-width: 760px;
  line-height: 1.8;
  opacity: 0.88;
}

.admin-management-page__hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 320px;
}

.admin-management-page__metric {
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.admin-management-page__metric span {
  display: block;
  font-size: 13px;
  opacity: 0.8;
}

.admin-management-page__metric strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.admin-management-page__filters {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 18px 20px;
}

.admin-management-page__search {
  width: 320px;
}

.admin-management-page__table {
  padding: 8px 0 2px;
}

.admin-management-page__account {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.admin-management-page__account-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-management-page__account-sub,
.admin-management-page__muted,
.admin-management-page__dialog-tip,
.admin-management-page__drawer-subtitle,
.admin-management-page__info-hint,
.admin-management-page__select-subtitle,
.admin-management-page__select-card p,
.admin-management-page__status-tip,
.admin-management-page__panel-head p {
  color: var(--admin-text-soft);
}

.admin-management-page__tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.admin-management-page__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-management-page__dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.admin-management-page__dialog-actions,
.admin-management-page__footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.admin-management-page__drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-management-page__drawer-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
}

.admin-management-page__tabs {
  margin-top: 4px;
}

.admin-management-page__tab-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.admin-management-page__info-card,
.admin-management-page__form-panel,
.admin-management-page__security-card {
  padding: 22px;
}

.admin-management-page__info-label {
  font-size: 14px;
  color: var(--admin-text-soft);
}

.admin-management-page__info-value {
  margin-top: 10px;
  font-size: 20px;
  font-weight: 700;
}

.admin-management-page__panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.admin-management-page__panel-head h3 {
  margin: 0;
  font-size: 20px;
}

.admin-management-page__card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.admin-management-page__card-grid--project {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-management-page__select-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.admin-management-page__select-card.is-active {
  border-color: var(--admin-primary);
  box-shadow: 0 10px 24px -18px rgba(84, 94, 118, 0.68);
  transform: translateY(-1px);
}

.admin-management-page__select-card.is-disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.admin-management-page__select-title {
  font-weight: 700;
}

.admin-management-page__select-card p {
  margin: 0;
  line-height: 1.7;
}

.admin-management-page__security-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.admin-management-page__status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 1200px) {
  .admin-management-page__hero {
    flex-direction: column;
  }

  .admin-management-page__hero-stats {
    min-width: 0;
  }

  .admin-management-page__tab-grid,
  .admin-management-page__security-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .admin-management-page__filters,
  .admin-management-page__dialog-grid,
  .admin-management-page__card-grid {
    grid-template-columns: 1fr;
    flex-wrap: wrap;
  }

  .admin-management-page__search {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .admin-management-page__hero-stats {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
