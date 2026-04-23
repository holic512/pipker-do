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
          <text class="study-page__hero-en">ACADEMIC QUEST</text>
          <text class="study-page__hero-title">{{ heroTitle }}</text>
          <view class="study-page__hero-divider"></view>
          <text class="study-page__hero-bank">{{ heroBankName }}</text>

          <view v-if="recommendedBank" class="study-page__hero-chips">
            <text class="study-page__hero-chip">{{ recommendedBank.resumeLabel }}</text>
            <text class="study-page__hero-chip">{{ difficultyLabel(recommendedBank.difficultyLevel) }}</text>
            <!-- 移除了原有的百分比文本，融合到底部按钮中 -->
          </view>

          <text class="study-page__hero-desc">{{ heroDescription }}</text>

          <!-- 优化: 环绕进度边框 + 脉冲动效按钮 -->
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
        </view>

        <view class="study-page__streak">
          <uni-icons type="fire-filled" size="14" color="#ffffff" />
          <text class="study-page__streak-text">{{ studyDaysLabel }}</text>
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
          <view
              v-for="item in shortcutItems"
              :key="item.key"
              class="study-page__shortcut-item"
              @tap="openShortcut(item.pagePath)"
          >
            <view class="study-page__shortcut-card">
              <view class="study-page__shortcut-icon">
                <uni-icons :type="item.icon" size="29" :color="item.iconColor" />
              </view>
            </view>
            <text class="study-page__shortcut-card-title">{{ item.title }}</text>
          </view>
        </view>
      </view>

      <view v-if="loadedOnce && !loading && !dashboard.records.length" class="study-page__empty">
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
import { createEmptyPracticeDashboard, difficultyLabel, formatProgress, normalizePracticeDashboard } from '@/pages/kyzz/practice/view'

interface StudyShortcutItem {
  key: string
  title: string
  icon: string
  iconColor: string
  pagePath: string
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
          icon: 'star-filled',
          iconColor: '#6f7890',
          pagePath: '/pages/kyzz/favorite/index'
        },
        {
          key: 'wrong-book',
          title: '错题库',
          icon: 'help',
          iconColor: '#6f7890',
          pagePath: '/pages/kyzz/wrong-book/index'
        },
        {
          key: 'note',
          title: '笔记',
          icon: 'compose',
          iconColor: '#6f7890',
          pagePath: '/pages/kyzz/note/index'
        },
        {
          key: 'leaderboard',
          title: '排行榜',
          icon: 'medal-filled',
          iconColor: '#6f7890',
          pagePath: '/pages/kyzz/leaderboard/index'
        }
      ]
    }
  },
  computed: {
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
    difficultyLabel,
    formatProgress,
    handleStartPractice(): void {
      if (!this.recommendedBank) {
        this.goPublicBanks()
        return
      }
      openPracticeTab().catch(() => {})
    },
    openShortcut(pagePath: string): void {
      uni.navigateTo({
        url: pagePath
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
  background:
      radial-gradient(circle at top, rgba(255, 255, 255, 0.98) 0%, rgba(244, 247, 252, 0.94) 48%, rgba(236, 241, 247, 0.92) 100%);
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
  opacity: 0.6;
  will-change: transform, opacity;
}

.study-page__glow--1 {
  top: -10%;
  left: -20%;
  width: 600rpx;
  height: 600rpx;
  background: rgba(186, 212, 255, 0.3);
  animation: float-ambient-1 12s ease-in-out infinite alternate;
}

.study-page__glow--2 {
  top: 20%;
  right: -10%;
  width: 500rpx;
  height: 500rpx;
  background: rgba(220, 230, 250, 0.4);
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
  letter-spacing: 0.08em;
  color: rgba(120, 127, 142, 0.88);
}

.study-page__cursor {
  display: inline-block;
  width: 4rpx;
  height: 28rpx;
  background-color: rgba(120, 127, 142, 0.88);
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
  background: rgba(178, 186, 201, 0.9);
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
  border-top: 2rpx solid rgba(197, 206, 220, 0.48);
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
  border: 2rpx solid rgba(202, 211, 224, 0.32);
  transform: translateX(-50%) rotate(45deg) translateZ(0);
  animation: spin-slow 40s linear infinite;
}

.study-page__hero-shell {
  position: relative;
  z-index: 2;
  margin: 64rpx 34rpx 0;
  width: auto;
  padding: 12rpx;
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
  padding: 68rpx 30rpx 64rpx;
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

/* 进度环绕脉冲按钮样式 ==================== */
.study-page__hero-action {
  margin-top: 36rpx;
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
          #64b5f6 0%,
          #1e88e5 var(--progress),
          rgba(255, 255, 255, 0.15) var(--progress)
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
  color: #1e88e5;
}

/* 百分比前的灵动小圆点 */
.study-page__hero-btn-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #64b5f6;
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

.study-page__shortcut-shell {
  position: relative;
  z-index: 2;
  margin: 28rpx 18rpx 0;
}

.study-page__empty {
  position: relative;
  z-index: 2;
  margin-top: 24rpx;
  padding: 28rpx 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.05);
}

.study-page__shortcut-title {
  font-size: 30rpx;
  line-height: 1.2;
  font-weight: 700;
  color: #203153;
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
  background: linear-gradient(180deg, #3c86ff 0%, #6ba6ff 100%);
}

.study-page__shortcut-title-bar--left {
  height: 26rpx;
}

.study-page__shortcut-title-bar--right {
  height: 20rpx;
}

.study-page__shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 22rpx;
}

.study-page__shortcut-item {
  min-width: 0;
}

.study-page__shortcut-card {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding-top: 100%;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.9);
  box-shadow:
    0 16rpx 34rpx rgba(43, 52, 55, 0.06),
    inset 0 0 0 1rpx rgba(232, 237, 244, 0.9);
}

.study-page__shortcut-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.study-page__shortcut-card-title {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
  font-weight: 600;
  text-align: center;
  color: #5c6678;
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
  0% { transform: translate3d(0, 0, 0) scale(1); opacity: 0.4; }
  100% { transform: translate3d(-60rpx, -40rpx, 0) scale(1.2); opacity: 0.7; }
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
  0% { box-shadow: 0 0 0 0 rgba(100, 181, 246, 0.4); }
  70% { box-shadow: 0 0 0 16rpx rgba(100, 181, 246, 0); }
  100% { box-shadow: 0 0 0 0 rgba(100, 181, 246, 0); }
}

@keyframes dot-pulse {
  0% { opacity: 0.4; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1.2); }
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
