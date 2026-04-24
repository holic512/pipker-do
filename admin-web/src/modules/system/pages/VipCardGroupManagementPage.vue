<template>
  <PageContainer title="会员卡组" description="按卡组配置会员权益，并在库存弹窗内维护兑换 Key。">
    <div class="vip-page__toolbar">
      <el-input v-model="filters.keyword" clearable placeholder="搜索卡组名称/备注" @keyup.enter="loadGroups" />
      <el-select v-model="filters.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadGroups">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
      <el-button type="success" @click="openEditor()">新建卡组</el-button>
    </div>

    <el-table v-loading="loading" :data="groups" row-key="id">
      <el-table-column prop="groupName" label="卡组名称" min-width="160" />
      <el-table-column label="权益" min-width="150">
        <template #default="{ row }">{{ vipTypeLabel(row.vipType) }} · {{ row.durationDays || '永久' }}天</template>
      </el-table-column>
      <el-table-column label="库存" min-width="220">
        <template #default="{ row }">
          <el-space wrap>
            <el-tag>总 {{ row.totalKeyCount }}</el-tag>
            <el-tag type="success">未用 {{ row.unusedKeyCount }}</el-tag>
            <el-tag type="warning">已用 {{ row.redeemedKeyCount }}</el-tag>
            <el-tag type="info">作废 {{ row.voidedKeyCount }}</el-tag>
          </el-space>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openInventory(row)">库存</el-button>
          <el-button link type="primary" @click="openEditor(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editorVisible" :title="editingGroup ? '编辑卡组' : '新建卡组'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="卡组名称" prop="groupName"><el-input v-model="form.groupName" maxlength="80" /></el-form-item>
        <el-form-item label="VIP类型" prop="vipType">
          <el-select v-model="form.vipType" @change="syncDurationByType">
            <el-option label="月卡" value="month" />
            <el-option label="季卡" value="quarter" />
            <el-option label="年卡" value="year" />
            <el-option label="永久" value="lifetime" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效天数" prop="durationDays"><el-input-number v-model="form.durationDays" :min="0" :max="3650" :disabled="form.vipType === 'lifetime'" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio-button :label="1">启用</el-radio-button><el-radio-button :label="0">停用</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" maxlength="255" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editorVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitForm">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="inventoryVisible" width="980px" destroy-on-close>
      <template #header>
        <div class="inventory-header">
          <div>
            <div class="inventory-header__title">{{ inventoryGroup?.groupName }} · 库存</div>
            <div class="inventory-header__desc">{{ vipTypeLabel(inventoryGroup?.vipType) }} · {{ inventoryGroup?.durationDays || '永久' }}天</div>
          </div>
        </div>
      </template>

      <div class="inventory-tools">
        <el-input v-model="inventoryFilters.keyword" clearable placeholder="搜索Key" @keyup.enter="loadInventoryKeys" />
        <el-select v-model="inventoryFilters.batchNo" clearable filterable placeholder="批次">
          <el-option v-for="batch in inventoryBatches" :key="batch.batchNo" :label="batch.batchNo" :value="batch.batchNo" />
        </el-select>
        <el-select v-model="inventoryFilters.status" clearable placeholder="使用状态">
          <el-option label="已作废" :value="0" />
          <el-option label="未使用" :value="1" />
          <el-option label="已使用" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadInventoryKeys">查询</el-button>
        <el-button :disabled="!inventoryFilters.batchNo" @click="exportBatchTxt(inventoryFilters.batchNo)">导出当前批次TXT</el-button>
      </div>

      <div class="inventory-create">
        <span class="inventory-tools__label">新增一批库存</span>
        <el-input-number v-model="batchCount" :min="1" :max="500" />
        <el-button type="success" :loading="batchSaving" :disabled="inventoryGroup?.status !== 1" @click="submitBatch">按数量生成Key</el-button>
      </div>

      <el-alert v-if="generatedKeys.length" class="inventory-generated" type="success" :closable="false" :title="`新批次 ${generatedBatchNo} 已生成 ${generatedKeys.length} 张卡，也已写入库存列表。`" />
      <el-input v-if="generatedKeys.length" class="inventory-generated" type="textarea" :rows="4" readonly :model-value="generatedKeys.map((item) => item.cardKey).join('\n')" />

      <el-table v-loading="batchListLoading" :data="inventoryBatches" row-key="batchNo" class="inventory-batch-table" height="180">
        <el-table-column prop="batchNo" label="批次" min-width="180" />
        <el-table-column label="统计" min-width="260">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag>总 {{ row.totalKeyCount }}</el-tag>
              <el-tag type="success">未用 {{ row.unusedKeyCount }}</el-tag>
              <el-tag type="warning">已用 {{ row.redeemedKeyCount }}</el-tag>
              <el-tag type="info">作废 {{ row.voidedKeyCount }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="生成时间" width="180" />
        <el-table-column label="批次操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="filterBatch(row.batchNo)">查看</el-button>
            <el-button link @click="exportBatchTxt(row.batchNo)">导出TXT</el-button>
            <el-button link type="danger" :disabled="row.unusedKeyCount < 1" @click="voidBatch(row)">批量作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-loading="inventoryLoading" :data="inventoryKeys" row-key="id" height="360">
        <el-table-column prop="cardKey" label="兑换Key" min-width="220" />
        <el-table-column prop="batchNo" label="批次" min-width="170" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="keyStatusType(row.status)">{{ keyStatusLabel(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="使用用户" min-width="150"><template #default="{ row }">{{ row.redeemedUserName || row.redeemedUserId || '-' }}</template></el-table-column>
        <el-table-column prop="redeemedAt" label="使用时间" width="180" />
        <el-table-column prop="voidReason" label="作废原因" min-width="150" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-tooltip v-if="row.status !== 1" content="已使用或已作废的卡保留审计记录，不能删除">
              <el-button link type="danger" disabled>删除</el-button>
            </el-tooltip>
            <el-button v-else link type="danger" @click="deleteKey(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import type { VipCardGroup, VipCardKey, VipCardKeyBatch } from '@/shared/types/admin'
import {
  batchCreateVipCardKeys,
  createVipCardGroup,
  deleteVipCardKey,
  fetchVipCardGroups,
  fetchVipCardKeyBatches,
  fetchVipCardKeys,
  updateVipCardGroup,
  updateVipCardGroupStatus,
  voidVipCardKeyBatch
} from '@/modules/system/api/vip'

const groups = ref<VipCardGroup[]>([])
const inventoryKeys = ref<VipCardKey[]>([])
const inventoryBatches = ref<VipCardKeyBatch[]>([])
const loading = ref(false)
const saving = ref(false)
const batchSaving = ref(false)
const inventoryLoading = ref(false)
const batchListLoading = ref(false)
const editorVisible = ref(false)
const inventoryVisible = ref(false)
const editingGroup = ref<VipCardGroup | null>(null)
const inventoryGroup = ref<VipCardGroup | null>(null)
const batchCount = ref(10)
const generatedKeys = ref<VipCardKey[]>([])
const generatedBatchNo = ref('')
const formRef = ref<FormInstance>()
const filters = reactive<{ keyword: string; status: number | undefined }>({ keyword: '', status: undefined })
const inventoryFilters = reactive<{ keyword: string; status: number | undefined; batchNo: string }>({ keyword: '', status: undefined, batchNo: '' })
const form = reactive({ groupName: '', vipType: 'month', durationDays: 30, status: 1, remark: '' })
const rules: FormRules = {
  groupName: [{ required: true, message: '请输入卡组名称', trigger: 'blur' }],
  vipType: [{ required: true, message: '请选择VIP类型', trigger: 'change' }],
  durationDays: [{ required: true, message: '请输入有效天数', trigger: 'change' }]
}

function vipTypeLabel(type?: string | null) {
  return ({ month: '月卡', quarter: '季卡', year: '年卡', lifetime: '永久', custom: '自定义' } as Record<string, string>)[type || ''] || '-'
}
function keyStatusLabel(status: number) { return ({ 0: '已作废', 1: '未使用', 2: '已使用' } as Record<number, string>)[status] || '未知' }
function keyStatusType(status: number) { return status === 1 ? 'success' : status === 2 ? 'warning' : 'info' }
function syncDurationByType() {
  const defaults: Record<string, number> = { month: 30, quarter: 90, year: 365, lifetime: 0, custom: 30 }
  form.durationDays = defaults[form.vipType] ?? 30
}
async function loadGroups() {
  loading.value = true
  try { groups.value = await fetchVipCardGroups({ keyword: filters.keyword || undefined, status: filters.status }) }
  finally { loading.value = false }
}
function resetFilters() { filters.keyword = ''; filters.status = undefined; loadGroups() }
function openEditor(row?: VipCardGroup) {
  editingGroup.value = row || null
  Object.assign(form, row ? { groupName: row.groupName, vipType: row.vipType, durationDays: row.durationDays, status: row.status, remark: row.remark || '' } : { groupName: '', vipType: 'month', durationDays: 30, status: 1, remark: '' })
  editorVisible.value = true
}
async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, remark: form.remark || null }
    if (editingGroup.value) await updateVipCardGroup(editingGroup.value.id, payload)
    else await createVipCardGroup(payload)
    ElMessage.success('保存成功')
    editorVisible.value = false
    await loadGroups()
  } finally { saving.value = false }
}
async function toggleStatus(row: VipCardGroup) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${next === 1 ? '启用' : '停用'}该卡组？`, '状态确认')
  await updateVipCardGroupStatus(row.id, next)
  ElMessage.success('状态已更新')
  await loadGroups()
}
async function openInventory(row: VipCardGroup) {
  inventoryGroup.value = row
  inventoryFilters.keyword = ''
  inventoryFilters.status = undefined
  inventoryFilters.batchNo = ''
  generatedKeys.value = []
  generatedBatchNo.value = ''
  batchCount.value = 10
  inventoryVisible.value = true
  await Promise.all([loadInventoryBatches(), loadInventoryKeys()])
}
async function loadInventoryKeys() {
  if (!inventoryGroup.value) return
  inventoryLoading.value = true
  try {
    inventoryKeys.value = await fetchVipCardKeys({ groupId: inventoryGroup.value.id, keyword: inventoryFilters.keyword || undefined, status: inventoryFilters.status, batchNo: inventoryFilters.batchNo || undefined })
  } finally { inventoryLoading.value = false }
}
async function loadInventoryBatches() {
  if (!inventoryGroup.value) return
  batchListLoading.value = true
  try {
    inventoryBatches.value = await fetchVipCardKeyBatches(inventoryGroup.value.id)
  } finally { batchListLoading.value = false }
}
function filterBatch(batchNo: string) {
  inventoryFilters.batchNo = batchNo
  loadInventoryKeys()
}
async function exportBatchTxt(batchNo: string) {
  if (!inventoryGroup.value || !batchNo) return
  const keys = await fetchVipCardKeys({ groupId: inventoryGroup.value.id, batchNo })
  const text = keys.map((item) => item.cardKey).join('\n')
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${inventoryGroup.value.groupName}-${batchNo}.txt`
  link.click()
  URL.revokeObjectURL(url)
}
async function voidBatch(row: VipCardKeyBatch) {
  if (!inventoryGroup.value) return
  const { value } = await ElMessageBox.prompt(`请输入批次 ${row.batchNo} 的作废原因`, '批量作废', { inputPattern: /\S+/, inputErrorMessage: '作废原因不能为空' })
  const result = await voidVipCardKeyBatch(inventoryGroup.value.id, row.batchNo, value)
  ElMessage.success(`已作废 ${result.voidedCount} 张，跳过 ${result.skippedCount} 张已使用/已作废卡`)
  await Promise.all([loadInventoryBatches(), loadInventoryKeys(), loadGroups()])
}
async function submitBatch() {
  if (!inventoryGroup.value) return
  batchSaving.value = true
  try {
    const result = await batchCreateVipCardKeys(inventoryGroup.value.id, batchCount.value)
    generatedKeys.value = result.keys
    generatedBatchNo.value = result.batchNo
    inventoryFilters.batchNo = result.batchNo
    ElMessage.success(`已新增 ${result.count} 张卡，批次 ${result.batchNo}`)
    await Promise.all([loadInventoryBatches(), loadInventoryKeys(), loadGroups()])
  } finally { batchSaving.value = false }
}
async function deleteKey(row: VipCardKey) {
  await ElMessageBox.confirm(`确认删除兑换Key：${row.cardKey}？`, '删除确认', { type: 'warning' })
  await deleteVipCardKey(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadInventoryBatches(), loadInventoryKeys(), loadGroups()])
}
onMounted(loadGroups)
</script>

<style scoped>
.vip-page__toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.vip-page__toolbar .el-input { max-width: 260px; }
.vip-page__toolbar .el-select { width: 140px; }
.inventory-header__title { font-weight: 700; color: var(--admin-text-primary); }
.inventory-header__desc { margin-top: 4px; font-size: 12px; color: var(--admin-text-secondary); }
.inventory-tools, .inventory-create { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.inventory-tools .el-input { max-width: 220px; }
.inventory-tools .el-select { width: 170px; }
.inventory-tools__label { color: var(--admin-text-secondary); font-size: 13px; }
.inventory-generated { margin-bottom: 12px; }
.inventory-batch-table { margin-bottom: 12px; }
</style>
