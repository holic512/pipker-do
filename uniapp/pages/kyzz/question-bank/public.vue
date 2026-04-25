<template>
	<!-- AI 索引: 公共题库页 -->
	<view class="question-bank-public-page">
		<view class="question-bank-public-page__search-shell">
			<view class="question-bank-public-page__search-box">
				<uni-icons type="search" size="18" color="#99a5b5" />
				<input
					v-model="keywordInput"
					class="question-bank-public-page__search-input"
					placeholder="搜索题库名称、副标题、分类"
					placeholder-class="question-bank-public-page__search-placeholder"
					confirm-type="search"
					@confirm="handleSearchConfirm"
				/>
				<view v-if="keywordInput" class="question-bank-public-page__search-clear" @tap="handleSearchClear">
					<text class="question-bank-public-page__search-clear-text">×</text>
				</view>
			</view>
		</view>

		<view class="question-bank-public-page__toolbar">
			<view class="question-bank-public-page__segmented">
				<uni-segmented-control
					:current="selectionStatusIndex"
					:values="selectionStatusLabels"
					active-color="#465a7e"
					in-active-color="#ffffff"
					style-type="button"
					@clickItem="handleSelectionStatusChange"
				/>
			</view>
			<button class="question-bank-public-page__filter-button" @tap="openFilterPopup">
				<uni-icons type="gear" size="16" color="#4f5c72" />
				<text class="question-bank-public-page__filter-text">筛选</text>
				<text v-if="filterBadgeCount" class="question-bank-public-page__filter-badge">{{ filterBadgeCount }}</text>
			</button>
		</view>

		<view v-if="loading && !loadedOnce" class="question-bank-public-page__state-card">
			<text class="question-bank-public-page__state-title">正在整理公共题库...</text>
			<text class="question-bank-public-page__state-desc">会同时带出已选择和未选择状态，方便你快速筛选。</text>
		</view>

		<template v-else-if="records.length">
			<view v-if="selectedRecords.length" class="question-bank-public-page__section">
				<view class="question-bank-public-page__section-head">
					<view class="question-bank-public-page__section-copy">
						<view class="question-bank-public-page__section-title-row">
							<view class="question-bank-public-page__section-title-mark">
								<view class="question-bank-public-page__section-title-bar question-bank-public-page__section-title-bar--left"></view>
								<view class="question-bank-public-page__section-title-bar question-bank-public-page__section-title-bar--right"></view>
							</view>
							<text class="question-bank-public-page__section-title">已选择</text>
						</view>
						<text class="question-bank-public-page__section-desc">这些题库已经加入你的个人题库，会继续记录进度。</text>
					</view>
					<text class="question-bank-public-page__section-count">{{ selectedRecords.length }}</text>
				</view>

				<view class="question-bank-public-page__list">
					<view
						v-for="item in selectedRecords"
						:key="item.id"
						class="question-bank-public-page__card"
					>
						<view class="question-bank-public-page__card-main">
							<image
								v-if="item.coverUrl"
								class="question-bank-public-page__cover"
								:src="item.coverUrl"
								mode="aspectFill"
							/>
							<view v-else class="question-bank-public-page__cover question-bank-public-page__cover--fallback">
								<text class="question-bank-public-page__cover-initial">{{ buildCoverInitial(item.bankName) }}</text>
								<view class="question-bank-public-page__cover-arc"></view>
							</view>

							<view class="question-bank-public-page__card-body">
								<text class="question-bank-public-page__card-title">{{ item.bankName }}</text>
								<text v-if="item.subtitle" class="question-bank-public-page__card-subtitle">{{ item.subtitle }}</text>

								<view class="question-bank-public-page__meta-row">
									<text class="question-bank-public-page__meta-pill question-bank-public-page__meta-pill--progress">进度 {{ formatProgress(item.currentProgress) }}</text>
									<text class="question-bank-public-page__meta-pill question-bank-public-page__meta-pill--category">{{ item.categoryName || '未分类' }}</text>
									<text class="question-bank-public-page__meta-pill" :class="difficultyTagClass(item.difficultyLevel)">{{ difficultyLabel(item.difficultyLevel) }}</text>
									<text class="question-bank-public-page__meta-pill question-bank-public-page__meta-pill--count">{{ item.questionCount }} 题</text>
								</view>
							</view>

							<button
								class="question-bank-public-page__icon-button question-bank-public-page__icon-button--remove"
								:disabled="togglingId === item.id"
								@tap="handleToggleSelection(item)"
							>
								<uni-icons type="minus" size="18" color="#6d7789" />
							</button>
						</view>
					</view>
				</view>
			</view>

			<view v-if="unselectedRecords.length" class="question-bank-public-page__section">
				<view class="question-bank-public-page__section-head">
					<view class="question-bank-public-page__section-copy">
						<view class="question-bank-public-page__section-title-row">
							<view class="question-bank-public-page__section-title-mark">
								<view class="question-bank-public-page__section-title-bar question-bank-public-page__section-title-bar--left"></view>
								<view class="question-bank-public-page__section-title-bar question-bank-public-page__section-title-bar--right"></view>
							</view>
							<text class="question-bank-public-page__section-title">未选择</text>
						</view>
						<text class="question-bank-public-page__section-desc">挑几套适合当前阶段的题库，加入后会自动出现在“我的题库”。</text>
					</view>
					<text class="question-bank-public-page__section-count">{{ unselectedRecords.length }}</text>
				</view>

				<view class="question-bank-public-page__list">
					<view
						v-for="item in unselectedRecords"
						:key="item.id"
						class="question-bank-public-page__card"
					>
						<view class="question-bank-public-page__card-main">
							<image
								v-if="item.coverUrl"
								class="question-bank-public-page__cover"
								:src="item.coverUrl"
								mode="aspectFill"
							/>
							<view v-else class="question-bank-public-page__cover question-bank-public-page__cover--fallback">
								<text class="question-bank-public-page__cover-initial">{{ buildCoverInitial(item.bankName) }}</text>
								<view class="question-bank-public-page__cover-arc"></view>
							</view>

							<view class="question-bank-public-page__card-body">
								<text class="question-bank-public-page__card-title">{{ item.bankName }}</text>
								<text v-if="item.subtitle" class="question-bank-public-page__card-subtitle">{{ item.subtitle }}</text>

								<view class="question-bank-public-page__meta-row">
									<text class="question-bank-public-page__meta-pill question-bank-public-page__meta-pill--category">{{ item.categoryName || '未分类' }}</text>
									<text class="question-bank-public-page__meta-pill" :class="difficultyTagClass(item.difficultyLevel)">{{ difficultyLabel(item.difficultyLevel) }}</text>
									<text class="question-bank-public-page__meta-pill question-bank-public-page__meta-pill--count">{{ item.questionCount }} 题</text>
								</view>
							</view>

							<button
								class="question-bank-public-page__icon-button question-bank-public-page__icon-button--add"
								:disabled="togglingId === item.id"
								@tap="handleToggleSelection(item)"
							>
								<uni-icons type="plus" size="18" color="#5f6f8b" />
							</button>
						</view>
					</view>
				</view>
			</view>
		</template>

		<view v-else class="question-bank-public-page__state-card">
			<text class="question-bank-public-page__state-title">{{ hasActiveFilters ? '没有匹配的题库' : '当前没有可展示的公共题库' }}</text>
			<text class="question-bank-public-page__state-desc">
				{{ hasActiveFilters ? '试试清空筛选条件，或换一个关键词重新搜索。' : '等后台上架题库后，这里会自动显示。' }}
			</text>
			<button
				v-if="hasActiveFilters"
				class="question-bank-public-page__ghost-button question-bank-public-page__ghost-button--inline"
				@tap="resetAllFilters"
			>
				清空筛选
			</button>
		</view>

		<uni-popup
			ref="filterPopup"
			type="bottom"
			background-color="#ffffff"
			border-radius="28rpx 28rpx 0 0"
			:is-mask-click="true"
		>
			<view class="question-bank-public-page__popup">
				<view class="question-bank-public-page__popup-head">
					<text class="question-bank-public-page__popup-title">筛选题库</text>
					<text class="question-bank-public-page__popup-close" @tap="closeFilterPopup">收起</text>
				</view>

				<view class="question-bank-public-page__filter-group">
					<text class="question-bank-public-page__filter-label">分类</text>
					<view class="question-bank-public-page__chip-row">
						<view
							class="question-bank-public-page__chip"
							:class="{ 'question-bank-public-page__chip--active': draftFilters.categoryId === null }"
							@tap="selectDraftCategory(null)"
						>
							全部
						</view>
						<view
							v-for="item in categories"
							:key="item.id"
							class="question-bank-public-page__chip"
							:class="{ 'question-bank-public-page__chip--active': draftFilters.categoryId === item.id }"
							@tap="selectDraftCategory(item.id)"
						>
							{{ item.categoryName }}
						</view>
					</view>
				</view>

				<view class="question-bank-public-page__filter-group">
					<text class="question-bank-public-page__filter-label">难度</text>
					<view class="question-bank-public-page__chip-row">
						<view
							class="question-bank-public-page__chip"
							:class="{ 'question-bank-public-page__chip--active': draftFilters.difficultyLevel === null }"
							@tap="selectDraftDifficulty(null)"
						>
							全部
						</view>
						<view
							v-for="item in difficultyOptions"
							:key="item.value"
							class="question-bank-public-page__chip"
							:class="{ 'question-bank-public-page__chip--active': draftFilters.difficultyLevel === item.value }"
							@tap="selectDraftDifficulty(item.value)"
						>
							{{ item.label }}
						</view>
					</view>
				</view>

				<view class="question-bank-public-page__popup-actions">
					<button class="question-bank-public-page__ghost-button" @tap="resetPopupFilters">清空筛选</button>
					<button class="question-bank-public-page__primary-button" @tap="applyPopupFilters">应用筛选</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getPublicQuestionBanks, updateQuestionBankSelection } from '@/pages/kyzz/api/question-bank'
