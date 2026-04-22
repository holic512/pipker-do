<template>
  <PageContainer title="角色管理" description="维护系统角色定义与状态，结合项目范围实现权限控制。">
    <div class="role-management">
      <!-- 顶部信息与统计 (轻量化设计，取代原先厚重的 Hero) -->
      <div class="overview-section">
        <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
        >
          <template #title>权限治理策略：当前系统侧重于“角色职责边界”与“项目范围”的双层控制，暂未启用按钮级细粒度权限管控。</template>
        </el-alert>

        <div class="stats-row">
          <div class="stat-item">
            <span class="label">全部角色</span>
            <span class="value">{{ roleStats.total }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="stat-item">
            <span class="label">启用中</span>
            <span class="value success">{{ roleStats.enabled }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="stat-item">
            <span class="label">系统保护</span>
            <span class="value warning">{{ roleStats.protected }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="stat-item">
            <span class="label">已分配账号</span>
            <span class="value">{{ roleStats.assignments }}</span>
          </div>
        </div>
      </div>

      <!-- 主体内容区 (搜索区与表格融为一体) -->
      <div class="main-content">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-input
                v-model="filters.keyword"
                placeholder="搜索角色编码 / 名称"
                clearable
                class="search-input"
                :prefix-icon="Search"
                @keyup.enter="loadRoles"
                @clear="loadRoles"
            />
            <el-select v-model="filters.status" clearable placeholder="全部状态" class="status-select" @change="loadRoles">
              <el-option label="启用中" :value="1" />
              <el-option label="已停用" :value="0" />
            </el-select>
            <el-button type="primary" plain :icon="Search" @click="loadRoles">查询</el-button>
            <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" :icon="Plus" @click="openCreateDrawer">新增角色</el-button>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table v-loading="loading" :data="roles" class="role-table" row-key="id">
          <el-table-column label="角色信息" min-width="220">
            <template #default="{ row }">
              <div class="role-info">
                <div class="role-name">
                  <span>{{ row.roleName }}</span>
                  <el-tag v-if="row.protectedRole" type="danger" size="small" effect="plain" class="ml-2">系统保护</el-tag>
                </div>
                <div class="role-code">{{ row.roleCode }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="description" label="职责说明" min-width="260" show-overflow-tooltip />

          <el-table-column label="能力边界" min-width="320">
            <template #default="{ row }">
              <div class="capability-tags">
                <el-tag
                    v-for="cap in row.capabilities"
                    :key="cap"
                    size="small"
                    type="info"
                    effect="light"
                >
                  {{ cap }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="关联账号" width="100" align="center">
            <template #default="{ row }">
              <el-link v-if="row.adminCount > 0" type="primary" :underline="false">{{ row.adminCount }}</el-link>
              <span v-else class="text-muted">0</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                  :model-value="row.status === 1"
                  :disabled="isToggleDisabled(row) || togglingRoleId === row.id"
                  :loading="togglingRoleId === row.id"
                  inline-prompt
                  active-text="启"
                  inactive-text="停"
                  @change="handleToggleStatus(row)"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button
                  link
                  type="primary"
                  :icon="Edit"
                  :disabled="isEditDisabled(row)"
                  @click="openEditDrawer(row)"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 表单抽屉 (标准的结构与间距) -->
    <el-drawer
        v-model="drawerVisible"
        :title="editingRoleId ? '编辑角色' : '新增角色'"
        size="480px"
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="role-form">
        <el-form-item label="角色编码 (唯一标识)" prop="roleCode">
          <el-input
              v-model="form.roleCode"
              :disabled="!!editingRoleId"
              maxlength="50"
              placeholder="例如: SYSTEM_ADMIN"
          />
          <div class="form-tip">建议使用大写字母、数字和下划线，不可随意修改。</div>
        </el-form-item>

        <el-form-item label="角色名称 (展示用)" prop="roleName">
          <el-input v-model="form.roleName" maxlength="50" placeholder="例如: 系统管理员" />
        </el-form-item>

        <el-form-item v-if="!editingRoleId" label="初始状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="0"
              active-text="立即启用"
              inactive-text="暂不启用"
          />
        </el-form-item>

        <!-- 动态提示区域，取代原先花哨的预览框 -->
        <el-alert
            v-if="form.roleCode"
            :title="`${form.roleCode} 权限预设参考`"
            type="info"
            :closable="false"
            class="mt-4"
        >
          <div class="preview-content">
            <p>{{ previewProfile.description }}</p>
            <div class="capability-tags mt-2">
              <el-tag v-for="cap in previewProfile.capabilities" :key="cap" size="small" type="info">
                {{ cap }}
              </el-tag>
            </div>
          </div>
        </el-alert>
      </el-form>

      <!-- 使用标准插槽 -->
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editingRoleId ? '保存修改' : '确认创建' }}
          </el-button>
        </div>
      </template>
    </el-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit } from '@element-plus/icons-vue' // 引入图标
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { createAdminRole, fetchAdminRoles, updateAdminRole, updateAdminRoleStatus } from '@/modules/system/api/role'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { AdminRoleSummary } from '@/shared/types/admin'

const sessionStore = useAdminSessionStore()

const loading = ref(false)
const saving = ref(false)
const togglingRoleId = ref<number | null>(null)
const drawerVisible = ref(false)
const editingRoleId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const roles = ref<AdminRoleSummary[]>([])

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined
})

