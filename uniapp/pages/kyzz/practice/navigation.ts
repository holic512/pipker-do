import type { KyzzPracticeLaunchTarget } from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序刷题路由跳转辅助。

const PRACTICE_TAB_URL = '/pages/kyzz/practice/index'
const PRACTICE_LAUNCH_TARGET_KEY = 'kyzz.practice.launch.target'

function normalizeOptionalNumber(value: unknown): number | null {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : null
}

export function persistPracticeLaunchTarget(target: KyzzPracticeLaunchTarget = {}): void {
	const normalizedTarget: KyzzPracticeLaunchTarget = {
		bankId: normalizeOptionalNumber(target.bankId),
		questionId: normalizeOptionalNumber(target.questionId)
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
		questionId: normalizeOptionalNumber((cachedTarget as KyzzPracticeLaunchTarget).questionId)
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
