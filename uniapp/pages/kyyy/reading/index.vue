<!--
@file KyyyReadingPage
@project pipker-do
@module 考研英语 / 小程序阅读做题
@description 提供单篇阅读全文展示、整页作答、单题存稿、交卷回显解析以及正文/题干标注能力。
@logic 1. 进入页面后恢复或创建阅读会话；2. 整篇加载全部题目并单题即存；3. 交卷后同页回显对错、标准答案与解析；4. 通过词级选区模型完成正文和题干高亮与解释。
@dependencies API: @/pages/kyyy/api/reading, View: @/pages/kyyy/reading/view, Util: @/pages/kyyy/reading/annotation, Component: PageShell, Component: KyyyTabbar, Shared: @/shared/session/session
@index_tags 考研英语, 阅读做题, 阅读标注, 解释弹窗, 整篇阅读
@author holic512
-->
<template>
	<page-shell
		root-class="kyyy-reading-page theme-page"
		content-style="padding: 0 24rpx 196rpx;"
	>
		<view v-if="sessionState.loading && !sessionState.loaded" class="kyyy-reading-page__state-card">
			<view class="kyyy-reading-page__loader"></view>
			<text class="kyyy-reading-page__state-title">正在加载阅读</text>
			<text class="kyyy-reading-page__state-desc">会优先恢复你上次没交卷的那一篇。</text>
		</view>

		<view v-else-if="sessionState.status === 'empty' || !sessionState.passage" class="kyyy-reading-page__state-card">
			<text class="kyyy-reading-page__state-title">当前暂无可练习阅读</text>
			<text class="kyyy-reading-page__state-desc">请检查当前考试方向下是否已导入阅读题。</text>
			<view class="kyyy-reading-page__state-actions">
				<view class="kyyy-reading-page__action-button" @tap="reloadSession(false)">
					<text>重新加载</text>
				</view>
				<view class="kyyy-reading-page__action-button kyyy-reading-page__action-button--ghost" @tap="goHome">
					<text>返回首页</text>
				</view>
			</view>
		</view>

		<view v-else class="kyyy-reading-page__content">
			<reading-passage-card
				:passage-meta-text="passageMetaText"
				:title="sessionState.passage.title"
				:tokens="sessionState.passage.tokens"
				:token-class-resolver="resolvePassageTokenClass"
				@token-longpress="handlePassageTokenLongPress"
				@token-tap="handlePassageTokenTap"
			/>

			<reading-summary-card
				v-if="sessionState.status === 'submitted' && sessionState.summary"
				:correct-count="sessionState.summary.correctCount"
				:wrong-count="sessionState.summary.wrongCount"
				:accuracy-text="accuracyText"
			/>

			<reading-question-card
				v-for="(question, questionIndex) in sessionState.questions"
				:key="question.questionId"
				:question="question"
				:display-no="questionIndex + 1"
				:saving="isQuestionSaving(question.questionId)"
				:submitted="sessionState.status === 'submitted'"
				:highlighted="locatingQuestionId === question.questionId"
				:token-class-resolver="createQuestionTokenClassResolver(question.questionId)"
				:option-class-resolver="createQuestionOptionClassResolver(question)"
				@token-longpress="handleQuestionTokenLongPress"
				@token-tap="handleQuestionTokenTap"
				@select-option="handleQuestionSelectOption"
			/>

			<view v-if="sessionState.status === 'submitted'" class="kyyy-reading-page__result-actions">
				<view class="kyyy-reading-page__action-button" @tap="restartSession">
					<text>重新开始</text>
				</view>
				<view class="kyyy-reading-page__action-button kyyy-reading-page__action-button--ghost" @tap="goHome">
					<text>返回首页</text>
				</view>
			</view>
		</view>

		<reading-action-menu
			v-if="actionMenu.visible"
			:mode="actionMenu.mode"
			:menu-style="actionMenuStyle"
			@create-mark="handleCreateMark"
			@create-explanation="handleCreateExplanation"
			@cancel-selection="resetSelectionState"
			@remove-annotation="handleRemoveAnnotation"
			@view-explanation="handleViewExplanation"
		/>

		<view
			v-else-if="sessionState.status === 'active' && sessionState.sessionId"
			class="kyyy-reading-page__footer"
		>
			<view class="kyyy-reading-page__footer-copy">
				<text class="kyyy-reading-page__footer-title">{{ progressText }}</text>
			</view>
			<view
				class="kyyy-reading-page__submit-button"
				:class="{ 'is-disabled': sessionState.submitting || sessionState.loading }"
				@tap="handleSubmit"
			>
				<text>{{ sessionState.submitting ? '提交中...' : '交卷' }}</text>
			</view>
		</view>

		<uni-popup
			ref="explanationPopup"
			type="center"
			background-color="#ffffff"
			border-radius="28rpx"
			:is-mask-click="true"
			@change="handleExplanationPopupChange"
		>
			<reading-explanation-content
				:title="explanationPopupTitle"
				:mode="explanationState.mode"
				:selected-text="explanationState.selectedText"
				:note-content="explanationState.noteContent"
				:saving="explanationState.saving"
				:body-text="explanationBodyText"
				:meta-text="explanationMetaText"
				@close="closeExplanationPopup"
				@save="handleSaveExplanation"
				@update:note-content="handleExplanationNoteInput"
			/>
		</uni-popup>

		<template #tabbar>
			<kyyy-tabbar current="reading" />
		</template>
	</page-shell>
