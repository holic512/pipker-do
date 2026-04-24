// AI 索引: KYZZ 小程序学习与题库首屏数据预加载。

import { getSessionSnapshot } from '@/shared/session/session'
import { getMineQuestionBanks } from '@/pages/kyzz/api/question-bank'
import { getPracticeDashboard } from '@/pages/kyzz/api/practice'
import type { KyzzQuestionBankMineResponse } from '@/pages/kyzz/question-bank/types'
import type { KyzzPracticeDashboardResponse } from '@/pages/kyzz/practice/types'
import { getPreloadCache, invalidatePreloadCache, preloadCache } from '@/shared/preload/cache'

const PRACTICE_DASHBOARD_KEY = 'kyzz:practice-dashboard'
const MINE_QUESTION_BANKS_KEY = 'kyzz:mine-question-banks'
const CORE_TTL_MS = 30 * 1000

function canPreloadAuthedData(): boolean {
	return getSessionSnapshot().isAuthenticated
}

function reportPreloadError(scope: string, error: unknown): void {
	console.warn(`[preload:kyzz] ${scope} skipped`, error)
}

export function preloadPracticeDashboard(options: { force?: boolean } = {}): Promise<KyzzPracticeDashboardResponse> {
	return preloadCache<KyzzPracticeDashboardResponse>(PRACTICE_DASHBOARD_KEY, getPracticeDashboard, {
		force: options.force,
		ttlMs: CORE_TTL_MS
	})
}

export function preloadMineQuestionBanks(options: { force?: boolean } = {}): Promise<KyzzQuestionBankMineResponse> {
	return preloadCache<KyzzQuestionBankMineResponse>(MINE_QUESTION_BANKS_KEY, getMineQuestionBanks, {
		force: options.force,
		ttlMs: CORE_TTL_MS
	})
}

export function getCachedPracticeDashboard(): KyzzPracticeDashboardResponse | null {
	return getPreloadCache<KyzzPracticeDashboardResponse>(PRACTICE_DASHBOARD_KEY)
}

export function getCachedMineQuestionBanks(): KyzzQuestionBankMineResponse | null {
	return getPreloadCache<KyzzQuestionBankMineResponse>(MINE_QUESTION_BANKS_KEY)
}

export function warmKyzzCorePreload(): void {
	if (!canPreloadAuthedData()) {
		return
	}
	preloadPracticeDashboard().catch((error) => reportPreloadError('practice dashboard', error))
	preloadMineQuestionBanks().catch((error) => reportPreloadError('mine question banks', error))
}

export function invalidateKyzzCorePreload(): void {
	invalidatePreloadCache(PRACTICE_DASHBOARD_KEY)
	invalidatePreloadCache(MINE_QUESTION_BANKS_KEY)
}
