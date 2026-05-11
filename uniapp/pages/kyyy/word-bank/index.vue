<template>
	<!-- AI 索引: KYYY 英语词库选择页。 -->
	<page-shell
		root-class="kyyy-word-bank-page theme-page"
		content-style="padding: 0 24rpx 40rpx;"
	>
		<view class="kyyy-word-bank-page__ambient kyyy-word-bank-page__ambient--left"></view>
		<view class="kyyy-word-bank-page__ambient kyyy-word-bank-page__ambient--right"></view>

		<view class="kyyy-word-bank-page__inner">
			<view class="kyyy-word-bank-page__hero">
				<text class="kyyy-word-bank-page__eyebrow">{{ eyebrow }}</text>
				<text class="kyyy-word-bank-page__title">{{ pageTitle }}</text>
				<view class="kyyy-word-bank-page__hero-meta">
					<text class="kyyy-word-bank-page__meta-pill">已选 {{ listState.summary.selectedCount }}</text>
					<text class="kyyy-word-bank-page__meta-pill">
						{{ defaultRecord ? `默认 ${defaultRecord.bankName}` : '未设默认' }}
					</text>
				</view>
			</view>

			<view v-if="defaultRecord" class="kyyy-word-bank-page__default-card">
				<text class="kyyy-word-bank-page__default-label">默认</text>
				<text class="kyyy-word-bank-page__default-name">{{ defaultRecord.bankName }}</text>
				<text v-if="defaultRecord.subtitle" class="kyyy-word-bank-page__default-subtitle">{{ defaultRecord.subtitle }}</text>
			</view>

			<view v-else class="kyyy-word-bank-page__hint-card">
				<text class="kyyy-word-bank-page__hint-text">先选一个默认词库</text>
			</view>

			<view v-if="loading && !listState.loaded" class="kyyy-word-bank-page__state-card">
				<text class="kyyy-word-bank-page__state-text">正在整理词库...</text>
			</view>

			<view v-else-if="listState.records.length" class="kyyy-word-bank-page__list">
				<view
					v-for="item in listState.records"
					:key="item.id"
					class="kyyy-word-bank-page__card"
					:class="{
						'is-selected': item.selected,
						'is-default': item.isDefault
					}"
				>
					<view class="kyyy-word-bank-page__card-main">
						<view class="kyyy-word-bank-page__card-copy">
							<view class="kyyy-word-bank-page__card-head">
								<text class="kyyy-word-bank-page__card-title">{{ item.bankName }}</text>
								<view class="kyyy-word-bank-page__card-tags">
									<text v-if="item.selected" class="kyyy-word-bank-page__card-tag">已选</text>
									<text v-if="item.isDefault" class="kyyy-word-bank-page__card-tag kyyy-word-bank-page__card-tag--default">默认</text>
								</view>
							</view>
							<text v-if="item.subtitle" class="kyyy-word-bank-page__card-subtitle">{{ item.subtitle }}</text>
							<view class="kyyy-word-bank-page__card-meta">
								<text class="kyyy-word-bank-page__card-meta-text">{{ item.wordCount }} 词</text>
								<text class="kyyy-word-bank-page__card-meta-text">{{ item.studyUserCount }} 人</text>
							</view>
						</view>

						<view class="kyyy-word-bank-page__card-actions">
							<view
								v-if="item.selected && !item.isDefault"
								class="kyyy-word-bank-page__action"
								:class="{ 'is-disabled': isActionDisabled(item.id) }"
								@tap.stop="handleSetDefault(item)"
							>
								设默认
							</view>
							<view
								v-if="item.selected"
								class="kyyy-word-bank-page__action kyyy-word-bank-page__action--ghost"
								:class="{ 'is-disabled': isActionDisabled(item.id) }"
								@tap.stop="handleToggleSelection(item, false)"
							>
								取消
							</view>
							<view
								v-else
								class="kyyy-word-bank-page__action"
								:class="{ 'is-disabled': isActionDisabled(item.id) }"
								@tap.stop="handleToggleSelection(item, true)"
							>
								选择
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else class="kyyy-word-bank-page__state-card">
				<text class="kyyy-word-bank-page__state-text">当前还没有可用词库</text>
			</view>
		</view>

		<template #tabbar>
			<kyyy-tabbar current="practice" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWordBanks, updateWordBankSelection } from '@/pages/kyyy/api/word-bank'
import { loadPracticeSettingsWithFallback, readCachedPracticeSettings, syncPracticeSettings } from '@/pages/kyyy/practice/settings'
import type { KyyyPracticeSettingState } from '@/pages/kyyy/practice/types'
import type {
	KyyyWordBankEntryMode,
	KyyyWordBankListResponse,
	KyyyWordBankListState,
	KyyyWordBankRecordResponse,
	KyyyWordBankRecordState
} from '@/pages/kyyy/word-bank/types'

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

function normalizeEntryMode(value: unknown): KyyyWordBankEntryMode {
	return value === 'study' || value === 'review' ? value : 'default'
}