import type {
	KyzzQuestionBankPublicCategoryResponse,
	KyzzQuestionBankPublicDraftFilters,
	KyzzQuestionBankPublicFilters,
	KyzzQuestionBankPublicRecordResponse,
	KyzzQuestionBankPublicViewRecord,
	KyzzQuestionBankSelectionStatus,
	SearchConfirmEvent,
	SegmentedControlClickEvent,
	UniPopupRef
} from '@/pages/kyzz/question-bank/types'

const DIFFICULTY_OPTIONS: ReadonlyArray<{ label: string; value: number }> = [
	{ label: '简单', value: 1 },
	{ label: '中等', value: 2 },
	{ label: '困难', value: 3 },
	{ label: '冲刺', value: 4 }
]

const SELECTION_STATUS_OPTIONS: ReadonlyArray<{ label: string; value: KyzzQuestionBankSelectionStatus }> = [
	{ label: '全部', value: 'all' },
	{ label: '已选择', value: 'selected' },
	{ label: '未选择', value: 'unselected' }
]

interface PublicPageState {
	loading: boolean
	loadedOnce: boolean
	togglingId: number | null
	keywordInput: string
	records: KyzzQuestionBankPublicViewRecord[]
	categories: KyzzQuestionBankPublicCategoryResponse[]
	filters: KyzzQuestionBankPublicFilters
	draftFilters: KyzzQuestionBankPublicDraftFilters
}

