import type {
	KyzzPracticeAnswerDraftState,
	KyzzPracticeBankRecordResponse,
	KyzzPracticeBankViewRecord,
	KyzzPracticeCommentItem,
	KyzzPracticeCommentLikeToggleResponse,
	KyzzPracticeCommentPageResponse,
	KyzzPracticeCommentState,
	KyzzPracticeDashboardResponse,
	KyzzPracticeDashboardState,
	KyzzPracticeEmptyState,
	KyzzPracticeNoticeViewModel,
	KyzzPracticeQuestionType,
	KyzzPracticeQuestionView,
	KyzzPracticeReviewState,
	KyzzPracticeReviewResponse,
	KyzzPracticeReviewViewResult,
	KyzzPracticeSettingResponse,
	KyzzPracticeSettingState,
	KyzzPracticeSessionResponse,
	KyzzPracticeSessionState,
	KyzzPracticeSourceType,
	KyzzPracticeUiState
} from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序刷题页数据归一化与展示辅助。

const DIFFICULTY_LABEL_MAP: Record<number, string> = {
	1: '简单',
	2: '中等',
	3: '困难',
	4: '冲刺'
}

const QUESTION_TYPE_LABEL_MAP: Record<KyzzPracticeQuestionType, string> = {
	single: '单选',
	multiple: '多选',
	short: '简答'
}

export function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

export function toBoolean(value: unknown, fallback = false): boolean {
	if (value === true || value === false) {
		return value
	}
	if (value === 1 || value === '1' || value === 'true') {
		return true
	}
	if (value === 0 || value === '0' || value === 'false') {
		return false
	}
	return fallback
}

export function createEmptyPracticeDashboard(): KyzzPracticeDashboardState {
	return {
		recommendedBankId: null,
		recommendedReason: '',
		records: []
	}
}

export function createEmptyPracticeSession(): KyzzPracticeSessionState {
	return {
		activeBank: null,
		switchableBanks: [],
		progress: {
			currentQuestionIndex: 1,
			totalQuestionCount: 0
		},
		question: null,
		previousQuestionId: null,
		previousQuestionIndex: 0,
		nextQuestionId: null,
		nextQuestionIndex: 0,
		reviewResult: null,
		sourceType: 'bank',
		sourceTitle: '题库练习'
	}
}

export function createEmptyPracticeAnswerDraft(): KyzzPracticeAnswerDraftState {
	return {
		selectedOptionKeys: [],
		answerText: '',
		questionStartedAt: Date.now()
	}
}

export function createEmptyPracticeReviewState(): KyzzPracticeReviewState {
	return {
		result: null
	}
}

export function createEmptyPracticeCommentState(): KyzzPracticeCommentState {
	return {
		questionId: null,
		records: [],
		pageNo: 0,
		pageSize: 10,
		total: 0,
		hasMore: false,
		loading: false,
		loadingMore: false,
		submitting: false,
		likingCommentIds: [],
		initialized: false,
		errorMessage: '',
		composerContent: ''
	}
}

export function createDefaultPracticeSettings(): KyzzPracticeSettingState {
	return {
		autoJumpOnCorrect: true,
		loaded: false,
		syncing: false
	}
}

export function createEmptyPracticeUiState(): KyzzPracticeUiState {
	return {
		loading: false,
		loadedOnce: false,
		submitting: false,
		emptyState: null
	}
}

export function normalizePracticeSettings(result: KyzzPracticeSettingResponse | null | undefined): KyzzPracticeSettingState {
	return {
		autoJumpOnCorrect: toBoolean(result?.autoJumpOnCorrect, true),
		loaded: true,
		syncing: false
	}
}

export function normalizePracticeBankRecord(record: KyzzPracticeBankRecordResponse): KyzzPracticeBankViewRecord {
	return {
		...record,
		questionCount: toNumber(record.questionCount),
		currentProgress: toNumber(record.currentProgress),
		studiedCount: toNumber(record.studiedCount),
		wrongCount: toNumber(record.wrongCount),
		resumeQuestionIndex: toNumber(record.resumeQuestionIndex)
	}
}

export function normalizePracticeDashboard(result: KyzzPracticeDashboardResponse | null | undefined): KyzzPracticeDashboardState {
	return {
		recommendedBankId: result?.recommendedBankId ?? null,
		recommendedReason: result?.recommendedReason || '',
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizePracticeBankRecord(item))
			: []
	}
}

