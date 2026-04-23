<template>
	<!-- AI 索引: KYZZ 学习页 -->
	<page-shell
		current="study"
		root-class="study-page theme-page"
		content-style="padding: 0 24rpx 32rpx;"
	>
		<view class="study-page__inner">
			<view class="study-page__motto">
				<text class="study-page__motto-text">“博观而约取，厚积而薄发”</text>
				<view class="study-page__motto-line"></view>
			</view>

			<view class="study-page__frame study-page__frame--left"></view>
			<view class="study-page__frame study-page__frame--right"></view>
			<view class="study-page__diamond"></view>

			<view class="study-page__hero-shell">
				<view class="study-page__hero">
					<text class="study-page__hero-en">ACADEMIC QUEST</text>
					<text class="study-page__hero-title">{{ heroTitle }}</text>
					<view class="study-page__hero-divider"></view>
					<text class="study-page__hero-bank">{{ heroBankName }}</text>
					<view v-if="recommendedBank" class="study-page__hero-chips">
						<text class="study-page__hero-chip">{{ recommendedBank.resumeLabel }}</text>
						<text class="study-page__hero-chip">{{ difficultyLabel(recommendedBank.difficultyLevel) }}</text>
						<text class="study-page__hero-chip">{{ formatProgress(recommendedBank.currentProgress) }}</text>
					</view>
					<text class="study-page__hero-desc">{{ heroDescription }}</text>
					<button class="study-page__hero-button" @tap="handleStartPractice">
						{{ heroButtonText }}
					</button>
				</view>

				<view class="study-page__streak">
					<uni-icons type="fire-filled" size="14" color="#ffffff" />
					<text class="study-page__streak-text">{{ studyDaysLabel }}</text>
				</view>
			</view>

			<view v-if="dashboard.records.length" class="study-page__queue">
				<view class="study-page__queue-head">
					<text class="study-page__queue-title">继续刷题</text>
					<text class="study-page__queue-desc">{{ dashboard.recommendedReason || '优先回到最近未完成的题库。' }}</text>
				</view>

				<view
					v-for="item in previewBanks"
					:key="item.bankId"
					class="study-page__queue-item"
					@tap="handleBankTap(item.bankId)"
				>
					<view class="study-page__queue-copy">
						<text class="study-page__queue-name">{{ item.bankName }}</text>
						<text class="study-page__queue-sub">{{ item.resumeLabel }} · {{ item.questionCount }} 题</text>
					</view>
					<view class="study-page__queue-side">
						<text class="study-page__queue-progress">{{ formatProgress(item.currentProgress) }}</text>
						<text class="study-page__queue-date">{{ formatLastPractice(item.lastPracticeAt) }}</text>
					</view>
				</view>
			</view>

			<view v-else-if="loadedOnce && !loading" class="study-page__empty">
				<text class="study-page__empty-title">先挑一套题库开始刷题</text>
				<text class="study-page__empty-desc">加入题库后，学习页会自动帮你记住上次刷到哪里，下次直接续上。</text>
				<button class="study-page__empty-button" @tap="goPublicBanks">去添加题库</button>
			</view>

			<study-garden />
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getPracticeDashboard } from '@/pages/kyzz/api/practice'
import { openPracticeTab } from '@/pages/kyzz/practice/navigation'
import type { KyzzPracticeBankViewRecord, KyzzPracticeDashboardState } from '@/pages/kyzz/practice/types'
import { createEmptyPracticeDashboard, difficultyLabel, formatLastPractice, formatProgress, normalizePracticeDashboard } from '@/pages/kyzz/practice/view'

interface StudyPageState {
	loading: boolean
	loadedOnce: boolean
	dashboard: KyzzPracticeDashboardState
}

function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}