function createDefaultFilters(): KyzzQuestionBankPublicFilters {
	return {
		keyword: '',
		categoryId: null,
		difficultyLevel: null,
		selectionStatus: 'all'
	}
}

function createDefaultDraftFilters(): KyzzQuestionBankPublicDraftFilters {
	return {
		categoryId: null,
		difficultyLevel: null
	}
}

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'PublicQuestionBankPage',
	data(): PublicPageState {
		return {
			loading: false,
			loadedOnce: false,
			togglingId: null,
			keywordInput: '',
			records: [],
			categories: [],
			filters: createDefaultFilters(),
			draftFilters: createDefaultDraftFilters()
		}
	},
	computed: {
		difficultyOptions(): ReadonlyArray<{ label: string; value: number }> {
			return DIFFICULTY_OPTIONS
		},
		selectionStatusLabels(): string[] {
			return SELECTION_STATUS_OPTIONS.map((item) => item.label)
		},
		selectionStatusIndex(): number {
			const index = SELECTION_STATUS_OPTIONS.findIndex((item) => item.value === this.filters.selectionStatus)
			return index === -1 ? 0 : index
		},
		selectedRecords(): KyzzQuestionBankPublicViewRecord[] {
			return this.records.filter((item) => item.selected)
		},
		unselectedRecords(): KyzzQuestionBankPublicViewRecord[] {
			return this.records.filter((item) => !item.selected)
		},
		hasActiveFilters(): boolean {
			return Boolean(
				this.filters.keyword
				|| this.filters.categoryId !== null
				|| this.filters.difficultyLevel !== null
				|| this.filters.selectionStatus !== 'all'
			)
		},
		filterBadgeCount(): number {
			let count = 0
			if (this.filters.categoryId !== null) {
				count += 1
			}
			if (this.filters.difficultyLevel !== null) {
				count += 1
			}
			return count
		}
	},
	onLoad() {
		this.bootstrapAndLoad()
	},
	methods: {
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
				await this.loadPublicQuestionBanks()
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '公共题库加载失败'),
					icon: 'none'
				})
			}
		},
		async loadPublicQuestionBanks(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const result = await getPublicQuestionBanks({
					keyword: this.filters.keyword,
					categoryId: this.filters.categoryId,
					difficultyLevel: this.filters.difficultyLevel,
					selectionStatus: this.filters.selectionStatus
				})
				this.categories = Array.isArray(result?.categories) ? result.categories : []
				this.records = this.sortRecords(
					Array.isArray(result?.records)
						? result.records.map((record) => this.normalizeRecord(record))
						: []
				)
				this.loadedOnce = true
			} catch (error) {
				this.loadedOnce = true
				throw error
			} finally {
				this.loading = false
			}
		},
		normalizeRecord(record: KyzzQuestionBankPublicRecordResponse): KyzzQuestionBankPublicViewRecord {
			return {
				...record,
				selected: Boolean(record.selected),
				questionCount: toNumber(record.questionCount),
				totalScore: toNumber(record.totalScore),
				ratingCount: toNumber(record.ratingCount),
				studyUserCount: toNumber(record.studyUserCount),
				sortNo: toNumber(record.sortNo),
				currentProgress: toNumber(record.currentProgress),
				studiedCount: toNumber(record.studiedCount),
				correctCount: toNumber(record.correctCount),
				wrongCount: toNumber(record.wrongCount)
			}
		},
		sortRecords(records: KyzzQuestionBankPublicViewRecord[]): KyzzQuestionBankPublicViewRecord[] {
			return [...records].sort((left, right) => {
				if (left.selected !== right.selected) {
					return left.selected ? -1 : 1
				}
				if (left.sortNo !== right.sortNo) {
					return left.sortNo - right.sortNo
				}
				if (left.studyUserCount !== right.studyUserCount) {
					return right.studyUserCount - left.studyUserCount
				}
				return right.id - left.id
			})
		},
		handleSearchConfirm(event: SearchConfirmEvent): void {
			this.filters.keyword = (event?.detail?.value || event?.value || this.keywordInput || '').trim()
			this.keywordInput = this.filters.keyword
			void this.loadPublicQuestionBanks()
		},
		handleSearchClear(): void {
			this.keywordInput = ''
			this.filters.keyword = ''
			void this.loadPublicQuestionBanks()
		},
		handleSelectionStatusChange(event: SegmentedControlClickEvent): void {
			const option = SELECTION_STATUS_OPTIONS[event.currentIndex]
			if (!option || option.value === this.filters.selectionStatus) {
				return
			}
			this.filters.selectionStatus = option.value
			void this.loadPublicQuestionBanks()
		},
		openFilterPopup(): void {
			this.draftFilters = {
				categoryId: this.filters.categoryId,
				difficultyLevel: this.filters.difficultyLevel
			}
			const popup = this.$refs.filterPopup as UniPopupRef | undefined
			popup?.open()
		},
		closeFilterPopup(): void {
			const popup = this.$refs.filterPopup as UniPopupRef | undefined
			popup?.close()
		},
		selectDraftCategory(categoryId: number | null): void {
			this.draftFilters.categoryId = categoryId
		},
		selectDraftDifficulty(level: number | null): void {
			this.draftFilters.difficultyLevel = level
		},
		resetPopupFilters(): void {
			this.draftFilters = createDefaultDraftFilters()
		},
		resetAllFilters(): void {
			this.keywordInput = ''
			this.filters = createDefaultFilters()
			this.resetPopupFilters()
			void this.loadPublicQuestionBanks()
		},
		applyPopupFilters(): void {
			this.filters.categoryId = this.draftFilters.categoryId
			this.filters.difficultyLevel = this.draftFilters.difficultyLevel
			this.closeFilterPopup()
			void this.loadPublicQuestionBanks()
		},
		difficultyLabel(level: number): string {
			return DIFFICULTY_OPTIONS.find((item) => item.value === level)?.label || `L${level || 0}`
		},
		difficultyTagClass(level: number): string {
			if (level === 1) {
				return 'question-bank-public-page__meta-pill--simple'
			}
			if (level === 2) {
				return 'question-bank-public-page__meta-pill--medium'
			}
			if (level === 3) {
				return 'question-bank-public-page__meta-pill--hard'
			}
			if (level === 4) {
				return 'question-bank-public-page__meta-pill--sprint'
			}
			return ''
		},
		formatProgress(value: number): string {
			const progress = toNumber(value)
			const normalized = Math.round(progress * 10) / 10
			const display = Number.isInteger(normalized) ? normalized : normalized.toFixed(1)
			return `${display}%`
		},
		buildCoverInitial(name: string): string {
			if (!name) {
				return '题'
			}
			return name.trim().slice(0, 1).toUpperCase()
		},
		async handleToggleSelection(item: KyzzQuestionBankPublicViewRecord): Promise<void> {
			if (this.togglingId !== null) {
				return
			}
			if (item.selected) {
				const confirmed = await this.confirmRemove(item)
				if (!confirmed) {
					return
				}
			}

			this.togglingId = item.id
			try {
				const updated = await updateQuestionBankSelection(item.id, !item.selected)
				this.applyRecordUpdate(this.normalizeRecord(updated))
				uni.showToast({
					title: updated.selected ? '已加入我的题库' : '已移出我的题库',
					icon: 'none'
				})
			} catch (error) {
				uni.showToast({
					title: resolveErrorMessage(error, '操作失败'),
					icon: 'none'
				})
			} finally {
				this.togglingId = null
			}
		},
		confirmRemove(item: KyzzQuestionBankPublicViewRecord): Promise<boolean> {
			return new Promise((resolve) => {
				uni.showModal({
					title: '移出题库',
					content: `将“${item.bankName}”移出我的题库。仅移出清单，不删除历史记录。`,
					confirmText: '确认移出',
					cancelText: '再想想',
					success: (result: { confirm?: boolean }) => resolve(Boolean(result.confirm)),
					fail: () => resolve(false)
				})
			})
		},
		applyRecordUpdate(record: KyzzQuestionBankPublicViewRecord): void {
			const nextRecords = [...this.records]
			const index = nextRecords.findIndex((item) => item.id === record.id)
			const shouldKeep = this.filters.selectionStatus === 'all'
				|| (this.filters.selectionStatus === 'selected' && record.selected)
				|| (this.filters.selectionStatus === 'unselected' && !record.selected)

			if (index >= 0) {
				if (shouldKeep) {
					nextRecords.splice(index, 1, record)
				} else {
					nextRecords.splice(index, 1)
				}
			} else if (shouldKeep) {
				nextRecords.push(record)
			}
			this.records = this.sortRecords(nextRecords)
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.question-bank-public-page {
	min-height: 100vh;
	padding: 20rpx 24rpx calc(env(safe-area-inset-bottom) + 40rpx);
	background: linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
	box-sizing: border-box;
}

.question-bank-public-page__search-shell {
}

.question-bank-public-page__search-box {
	display: flex;
	align-items: center;
	gap: 14rpx;
	height: 82rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: #ffffff;
	box-shadow:
		0 10rpx 24rpx rgba(45, 58, 77, 0.05),
		inset 0 0 0 1rpx #cfd8e6;
}

.question-bank-public-page__search-input {
	flex: 1;
	min-width: 0;
	height: 100%;
	font-size: 26rpx;
	color: #2d3645;
}

.question-bank-public-page__search-placeholder {
	font-size: 24rpx;
	color: #a9b2bf;
}

.question-bank-public-page__search-clear {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	background: #9aa7ba;
}

.question-bank-public-page__search-clear-text {
	font-size: 28rpx;
	line-height: 1;
	color: #ffffff;
}

.question-bank-public-page__toolbar {
	display: flex;
	align-items: center;
	gap: 18rpx;
	margin-top: 22rpx;
}

.question-bank-public-page__segmented {
	flex: 1;
	min-width: 0;
}

.question-bank-public-page__filter-button {
	position: relative;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	min-width: 136rpx;
	height: 72rpx;
	margin: 0;
	padding: 0 20rpx;
	border-radius: 22rpx;
	background: #e8f0ff;
	color: #345176;
	font-size: 24rpx;
	font-weight: 700;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
}

.question-bank-public-page__filter-button::after {
	border: 0;
}

.question-bank-public-page__filter-badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 34rpx;
	height: 34rpx;
	padding: 0 8rpx;
	border-radius: 999rpx;
	background: #465a7e;
	font-size: 20rpx;
	line-height: 1;
	color: #ffffff;
}

