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
	const words = Array.isArray(cache.words)
		? cache.words.map((item) => normalizeDailyWord(item)).filter(Boolean)
		: cache.word
			? [normalizeDailyWord(cache.word)]
			: []
	return {
		userKey: cache.userKey,
		dateKey: cache.dateKey,
		words,
		word: words[0] || null
	}
}

export function readTodayCachedDailyWords(): KyyyHomeDailyWordState[] | null {
	try {
		const cache = normalizeCache(uni.getStorageSync(CACHE_KEY))
		if (!cache) {
			return null
		}
		if (cache.userKey !== resolveCurrentUserKey() || cache.dateKey !== resolveLocalDateKey()) {
			return null
		}
		if (Array.isArray(cache.words) && cache.words.length > 0) {
			return cache.words.map((item) => normalizeDailyWord(item))
		}
		if (cache.word) {
			return [normalizeDailyWord(cache.word)]
		}
		return null
	} catch (error) {
		return null
	}
}

export function readTodayCachedDailyWord(): KyyyHomeDailyWordState | null {
	return readTodayCachedDailyWords()?.[0] || null
}

export function cacheTodayDailyWords(words: KyyyHomeDailyWordState[]): void {
	try {
		const normalizedWords = (Array.isArray(words) ? words : [])
			.map((item) => normalizeDailyWord(item))
			.filter(Boolean)
		uni.setStorageSync(CACHE_KEY, {
			userKey: resolveCurrentUserKey(),
			dateKey: resolveLocalDateKey(),
			words: normalizedWords,
			word: normalizedWords[0] || null
		} as KyyyHomeDailyWordCache)
	} catch (error) {
		console.warn('[kyyy-home] cache daily word failed', error)
	}
}

export function cacheTodayDailyWord(word: KyyyHomeDailyWordState): void {
	cacheTodayDailyWords([word])
}
