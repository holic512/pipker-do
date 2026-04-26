<template>
  <!-- AI 索引: KYZZ 学习页 -->
  <page-shell
      current="study"
      root-class="study-page theme-page"
      content-style="padding: 0 24rpx 32rpx;"
  >
    <!-- 高性能极简动态背景 -->
    <view class="study-page__ambient-bg">
      <view class="study-page__glow study-page__glow--1"></view>
      <view class="study-page__glow study-page__glow--2"></view>
    </view>

    <view class="study-page__inner">
      <!-- 打字机励志语录 -->
      <view class="study-page__motto">
        <text class="study-page__motto-text">
          {{ displayedMotto }}<text class="study-page__cursor" :class="{ 'is-typing': isTyping }"></text>
        </text>
        <view class="study-page__motto-line"></view>
      </view>

      <!-- 几何装饰与动效 -->
      <view class="study-page__frame study-page__frame--left"></view>
      <view class="study-page__frame study-page__frame--right"></view>
      <view class="study-page__diamond"></view>

      <view class="study-page__hero-shell">
        <view class="study-page__hero">
          <template v-if="!isInitialLoading">
            <text class="study-page__hero-en">ACADEMIC QUEST</text>
            <text class="study-page__hero-title">{{ heroTitle }}</text>
            <view class="study-page__hero-divider"></view>
            <text class="study-page__hero-bank">{{ heroBankName }}</text>

            <view class="study-page__hero-action">
              <button
                  class="study-page__hero-button-wrapper"
                  :class="{ 'is-progress-active': recommendedBank }"
                  :style="progressStyle"
                  @tap="handleStartPractice"
              >
                <view class="study-page__hero-button-inner">
                  <text class="study-page__hero-btn-text">{{ heroButtonText }}</text>
                  <view v-if="recommendedBank" class="study-page__hero-btn-pct">
                    <view class="study-page__hero-btn-dot"></view>
                    <text>{{ formatProgress(recommendedBank.currentProgress) }}</text>
                  </view>
                </view>
              </button>
            </view>
          </template>

          <template v-else>
            <view class="study-page__hero-skeleton-mark study-page__skeleton-shimmer"></view>
            <view class="study-page__hero-skeleton-title study-page__skeleton-shimmer"></view>
            <view class="study-page__hero-skeleton-divider study-page__skeleton-shimmer"></view>
            <view class="study-page__hero-skeleton-text study-page__skeleton-shimmer"></view>
            <view class="study-page__hero-skeleton-button study-page__skeleton-shimmer"></view>
          </template>
        </view>

        <view class="study-page__streak" :class="{ 'is-skeleton': isInitialLoading }">
          <template v-if="!isInitialLoading">
            <uni-icons type="fire-filled" size="14" color="#ff9f2e" />
            <text class="study-page__streak-text">{{ studyDaysLabel }}</text>
          </template>
          <template v-else>
            <view class="study-page__streak-dot"></view>
            <text class="study-page__streak-text">正在同步进度</text>
          </template>
        </view>
      </view>

      <view class="study-page__shortcut-shell">
        <view class="study-page__shortcut-title-row">
          <view class="study-page__shortcut-title-mark">
            <view class="study-page__shortcut-title-bar study-page__shortcut-title-bar--left"></view>
            <view class="study-page__shortcut-title-bar study-page__shortcut-title-bar--right"></view>
          </view>
          <text class="study-page__shortcut-title">常用入口</text>
        </view>

        <view class="study-page__shortcut-grid">
          <template v-if="!isInitialLoading">
            <view
                v-for="item in shortcutItems"
                :key="item.key"
                class="study-page__shortcut-item"
                @tap="openShortcut(item)"
            >
              <view class="study-page__shortcut-card">
                <view class="study-page__shortcut-card-glow"></view>
                <view class="study-page__shortcut-icon">
                  <uni-icons :type="item.icon" size="29" :color="item.iconColor" />
                </view>
                <view class="study-page__shortcut-copy">
                  <text class="study-page__shortcut-card-title">{{ item.title }}</text>
                  <text class="study-page__shortcut-card-desc">{{ item.description }}</text>
                </view>
                <view class="study-page__shortcut-arrow">
                  <uni-icons type="right" size="13" color="#8e98aa" />
                </view>
              </view>
            </view>
          </template>
          <template v-else>
            <view
                v-for="item in shortcutItems"
                :key="`${item.key}-skeleton`"
                class="study-page__shortcut-item"
            >
              <view class="study-page__shortcut-card study-page__shortcut-card--skeleton study-page__skeleton-shimmer"></view>
              <view class="study-page__shortcut-title-skeleton study-page__skeleton-shimmer"></view>
            </view>
          </template>
        </view>
      </view>

    </view>
  </page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getCachedPracticeDashboard, preloadPracticeDashboard, warmKyzzCorePreload } from '@/shared/preload/kyzz'
