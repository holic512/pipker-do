import { getSessionSnapshot } from '@/shared/session/session'
import type { KyyyHomeDailyWordCache, KyyyHomeDailyWordState } from '@/pages/kyyy/home/types'
import { normalizeDailyWord } from '@/pages/kyyy/home/view'

// AI 索引: KYYY 首页每日一词本地缓存。

const CACHE_KEY = 'kyyy_home_daily_word'

interface UserKeyShape {
	id?: string | number | null
	userId?: string | number | null
	openid?: string | number | null
	openId?: string | number | null
	unionId?: string | number | null
}

function resolveCurrentUserKey(): string {
	const currentUser = getSessionSnapshot<UserKeyShape>().currentUser
	const userKey = currentUser?.id ?? currentUser?.userId ?? currentUser?.openid ?? currentUser?.openId ?? currentUser?.unionId
	return userKey === null || userKey === undefined || userKey === '' ? 'anonymous' : String(userKey)
}

function resolveLocalDateKey(): string {
	const now = new Date()
	const year = now.getFullYear()
	const month = `${now.getMonth() + 1}`.padStart(2, '0')
	const day = `${now.getDate()}`.padStart(2, '0')
	return `${year}-${month}-${day}`
}

function normalizeCache(value: unknown): KyyyHomeDailyWordCache | null {
	if (!value || typeof value !== 'object' || Array.isArray(value)) {
		return null
	}
	const cache = value as KyyyHomeDailyWordCache
	if (typeof cache.userKey !== 'string' || typeof cache.dateKey !== 'string') {
		return null
	}
	return {
		userKey: cache.userKey,
		dateKey: cache.dateKey,
		word: normalizeDailyWord(cache.word)
	}
}

export function readTodayCachedDailyWord(): KyyyHomeDailyWordState | null {
	try {
		const cache = normalizeCache(uni.getStorageSync(CACHE_KEY))
		if (!cache) {
			return null
		}
		if (cache.userKey !== resolveCurrentUserKey() || cache.dateKey !== resolveLocalDateKey()) {
			return null
		}
		return normalizeDailyWord(cache.word)
	} catch (error) {
		return null
	}
}

export function cacheTodayDailyWord(word: KyyyHomeDailyWordState): void {
	try {
		uni.setStorageSync(CACHE_KEY, {
			userKey: resolveCurrentUserKey(),
			dateKey: resolveLocalDateKey(),
			word: normalizeDailyWord(word)
		} as KyyyHomeDailyWordCache)
	} catch (error) {
		console.warn('[kyyy-home] cache daily word failed', error)
	}
}