export function normalizePracticeSession(result: KyzzPracticeSessionResponse): KyzzPracticeSessionState {
	return {
		activeBank: result?.activeBank ? normalizePracticeBankRecord(result.activeBank) : null,
		switchableBanks: Array.isArray(result?.switchableBanks)
			? result.switchableBanks.map((item) => normalizePracticeBankRecord(item))
			: [],
		progress: {
			currentQuestionIndex: toNumber(result?.progress?.currentQuestionIndex, 1),
			totalQuestionCount: toNumber(result?.progress?.totalQuestionCount)
		},
		question: result?.question
			? {
				...result.question,
				score: toNumber(result.question.score),
				sortNo: toNumber(result.question.sortNo),
				yearNo: result.question.yearNo === null || result.question.yearNo === undefined || result.question.yearNo === ''
					? null
					: toNumber(result.question.yearNo, 0),
				isFavorite: toBoolean(result.question.isFavorite)
			}
			: null,
		previousQuestionId: result?.previousQuestionId ?? null,
		previousQuestionIndex: toNumber(result?.previousQuestionIndex),
		nextQuestionId: result?.nextQuestionId ?? null,
		nextQuestionIndex: toNumber(result?.nextQuestionIndex),
		reviewResult: result?.reviewResult ? normalizePracticeReviewResult(result.reviewResult) : null,
		sourceType: normalizePracticeSourceType(result?.sourceType),
		sourceTitle: result?.sourceTitle || sourceTypeLabel(normalizePracticeSourceType(result?.sourceType))
	}
}

export function normalizePracticeReviewResult(result: KyzzPracticeReviewResponse): KyzzPracticeReviewViewResult {
	const sourceType = normalizePracticeSourceType(result.sourceType)
	return {
		...result,
		sourceType,
		sourceTitle: result.sourceTitle || sourceTypeLabel(sourceType),
		completedSource: Boolean(result.completedSource),
		updatedBank: result.updatedBank ? normalizePracticeBankRecord(result.updatedBank) : null,
		nextQuestionIndex: toNumber(result.nextQuestionIndex)
	}
}

export function normalizePracticeCommentItem(item: KyzzPracticeCommentItem): KyzzPracticeCommentItem {
	return {
		...item,
		commentId: toNumber(item.commentId),
		questionId: toNumber(item.questionId),
		likeCount: toNumber(item.likeCount),
		replyCount: toNumber(item.replyCount),
		author: {
			id: toNumber(item?.author?.id),
			nickname: resolveCommentAuthorName(item?.author?.nickname),
			avatarUrl: item?.author?.avatarUrl || null
		},
		isMine: Boolean(item.isMine),
		isLiked: toBoolean(item.isLiked),
		createdAt: item.createdAt || null,
		createdAtLabel: formatCommentTime(item.createdAt || null)
	}
}

export function normalizePracticeCommentLikeToggleResponse(
	result: KyzzPracticeCommentLikeToggleResponse
): KyzzPracticeCommentLikeToggleResponse {
	return {
		commentId: toNumber(result?.commentId),
		questionId: toNumber(result?.questionId),
		isLiked: toBoolean(result?.isLiked),
		likeCount: toNumber(result?.likeCount)
	}
}

export function sortPracticeCommentsByLike(records: KyzzPracticeCommentItem[]): KyzzPracticeCommentItem[] {
	return [...records].sort((left, right) => {
		const likeDiff = toNumber(right.likeCount) - toNumber(left.likeCount)
		if (likeDiff !== 0) {
			return likeDiff
		}
		const leftCreatedAt = left.createdAt || ''
		const rightCreatedAt = right.createdAt || ''
		if (leftCreatedAt !== rightCreatedAt) {
			return rightCreatedAt.localeCompare(leftCreatedAt)
		}
		return toNumber(right.commentId) - toNumber(left.commentId)
	})
}

export function normalizePracticeCommentPage(result: KyzzPracticeCommentPageResponse): KyzzPracticeCommentPageResponse {
	return {
		records: Array.isArray(result?.records)
			? result.records.map((item) => normalizePracticeCommentItem(item))
			: [],
		pageNo: toNumber(result?.pageNo),
		pageSize: toNumber(result?.pageSize, 10),
		hasMore: Boolean(result?.hasMore),
		total: toNumber(result?.total)
	}
}

export function normalizePracticeSourceType(value: unknown): KyzzPracticeSourceType {
	if (value === 'wrong_book' || value === 'favorite') {
		return value
	}
	return 'bank'
}

export function sourceTypeLabel(value: KyzzPracticeSourceType): string {
	if (value === 'wrong_book') {
		return '错题本练习'
	}
	if (value === 'favorite') {
		return '收藏练习'
	}
	return '题库练习'
}

export function difficultyLabel(level: number): string {
	return DIFFICULTY_LABEL_MAP[level] || `L${level || 0}`
}

export function questionTypeLabel(questionType: KyzzPracticeQuestionType): string {
	return QUESTION_TYPE_LABEL_MAP[questionType] || questionType
}

export function formatProgress(value: number): string {
	const normalized = Math.round(toNumber(value) * 10) / 10
	return `${Number.isInteger(normalized) ? normalized : normalized.toFixed(1)}%`
}

export function buildCoverInitial(name: string): string {
	if (!name) {
		return '题'
	}
	return name.trim().slice(0, 1).toUpperCase()
}

