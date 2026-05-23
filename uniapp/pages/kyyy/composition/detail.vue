<!--
@file KyyyCompositionDetailPage
@project pipker-do
@module 考研英语 / 小程序作文知识库
@description 展示单篇作文题目、范文与中英翻译详情，承接列表页和首页推荐入口。
@logic 1. 按 essayId 拉取详情；2. 分段展示题目与范文长文本；3. 保留标签和来源等辅助信息。
@dependencies API: @/pages/kyyy/api/writing, Types: @/pages/kyyy/composition/types, View: @/pages/kyyy/composition/view
@index_tags 考研英语, 作文详情, 范文, 翻译, 真题
@author holic512
-->
<template>
	<view class="kyyy-composition-detail">
		<view v-if="loading && !detailState" class="kyyy-composition-detail__state">
			<text>正在加载作文详情...</text>
		</view>
		<view v-else-if="detailState" class="kyyy-composition-detail__content">
			<view class="kyyy-composition-detail__hero">
				<text class="kyyy-composition-detail__title">{{ detailState.sourceTitle }}</text>
				<text class="kyyy-composition-detail__meta">{{ buildEssayMetaText(detailState) }}</text>
				<view v-if="detailState.knowledgeTags.length" class="kyyy-composition-detail__tag-row">
					<text
						v-for="tag in detailState.knowledgeTags"
						:key="`${detailState.id}-${tag}`"
						class="kyyy-composition-detail__tag"
					>
						{{ tag }}
					</text>
				</view>
			</view>

			<view class="kyyy-composition-detail__section">
				<text class="kyyy-composition-detail__section-title">英文题目</text>
				<text class="kyyy-composition-detail__block-text">{{ detailState.promptContent }}</text>
			</view>

			<view v-if="promptTranslationParagraphs.length" class="kyyy-composition-detail__section">
				<text class="kyyy-composition-detail__section-title">中文题目翻译</text>
				<text
					v-for="(item, index) in promptTranslationParagraphs"
					:key="`prompt-translation-${index}`"
					class="kyyy-composition-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>

			<view class="kyyy-composition-detail__section">
				<text class="kyyy-composition-detail__section-title">英文范文</text>
				<text
					v-for="(item, index) in sampleParagraphs"
					:key="`sample-${index}`"
					class="kyyy-composition-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>

			<view v-if="sampleTranslationParagraphs.length" class="kyyy-composition-detail__section">
				<text class="kyyy-composition-detail__section-title">中文范文翻译</text>
				<text
					v-for="(item, index) in sampleTranslationParagraphs"
					:key="`sample-translation-${index}`"
					class="kyyy-composition-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>
		</view>
		<view v-else class="kyyy-composition-detail__state">
			<text>作文内容不存在或暂时不可查看</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getWritingEssayDetail } from '@/pages/kyyy/api/writing'
import type { KyyyWritingEssayDetailState } from '@/pages/kyyy/composition/types'
import {
	buildEssayMetaText,
	normalizeEssayDetail,
	resolveEssaySectionLabel,
	resolveExamDirectionLabel,
	resolvePromptCategoryLabel,
	splitParagraphs
} from '@/pages/kyyy/composition/view'

interface CompositionDetailPageState {
	essayId: number
	loading: boolean
	detailState: KyyyWritingEssayDetailState | null
}

function toEssayId(value: unknown): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? parsed : 0
}

export default defineComponent({
	name: 'KyyyCompositionDetailPage',
	data(): CompositionDetailPageState {
		return {
			essayId: 0,
			loading: false,
			detailState: null
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.essayId = toEssayId(query?.essayId)
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		promptTranslationParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.promptTranslation) : []
		},
		sampleParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.sampleContent) : []
		},
		sampleTranslationParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.sampleTranslation) : []
		}
	},
	methods: {
		resolveExamDirectionLabel,
		resolveEssaySectionLabel,
		resolvePromptCategoryLabel,
		buildEssayMetaText,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-composition-detail] bootstrap auth failed', error)
			}
			await this.loadDetail()
		},
		async loadDetail(): Promise<void> {
			if (!this.essayId || this.loading) {
				return
			}
			this.loading = true
			try {
				this.detailState = normalizeEssayDetail(await getWritingEssayDetail(this.essayId))
			} catch (error) {
				console.warn('[kyyy-composition-detail] load detail failed', error)
				this.detailState = null
				uni.showToast({
					title: '作文详情加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-composition-detail {
	min-height: 100vh;
	padding: 24rpx;
	background: linear-gradient(180deg, #f5f7fa 0%, #edf1f6 100%);
}

.kyyy-composition-detail__content {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.kyyy-composition-detail__hero,
.kyyy-composition-detail__section,
.kyyy-composition-detail__state {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-composition-detail__hero {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	padding: 24rpx;
	background: linear-gradient(140deg, #5a647c 0%, #8292ad 100%);
}

.kyyy-composition-detail__tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
}

.kyyy-composition-detail__pill,
.kyyy-composition-detail__tag {
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.14);
	font-size: 20rpx;
	color: #f4f7ff;
}

.kyyy-composition-detail__title {
	font-size: 36rpx;
	line-height: 1.34;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-composition-detail__meta {
	font-size: 24rpx;
	line-height: 1.6;
	color: rgba(241, 245, 252, 0.88);
}

.kyyy-composition-detail__section {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 22rpx;
}

.kyyy-composition-detail__section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #24303a;
}

.kyyy-composition-detail__block-text,
.kyyy-composition-detail__paragraph {
	font-size: 26rpx;
	line-height: 1.82;
	color: #33404a;
	white-space: pre-wrap;
}

.kyyy-composition-detail__state {
	padding: 32rpx;
	text-align: center;
	font-size: 24rpx;
	color: #687480;
}
</style>
