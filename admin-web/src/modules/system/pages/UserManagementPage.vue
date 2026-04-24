<template>
  <PageContainer title="用户VIP" description="统一管理用户会员状态，支持手动开通、延期和作废会员记录。">
    <div class="vip-page__toolbar">
      <el-input v-model="filters.keyword" clearable placeholder="搜索昵称/账号/手机/邮箱" @keyup.enter="loadUsers" />
      <el-select v-model="filters.vipStatus" clearable placeholder="会员状态">
        <el-option label="VIP用户" :value="1" />
        <el-option label="非VIP" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadUsers">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="users" row-key="userId">
      <el-table-column prop="nickname" label="用户" min-width="150" />
      <el-table-column prop="username" label="账号" min-width="140" />
      <el-table-column prop="phone" label="手机" min-width="130" />
      <el-table-column label="VIP状态" min-width="160"><template #default="{ row }"><el-tag :type="row.isVip ? 'success' : 'info'">{{ row.isVip ? vipTypeLabel(row.vipType) : '非VIP' }}</el-tag><span v-if="row.vipExpireAt" class="vip-page__muted"> {{ row.vipExpireAt }}</span></template></el-table-column>
      <el-table-column prop="vipRecordCount" label="记录数" width="90" />
      <el-table-column prop="lastLoginAt" label="最后登录" width="180" />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openRecords(row)">会员记录</el-button><el-button link type="success" @click="openGrant(row)">开通/延期</el-button></template></el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" size="760px" destroy-on-close>
      <template #header><div><strong>{{ selectedUser?.nickname }}</strong><div class="vip-page__muted">用户ID：{{ selectedUser?.userId }}</div></div></template>
      <el-table v-loading="recordsLoading" :data="records" row-key="id">
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ vipTypeLabel(row.vipType) }}</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.vipStatus === 1 ? 'success' : 'info'">{{ row.vipStatus === 1 ? '生效中' : '已失效' }}</el-tag></template></el-table-column>
        <el-table-column prop="sourceType" label="来源" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="invalidReason" label="作废原因" min-width="140" />
        <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="danger" :disabled="row.vipStatus !== 1" @click="voidRecord(row)">作废</el-button></template></el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="grantVisible" title="开通/延期会员" width="520px">
      <el-form ref="grantFormRef" :model="grantForm" :rules="grantRules" label-width="96px">
        <el-form-item label="用户"><span>{{ grantUser?.nickname }}</span></el-form-item>
        <el-form-item label="VIP类型" prop="vipType"><el-select v-model="grantForm.vipType" @change="syncDurationByType"><el-option label="月卡" value="month" /><el-option label="季卡" value="quarter" /><el-option label="年卡" value="year" /><el-option label="永久" value="lifetime" /><el-option label="自定义" value="custom" /></el-select></el-form-item>
        <el-form-item label="有效天数" prop="durationDays"><el-input-number v-model="grantForm.durationDays" :min="0" :max="3650" :disabled="grantForm.vipType === 'lifetime'" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="grantForm.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="grantForm.remark" maxlength="255" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="grantVisible = false">取消</el-button><el-button type="primary" :loading="grantSaving" @click="submitGrant">确认</el-button></template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import type { AdminVipRecord, AdminVipUser } from '@/shared/types/admin'
import { fetchVipRecords, fetchVipUsers, grantVip, voidVipRecord } from '@/modules/system/api/vip'

const users = ref<AdminVipUser[]>([])
const records = ref<AdminVipRecord[]>([])
const selectedUser = ref<AdminVipUser | null>(null)
const grantUser = ref<AdminVipUser | null>(null)
const loading = ref(false)
const recordsLoading = ref(false)
const grantSaving = ref(false)
const drawerVisible = ref(false)
const grantVisible = ref(false)
const grantFormRef = ref<FormInstance>()
const filters = reactive<{ keyword: string; vipStatus: number | undefined }>({ keyword: '', vipStatus: undefined })
const grantForm = reactive({ vipType: 'month', durationDays: 30, amount: 0, remark: '' })
const grantRules: FormRules = { vipType: [{ required: true, message: '请选择VIP类型', trigger: 'change' }], durationDays: [{ required: true, message: '请输入有效天数', trigger: 'change' }] }

function vipTypeLabel(type?: string | null) { return ({ month: '月卡', quarter: '季卡', year: '年卡', lifetime: '永久', custom: '自定义' } as Record<string, string>)[type || ''] || '-' }
function syncDurationByType() { grantForm.durationDays = ({ month: 30, quarter: 90, year: 365, lifetime: 0, custom: 30 } as Record<string, number>)[grantForm.vipType] ?? 30 }
async function loadUsers() { loading.value = true; try { users.value = await fetchVipUsers({ keyword: filters.keyword || undefined, vipStatus: filters.vipStatus }) } finally { loading.value = false } }
function resetFilters() { filters.keyword = ''; filters.vipStatus = undefined; loadUsers() }
async function openRecords(row: AdminVipUser) { selectedUser.value = row; drawerVisible.value = true; recordsLoading.value = true; try { records.value = await fetchVipRecords(row.userId) } finally { recordsLoading.value = false } }
function openGrant(row: AdminVipUser) { grantUser.value = row; Object.assign(grantForm, { vipType: 'month', durationDays: 30, amount: 0, remark: '' }); grantVisible.value = true }
async function submitGrant() { await grantFormRef.value?.validate(); if (!grantUser.value) return; grantSaving.value = true; try { await grantVip(grantUser.value.userId, { ...grantForm, remark: grantForm.remark || null }); ElMessage.success('会员已开通/延期'); grantVisible.value = false; await loadUsers() } finally { grantSaving.value = false } }
async function voidRecord(row: AdminVipRecord) { if (!selectedUser.value) return; const { value } = await ElMessageBox.prompt('请输入作废原因', '作废会员记录', { inputPattern: /\S+/, inputErrorMessage: '作废原因不能为空' }); await voidVipRecord(selectedUser.value.userId, row.id, value); ElMessage.success('会员记录已作废'); await openRecords(selectedUser.value); await loadUsers() }
onMounted(loadUsers)
</script>

<style scoped>
.vip-page__toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.vip-page__toolbar .el-input { max-width: 280px; }
.vip-page__toolbar .el-select { width: 150px; }
.vip-page__muted { color: var(--admin-text-secondary); font-size: 12px; margin-left: 4px; }
</style>