</template>

<script lang="ts">
import { defineComponent, nextTick } from 'vue'
import PageShell from '@/components/page-shell/page-shell.vue'
import KyyyTabbar from '@/components/kyyy/kyyy-tabbar.vue'
import ReadingPassageCard from '@/components/kyyy/reading/ReadingPassageCard.vue'
import ReadingSummaryCard from '@/components/kyyy/reading/ReadingSummaryCard.vue'
import ReadingQuestionCard from '@/components/kyyy/reading/ReadingQuestionCard.vue'
import ReadingActionMenu from '@/components/kyyy/reading/ReadingActionMenu.vue'
import ReadingExplanationContent from '@/components/kyyy/reading/ReadingExplanationContent.vue'
import { bootstrapAuth } from '@/shared/session/session'
import { resolveErrorMessage, toPositiveInt } from '@/pages/kyyy/reading/helpers'
import {
	createReadingAnnotation,
	deleteReadingAnnotation,
	getReadingSession,
	saveReadingAnswer,
	submitReadingSession,
	updateReadingAnnotation
} from '@/pages/kyyy/api/reading'
import {
	buildRangeFromTokens,
	findAnnotationByToken,
	hasAnnotationOverlap,
	isSelectableToken,
	type KyyyReadingRangeState
} from '@/pages/kyyy/reading/annotation'
import type {
	KyyyReadingAnnotationContentType,
	KyyyReadingActionMenuState as ReadingActionMenuState,
	KyyyReadingAnnotationState,
	KyyyReadingExplanationState as ReadingExplanationState,
	KyyyReadingPoint as ReadingPoint,
	KyyyReadingPopupRef as UniPopupRef,
	KyyyReadingQuestionState,
	KyyyReadingPageState as ReadingPageState,
	KyyyReadingSelectionTarget as ReadingSelectionTarget,
	KyyyReadingSessionState,
	KyyyReadingTextToken,
	KyyyReadingTextTouchLikeEvent as TextTouchLikeEvent
} from '@/pages/kyyy/reading/types'
import { createDefaultReadingExplanationState } from '@/pages/kyyy/reading/types'
import { createEmptyReadingSession, normalizeReadingAnnotation, normalizeReadingSession } from '@/pages/kyyy/reading/view'