const form = reactive({
  roleCode: '',
  roleName: '',
  status: 1
})

// 原有逻辑保留
const roleProfileMap: Record<string, { description: string; capabilities: string[] }> = {
  SUPER_ADMIN: {
    description: '全局最高权限，拥有管理员、角色、项目授权和全部业务数据的控制权。',
    capabilities: ['管理管理员', '管理角色', '全项目授权', '全部业务菜单']
  },
  SYSTEM_ADMIN: {
    description: '通用系统治理，侧重日常维护。',
    capabilities: ['普通账号管理', '角色状态维护', '项目授权配置', '通用管理菜单']
  },
  PROJECT_ADMIN: {
    description: '负责单/多业务项目的运营与配置。',
    capabilities: ['授权项目菜单', '维护业务内容', '项目运营配置']
  },
  CONTENT_OPERATOR: {
    description: '负责题库、标签等内容编辑。',
    capabilities: ['题库与标签', '内容上下架', '授权项目限制']
  },
  DATA_VIEWER: {
    description: '仅查看数据和报表，无写操作权限。',
    capabilities: ['工作台与报表', '列表与详情', '禁止写操作']
  }
}

const formRules: FormRules<typeof form> = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (editingRoleId.value) return callback()
        if (!/^[A-Z][A-Z0-9_]{1,49}$/.test(value || '')) {
          return callback(new Error('需为 2-50 位大写字母、数字或下划线，以字母开头'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const currentRoles = computed(() => sessionStore.adminUser?.roles || [])
const isSuperAdmin = computed(() => currentRoles.value.includes('SUPER_ADMIN'))

const roleStats = computed(() => ({
  total: roles.value.length,
  enabled: roles.value.filter((item) => item.status === 1).length,
  protected: roles.value.filter((item) => item.protectedRole).length,
  assignments: roles.value.reduce((sum, item) => sum + item.adminCount, 0)
}))

const previewProfile = computed(() => {
  const code = (form.roleCode || '').trim().toUpperCase()
  return roleProfileMap[code] || {
    description: '自定义角色目前将作为身份标识，具体权限由后续项目授权和菜单分配决定。',
    capabilities: ['可分配管理员', '受项目授权限制']
  }
})

// 方法区
function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  loadRoles()
}

function resetForm() {
  form.roleCode = ''
  form.roleName = ''
  form.status = 1
  formRef.value?.clearValidate()
}

function openCreateDrawer() {
  editingRoleId.value = null
  resetForm()
  drawerVisible.value = true
}

function openEditDrawer(role: AdminRoleSummary) {
  editingRoleId.value = role.id
  form.roleCode = role.roleCode
  form.roleName = role.roleName
  form.status = role.status
  drawerVisible.value = true
}

function isEditDisabled(role: AdminRoleSummary) {
  return role.protectedRole && !isSuperAdmin.value
}

function isToggleDisabled(role: AdminRoleSummary) {
  return role.roleCode === 'SUPER_ADMIN' || (role.protectedRole && !isSuperAdmin.value)
}

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await fetchAdminRoles({
      keyword: filters.keyword.trim() || undefined,
      status: filters.status
    })
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      roleCode: form.roleCode.trim().toUpperCase(),
      roleName: form.roleName.trim(),
      status: form.status
    }
    if (editingRoleId.value) {
      await updateAdminRole(editingRoleId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createAdminRole(payload)
      ElMessage.success('创建成功')
    }
    drawerVisible.value = false
    loadRoles()
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(role: AdminRoleSummary) {
  const nextStatus = role.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(
        `确定要${actionText}角色【${role.roleName}】吗？`,
        '状态变更确认',
        { type: 'warning' }
    )
    togglingRoleId.value = role.id
    await updateAdminRoleStatus(role.id, nextStatus)
    ElMessage.success(`角色已${actionText}`)
    role.status = nextStatus // 乐观更新
  } catch (e) {
    // 取消操作或接口报错不处理
  } finally {
    togglingRoleId.value = null
  }
}

onMounted(() => loadRoles())
</script>

<style scoped lang="scss">
@use '../../../styles/system-management.scss' as systemManagement;

@include systemManagement.management-page-shell('.role-management');

/* 表格定制样式 */
.role-table {
  width: 100%;

  .role-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .role-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      display: flex;
      align-items: center;
    }
    .role-code {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      font-family: monospace;
    }
  }

  .capability-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
  }
}

/* 表单与抽屉样式 */
.role-form {
  .form-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.4;
    margin-top: 4px;
  }

  .preview-content {
    font-size: 13px;
    p {
      margin: 0;
      line-height: 1.5;
    }
  }
}

.drawer-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.ml-2 { margin-left: 8px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
</style>
