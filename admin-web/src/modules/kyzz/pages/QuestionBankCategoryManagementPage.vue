<template>
  <PageContainer
    :title="`${projectName} · 题库分类管理`"
    description="围绕现有分类表维护编码、名称、层级、排序和启停状态，并同步查看题库与题目占用情况。"
  >
    <template v-if="isKyzz">
      <div class="kyzz-category-page">
        <div class="overview-section">
          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
            title="分类使用建议：一级分类用于大板块，二级和三级用于精细化题库组织。层级本身是运营语义，不再额外维护父级关系。"
          />

          <div class="stats-row">
            <div class="stat-item">
              <span class="label">分类总数</span>
              <span class="value">{{ dashboard.stats.totalCategories }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">启用中</span>
              <span class="value success">{{ dashboard.stats.enabledCategories }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">一级分类</span>
              <span class="value">{{ dashboard.stats.levelOneCategories }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">挂载题库</span>
              <span class="value">{{ dashboard.stats.linkedQuestionBanks }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">覆盖题目</span>
              <span class="value warning">{{ dashboard.stats.linkedQuestions }}</span>
            </div>
          </div>
        </div>

        <div class="main-content">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input
                v-model="filters.keyword"
                class="search-input"
                clearable
                placeholder="搜索编码 / 名称"
                :prefix-icon="Search"
                @keyup.enter="loadDashboard"
                @clear="loadDashboard"
              />
              <el-select v-model="filters.isEnabled" clearable placeholder="全部状态" class="status-select" @change="loadDashboard">
                <el-option label="启用中" :value="1" />
                <el-option label="已停用" :value="0" />
              </el-select>
              <el-select v-model="filters.categoryLevel" clearable placeholder="全部层级" class="level-select" @change="loadDashboard">
                <el-option label="一级分类" :value="1" />
                <el-option label="二级分类" :value="2" />
                <el-option label="三级分类" :value="3" />
              </el-select>
              <el-button type="primary" plain :icon="Search" @click="loadDashboard">查询</el-button>
              <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            </div>
            <div class="toolbar-right">
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增分类</el-button>
            </div>
          </div>

          <el-table v-loading="loading" :data="tableRows" row-key="id" class="kyzz-category-table">
            <el-table-column label="分类信息" min-width="320">
              <template #default="{ row }">
                <div class="kyzz-category-page__name-cell">
                  <div class="kyzz-category-page__name-head">
                    <span class="kyzz-category-page__name">{{ row.categoryName }}</span>
                    <el-tag size="small" effect="plain">L{{ row.categoryLevel }}</el-tag>
                  </div>
                  <div class="kyzz-category-page__sub code">{{ row.categoryCode }}</div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="占用情况" min-width="200">
              <template #default="{ row }">
                <div class="kyzz-category-page__metric-group">
                  <span>题库 {{ row.questionBankCount }}</span>
                  <span>题目 {{ row.questionCount }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="排序" width="90" align="center">
              <template #default="{ row }">
                {{ row.sortNo }}
              </template>
            </el-table-column>

            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.isEnabled === 1"
                  inline-prompt
                  active-text="启"
                  inactive-text="停"
                  :loading="togglingId === row.id"
                  :disabled="togglingId === row.id"
                  @change="handleToggleStatus(row)"
                />
              </template>
            </el-table-column>

            <el-table-column label="更新时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updatedAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
                <el-button
                  link
                  type="danger"
                  :icon="Delete"
                  :disabled="!row.canDelete || deletingId === row.id"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题库分类' : '新增题库分类'" width="640px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
          <div class="kyzz-category-page__form-grid">
            <el-form-item label="分类编码" prop="categoryCode">
              <el-input v-model="form.categoryCode" maxlength="48" placeholder="例如 marxism_outline" />
            </el-form-item>
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" maxlength="50" placeholder="例如 马原导学" />
            </el-form-item>
            <el-form-item label="分类层级" prop="categoryLevel">
              <el-select v-model="form.categoryLevel" placeholder="请选择层级">
                <el-option label="一级分类" :value="1" />
                <el-option label="二级分类" :value="2" />
                <el-option label="三级分类" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序值" prop="sortNo">
              <el-input-number v-model="form.sortNo" :min="0" :max="9999" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态" prop="isEnabled">
              <el-radio-group v-model="form.isEnabled">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="如果分类已经绑定题库或题目，系统会阻止直接删除，请先完成数据迁移。"
          />
        </el-form>

        <template #footer>
          <div class="kyzz-category-page__dialog-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitForm">
              {{ editingId ? '保存修改' : '创建分类' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </template>

    <el-empty
      v-else
      description="当前页面仅对考研政治项目开放。请先切换到 kyzz 项目工作区。"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { KyzzCategoryAdminDashboard, KyzzCategoryAdminItem } from '@/shared/types/admin'
import {
  createKyzzCategory,
  deleteKyzzCategory,
  fetchKyzzCategoryDashboard,
  updateKyzzCategory,
  updateKyzzCategoryStatus
} from '@/modules/kyzz/api/question-bank-category'

const route = useRoute()
const sessionStore = useAdminSessionStore()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const isKyzz = computed(() => projectCode.value === 'kyzz')
const projectName = computed(() => {
  return sessionStore.availableProjects.find((item) => item.code === projectCode.value)?.name || '考研政治'
})

const loading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const dashboard = reactive<KyzzCategoryAdminDashboard>({
  stats: {
    totalCategories: 0,
    enabledCategories: 0,
    levelOneCategories: 0,
    linkedQuestionBanks: 0,
    linkedQuestions: 0
  },
  categories: []
})

const filters = reactive({
  keyword: '',
  isEnabled: undefined as number | undefined,
  categoryLevel: undefined as number | undefined
})

const form = reactive({
  categoryCode: '',
  categoryName: '',
  categoryLevel: 1,
  sortNo: 0,
  isEnabled: 1
})

const formRules: FormRules<typeof form> = {
  categoryCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!/^[a-z][a-z0-9_-]{1,47}$/.test(value || '')) {
          return callback(new Error('编码需为 2-48 位小写字母、数字、下划线或中划线，并以字母开头'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度需在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  categoryLevel: [
    { required: true, message: '请选择分类层级', trigger: 'change' }
  ]
}

const tableRows = computed(() => {
  return [...dashboard.categories].sort((a, b) => a.categoryLevel - b.categoryLevel || a.sortNo - b.sortNo || a.id - b.id)
})

function resetForm() {
  form.categoryCode = ''
  form.categoryName = ''
  form.categoryLevel = 1
  form.sortNo = 0
  form.isEnabled = 1
}

function fillForm(row: KyzzCategoryAdminItem) {
  form.categoryCode = row.categoryCode
  form.categoryName = row.categoryName
  form.categoryLevel = row.categoryLevel
  form.sortNo = row.sortNo
  form.isEnabled = row.isEnabled
}

async function loadDashboard() {
  if (!isKyzz.value) {
    return
  }
  loading.value = true
  try {
    const result = await fetchKyzzCategoryDashboard({
      keyword: filters.keyword || undefined,
      isEnabled: filters.isEnabled,
      categoryLevel: filters.categoryLevel
    })
    dashboard.stats = result.stats
    dashboard.categories = result.categories
  } catch (error) {
    ElMessage.error((error as Error).message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.isEnabled = undefined
  filters.categoryLevel = undefined
  void loadDashboard()
}

function openCreateDialog() {
  editingId.value = null
  dialogVisible.value = true
  resetForm()
  formRef.value?.clearValidate()
}

function openEditDialog(row: KyzzCategoryAdminItem) {
  editingId.value = row.id
  dialogVisible.value = true
  fillForm(row)
  formRef.value?.clearValidate()
}

async function submitForm() {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) {
      return
    }
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = {
      categoryCode: form.categoryCode.trim(),
      categoryName: form.categoryName.trim(),
      categoryLevel: form.categoryLevel,
      sortNo: form.sortNo,
      isEnabled: form.isEnabled
    }

    if (editingId.value) {
      await updateKyzzCategory(editingId.value, payload)
      ElMessage.success('分类已更新')
    } else {
      await createKyzzCategory(payload)
      ElMessage.success('分类已创建')
    }

    dialogVisible.value = false
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '保存分类失败')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row: KyzzCategoryAdminItem) {
  togglingId.value = row.id
  try {
    await updateKyzzCategoryStatus(row.id, row.isEnabled === 1 ? 0 : 1)
    ElMessage.success(row.isEnabled === 1 ? '分类已停用' : '分类已启用')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '状态更新失败')
  } finally {
    togglingId.value = null
  }
}

async function handleDelete(row: KyzzCategoryAdminItem) {
  try {
    await ElMessageBox.confirm(
      `将删除分类“${row.categoryName}”。只有未被题库或题目占用的分类才能删除。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  deletingId.value = row.id
  try {
    await deleteKyzzCategory(row.id)
    ElMessage.success('分类已删除')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '删除分类失败')
  } finally {
    deletingId.value = null
  }
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '暂无记录'
  }
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  void loadDashboard()
})
</script>

<style scoped lang="scss">
@use '../../../styles/system-management.scss' as systemManagement;

@include systemManagement.management-page-shell('.kyzz-category-page');

.kyzz-category-page {
  .level-select {
    width: 140px;
  }
}

.kyzz-category-page__name-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kyzz-category-page__name-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.kyzz-category-page__name {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-category-page__sub {
  font-size: 13px;
  color: var(--admin-text-soft);
}

.kyzz-category-page__sub.code {
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
}

.kyzz-category-page__metric-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kyzz-category-page__metric-group span {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--admin-surface-soft);
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.kyzz-category-page__form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.kyzz-category-page__dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .kyzz-category-page__form-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
