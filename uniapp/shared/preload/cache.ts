// AI 索引: 小程序通用预加载缓存，复用首屏与 tab 预测请求结果。

export interface PreloadCacheOptions {
	ttlMs?: number
	force?: boolean
}

interface PreloadCacheEntry<T> {
	data: T | null
	promise: Promise<T> | null
	updatedAt: number
	expiresAt: number
}

const DEFAULT_TTL_MS = 30 * 1000
const entries = new Map<string, PreloadCacheEntry<unknown>>()

function now(): number {
	return Date.now()
}

function createEntry<T>(): PreloadCacheEntry<T> {
	return {
		data: null,
		promise: null,
		updatedAt: 0,
		expiresAt: 0
	}
}

function getEntry<T>(key: string): PreloadCacheEntry<T> {
	const currentEntry = entries.get(key) as PreloadCacheEntry<T> | undefined
	if (currentEntry) {
		return currentEntry
	}
	const nextEntry = createEntry<T>()
	entries.set(key, nextEntry as PreloadCacheEntry<unknown>)
	return nextEntry
}

export function getPreloadCache<T>(key: string, options: { allowStale?: boolean } = {}): T | null {
	const entry = entries.get(key) as PreloadCacheEntry<T> | undefined
	if (!entry || entry.data === null) {
		return null
	}
	if (!options.allowStale && entry.expiresAt <= now()) {
		return null
	}
	return entry.data
}

export function setPreloadCache<T>(key: string, data: T, options: Pick<PreloadCacheOptions, 'ttlMs'> = {}): T {
	const entry = getEntry<T>(key)
	const timestamp = now()
	entry.data = data
	entry.updatedAt = timestamp
	entry.expiresAt = timestamp + (options.ttlMs ?? DEFAULT_TTL_MS)
	entry.promise = null
	return data
}

export function preloadCache<T>(key: string, loader: () => Promise<T>, options: PreloadCacheOptions = {}): Promise<T> {
	const entry = getEntry<T>(key)
	const timestamp = now()
	if (!options.force && entry.data !== null && entry.expiresAt > timestamp) {
		return Promise.resolve(entry.data)
	}
	if (!options.force && entry.promise) {
		return entry.promise
	}
	const promise = loader()
		.then((data) => setPreloadCache(key, data, options))
		.finally(() => {
			const latestEntry = entries.get(key) as PreloadCacheEntry<T> | undefined
			if (latestEntry?.promise === promise) {
				latestEntry.promise = null
			}
		})
	entry.promise = promise
	return promise
}

export function invalidatePreloadCache(key?: string): void {
	if (!key) {
		entries.clear()
		return
	}
	entries.delete(key)
}
