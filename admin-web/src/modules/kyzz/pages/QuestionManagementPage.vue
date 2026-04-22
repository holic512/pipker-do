<template>
  <PageContainer
    :title="`${projectName} · 题目管理`"
    description="围绕题干、答案、选项和题库归属维护单选、多选、简答题，并与题库题量实时联动。"
  >
    <template v-if="isKyzz">
      <div class="kyzz-question-page">
        <div class="overview-section">
          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="strategy-alert"
            title="表结构解读：kyzz_question 负责题干、答案、题库归属与启停状态，kyzz_question_option 负责客观题选项，question_count 将随着题目维护自动回写题库。"
          />

          <div class="stats-row">
            <div class="stat-item">
              <span class="label">题目总数</span>
              <span class="value">{{ dashboard.stats.totalQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">启用中</span>
              <span class="value success">{{ dashboard.stats.activeQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">已停用</span>
              <span class="value warning">{{ dashboard.stats.inactiveQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">单选题</span>
              <span class="value">{{ dashboard.stats.singleChoiceQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">多选题</span>
              <span class="value">{{ dashboard.stats.multipleChoiceQuestions }}</span>
            </div>
            <el-divider direction="vertical" />
            <div class="stat-item">
              <span class="label">简答题</span>
              <span class="value">{{ dashboard.stats.shortAnswerQuestions }}</span>
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
                placeholder="搜索题干 / 来源 / 标准答案"
                :prefix-icon="Search"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
              <el-select
                v-model="filters.questionBankId"
                clearable
                filterable
                placeholder="全部题库"
                class="bank-select"
                @change="handleSearch"
              >
                <el-option
                  v-for="bank in questionBankOptions"
                  :key="bank.id"
                  :label="formatBankLabel(bank)"
                  :value="bank.id"
                />
              </el-select>
              <el-select
                v-model="filters.categoryId"
                clearable
                filterable
                placeholder="全部分类"
                class="category-select"
                @change="handleSearch"
              >
                <el-option
                  v-for="category in categoryOptions"
                  :key="category.id"
                  :label="formatCategoryLabel(category)"
                  :value="category.id"
                />
              </el-select>
              <el-select
                v-model="filters.tagId"
                clearable
                filterable
                placeholder="全部标签"
                class="tag-select"
                @change="handleSearch"
              >
                <el-option
                  v-for="tag in questionTagOptions"
                  :key="tag.id"
                  :label="formatTagOptionLabel(tag)"
                  :value="tag.id"
                />
              </el-select>
              <el-select
                v-model="filters.questionType"
                clearable
                placeholder="全部题型"
                class="type-select"
                @change="handleSearch"
              >
                <el-option
                  v-for="option in questionTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <el-select
                v-model="filters.status"
                clearable
                placeholder="全部状态"
                class="status-select"
                @change="handleSearch"
              >
                <el-option label="启用中" :value="1" />
                <el-option label="已停用" :value="0" />
              </el-select>
              <el-select
                v-model="filters.difficultyLevel"
                clearable
                placeholder="全部难度"
                class="difficulty-select"
                @change="handleSearch"
              >
                <el-option
                  v-for="option in difficultyOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <el-input
                v-model="filters.yearNoText"
                clearable
                placeholder="年份"
                class="year-input"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
              <el-button type="primary" plain :icon="Search" @click="handleSearch">查询</el-button>
              <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
            </div>
            <div class="toolbar-right">
              <el-button type="primary" :icon="Plus" :disabled="!questionBankOptions.length" @click="openCreateDrawer">
                新增题目
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="prefilledBank"
            type="success"
            show-icon
            :closable="false"
            class="kyzz-question-page__entry-alert"
            :title="`当前由题库“${prefilledBank.bankName}”快捷进入，题库筛选已默认带出；重置筛选时会回到该题库。`"
          />
          <el-alert
            v-if="prefilledTag"
            type="info"
            show-icon
            :closable="false"
            class="kyzz-question-page__entry-alert"
            :title="`当前由标签“${prefilledTag.tagName}”快捷进入，标签筛选已默认带出；重置筛选时会回到该标签。`"
          />

          <el-table v-loading="loading" :data="dashboard.records" row-key="id" class="kyzz-question-page__table">
            <el-table-column label="题目内容" min-width="420">
              <template #default="{ row }">
                <div class="kyzz-question-page__content-cell">
                  <div class="kyzz-question-page__content-head">
                    <el-tag size="small" effect="plain">{{ questionTypeLabel(row.questionType) }}</el-tag>
                    <el-tag size="small" type="info" effect="light">{{ difficultyLabel(row.difficultyLevel) }}</el-tag>
                    <span class="kyzz-question-page__content-meta">排序 {{ row.sortNo }}</span>
                  </div>
                  <div class="kyzz-question-page__stem">{{ row.stemPreview }}</div>
                  <div v-if="row.sourceName || row.yearNo" class="kyzz-question-page__subtext">
                    <span v-if="row.sourceName">来源：{{ row.sourceName }}</span>
                    <span v-if="row.yearNo">年份：{{ row.yearNo }}</span>
                  </div>
                  <div class="kyzz-question-page__tag-strip">
                    <template v-if="row.tags.length">
                      <el-tag
                        v-for="tag in row.tags"
                        :key="`${row.id}-${tag.id}`"
                        size="small"
                        effect="light"
                        :style="buildTagStyle(tag.color)"
                      >
                        {{ tag.tagName }}
                      </el-tag>
                    </template>
                    <span v-else class="kyzz-question-page__content-meta">未打标签</span>
                  </div>
                  <div v-if="row.analysis" class="kyzz-question-page__analysis">
                    解析：{{ previewText(row.analysis, 72) }}
                  </div>
                  <div v-if="row.deleteBlockReason" class="kyzz-question-page__warning">
                    {{ row.deleteBlockReason }}
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="归属信息" min-width="230">
              <template #default="{ row }">
                <div class="kyzz-question-page__ownership">
                  <div class="kyzz-question-page__ownership-main">{{ row.questionBankName || '未归属题库' }}</div>
                  <div class="kyzz-question-page__subtext">
                    {{ row.categoryName || '未单独归类' }}
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="答案摘要" min-width="240">
              <template #default="{ row }">
                <div class="kyzz-question-page__answer-cell">
                  <div class="kyzz-question-page__answer-tags">
                    <template v-if="row.questionType !== 'short'">
                      <el-tag
                        v-for="key in row.correctOptionKeys"
                        :key="`${row.id}-${key}`"
                        size="small"
                        type="success"
                        effect="light"
                      >
                        {{ key }}
                      </el-tag>
                      <span class="kyzz-question-page__content-meta">共 {{ row.optionCount }} 个选项</span>
                    </template>
                    <span v-else class="kyzz-question-page__content-meta">简答标准答案</span>
                  </div>
                  <div class="kyzz-question-page__answer-text">{{ previewText(row.answerText, 72) }}</div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="分值 / 年份" width="140" align="center">
              <template #default="{ row }">
                <div class="kyzz-question-page__metric-stack">
                  <span>{{ Number(row.score).toFixed(2) }} 分</span>
                  <span>{{ row.yearNo || '未设年份' }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status === 1"
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

            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
                <el-button link type="primary" :icon="CopyDocument" @click="openCopyDrawer(row)">复制</el-button>
                <el-button
                  link
                  type="danger"
                  :icon="Delete"
                  :disabled="deletingId === row.id"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="kyzz-question-page__pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="dashboard.pagination.pageNo"
              :page-size="dashboard.pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="dashboard.pagination.total"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </div>

      <el-drawer
        v-model="drawerVisible"
        :title="drawerTitle"
        size="960px"
        destroy-on-close
        class="kyzz-question-page__drawer"
      >
        <div v-loading="drawerLoading" class="kyzz-question-page__drawer-body">
          <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
            <div class="kyzz-question-page__section">
              <div class="kyzz-question-page__section-title">基本信息</div>
              <div class="kyzz-question-page__form-grid">
                <el-form-item label="所属题库" prop="questionBankId">
                  <el-select
                    v-model="form.questionBankId"
                    filterable
                    placeholder="请选择题库"
                    @change="handleFormBankChange"
                  >
                    <el-option
                      v-for="bank in questionBankOptions"
                      :key="bank.id"
                      :label="formatBankLabel(bank)"
                      :value="bank.id"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="题型" prop="questionType">
                  <el-radio-group v-model="form.questionType">
                    <el-radio
                      v-for="option in questionTypeOptions"
                      :key="option.value"
                      :value="option.value"
                    >
                      {{ option.label }}
                    </el-radio>
                  </el-radio-group>
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

                <el-form-item label="题目分值" prop="score">
                  <el-input-number
                    v-model="form.score"
                    :min="0.01"
                    :max="9999.99"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                  />
                </el-form-item>

                <el-form-item label="年份" prop="yearNo">
                  <el-input-number
                    v-model="form.yearNo"
                    :min="1900"
                    :max="currentYear + 1"
                    controls-position="right"
                  />
                </el-form-item>

                <el-form-item label="题目来源" prop="sourceName">
                  <el-input v-model="form.sourceName" maxlength="100" placeholder="例如 2026 模拟卷一" />
                </el-form-item>

                <el-form-item label="排序值" prop="sortNo">
                  <el-input-number v-model="form.sortNo" :min="0" :max="9999" controls-position="right" />
                </el-form-item>

                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio :value="1">启用</el-radio>
                    <el-radio :value="0">停用</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item class="is-span-2" label="题目标签">
                  <div class="kyzz-question-page__tag-panel">
                    <el-select
                      v-model="form.tagIds"
                      multiple
                      filterable
                      clearable
                      collapse-tags
                      collapse-tags-tooltip
                      placeholder="可选多个标签，不打标签也可保存"
                    >
                      <el-option
                        v-for="tag in questionTagOptions"
                        :key="tag.id"
                        :label="formatTagOptionLabel(tag)"
                        :value="tag.id"
                      />
                    </el-select>
                    <div class="kyzz-question-page__tag-tip">
                      建议用标签标记“高频考点”“易错题”“真题改编”等运营语义，便于后续筛选和维护。
                    </div>
                  </div>
                </el-form-item>

                <el-form-item class="is-span-2" label="分类联动">
                  <div class="kyzz-question-page__category-panel">
                    <div class="kyzz-question-page__category-head">
                      <div class="kyzz-question-page__category-summary">
                        <span class="title">题目分类</span>
                        <span class="desc">
                          {{ followBankCategory ? followBankCategoryText : '当前题目已单独归类，切换题库时不会自动覆盖分类。' }}
                        </span>
                      </div>
                      <el-switch
                        v-model="followBankCategory"
                        inline-prompt
                        active-text="跟随"
                        inactive-text="单设"
                        @change="handleFollowBankCategoryChange"
                      />
                    </div>

                    <el-select
                      v-model="form.categoryId"
                      clearable
                      filterable
                      placeholder="请选择分类"
                      :disabled="followBankCategory"
                      @clear="handleCategoryClear"
                    >
                      <el-option
                        v-for="category in categoryOptions"
                        :key="category.id"
                        :label="formatCategoryLabel(category)"
                        :value="category.id"
                        :disabled="category.isEnabled !== 1 && category.id !== form.categoryId"
                      />
                    </el-select>
                  </div>
                </el-form-item>
              </div>
            </div>

            <div class="kyzz-question-page__section">
              <div class="kyzz-question-page__section-title">题干与解析</div>
              <el-form-item label="题干" prop="stem">
                <el-input
                  v-model="form.stem"
                  type="textarea"
                  :rows="6"
                  maxlength="20000"
                  show-word-limit
                  placeholder="请输入完整题干，必要时可包含材料背景、题目设问和作答要求。"
                />
              </el-form-item>
              <el-form-item label="解析" prop="analysis">
                <el-input
                  v-model="form.analysis"
                  type="textarea"
                  :rows="5"
                  maxlength="20000"
                  show-word-limit
                  placeholder="建议补充考点、解题思路、易错点或简答题评分要点。"
                />
              </el-form-item>
            </div>

            <div class="kyzz-question-page__section">
              <div class="kyzz-question-page__section-title">答案设置</div>

              <el-alert
                v-if="isObjectiveQuestion"
                type="info"
                show-icon
                :closable="false"
                title="客观题的标准答案会根据你勾选的正确选项自动生成，无需手动输入答案字母。"
              />
              <el-alert
                v-else
                type="warning"
                show-icon
                :closable="false"
                title="简答题不保存选项，请直接填写标准答案或参考要点。"
              />

              <template v-if="isObjectiveQuestion">
                <div class="kyzz-question-page__objective-toolbar">
                  <div class="kyzz-question-page__objective-summary">
                    <span class="label">标准答案预览</span>
                    <span class="value">{{ objectiveAnswerPreview || '尚未设置正确答案' }}</span>
                    <span class="hint">建议直接在下方勾选正确项，系统会自动同步答案。</span>
                  </div>
                  <el-button plain :disabled="form.options.length >= 8" @click="addOption">新增选项</el-button>
                </div>

                <el-radio-group
                  v-if="form.questionType === 'single'"
                  v-model="singleCorrectKey"
                  class="kyzz-question-page__option-list"
                >
                  <div
                    v-for="(option, index) in form.options"
                    :key="`single-${option.optionKey}-${index}`"
                    class="kyzz-question-page__option-row"
                  >
                    <div class="kyzz-question-page__option-head">
                      <div class="kyzz-question-page__option-meta">
                        <span class="kyzz-question-page__option-key">{{ option.optionKey }}</span>
                        <span class="kyzz-question-page__option-title">选项 {{ option.optionKey }}</span>
                      </div>
                      <div class="kyzz-question-page__option-actions">
                        <el-radio :value="option.optionKey">正确答案</el-radio>
                        <el-button
                          link
                          type="danger"
                          :disabled="form.options.length <= 2"
                          @click.prevent="removeOption(index)"
                        >
                          移除
                        </el-button>
                      </div>
                    </div>
                    <el-input
                      v-model="option.optionContent"
                      type="textarea"
                      :rows="3"
                      maxlength="10000"
                      show-word-limit
                      placeholder="请输入选项内容，支持较长文本或材料片段。"
                    />
                  </div>
                </el-radio-group>

                <el-checkbox-group
                  v-else
                  v-model="multipleCorrectKeys"
                  class="kyzz-question-page__option-list"
                >
                  <div
                    v-for="(option, index) in form.options"
                    :key="`multiple-${option.optionKey}-${index}`"
                    class="kyzz-question-page__option-row"
                  >
                    <div class="kyzz-question-page__option-head">
                      <div class="kyzz-question-page__option-meta">
                        <span class="kyzz-question-page__option-key">{{ option.optionKey }}</span>
                        <span class="kyzz-question-page__option-title">选项 {{ option.optionKey }}</span>
                      </div>
                      <div class="kyzz-question-page__option-actions">
                        <el-checkbox :value="option.optionKey">正确答案</el-checkbox>
                        <el-button
                          link
                          type="danger"
                          :disabled="form.options.length <= 2"
                          @click.prevent="removeOption(index)"
                        >
                          移除
                        </el-button>
                      </div>
                    </div>
                    <el-input
                      v-model="option.optionContent"
                      type="textarea"
                      :rows="3"
                      maxlength="10000"
                      show-word-limit
                      placeholder="请输入选项内容，支持较长文本或材料片段。"
                    />
                  </div>
                </el-checkbox-group>
              </template>

              <el-form-item v-else label="标准答案" prop="answerText">
                <el-input
                  v-model="form.answerText"
                  type="textarea"
                  :rows="6"
                  maxlength="20000"
                  show-word-limit
                  placeholder="建议写清参考答案、作答结构或评分要点。"
                />
              </el-form-item>
            </div>
          </el-form>
        </div>

        <template #footer>
          <div class="kyzz-question-page__dialog-actions">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitForm">
              {{ drawerMode === 'edit' ? '保存修改' : '保存题目' }}
            </el-button>
          </div>
        </template>
      </el-drawer>
    </template>

    <el-empty
      v-else
      description="当前页面仅对考研政治项目开放。请先切换到 kyzz 项目工作区。"
    />
  </PageContainer>
</template>

<script setup lang="ts">
// AI 索引: KYZZ 题目管理页面。
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { CopyDocument, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageContainer from '@/shared/components/PageContainer.vue'
import { useAdminSessionStore } from '@/modules/system/store/session'
import type {
  KyzzCategoryOption,
  KyzzQuestionAdminDashboard,
  KyzzQuestionAdminDetail,
  KyzzQuestionAdminItem,
  KyzzQuestionBankOption,
  KyzzQuestionTagOption
} from '@/shared/types/admin'
import {
  createKyzzQuestion,
  deleteKyzzQuestion,
  fetchKyzzQuestionDashboard,
  fetchKyzzQuestionDetail,
  updateKyzzQuestion,
  updateKyzzQuestionStatus
} from '@/modules/kyzz/api/question'

interface QuestionOptionForm {
  optionKey: string
  optionContent: string
  isCorrect: number
}

const route = useRoute()
const sessionStore = useAdminSessionStore()
const currentYear = new Date().getFullYear()

const projectCode = computed(() => String(route.params.projectCode || sessionStore.currentProjectCode || 'kyzz'))
const isKyzz = computed(() => projectCode.value === 'kyzz')
const projectName = computed(() => {
  return sessionStore.availableProjects.find((item) => item.code === projectCode.value)?.name || '考研政治'
})

const questionTypeOptions = [
  { label: '单选题', value: 'single' },
  { label: '多选题', value: 'multiple' },
  { label: '简答题', value: 'short' }
]

const difficultyOptions = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 }
]

const loading = ref(false)
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)
const editingId = ref<number | null>(null)
const drawerMode = ref<'create' | 'edit' | 'copy'>('create')
const followBankCategory = ref(true)
const formRef = ref<FormInstance>()

const dashboard = reactive<KyzzQuestionAdminDashboard>({
  stats: {
    totalQuestions: 0,
    activeQuestions: 0,
    inactiveQuestions: 0,
    singleChoiceQuestions: 0,
    multipleChoiceQuestions: 0,
    shortAnswerQuestions: 0
  },
  records: [],
  pagination: {
    pageNo: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  },
  questionBanks: [],
  categories: [],
  tags: []
})

const filters = reactive({
  keyword: '',
  questionBankId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  tagId: undefined as number | undefined,
  questionType: undefined as string | undefined,
  status: undefined as number | undefined,
  difficultyLevel: undefined as number | undefined,
  yearNoText: '',
  pageNo: 1,
  pageSize: 20
})

const form = reactive({
  questionBankId: null as number | null,
  categoryId: null as number | null,
  questionType: 'single',
  stem: '',
  analysis: '',
  answerText: '',
  difficultyLevel: 2,
  score: 1,
  sourceName: '',
  yearNo: currentYear as number | null,
  sortNo: 0,
  status: 1,
  tagIds: [] as number[],
  options: [] as QuestionOptionForm[]
})

const formRules: FormRules = {
  questionBankId: [{ required: true, message: '请选择所属题库', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  stem: [
    { required: true, message: '请输入题干', trigger: 'blur' },
    { min: 2, max: 20000, message: '题干长度需在 2 到 20000 个字符之间', trigger: 'blur' }
  ],
  answerText: [
    {
      validator: (_, value, callback) => {
        if (form.questionType !== 'short') {
          callback()
          return
        }
        if (!(value || '').trim()) {
          callback(new Error('简答题标准答案不能为空'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const routeBankId = computed(() => {
  const raw = Array.isArray(route.query.questionBankId) ? route.query.questionBankId[0] : route.query.questionBankId
  if (!raw) {
    return undefined
  }
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
})
const routeTagId = computed(() => {
  const raw = Array.isArray(route.query.tagId) ? route.query.tagId[0] : route.query.tagId
  if (!raw) {
    return undefined
  }
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
})

const questionBankOptions = computed(() => dashboard.questionBanks)
const categoryOptions = computed(() => dashboard.categories)
const questionTagOptions = computed(() => dashboard.tags)
const questionBankMap = computed(() => {
  const result = new Map<number, KyzzQuestionBankOption>()
  questionBankOptions.value.forEach((bank) => result.set(bank.id, bank))
  return result
})
const prefilledBank = computed(() => questionBankOptions.value.find((bank) => bank.id === routeBankId.value))
const prefilledTag = computed(() => questionTagOptions.value.find((tag) => tag.id === routeTagId.value))
const isObjectiveQuestion = computed(() => form.questionType !== 'short')
const drawerTitle = computed(() => {
  if (drawerMode.value === 'edit') {
    return '编辑题目'
  }
  if (drawerMode.value === 'copy') {
    return '复制题目'
  }
  return '新增题目'
})
const followBankCategoryText = computed(() => {
  const bank = form.questionBankId ? questionBankMap.value.get(form.questionBankId) : undefined
  if (!bank?.categoryName) {
    return '当前会跟随题库，但该题库暂未绑定分类。'
  }
  return `当前会跟随题库带出“${bank.categoryName}”。`
})
const singleCorrectKey = computed({
  get: () => form.options.find((option) => option.isCorrect === 1)?.optionKey || '',
  set: (value: string) => {
    form.options.forEach((option) => {
      option.isCorrect = option.optionKey === value ? 1 : 0
    })
  }
})
const multipleCorrectKeys = computed({
  get: () => form.options.filter((option) => option.isCorrect === 1).map((option) => option.optionKey),
  set: (values: string[]) => {
    const selected = new Set(values)
    form.options.forEach((option) => {
      option.isCorrect = selected.has(option.optionKey) ? 1 : 0
    })
  }
})
const objectiveAnswerPreview = computed(() => {
  return form.options
    .filter((option) => option.isCorrect === 1)
    .map((option) => option.optionKey)
    .join(',')
})

function createDefaultOptions(): QuestionOptionForm[] {
  return Array.from({ length: 4 }, (_, index) => ({
    optionKey: String.fromCharCode(65 + index),
    optionContent: '',
    isCorrect: index === 0 ? 1 : 0
  }))
}

function setFormOptions(options: QuestionOptionForm[]) {
  form.options.splice(0, form.options.length, ...options)
}

function questionTypeLabel(questionType: string) {
  return questionTypeOptions.find((option) => option.value === questionType)?.label || questionType
}

function difficultyLabel(level: number) {
  return difficultyOptions.find((option) => option.value === level)?.label || `L${level}`
}

function categoryLevelLabel(level: number) {
  if (level === 1) {
    return '一级分类'
  }
  if (level === 2) {
    return '二级分类'
  }
  if (level === 3) {
    return '三级分类'
  }
  return `第 ${level} 级分类`
}

function formatCategoryLabel(category: KyzzCategoryOption) {
  return `${categoryLevelLabel(category.categoryLevel)} · ${category.categoryName}`
}

function formatBankLabel(bank: KyzzQuestionBankOption) {
  return `${bank.bankName} · ${bank.bankCode}`
}

function formatTagOptionLabel(tag: KyzzQuestionTagOption) {
  return `${tag.tagName} · ${tag.useCount} 题`
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

function previewText(value: string | null | undefined, limit = 60) {
  const normalized = (value || '').replace(/\s+/g, ' ').trim()
  if (!normalized) {
    return '未填写'
  }
  if (normalized.length <= limit) {
    return normalized
  }
  return `${normalized.slice(0, limit)}...`
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '暂无记录'
  }
  return value.replace('T', ' ').slice(0, 16)
}

function resolveFilterYearNo() {
  const normalized = filters.yearNoText.trim()
  if (!normalized) {
    return undefined
  }
  const value = Number(normalized)
  return Number.isFinite(value) ? value : undefined
}

function resetForm() {
  editingId.value = null
  drawerMode.value = 'create'
  followBankCategory.value = true
  form.questionBankId = filters.questionBankId ?? routeBankId.value ?? null
  form.categoryId = null
  form.questionType = 'single'
  form.stem = ''
  form.analysis = ''
  form.answerText = ''
  form.difficultyLevel = 2
  form.score = 1
  form.sourceName = ''
  form.yearNo = currentYear
  form.sortNo = 0
  form.status = 1
  form.tagIds = filters.tagId ? [filters.tagId] : []
  setFormOptions(createDefaultOptions())
  syncCategoryFromBank()
}

function syncCategoryFromBank() {
  if (!followBankCategory.value) {
    return
  }
  const bank = form.questionBankId ? questionBankMap.value.get(form.questionBankId) : undefined
  form.categoryId = bank?.categoryId ?? null
}

function isFollowingBankCategory(questionBankId: number | null, categoryId: number | null) {
  if (!questionBankId) {
    return categoryId == null
  }
  const bank = questionBankMap.value.get(questionBankId)
  return (bank?.categoryId ?? null) === (categoryId ?? null)
}

function resequenceOptions() {
  form.options.forEach((option, index) => {
    option.optionKey = String.fromCharCode(65 + index)
  })
}

function addOption() {
  if (form.options.length >= 8) {
    ElMessage.warning('客观题最多保留 8 个选项')
    return
  }
  form.options.push({
    optionKey: String.fromCharCode(65 + form.options.length),
    optionContent: '',
    isCorrect: 0
  })
}

function removeOption(index: number) {
  if (form.options.length <= 2) {
    ElMessage.warning('客观题至少保留 2 个选项')
    return
  }
  form.options.splice(index, 1)
  resequenceOptions()
  if (form.questionType === 'single' && !form.options.some((option) => option.isCorrect === 1) && form.options[0]) {
    form.options[0].isCorrect = 1
  }
}

function fillForm(detail: KyzzQuestionAdminDetail, mode: 'edit' | 'copy') {
  editingId.value = mode === 'edit' ? detail.id : null
  drawerMode.value = mode
  form.questionBankId = detail.questionBankId
  form.categoryId = detail.categoryId
  form.questionType = detail.questionType
  form.stem = detail.stem
  form.analysis = detail.analysis || ''
  form.answerText = detail.answerText || ''
  form.difficultyLevel = detail.difficultyLevel
  form.score = Number(detail.score)
  form.sourceName = detail.sourceName || ''
  form.yearNo = detail.yearNo
  form.sortNo = detail.sortNo
  form.status = mode === 'copy' ? 1 : detail.status
  form.tagIds = detail.tags.map((tag) => tag.id)
  setFormOptions(
    detail.questionType === 'short'
      ? []
      : (detail.options.length
        ? detail.options.map((option) => ({
            optionKey: option.optionKey,
            optionContent: option.optionContent,
            isCorrect: option.isCorrect
          }))
        : createDefaultOptions())
  )
  followBankCategory.value = isFollowingBankCategory(detail.questionBankId, detail.categoryId)
  if (followBankCategory.value) {
    syncCategoryFromBank()
  }
}

async function loadDashboard() {
  if (!isKyzz.value) {
    return
  }
  loading.value = true
  try {
    const result = await fetchKyzzQuestionDashboard({
      pageNo: filters.pageNo,
      pageSize: filters.pageSize,
      keyword: filters.keyword.trim() || undefined,
      questionBankId: filters.questionBankId,
      categoryId: filters.categoryId,
      tagId: filters.tagId,
      questionType: filters.questionType,
      status: filters.status,
      difficultyLevel: filters.difficultyLevel,
      yearNo: resolveFilterYearNo()
    })
    dashboard.stats = result.stats
    dashboard.records = result.records
    dashboard.pagination = result.pagination
    dashboard.questionBanks = result.questionBanks
    dashboard.categories = result.categories
    dashboard.tags = result.tags
  } catch (error) {
    ElMessage.error((error as Error).message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  filters.pageNo = 1
  void loadDashboard()
}

function resetFilters() {
  filters.keyword = ''
  filters.questionBankId = routeBankId.value
  filters.categoryId = undefined
  filters.tagId = routeTagId.value
  filters.questionType = undefined
  filters.status = undefined
  filters.difficultyLevel = undefined
  filters.yearNoText = ''
  filters.pageNo = 1
  filters.pageSize = 20
  void loadDashboard()
}

function handlePageChange(pageNo: number) {
  filters.pageNo = pageNo
  void loadDashboard()
}

function handleSizeChange(pageSize: number) {
  filters.pageSize = pageSize
  filters.pageNo = 1
  void loadDashboard()
}

function openCreateDrawer() {
  if (!questionBankOptions.value.length) {
    ElMessage.warning('请先创建题库，再开始维护题目')
    return
  }
  drawerVisible.value = true
  drawerLoading.value = false
  resetForm()
  nextTick(() => formRef.value?.clearValidate())
}

async function openEditDrawer(row: KyzzQuestionAdminItem) {
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    const detail = await fetchKyzzQuestionDetail(row.id)
    fillForm(detail, 'edit')
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    drawerVisible.value = false
    ElMessage.error((error as Error).message || '加载题目详情失败')
  } finally {
    drawerLoading.value = false
  }
}

async function openCopyDrawer(row: KyzzQuestionAdminItem) {
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    const detail = await fetchKyzzQuestionDetail(row.id)
    fillForm(detail, 'copy')
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    drawerVisible.value = false
    ElMessage.error((error as Error).message || '加载复制内容失败')
  } finally {
    drawerLoading.value = false
  }
}

function handleFormBankChange() {
  if (followBankCategory.value) {
    syncCategoryFromBank()
  }
}

function handleFollowBankCategoryChange(value: boolean) {
  if (value) {
    syncCategoryFromBank()
    return
  }
  if (!form.categoryId && form.questionBankId) {
    form.categoryId = questionBankMap.value.get(form.questionBankId)?.categoryId ?? null
  }
}

function handleCategoryClear() {
  if (followBankCategory.value) {
    syncCategoryFromBank()
    return
  }
  const bankCategoryId = form.questionBankId ? questionBankMap.value.get(form.questionBankId)?.categoryId ?? null : null
  if (bankCategoryId != null) {
    followBankCategory.value = true
    form.categoryId = bankCategoryId
  }
}

function validateOptionSection() {
  if (form.questionType === 'short') {
    if (!form.answerText.trim()) {
      ElMessage.warning('请填写简答题标准答案')
      return false
    }
    return true
  }

  if (form.options.length < 2 || form.options.length > 8) {
    ElMessage.warning('客观题需保持 2 到 8 个选项')
    return false
  }
  if (form.options.some((option) => !option.optionContent.trim())) {
    ElMessage.warning('请补全所有选项内容')
    return false
  }
  const correctCount = form.options.filter((option) => option.isCorrect === 1).length
  if (form.questionType === 'single' && correctCount !== 1) {
    ElMessage.warning('单选题必须且只能设置 1 个正确答案')
    return false
  }
  if (form.questionType === 'multiple' && correctCount < 2) {
    ElMessage.warning('多选题至少需要设置 2 个正确答案')
    return false
  }
  return true
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

  if (!validateOptionSection()) {
    return
  }

  saving.value = true
  try {
    const payload = {
      questionBankId: Number(form.questionBankId),
      categoryId: followBankCategory.value ? null : form.categoryId,
      questionType: form.questionType,
      stem: form.stem.trim(),
      analysis: form.analysis.trim(),
      answerText: form.questionType === 'short' ? form.answerText.trim() : objectiveAnswerPreview.value,
      difficultyLevel: form.difficultyLevel,
      score: Number(form.score),
      sourceName: form.sourceName.trim(),
      yearNo: form.yearNo,
      sortNo: form.sortNo,
      status: form.status,
      tagIds: form.tagIds,
      options: form.questionType === 'short'
        ? []
        : form.options.map((option) => ({
            optionKey: option.optionKey,
            optionContent: option.optionContent.trim(),
            isCorrect: option.isCorrect
          }))
    }

    if (drawerMode.value === 'edit' && editingId.value) {
      await updateKyzzQuestion(editingId.value, payload)
      ElMessage.success('题目已更新')
    } else {
      await createKyzzQuestion(payload)
      filters.pageNo = 1
      ElMessage.success(drawerMode.value === 'copy' ? '题目已复制创建' : '题目已创建')
    }

    drawerVisible.value = false
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '保存题目失败')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row: KyzzQuestionAdminItem) {
  togglingId.value = row.id
  try {
    await updateKyzzQuestionStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(row.status === 1 ? '题目已停用' : '题目已启用')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '状态更新失败')
  } finally {
    togglingId.value = null
  }
}

async function handleDelete(row: KyzzQuestionAdminItem) {
  try {
    await ElMessageBox.confirm(
      `将删除该题目。${row.deleteBlockReason ? `当前已识别到阻断信息：${row.deleteBlockReason}` : '若未产生学习记录，将直接移除题干与选项。'}`,
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
    await deleteKyzzQuestion(row.id)
    if (dashboard.records.length === 1 && filters.pageNo > 1) {
      filters.pageNo -= 1
    }
    ElMessage.success('题目已删除')
    await loadDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '删除题目失败')
  } finally {
    deletingId.value = null
  }
}

watch(
  () => [routeBankId.value, routeTagId.value],
  ([bankId, tagId]) => {
    filters.questionBankId = bankId
    filters.tagId = tagId
    filters.pageNo = 1
    void loadDashboard()
  },
  { immediate: true }
)

watch(
  () => form.questionType,
  (nextType, previousType) => {
    if (nextType === previousType) {
      return
    }
    if (nextType === 'short') {
      setFormOptions([])
      return
    }
    if (previousType === 'short' || form.options.length === 0) {
      setFormOptions(createDefaultOptions())
      return
    }
    resequenceOptions()
  }
)
</script>

<style scoped lang="scss">
@use '../../../styles/system-management.scss' as systemManagement;

@include systemManagement.management-page-shell('.kyzz-question-page');

.kyzz-question-page {
  .bank-select {
    width: 240px;
  }

  .category-select {
    width: 220px;
  }

  .tag-select {
    width: 220px;
  }

  .type-select,
  .difficulty-select {
    width: 140px;
  }

  .year-input {
    width: 120px;
  }
}

.kyzz-question-page__entry-alert {
  margin-bottom: 16px;
}

.kyzz-question-page__content-cell,
.kyzz-question-page__answer-cell,
.kyzz-question-page__ownership {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kyzz-question-page__content-head,
.kyzz-question-page__answer-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.kyzz-question-page__tag-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.kyzz-question-page__stem {
  color: var(--el-text-color-primary);
  line-height: 1.7;
  font-weight: 600;
}

.kyzz-question-page__subtext,
.kyzz-question-page__analysis,
.kyzz-question-page__content-meta,
.kyzz-question-page__answer-text {
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
}

.kyzz-question-page__subtext {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.kyzz-question-page__warning {
  color: var(--el-color-warning-dark-2);
  background: var(--el-color-warning-light-9);
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.6;
}

.kyzz-question-page__ownership-main {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-question-page__metric-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.kyzz-question-page__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.kyzz-question-page__drawer-body {
  padding-right: 4px;
}

.kyzz-question-page__section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--admin-border);
}

.kyzz-question-page__section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.kyzz-question-page__section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-question-page__form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.kyzz-question-page__form-grid :deep(.is-span-2) {
  grid-column: 1 / -1;
}

.kyzz-question-page__tag-panel,
.kyzz-question-page__category-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
}

.kyzz-question-page__tag-tip {
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
}

.kyzz-question-page__category-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.kyzz-question-page__category-summary {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.kyzz-question-page__category-summary .title {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-question-page__category-summary .desc {
  color: var(--admin-text-soft);
  font-size: 13px;
  line-height: 1.7;
}

.kyzz-question-page__objective-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 16px;
  margin: 16px 0;
}

.kyzz-question-page__objective-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  flex: 1;
  min-width: 0;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
}

.kyzz-question-page__objective-summary .label {
  color: var(--admin-text-soft);
  font-size: 13px;
}

.kyzz-question-page__objective-summary .value {
  font-family: 'SFMono-Regular', 'JetBrains Mono', monospace;
  color: var(--el-color-success-dark-2);
  font-weight: 700;
}

.kyzz-question-page__objective-summary .hint {
  color: var(--admin-text-soft);
  font-size: 12px;
  line-height: 1.6;
}

.kyzz-question-page__option-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: stretch;
  gap: 16px;
  width: 100%;
}

.kyzz-question-page__option-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  min-width: 0;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid var(--admin-border);
  background: var(--admin-surface-soft);
  box-sizing: border-box;
}

.kyzz-question-page__option-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.kyzz-question-page__option-meta,
.kyzz-question-page__option-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.kyzz-question-page__option-actions {
  flex-shrink: 0;
}

.kyzz-question-page__option-title {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.kyzz-question-page__option-key {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 700;
  flex-shrink: 0;
}

.kyzz-question-page__option-row :deep(.el-textarea) {
  width: 100%;
}

.kyzz-question-page__dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .kyzz-question-page__form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .kyzz-question-page__category-head,
  .kyzz-question-page__objective-toolbar,
  .kyzz-question-page__option-head {
    flex-direction: column;
    align-items: stretch;
  }

  .kyzz-question-page__option-list {
    grid-template-columns: minmax(0, 1fr);
  }

  .kyzz-question-page__pagination {
    justify-content: center;
  }
}
</style>
