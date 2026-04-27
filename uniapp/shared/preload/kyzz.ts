// AI 索引: KYZZ 小程序学习与题库首屏数据预加载。

import { getSessionSnapshot } from '@/shared/session/session'
import { getMineQuestionBanks } from '@/pages/kyzz/api/question-bank'
import { getPracticeAnswerPreview, getPracticeDashboard, getPracticeSession } from '@/pages/kyzz/api/practice'
import type { KyzzQuestionBankMineResponse } from '@/pages/kyzz/question-bank/types'
import type {
	KyzzPracticeAnswerPreviewResponse,
	KyzzPracticeDashboardResponse,
	KyzzPracticeSessionQuery,
	KyzzPracticeSessionResponse
} from '@/pages/kyzz/practice/types'
import { getPreloadCache, invalidatePreloadCache, preloadCache, setPreloadCache } from '@/shared/preload/cache'

const PRACTICE_DASHBOARD_KEY = 'kyzz:practice-dashboard'
const MINE_QUESTION_BANKS_KEY = 'kyzz:mine-question-banks'
const CORE_TTL_MS = 30 * 1000
const PRACTICE_DETAIL_TTL_MS = 2 * 60 * 1000
const kyzzPreloadKeys = new Set<string>([
	PRACTICE_DASHBOARD_KEY,
	MINE_QUESTION_BANKS_KEY
])

interface UserKeyShape {
	id?: string | number | null
	userId?: string | number | null
	openid?: string | number | null
	openId?: string | number | null
	unionId?: string | number | null
}

function canPreloadAuthedData(): boolean {
	return getSessionSnapshot().isAuthenticated
}

function reportPreloadError(scope: string, error: unknown): void {
	console.warn(`[preload:kyzz] ${scope} skipped`, error)
}

function resolveCurrentUserKey(): string {
	const currentUser = getSessionSnapshot<UserKeyShape>().currentUser
	const userKey = currentUser?.id ?? currentUser?.userId ?? currentUser?.openid ?? currentUser?.openId ?? currentUser?.unionId
	return userKey === null || userKey === undefined || userKey === '' ? 'anonymous' : String(userKey)
}

function normalizeQueryValue(value: unknown): string {
	if (value === null || value === undefined || value === '') {
		return ''
	}
	return String(value)
}

function trackKyzzPreloadKey(key: string): string {
	kyzzPreloadKeys.add(key)
	return key
}

function practiceSessionKey(query: KyzzPracticeSessionQuery = {}): string {
	return trackKyzzPreloadKey([
		'kyzz:practice-session',
		resolveCurrentUserKey(),
		`bank:${normalizeQueryValue(query.bankId)}`,
		`question:${normalizeQueryValue(query.questionId)}`,
		`fresh:${normalizeQueryValue(query.freshAttempt)}`,
		`source:${normalizeQueryValue(query.sourceType)}`,
		`status:${normalizeQueryValue(query.sourceStatus)}`,
		`keyword:${normalizeQueryValue(query.keyword)}`
	].join(':'))
}

function practiceAnswerPreviewKey(questionId: number, bankId: number): string {
	return trackKyzzPreloadKey([
		'kyzz:practice-answer-preview',
		resolveCurrentUserKey(),
		`bank:${normalizeQueryValue(bankId)}`,
		`question:${normalizeQueryValue(questionId)}`
	].join(':'))
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

export function preloadPracticeSession(query: KyzzPracticeSessionQuery = {}, options: { force?: boolean } = {}): Promise<KyzzPracticeSessionResponse> {
	return preloadCache<KyzzPracticeSessionResponse>(practiceSessionKey(query), () => getPracticeSession(query), {
		force: options.force,
		ttlMs: PRACTICE_DETAIL_TTL_MS
	})
}

export function getCachedPracticeSession(query: KyzzPracticeSessionQuery = {}): KyzzPracticeSessionResponse | null {
	return getPreloadCache<KyzzPracticeSessionResponse>(practiceSessionKey(query))
}

export function setCachedPracticeSession(query: KyzzPracticeSessionQuery, data: KyzzPracticeSessionResponse): KyzzPracticeSessionResponse {
	return setPreloadCache<KyzzPracticeSessionResponse>(practiceSessionKey(query), data, {
		ttlMs: PRACTICE_DETAIL_TTL_MS
	})
}

export function preloadPracticeAnswerPreview(
	questionId: number,
	bankId: number,
	options: { force?: boolean } = {}
): Promise<KyzzPracticeAnswerPreviewResponse> {
	return preloadCache<KyzzPracticeAnswerPreviewResponse>(practiceAnswerPreviewKey(questionId, bankId), () => getPracticeAnswerPreview(questionId, bankId), {
		force: options.force,
		ttlMs: PRACTICE_DETAIL_TTL_MS
	})
}

export function getCachedPracticeAnswerPreview(questionId: number, bankId: number): KyzzPracticeAnswerPreviewResponse | null {
	return getPreloadCache<KyzzPracticeAnswerPreviewResponse>(practiceAnswerPreviewKey(questionId, bankId))
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

export function invalidateKyzzPreload(): void {
	kyzzPreloadKeys.forEach((key) => invalidatePreloadCache(key))
}
