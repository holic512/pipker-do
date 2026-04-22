<template>
  <PageContainer
    :title="`${projectName} · 标签管理`"
    description="围绕题目标签维护名称、颜色和引用次数，并与题目管理页形成双向联动。"
  >
    <template v-if="isKyzz">
      <div class="kyzz-tag-page">
        <div class="overview-section">
          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
            title="标签维护说明：标签只服务题目管理，不扩展题库标签；标签引用次数会随着题目新增、编辑、删除自动回写。"
          />

          <div class="stats-row">
            <div class="stat-item">
              <span class="label">标签总数</span>
              <span class="value">{{ dashboard.stats.totalTags }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">已引用</span>
              <span class="value success">{{ dashboard.stats.usedTags }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">未引用</span>
              <span class="value">{{ dashboard.stats.unusedTags }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">已打标签题目</span>
              <span class="value warning">{{ dashboard.stats.taggedQuestionCount }}</span>
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
                placeholder="搜索标签名称"
                :prefix-icon="Search"
                @keyup.enter="loadDashboard"
                @clear="loadDashboard"
              />
              <el-select v-model="filters.usedStatus" clearable placeholder="全部引用状态" class="status-select" @change="loadDashboard">
                <el-option label="已引用" :value="1" />
                <el-option label="未引用" :value="0" />
              </el-select>
              <el-button type="primary" plain :icon="Search" @click="loadDashboard">查询</el-button>
              <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            </div>
            <div class="toolbar-right">
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增标签</el-button>
            </div>
          </div>

          <el-table v-loading="loading" :data="dashboard.tags" row-key="id" class="kyzz-tag-page__table">
            <el-table-column label="标签信息" min-width="320">
              <template #default="{ row }">
                <div class="kyzz-tag-page__info-cell">
                  <div class="kyzz-tag-page__info-head">
                    <el-tag
                      size="large"
                      effect="light"
                      class="kyzz-tag-page__tag-chip"
                      :style="buildTagStyle(row.color)"
                    >
                      {{ row.tagName }}
                    </el-tag>
                    <span class="kyzz-tag-page__subtext">{{ row.color || '默认系统色' }}</span>
                  </div>
                  <div class="kyzz-tag-page__subtext">
                    题目页可按该标签筛选、录入和复制题目时继承标签。
                  </div>
                  <div v-if="row.deleteBlockReason" class="kyzz-tag-page__warning">
                    {{ row.deleteBlockReason }}
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="引用情况" min-width="220">
              <template #default="{ row }">
                <div class="kyzz-tag-page__metric-group">
                  <span>展示引用 {{ row.useCount }}</span>
                  <span :class="{ mismatch: row.useCount !== row.actualUseCount }">
                    实际引用 {{ row.actualUseCount }}
                  </span>
                </div>
                <div v-if="row.useCount !== row.actualUseCount" class="kyzz-tag-page__subtext">
                  当前标签引用数存在差异，建议执行校准。
                </div>
              </template>
            </el-table-column>

            <el-table-column label="更新时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updatedAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
                <el-button link type="primary" :icon="View" @click="openQuestions(row)">查看题目</el-button>
                <el-button
                  v-if="row.useCount !== row.actualUseCount"
                  link
                  type="warning"
                  :loading="syncingId === row.id"
                  @click="handleSyncUseCount(row)"
                >
                  校准引用数
                </el-button>
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

      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目标签' : '新增题目标签'" width="640px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
          <div class="kyzz-tag-page__form-grid">
            <el-form-item label="标签名称" prop="tagName">
              <el-input v-model="form.tagName" maxlength="50" placeholder="例如 高频考点" />
            </el-form-item>

            <el-form-item label="标签颜色">
              <div class="kyzz-tag-page__color-panel">
                <el-color-picker
                  v-model="form.color"
                  :predefine="tagColorPresets"
                  color-format="hex"
                  @active-change="handleColorActiveChange"
                />
                <el-input v-model="form.color" maxlength="7" placeholder="例如 #3E7BFA，可留空" />
                <el-tag size="large" effect="light" class="kyzz-tag-page__tag-chip" :style="buildTagStyle(form.color)">
                  {{ form.tagName.trim() || '标签预览' }}
                </el-tag>
              </div>
            </el-form-item>
          </div>

          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="标签已被题目引用后不能直接删除；如引用数与实际不一致，可在列表中执行“校准引用数”。"
          />
        </el-form>

        <template #footer>
          <div class="kyzz-tag-page__dialog-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitForm">
              {{ editingId ? '保存修改' : '创建标签' }}
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
// AI 索引: KYZZ 题目标签管理页面。
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type { KyzzQuestionTagAdminDashboard, KyzzQuestionTagAdminRow } from '@/shared/types/admin'
import {
  createKyzzQuestionTag,
  deleteKyzzQuestionTag,
  fetchKyzzQuestionTagDashboard,
  syncKyzzQuestionTagUseCount,
  updateKyzzQuestionTag
} from '@/modules/kyzz/api/question-tag'

const route = useRoute()
const router = useRouter()
const sessionStore = useAdminSessionStore()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const isKyzz = computed(() => projectCode.value === 'kyzz')
const projectName = computed(() => {
  return sessionStore.availableProjects.find((item) => item.code === projectCode.value)?.name || '考研政治'
})

const tagColorPresets = ['#3E7BFA', '#2F9E44', '#F08C00', '#D9480F', '#C2255C', '#7B61FF', '#0C8599', '#5F3DC4']

const loading = ref(false)
const saving = ref(false)
const deletingId = ref<number | null>(null)
const syncingId = ref<number | null>(null)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const dashboard = reactive<KyzzQuestionTagAdminDashboard>({
  stats: {
    totalTags: 0,
    usedTags: 0,
    unusedTags: 0,
    taggedQuestionCount: 0
  },
  tags: []
})

const filters = reactive({
  keyword: '',
  usedStatus: undefined as number | undefined
})

const form = reactive({
  tagName: '',
  color: ''
})

const formRules: FormRules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 50, message: '标签名称长度需在 1 到 50 个字符之间', trigger: 'blur' }
  ]
}