import { openBankPracticeTab, openPracticeTab } from '@/pages/kyzz/practice/navigation'
import type { KyzzPracticeBankViewRecord, KyzzPracticeDashboardState } from '@/pages/kyzz/practice/types'
import { createEmptyPracticeDashboard, formatProgress, normalizePracticeDashboard } from '@/pages/kyzz/practice/view'

interface StudyShortcutItem {
  key: string
  title: string
  description: string
  icon: string
  iconColor: string
  pagePath?: string
}

interface StudyPageState {
  loading: boolean
  loadedOnce: boolean
  dashboard: KyzzPracticeDashboardState
  fullMotto: string
  displayedMotto: string
  isTyping: boolean
  typewriterTimer: number | null
  shortcutItems: StudyShortcutItem[]
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
      dashboard: createEmptyPracticeDashboard(),
      fullMotto: '“博观而约取，厚积而薄发”',
      displayedMotto: '',
      isTyping: false,
      typewriterTimer: null,
      shortcutItems: [
        {
          key: 'favorite',
          title: '收藏',
          description: '高频重点',
          icon: 'star-filled',
          iconColor: '#8a5f2d',
          pagePath: '/pages/kyzz/favorite/index'
        },
        {
          key: 'wrong-book',
          title: '错题库',
          description: '错因复盘',
          icon: 'help',
          iconColor: '#9a4f4b',
          pagePath: '/pages/kyzz/wrong-book/index'
        },
        {
          key: 'random',
          title: '随机一题',
          description: '全库抽题',
          icon: 'reload',
          iconColor: '#315f42',
        },
        {
          key: 'leaderboard',
          title: '排行榜',
          description: '阶段坐标',
          icon: 'medal-filled',
          iconColor: '#345176',
          pagePath: '/pages/kyzz/leaderboard/index'
        }
      ]
    }
  },
  computed: {
    isInitialLoading(): boolean {
      return this.loading && !this.loadedOnce
    },
    recommendedBank(): KyzzPracticeBankViewRecord | null {
      return this.dashboard.records.find((item) => item.bankId === this.dashboard.recommendedBankId) || this.dashboard.records[0] || null
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
    },
    progressStyle(): string {
      if (!this.recommendedBank) return '--progress: 0%;'
      const rawProgress = this.recommendedBank.currentProgress || 0
      // 确保进度传给 CSS 变量，作为圆锥渐变的百分比界限
      return `--progress: ${rawProgress}%;`
    }
  },
  onShow() {
    this.bootstrapAndLoad()
    this.startTypewriter()
  },
  onHide() {
    this.clearTypewriter()
  },
  unmounted() {
    this.clearTypewriter()
  },
  methods: {
    async bootstrapAndLoad(): Promise<void> {
      if (this.loading) {
        return
      }
      const cachedDashboard = getCachedPracticeDashboard()
      if (cachedDashboard) {
        this.dashboard = normalizePracticeDashboard(cachedDashboard)
        this.loadedOnce = true
      }
      this.loading = true
      try {
        await bootstrapAuth({ silent: true })
        const result = await preloadPracticeDashboard({ force: this.loadedOnce })
        this.dashboard = normalizePracticeDashboard(result)
        this.loadedOnce = true
        warmKyzzCorePreload()
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
    startTypewriter() {
      this.clearTypewriter()
      this.displayedMotto = ''
      this.isTyping = true
      let index = 0

      this.typewriterTimer = setInterval(() => {
        if (index < this.fullMotto.length) {
          this.displayedMotto += this.fullMotto.charAt(index)
          index++
        } else {
          this.isTyping = false
          this.clearTypewriter()
        }
      }, 150)
    },
    clearTypewriter() {
      if (this.typewriterTimer) {
        clearInterval(this.typewriterTimer)
        this.typewriterTimer = null
      }
    },
    formatProgress,
    handleStartPractice(): void {
      if (!this.recommendedBank) {
        this.goPublicBanks()
        return
      }
      openBankPracticeTab({ bankId: this.recommendedBank.bankId }).catch(() => {})
    },
    openShortcut(item: StudyShortcutItem): void {
      if (item.key === 'random') {
        openPracticeTab({
          sourceType: 'random',
          freshAttempt: true
        }).catch(() => {
          uni.showToast({
            title: '跳转刷题失败',
            icon: 'none'
          })
        })
        return
      }
      if (!item.pagePath) {
        return
      }
      uni.navigateTo({
        url: item.pagePath
      })
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
  position: relative;
  background: linear-gradient(180deg, #f7f9fc 0%, #eef3f8 52%, #e9eff6 100%);
  box-sizing: border-box;
  overflow: hidden;
}

/* 高性能环境背景光晕 */
.study-page__ambient-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.study-page__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.22;
  will-change: transform, opacity;
}

.study-page__glow--1 {
  top: -10%;
  left: -20%;
  width: 600rpx;
  height: 600rpx;
  background: rgba(149, 168, 197, 0.2);
  animation: float-ambient-1 12s ease-in-out infinite alternate;
}

.study-page__glow--2 {
  top: 20%;
  right: -10%;
  width: 500rpx;
  height: 500rpx;
  background: rgba(188, 205, 226, 0.18);
  animation: float-ambient-2 15s ease-in-out infinite alternate;
}

.study-page__inner {
  position: relative;
  padding-top: 96rpx;
  z-index: 1;
}

.study-page__motto {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 80rpx;
}

.study-page__motto-text {
  position: relative;
  font-size: 30rpx;
  line-height: 1.5;
  font-weight: 600;
  font-style: italic;
  color: #5a6678;
}

.study-page__cursor {
  display: inline-block;
  width: 4rpx;
  height: 28rpx;
  background-color: #5a6678;
  margin-left: 6rpx;
  vertical-align: middle;
  transform: translateY(-2rpx);
  animation: cursor-blink 1s step-end infinite;
}
.study-page__cursor.is-typing {
  animation: none;
  opacity: 1;
}

.study-page__motto-line {
  margin-top: 24rpx;
  width: 182rpx;
  height: 2rpx;
  background: #c9d2dc;
}

.study-page__frame,
.study-page__diamond {
  position: absolute;
  pointer-events: none;
  will-change: transform;
}

.study-page__frame {
  top: 132rpx;
  width: 260rpx;
  height: 260rpx;
  border-top: 2rpx solid rgba(174, 187, 205, 0.32);
}

.study-page__frame--left {
  left: -92rpx;
  transform: skewY(-35deg) translateZ(0);
  animation: float-left 6s ease-in-out infinite;
}

.study-page__frame--right {
  right: -92rpx;
  transform: skewY(35deg) translateZ(0);
  animation: float-right 7s ease-in-out infinite;
}

.study-page__diamond {
  left: 50%;
  top: 248rpx;
  width: 520rpx;
  height: 520rpx;
  border: 2rpx solid rgba(174, 187, 205, 0.22);
  transform: translateX(-50%) rotate(45deg) translateZ(0);
  animation: spin-slow 40s linear infinite;
}

.study-page__hero-shell {
  position: relative;
  z-index: 2;
  width: 632rpx;
  height: 632rpx;
  max-width: calc(100% - 88rpx);
  margin: 64rpx auto 0;
  padding: 12rpx;
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow:
      0 16rpx 34rpx rgba(45, 58, 77, 0.09),
      inset 0 0 0 2rpx #d4deea;
  box-sizing: border-box;
}

.study-page__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 52rpx 28rpx 48rpx;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: linear-gradient(180deg, #263142 0%, #334056 100%);
  box-shadow:
      inset 0 0 0 2rpx rgba(255, 255, 255, 0.05),
      0 10rpx 22rpx rgba(49, 58, 76, 0.16);
}

.study-page__hero-en {
  font-size: 24rpx;
  line-height: 1.4;
  letter-spacing: 0;
  color: rgba(226, 232, 240, 0.68);
}

.study-page__hero-title {
  margin-top: 26rpx;
  font-size: 62rpx;
  line-height: 1.18;
  font-weight: 700;
  letter-spacing: 0;
  color: #ffffff;
}

.study-page__hero-divider {
  margin: 32rpx 0 34rpx;
  width: 90rpx;
  height: 4rpx;
  border-radius: 999rpx;
  background: rgba(201, 211, 225, 0.76);
}

.study-page__hero-bank {
  display: block;
  max-width: 100%;
  font-size: 24rpx;
  line-height: 1.55;
  font-weight: 600;
  color: rgba(241, 245, 249, 0.9);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.study-page__hero-skeleton-mark,
.study-page__hero-skeleton-title,
.study-page__hero-skeleton-divider,
.study-page__hero-skeleton-text,
.study-page__hero-skeleton-button,
.study-page__shortcut-title-skeleton {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.28) 50%, rgba(255, 255, 255, 0.12) 100%);
  background-size: 200% 100%;
}

.study-page__hero-skeleton-mark {
  width: 220rpx;
  height: 24rpx;
  border-radius: 999rpx;
}

.study-page__hero-skeleton-title {
  width: 330rpx;
  height: 72rpx;
  margin-top: 28rpx;
  border-radius: 24rpx;
}

.study-page__hero-skeleton-divider {
  width: 96rpx;
  height: 6rpx;
  margin: 34rpx 0 36rpx;
  border-radius: 999rpx;
}

.study-page__hero-skeleton-text {
  width: 320rpx;
  height: 28rpx;
  border-radius: 999rpx;
}

.study-page__hero-skeleton-button {
  width: 250rpx;
  height: 88rpx;
  margin-top: 34rpx;
  border-radius: 999rpx;
}

/* 进度环绕脉冲按钮样式 ==================== */
.study-page__hero-action {
  margin-top: 28rpx;
  position: relative;
  display: flex;
  justify-content: center;
}

.study-page__hero-button-wrapper {
  position: relative;
  margin: 0;
  padding: 4rpx; /* 控制边框进度条的厚度 */
  border-radius: 99rpx;
  background: rgba(255, 255, 255, 0.95);
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 14rpx 28rpx rgba(19, 26, 41, 0.18);
}

.study-page__hero-button-wrapper::after {
  border: none; /* 去除uniapp自带的button边框 */
}

.study-page__hero-button-wrapper:active {
  transform: scale(0.95);
}

/* 带有题库进度时的动态渐变边框和脉冲 */
.study-page__hero-button-wrapper.is-progress-active {
  /* 利用圆锥渐变形成从顶部 12 点钟顺时针绘制的进度条 */
  background: conic-gradient(
          from 0deg,
          #8fb79a 0%,
          #4f7f58 var(--progress),
          rgba(255, 255, 255, 0.18) var(--progress)
  );
  /* 呼吸脉冲光晕动效 */
  animation: button-pulse-glow 2.5s infinite cubic-bezier(0.4, 0, 0.2, 1);
}

/* 按钮内部白底区域遮盖掉中心渐变，仅露出边框 */
.study-page__hero-button-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  height: 80rpx;
  padding: 0 36rpx;
  border-radius: 99rpx;
  background: #ffffff; /* 内部底色 */
}

.study-page__hero-btn-text {
  color: #313b4d;
  font-size: 26rpx;
  font-weight: 700;
}

.study-page__hero-btn-pct {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 4rpx 14rpx;
  background: rgba(239, 246, 255, 0.8);
  border-radius: 99rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #3f754d;
}

/* 百分比前的灵动小圆点 */
.study-page__hero-btn-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #4f7f58;
  animation: dot-pulse 1.5s infinite alternate;
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
  background: linear-gradient(180deg, #465a7e 0%, #657896 100%);
  box-shadow: 0 10rpx 20rpx rgba(54, 75, 111, 0.22);
  transform: translateX(14rpx);
}

.study-page__streak.is-skeleton {
  background: linear-gradient(180deg, rgba(70, 90, 126, 0.92) 0%, rgba(101, 120, 150, 0.92) 100%);
}

.study-page__streak-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  animation: dot-pulse 1.4s infinite alternate;
}