.question-bank-public-page__state-card,
.question-bank-public-page__section {
	margin-top: 22rpx;
}

.question-bank-public-page__state-card {
	padding: 42rpx 32rpx;
	border-radius: 28rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 34rpx rgba(45, 58, 77, 0.06);
	border: 1rpx solid #d6deea;
}

.question-bank-public-page__state-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2e3642;
}

.question-bank-public-page__state-desc {
	display: block;
	margin-top: 16rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #7c8593;
}

.question-bank-public-page__section-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 16rpx;
	margin-bottom: 18rpx;
}

.question-bank-public-page__section-copy {
	min-width: 0;
	flex: 1;
}

.question-bank-public-page__section-title-row {
	display: flex;
	align-items: center;
	gap: 14rpx;
}

.question-bank-public-page__section-title-mark {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	flex-shrink: 0;
}

.question-bank-public-page__section-title-bar {
	width: 10rpx;
	border-radius: 999rpx;
	background: linear-gradient(180deg, #465a7e 0%, #8ba0bf 100%);
}

.question-bank-public-page__section-title-bar--left {
	height: 26rpx;
}

.question-bank-public-page__section-title-bar--right {
	height: 20rpx;
}

.question-bank-public-page__section-title {
	display: block;
	font-size: 34rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2d3440;
}

.question-bank-public-page__section-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #7d8694;
}