export function formatLastPractice(value: string | null): string {
	if (!value) {
		return '暂未开始'
	}
	const normalized = value.replace(/-/g, '/')
	const practiceDate = new Date(normalized)
	if (Number.isNaN(practiceDate.getTime())) {
		return value.slice(0, 16).replace('T', ' ')
	}
	const today = new Date()
	const oneDay = 24 * 60 * 60 * 1000
	const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime()
	const practiceStart = new Date(practiceDate.getFullYear(), practiceDate.getMonth(), practiceDate.getDate()).getTime()
	const diffDays = Math.floor((todayStart - practiceStart) / oneDay)
	if (diffDays === 0) {
		return '今天'
	}
	if (diffDays === 1) {
		return '昨天'
	}
	if (diffDays > 1 && diffDays < 7) {
		return `${diffDays} 天前`
	}
	return `${practiceDate.getFullYear()}-${pad(practiceDate.getMonth() + 1)}-${pad(practiceDate.getDate())}`
}

export function resolveCommentAuthorName(value: string | null | undefined): string {
	if (!value || !value.trim()) {
		return '同学'
	}
	return value.trim()
}

export function buildCommentAuthorInitial(name: string): string {
	return resolveCommentAuthorName(name).slice(0, 1).toUpperCase()
}

export function formatCommentTime(value: string | null): string {
	if (!value) {
		return '刚刚'
	}
	const normalized = value.replace(/-/g, '/')
	const createdAt = new Date(normalized)
	if (Number.isNaN(createdAt.getTime())) {
		return value
	}
	const diff = Date.now() - createdAt.getTime()
	const minute = 60 * 1000
	const hour = 60 * minute
	const day = 24 * hour
	if (diff < minute) {
		return '刚刚'
	}
	if (diff < hour) {
		return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
	}
	if (diff < day) {
		return `${Math.max(1, Math.floor(diff / hour))} 小时前`
	}
	if (diff < 7 * day) {
		return `${Math.max(1, Math.floor(diff / day))} 天前`
	}
	return `${createdAt.getFullYear()}-${pad(createdAt.getMonth() + 1)}-${pad(createdAt.getDate())}`
}

export function isPracticeBankCompleted(bank: KyzzPracticeBankViewRecord | null): boolean {
	return Boolean(bank && bank.resumeStatus === 'completed')
}

export function isPracticeBankInProgress(bank: KyzzPracticeBankViewRecord | null): boolean {
	return Boolean(bank && bank.resumeStatus === 'in_progress')
}

export function hasQuestionOptions(question: KyzzPracticeQuestionView | null): boolean {
	return Boolean(question && Array.isArray(question.options) && question.options.length)
}

export function difficultyTagClass(level: number): string {
	if (level === 1) {
		return 'is-simple'
	}
	if (level === 2) {
		return 'is-medium'
	}
	if (level === 3) {
		return 'is-hard'
	}
	if (level === 4) {
		return 'is-sprint'
	}
	return ''
}

export function progressPercent(value: number): number {
	const progress = toNumber(value)
	if (progress <= 0) {
		return 0
	}
	if (progress >= 100) {
		return 100
	}
	return progress
}

export function buildLoadingNotice(): KyzzPracticeNoticeViewModel {
	return {
		title: '正在为你接续上次进度...',
		description: '会优先回到最近未完成的题库和题目位置。',
		variant: 'accent'
	}
}

export function buildNoBankNotice(): KyzzPracticeNoticeViewModel {
	return {
		title: '还没有可练习的题库',
		description: '先去公共题库挑几套适合当前阶段的内容，之后这里会自动续刷。',
		variant: 'default',
		primaryText: '去添加题库',
		secondaryText: '我的题库'
	}
}

export function buildNoQuestionNotice(): KyzzPracticeNoticeViewModel {
	return {
		title: '当前没有可展示的题目',
		description: '先去添加一套题库，或者稍后再来继续刷题。',
		variant: 'default',
		primaryText: '去添加题库',
		secondaryText: '返回学习'
	}
}

export function resolvePracticeEmptyState(error: unknown): KyzzPracticeEmptyState | null {
	const message = error instanceof Error ? error.message : ''
	if (!message) {
		return null
	}
	if (message.includes('可刷题库') || message.includes('已选择题库') || message.includes('先去添加')) {
		return 'no_bank'
	}
	if (message.includes('暂无可练习题目') || message.includes('没有可展示的题目')) {
		return 'no_question'
	}
	return null
}

export function buildCompletedNotice(bank: KyzzPracticeBankViewRecord | null): KyzzPracticeNoticeViewModel | null {
	if (!bank || bank.resumeStatus !== 'completed') {
		return null
	}
	return {
		title: '这一轮已经完成',
		description: '可以直接再刷一遍，或者切换到下一套题库继续保持节奏。',
		variant: 'success',
		pills: [
			bank.bankName,
			bank.resumeLabel,
			formatProgress(bank.currentProgress)
		]
	}
}

function pad(value: number): string {
	return String(value).padStart(2, '0')
}