.study-page__streak-text {
  font-size: 22rpx;
  line-height: 1;
  font-weight: 700;
  letter-spacing: 0;
  color: #ffffff;
}

.study-page__shortcut-shell {
  position: relative;
  z-index: 2;
  margin: 40rpx 18rpx 0;
}

.study-page__shortcut-title {
  font-size: 30rpx;
  line-height: 1.2;
  font-weight: 700;
  color: #243146;
}

.study-page__shortcut-title-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.study-page__shortcut-title-mark {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
}

.study-page__shortcut-title-bar {
  width: 10rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #465a7e 0%, #8ba0bf 100%);
}

.study-page__shortcut-title-bar--left {
  height: 26rpx;
}

.study-page__shortcut-title-bar--right {
  height: 20rpx;
}

.study-page__shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.study-page__shortcut-item {
  min-width: 0;
}

.study-page__shortcut-card {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  min-height: 136rpx;
  padding: 26rpx 58rpx 26rpx 22rpx;
  border-radius: 30rpx;
  overflow: hidden;
  box-sizing: border-box;
  background: #ffffff;
  box-shadow:
    0 14rpx 30rpx rgba(45, 58, 77, 0.055),
    inset 0 0 0 1rpx #d6deea;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.study-page__shortcut-card:active {
  transform: scale(0.975) translateY(2rpx);
  box-shadow:
    0 10rpx 26rpx rgba(43, 52, 55, 0.06),
    inset 0 0 0 1rpx #cbd5e4;
}