.question-bank-public-page__section-count {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 56rpx;
	height: 56rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: #eef2f6;
	box-shadow: inset 0 0 0 1rpx #c9d2dc;
	font-size: 24rpx;
	font-weight: 700;
	color: #3c4a58;
}

.question-bank-public-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.question-bank-public-page__card {
	padding: 22rpx;
	border-radius: 30rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 30rpx rgba(45, 58, 77, 0.055);
	border: 1rpx solid #d6deea;
}

.question-bank-public-page__card-main {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.question-bank-public-page__cover {
	width: 148rpx;
	height: 148rpx;
	border-radius: 24rpx;
	background: linear-gradient(145deg, #e8f0ff 0%, #dbe7f7 100%);
	flex-shrink: 0;
	display: block;
	overflow: hidden;
}

.question-bank-public-page__cover--fallback {
	position: relative;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	padding: 18rpx;
	box-sizing: border-box;
}

.question-bank-public-page__cover-initial {
	font-size: 52rpx;
	line-height: 1;
	font-family: $heading-font-family;
	font-weight: 700;
	color: #42546f;
}

.question-bank-public-page__cover-arc {
	width: 100%;
	height: 36rpx;
	border-radius: 999rpx 999rpx 16rpx 16rpx;
	background: rgba(66, 84, 111, 0.18);
}

.question-bank-public-page__card-body {
	flex: 1;
	min-width: 0;
}

.question-bank-public-page__card-title {
	display: block;
	font-size: 30rpx;
	line-height: 1.35;
	font-weight: 700;
	color: #2d3642;
}

.question-bank-public-page__card-subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #7f8896;
}

.question-bank-public-page__meta-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-top: 18rpx;
}