function buildTagStyle(color?: string | null) {
  const normalized = (color || '').trim()
  if (!normalized) {
    return {}
  }
  return {
    color: normalized,
    borderColor: `${normalized}33`,
    backgroundColor: `${normalized}14`
  }
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '暂无记录'
  }
  return value.replace('T', ' ').slice(0, 16)
}

function resetForm() {
  editingId.value = null
  form.tagName = ''
  form.color = ''
}

async function loadDashboard() {
  if (!isKyzz.value) {
    return
  }
  loading.value = true
  try {
    const result = await fetchKyzzQuestionTagDashboard({
      keyword: filters.keyword.trim() || undefined,
      usedStatus: filters.usedStatus
    })
    dashboard.stats = result.stats
    dashboard.tags = result.tags
  } catch (error) {
    ElMessage.error((error as Error).message || '加载题目标签失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.usedStatus = undefined
  void loadDashboard()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: KyzzQuestionTagAdminRow) {
  editingId.value = row.id
  form.tagName = row.tagName
  form.color = row.color || ''
  dialogVisible.value = true
}

function handleColorActiveChange(color: string) {
  if (color) {
    form.color = color.toUpperCase()
  }
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
      tagName: form.tagName.trim(),
      color: form.color.trim()
    }
    if (editingId.value) {
      await updateKyzzQuestionTag(editingId.value, payload)
      ElMessage.success('标签已更新')
    } else {
      await createKyzzQuestionTag(payload)
      ElMessage.success('标签已创建')
    }
    dialogVisible.value = false
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '保存标签失败')
  } finally {
    saving.value = false
  }
}

function openQuestions(row: KyzzQuestionTagAdminRow) {
  void router.push({
    name: 'project-questions',
    params: { projectCode: projectCode.value },
    query: { tagId: String(row.id) }
  })
}

async function handleSyncUseCount(row: KyzzQuestionTagAdminRow) {
  syncingId.value = row.id
  try {
    await syncKyzzQuestionTagUseCount(row.id)
    ElMessage.success('标签引用数已校准')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '校准引用数失败')
  } finally {
    syncingId.value = null
  }
}

async function handleDelete(row: KyzzQuestionTagAdminRow) {
  try {
    await ElMessageBox.confirm(
      row.deleteBlockReason || '将删除该标签。若当前没有题目引用，将直接移除。',
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
    await deleteKyzzQuestionTag(row.id)
    ElMessage.success('标签已删除')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '删除标签失败')
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<style scoped lang="scss">
@use '../../../styles/system-management.scss' as systemManagement;

@include systemManagement.management-page-shell('.kyzz-tag-page');

.kyzz-tag-page__table :deep(.el-tag) {
  max-width: 100%;
}

.kyzz-tag-page__info-cell {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kyzz-tag-page__info-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.kyzz-tag-page__tag-chip {
  max-width: 100%;
}

.kyzz-tag-page__subtext {
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
}

.kyzz-tag-page__warning {
  color: var(--el-color-warning-dark-2);
  background: var(--el-color-warning-light-9);
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.6;
}

.kyzz-tag-page__metric-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kyzz-tag-page__metric-group span {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.kyzz-tag-page__metric-group span.mismatch {
  color: var(--el-color-warning-dark-2);
}

.kyzz-tag-page__form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.kyzz-tag-page__color-panel {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.kyzz-tag-page__color-panel :deep(.el-tag) {
  grid-column: 1 / -1;
  justify-self: flex-start;
}

.kyzz-tag-page__dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .kyzz-tag-page__form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .kyzz-tag-page__color-panel {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
