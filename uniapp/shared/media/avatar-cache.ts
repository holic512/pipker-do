// AI 索引: 小程序用户头像本地文件缓存，避免我的页二次进入远程头像白块。

const AVATAR_CACHE_KEY = 'PIPKER_AVATAR_CACHE_V1'
const DEFAULT_USER_KEY = 'current'

interface AvatarCacheRecord {
	userKey: string
	remoteUrl: string
	localPath: string
	updatedAt: number
}

type AvatarCacheStore = Record<string, AvatarCacheRecord>

type UserLike = {
	id?: string | number | null
	userId?: string | number | null
	openid?: string | null
	openId?: string | null
	unionId?: string | null
	avatarUrl?: string | null
}

const inflightTasks = new Map<string, Promise<string>>()

function readStore(): AvatarCacheStore {
	const cached = uni.getStorageSync(AVATAR_CACHE_KEY)
	return cached && typeof cached === 'object' ? (cached as AvatarCacheStore) : {}
}

function writeStore(store: AvatarCacheStore): void {
	uni.setStorageSync(AVATAR_CACHE_KEY, store)
}

function normalizeUrl(url: unknown): string {
	return typeof url === 'string' ? url.trim() : ''
}

function isLocalPath(path: string): boolean {
	return /^(wxfile|ttfile|ksfile|qfile|file|unifile):\/\//.test(path) || path.startsWith('_doc/') || path.startsWith('http://tmp/') || path.startsWith('https://tmp/')
}

function buildCacheKey(userKey: string): string {
	return userKey || DEFAULT_USER_KEY
}

function downloadFile(url: string): Promise<string> {
	return new Promise((resolve, reject) => {
		uni.downloadFile({
			url,
			success: (result: { statusCode?: number; tempFilePath?: string }) => {
				if (result.statusCode && (result.statusCode < 200 || result.statusCode >= 300)) {
					reject(new Error(`头像下载失败：${result.statusCode}`))
					return
				}
				if (!result.tempFilePath) {
					reject(new Error('头像下载失败：缺少临时文件'))
					return
				}
				resolve(result.tempFilePath)
			},
			fail: reject
		})
	})
}

function saveFile(tempFilePath: string): Promise<string> {
	return new Promise((resolve, reject) => {
		uni.saveFile({
			tempFilePath,
			success: (result: { savedFilePath?: string }) => {
				if (!result.savedFilePath) {
					reject(new Error('头像保存失败：缺少本地路径'))
					return
				}
				resolve(result.savedFilePath)
			},
			fail: reject
		})
	})
}

function removeSavedFile(filePath: string): void {
	if (!filePath || !isLocalPath(filePath)) {
		return
	}
	uni.removeSavedFile({
		filePath,
		fail: () => {}
	})
}

export function resolveAvatarUserKey(user: unknown): string {
	const currentUser = user && typeof user === 'object' ? (user as UserLike) : null
	const userKey = currentUser?.id ?? currentUser?.userId ?? currentUser?.openid ?? currentUser?.openId ?? currentUser?.unionId
	return userKey === undefined || userKey === null || userKey === '' ? DEFAULT_USER_KEY : String(userKey)
}

export function getCachedAvatarPath(remoteUrl: unknown, userKey = DEFAULT_USER_KEY): string {
	const normalizedUrl = normalizeUrl(remoteUrl)
	if (!normalizedUrl) {
		return ''
	}
	const record = readStore()[buildCacheKey(userKey)]
	if (!record || record.remoteUrl !== normalizedUrl || !record.localPath) {
		return ''
	}
	return record.localPath
}

export function getAnyCachedAvatarPath(userKey = DEFAULT_USER_KEY): string {
	const record = readStore()[buildCacheKey(userKey)]
	return record?.localPath || ''
}

export function resolveDisplayAvatarUrl(remoteUrl: unknown, userKey = DEFAULT_USER_KEY): string {
	const normalizedUrl = normalizeUrl(remoteUrl)
	if (!normalizedUrl) {
		return ''
	}
	return getCachedAvatarPath(normalizedUrl, userKey) || getAnyCachedAvatarPath(userKey) || normalizedUrl
}

export function resolveUserDisplayAvatarUrl(user: unknown): string {
	const currentUser = user && typeof user === 'object' ? (user as UserLike) : null
	return resolveDisplayAvatarUrl(currentUser?.avatarUrl, resolveAvatarUserKey(user))
}

export async function warmAvatarCache(remoteUrl: unknown, userKey = DEFAULT_USER_KEY): Promise<string> {
	const normalizedUrl = normalizeUrl(remoteUrl)
	if (!normalizedUrl) {
		return ''
	}
	const cacheKey = buildCacheKey(userKey)
	const cachedPath = getCachedAvatarPath(normalizedUrl, cacheKey)
	if (cachedPath) {
		return cachedPath
	}
	const inflightKey = `${cacheKey}::${normalizedUrl}`
	const inflightTask = inflightTasks.get(inflightKey)
	if (inflightTask) {
		return inflightTask
	}
	const task = (async () => {
		const store = readStore()
		const previousRecord = store[cacheKey]
		const tempFilePath = await downloadFile(normalizedUrl)
		const localPath = await saveFile(tempFilePath)
		store[cacheKey] = {
			userKey: cacheKey,
			remoteUrl: normalizedUrl,
			localPath,
			updatedAt: Date.now()
		}
		writeStore(store)
		if (previousRecord?.localPath && previousRecord.localPath !== localPath) {
			removeSavedFile(previousRecord.localPath)
		}
		return localPath
	})().finally(() => {
		inflightTasks.delete(inflightKey)
	})
	inflightTasks.set(inflightKey, task)
	return task
}

export function warmUserAvatarCache(user: unknown): Promise<string> {
	const currentUser = user && typeof user === 'object' ? (user as UserLike) : null
	return warmAvatarCache(currentUser?.avatarUrl, resolveAvatarUserKey(user))
}

export async function cacheLocalAvatar(remoteUrl: unknown, userKey: string, tempFilePath: string): Promise<string> {
	const normalizedUrl = normalizeUrl(remoteUrl)
	if (!normalizedUrl || !tempFilePath) {
		return ''
	}
	const cacheKey = buildCacheKey(userKey)
	const store = readStore()
	const previousRecord = store[cacheKey]
	const localPath = isLocalPath(tempFilePath) ? await saveFile(tempFilePath) : tempFilePath
	store[cacheKey] = {
		userKey: cacheKey,
		remoteUrl: normalizedUrl,
		localPath,
		updatedAt: Date.now()
	}
	writeStore(store)
	if (previousRecord?.localPath && previousRecord.localPath !== localPath) {
		removeSavedFile(previousRecord.localPath)
	}
	return localPath
}

export function clearAvatarCache(userKey?: string): void {
	const store = readStore()
	if (userKey) {
		const cacheKey = buildCacheKey(userKey)
		removeSavedFile(store[cacheKey]?.localPath || '')
		delete store[cacheKey]
		writeStore(store)
		return
	}
	Object.values(store).forEach((record) => removeSavedFile(record.localPath))
	uni.removeStorageSync(AVATAR_CACHE_KEY)
}