function createEmptyWordBankList(): KyyyWordBankListState {
	return {
		summary: {
			totalCount: 0,
			selectedCount: 0,
			defaultWordBankId: null
		},
		records: [],
		loaded: false
	}
}

function normalizeWordBankRecord(record: KyyyWordBankRecordResponse | null | undefined): KyyyWordBankRecordState {
	return {
		id: Math.max(toNumber(record?.id), 0),
		bankCode: normalizeText(record?.bankCode),
		bankName: normalizeText(record?.bankName),
		subtitle: normalizeText(record?.subtitle),
		description: normalizeText(record?.description),
		wordCount: Math.max(toNumber(record?.wordCount), 0),
		studyUserCount: Math.max(toNumber(record?.studyUserCount), 0),
		sortNo: Math.max(toNumber(record?.sortNo), 0),
		joinSource: normalizeText(record?.joinSource),
		joinedAt: normalizeText(record?.joinedAt),
		lastPracticeAt: normalizeText(record?.lastPracticeAt),
		selected: Boolean(record?.selected),
		isDefault: Boolean(record?.isDefault)
	}
}

function normalizeWordBankList(result: KyyyWordBankListResponse | null | undefined): KyyyWordBankListState {
	const records = Array.isArray(result?.records)
		? result.records.map((item) => normalizeWordBankRecord(item)).filter((item) => item.id > 0)
		: []
	return {
		summary: {
			totalCount: Math.max(toNumber(result?.summary?.totalCount, records.length), 0),
			selectedCount: Math.max(toNumber(result?.summary?.selectedCount), 0),
			defaultWordBankId: result?.summary?.defaultWordBankId === null || result?.summary?.defaultWordBankId === undefined
				? null
				: (() => {
					const resolved = Math.max(toNumber(result?.summary?.defaultWordBankId), 0)
					return resolved > 0 ? resolved : null
				})()
		},
		records,
		loaded: true
	}
}

interface WordBankPageState {
	entryMode: KyyyWordBankEntryMode
	loading: boolean
	togglingId: number | null
	defaultingId: number | null
	listState: KyyyWordBankListState
	practiceSettings: KyyyPracticeSettingState
}

