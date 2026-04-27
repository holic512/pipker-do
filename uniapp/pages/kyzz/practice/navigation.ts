import type { KyzzPracticeLaunchTarget } from '@/pages/kyzz/practice/types'
import {
	buildLaunchTargetFromSourceProgress,
	clearSourcePracticeProgress,
	dismissSourcePracticeProgress,
	readSourcePracticeProgress,
	sourceTypeLabel
} from '@/pages/kyzz/practice/progress'

// AI 索引: KYZZ 小程序刷题路由跳转辅助。

const PRACTICE_TAB_URL = '/pages/kyzz/practice/index'
const PRACTICE_LAUNCH_TARGET_KEY = 'kyzz.practice.launch.target'

function normalizeOptionalNumber(value: unknown): number | null {
	if (value === null || value === undefined || value === '') {
		return null
	}
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : null
}

function normalizeOptionalBoolean(value: unknown): boolean | null {
	if (value === true || value === false) {
		return value
	}
	if (value === 'true' || value === '1' || value === 1) {
		return true
	}
	if (value === 'false' || value === '0' || value === 0) {
		return false
	}
	return null
}

function normalizeSourceType(value: unknown): KyzzPracticeLaunchTarget['sourceType'] | null {
	if (value === 'bank' || value === 'wrong_book' || value === 'favorite' || value === 'random') {
		return value
	}
	return null
}

function normalizeSourceStatus(value: unknown): KyzzPracticeLaunchTarget['sourceStatus'] | null {
	if (value === 'all' || value === 'active' || value === 'mastered') {
		return value
	}
	return null
}

function normalizeOptionalString(value: unknown): string | null {
	return typeof value === 'string' && value.trim() ? value.trim() : null
}

function normalizeLaunchTarget(target: KyzzPracticeLaunchTarget = {}): KyzzPracticeLaunchTarget {
	return {
		bankId: normalizeOptionalNumber(target.bankId),
		questionId: normalizeOptionalNumber(target.questionId),
		freshAttempt: normalizeOptionalBoolean(target.freshAttempt),
		sourceType: normalizeSourceType(target.sourceType),
		sourceStatus: normalizeSourceStatus(target.sourceStatus),
		keyword: normalizeOptionalString(target.keyword)
	}
}

function hasLaunchTarget(target: KyzzPracticeLaunchTarget): boolean {
	return target.bankId !== null && target.bankId !== undefined
		|| target.questionId !== null && target.questionId !== undefined
		|| target.sourceType !== null && target.sourceType !== undefined
}

function buildEntryTarget(target: KyzzPracticeLaunchTarget): KyzzPracticeLaunchTarget {
	if (target.sourceType === 'bank' || target.sourceType === 'wrong_book' || target.sourceType === 'favorite' || target.sourceType === 'random') {
		return target
	}
	if (hasLaunchTarget(target)) {
		return target
	}
	return {
		...target,
		sourceType: 'bank',
		sourceStatus: null,
		keyword: null
	}
}

function targetLabel(target: KyzzPracticeLaunchTarget): string {
	if (target.sourceType === 'favorite' || target.sourceType === 'wrong_book' || target.sourceType === 'random') {
		return sourceTypeLabel(target.sourceType)
	}
	return '题库刷题'
}

function showSourceResumeModal(target: KyzzPracticeLaunchTarget): Promise<boolean> {
	const progress = readSourcePracticeProgress()
	if (!progress) {
		return Promise.resolve(false)
	}
	return new Promise((resolve) => {
		const progressName = progress.sourceTitle || sourceTypeLabel(progress.sourceType)
		const bankName = progress.bankName ? `（${progress.bankName}）` : ''
		const progressText = progress.totalQuestionCount > 1
			? `第 ${progress.currentQuestionIndex} / ${progress.totalQuestionCount} 题`
			: '当前题'
		uni.showModal({
			title: '继续未完成练习？',
			content: `当前存在未完成的${progressName}${bankName}，停在${progressText}。要继续未完成练习，还是进入${targetLabel(target)}？`,
			confirmText: '继续',
			cancelText: '进入',
			success: (result: { confirm: boolean }) => {
				resolve(Boolean(result.confirm))
			},
			fail: () => {
				resolve(false)
			}
		})
	})
}