export default defineComponent({
	name: 'KyyyReadingPage',
	components: {
		PageShell,
		KyyyTabbar,
		ReadingPassageCard,
		ReadingSummaryCard,
		ReadingQuestionCard,
		ReadingActionMenu,
		ReadingExplanationContent
	},
	data(): ReadingPageState {
		return {
			sessionState: createEmptyReadingSession(),
			passageId: null,
			targetQuestionId: null,
			locatingQuestionId: null,
			savingQuestionIds: {},
			questionUsedSeconds: {},
			questionTouchedAt: {},
			sessionToken: '',
			selectionMode: false,
			selectionTarget: null,
			selectionAnchorToken: null,
			previewRange: null,
			actionMenu: {
				visible: false,
				mode: 'selection',
				left: 0,
				top: 0
			},
			activeAnnotation: null,
			activeAnnotationTarget: null,
			explanationState: createDefaultReadingExplanationState()
		}
	},
	onLoad(query?: Record<string, string | undefined>) {
		this.passageId = toPositiveInt(query?.passageId)
		this.targetQuestionId = toPositiveInt(query?.questionId)
	},
	onShow() {
		this.bootstrapAndLoad(false)
	},
	computed: {
		passageMetaText(): string {
			const passage = this.sessionState.passage
			if (!passage) {
				return ''
			}
			const parts = [
				passage.examDirectionLabel || '',
				passage.sourceYear ? `${passage.sourceYear}` : '',
				passage.sourceName || '',
				passage.passageNo ? `Text ${passage.passageNo}` : ''
			].filter(Boolean)
			return parts.join(' · ')
		},
		progressText(): string {
			return `已答 ${this.sessionState.progress.answeredCount}/${this.sessionState.progress.totalQuestions}`
		},
		accuracyText(): string {
			return `${this.sessionState.summary?.accuracyRate || 0}%`
		},
		actionMenuStyle(): string {
			return `left:${this.actionMenu.left}px;top:${this.actionMenu.top}px;`
		},
		explanationPopupTitle(): string {
			return this.explanationState.mode === 'view' ? '解释' : '添加解释'
		},
		explanationBodyText(): string {
			const text = this.explanationState.noteContent.trim()
			return text || '暂无解释'
		},
		explanationMetaText(): string {
			if (this.explanationState.mode !== 'view') {
				return `${this.explanationState.noteContent.length}/200`
			}
			return this.explanationState.noteContent.trim() ? '' : '当前标注还没有解释内容'
		}
	},
	methods: {
		async bootstrapAndLoad(freshAttempt: boolean): Promise<void> {
			this.sessionState = {
				...this.sessionState,
				loading: true
			}
			try {
				await bootstrapAuth({ silent: true })
			} catch (error) {
				console.warn('[kyyy-reading] bootstrap auth failed', error)
			}
			await this.loadSession(freshAttempt)
		},
		async loadSession(freshAttempt: boolean): Promise<void> {
			try {
				const response = await getReadingSession(this.passageId, freshAttempt)
				this.applySessionState(normalizeReadingSession(response))
			} catch (error) {
				console.warn('[kyyy-reading] load session failed', error)
				this.sessionState = {
					...this.sessionState,
					loading: false,
					submitting: false,
					loaded: true
				}
				uni.showToast({
					title: resolveErrorMessage(error, '阅读加载失败'),
					icon: 'none'
				})
			}
		},
		applySessionState(nextState: KyyyReadingSessionState): void {
			const nextSessionToken = `${nextState.sessionId || 0}:${nextState.passage?.id || 0}:${nextState.status}`
			if (nextSessionToken !== this.sessionToken) {
				this.questionUsedSeconds = {}
				this.questionTouchedAt = {}
				this.savingQuestionIds = {}
			}
			const now = Date.now()
			nextState.questions.forEach((question) => {
				if (!this.questionTouchedAt[question.questionId]) {
					this.questionTouchedAt[question.questionId] = now
				}
			})
			this.sessionToken = nextSessionToken
			this.passageId = nextState.passage?.id || this.passageId
			this.sessionState = {
				...nextState,
				loading: false,
				submitting: false
			}
			this.resetSelectionState()
			this.closeExplanationPopup()
			void this.locateTargetQuestion()
		},
		reloadSession(freshAttempt: boolean): void {
			if (this.sessionState.loading || this.sessionState.submitting) {
				return
			}
			this.bootstrapAndLoad(freshAttempt)
		},
		async locateTargetQuestion(): Promise<void> {
			if (!this.targetQuestionId) {
				this.locatingQuestionId = null
				return
			}
			const targetQuestion = this.sessionState.questions.find((item) => item.questionId === this.targetQuestionId)
			if (!targetQuestion) {
				this.locatingQuestionId = null
				return
			}
			this.locatingQuestionId = targetQuestion.questionId
			await nextTick()
			uni.pageScrollTo({
				selector: `#reading-question-${targetQuestion.questionId}`,
				duration: 0,
				offsetTop: 88
			})
			setTimeout(() => {
				if (this.locatingQuestionId === targetQuestion.questionId) {
					this.locatingQuestionId = null
				}
			}, 2200)
			this.targetQuestionId = null
		},
		handlePassageTokenLongPress(token: KyyyReadingTextToken, event: unknown): void {
			this.handleTokenLongPress('passage_text', null, token, event as TextTouchLikeEvent)
		},
		handlePassageTokenTap(token: KyyyReadingTextToken, event: unknown): void {
			this.handleTokenTap('passage_text', null, token, event as TextTouchLikeEvent)
		},
		handleQuestionTokenLongPress(questionId: number, token: KyyyReadingTextToken, event: unknown): void {
			this.handleTokenLongPress('question_stem', questionId, token, event as TextTouchLikeEvent)
		},
		handleQuestionTokenTap(questionId: number, token: KyyyReadingTextToken, event: unknown): void {
			this.handleTokenTap('question_stem', questionId, token, event as TextTouchLikeEvent)
		},
		handleQuestionSelectOption(questionId: number, optionKey: string): void {
			this.handleSelectOption(questionId, optionKey)
		},
		isQuestionSaving(questionId: number): boolean {
			return Boolean(this.savingQuestionIds[questionId])
		},
		resolvePassageTokenClass(token: KyyyReadingTextToken): Record<string, boolean> {
			return this.resolveTextTokenClass('passage_text', null, token)
		},
		createQuestionTokenClassResolver(questionId: number): (token: KyyyReadingTextToken) => Record<string, boolean> {
			return (token: KyyyReadingTextToken) => this.resolveTextTokenClass('question_stem', questionId, token)
		},
		createQuestionOptionClassResolver(question: KyyyReadingQuestionState): (optionKey: string) => Record<string, boolean> {
			return (optionKey: string) => this.resolveOptionClass(question, optionKey)
		},
		resolveOptionClass(question: KyyyReadingQuestionState, optionKey: string): Record<string, boolean> {
			const selected = question.selectedOptionKey === optionKey
			const submitted = this.sessionState.status === 'submitted'
			const correct = submitted && question.correctAnswer === optionKey
			const wrong = submitted && selected && question.correctAnswer !== optionKey
			const disabled = this.sessionState.status !== 'active' || this.isQuestionSaving(question.questionId) || this.sessionState.submitting
			return {
				'is-selected': selected,
				'is-correct': correct,
				'is-wrong': wrong,
				'is-disabled': disabled
			}
		},
		resolveTextTokenClass(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			token: KyyyReadingTextToken): Record<string, boolean> {
			const annotations = this.resolveAnnotationsForTarget(contentType, questionId)
			const hitAnnotation = findAnnotationByToken(annotations, token)
			const isPreview = this.selectionMode
				&& this.targetsMatch(this.selectionTarget, this.buildSelectionTarget(contentType, questionId))
				&& !!this.previewRange
				&& this.previewRange.startOffset < token.endOffset
				&& this.previewRange.endOffset > token.startOffset
			const hasExplanation = !!hitAnnotation?.noteContent.trim()
			return {
				'is-mark': !!hitAnnotation && !hasExplanation,
				'is-explanation': !!hitAnnotation && hasExplanation,
				'is-preview': isPreview,
				'is-selectable': isSelectableToken(token)
			}
		},
		resolveUsedSeconds(questionId: number): number {
			const now = Date.now()
			const lastTouchedAt = this.questionTouchedAt[questionId] || now
			const accumulated = this.questionUsedSeconds[questionId] || 0
			const deltaSeconds = Math.max(Math.round((now - lastTouchedAt) / 1000), 0)
			const nextValue = accumulated + deltaSeconds
			this.questionUsedSeconds[questionId] = nextValue
			this.questionTouchedAt[questionId] = now
			return nextValue
		},
		async handleSelectOption(questionId: number, optionKey: string): Promise<void> {
			if (this.sessionState.status !== 'active' || !this.sessionState.sessionId) {
				return
			}
			if (this.isQuestionSaving(questionId) || this.sessionState.submitting) {
				return
			}
			if (this.actionMenu.visible || this.explanationState.visible) {
				this.closeActionMenu()
			}
			const targetQuestion = this.sessionState.questions.find((item) => item.questionId === questionId)
			if (!targetQuestion) {
				return
			}
			const previousSelectedOptionKey = targetQuestion.selectedOptionKey
			const previousAnswerStatus = targetQuestion.answerStatus
			targetQuestion.selectedOptionKey = optionKey
			targetQuestion.answerStatus = 'answered'
			this.savingQuestionIds = {
				...this.savingQuestionIds,
				[questionId]: true
			}
			try {
				const response = await saveReadingAnswer(this.sessionState.sessionId, questionId, {
					answerContent: optionKey,
					usedSeconds: this.resolveUsedSeconds(questionId)
				})
				this.applySessionState(normalizeReadingSession(response))
			} catch (error) {
				console.warn('[kyyy-reading] save answer failed', error)
				targetQuestion.selectedOptionKey = previousSelectedOptionKey
				targetQuestion.answerStatus = previousAnswerStatus
				uni.showToast({
					title: resolveErrorMessage(error, '答案保存失败'),
					icon: 'none'
				})
			} finally {
				const nextSavingQuestionIds = { ...this.savingQuestionIds }
				delete nextSavingQuestionIds[questionId]
				this.savingQuestionIds = nextSavingQuestionIds
			}
		},
		async handleSubmit(): Promise<void> {
			if (this.sessionState.status !== 'active' || !this.sessionState.sessionId) {
				return
			}
			if (this.sessionState.submitting || this.sessionState.loading) {
				return
			}
			if (this.selectionMode || this.actionMenu.visible || this.explanationState.visible) {
				uni.showToast({
					title: '请先完成当前标注操作',
					icon: 'none'
				})
				return
			}
			if (this.sessionState.progress.unansweredCount > 0) {
				const confirmResult = await uni.showModal({
					title: '确认交卷',
					content: `还有 ${this.sessionState.progress.unansweredCount} 题未作答，未答题会按 skipped 处理，是否继续提交？`,
					confirmText: '继续交卷',
					cancelText: '继续作答'
				})
				if (!confirmResult.confirm) {
					return
				}
			}
			this.sessionState = {
				...this.sessionState,
				submitting: true
			}
			const sessionId = this.sessionState.sessionId
			if (!sessionId) {
				this.sessionState = {
					...this.sessionState,
					submitting: false
				}
				return
			}
			try {
				const response = await submitReadingSession(sessionId)
				this.applySessionState(normalizeReadingSession(response))
			} catch (error) {
				console.warn('[kyyy-reading] submit session failed', error)
				this.sessionState = {
					...this.sessionState,
					submitting: false
				}
				uni.showToast({
					title: resolveErrorMessage(error, '交卷失败'),
					icon: 'none'
				})
			}
		},
		handleTokenLongPress(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			token: KyyyReadingTextToken,
			event: TextTouchLikeEvent): void {
			if (!isSelectableToken(token)) {
				return
			}
			const point = this.extractEventPoint(event)
			const annotation = this.resolveAnnotationByToken(contentType, questionId, token)
			if (annotation) {
				this.openExistingActionMenu(contentType, questionId, annotation, point)
				return
			}
			this.startSelection(contentType, questionId, token, point)
		},
		handleTokenTap(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			token: KyyyReadingTextToken,
			event: TextTouchLikeEvent): void {
			const annotation = this.resolveAnnotationByToken(contentType, questionId, token)
			const point = this.extractEventPoint(event)
			if (this.selectionMode && this.selectionTarget && this.selectionAnchorToken) {
				const target = this.buildSelectionTarget(contentType, questionId)
				if (!this.targetsMatch(target, this.selectionTarget) || !isSelectableToken(token)) {
					return
				}
				const sourceText = this.resolveSourceText(contentType, questionId)
				this.previewRange = buildRangeFromTokens(sourceText, this.selectionAnchorToken, token)
				this.openSelectionActionMenu(point)
				return
			}
			if (annotation) {
				this.openExistingActionMenu(contentType, questionId, annotation, point)
			}
		},
		startSelection(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			token: KyyyReadingTextToken,
			point: ReadingPoint): void {
			const sourceText = this.resolveSourceText(contentType, questionId)
			this.closeExplanationPopup()
			this.selectionMode = true
			this.selectionTarget = this.buildSelectionTarget(contentType, questionId)
			this.selectionAnchorToken = token
			this.previewRange = buildRangeFromTokens(sourceText, token, token)
			this.activeAnnotation = null
			this.activeAnnotationTarget = null
			this.openSelectionActionMenu(point)
		},
		async handleCreateMark(): Promise<void> {
			if (!this.selectionMode || !this.selectionTarget || !this.previewRange || !this.sessionState.passage?.id) {
				return
			}
			const annotations = this.resolveAnnotationsForTarget(this.selectionTarget.contentType, this.selectionTarget.questionId)
			if (hasAnnotationOverlap(annotations, this.previewRange)) {
				uni.showToast({
					title: '当前选区与已有标注重叠',
					icon: 'none'
				})
				return
			}
			try {
				const response = await createReadingAnnotation({
					passageId: this.sessionState.passage.id,
					questionId: this.selectionTarget.questionId,
					contentType: this.selectionTarget.contentType,
					startOffset: this.previewRange.startOffset,
					endOffset: this.previewRange.endOffset,
					selectedText: this.previewRange.selectedText,
					noteContent: ''
				})
				const annotation = normalizeReadingAnnotation(response)
				if (!annotation) {
					throw new Error('标注结果无效')
				}
				this.insertAnnotationIntoState(this.selectionTarget.contentType, this.selectionTarget.questionId, annotation)
				this.resetSelectionState()
			} catch (error) {
				console.warn('[kyyy-reading] create mark failed', error)
				uni.showToast({
					title: resolveErrorMessage(error, '标注失败'),
					icon: 'none'
				})
			}
		},
		handleCreateExplanation(): void {
			if (!this.selectionMode || !this.selectionTarget || !this.previewRange) {
				return
			}
			const annotations = this.resolveAnnotationsForTarget(this.selectionTarget.contentType, this.selectionTarget.questionId)
			if (hasAnnotationOverlap(annotations, this.previewRange)) {
				uni.showToast({
					title: '当前选区与已有标注重叠',
					icon: 'none'
				})
				return
			}
			this.explanationState = {
				visible: true,
				mode: 'create',
				annotationId: null,
				contentType: this.selectionTarget.contentType,
				questionId: this.selectionTarget.questionId,
				startOffset: this.previewRange.startOffset,
				endOffset: this.previewRange.endOffset,
				selectedText: this.previewRange.selectedText,
				noteContent: '',
				saving: false
			}
			this.closeActionMenu()
			this.openExplanationPopup()
		},
		async handleRemoveAnnotation(): Promise<void> {
			if (!this.activeAnnotation) {
				return
			}
			const confirmResult = await uni.showModal({
				title: '取消标注',
				content: '取消后这段高亮会被移除，是否继续？',
				confirmText: '取消标注',
				cancelText: '返回'
			})
			if (!confirmResult.confirm) {
				return
			}
			try {
				await deleteReadingAnnotation(this.activeAnnotation.id)
				this.removeAnnotationFromState(this.activeAnnotation.id)
				this.closeActionMenu()
			} catch (error) {
				console.warn('[kyyy-reading] delete annotation failed', error)
				uni.showToast({
					title: resolveErrorMessage(error, '取消标注失败'),
					icon: 'none'
				})
			}
		},
		handleViewExplanation(): void {
			if (!this.activeAnnotation || !this.activeAnnotationTarget) {
				return
			}
			const hasExplanation = !!this.activeAnnotation.noteContent.trim()
			this.explanationState = {
				visible: true,
				mode: hasExplanation ? 'view' : 'edit',
				annotationId: this.activeAnnotation.id,
				contentType: this.activeAnnotationTarget.contentType,
				questionId: this.activeAnnotationTarget.questionId,
				startOffset: this.activeAnnotation.startOffset,
				endOffset: this.activeAnnotation.endOffset,
				selectedText: this.activeAnnotation.selectedText,
				noteContent: this.activeAnnotation.noteContent,
				saving: false
			}
			this.closeActionMenu()
			this.openExplanationPopup()
		},
		async handleSaveExplanation(): Promise<void> {
			const noteContent = this.explanationState.noteContent.trim()
			if (!noteContent) {
				uni.showToast({
					title: '解释内容不能为空',
					icon: 'none'
				})
				return
			}
			if (!this.sessionState.passage?.id) {
				return
			}
			this.explanationState = {
				...this.explanationState,
				saving: true
			}
			try {
				const response = this.explanationState.mode === 'edit' && this.explanationState.annotationId
					? await updateReadingAnnotation(this.explanationState.annotationId, {
						noteContent
					})
					: await createReadingAnnotation({
						passageId: this.sessionState.passage.id,
						questionId: this.explanationState.questionId,
						contentType: this.explanationState.contentType,
						startOffset: this.explanationState.startOffset,
						endOffset: this.explanationState.endOffset,
						selectedText: this.explanationState.selectedText,
						noteContent
					})
				const annotation = normalizeReadingAnnotation(response)
				if (!annotation) {
					throw new Error('解释结果无效')
				}
				if (this.explanationState.mode === 'edit') {
					this.replaceAnnotationInState(this.explanationState.contentType, this.explanationState.questionId, annotation)
				} else {
					this.insertAnnotationIntoState(this.explanationState.contentType, this.explanationState.questionId, annotation)
				}
				this.closeExplanationPopup()
				this.resetSelectionState()
			} catch (error) {
				console.warn('[kyyy-reading] create explanation failed', error)
				this.explanationState = {
					...this.explanationState,
					saving: false
				}
				uni.showToast({
					title: resolveErrorMessage(error, '解释保存失败'),
					icon: 'none'
				})
			}
		},
		openExplanationPopup(): void {
			;(this.$refs.explanationPopup as UniPopupRef | undefined)?.open()
		},
		closeExplanationPopup(): void {
			;(this.$refs.explanationPopup as UniPopupRef | undefined)?.close()
		},
		handleExplanationPopupChange(event?: { show?: boolean }): void {
			const visible = !!event?.show
			const previousMode = this.explanationState.mode
			this.explanationState.visible = visible
			if (!visible && !this.explanationState.saving) {
				this.explanationState = createDefaultReadingExplanationState()
				if (previousMode === 'create') {
					this.resetSelectionState()
				}
			}
		},
		handleExplanationNoteInput(value: string): void {
			this.explanationState = {
				...this.explanationState,
				noteContent: value
			}
		},
		openSelectionActionMenu(point: ReadingPoint): void {
			this.activeAnnotation = null
			this.activeAnnotationTarget = null
			this.actionMenu = {
				visible: true,
				mode: 'selection',
				...this.normalizeActionMenuPoint(point)
			}
		},
		openExistingActionMenu(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			annotation: KyyyReadingAnnotationState,
			point: ReadingPoint): void {
			this.resetSelectionState()
			this.activeAnnotation = annotation
			this.activeAnnotationTarget = this.buildSelectionTarget(contentType, questionId)
			this.actionMenu = {
				visible: true,
				mode: 'existing',
				...this.normalizeActionMenuPoint(point)
			}
		},
		closeActionMenu(): void {
			this.actionMenu.visible = false
			this.activeAnnotation = null
			this.activeAnnotationTarget = null
		},
		resetSelectionState(): void {
			this.selectionMode = false
			this.selectionTarget = null
			this.selectionAnchorToken = null
			this.previewRange = null
			this.closeActionMenu()
		},
		restartSession(): void {
			this.reloadSession(true)
		},
		goHome(): void {
			uni.reLaunch({
				url: '/pages/kyyy/index',
				fail: (error: unknown) => {
					console.warn('[kyyy-reading] go home failed', error)
				}
			})
		},
		buildSelectionTarget(contentType: KyyyReadingAnnotationContentType, questionId: number | null): ReadingSelectionTarget {
			return {
				contentType,
				questionId: questionId || null
			}
		},
		targetsMatch(first: ReadingSelectionTarget | null, second: ReadingSelectionTarget | null): boolean {
			return !!first
				&& !!second
				&& first.contentType === second.contentType
				&& (first.questionId || null) === (second.questionId || null)
		},
		resolveAnnotationsForTarget(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null): KyyyReadingAnnotationState[] {
			if (contentType === 'passage_text') {
				return this.sessionState.passage?.annotations || []
			}
			return this.sessionState.questions.find((item) => item.questionId === questionId)?.annotations || []
		},
		resolveAnnotationByToken(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			token: KyyyReadingTextToken): KyyyReadingAnnotationState | null {
			return findAnnotationByToken(this.resolveAnnotationsForTarget(contentType, questionId), token)
		},
		resolveSourceText(contentType: KyyyReadingAnnotationContentType, questionId: number | null): string {
			if (contentType === 'passage_text') {
				return this.sessionState.passage?.passageText || ''
			}
			return this.sessionState.questions.find((item) => item.questionId === questionId)?.stem || ''
		},
		sortAnnotations(annotations: KyyyReadingAnnotationState[]): KyyyReadingAnnotationState[] {
			return [...annotations].sort((first, second) => {
				if (first.startOffset !== second.startOffset) {
					return first.startOffset - second.startOffset
				}
				if (first.endOffset !== second.endOffset) {
					return first.endOffset - second.endOffset
				}
				return first.id - second.id
			})
		},
		insertAnnotationIntoState(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			annotation: KyyyReadingAnnotationState): void {
			if (contentType === 'passage_text' && this.sessionState.passage) {
				this.sessionState = {
					...this.sessionState,
					passage: {
						...this.sessionState.passage,
						annotations: this.sortAnnotations([...this.sessionState.passage.annotations, annotation])
					}
				}
				return
			}
			this.sessionState = {
				...this.sessionState,
				questions: this.sessionState.questions.map((question) => question.questionId === questionId
					? {
						...question,
						annotations: this.sortAnnotations([...question.annotations, annotation])
					}
					: question)
			}
		},
		replaceAnnotationInState(contentType: KyyyReadingAnnotationContentType,
			questionId: number | null,
			annotation: KyyyReadingAnnotationState): void {
			if (contentType === 'passage_text' && this.sessionState.passage) {
				this.sessionState = {
					...this.sessionState,
					passage: {
						...this.sessionState.passage,
						annotations: this.sortAnnotations(this.sessionState.passage.annotations.map((item) => item.id === annotation.id ? annotation : item))
					}
				}
				return
			}
			this.sessionState = {
				...this.sessionState,
				questions: this.sessionState.questions.map((question) => question.questionId === questionId
					? {
						...question,
						annotations: this.sortAnnotations(question.annotations.map((item) => item.id === annotation.id ? annotation : item))
					}
					: question)
			}
		},
		removeAnnotationFromState(annotationId: number): void {
			this.sessionState = {
				...this.sessionState,
				passage: this.sessionState.passage
					? {
						...this.sessionState.passage,
						annotations: this.sessionState.passage.annotations.filter((item) => item.id !== annotationId)
					}
					: null,
				questions: this.sessionState.questions.map((question) => ({
					...question,
					annotations: question.annotations.filter((item) => item.id !== annotationId)
				}))
			}
		},
		extractEventPoint(event: TextTouchLikeEvent): ReadingPoint {
			const changedTouch = event?.changedTouches?.[0]
			const detailX = Number(event?.detail?.x)
			const detailY = Number(event?.detail?.y)
			const touchX = Number(changedTouch?.x)
			const touchY = Number(changedTouch?.y)
			const x = Number.isFinite(detailX) ? detailX : (Number.isFinite(touchX) ? touchX : 120)
			const y = Number.isFinite(detailY) ? detailY : (Number.isFinite(touchY) ? touchY : 120)
			return { x, y }
		},
		normalizeActionMenuPoint(point: ReadingPoint): { left: number; top: number } {
			const systemInfo = uni.getSystemInfoSync()
			const menuHalfWidth = 120
			const left = Math.min(Math.max(point.x, menuHalfWidth), systemInfo.windowWidth - menuHalfWidth)
			const top = Math.max(point.y - 52, 72)
			return { left, top }
		}
	}
})
</script>

