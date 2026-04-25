<template>
	<view class="leaderboard-page" @tap="handlePageTap">
		<view class="leaderboard-page__scope-tabs">
			<view
				v-for="tab in scopeTabs"
				:key="tab.scope"
				class="leaderboard-page__scope-tab"
				:class="{ 'is-active': currentScope === tab.scope }"
				@tap="handleScopeChange(tab.scope)"
			>
				<view class="leaderboard-page__scope-icon-shell" :style="{ background: tab.iconShellBackground }">
					<uni-icons :type="tab.icon" size="18" :color="tab.iconColor" />
				</view>
				<text class="leaderboard-page__scope-label">{{ tab.label }}</text>
			</view>
		</view>

		<view class="leaderboard-page__my-card">
			<view class="leaderboard-page__my-head">
				<view class="leaderboard-page__my-title">
					<uni-icons type="staff-filled" size="18" color="#6b5d40" />
					<text class="leaderboard-page__my-title-text">我的排名焦点</text>
				</view>
				<view class="leaderboard-page__my-head-actions">
					<text class="leaderboard-page__my-range">{{ scopeLabel(currentScope) }}</text>
					<view class="leaderboard-page__rule-trigger" @tap.stop="toggleRulePopover">
						<text class="leaderboard-page__rule-trigger-text">!</text>
					</view>
				</view>
			</view>

			<view v-if="showRulePopover" class="leaderboard-page__rule-popover" @tap.stop>
				<view class="leaderboard-page__rule-popover-arrow"></view>
				<view class="leaderboard-page__rule-popover-row">
					<text class="leaderboard-page__rule-popover-label">区间时间</text>
					<text class="leaderboard-page__rule-popover-value">
						{{ dashboard.summary.periodLabel || scopeLabel(currentScope) }} · {{ formatRuleTime(dashboard.summary.generatedAt) }}
					</text>
				</view>
				<view class="leaderboard-page__rule-popover-row">
					<text class="leaderboard-page__rule-popover-label">排序规则</text>
					<text class="leaderboard-page__rule-popover-value">{{ dashboard.summary.ruleDescription }}</text>
				</view>
			</view>

			<view v-if="dashboard.myRecord" class="leaderboard-page__my-main">
				<view class="leaderboard-page__my-rank-shell">
					<text class="leaderboard-page__my-rank-label">当前名次</text>
					<text class="leaderboard-page__my-rank-value">#{{ dashboard.myRecord.rankNo }}</text>
					<text class="leaderboard-page__my-last">{{ formatLeaderboardTime(dashboard.myRecord.lastPracticeAt) }}</text>
				</view>
				<view class="leaderboard-page__my-metrics">
					<view class="leaderboard-page__metric-pill">
						<uni-icons type="compose" size="14" color="#61779d" />
						<text class="leaderboard-page__metric-text">做题 {{ dashboard.myRecord.studyCount }}</text>
					</view>
					<view class="leaderboard-page__metric-pill">
						<uni-icons type="checkbox-filled" size="14" color="#5e8d69" />
						<text class="leaderboard-page__metric-text">答对 {{ dashboard.myRecord.correctCount }}</text>
					</view>
					<view class="leaderboard-page__metric-pill">
						<uni-icons type="star-filled" size="14" color="#bd8a4c" />
						<text class="leaderboard-page__metric-text">正确率 {{ formatAccuracyRate(dashboard.myRecord.accuracyRate) }}</text>
					</view>
				</view>
			</view>

			<view v-else class="leaderboard-page__my-empty">
				<text class="leaderboard-page__my-empty-title">本周期还未上榜</text>
				<text class="leaderboard-page__my-empty-desc">先做几道题就会进入榜单，焦点卡会第一时间显示你的排名变化。</text>
				<button class="leaderboard-page__my-empty-action" @tap="goPractice">去刷题冲榜</button>
			</view>
		</view>

		<view v-if="refreshing" class="leaderboard-page__refreshing">
			<uni-icons type="refresh" size="15" color="#73829a" />
			<text class="leaderboard-page__refreshing-text">正在刷新榜单数据...</text>
		</view>

		<view v-if="loading && !loadedOnce" class="leaderboard-page__state-card">
			<text class="leaderboard-page__state-title">正在统计最新排行...</text>
			<text class="leaderboard-page__state-desc">会按做题量、正确率与活跃时间生成榜单。</text>
		</view>

		<view v-else-if="loadErrorMessage && !hasDisplayData" class="leaderboard-page__state-card leaderboard-page__state-card--error">
			<text class="leaderboard-page__state-title">排行榜加载失败</text>
			<text class="leaderboard-page__state-desc">{{ loadErrorMessage }}</text>
			<button class="leaderboard-page__retry-btn" @tap="retryLoad">重试加载</button>
		</view>

		<template v-else>
			<view v-if="topThreeRecords.length" class="leaderboard-page__top3">
				<view class="leaderboard-page__section-head">
					<text class="leaderboard-page__section-title">TOP 3 冲榜区</text>
					<text class="leaderboard-page__section-meta">信息实时聚合，排名规则一致公开</text>
				</view>
				<view class="leaderboard-page__top3-list">
					<view
						v-for="item in topThreeRecords"
						:key="`top-${item.userId}`"
						class="leaderboard-page__top3-card"
						:class="{ 'is-me': item.isMe }"
					>
						<view class="leaderboard-page__top3-rank">
							<uni-icons type="medal-filled" size="22" :color="rankIconColor(item.rankNo)" />
							<text class="leaderboard-page__top3-rank-no">#{{ item.rankNo }}</text>
						</view>
						<view class="leaderboard-page__top3-user">
							<image v-if="item.avatarUrl" class="leaderboard-page__avatar" :src="item.avatarUrl" mode="aspectFill" />
							<view v-else class="leaderboard-page__avatar leaderboard-page__avatar--fallback">
								<text class="leaderboard-page__avatar-text">{{ avatarInitial(item.nickname) }}</text>
							</view>
							<text class="leaderboard-page__top3-name">{{ item.nickname }}</text>
						</view>
						<text class="leaderboard-page__top3-score">{{ item.studyCount }} 题 · {{ formatAccuracyRate(item.accuracyRate) }}</text>
					</view>
				</view>
			</view>

			<view v-if="dashboard.records.length" class="leaderboard-page__full-list">
				<view class="leaderboard-page__section-head">
					<text class="leaderboard-page__section-title">完整榜单</text>
					<text class="leaderboard-page__section-meta">名次、做题数、答对数、正确率全部公开</text>
				</view>
				<view
					v-for="item in dashboard.records"
					:key="item.userId"
					class="leaderboard-page__row"
					:class="{ 'is-me': item.isMe }"
				>
					<view class="leaderboard-page__row-rank" :class="rankClass(item.rankNo)">
						<text class="leaderboard-page__row-rank-no">#{{ item.rankNo }}</text>
					</view>
					<view class="leaderboard-page__row-main">
						<view class="leaderboard-page__row-user">
							<image v-if="item.avatarUrl" class="leaderboard-page__avatar leaderboard-page__avatar--small" :src="item.avatarUrl" mode="aspectFill" />
							<view v-else class="leaderboard-page__avatar leaderboard-page__avatar--small leaderboard-page__avatar--fallback">
								<text class="leaderboard-page__avatar-text">{{ avatarInitial(item.nickname) }}</text>
							</view>
							<view class="leaderboard-page__row-user-copy">
								<text class="leaderboard-page__row-name">{{ item.nickname }}</text>
							</view>
							<view v-if="item.isMe" class="leaderboard-page__me-badge">我</view>
						</view>
						<view class="leaderboard-page__row-metrics">
							<view class="leaderboard-page__metric-pill">
								<uni-icons type="compose" size="13" color="#60779f" />
								<text class="leaderboard-page__metric-text">做题 {{ item.studyCount }}</text>
							</view>
							<view class="leaderboard-page__metric-pill">
								<uni-icons type="checkbox-filled" size="13" color="#5f8a6b" />
								<text class="leaderboard-page__metric-text">答对 {{ item.correctCount }}</text>
							</view>
							<view class="leaderboard-page__metric-pill">
								<uni-icons type="star-filled" size="13" color="#b98547" />
								<text class="leaderboard-page__metric-text">正确率 {{ formatAccuracyRate(item.accuracyRate) }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else class="leaderboard-page__state-card">
				<text class="leaderboard-page__state-title">当前区间还没有上榜数据</text>
				<text class="leaderboard-page__state-desc">先做题后会自动进入该区间排行榜，并在上方焦点卡同步你的名次。</text>
				<button class="leaderboard-page__retry-btn" @tap="goPractice">去刷题</button>
			</view>
		</template>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getLeaderboard } from '@/pages/kyzz/api/leaderboard'
