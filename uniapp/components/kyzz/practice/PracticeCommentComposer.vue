<template>
	<view class="practice-comment-composer">
		<view class="practice-comment-composer__head">
			<text class="practice-comment-composer__title">说说你的思路</text>
			<text class="practice-comment-composer__count">{{ currentLength }}/300</text>
		</view>

		<textarea
			:value="modelValue"
			class="practice-comment-composer__input"
			placeholder="这道题你是怎么理解的？说说你的思路"
			maxlength="300"
			:auto-height="true"
			:disabled="submitting"
			@input="handleInput"
		/>

		<view class="practice-comment-composer__footer">
			<text class="practice-comment-composer__hint">先把自己的理解写下来，会更容易留下有价值的评论。</text>
			<button
				class="practice-comment-composer__submit"
				:class="{ 'is-disabled': submitDisabled }"
				:disabled="submitDisabled"
				@tap="$emit('submit')"
			>
				{{ submitting ? '发布中...' : '发布评论' }}
			</button>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

// AI 索引: KYZZ 刷题页评论发布输入组件。

export default defineComponent({
	name: 'PracticeCommentComposer',
	props: {
		modelValue: {
			type: String,
			default: ''
		},
		submitting: {
			type: Boolean,
			default: false
		}
	},
	emits: ['update:modelValue', 'submit'],
	computed: {
		currentLength(): number {
			return this.modelValue.trim().length
		},
		submitDisabled(): boolean {
			return this.submitting || this.currentLength <= 0
		}
	},
	methods: {
		handleInput(event: { detail?: { value?: string } }): void {
			this.$emit('update:modelValue', event?.detail?.value || '')
		}
	}
})
</script>

<style lang="scss" scoped>
.practice-comment-composer {
	margin-top: 18rpx;
	padding: 26rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.97);
	box-shadow: 0 18rpx 36rpx rgba(50, 60, 72, 0.05);
	border: 1rpx solid rgba(217, 225, 236, 0.92);
}

.practice-comment-composer__head,
.practice-comment-composer__footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
}

.practice-comment-composer__title {
	font-size: 28rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #25303d;
}

.practice-comment-composer__count {
	font-size: 22rpx;
	line-height: 1;
	color: #7b8798;
}

.practice-comment-composer__input {
	width: 100%;
	min-height: 152rpx;
	margin-top: 18rpx;
	padding: 20rpx 22rpx;
	box-sizing: border-box;
	border-radius: 24rpx;
	background: rgba(244, 247, 250, 0.96);
	box-shadow: inset 0 0 0 1rpx rgba(218, 226, 236, 0.92);
	font-size: 26rpx;
	line-height: 1.7;
	color: #2d3743;
}

.practice-comment-composer__hint {
	flex: 1;
	min-width: 0;
	margin-top: 18rpx;
	font-size: 22rpx;
	line-height: 1.6;
	color: #798493;
}

.practice-comment-composer__submit {
	margin: 18rpx 0 0;
	padding: 0 26rpx;
	height: 68rpx;
	line-height: 68rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, rgba(84, 94, 118, 0.96) 0%, rgba(120, 132, 162, 0.94) 100%);
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
	box-shadow: 0 16rpx 28rpx rgba(84, 94, 118, 0.16);
	flex-shrink: 0;
}

.practice-comment-composer__submit.is-disabled {
	background: rgba(197, 205, 216, 0.96);
	box-shadow: none;
	color: rgba(255, 255, 255, 0.9);
}

.practice-comment-composer__submit::after {
	border: 0;
}
</style>
