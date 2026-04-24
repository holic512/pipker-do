<template>
  <PageContainer title="兑换Key" description="查看会员兑换 Key 的发放、兑换和作废状态。">
    <div class="vip-page__toolbar">
      <el-input v-model="filters.keyword" clearable placeholder="搜索Key" @keyup.enter="loadKeys" />
      <el-select v-model="filters.groupId" clearable filterable placeholder="卡组">
        <el-option v-for="group in groups" :key="group.id" :label="group.groupName" :value="group.id" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="状态">
        <el-option label="已作废" :value="0" />
        <el-option label="未使用" :value="1" />
        <el-option label="已兑换" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadKeys">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="keys" row-key="id">
      <el-table-column prop="cardKey" label="兑换Key" min-width="220" />
      <el-table-column prop="groupName" label="卡组" min-width="150" />
      <el-table-column label="权益" min-width="140"><template #default="{ row }">{{ vipTypeLabel(row.vipType) }} · {{ row.durationDays || '永久' }}天</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="keyStatusType(row.status)">{{ keyStatusLabel(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="兑换用户" min-width="140"><template #default="{ row }">{{ row.redeemedUserName || row.redeemedUserId || '-' }}</template></el-table-column>
      <el-table-column prop="redeemedAt" label="兑换时间" width="180" />
      <el-table-column prop="voidReason" label="作废原因" min-width="160" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button link type="danger" :disabled="row.status !== 1" @click="voidKey(row)">作废</el-button></template></el-table-column>
    </el-table>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import type { VipCardGroup, VipCardKey } from '@/shared/types/admin'
import { fetchVipCardGroups, fetchVipCardKeys, voidVipCardKey } from '@/modules/system/api/vip'

const keys = ref<VipCardKey[]>([])
const groups = ref<VipCardGroup[]>([])
const loading = ref(false)
const filters = reactive<{ keyword: string; groupId: number | undefined; status: number | undefined }>({ keyword: '', groupId: undefined, status: undefined })

function vipTypeLabel(type?: string | null) { return ({ month: '月卡', quarter: '季卡', year: '年卡', lifetime: '永久', custom: '自定义' } as Record<string, string>)[type || ''] || '-' }
function keyStatusLabel(status: number) { return ({ 0: '已作废', 1: '未使用', 2: '已兑换' } as Record<number, string>)[status] || '未知' }
function keyStatusType(status: number) { return status === 1 ? 'success' : status === 2 ? 'warning' : 'info' }

async function loadGroups() { groups.value = await fetchVipCardGroups() }
async function loadKeys() {
  loading.value = true
  try { keys.value = await fetchVipCardKeys({ keyword: filters.keyword || undefined, groupId: filters.groupId, status: filters.status }) }
  finally { loading.value = false }
}
function resetFilters() { filters.keyword = ''; filters.groupId = undefined; filters.status = undefined; loadKeys() }
async function voidKey(row: VipCardKey) {
  const { value } = await ElMessageBox.prompt('请输入作废原因', '作废兑换Key', { inputPattern: /\S+/, inputErrorMessage: '作废原因不能为空' })
  await voidVipCardKey(row.id, value)
  ElMessage.success('兑换Key已作废')
  await loadKeys()
}
onMounted(async () => { await loadGroups(); await loadKeys() })
</script>

<style scoped>
.vip-page__toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.vip-page__toolbar .el-input { max-width: 240px; }
.vip-page__toolbar .el-select { width: 160px; }
</style>