import type {
	KyzzLeaderboardDashboardState,
	KyzzLeaderboardRecordState,
	KyzzLeaderboardScope
} from '@/pages/kyzz/leaderboard/types'
import {
	createEmptyLeaderboardState,
	formatAccuracyRate,
	formatLeaderboardTime,
	normalizeLeaderboardDashboard
} from '@/pages/kyzz/leaderboard/view'

// AI 索引: KYZZ 小程序排行榜正式页。

interface LeaderboardScopeTab {
	scope: KyzzLeaderboardScope
	label: string
	icon: string
	iconColor: string
	iconShellBackground: string
}

interface LoadOptions {
	forceLoading?: boolean
}

interface LeaderboardPageState {
	loading: boolean
	refreshing: boolean
	loadedOnce: boolean
	loadErrorMessage: string
	showRulePopover: boolean
	currentScope: KyzzLeaderboardScope
	requestToken: number
	dashboard: KyzzLeaderboardDashboardState
	scopeTabs: LeaderboardScopeTab[]
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'KyzzLeaderboardPage',
	data(): LeaderboardPageState {
		return {
			loading: false,
			refreshing: false,
			loadedOnce: false,
			loadErrorMessage: '',
			showRulePopover: false,
			currentScope: 'daily',
			requestToken: 0,
			dashboard: createEmptyLeaderboardState('daily'),
			scopeTabs: [
				{
					scope: 'daily',
					label: '日榜',
					icon: 'calendar-filled',
					iconColor: '#334f7c',
					iconShellBackground: '#e8f0ff'
				},
				{
					scope: 'weekly',
					label: '周榜',
					icon: 'fire-filled',
					iconColor: '#78511f',
					iconShellBackground: '#fff0dc'
				},
				{
					scope: 'total',
					label: '总榜',
					icon: 'medal-filled',
					iconColor: '#735817',
					iconShellBackground: '#fff6df'
				}
			]
		}
	},
	computed: {
		topThreeRecords(): KyzzLeaderboardRecordState[] {
			return this.dashboard.records.slice(0, 3)
		},
		hasDisplayData(): boolean {
			return this.dashboard.records.length > 0 || Boolean(this.dashboard.myRecord)
		}
	},
	onShow() {
		this.loadLeaderboard({ forceLoading: true })
	},
	methods: {
		formatAccuracyRate,
		formatLeaderboardTime,
		scopeLabel(scope: KyzzLeaderboardScope): string {
			if (scope === 'weekly') {
				return '周榜'
			}
			if (scope === 'total') {
				return '总榜'
			}
			return '日榜'
		},
		rankIconColor(rankNo: number): string {
			if (rankNo === 1) {
				return '#e7ad2f'
			}
			if (rankNo === 2) {
				return '#9ca6b6'
			}
			if (rankNo === 3) {
				return '#b47a52'
			}
			return '#76849d'
		},
		rankClass(rankNo: number): string {
			if (rankNo === 1) {
				return 'is-rank-1'
			}
			if (rankNo === 2) {
				return 'is-rank-2'
			}
			if (rankNo === 3) {
				return 'is-rank-3'
			}
			return 'is-rank-normal'
		},
		formatRuleTime(value: string | null): string {
			if (!value) {
				return '暂无'
			}
			const normalized = value.replace(/-/g, '/')
			const targetDate = new Date(normalized)
			if (Number.isNaN(targetDate.getTime())) {
				return value.slice(0, 16).replace('T', ' ')
			}
			const year = targetDate.getFullYear()
			const month = String(targetDate.getMonth() + 1).padStart(2, '0')
			const day = String(targetDate.getDate()).padStart(2, '0')
			const hour = String(targetDate.getHours()).padStart(2, '0')
			const minute = String(targetDate.getMinutes()).padStart(2, '0')
			return `${year}-${month}-${day} ${hour}:${minute}`
		},
		toggleRulePopover(): void {
			this.showRulePopover = !this.showRulePopover
		},
		handlePageTap(): void {
			if (this.showRulePopover) {
				this.showRulePopover = false
			}
		},
		avatarInitial(name: string): string {
			if (!name) {
				return '学'
			}
			return name.trim().slice(0, 1).toUpperCase()
		},
		async handleScopeChange(scope: KyzzLeaderboardScope): Promise<void> {
			if (scope === this.currentScope) {
				return
			}
			this.currentScope = scope
			this.showRulePopover = false
			this.dashboard = createEmptyLeaderboardState(scope)
			this.loadedOnce = false
			await this.loadLeaderboard({ forceLoading: true })
		},
		async loadLeaderboard(options: LoadOptions = {}): Promise<void> {
			const token = ++this.requestToken
			const hadData = this.hasDisplayData
			this.loadErrorMessage = ''
			if (!this.loadedOnce || options.forceLoading) {
				this.loading = true
			} else {
				this.refreshing = true
			}
			try {
				await bootstrapAuth({ silent: true })
				const result = await getLeaderboard({
					scope: this.currentScope,
					limit: 50
				})
				if (token !== this.requestToken) {
					return
				}
				this.dashboard = normalizeLeaderboardDashboard(result, this.currentScope)
				this.loadedOnce = true
			} catch (error) {
				if (token !== this.requestToken) {
					return
				}
				this.loadedOnce = true
				this.loadErrorMessage = resolveErrorMessage(error, '排行榜加载失败')
				if (hadData) {
					uni.showToast({
						title: this.loadErrorMessage,
						icon: 'none'
					})
				}
			} finally {
				if (token === this.requestToken) {
					this.loading = false
					this.refreshing = false
				}
			}
		},
		retryLoad(): void {
			this.showRulePopover = false
			this.loadLeaderboard({ forceLoading: true })
		},
		goPractice(): void {
			this.showRulePopover = false
			uni.switchTab({
				url: '/pages/kyzz/practice/index'
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.leaderboard-page {
	min-height: 100vh;
	padding: 24rpx 24rpx calc(env(safe-area-inset-bottom) + 32rpx);
	box-sizing: border-box;
	background: linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
}

.leaderboard-page__scope-tabs {
	display: flex;
	gap: 14rpx;
}

.leaderboard-page__scope-tab {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	height: 76rpx;
	border-radius: 22rpx;
	background: #ffffff;
	box-shadow:
		0 8rpx 18rpx rgba(45, 58, 77, 0.04),
		inset 0 0 0 1rpx #d6deea;
	transition: all 0.2s ease;
}

.leaderboard-page__scope-tab.is-active {
	background: #eef4ff;
	box-shadow:
		0 14rpx 24rpx rgba(55, 84, 132, 0.1),
		inset 0 0 0 1rpx #9fb7dd;
}

.leaderboard-page__scope-icon-shell {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38rpx;
	height: 38rpx;
	border-radius: 12rpx;
}

.leaderboard-page__scope-label {
	font-size: 24rpx;
	color: #334155;
	font-weight: 700;
}

.leaderboard-page__my-card,
.leaderboard-page__top3,
.leaderboard-page__full-list,
.leaderboard-page__state-card {
	margin-top: 20rpx;
	border-radius: 26rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 30rpx rgba(45, 58, 77, 0.055);
	border: 1rpx solid #d6deea;
}

.leaderboard-page__my-card {
	position: relative;
	padding: 24rpx;
	background: linear-gradient(135deg, #ffffff 0%, #fffaf0 100%);
	border-color: #e6cf91;
}

.leaderboard-page__my-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.leaderboard-page__my-head-actions {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
}

.leaderboard-page__my-title {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
}

.leaderboard-page__my-title-text {
	font-size: 28rpx;
	font-weight: 700;
	color: #3c4a58;
}

.leaderboard-page__my-range {
	font-size: 22rpx;
	color: #735817;
	background: #fff6df;
	padding: 6rpx 12rpx;
	border-radius: 999rpx;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
}

.leaderboard-page__rule-trigger {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 44rpx;
	height: 44rpx;
	border-radius: 50%;
	background: #ffffff;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
}

.leaderboard-page__rule-trigger-text {
	font-size: 28rpx;
	line-height: 1;
	font-weight: 700;
	color: #735817;
}

.leaderboard-page__rule-popover {
	position: absolute;
	top: 84rpx;
	right: 24rpx;
	z-index: 10;
	width: 470rpx;
	max-width: calc(100% - 48rpx);
	padding: 18rpx;
	border-radius: 18rpx;
	background: #ffffff;
	box-shadow: 0 14rpx 28rpx rgba(45, 58, 77, 0.12);
	border: 1rpx solid #e6cf91;
}

.leaderboard-page__rule-popover-arrow {
	position: absolute;
	top: -10rpx;
	right: 28rpx;
	width: 20rpx;
	height: 20rpx;
	background: #ffffff;
	transform: rotate(45deg);
}

.leaderboard-page__rule-popover-row + .leaderboard-page__rule-popover-row {
	margin-top: 10rpx;
}

.leaderboard-page__rule-popover-label {
	display: block;
	font-size: 20rpx;
	color: #735817;
}

.leaderboard-page__rule-popover-value {
	display: block;
	margin-top: 6rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #3c4a58;
}

.leaderboard-page__my-main {
	margin-top: 16rpx;
	display: flex;
	gap: 14rpx;
}

.leaderboard-page__my-rank-shell {
	flex: 0 0 220rpx;
	padding: 14rpx 16rpx;
	border-radius: 18rpx;
	background: #fff6df;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
}

.leaderboard-page__my-rank-label {
	display: block;
	font-size: 20rpx;
	color: #735817;
}

.leaderboard-page__my-rank-value {
	display: block;
	margin-top: 8rpx;
	font-size: 42rpx;
	line-height: 1;
	font-weight: 700;
	color: #4f3d16;
}

.leaderboard-page__my-last {
	display: block;
	margin-top: 12rpx;
	font-size: 20rpx;
	color: #735817;
}

.leaderboard-page__my-metrics {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 10rpx;
}

.leaderboard-page__metric-pill {
	display: inline-flex;
	align-items: center;
	padding: 8rpx 12rpx;
	gap: 8rpx;
	border-radius: 999rpx;
	background: #f7f9fc;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.leaderboard-page__metric-text {
	font-size: 21rpx;
	color: #3c4a58;
}

.leaderboard-page__my-empty {
	margin-top: 16rpx;
	padding: 18rpx;
	border-radius: 18rpx;
	background: #ffffff;
	box-shadow: inset 0 0 0 1rpx #d6deea;
}

.leaderboard-page__my-empty-title {
	display: block;
	font-size: 26rpx;
	color: #1f2937;
	font-weight: 700;
}

.leaderboard-page__my-empty-desc {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #5f6d7f;
}

.leaderboard-page__my-empty-action,
.leaderboard-page__retry-btn {
	margin-top: 14rpx;
	height: 76rpx;
	line-height: 76rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 700;
}

.leaderboard-page__my-empty-action::after,
.leaderboard-page__retry-btn::after {
	border: 0;
}

.leaderboard-page__my-empty-action {
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
}

.leaderboard-page__retry-btn {
	background: linear-gradient(135deg, #465a7e 0%, #6d7f9b 100%);
	color: #ffffff;
	box-shadow: 0 12rpx 22rpx rgba(54, 75, 111, 0.18);
}

.leaderboard-page__refreshing {
	margin-top: 16rpx;
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: #eef2f6;
	box-shadow: inset 0 0 0 1rpx #c9d2dc;
}

.leaderboard-page__refreshing-text {
	font-size: 20rpx;
	color: #3c4a58;
}

.leaderboard-page__state-card {
	padding: 28rpx 24rpx;
}

.leaderboard-page__state-card--error {
	background: #fdebea;
	border-color: #dc9b96;
}

.leaderboard-page__state-title {
	display: block;
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2937;
}

.leaderboard-page__state-desc {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.7;
	color: #5f6d7f;
}

.leaderboard-page__section-head {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
}

.leaderboard-page__section-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2937;
}

.leaderboard-page__section-meta {
	font-size: 20rpx;
	color: #5f6d7f;
}

.leaderboard-page__top3,
.leaderboard-page__full-list {
	padding: 22rpx;
}

.leaderboard-page__top3-list {
	margin-top: 14rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.leaderboard-page__top3-card {
	padding: 16rpx;
	border-radius: 20rpx;
	background: #f7f9fc;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.leaderboard-page__top3-card.is-me {
	background: #fff6df;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
}

.leaderboard-page__top3-rank {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	min-width: 106rpx;
}

.leaderboard-page__top3-rank-no {
	font-size: 24rpx;
	font-weight: 700;
	color: #334155;
}

.leaderboard-page__top3-user {
	flex: 1;
	display: flex;
	align-items: center;
	min-width: 0;
}

.leaderboard-page__top3-name {
	margin-left: 10rpx;
	font-size: 24rpx;
	color: #1f2937;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.leaderboard-page__top3-score {
	font-size: 22rpx;
	color: #3c4a58;
}

.leaderboard-page__full-list {
	margin-bottom: 16rpx;
}

.leaderboard-page__row {
	margin-top: 12rpx;
	padding: 14rpx;
	border-radius: 18rpx;
	background: #f7f9fc;
	display: flex;
	gap: 12rpx;
	box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.leaderboard-page__row.is-me {
	background: #fff6df;
	box-shadow: inset 0 0 0 1rpx #e6cf91;
}

.leaderboard-page__row-rank {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 86rpx;
	height: 86rpx;
	border-radius: 18rpx;
	flex-shrink: 0;
}

.leaderboard-page__row-rank.is-rank-1 {
	background: linear-gradient(135deg, #ffe8b8 0%, #fff3cf 100%);
}

.leaderboard-page__row-rank.is-rank-2 {
	background: linear-gradient(135deg, #e7edf4 0%, #f4f7fb 100%);
}

.leaderboard-page__row-rank.is-rank-3 {
	background: linear-gradient(135deg, #f5dfd0 0%, #faece2 100%);
}

.leaderboard-page__row-rank.is-rank-normal {
	background: #e8f0ff;
}

.leaderboard-page__row-rank-no {
	font-size: 24rpx;
	font-weight: 700;
	color: #334155;
}

.leaderboard-page__row-main {
	flex: 1;
	min-width: 0;
}

.leaderboard-page__row-user {
	display: flex;
	align-items: center;
}

.leaderboard-page__row-user-copy {
	flex: 1;
	min-width: 0;
	margin-left: 10rpx;
}

.leaderboard-page__row-name {
	display: block;
	font-size: 24rpx;
	color: #1f2937;
	font-weight: 700;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.leaderboard-page__me-badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	height: 38rpx;
	padding: 0 12rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #e0bc74 0%, #efce8c 100%);
	color: #5b4626;
	font-size: 20rpx;
	font-weight: 700;
}

.leaderboard-page__row-metrics {
	margin-top: 10rpx;
	display: flex;
	flex-wrap: wrap;
	gap: 8rpx;
}

.leaderboard-page__avatar {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	flex-shrink: 0;
}

.leaderboard-page__avatar--small {
	width: 48rpx;
	height: 48rpx;
}

.leaderboard-page__avatar--fallback {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #8b95ac 0%, #adb6c8 100%);
}

.leaderboard-page__avatar-text {
	font-size: 22rpx;
	font-weight: 700;
	color: #ffffff;
}
</style>
