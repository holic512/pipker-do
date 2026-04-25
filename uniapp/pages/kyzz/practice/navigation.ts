import type { KyzzPracticeLaunchTarget } from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序刷题路由跳转辅助。

const PRACTICE_TAB_URL = '/pages/kyzz/practice/index'
const PRACTICE_LAUNCH_TARGET_KEY = 'kyzz.practice.launch.target'

function normalizeOptionalNumber(value: unknown): number | null {
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
	if (value === 'bank' || value === 'wrong_book' || value === 'favorite') {
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

export function persistPracticeLaunchTarget(target: KyzzPracticeLaunchTarget = {}): void {
	const normalizedTarget: KyzzPracticeLaunchTarget = {
		bankId: normalizeOptionalNumber(target.bankId),
		questionId: normalizeOptionalNumber(target.questionId),
		freshAttempt: normalizeOptionalBoolean(target.freshAttempt),
		sourceType: normalizeSourceType(target.sourceType),
		sourceStatus: normalizeSourceStatus(target.sourceStatus),
		keyword: normalizeOptionalString(target.keyword)
	}
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

// AI 索引: 不带 target 时仅切回练习 tab，保留当前普通/错题/收藏练习上下文。
export function openPracticeTab(target: KyzzPracticeLaunchTarget = {}): Promise<void> {
	persistPracticeLaunchTarget(target)
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