export default defineComponent({
	name: 'StudyPage',
	data(): StudyPageState {
		return {
			loading: false,
			loadedOnce: false,
			dashboard: createEmptyPracticeDashboard()
		}
	},
	computed: {
		recommendedBank(): KyzzPracticeBankViewRecord | null {
			return this.dashboard.records.find((item) => item.bankId === this.dashboard.recommendedBankId) || this.dashboard.records[0] || null
		},
		previewBanks(): KyzzPracticeBankViewRecord[] {
			return this.dashboard.records.slice(0, 3)
		},
		heroTitle(): string {
			if (!this.recommendedBank) {
				return '开始刷题'
			}
			return this.recommendedBank.resumeStatus === 'not_started' ? '开始刷题' : '继续刷题'
		},
		heroBankName(): string {
			return this.recommendedBank ? `当前题库：${this.recommendedBank.bankName}` : '当前还没有可练习的题库'
		},
		heroDescription(): string {
			if (this.dashboard.recommendedReason) {
				return this.dashboard.recommendedReason
			}
			return this.recommendedBank
				? '已按你的最近练习状态准备好入口，点一下就能直接进入。'
				: '先从公共题库里选一套适合当前阶段的内容。'
		},
		heroButtonText(): string {
			return this.recommendedBank ? '进入刷题' : '去添加题库'
		},
		studyDaysLabel(): string {
			if (!this.recommendedBank) {
				return 'READY'
			}
			if (this.recommendedBank.resumeStatus === 'completed') {
				return 'REVIEW'
			}
			return `第 ${Math.max(this.recommendedBank.resumeQuestionIndex, 1)} 题`
		}
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	methods: {
		async bootstrapAndLoad(): Promise<void> {
			if (this.loading) {
				return
			}
			this.loading = true
			try {
				await bootstrapAuth({ silent: true })
				const result = await getPracticeDashboard()
				this.dashboard = normalizePracticeDashboard(result)
				this.loadedOnce = true
			} catch (error) {
				this.loadedOnce = true
				uni.showToast({
					title: resolveErrorMessage(error, '学习页加载失败'),
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		difficultyLabel,
		formatLastPractice,
		formatProgress,
		handleStartPractice(): void {
			if (!this.recommendedBank) {
				this.goPublicBanks()
				return
			}
			openPracticeTab().catch(() => {})
		},
		handleBankTap(bankId: number): void {
			openPracticeTab({ bankId }).catch(() => {})
		},
		goPublicBanks(): void {
			uni.navigateTo({
				url: '/pages/kyzz/question-bank/public'
			})
		}
	}
})
</script>

<style lang="scss">
@import '@/uni.scss';

.study-page {
	background:
		radial-gradient(circle at top, rgba(255, 255, 255, 0.98) 0%, rgba(244, 247, 252, 0.94) 48%, rgba(236, 241, 247, 0.92) 100%);
	box-sizing: border-box;
}

.study-page__inner {
	position: relative;
	padding-top: 96rpx;
	overflow: hidden;
}

.study-page__motto {
	position: relative;
	z-index: 2;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.study-page__motto-text {
	font-size: 30rpx;
	line-height: 1.5;
	font-weight: 600;
	font-style: italic;
	letter-spacing: 0.08em;
	color: rgba(120, 127, 142, 0.88);
}

.study-page__motto-line {
	margin-top: 24rpx;
	width: 182rpx;
	height: 2rpx;
	background: rgba(178, 186, 201, 0.9);
}

.study-page__frame,
.study-page__diamond {
	position: absolute;
	pointer-events: none;
}

.study-page__frame {
	top: 132rpx;
	width: 260rpx;
	height: 260rpx;
	border-top: 2rpx solid rgba(197, 206, 220, 0.48);
}

.study-page__frame--left {
	left: -92rpx;
	transform: skewY(-35deg);
}

.study-page__frame--right {
	right: -92rpx;
	transform: skewY(35deg);
}

.study-page__diamond {
	left: 50%;
	top: 248rpx;
	width: 520rpx;
	height: 520rpx;
	border: 2rpx solid rgba(202, 211, 224, 0.32);
	transform: translateX(-50%) rotate(45deg);
}

.study-page__hero-shell {
	position: relative;
	z-index: 2;
	margin: 108rpx auto 0;
	width: 100%;
	padding: 18rpx;
	border-radius: 26rpx;
	background: rgba(248, 250, 253, 0.95);
	box-shadow:
		0 14rpx 40rpx rgba(147, 160, 182, 0.18),
		inset 0 0 0 2rpx rgba(196, 205, 220, 0.85);
	box-sizing: border-box;
}

.study-page__hero {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 88rpx 40rpx 84rpx;
	border-radius: 22rpx;
	background: linear-gradient(180deg, #202837 0%, #2b3446 100%);
	box-shadow:
		inset 0 0 0 2rpx rgba(255, 255, 255, 0.05),
		0 10rpx 22rpx rgba(49, 58, 76, 0.18);
}

.study-page__hero-en {
	font-size: 24rpx;
	line-height: 1.4;
	letter-spacing: 0.22em;
	color: rgba(206, 212, 223, 0.6);
}

.study-page__hero-title {
	margin-top: 32rpx;
	font-size: 66rpx;
	line-height: 1.18;
	font-weight: 700;
	letter-spacing: 0.04em;
	color: #ffffff;
}

.study-page__hero-divider {
	margin: 38rpx 0 32rpx;
	width: 90rpx;
	height: 4rpx;
	border-radius: 999rpx;
	background: rgba(180, 191, 209, 0.62);
}

.study-page__hero-bank {
	display: block;
	font-size: 28rpx;
	line-height: 1.6;
	color: rgba(232, 236, 242, 0.86);
	text-align: center;
}

.study-page__hero-chips {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	justify-content: center;
	margin-top: 18rpx;
}

.study-page__hero-chip {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 44rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.1);
	font-size: 20rpx;
	line-height: 1;
	color: rgba(236, 240, 246, 0.88);
}

.study-page__hero-desc {
	display: block;
	margin-top: 18rpx;
	font-size: 25rpx;
	line-height: 1.7;
	text-align: center;
	color: rgba(226, 231, 239, 0.8);
}

.study-page__hero-button {
	margin-top: 28rpx;
	padding: 0 36rpx;
	height: 84rpx;
	line-height: 84rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.95);
	color: #313b4d;
	font-size: 25rpx;
	font-weight: 700;
	box-shadow: 0 14rpx 28rpx rgba(19, 26, 41, 0.18);
}

.study-page__hero-button::after {
	border: 0;
}

.study-page__streak {
	position: absolute;
	right: 0;
	bottom: 34rpx;
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	padding: 10rpx 24rpx;
	border: 4rpx solid #ffffff;
	border-radius: 999rpx;
	background: linear-gradient(180deg, #6a738c 0%, #7a8399 100%);
	box-shadow: 0 10rpx 20rpx rgba(110, 121, 143, 0.24);
	transform: translateX(14rpx);
}

.study-page__streak-text {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	letter-spacing: 0.04em;
	color: #ffffff;
}

.study-page__queue,
.study-page__empty {
	position: relative;
	z-index: 2;
	margin-top: 24rpx;
	padding: 28rpx 24rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.05);
}

.study-page__queue-head {
	display: flex;
	flex-direction: column;
}

.study-page__queue-title {
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2d3642;
}

.study-page__queue-desc {
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #778293;
}

.study-page__queue-item {
	display: flex;
	align-items: center;
	gap: 18rpx;
	margin-top: 18rpx;
	padding-top: 18rpx;
	border-top: 1rpx solid rgba(225, 231, 239, 0.84);
}

.study-page__queue-copy {
	flex: 1;
	min-width: 0;
}

.study-page__queue-name {
	display: block;
	font-size: 26rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #2e3643;
}

.study-page__queue-sub {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	line-height: 1.5;
	color: #7c8795;
}

.study-page__queue-side {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	flex-shrink: 0;
}

.study-page__queue-progress {
	font-size: 22rpx;
	line-height: 1;
	font-weight: 700;
	color: #55627a;
}

.study-page__queue-date {
	margin-top: 8rpx;
	font-size: 20rpx;
	line-height: 1.2;
	color: #8b94a1;
}

.study-page__empty-title {
	display: block;
	font-size: 30rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #2d3642;
}

.study-page__empty-desc {
	display: block;
	margin-top: 14rpx;
	font-size: 24rpx;
	line-height: 1.7;
	color: #7a8594;
}

.study-page__empty-button {
	margin-top: 24rpx;
	padding: 0 32rpx;
	height: 78rpx;
	line-height: 78rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #545e76 0%, #7f8ca7 100%);
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
	box-shadow: 0 14rpx 28rpx rgba(84, 94, 118, 0.2);
}

.study-page__empty-button::after {
	border: 0;
}

@media screen and (max-width: 375px) {
	.study-page__hero {
		padding-left: 28rpx;
		padding-right: 28rpx;
	}

	.study-page__hero-title {
		font-size: 58rpx;
	}

	.study-page__hero-bank {
		font-size: 24rpx;
	}
}
</style>
