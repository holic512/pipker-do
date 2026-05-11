<template>
	<!-- AI 索引: KYYY 练习入口网关。 -->
	<page-shell
		root-class="kyyy-practice-gateway theme-page"
		content-style="padding: 0 24rpx 40rpx;"
	>
		<view class="kyyy-practice-gateway__card">
			<text class="kyyy-practice-gateway__eyebrow">{{ eyebrow }}</text>
			<text class="kyyy-practice-gateway__title">{{ title }}</text>
			<text class="kyyy-practice-gateway__desc">正在进入词库选择页</text>
		</view>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import { bootstrapAuth } from '@/shared/session/session'
import type { KyyyPracticeEntryMode } from '@/pages/kyyy/home/types'
import type { KyyyWordBankEntryMode } from '@/pages/kyyy/word-bank/types'

function normalizeEntryMode(value: unknown): KyyyWordBankEntryMode {
	return value === 'study' || value === 'review' ? value : 'default'
}

function resolveTargetUrl(mode: KyyyWordBankEntryMode): string {
	return mode === 'default'
		? '/pages/kyyy/word-bank/index'
		: `/pages/kyyy/word-bank/index?mode=${mode}`
}

export default defineComponent({
	name: 'KyyyPracticePage',
	components: {
		PageShell
	},
	data() {
		return {
			entryMode: 'default' as KyyyPracticeEntryMode
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.entryMode = normalizeEntryMode(query?.mode)
	},
	onShow() {
		this.routeToWordBank().catch((error) => {
			console.warn('[kyyy-practice] route failed', error)
		})
	},
	computed: {
		eyebrow(): string {
			if (this.entryMode === 'study') {
				return 'STUDY'
			}
			if (this.entryMode === 'review') {
				return 'REVIEW'
			}
			return 'PRACTICE'
		},
		title(): string {
			if (this.entryMode === 'study') {
				return '学习词库'
			}
			if (this.entryMode === 'review') {
				return '复习词库'
			}
			return '词库'
		}
	},
	methods: {
		async routeToWordBank(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-practice] bootstrap auth failed', error)
			}
			uni.reLaunch({
				url: resolveTargetUrl(this.entryMode),
				fail: (error: unknown) => {
					console.warn('[kyyy-practice] reLaunch failed', error)
				}
			})
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-practice-gateway__card {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	padding: 36rpx 32rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow:
		0 18rpx 38rpx rgba(61, 75, 93, 0.08),
		inset 0 0 0 1rpx rgba(222, 229, 238, 0.88);
}

.kyyy-practice-gateway__eyebrow {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.2em;
	color: #7a8697;
}

.kyyy-practice-gateway__title {
	font-size: 42rpx;
	line-height: 1.22;
	font-weight: 760;
	color: #243041;
}

.kyyy-practice-gateway__desc {
	font-size: 24rpx;
	line-height: 1.5;
	color: #6b7789;
}
</style>