export default defineComponent({
	name: 'KyyyWordBankPage',
	components: {
		PageShell,
		KyyyTabbar
	},
	data(): WordBankPageState {
		return {
			entryMode: 'default',
			loading: false,
			togglingId: null,
			defaultingId: null,
			listState: createEmptyWordBankList(),
			practiceSettings: readCachedPracticeSettings()
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.entryMode = normalizeEntryMode(query?.mode)
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		eyebrow(): string {
			if (this.entryMode === 'study') {
				return 'STUDY BANK'
			}
			if (this.entryMode === 'review') {
				return 'REVIEW BANK'
			}
			return 'WORD BANK'
		},
		pageTitle(): string {
			if (this.entryMode === 'study') {
				return '学习词库'
			}
			if (this.entryMode === 'review') {
				return '复习词库'
			}
			return '词库'
		},
		defaultRecord(): KyyyWordBankRecordState | null {
			const defaultWordBankId = this.practiceSettings.defaultWordBankId || this.listState.summary.defaultWordBankId
			if (!defaultWordBankId) {
				return null
			}
			return this.listState.records.find((item) => item.id === defaultWordBankId) || null
		}
	},
	methods: {
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-word-bank] bootstrap auth failed', error)
			}
			await this.loadData()
		},
		async loadData(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				const [wordBanks, practiceSettings] = await Promise.all([
					getWordBanks(),
					loadPracticeSettingsWithFallback()
				])
				this.listState = normalizeWordBankList(wordBanks)
				this.practiceSettings = practiceSettings
			} catch (error) {
				console.warn('[kyyy-word-bank] load failed', error)
				this.listState = {
					...this.listState,
					loaded: true
				}
				uni.showToast({
					title: '词库加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		isActionDisabled(bankId: number): boolean {
			return this.togglingId === bankId || this.defaultingId === bankId || this.loading
		},
		async handleToggleSelection(item: KyyyWordBankRecordState, nextSelected: boolean): Promise<void> {
			if (!item.id || this.isActionDisabled(item.id)) {
				return
			}
			this.togglingId = item.id
			try {
				await updateWordBankSelection(item.id, {
					selected: nextSelected
				})
				await this.loadData()
			} catch (error) {
				console.warn('[kyyy-word-bank] toggle selection failed', error)
				uni.showToast({
					title: nextSelected ? '选择失败' : '取消失败',
					icon: 'none'
				})
			} finally {
				this.togglingId = null
			}
		},
		async handleSetDefault(item: KyyyWordBankRecordState): Promise<void> {
			if (!item.id || !item.selected || item.isDefault || this.isActionDisabled(item.id)) {
				return
			}
			this.defaultingId = item.id
			try {
				this.practiceSettings = await syncPracticeSettings({
					defaultWordBankId: item.id
				})
				await this.loadData()
			} catch (error) {
				console.warn('[kyyy-word-bank] set default failed', error)
				uni.showToast({
					title: '设置失败',
					icon: 'none'
				})
			} finally {
				this.defaultingId = null
			}
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-word-bank-page {
	position: relative;
	overflow: hidden;
}

.kyyy-word-bank-page__ambient {
	position: absolute;
	border-radius: 50%;
	filter: blur(10rpx);
	pointer-events: none;
}

.kyyy-word-bank-page__ambient--left {
	top: 84rpx;
	left: -88rpx;
	width: 240rpx;
	height: 240rpx;
	background: radial-gradient(circle, rgba(217, 228, 248, 0.92) 0%, rgba(217, 228, 248, 0) 72%);
}

.kyyy-word-bank-page__ambient--right {
	top: 420rpx;
	right: -96rpx;
	width: 280rpx;
	height: 280rpx;
	background: radial-gradient(circle, rgba(244, 233, 221, 0.86) 0%, rgba(244, 233, 221, 0) 74%);
}

.kyyy-word-bank-page__inner {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding-top: 8rpx;
}

.kyyy-word-bank-page__hero,
.kyyy-word-bank-page__default-card,
.kyyy-word-bank-page__hint-card,
.kyyy-word-bank-page__state-card,
.kyyy-word-bank-page__card {
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow:
		0 18rpx 38rpx rgba(61, 75, 93, 0.08),
		inset 0 0 0 1rpx rgba(222, 229, 238, 0.88);
}

.kyyy-word-bank-page__hero {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	padding: 34rpx 32rpx;
}

.kyyy-word-bank-page__eyebrow {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.18em;
	color: #7a8697;
}

.kyyy-word-bank-page__title {
	font-size: 42rpx;
	line-height: 1.22;
	font-weight: 760;
	color: #243041;
}

.kyyy-word-bank-page__hero-meta,
.kyyy-word-bank-page__card-meta,
.kyyy-word-bank-page__card-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.kyyy-word-bank-page__meta-pill,
.kyyy-word-bank-page__card-tag {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 44rpx;
	padding: 0 18rpx;
	border-radius: 999rpx;
	background: #eff4fb;
	box-shadow: inset 0 0 0 1rpx #d2dce9;
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #506074;
	box-sizing: border-box;
}

.kyyy-word-bank-page__card-tag--default {
	background: #eef5ec;
	box-shadow: inset 0 0 0 1rpx #c8dac8;
	color: #44614f;
}

.kyyy-word-bank-page__default-card,
.kyyy-word-bank-page__hint-card,
.kyyy-word-bank-page__state-card {
	padding: 26rpx 28rpx;
}

.kyyy-word-bank-page__default-label,
.kyyy-word-bank-page__hint-text,
.kyyy-word-bank-page__state-text {
	font-size: 23rpx;
	line-height: 1.5;
	font-weight: 700;
	color: #657487;
}

.kyyy-word-bank-page__default-name {
	display: block;
	margin-top: 8rpx;
	font-size: 34rpx;
	line-height: 1.3;
	font-weight: 760;
	color: #243041;
}

.kyyy-word-bank-page__default-subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	line-height: 1.5;
	color: #708092;
}

.kyyy-word-bank-page__list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.kyyy-word-bank-page__card {
	padding: 24rpx;
}

.kyyy-word-bank-page__card.is-selected {
	box-shadow:
		0 20rpx 40rpx rgba(61, 75, 93, 0.08),
		inset 0 0 0 1rpx rgba(190, 205, 224, 0.96);
}

.kyyy-word-bank-page__card.is-default {
	box-shadow:
		0 20rpx 40rpx rgba(61, 75, 93, 0.08),
		inset 0 0 0 1rpx rgba(188, 214, 193, 0.98);
}

.kyyy-word-bank-page__card-main {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 24rpx;
}

.kyyy-word-bank-page__card-copy {
	min-width: 0;
	flex: 1;
}

.kyyy-word-bank-page__card-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 12rpx;
}

.kyyy-word-bank-page__card-title {
	min-width: 0;
	font-size: 30rpx;
	line-height: 1.35;
	font-weight: 760;
	color: #253244;
}

.kyyy-word-bank-page__card-subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 23rpx;
	line-height: 1.5;
	color: #738094;
}

.kyyy-word-bank-page__card-meta {
	margin-top: 16rpx;
}

.kyyy-word-bank-page__card-meta-text {
	font-size: 22rpx;
	line-height: 1.4;
	font-weight: 650;
	color: #728094;
}

.kyyy-word-bank-page__card-actions {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 12rpx;
	flex-shrink: 0;
}

.kyyy-word-bank-page__action {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 120rpx;
	min-height: 52rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	background: #5f7491;
	box-shadow: 0 12rpx 24rpx rgba(71, 88, 108, 0.14);
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #ffffff;
	box-sizing: border-box;
}

.kyyy-word-bank-page__action--ghost {
	background: #eef3f9;
	box-shadow: inset 0 0 0 1rpx #ced9e7;
	color: #5e6f84;
}

.kyyy-word-bank-page__action.is-disabled {
	opacity: 0.55;
}
</style>
