<!--
@file KyyyTranslationDetailPage
@project pipker-do
@module 考研英语 / 小程序翻译知识库
@description 展示单题翻译原文、题目说明、分段译文与参考译文详情。
@logic 1. 按 passageId 拉取详情；2. 分段展示原文与中文译文；3. 展示整题参考译文与备注。
@dependencies API: @/pages/kyyy/api/translation, Types: @/pages/kyyy/translation/types, View: @/pages/kyyy/translation/view
@index_tags 考研英语, 翻译详情, 知识库, 分段译文, 真题
@author holic512
-->
<template>
	<view class="kyyy-translation-detail">
		<view v-if="loading && !detailState" class="kyyy-translation-detail__state">
			<text>正在加载翻译详情...</text>
		</view>
		<view v-else-if="detailState" class="kyyy-translation-detail__content">
			<view class="kyyy-translation-detail__hero">
				<text class="kyyy-translation-detail__title">{{ detailState.sourceTitle }}</text>
				<text class="kyyy-translation-detail__meta">{{ buildTranslationMetaText(detailState) }}</text>
				<view v-if="detailState.knowledgeTags.length" class="kyyy-translation-detail__tag-row">
					<text
						v-for="tag in detailState.knowledgeTags"
						:key="`${detailState.id}-${tag}`"
						class="kyyy-translation-detail__tag"
					>
						{{ tag }}
					</text>
				</view>
			</view>

			<view v-if="detailState.promptInstruction" class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">题目</text>
				<text class="kyyy-translation-detail__block-text">{{ detailState.promptInstruction }}</text>
			</view>

			<view class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">原文</text>
				<text
					v-for="(item, index) in promptParagraphs"
					:key="`prompt-${index}`"
					class="kyyy-translation-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>

			<view v-if="promptTranslationParagraphs.length" class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">题目翻译</text>
				<text
					v-for="(item, index) in promptTranslationParagraphs"
					:key="`prompt-translation-${index}`"
					class="kyyy-translation-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>

			<view v-if="detailState.segments.length" class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">分段译文</text>
				<view
					v-for="item in detailState.segments"
					:key="`${detailState.id}-${item.segmentNo}`"
					class="kyyy-translation-detail__segment-card"
				>
					<text class="kyyy-translation-detail__segment-no">{{ item.segmentNo }}</text>
					<text class="kyyy-translation-detail__segment-source">{{ item.sourceText }}</text>
					<text class="kyyy-translation-detail__segment-translation">{{ item.translatedText }}</text>
				</view>
			</view>

			<view v-if="referenceTranslationParagraphs.length" class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">参考译文</text>
				<text
					v-for="(item, index) in referenceTranslationParagraphs"
					:key="`reference-${index}`"
					class="kyyy-translation-detail__paragraph"
				>
					{{ item }}
				</text>
			</view>

			<view v-if="detailState.referenceNote" class="kyyy-translation-detail__section">
				<text class="kyyy-translation-detail__section-title">备注</text>
				<text class="kyyy-translation-detail__block-text">{{ detailState.referenceNote }}</text>
			</view>
		</view>
		<view v-else class="kyyy-translation-detail__state">
			<text>翻译内容不存在或暂时不可查看</text>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { bootstrapAuth } from '@/shared/session/session'
import { getTranslationDetail } from '@/pages/kyyy/api/translation'
import type { KyyyTranslationDetailState } from '@/pages/kyyy/translation/types'
import {
	buildTranslationMetaText,
	normalizeTranslationDetail,
	resolveExamDirectionLabel,
	resolveTranslationModeLabel,
	splitParagraphs
} from '@/pages/kyyy/translation/view'

interface TranslationDetailPageState {
	passageId: number
	loading: boolean
	detailState: KyyyTranslationDetailState | null
}

function toPassageId(value: unknown): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? parsed : 0
}

export default defineComponent({
	name: 'KyyyTranslationDetailPage',
	data(): TranslationDetailPageState {
		return {
			passageId: 0,
			loading: false,
			detailState: null
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.passageId = toPassageId(query?.passageId)
	},
	onShow() {
		this.bootstrapAndLoad()
	},
	computed: {
		promptParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.promptContent) : []
		},
		promptTranslationParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.promptTranslation) : []
		},
		referenceTranslationParagraphs(): string[] {
			return this.detailState ? splitParagraphs(this.detailState.referenceTranslation) : []
		}
	},
	methods: {
		resolveExamDirectionLabel,
		resolveTranslationModeLabel,
		buildTranslationMetaText,
		async bootstrapAndLoad(): Promise<void> {
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-translation-detail] bootstrap auth failed', error)
			}
			await this.loadDetail()
		},
		async loadDetail(): Promise<void> {
			if (!this.passageId || this.loading) {
				return
			}
			this.loading = true
			try {
				this.detailState = normalizeTranslationDetail(await getTranslationDetail(this.passageId))
			} catch (error) {
				console.warn('[kyyy-translation-detail] load detail failed', error)
				this.detailState = null
				uni.showToast({
					title: '翻译详情加载失败',
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
.kyyy-translation-detail {
	min-height: 100vh;
	padding: 24rpx;
	background: linear-gradient(180deg, #f5f7fa 0%, #edf1f6 100%);
}

.kyyy-translation-detail__content {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.kyyy-translation-detail__hero,
.kyyy-translation-detail__section,
.kyyy-translation-detail__state,
.kyyy-translation-detail__segment-card {
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	box-shadow: 0 18rpx 40rpx rgba(43, 52, 55, 0.08);
}

.kyyy-translation-detail__hero {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 32rpx;
	background: linear-gradient(140deg, #556070 0%, #7d8c9f 100%);
}

.kyyy-translation-detail__tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
}

.kyyy-translation-detail__pill,
.kyyy-translation-detail__tag {
	padding: 8rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.14);
	font-size: 20rpx;
	color: #f4f7ff;
}

.kyyy-translation-detail__title {
	font-size: 36rpx;
	line-height: 1.34;
	font-weight: 700;
	color: #ffffff;
}

.kyyy-translation-detail__meta {
	font-size: 24rpx;
	line-height: 1.6;
	color: rgba(241, 245, 252, 0.88);
}

.kyyy-translation-detail__section {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	padding: 28rpx;
}

.kyyy-translation-detail__section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #24303a;
}

.kyyy-translation-detail__block-text,
.kyyy-translation-detail__paragraph,
.kyyy-translation-detail__segment-source,
.kyyy-translation-detail__segment-translation {
	font-size: 26rpx;
	line-height: 1.82;
	color: #33404a;
	white-space: pre-wrap;
}

.kyyy-translation-detail__segment-card {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 24rpx;
	background: linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%);
}

.kyyy-translation-detail__segment-no {
	font-size: 24rpx;
	font-weight: 700;
	color: #506171;
}

.kyyy-translation-detail__segment-source {
	color: #24313c;
}

.kyyy-translation-detail__segment-translation {
	color: #5b6774;
}

.kyyy-translation-detail__state {
	padding: 32rpx;
	text-align: center;
	font-size: 24rpx;
	color: #687480;
}
</style>