<style lang="scss" scoped>
.kyyy-reading-page__content {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.kyyy-reading-page__state-card {
	background: rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(210, 218, 226, 0.92);
	border-radius: 28rpx;
	box-shadow: 0 18rpx 36rpx rgba(43, 52, 55, 0.08);
	padding: 32rpx 28rpx;
}

.kyyy-reading-page__state-desc {
	font-size: 24rpx;
	line-height: 1.7;
	color: #667483;
}

.kyyy-reading-page__state-title,
.kyyy-reading-page__footer-title {
	font-size: 32rpx;
	font-weight: 700;
	line-height: 1.5;
	color: #253445;
}

.kyyy-reading-page__footer {
	position: fixed;
	left: 24rpx;
	right: 24rpx;
	bottom: calc(env(safe-area-inset-bottom) + 120rpx);
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
	padding: 16rpx 20rpx;
	border-radius: 22rpx;
	background: rgba(255, 255, 255, 0.98);
	border: 1rpx solid rgba(213, 220, 227, 0.95);
	box-shadow: 0 20rpx 40rpx rgba(43, 52, 55, 0.12);
	z-index: 20;
}

.kyyy-reading-page__footer-copy {
	display: flex;
	align-items: center;
	min-width: 0;
	flex: 1;
}

.kyyy-reading-page__submit-button,
.kyyy-reading-page__action-button {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 64rpx;
	padding: 0 28rpx;
	border-radius: 16rpx;
	background: #4b617a;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 600;
}

.kyyy-reading-page__submit-button.is-disabled {
	opacity: 0.7;
}

.kyyy-reading-page__action-button--ghost {
	background: #eef2f5;
	color: #46566a;
}

.kyyy-reading-page__state-actions,
.kyyy-reading-page__result-actions {
	display: flex;
	gap: 20rpx;
	margin-top: 24rpx;
}

.kyyy-reading-page__action-button {
	flex: 1;
}

.kyyy-reading-page__loader {
	width: 52rpx;
	height: 52rpx;
	margin: 0 auto 18rpx;
	border-radius: 50%;
	border: 5rpx solid rgba(76, 97, 122, 0.15);
	border-top-color: #4c617a;
	animation: kyyy-reading-spin 1s linear infinite;
}

.kyyy-reading-page__state-card {
	margin-top: 24rpx;
	text-align: center;
}

.kyyy-reading-page__state-desc {
	display: block;
	margin-top: 10rpx;
}

.kyyy-reading-page__footer-title {
	font-size: 28rpx;
	line-height: 1.2;
}

@keyframes kyyy-reading-spin {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}
</style>
