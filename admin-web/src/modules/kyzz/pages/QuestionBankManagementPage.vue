<template>
  <PageContainer
    :title="`${projectName} · 题库管理`"
    description="围绕题库表维护展示信息、分类归属和上下架状态，同时同步观察真实题量与运营热度指标。"
  >
    <template v-if="isKyzz">
      <div class="kyzz-question-bank-page">
        <div class="overview-section">
          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
            title="表结构解读：bank_code / bank_name 决定题库身份，category_id / difficulty_level 决定归类与使用场景，question_count / study_user_count / total_score 则是运营观察位。"
          />

          <div class="stats-row">
            <div class="stat-item">
              <span class="label">题库总数</span>
              <span class="value">{{ dashboard.stats.totalBanks }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">上架中</span>
              <span class="value success">{{ dashboard.stats.activeBanks }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">未归类</span>
              <span class="value warning">{{ dashboard.stats.uncategorizedBanks }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">实际题量</span>
              <span class="value">{{ dashboard.stats.totalQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">累计学习人数</span>
              <span class="value">{{ dashboard.stats.totalStudyUsers }}</span>
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
                placeholder="搜索编码 / 名称 / 副标题"
                :prefix-icon="Search"
                @keyup.enter="loadDashboard"
                @clear="loadDashboard"
              />
              <el-select v-model="filters.status" clearable placeholder="全部状态" class="status-select" @change="loadDashboard">
                <el-option label="上架中" :value="1" />
                <el-option label="已下架" :value="0" />
              </el-select>
              <el-select v-model="filters.categoryId" clearable placeholder="全部分类" class="category-select" @change="loadDashboard">
                <el-option
                  v-for="category in categoryOptions"
                  :key="category.id"
                  :label="formatCategoryLabel(category)"
                  :value="category.id"
                />
              </el-select>
              <el-select v-model="filters.difficultyLevel" clearable placeholder="全部难度" class="difficulty-select" @change="loadDashboard">
                <el-option
                  v-for="option in difficultyOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <el-button type="primary" plain :icon="Search" @click="loadDashboard">查询</el-button>
              <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            </div>
            <div class="toolbar-right">
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增题库</el-button>
            </div>
          </div>

          <el-table v-loading="loading" :data="tableRows" row-key="id" class="kyzz-question-bank-table">
            <el-table-column label="题库信息" min-width="420">
              <template #default="{ row }">
                <div class="kyzz-question-bank-page__bank-cell">
                  <div class="kyzz-question-bank-page__cover">
                    <img v-if="row.coverUrl" :src="row.coverUrl" alt="题库封面" />
                    <div v-else class="kyzz-question-bank-page__cover-placeholder">无封面</div>
                  </div>
                  <div class="kyzz-question-bank-page__bank-main">
                    <div class="kyzz-question-bank-page__bank-head">
                      <span class="kyzz-question-bank-page__bank-name">{{ row.bankName }}</span>
                      <el-tag size="small" effect="plain">{{ difficultyLabel(row.difficultyLevel) }}</el-tag>
                      <el-tag v-if="row.categoryName" size="small" type="info" effect="light">
                        {{ row.categoryName }}
                      </el-tag>
                      <el-tag v-else size="small" type="warning" effect="light">未归类</el-tag>
                    </div>
                    <div class="kyzz-question-bank-page__bank-sub code">{{ row.bankCode }}</div>
                    <div v-if="row.subtitle" class="kyzz-question-bank-page__bank-sub">{{ row.subtitle }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="题量校验" min-width="190">
              <template #default="{ row }">
                <div class="kyzz-question-bank-page__metric-group">
                  <span>展示 {{ row.questionCount }}</span>
                  <span :class="{ mismatch: row.questionCount !== row.actualQuestionCount }">
                    实际 {{ row.actualQuestionCount }}
                  </span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="热度指标" min-width="230">
              <template #default="{ row }">
                <div class="kyzz-question-bank-page__metric-group">
                  <span>评分 {{ row.totalScore.toFixed(2) }}</span>
                  <span>评测 {{ row.ratingCount }}</span>
                  <span>收藏 {{ row.collectCount }}</span>
                  <span>学习 {{ row.studyUserCount }}</span>
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
                  :model-value="row.status === 1"
                  inline-prompt
                  active-text="上"
                  inactive-text="下"
                  :loading="togglingId === row.id"
                  :disabled="togglingId === row.id"
                  @change="handleToggleStatus(row)"
                />
              </template>
            </el-table-column>

            <el-table-column label="创建人" width="130">
              <template #default="{ row }">
                {{ row.createdByDisplayName || '未记录' }}
              </template>
            </el-table-column>

            <el-table-column label="更新时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updatedAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
                <el-button link type="primary" :icon="Picture" @click="openCoverDialog(row)">封面</el-button>
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

      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题库' : '新增题库'" width="760px" destroy-on-close>
        <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
          <div class="kyzz-question-bank-page__form-grid">
            <el-form-item label="题库编码" prop="bankCode">
              <div class="kyzz-question-bank-page__code-block">
                <el-radio-group v-model="bankCodeMode">
                  <el-radio value="auto">自动生成</el-radio>
                  <el-radio value="manual">手动输入</el-radio>
                </el-radio-group>
                <el-input
                  v-if="bankCodeMode === 'manual'"
                  v-model="form.bankCode"
                  maxlength="48"
                  placeholder="例如 politics_sprint_2026"
                />
                <el-alert
                  v-else
                  type="info"
                  :closable="false"
                  show-icon
                  :title="`系统将根据题库名称自动生成编码，当前预览：${bankCodePreview}`"
                />
              </div>
            </el-form-item>
            <el-form-item label="题库名称" prop="bankName">
              <el-input v-model="form.bankName" maxlength="100" placeholder="例如 2026 冲刺选择题精练" />
            </el-form-item>
            <el-form-item label="副标题" prop="subtitle">
              <el-input v-model="form.subtitle" maxlength="255" placeholder="例如 高频考点 + 易错题二刷" />
            </el-form-item>
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" clearable placeholder="请选择分类">
                <el-option
                  v-for="category in categoryOptions"
                  :key="category.id"
                  :label="formatCategoryLabel(category)"
                  :value="category.id"
                  :disabled="category.isEnabled !== 1"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="form.difficultyLevel" placeholder="请选择难度">
                <el-option
                  v-for="option in difficultyOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="排序值" prop="sortNo">
              <el-input-number v-model="form.sortNo" :min="0" :max="9999" controls-position="right" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <el-form-item label="题库介绍" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="5000"
              show-word-limit
              placeholder="建议写清适用阶段、题型结构、推荐使用方式和和其他题库的区别，方便后续运营与前端展示。"
            />
          </el-form-item>

          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="封面不再作为建库表单字段，建好题库后请通过表格操作里的“封面”按钮单独上传和维护。"
          />
        </el-form>

        <template #footer>
          <div class="kyzz-question-bank-page__dialog-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitForm">
              {{ editingId ? '保存修改' : '创建题库' }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="coverDialogVisible" title="管理题库封面" width="560px" destroy-on-close>
        <div v-if="coverTarget" class="kyzz-question-bank-page__cover-dialog">
          <div class="kyzz-question-bank-page__cover-preview">
            <img v-if="coverTarget.coverUrl" :src="coverTarget.coverUrl" alt="题库封面" />
            <div v-else class="kyzz-question-bank-page__cover-placeholder large">当前未上传封面</div>
          </div>

          <div class="kyzz-question-bank-page__cover-meta">
            <div class="kyzz-question-bank-page__cover-title">{{ coverTarget.bankName }}</div>
            <div class="kyzz-question-bank-page__bank-sub code">{{ coverTarget.bankCode }}</div>
            <div class="kyzz-question-bank-page__cover-tip">
              封面将存入本项目本地文件服务，数据库只保存 storageKey，对外展示时再解析为访问 URL。
            </div>
          </div>
        </div>

        <template #footer>
          <div class="kyzz-question-bank-page__dialog-actions">
            <input
              ref="coverFileInputRef"
              type="file"
              accept="image/*"
              class="kyzz-question-bank-page__file-input"
              @change="handleCoverFileChange"
            />
            <el-button @click="coverDialogVisible = false">关闭</el-button>
            <el-button :loading="coverUploading" @click="triggerCoverUpload">
              {{ coverTarget?.coverUrl ? '替换封面' : '上传封面' }}
            </el-button>
            <el-button
              type="danger"
              plain
              :disabled="!coverTarget?.coverUrl || coverUploading"
              @click="removeCover"
            >
              移除封面
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
import { Delete, Edit, Picture, Plus, Refresh, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type {
  KyzzCategoryOption,
  KyzzQuestionBankAdminDashboard,
  KyzzQuestionBankAdminItem
} from '@/shared/types/admin'
import {
  createKyzzQuestionBank,
  deleteKyzzQuestionBank,
  fetchKyzzQuestionBankDashboard,
  updateKyzzQuestionBank,
  updateKyzzQuestionBankCover,
  updateKyzzQuestionBankStatus,
  uploadKyzzQuestionBankCover
} from '@/modules/kyzz/api/question-bank'

const route = useRoute()
const sessionStore = useAdminSessionStore()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const isKyzz = computed(() => projectCode.value === 'kyzz')
const projectName = computed(() => {
  return sessionStore.availableProjects.find((item) => item.code === projectCode.value)?.name || '考研政治'
})

const difficultyOptions = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 },
  { label: '冲刺', value: 4 }
]

const loading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const bankCodeMode = ref<'auto' | 'manual'>('auto')
const coverDialogVisible = ref(false)
const coverTarget = ref<KyzzQuestionBankAdminItem | null>(null)
const coverUploading = ref(false)
const coverFileInputRef = ref<HTMLInputElement | null>(null)

const dashboard = reactive<KyzzQuestionBankAdminDashboard>({
  stats: {
    totalBanks: 0,
    activeBanks: 0,
    inactiveBanks: 0,
    uncategorizedBanks: 0,
    totalQuestions: 0,
    totalStudyUsers: 0
  },
  banks: [],
  categories: []
})

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  difficultyLevel: undefined as number | undefined
})

const form = reactive({
  bankCode: '',
  bankName: '',
  subtitle: '',
  description: '',
  categoryId: null as number | null,
  difficultyLevel: 2,
  sortNo: 0,
  status: 1
})

const formRules: FormRules<typeof form> = {
  bankCode: [
    {
      validator: (_, value, callback) => {
        if (bankCodeMode.value !== 'manual') {
          callback()
          return
        }
        if (!(value || '').trim()) {
          callback(new Error('请输入题库编码'))
          return
        }
        if (!/^[a-z][a-z0-9_-]{1,47}$/.test(value || '')) {
          callback(new Error('编码需为 2-48 位小写字母、数字、下划线或中划线，并以字母开头'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  bankName: [
    { required: true, message: '请输入题库名称', trigger: 'blur' },
    { min: 2, max: 100, message: '题库名称长度需在 2 到 100 个字符之间', trigger: 'blur' }
  ],
  subtitle: [
    { max: 255, message: '副标题不能超过 255 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 5000, message: '题库介绍不能超过 5000 个字符', trigger: 'blur' }
  ]
}

const tableRows = computed(() => {
  return [...dashboard.banks].sort((a, b) => a.sortNo - b.sortNo || b.id - a.id)
})

const categoryOptions = computed(() => dashboard.categories)
const bankCodePreview = computed(() => buildAutoCodePreview(form.bankName, 'bank'))

function difficultyLabel(level: number) {
  return difficultyOptions.find((option) => option.value === level)?.label || `L${level}`
}

function formatCategoryLabel(category: KyzzCategoryOption) {
  return `${'L' + category.categoryLevel} · ${category.categoryName}`
}

function resetForm() {
  form.bankCode = ''
  form.bankName = ''
  form.subtitle = ''
  form.description = ''
  form.categoryId = null
  form.difficultyLevel = 2
  form.sortNo = 0
  form.status = 1
  bankCodeMode.value = 'auto'
}

function fillForm(row: KyzzQuestionBankAdminItem) {
  form.bankCode = row.bankCode
  form.bankName = row.bankName
  form.subtitle = row.subtitle || ''
  form.description = row.description || ''
  form.categoryId = row.categoryId
  form.difficultyLevel = row.difficultyLevel
  form.sortNo = row.sortNo
  form.status = row.status
  bankCodeMode.value = 'manual'
}

async function loadDashboard() {
  if (!isKyzz.value) {
    return
  }
  loading.value = true
  try {
    const result = await fetchKyzzQuestionBankDashboard({
      keyword: filters.keyword || undefined,
      status: filters.status,
      categoryId: filters.categoryId,
      difficultyLevel: filters.difficultyLevel
    })
    dashboard.stats = result.stats
    dashboard.banks = result.banks
    dashboard.categories = result.categories
    if (coverTarget.value) {
      coverTarget.value = result.banks.find((bank) => bank.id === coverTarget.value?.id) || null
    }
  } catch (error) {
    ElMessage.error((error as Error).message || '加载题库失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  filters.categoryId = undefined
  filters.difficultyLevel = undefined
  void loadDashboard()
}

function openCreateDialog() {
  editingId.value = null
  dialogVisible.value = true
  resetForm()
  formRef.value?.clearValidate()
}

function openEditDialog(row: KyzzQuestionBankAdminItem) {
  editingId.value = row.id
  dialogVisible.value = true
  fillForm(row)
  formRef.value?.clearValidate()
}

function openCoverDialog(row: KyzzQuestionBankAdminItem) {
  coverTarget.value = row
  coverDialogVisible.value = true
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
      bankCode: bankCodeMode.value === 'manual' ? form.bankCode.trim() : '',
      bankName: form.bankName.trim(),
      subtitle: form.subtitle.trim(),
      description: form.description.trim(),
      categoryId: form.categoryId,
      difficultyLevel: form.difficultyLevel,
      sortNo: form.sortNo,
      status: form.status
    }

    if (editingId.value) {
      await updateKyzzQuestionBank(editingId.value, payload)
      ElMessage.success('题库已更新')
    } else {
      await createKyzzQuestionBank(payload)
      ElMessage.success('题库已创建')
    }

    dialogVisible.value = false
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '保存题库失败')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row: KyzzQuestionBankAdminItem) {
  togglingId.value = row.id
  try {
    await updateKyzzQuestionBankStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(row.status === 1 ? '题库已下架' : '题库已上架')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '状态更新失败')
  } finally {
    togglingId.value = null
  }
}

async function handleDelete(row: KyzzQuestionBankAdminItem) {
  try {
    await ElMessageBox.confirm(
      `将删除题库“${row.bankName}”。只有未产生题目、评分、学习或答题记录的题库才能删除。`,
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
    await deleteKyzzQuestionBank(row.id)
    ElMessage.success('题库已删除')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '删除题库失败')
  } finally {
    deletingId.value = null
  }
}

function triggerCoverUpload() {
  if (coverUploading.value) {
    return
  }
  coverFileInputRef.value?.click()
}

async function handleCoverFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !coverTarget.value) {
    clearCoverInput()
    return
  }

  coverUploading.value = true
  try {
    const uploaded = await uploadKyzzQuestionBankCover(file)
    const updated = await updateKyzzQuestionBankCover(coverTarget.value.id, uploaded.storageKey)
    coverTarget.value = updated
    ElMessage.success('题库封面已更新')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '上传封面失败')
  } finally {
    coverUploading.value = false
    clearCoverInput()
  }
}

async function removeCover() {
  if (!coverTarget.value) {
    return
  }
  coverUploading.value = true
  try {
    const updated = await updateKyzzQuestionBankCover(coverTarget.value.id, null)
    coverTarget.value = updated
    ElMessage.success('题库封面已移除')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '移除封面失败')
  } finally {
    coverUploading.value = false
  }
}

function clearCoverInput() {
  if (coverFileInputRef.value) {
    coverFileInputRef.value.value = ''
  }
}

function buildAutoCodePreview(name: string, prefix: string) {
  const normalized = (name || '')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')

  if (normalized) {
    return normalized.slice(0, 48)
  }
  return `${prefix}_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}`
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

@include systemManagement.management-page-shell('.kyzz-question-bank-page');

.kyzz-question-bank-page {
  .category-select {
    width: 220px;
  }

  .difficulty-select {
    width: 140px;
  }
}

.kyzz-question-bank-page__bank-cell {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.kyzz-question-bank-page__bank-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.kyzz-question-bank-page__cover {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
  flex-shrink: 0;
}

.kyzz-question-bank-page__cover img,
.kyzz-question-bank-page__cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.kyzz-question-bank-page__cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-text-soft);
  font-size: 12px;
  text-align: center;
  padding: 8px;
}

.kyzz-question-bank-page__cover-placeholder.large {
  min-height: 220px;
  font-size: 14px;
}

.kyzz-question-bank-page__bank-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.kyzz-question-bank-page__bank-name {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-question-bank-page__bank-sub {
  font-size: 13px;
  color: var(--admin-text-soft);
}

.kyzz-question-bank-page__bank-sub.code {
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
}

.kyzz-question-bank-page__metric-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kyzz-question-bank-page__metric-group span {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--admin-surface-soft);
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.kyzz-question-bank-page__metric-group span.mismatch {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning-dark-2);
}

.kyzz-question-bank-page__form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.kyzz-question-bank-page__code-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kyzz-question-bank-page__cover-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.kyzz-question-bank-page__cover-preview {
  height: 220px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
}

.kyzz-question-bank-page__cover-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kyzz-question-bank-page__cover-title {
  font-size: 18px;
  font-weight: 700;
}

.kyzz-question-bank-page__cover-tip {
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
}

.kyzz-question-bank-page__dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.kyzz-question-bank-page__file-input {
  display: none;
}

@media (max-width: 768px) {
  .kyzz-question-bank-page__form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .kyzz-question-bank-page__bank-cell {
    flex-direction: column;
  }
}
</style>
