import { getSessionSnapshot } from '@/shared/session/session'
import type {
	KyzzPracticeAnswerDraftState,
	KyzzPracticeLaunchTarget,
	KyzzPracticeSourceStatus,
	KyzzPracticeSourceType
} from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序专项练习本机续练进度缓存。

export type KyzzSourcePracticeType = Exclude<KyzzPracticeSourceType, 'bank'>

export interface KyzzSourcePracticeProgress {
	version: 1
	sourceType: KyzzSourcePracticeType
	sourceStatus: KyzzPracticeSourceStatus | null
	keyword: string | null
	bankId: number | null
	questionId: number | null
	sourceTitle: string
	bankName: string | null
	currentQuestionIndex: number
	totalQuestionCount: number
	freshAttempt: boolean | null
	answerDraft: Pick<KyzzPracticeAnswerDraftState, 'selectedOptionKeys' | 'answerText'> | null
	dismissed: boolean
	updatedAt: number
}

interface UserKeyShape {
	id?: string | number | null
	userId?: string | number | null
	openid?: string | number | null
	openId?: string | number | null
	unionId?: string | number | null
}

const SOURCE_PROGRESS_KEY_PREFIX = 'kyzz.practice.source-progress'
const SOURCE_PROGRESS_VERSION = 1

function normalizeOptionalNumber(value: unknown): number | null {
	if (value === null || value === undefined || value === '') {
		return null
	}
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : null
}

function normalizeOptionalString(value: unknown): string | null {
	return typeof value === 'string' && value.trim() ? value.trim() : null
}

function normalizeSourceStatus(value: unknown): KyzzPracticeSourceStatus | null {
	if (value === 'all' || value === 'active' || value === 'mastered') {
		return value
	}
	return null
}

function normalizeSourceType(value: unknown): KyzzSourcePracticeType | null {
	if (value === 'wrong_book' || value === 'favorite' || value === 'random') {
		return value
	}
	return null
}

function normalizeAnswerDraft(value: unknown): Pick<KyzzPracticeAnswerDraftState, 'selectedOptionKeys' | 'answerText'> | null {
	if (!value || typeof value !== 'object') {
		return null
	}
	const draft = value as Partial<Pick<KyzzPracticeAnswerDraftState, 'selectedOptionKeys' | 'answerText'>>
	return {
		selectedOptionKeys: Array.isArray(draft.selectedOptionKeys)
			? draft.selectedOptionKeys.map((item) => String(item).trim().toUpperCase()).filter(Boolean).sort()
			: [],
		answerText: typeof draft.answerText === 'string' ? draft.answerText : ''
	}
}

function resolveCurrentUserKey(): string {
	const snapshot = getSessionSnapshot<UserKeyShape>()
	const currentUser = snapshot.currentUser
	const userKey = currentUser?.id ?? currentUser?.userId ?? currentUser?.openid ?? currentUser?.openId ?? currentUser?.unionId
	if (userKey !== null && userKey !== undefined && userKey !== '') {
		return String(userKey)
	}
	return snapshot.token ? `token:${snapshot.token.slice(0, 12)}` : 'anonymous'
}

function sourceProgressKey(): string {
	return `${SOURCE_PROGRESS_KEY_PREFIX}:${resolveCurrentUserKey()}`
}

function normalizeProgress(value: unknown): KyzzSourcePracticeProgress | null {
	if (!value || typeof value !== 'object') {
		return null
	}
	const progress = value as Partial<KyzzSourcePracticeProgress>
	const sourceType = normalizeSourceType(progress.sourceType)
	const questionId = normalizeOptionalNumber(progress.questionId)
	if (!sourceType || !questionId) {
		return null
	}
	const currentQuestionIndex = normalizeOptionalNumber(progress.currentQuestionIndex) ?? 1
	const totalQuestionCount = normalizeOptionalNumber(progress.totalQuestionCount) ?? 1
	return {
		version: SOURCE_PROGRESS_VERSION,
		sourceType,
		sourceStatus: normalizeSourceStatus(progress.sourceStatus),
		keyword: normalizeOptionalString(progress.keyword),
		bankId: normalizeOptionalNumber(progress.bankId),
		questionId,
		sourceTitle: normalizeOptionalString(progress.sourceTitle) || sourceTypeLabel(sourceType),
		bankName: normalizeOptionalString(progress.bankName),
		currentQuestionIndex: currentQuestionIndex > 0 ? currentQuestionIndex : 1,
		totalQuestionCount: totalQuestionCount > 0 ? totalQuestionCount : 1,
		freshAttempt: progress.freshAttempt === true ? true : progress.freshAttempt === false ? false : null,
		answerDraft: normalizeAnswerDraft(progress.answerDraft),
		dismissed: Boolean(progress.dismissed),
		updatedAt: normalizeOptionalNumber(progress.updatedAt) ?? Date.now()
	}
}

export function isSourcePracticeType(value: unknown): value is KyzzSourcePracticeType {
	return normalizeSourceType(value) !== null
}

export function sourceTypeLabel(sourceType: KyzzSourcePracticeType): string {
	if (sourceType === 'favorite') {
		return '收藏练习'
	}
	if (sourceType === 'wrong_book') {
		return '错题练习'
	}
	return '随机一题'
}

export function readSourcePracticeProgress(options: { includeDismissed?: boolean } = {}): KyzzSourcePracticeProgress | null {
	const progress = normalizeProgress(uni.getStorageSync(sourceProgressKey()))
	if (!progress) {
		return null
	}
	if (!options.includeDismissed && progress.dismissed) {
		return null
	}
	return progress
}

export function saveSourcePracticeProgress(progress: Omit<KyzzSourcePracticeProgress, 'version' | 'updatedAt'>): KyzzSourcePracticeProgress | null {
	const normalized = normalizeProgress({
		...progress,
		version: SOURCE_PROGRESS_VERSION,
		updatedAt: Date.now()
	})
	if (!normalized) {
		return null
	}
	uni.setStorageSync(sourceProgressKey(), normalized)
	return normalized
}

export function clearSourcePracticeProgress(): void {
	uni.removeStorageSync(sourceProgressKey())
}

export function dismissSourcePracticeProgress(): void {
	const progress = readSourcePracticeProgress({ includeDismissed: true })
	if (!progress) {
		return
	}
	saveSourcePracticeProgress({
		...progress,
		dismissed: true
	})
}

export function buildLaunchTargetFromSourceProgress(progress: KyzzSourcePracticeProgress): KyzzPracticeLaunchTarget {
	return {
		bankId: progress.bankId,
		questionId: progress.questionId,
		freshAttempt: progress.freshAttempt,
		sourceType: progress.sourceType,
		sourceStatus: progress.sourceStatus,
		keyword: progress.keyword
	}
}

export function sourceProgressMatchesQuestion(
	progress: KyzzSourcePracticeProgress | null,
	sourceType: KyzzPracticeSourceType | null | undefined,
	questionId: number | null | undefined
): boolean {
	return Boolean(
		progress
		&& questionId
		&& progress.questionId === questionId
		&& progress.sourceType === sourceType
	)
}