.study-page__shortcut-card-glow {
  position: absolute;
  top: -54rpx;
  right: -48rpx;
  width: 146rpx;
  height: 146rpx;
  border-radius: 999rpx;
  background: radial-gradient(circle, rgba(232, 240, 255, 0.48) 0%, rgba(232, 240, 255, 0) 68%);
  pointer-events: none;
}

.study-page__shortcut-card--skeleton {
  background-color: #ffffff;
  box-shadow:
    0 14rpx 30rpx rgba(45, 58, 77, 0.045),
    inset 0 0 0 1rpx #d6deea;
}

.study-page__shortcut-icon {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  flex: 0 0 58rpx;
  border-radius: 20rpx;
  background: #f7f9fc;
  box-shadow:
    0 8rpx 18rpx rgba(54, 68, 90, 0.06),
    inset 0 0 0 1rpx #cbd5e4;
}

.study-page__shortcut-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: 18rpx;
}

.study-page__shortcut-card-title {
  display: block;
  font-size: 26rpx;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: 0;
  color: #273247;
}

.study-page__shortcut-card-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  line-height: 1.25;
  font-weight: 500;
  color: #5f6d7f;
  white-space: nowrap;
}

.study-page__shortcut-arrow {
  position: absolute;
  right: 20rpx;
  top: 50%;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32rpx;
  height: 32rpx;
  border-radius: 999rpx;
  background: #f7f9fc;
  transform: translateY(-50%);
  box-shadow: inset 0 0 0 1rpx #cbd5e4;
}

