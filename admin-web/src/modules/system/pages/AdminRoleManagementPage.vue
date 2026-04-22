<template>
  <PageContainer title="角色管理" description="维护后台角色定义、状态和职责边界，当前采用角色能力与项目范围两层控制。">
    <template #actions>
      <el-button type="primary" @click="openCreateDrawer">新增角色</el-button>
    </template>

    <section class="role-page">
      <article class="role-page__hero admin-card">
        <div>
          <div class="role-page__hero-mark">权限治理</div>
          <h2>先管角色边界，再分配管理员</h2>
          <p>当前系统还没有按钮级权限表，所以首版角色管理重点是定义职责范围、保持角色状态清晰，并与项目权限配合使用。</p>
        </div>
        <div class="role-page__hero-stats">
          <div class="role-page__metric">
            <span>角色总数</span>
            <strong>{{ roleStats.total }}</strong>
          </div>
          <div class="role-page__metric">
            <span>启用角色</span>
            <strong>{{ roleStats.enabled }}</strong>
          </div>
          <div class="role-page__metric">
            <span>受保护角色</span>
            <strong>{{ roleStats.protected }}</strong>
          </div>
          <div class="role-page__metric">
            <span>角色分配数</span>
            <strong>{{ roleStats.assignments }}</strong>
          </div>
        </div>
      </article>

      <section class="role-page__filters admin-card">
        <el-input
          v-model="filters.keyword"
          class="role-page__search"
          clearable
          placeholder="搜索角色编码或角色名称"
          @keyup.enter="loadRoles"
          @clear="loadRoles"
        />
        <el-select v-model="filters.status" clearable placeholder="全部状态" @change="loadRoles">
          <el-option label="启用中" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
        <el-button @click="loadRoles">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </section>

      <section class="role-page__table admin-card">
        <el-table v-loading="loading" :data="roles" stripe>
          <el-table-column label="角色" min-width="240">
            <template #default="{ row }">
              <div class="role-page__role-cell">
                <div class="role-page__role-head">
                  <strong>{{ row.roleName }}</strong>
                  <el-tag v-if="row.protectedRole" type="danger" effect="light">受保护</el-tag>
                </div>
                <div class="role-page__role-code">{{ row.roleCode }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="职责说明" min-width="280">
            <template #default="{ row }">
              <div class="role-page__description">{{ row.description }}</div>
            </template>
          </el-table-column>

          <el-table-column label="能力边界" min-width="300">
            <template #default="{ row }">
              <div class="role-page__capabilities">
                <el-tag v-for="capability in row.capabilities" :key="capability" effect="plain" round>
                  {{ capability }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="关联管理员" width="120" align="center">
            <template #default="{ row }">
              <strong>{{ row.adminCount }}</strong>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
                {{ row.status === 1 ? '启用中' : '已停用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="最近更新" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.updatedAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="210" fixed="right">
            <template #default="{ row }">
              <div class="role-page__actions">
                <el-button link type="primary" :disabled="isEditDisabled(row)" @click="openEditDrawer(row)">
                  编辑
                </el-button>
                <el-button
                  link
                  :type="row.status === 1 ? 'warning' : 'success'"
                  :disabled="isToggleDisabled(row)"
                  :loading="togglingRoleId === row.id"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </section>

    <el-drawer v-model="drawerVisible" :title="editingRoleId ? '编辑角色' : '新增角色'" size="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :disabled="!!editingRoleId"
            maxlength="50"
            placeholder="例如 SYSTEM_ADMIN"
          />
          <div class="role-page__field-tip">建议使用大写字母、数字和下划线，后续可直接作为权限判断标识。</div>
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" maxlength="50" placeholder="例如 系统管理员" />
        </el-form-item>

        <el-form-item v-if="!editingRoleId" label="初始状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <section class="role-page__preview">
          <div class="role-page__preview-title">角色能力预览</div>
          <p>{{ previewProfile.description }}</p>
          <div class="role-page__capabilities">
            <el-tag v-for="capability in previewProfile.capabilities" :key="capability" effect="plain" round>
              {{ capability }}
            </el-tag>
          </div>
        </section>

        <div class="role-page__drawer-actions">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">
            {{ editingRoleId ? '保存修改' : '创建角色' }}
          </el-button>
        </div>
      </el-form>
    </el-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

const filters = reactive<{
  keyword: string
  status: number | undefined
}>({
  keyword: '',
  status: undefined
})

const form = reactive({
  roleCode: '',
  roleName: '',
  status: 1
})

const roleProfileMap: Record<string, { description: string; capabilities: string[] }> = {
  SUPER_ADMIN: {
    description: '负责后台全局治理，拥有管理员、角色、项目授权和全部业务数据的最高权限。',
    capabilities: ['管理所有管理员账号', '管理角色定义与启停', '配置全部项目访问范围', '访问所有业务管理菜单']
  },
  SYSTEM_ADMIN: {
    description: '负责后台通用治理，侧重管理员、角色和项目权限的日常维护。',
    capabilities: ['管理普通管理员账号', '维护角色状态与说明', '配置项目授权范围', '访问通用管理菜单']
  },
  PROJECT_ADMIN: {
    description: '负责单个或多个业务项目的后台运营与配置，不处理系统级账号治理。',
    capabilities: ['访问被授权项目菜单', '维护项目业务内容', '管理项目内运营配置', '查看项目数据概览']
  },
  CONTENT_OPERATOR: {
    description: '负责题库、分类、标签等内容维护工作，强调内容编辑而非系统治理。',
    capabilities: ['维护题库与标签', '处理内容上下架', '查看内容运营数据', '限制在授权项目内操作']
  },
  DATA_VIEWER: {
    description: '负责查看后台数据和报表，不参与新增、修改、删除等写操作。',
    capabilities: ['查看工作台与报表', '查看列表与详情', '不能执行写操作', '限制在授权项目内访问']
  }
}

const formRules: FormRules<typeof form> = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (editingRoleId.value) {
          callback()
          return
        }
        if (!/^[A-Z][A-Z0-9_]{1,49}$/.test(value || '')) {
          callback(new Error('角色编码需为 2 到 50 位大写字母、数字或下划线，且以字母开头'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度保持在 2 到 50 个字符之间', trigger: 'blur' }
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
  const roleCode = (form.roleCode || '').trim().toUpperCase()
  return roleProfileMap[roleCode] || {
    description: '自定义角色当前按角色编码进行职责约定，后续如果引入细粒度权限表，可以继续扩展到菜单或按钮级别。',
    capabilities: ['可参与管理员角色分配', '可结合项目授权限制范围', '当前不区分按钮级权限']
  }
})

function formatDateTime(value: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}

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
  if (role.roleCode === 'SUPER_ADMIN') {
    return true
  }
  return role.protectedRole && !isSuperAdmin.value
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
    if (editingRoleId.value) {
      await updateAdminRole(editingRoleId.value, {
        roleCode: form.roleCode.trim().toUpperCase(),
        roleName: form.roleName.trim(),
        status: form.status
      })
      ElMessage.success('角色信息已更新')
    } else {
      await createAdminRole({
        roleCode: form.roleCode.trim().toUpperCase(),
        roleName: form.roleName.trim(),
        status: form.status
      })
      ElMessage.success('角色已创建')
    }
    drawerVisible.value = false
    await loadRoles()
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(role: AdminRoleSummary) {
  const nextStatus = role.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    nextStatus === 1 ? `确定启用角色“${role.roleName}”吗？` : `确定停用角色“${role.roleName}”吗？`,
    nextStatus === 1 ? '启用角色' : '停用角色',
    {
      confirmButtonText: nextStatus === 1 ? '确认启用' : '确认停用',
      cancelButtonText: '取消',
      type: nextStatus === 1 ? 'success' : 'warning'
    }
  )

  togglingRoleId.value = role.id
  try {
    await updateAdminRoleStatus(role.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '角色已启用' : '角色已停用')
    await loadRoles()
  } finally {
    togglingRoleId.value = null
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped lang="scss">
.role-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.role-page__hero,
.role-page__filters,
.role-page__table {
  border-radius: var(--admin-radius-md);
  border: 1px solid var(--admin-border);
  background: var(--admin-surface);
  box-shadow: var(--admin-shadow);
}

.role-page__hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background:
    linear-gradient(135deg, rgba(84, 94, 118, 0.96), rgba(120, 132, 159, 0.88)),
    #545e76;
  color: #fff;
}

.role-page__hero-mark {
  font-size: 13px;
  letter-spacing: 0.16em;
  opacity: 0.78;
}

.role-page__hero h2 {
  margin: 12px 0 8px;
  font-size: 30px;
}

.role-page__hero p {
  margin: 0;
  max-width: 760px;
  line-height: 1.8;
  opacity: 0.88;
}

.role-page__hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 320px;
}

.role-page__metric {
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.role-page__metric span {
  display: block;
  font-size: 13px;
  opacity: 0.8;
}

.role-page__metric strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.role-page__filters {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 18px 20px;
}

.role-page__search {
  width: 320px;
}

.role-page__table {
  padding: 8px 0 2px;
}

.role-page__role-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.role-page__role-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-page__role-code,
.role-page__description {
  color: var(--admin-text-soft);
  line-height: 1.7;
}

.role-page__capabilities {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.role-page__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-page__field-tip {
  margin-top: 8px;
  font-size: 13px;
  color: var(--admin-text-soft);
}

.role-page__preview {
  margin-top: 8px;
  padding: 18px;
  border-radius: 16px;
  background: var(--admin-surface-soft);
}

.role-page__preview-title {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 700;
}

.role-page__preview p {
  margin: 0 0 14px;
  color: var(--admin-text-soft);
  line-height: 1.7;
}

.role-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
}

@media (max-width: 1080px) {
  .role-page__hero {
    flex-direction: column;
  }

  .role-page__hero-stats {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .role-page__hero-stats {
    grid-template-columns: 1fr 1fr;
  }

  .role-page__filters {
    flex-wrap: wrap;
  }

  .role-page__search {
    width: 100%;
  }
}
</style>
