<template>
  <PageContainer
    :title="`${projectName} · 用户题库选择`"
    description="按用户维度管理题库选择关系，支持检索、手动分配和移除，并查看学习进度摘要。"
  >
    <template v-if="isKyzz">
      <div class="kyzz-user-bank-page">
        <div class="overview-section">
          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
            title="关系说明：移除题库会直接删除用户与题库的选择关系，不保留该关系进度快照。"
          />

          <div class="stats-row">
            <div class="stat-item">
              <span class="label">用户总数</span>
              <span class="value">{{ dashboard.stats.totalUsers }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">已选题库用户</span>
              <span class="value success">{{ dashboard.stats.selectedUsers }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">未选题库用户</span>
              <span class="value warning">{{ dashboard.stats.unselectedUsers }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">选择关系总数</span>
              <span class="value">{{ dashboard.stats.totalSelections }}</span>
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
                placeholder="搜索用户ID / 昵称 / 用户名 / 手机号"
                :prefix-icon="Search"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
              <el-select
                v-model="filters.selectionStatus"
                clearable
                placeholder="全部选择状态"
                class="selection-select"
                @change="handleSearch"
              >
                <el-option label="已选题库" value="selected" />
                <el-option label="未选题库" value="unselected" />
              </el-select>
              <el-button type="primary" plain :icon="Search" @click="handleSearch">查询</el-button>
              <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            </div>
          </div>

          <el-table v-loading="loading" :data="dashboard.records" row-key="userId">
            <el-table-column label="用户" min-width="260">
              <template #default="{ row }">
                <div class="kyzz-user-bank-page__user-cell">
                  <div class="kyzz-user-bank-page__user-main">{{ row.nickname || '未设置昵称' }}</div>
                  <div class="kyzz-user-bank-page__user-sub">
                    ID {{ row.userId }}
                    <span v-if="row.username">· {{ row.username }}</span>
                    <span v-if="row.phone">· {{ row.phone }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="账号状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="已选题库数" width="130" align="center">
              <template #default="{ row }">
                <span class="kyzz-user-bank-page__count">{{ row.selectedBankCount }}</span>
              </template>
            </el-table-column>

            <el-table-column label="最近练习" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.lastPracticeAt) }}
              </template>
            </el-table-column>

            <el-table-column label="关系更新时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updatedAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :loading="openingUserId === row.userId" @click="openDetail(row.userId)">
                  管理题库
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="kyzz-user-bank-page__pagination">
            <el-pagination
              v-model:current-page="pager.pageNo"
              v-model:page-size="pager.pageSize"
              background
              layout="total, sizes, prev, pager, next"
              :total="dashboard.pagination.total"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="handlePageSizeChange"
              @current-change="handlePageNoChange"
            />
          </div>
        </div>
      </div>

      <el-drawer
        v-model="detailDrawerVisible"
        size="780px"
        destroy-on-close
        :title="detail ? `${detail.nickname || '未设置昵称'} · 题库选择管理` : '题库选择管理'"
      >
        <div v-loading="detailLoading" class="kyzz-user-bank-page__drawer-body">
          <template v-if="detail">
            <div class="kyzz-user-bank-page__drawer-meta">
              <div>用户ID：{{ detail.userId }}</div>
              <div>账号状态：{{ detail.status === 1 ? '正常' : '禁用' }}</div>
              <div>最后登录：{{ formatDateTime(detail.lastLoginAt) }}</div>
            </div>

            <div class="kyzz-user-bank-page__drawer-stats">
              <div class="stat-chip">已选 {{ detail.summary.selectedBankCount }}</div>
              <div class="stat-chip">进行中 {{ detail.summary.inProgressBankCount }}</div>
              <div class="stat-chip">已完成 {{ detail.summary.completedBankCount }}</div>
            </div>

            <div class="kyzz-user-bank-page__add-panel">
              <el-select
                v-model="pendingBankId"
                filterable
                clearable
                placeholder="选择要添加的题库"
                class="add-select"
                :disabled="saving"
              >
                <el-option
                  v-for="bank in unselectedBanks"
                  :key="bank.questionBankId"
                  :label="formatBankOptionLabel(bank)"
                  :value="bank.questionBankId"
                />
              </el-select>
              <el-button type="primary" :disabled="!pendingBankId || saving" :loading="saving" @click="handleAddBank">
                添加题库
              </el-button>
            </div>

            <el-table :data="detail.selectedBanks" row-key="questionBankId" class="kyzz-user-bank-page__drawer-table">
              <el-table-column label="题库" min-width="260">
                <template #default="{ row }">
                  <div class="kyzz-user-bank-page__bank-cell">
                    <div class="kyzz-user-bank-page__bank-main">{{ row.bankName }}</div>
                    <div class="kyzz-user-bank-page__bank-sub">
                      {{ row.bankCode }}
                      <span v-if="row.categoryName">· {{ row.categoryName }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="难度" width="100" align="center">
                <template #default="{ row }">
                  {{ difficultyLabel(row.difficultyLevel) }}
                </template>
              </el-table-column>

              <el-table-column label="进度" width="140" align="center">
                <template #default="{ row }">
                  <div class="kyzz-user-bank-page__progress-cell">
                    <span>{{ formatProgress(row.currentProgress) }}</span>
                    <span class="meta">{{ row.studiedCount }}/{{ row.questionCount }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="最近练习" width="170">
                <template #default="{ row }">
                  {{ formatDateTime(row.lastPracticeAt) }}
                </template>
              </el-table-column>

              <el-table-column label="加入时间" width="170">
                <template #default="{ row }">
                  {{ formatDateTime(row.joinedAt) }}
                </template>
              </el-table-column>

              <el-table-column label="来源" width="100" align="center">
                <template #default="{ row }">
                  {{ joinSourceLabel(row.joinSource) }}
                </template>
              </el-table-column>

              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button
                    link
                    type="danger"
                    :disabled="saving"
                    :loading="saving && mutatingBankId === row.questionBankId"
                    @click="handleRemoveBank(row.questionBankId, row.bankName)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </div>
      </el-drawer>
    </template>

    <el-empty
      v-else
      description="当前页面仅对考研政治项目开放。请先切换到 kyzz 项目工作区。"
    />
  </PageContainer>
</template>

<script setup lang="ts">
// AI 索引: KYZZ 管理端用户题库选择管理页面。
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type {
  KyzzUserQuestionBankAdminBankOption,
  KyzzUserQuestionBankAdminDashboard,
  KyzzUserQuestionBankAdminSelectedBank,
  KyzzUserQuestionBankAdminUserDetail
} from '@/shared/types/admin'
import {
  fetchKyzzUserQuestionBankDashboard,
  fetchKyzzUserQuestionBankDetail,
  updateKyzzUserQuestionBankSelection
} from '@/modules/kyzz/api/user-question-bank'

const route = useRoute()
const sessionStore = useAdminSessionStore()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const isKyzz = computed(() => projectCode.value === 'kyzz')
const projectName = computed(() => {
  return sessionStore.availableProjects.find((item) => item.code === projectCode.value)?.name || '考研政治'
})

const loading = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const openingUserId = ref<number | null>(null)
const mutatingBankId = ref<number | null>(null)

const detailDrawerVisible = ref(false)
const currentUserId = ref<number | null>(null)
const pendingBankId = ref<number | null>(null)
const detail = ref<KyzzUserQuestionBankAdminUserDetail | null>(null)

const filters = reactive({
  keyword: '',
  selectionStatus: undefined as 'selected' | 'unselected' | undefined
})

const pager = reactive({
  pageNo: 1,
  pageSize: 20
})

const dashboard = reactive<KyzzUserQuestionBankAdminDashboard>({
  stats: {
    totalUsers: 0,
    selectedUsers: 0,
    unselectedUsers: 0,
    totalSelections: 0
  },
  records: [],
  pagination: {
    pageNo: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  }
})

const unselectedBanks = computed(() => {
  return (detail.value?.availableBanks || []).filter((bank) => !bank.selected)
})

function difficultyLabel(level: number) {
  if (level === 1) return '简单'
  if (level === 2) return '中等'
  if (level === 3) return '困难'
  if (level === 4) return '冲刺'
  return `L${level}`
}

function joinSourceLabel(source: string | null) {
  if (!source) {
    return '未记录'
  }
  if (source === 'manual') {
    return '手动'
  }
  if (source === 'recommend') {
    return '推荐'
  }
  if (source === 'vip') {
    return 'VIP'
  }
  return source
}

function formatProgress(progress: number) {
  if (Number.isNaN(progress)) {
    return '0.00%'
  }
  return `${Number(progress).toFixed(2)}%`
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '暂无记录'
  }
  return value.replace('T', ' ').slice(0, 16)
}

function formatBankOptionLabel(bank: KyzzUserQuestionBankAdminBankOption) {
  const category = bank.categoryName ? ` · ${bank.categoryName}` : ''
  return `${bank.bankName} (${bank.bankCode})${category}`
}

async function loadDashboard() {
  if (!isKyzz.value) {
    return
  }
  loading.value = true
  try {
    const result = await fetchKyzzUserQuestionBankDashboard({
      keyword: filters.keyword || undefined,
      selectionStatus: filters.selectionStatus,
      pageNo: pager.pageNo,
      pageSize: pager.pageSize
    })
    dashboard.stats = result.stats
    dashboard.records = result.records
    dashboard.pagination = result.pagination
  } catch (error) {
    ElMessage.error((error as Error).message || '加载用户题库选择数据失败')
  } finally {
    loading.value = false
  }
}

async function loadDetail(userId: number) {
  detailLoading.value = true
  try {
    detail.value = await fetchKyzzUserQuestionBankDetail(userId)
  } catch (error) {
    ElMessage.error((error as Error).message || '加载用户题库详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function openDetail(userId: number) {
  openingUserId.value = userId
  currentUserId.value = userId
  pendingBankId.value = null
  detailDrawerVisible.value = true
  await loadDetail(userId)
  openingUserId.value = null
}

async function refreshCurrentDetail() {
  if (!currentUserId.value) {
    return
  }
  await loadDetail(currentUserId.value)
}

async function handleAddBank() {
  if (!currentUserId.value || !pendingBankId.value) {
    return
  }
  const selectedBank = unselectedBanks.value.find((item) => item.questionBankId === pendingBankId.value)
  if (!selectedBank) {
    ElMessage.warning('请选择要添加的题库')
    return
  }

  saving.value = true
  mutatingBankId.value = pendingBankId.value
  try {
    await updateKyzzUserQuestionBankSelection(currentUserId.value, pendingBankId.value, { selected: true })
    ElMessage.success('题库已添加给当前用户')
    pendingBankId.value = null
    await Promise.all([loadDashboard(), refreshCurrentDetail()])
  } catch (error) {
    ElMessage.error((error as Error).message || '添加题库失败')
  } finally {
    saving.value = false
    mutatingBankId.value = null
  }
}

async function handleRemoveBank(bankId: number, bankName: string) {
  if (!currentUserId.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `将从当前用户移除题库“${bankName}”，该题库选择关系会被直接删除。`,
      '确认移除',
      {
        type: 'warning',
        confirmButtonText: '确认移除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  saving.value = true
  mutatingBankId.value = bankId
  try {
    await updateKyzzUserQuestionBankSelection(currentUserId.value, bankId, { selected: false })
    ElMessage.success('题库已移除')
    await Promise.all([loadDashboard(), refreshCurrentDetail()])
  } catch (error) {
    ElMessage.error((error as Error).message || '移除题库失败')
  } finally {
    saving.value = false
    mutatingBankId.value = null
  }
}

function handleSearch() {
  pager.pageNo = 1
  void loadDashboard()
}

function resetFilters() {
  filters.keyword = ''
  filters.selectionStatus = undefined
  pager.pageNo = 1
  pager.pageSize = 20
  void loadDashboard()
}

function handlePageNoChange(pageNo: number) {
  pager.pageNo = pageNo
  void loadDashboard()
}

function handlePageSizeChange(pageSize: number) {
  pager.pageSize = pageSize
  pager.pageNo = 1
  void loadDashboard()
}

onMounted(() => {
  void loadDashboard()
})
</script>

<style scoped lang="scss">
@use '../../../styles/system-management.scss' as systemManagement;

@include systemManagement.management-page-shell('.kyzz-user-bank-page');

.kyzz-user-bank-page {
  .selection-select {
    width: 160px;
  }
}

.kyzz-user-bank-page__user-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kyzz-user-bank-page__user-main {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.kyzz-user-bank-page__user-sub {
  color: var(--admin-text-soft);
  font-size: 13px;
}

.kyzz-user-bank-page__count {
  font-weight: 700;
  color: var(--el-color-primary);
}

.kyzz-user-bank-page__pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.kyzz-user-bank-page__drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 240px;
}

.kyzz-user-bank-page__drawer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--admin-text-soft);
  font-size: 13px;
}

.kyzz-user-bank-page__drawer-stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.kyzz-user-bank-page__drawer-stats .stat-chip {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--admin-surface-soft);
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.kyzz-user-bank-page__add-panel {
  display: flex;
  gap: 12px;
  align-items: center;
}

.kyzz-user-bank-page__add-panel .add-select {
  flex: 1;
}

.kyzz-user-bank-page__bank-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kyzz-user-bank-page__bank-main {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.kyzz-user-bank-page__bank-sub {
  color: var(--admin-text-soft);
  font-size: 12px;
}

.kyzz-user-bank-page__progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.2;
}

.kyzz-user-bank-page__progress-cell .meta {
  color: var(--admin-text-soft);
  font-size: 12px;
}

@media (max-width: 768px) {
  .kyzz-user-bank-page__add-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .kyzz-user-bank-page__pagination {
    justify-content: center;
  }
}
</style>