.question-bank-public-page__meta-pill {
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: #ffffff;
	box-shadow: inset 0 0 0 1rpx #cfd8e6;
	font-size: 20rpx;
	line-height: 1;
	font-weight: 700;
	color: #39465a;
}

.question-bank-public-page__meta-pill--progress {
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
	color: #334f7c;
}

.question-bank-public-page__meta-pill--category {
	background: #eef2f6;
	box-shadow: inset 0 0 0 1rpx #c9d2dc;
	color: #3c4a58;
}

.question-bank-public-page__meta-pill--simple {
	background: #e5f4e8;
	box-shadow: inset 0 0 0 1rpx #bcdabe;
	color: #315f42;
}

.question-bank-public-page__meta-pill--medium {
	background: #e7efff;
	box-shadow: inset 0 0 0 1rpx #b9cae8;
	color: #314f79;
}

.question-bank-public-page__meta-pill--hard {
	background: #fff0dc;
	box-shadow: inset 0 0 0 1rpx #e6c58e;
	color: #78511f;
}

.question-bank-public-page__meta-pill--sprint {
	background: #fde8e5;
	box-shadow: inset 0 0 0 1rpx #e8b8b3;
	color: #87413e;
}

.question-bank-public-page__meta-pill--count {
	background: #fff6df;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
	color: #735817;
}