export function persistPracticeLaunchTarget(target: KyzzPracticeLaunchTarget = {}): void {
	const normalizedTarget = normalizeLaunchTarget(target)
	if (normalizedTarget.bankId === null && normalizedTarget.questionId === null && normalizedTarget.sourceType === null) {
		uni.removeStorageSync(PRACTICE_LAUNCH_TARGET_KEY)
		return
	}
	uni.setStorageSync(PRACTICE_LAUNCH_TARGET_KEY, normalizedTarget)
}

export function consumePracticeLaunchTarget(): KyzzPracticeLaunchTarget {
	const cachedTarget = uni.getStorageSync(PRACTICE_LAUNCH_TARGET_KEY)
	uni.removeStorageSync(PRACTICE_LAUNCH_TARGET_KEY)
	if (!cachedTarget || typeof cachedTarget !== 'object') {
		return {}
	}
	return {
		bankId: normalizeOptionalNumber((cachedTarget as KyzzPracticeLaunchTarget).bankId),
		questionId: normalizeOptionalNumber((cachedTarget as KyzzPracticeLaunchTarget).questionId),
		freshAttempt: normalizeOptionalBoolean((cachedTarget as KyzzPracticeLaunchTarget).freshAttempt),
		sourceType: normalizeSourceType((cachedTarget as KyzzPracticeLaunchTarget).sourceType),
		sourceStatus: normalizeSourceStatus((cachedTarget as KyzzPracticeLaunchTarget).sourceStatus),
		keyword: normalizeOptionalString((cachedTarget as KyzzPracticeLaunchTarget).keyword)
	}
}

export async function resolvePracticeEntryTarget(target: KyzzPracticeLaunchTarget = {}): Promise<KyzzPracticeLaunchTarget> {
	const normalizedTarget = normalizeLaunchTarget(target)
	const pendingProgress = readSourcePracticeProgress()
	if (!pendingProgress) {
		if (normalizedTarget.sourceType === 'favorite' || normalizedTarget.sourceType === 'wrong_book' || normalizedTarget.sourceType === 'random') {
			clearSourcePracticeProgress()
		}
		return normalizedTarget
	}
	const shouldResume = await showSourceResumeModal(normalizedTarget)
	if (shouldResume) {
		return buildLaunchTargetFromSourceProgress(pendingProgress)
	}
	const entryTarget = buildEntryTarget(normalizedTarget)
	if (entryTarget.sourceType === 'bank' || entryTarget.sourceType === null || entryTarget.sourceType === undefined) {
		dismissSourcePracticeProgress()
	} else if (entryTarget.sourceType === 'favorite' || entryTarget.sourceType === 'wrong_book' || entryTarget.sourceType === 'random') {
		clearSourcePracticeProgress()
	}
	return entryTarget
}

export async function resolveInitialPracticeLaunchTarget(): Promise<KyzzPracticeLaunchTarget> {
	const pendingProgress = readSourcePracticeProgress()
	if (!pendingProgress) {
		return {}
	}
	const shouldResume = await showSourceResumeModal({})
	if (shouldResume) {
		return buildLaunchTargetFromSourceProgress(pendingProgress)
	}
	dismissSourcePracticeProgress()
	return {
		sourceType: 'bank',
		sourceStatus: null,
		keyword: null
	}
}

// AI 索引: 不带 target 时仅切回练习 tab，保留当前普通/错题/收藏练习上下文。
export async function openPracticeTab(target: KyzzPracticeLaunchTarget = {}): Promise<void> {
	const resolvedTarget = await resolvePracticeEntryTarget(target)
	persistPracticeLaunchTarget(resolvedTarget)
	return new Promise((resolve, reject) => {
		uni.switchTab({
			url: PRACTICE_TAB_URL,
			success: () => resolve(),
			fail: (error: unknown) => {
				uni.removeStorageSync(PRACTICE_LAUNCH_TARGET_KEY)
				reject(error)
			}
		})
	})
}

// AI 索引: 首页开始刷题、题库列表等入口使用普通题库练习，不继承错题或收藏上下文。
export function openBankPracticeTab(target: KyzzPracticeLaunchTarget = {}): Promise<void> {
	return openPracticeTab({
		...target,
		sourceType: 'bank',
		sourceStatus: null,
		keyword: null
	})
}
