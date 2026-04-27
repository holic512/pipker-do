<template>
  <PageContainer title="系统配置" description="维护通用系统配置、OpenAI LLM 测试调用和相关记录。">
    <section class="system-config-page">
      <div class="system-config-page__stats admin-card">
        <div class="system-config-page__stat">
          <span class="system-config-page__stat-label">配置项</span>
          <strong>{{ configStats.total }}</strong>
        </div>
        <el-divider direction="vertical" />
        <div class="system-config-page__stat">
          <span class="system-config-page__stat-label">已启用</span>
          <strong class="is-success">{{ configStats.enabled }}</strong>
        </div>
        <el-divider direction="vertical" />
        <div class="system-config-page__stat">
          <span class="system-config-page__stat-label">敏感配置</span>
          <strong class="is-warning">{{ configStats.sensitive }}</strong>
        </div>
        <el-divider direction="vertical" />
        <div class="system-config-page__stat">
          <span class="system-config-page__stat-label">最近调用</span>
          <strong>{{ recordPage.total }}</strong>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="system-config-page__tabs">
        <el-tab-pane label="配置管理" name="configs">
          <div class="system-config-page__toolbar">
            <div class="system-config-page__toolbar-left">
              <el-select v-model="configFilters.group" clearable placeholder="全部分组" class="system-config-page__group-select" @change="loadConfigs">
                <el-option label="llm.openai" value="llm.openai" />
              </el-select>
              <el-input
                v-model="configFilters.keyword"
                clearable
                placeholder="搜索配置名称 / Key"
                class="system-config-page__search"
                :prefix-icon="Search"
                @keyup.enter="loadConfigs"
                @clear="loadConfigs"
              />
              <el-button type="primary" plain :icon="Search" @click="loadConfigs">查询</el-button>
              <el-button :icon="Refresh" @click="resetConfigFilters">重置</el-button>
            </div>
          </div>

          <el-table v-loading="configLoading" :data="configs" row-key="id" class="system-config-page__table">
            <el-table-column label="配置项" min-width="260">
              <template #default="{ row }">
                <div class="system-config-page__config-name">{{ row.configName }}</div>
                <div class="system-config-page__config-key">{{ row.configKey }}</div>
              </template>
            </el-table-column>
            <el-table-column label="分组" prop="configGroup" width="130" />
            <el-table-column label="类型" width="110">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.configType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="当前值" min-width="260">
              <template #default="{ row }">
                <span class="system-config-page__value">{{ displayConfigValue(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
                  {{ row.enabled === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="更新时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openConfigDialog(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="AI 测试" name="test">
          <div class="system-config-page__test-grid">
            <el-form label-position="top" class="system-config-page__test-form admin-card">
              <el-form-item label="调用模式">
                <el-radio-group v-model="testForm.mode">
                  <el-radio-button value="text">文本</el-radio-button>
                  <el-radio-button value="json">JSON</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="场景">
                <el-input v-model="testForm.scene" maxlength="80" />
              </el-form-item>
              <el-form-item label="System Prompt">
                <el-input v-model="testForm.systemPrompt" type="textarea" :rows="4" />
              </el-form-item>
              <el-form-item label="User Prompt">
                <el-input v-model="testForm.userPrompt" type="textarea" :rows="5" />
              </el-form-item>
              <template v-if="testForm.mode === 'json'">
                <el-form-item label="Schema Name">
                  <el-input v-model="testForm.schemaName" maxlength="64" />
                </el-form-item>
                <el-form-item label="JSON Schema">
                  <el-input v-model="testForm.jsonSchema" type="textarea" :rows="8" class="system-config-page__mono-input" />
                </el-form-item>
              </template>
              <div class="system-config-page__actions">
                <el-button type="primary" :icon="Cpu" :loading="testing" @click="submitLlmTest">发起测试</el-button>
              </div>
            </el-form>

            <div class="system-config-page__result admin-card">
              <div class="system-config-page__result-head">
                <span>测试结果</span>
                <el-tag v-if="testResult" type="success" effect="light">record #{{ testResult.recordId }}</el-tag>
              </div>
              <el-empty v-if="!testResult" description="暂无测试结果" :image-size="72" />
              <template v-else>
                <div class="system-config-page__meta-grid">
                  <div>
                    <span>模型</span>
                    <strong>{{ testResult.model }}</strong>
                  </div>
                  <div>
                    <span>耗时</span>
                    <strong>{{ testResult.latencyMs }}ms</strong>
                  </div>
                  <div>
                    <span>Tokens</span>
                    <strong>{{ testResult.usage.totalTokens ?? '-' }}</strong>
                  </div>
                </div>
                <pre class="system-config-page__output">{{ testResult.jsonContent || testResult.content }}</pre>
              </template>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="AI 记录" name="records">
          <div class="system-config-page__toolbar">
            <div class="system-config-page__toolbar-left">
              <el-select v-model="recordFilters.status" clearable placeholder="全部状态" class="system-config-page__status-select" @change="loadRecords">
                <el-option label="成功" value="success" />
                <el-option label="失败" value="failed" />
              </el-select>
              <el-input
                v-model="recordFilters.scene"
                clearable
                placeholder="搜索场景"
                class="system-config-page__search"
                :prefix-icon="Search"
                @keyup.enter="loadRecords"
                @clear="loadRecords"
              />
              <el-button type="primary" plain :icon="Search" @click="loadRecords">查询</el-button>
              <el-button :icon="Refresh" @click="resetRecordFilters">重置</el-button>
            </div>
          </div>

          <el-table v-loading="recordLoading" :data="records" row-key="id" class="system-config-page__table">
            <el-table-column label="场景" prop="scene" min-width="160" />
            <el-table-column label="模型" prop="model" min-width="140" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="耗时" width="100">
              <template #default="{ row }">{{ row.latencyMs }}ms</template>
            </el-table-column>
            <el-table-column label="Tokens" width="110">
              <template #default="{ row }">{{ row.totalTokens ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="输出预览" min-width="260">
              <template #default="{ row }">
                <span class="system-config-page__value">{{ row.errorMessage || row.outputPreview || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openRecordDrawer(row.id)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="recordPage.pageNo"
            v-model:page-size="recordPage.pageSize"
            class="system-config-page__pagination"
            layout="total, sizes, prev, pager, next"
            :total="recordPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadRecords"
            @size-change="loadRecords"
          />
        </el-tab-pane>

        <el-tab-pane label="变更记录" name="changeLogs">
          <div class="system-config-page__toolbar">
            <div class="system-config-page__toolbar-left">
              <el-input
                v-model="changeLogFilters.configKey"
                clearable
                placeholder="配置 Key"
                class="system-config-page__search"
                :prefix-icon="Search"
                @keyup.enter="loadChangeLogs"
                @clear="loadChangeLogs"
              />
              <el-button type="primary" plain :icon="Search" @click="loadChangeLogs">查询</el-button>
              <el-button :icon="Refresh" @click="resetChangeLogFilters">重置</el-button>
            </div>
          </div>

          <el-table v-loading="changeLogLoading" :data="changeLogs" row-key="id" class="system-config-page__table">
            <el-table-column label="配置 Key" prop="configKey" min-width="220" />
            <el-table-column label="旧值" min-width="220">
              <template #default="{ row }">
                <span class="system-config-page__value">{{ row.oldValueMasked || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="新值" min-width="220">
              <template #default="{ row }">
                <span class="system-config-page__value">{{ row.newValueMasked || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作人" width="100">
              <template #default="{ row }">{{ row.changedBy || '-' }}</template>
            </el-table-column>
            <el-table-column label="请求ID" prop="requestId" min-width="180" />
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="changeLogPage.pageNo"
            v-model:page-size="changeLogPage.pageSize"
            class="system-config-page__pagination"
            layout="total, sizes, prev, pager, next"
            :total="changeLogPage.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadChangeLogs"
            @size-change="loadChangeLogs"
          />
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="configDialogVisible" :title="editingConfig ? `编辑 ${editingConfig.configName}` : '编辑配置'" width="640px" destroy-on-close>
      <el-form v-if="editingConfig" label-position="top">
        <el-form-item label="配置 Key">
          <el-input :model-value="editingConfig.configKey" disabled />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="configForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <template v-if="editingConfig.sensitive">
          <el-form-item label="当前值">
            <el-input :model-value="editingConfig.maskedValue || '未配置'" disabled />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="configForm.keepSensitiveValue">保留原密钥</el-checkbox>
          </el-form-item>
          <el-form-item label="新值">
            <el-input v-model="configForm.value" type="password" show-password :disabled="configForm.keepSensitiveValue" />
          </el-form-item>
        </template>
        <el-form-item v-else label="配置值">
          <el-input
            v-model="configForm.value"
            :type="editingConfig.configType === 'json' ? 'textarea' : 'text'"
            :rows="editingConfig.configType === 'json' ? 8 : 2"
            :class="{ 'system-config-page__mono-input': editingConfig.configType === 'json' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="system-config-page__actions">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingConfig" @click="submitConfig">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="recordDrawerVisible" size="720px" title="AI 调用详情" destroy-on-close>
      <div v-loading="recordDetailLoading" class="system-config-page__drawer">
        <template v-if="recordDetail">
          <div class="system-config-page__detail-grid">
            <div><span>场景</span><strong>{{ recordDetail.scene }}</strong></div>
            <div><span>模型</span><strong>{{ recordDetail.model || '-' }}</strong></div>
            <div><span>状态</span><strong>{{ recordDetail.status }}</strong></div>
            <div><span>耗时</span><strong>{{ recordDetail.latencyMs }}ms</strong></div>
            <div><span>Tokens</span><strong>{{ recordDetail.totalTokens ?? '-' }}</strong></div>
            <div><span>请求ID</span><strong>{{ recordDetail.requestId || '-' }}</strong></div>
          </div>
          <h3>输入预览</h3>
          <pre class="system-config-page__output">{{ recordDetail.inputPreview || '-' }}</pre>
          <h3>{{ recordDetail.status === 'success' ? '输出预览' : '错误信息' }}</h3>
          <pre class="system-config-page__output">{{ recordDetail.errorMessage || recordDetail.outputPreview || '-' }}</pre>
        </template>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
// ai-index: admin system config and llm records page
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Cpu, Edit, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import {
  fetchLlmCallRecordDetail,
  fetchLlmCallRecords,
  fetchSystemConfigChangeLogs,
  fetchSystemConfigs,
  testLlm,
  updateSystemConfig,
  type AdminLlmTestRequest
} from '@/modules/system/api/config'
import type { LlmCallRecord, LlmGenerateResult, SystemConfigChangeLog, SystemConfigItem } from '@/shared/types/admin'

type ActiveTab = 'configs' | 'test' | 'records' | 'changeLogs'

const activeTab = ref<ActiveTab>('configs')

const configLoading = ref(false)
const configs = ref<SystemConfigItem[]>([])
const configFilters = reactive({
  group: '',
  keyword: ''
})

const configDialogVisible = ref(false)
const savingConfig = ref(false)
const editingConfig = ref<SystemConfigItem | null>(null)
const configForm = reactive({
  value: '',
  enabled: 1,
  keepSensitiveValue: true
})

const testing = ref(false)
const testResult = ref<LlmGenerateResult | null>(null)
const testForm = reactive<AdminLlmTestRequest>({
  mode: 'text',
  scene: 'admin-test',
  systemPrompt: '你是后台配置连通性测试助手，只返回必要结果。',
  userPrompt: '请用一句话回复：LLM 配置测试成功。',
  schemaName: 'llm_test_result',
  jsonSchema: JSON.stringify(
    {
      type: 'object',
      properties: {
        ok: { type: 'boolean' },
        message: { type: 'string' }
      },
      required: ['ok', 'message'],
      additionalProperties: false
    },
    null,
    2
  )
})

const recordLoading = ref(false)
const records = ref<LlmCallRecord[]>([])
const recordFilters = reactive({
  status: '',
  scene: ''
})
const recordPage = reactive({
  pageNo: 1,
  pageSize: 20,
  total: 0
})
const recordDrawerVisible = ref(false)
const recordDetailLoading = ref(false)
const recordDetail = ref<LlmCallRecord | null>(null)

const changeLogLoading = ref(false)
const changeLogs = ref<SystemConfigChangeLog[]>([])
const changeLogFilters = reactive({
  configKey: ''
})
const changeLogPage = reactive({
  pageNo: 1,
  pageSize: 20,
  total: 0
})

const configStats = computed(() => ({
  total: configs.value.length,
  enabled: configs.value.filter((item) => item.enabled === 1).length,
  sensitive: configs.value.filter((item) => item.sensitive).length
}))

watch(activeTab, (tab) => {
  if (tab === 'records' && records.value.length === 0) {
    loadRecords()
  }
  if (tab === 'changeLogs' && changeLogs.value.length === 0) {
    loadChangeLogs()
  }
})

onMounted(async () => {
  await Promise.all([loadConfigs(), loadRecords()])
})

async function loadConfigs() {
  configLoading.value = true
  try {
    configs.value = await fetchSystemConfigs({
      group: configFilters.group || undefined,
      keyword: configFilters.keyword || undefined
    })
  } finally {
    configLoading.value = false
  }
}

function resetConfigFilters() {
  configFilters.group = ''
  configFilters.keyword = ''
  loadConfigs()
}

function displayConfigValue(row: SystemConfigItem) {
  if (row.sensitive) {
    return row.hasValue ? row.maskedValue || '********' : '未配置'
  }
  return row.value || '-'
}

function openConfigDialog(row: SystemConfigItem) {
  editingConfig.value = row
  configForm.enabled = row.enabled
  configForm.keepSensitiveValue = row.sensitive
  configForm.value = row.sensitive ? '' : row.value || ''
  configDialogVisible.value = true
}

async function submitConfig() {
  if (!editingConfig.value) {
    return
  }
  savingConfig.value = true
  try {
    await updateSystemConfig(editingConfig.value.configKey, {
      value: configForm.value,
      enabled: configForm.enabled,
      keepSensitiveValue: configForm.keepSensitiveValue
    })
    ElMessage.success('配置已保存')
    configDialogVisible.value = false
    await Promise.all([loadConfigs(), loadChangeLogs()])
  } finally {
    savingConfig.value = false
  }
}

async function submitLlmTest() {
  testing.value = true
  try {
    const response = await testLlm({ ...testForm })
    testResult.value = response.result
    ElMessage.success('测试调用已完成')
    recordPage.pageNo = 1
    await loadRecords()
  } finally {
    testing.value = false
  }
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const result = await fetchLlmCallRecords({
      status: recordFilters.status || undefined,
      scene: recordFilters.scene || undefined,
      pageNo: recordPage.pageNo,
      pageSize: recordPage.pageSize
    })
    records.value = result.records
    recordPage.total = result.total
  } finally {
    recordLoading.value = false
  }
}

function resetRecordFilters() {
  recordFilters.status = ''
  recordFilters.scene = ''
  recordPage.pageNo = 1
  loadRecords()
}

async function openRecordDrawer(recordId: number) {
  recordDrawerVisible.value = true
  recordDetailLoading.value = true
  recordDetail.value = null
  try {
    recordDetail.value = await fetchLlmCallRecordDetail(recordId)
  } finally {
    recordDetailLoading.value = false
  }
}

async function loadChangeLogs() {
  changeLogLoading.value = true
  try {
    const result = await fetchSystemConfigChangeLogs({
      configKey: changeLogFilters.configKey || undefined,
      pageNo: changeLogPage.pageNo,
      pageSize: changeLogPage.pageSize
    })
    changeLogs.value = result.records
    changeLogPage.total = result.total
  } finally {
    changeLogLoading.value = false
  }
}

function resetChangeLogFilters() {
  changeLogFilters.configKey = ''
  changeLogPage.pageNo = 1
  loadChangeLogs()
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const pad = (item: number) => String(item).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
</script>

<style scoped lang="scss">
.system-config-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.system-config-page__stats {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
}

.system-config-page__stat {
  min-width: 120px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  strong {
    font-size: 24px;
    color: var(--admin-text);
  }

  .is-success {
    color: #16a34a;
  }

  .is-warning {
    color: #d97706;
  }
}

.system-config-page__stat-label,
.system-config-page__config-key,
.system-config-page__value,
.system-config-page__detail-grid span,
.system-config-page__meta-grid span {
  color: var(--admin-text-soft);
  font-size: 13px;
}

.system-config-page__tabs {
  padding: 20px 22px 24px;
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-lg);
  background: var(--admin-surface);
}

.system-config-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.system-config-page__toolbar-left {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.system-config-page__group-select,
.system-config-page__status-select {
  width: 150px;
}

.system-config-page__search {
  width: 260px;
}

.system-config-page__config-name {
  font-weight: 600;
  color: var(--admin-text);
}

.system-config-page__config-key {
  margin-top: 4px;
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
}

.system-config-page__value {
  display: inline-block;
  max-width: 460px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.system-config-page__test-grid {
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(360px, 1.1fr);
  gap: 18px;
}

.system-config-page__test-form,
.system-config-page__result {
  padding: 22px;
}

.system-config-page__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.system-config-page__result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  font-weight: 700;
}

.system-config-page__meta-grid,
.system-config-page__detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;

  div {
    min-height: 72px;
    padding: 14px;
    border: 1px solid var(--admin-border);
    border-radius: var(--admin-radius-md);
    background: var(--admin-bg);
  }

  strong {
    display: block;
    margin-top: 8px;
    overflow-wrap: anywhere;
  }
}

.system-config-page__output {
  max-height: 420px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-md);
  background: #0f172a;
  color: #e2e8f0;
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.system-config-page__mono-input :deep(textarea) {
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
}

.system-config-page__pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

.system-config-page__drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;

  h3 {
    margin: 4px 0 0;
    font-size: 15px;
  }
}

@media (max-width: 1100px) {
  .system-config-page__test-grid {
    grid-template-columns: 1fr;
  }
}
</style>
