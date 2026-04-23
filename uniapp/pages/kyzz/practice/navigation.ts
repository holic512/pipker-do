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

export function persistPracticeLaunchTarget(target: KyzzPracticeLaunchTarget = {}): void {
	const normalizedTarget: KyzzPracticeLaunchTarget = {
		bankId: normalizeOptionalNumber(target.bankId),
		questionId: normalizeOptionalNumber(target.questionId),
		freshAttempt: normalizeOptionalBoolean(target.freshAttempt)
	}
	if (normalizedTarget.bankId === null && normalizedTarget.questionId === null) {
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
		freshAttempt: normalizeOptionalBoolean((cachedTarget as KyzzPracticeLaunchTarget).freshAttempt)
	}
}

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