.question-bank-public-page__icon-button,
.question-bank-public-page__primary-button,
.question-bank-public-page__ghost-button {
	margin: 0;
}

.question-bank-public-page__icon-button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 76rpx;
	height: 76rpx;
	padding: 0;
	border-radius: 24rpx;
	flex-shrink: 0;
	background: #f7f9fc;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.question-bank-public-page__icon-button--add {
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
}

.question-bank-public-page__icon-button--remove {
	background: #fff0e5;
	box-shadow: inset 0 0 0 1rpx #e5bea4;
}

.question-bank-public-page__primary-button::after,
.question-bank-public-page__ghost-button::after,
.question-bank-public-page__icon-button::after {
	border: 0;
}

.question-bank-public-page__icon-button[disabled] {
	background: #d9e0ea;
	box-shadow: inset 0 0 0 1rpx #c4cedb;
	opacity: 1;
}

.question-bank-public-page__primary-button,
.question-bank-public-page__ghost-button {
	min-width: 156rpx;
	height: 74rpx;
	line-height: 74rpx;
	padding: 0 28rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 700;
}

.question-bank-public-page__primary-button {
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(54, 75, 111, 0.22);
}

.question-bank-public-page__ghost-button {
	background: #f7f9fc;
	color: #34445e;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.question-bank-public-page__ghost-button--inline {
	margin-top: 24rpx;
}

.question-bank-public-page__popup {
	padding: 28rpx 24rpx calc(env(safe-area-inset-bottom) + 32rpx);
	background: #ffffff;
}

.question-bank-public-page__popup-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.question-bank-public-page__popup-title {
	font-size: 32rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2f3745;
}

.question-bank-public-page__popup-close {
	font-size: 24rpx;
	line-height: 1;
	color: #7b8593;
}

.question-bank-public-page__filter-group {
	margin-top: 28rpx;
}

.question-bank-public-page__filter-label {
	display: block;
	font-size: 24rpx;
	line-height: 1.2;
	font-weight: 600;
	color: #4f5b70;
}

.question-bank-public-page__chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-top: 18rpx;
}

.question-bank-public-page__chip {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 0 22rpx;
	height: 64rpx;
	border-radius: 999rpx;
	background: #f7f9fc;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
	font-size: 22rpx;
	line-height: 1;
	color: #405066;
}

.question-bank-public-page__chip--active {
	background: #e8f0ff;
	box-shadow: inset 0 0 0 1rpx #bccbe4;
	color: #334f7c;
	font-weight: 700;
}

.question-bank-public-page__popup-actions {
	display: flex;
	justify-content: flex-end;
	gap: 16rpx;
	margin-top: 34rpx;
}
</style>