.study-page__shortcut-title-skeleton {
  width: 84rpx;
  height: 24rpx;
  margin: 14rpx auto 0;
  border-radius: 999rpx;
}

.study-page__skeleton-shimmer {
  animation: study-skeleton-shimmer 1.4s ease-in-out infinite;
}

/* =======================================
   Keyframes: 高性能动画 (硬件加速)
   ======================================= */
@keyframes cursor-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes float-ambient-1 {
  0% { transform: translate3d(0, 0, 0) scale(1); }
  100% { transform: translate3d(80rpx, 40rpx, 0) scale(1.1); }
}

@keyframes float-ambient-2 {
  0% { transform: translate3d(0, 0, 0) scale(1); opacity: 0.18; }
  100% { transform: translate3d(-60rpx, -40rpx, 0) scale(1.12); opacity: 0.26; }
}

@keyframes float-left {
  0%, 100% { transform: skewY(-35deg) translateY(0) translateZ(0); }
  50% { transform: skewY(-35deg) translateY(-20rpx) translateZ(0); }
}

@keyframes float-right {
  0%, 100% { transform: skewY(35deg) translateY(0) translateZ(0); }
  50% { transform: skewY(35deg) translateY(-20rpx) translateZ(0); }
}

@keyframes spin-slow {
  0% { transform: translateX(-50%) rotate(45deg) translateZ(0); }
  100% { transform: translateX(-50%) rotate(405deg) translateZ(0); }
}

/* 按钮环绕边框的脉冲发光 */
@keyframes button-pulse-glow {
  0% { box-shadow: 0 0 0 0 rgba(79, 127, 88, 0.28); }
  70% { box-shadow: 0 0 0 16rpx rgba(79, 127, 88, 0); }
  100% { box-shadow: 0 0 0 0 rgba(79, 127, 88, 0); }
}

@keyframes dot-pulse {
  0% { opacity: 0.4; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1.2); }
}

@keyframes study-skeleton-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media screen and (max-width: 375px) {
  .study-page__hero-shell {
    width: 596rpx;
    height: 596rpx;
    max-width: calc(100% - 92rpx);
  }

  .study-page__hero {
    padding-left: 24rpx;
    padding-right: 24rpx;
  }

  .study-page__hero-title {
    font-size: 56rpx;
  }

  .study-page__hero-bank {
    font-size: 22rpx;
  }
}
</style>
