import type { KyyyHomeDailyWordResponse, KyyyHomeDailyWordState } from '@/pages/kyyy/home/types'
import { normalizeDailyWord } from '@/pages/kyyy/home/view'

// AI 索引: KYYY 首页每日一词本地缓存。

const CACHE_KEY = 'kyyy_home_daily_words'

function normalizeCache(value: unknown): KyyyHomeDailyWordState[] | null {
	if (!Array.isArray(value)) {
		return null
	}
	const words = value
		.filter((item): item is KyyyHomeDailyWordResponse => {
			if (!item || typeof item !== 'object' || Array.isArray(item)) {
				return false
			}
			const wordText = (item as { wordText?: unknown }).wordText
			return typeof wordText === 'string' && wordText.trim().length > 0
		})
		.map((item) => normalizeDailyWord(item))
	if (!words.length) {
		return null
	}
	return words
}

export function readCachedDailyWords(): KyyyHomeDailyWordState[] | null {
	try {
		return normalizeCache(uni.getStorageSync(CACHE_KEY))
	} catch (error) {
		return null
	}
}

export function cacheDailyWords(words: KyyyHomeDailyWordState[]): void {
	try {
		const normalizedWords = (Array.isArray(words) ? words : [])
			.map((item) => normalizeDailyWord(item))
			.filter((item) => !!item.wordText)
		uni.setStorageSync(CACHE_KEY, normalizedWords)
	} catch (error) {
		console.warn('[kyyy-home] cache daily word failed', error)
	}
}
