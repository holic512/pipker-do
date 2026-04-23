// AI 索引: 小程序反扒请求头、本地冷却与风险响应辅助。

import { getDeviceId, getOrCreateDeviceProfile } from '@/shared/platform/device'

const REQUEST_COOLDOWN_KEY = 'PIPKER_REQUEST_COOLDOWNS'

export type AntiCrawlerAction = 'cooldown' | 'blocked'

export interface AntiCrawlerResponsePayload {
	action: AntiCrawlerAction
	retryAfterSeconds: number
	ruleCode: string | null
}

interface AntiCrawlerCooldownRecord {
	action: AntiCrawlerAction
	ruleCode: string | null
	expiresAt: number
}

export interface AntiCrawlerCooldownState extends AntiCrawlerResponsePayload {
	expiresAt: number
}

let cachedCooldownMap: Record<string, AntiCrawlerCooldownRecord> | null = null

function isRecord(value: unknown): value is Record<string, unknown> {
	return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function createRequestId(): string {
	return `req_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 10)}`
}

function normalizeCooldownRecord(value: unknown): AntiCrawlerCooldownRecord | null {
	if (!isRecord(value)) {
		return null
	}
	const action = value.action
	const expiresAt = Number(value.expiresAt)
	if ((action !== 'cooldown' && action !== 'blocked') || !Number.isFinite(expiresAt) || expiresAt <= 0) {
		return null
	}
	return {
		action,
		ruleCode: typeof value.ruleCode === 'string' ? value.ruleCode : null,
		expiresAt
	}
}

function loadCooldownMap(): Record<string, AntiCrawlerCooldownRecord> {
	if (cachedCooldownMap) {
		return cachedCooldownMap
	}
	const storedValue = uni.getStorageSync(REQUEST_COOLDOWN_KEY)
	const resolvedMap: Record<string, AntiCrawlerCooldownRecord> = {}
	if (isRecord(storedValue)) {
		Object.keys(storedValue).forEach((key) => {
			const normalizedRecord = normalizeCooldownRecord(storedValue[key])
			if (normalizedRecord) {
				resolvedMap[key] = normalizedRecord
			}
		})
	}
	cachedCooldownMap = resolvedMap
	return cachedCooldownMap
}

function persistCooldownMap(): void {
	uni.setStorageSync(REQUEST_COOLDOWN_KEY, cachedCooldownMap || {})
}

function cleanupExpiredCooldowns(): void {
	const cooldownMap = loadCooldownMap()
	const now = Date.now()
	let changed = false
	Object.keys(cooldownMap).forEach((key) => {
		const item = cooldownMap[key]
		if (!item || !item.expiresAt || item.expiresAt <= now) {
			delete cooldownMap[key]
			changed = true
		}
	})
	if (changed) {
		persistCooldownMap()
	}
}

export function normalizeAntiCrawlerRoute(url: string): string {
	if (!url) {
		return '/'
	}
	const value = String(url)
	const protocolIndex = value.indexOf('://')
	let path = value
	if (protocolIndex >= 0) {
		const firstSlashAfterHost = value.indexOf('/', protocolIndex + 3)
		path = firstSlashAfterHost >= 0 ? value.slice(firstSlashAfterHost) : '/'
	}
	const queryIndex = path.indexOf('?')
	return queryIndex >= 0 ? path.slice(0, queryIndex) : path
}

function buildCooldownKey(url: string): string {
	return `${getDeviceId()}::${normalizeAntiCrawlerRoute(url)}`
}

export function buildAntiCrawlerHeaders(header: Record<string, string> = {}): Record<string, string> {
	const deviceProfile = getOrCreateDeviceProfile()
	return {
		...header,
		'X-Request-Id': createRequestId(),
		'X-Device-Id': deviceProfile.deviceId,
		'X-Client-Platform': deviceProfile.platform || 'unknown',
		'X-Client-Version': deviceProfile.appVersion || 'unknown'
	}
}

export function getActiveCooldown(url: string): AntiCrawlerCooldownState | null {
	cleanupExpiredCooldowns()
	const cooldownItem = loadCooldownMap()[buildCooldownKey(url)]
	if (!cooldownItem || !cooldownItem.expiresAt) {
		return null
	}
	const remainingMilliseconds = cooldownItem.expiresAt - Date.now()
	if (remainingMilliseconds <= 0) {
		return null
	}
	return {
		...cooldownItem,
		retryAfterSeconds: Math.max(1, Math.ceil(remainingMilliseconds / 1000))
	}
}

export function saveRiskCooldown(url: string, payload: AntiCrawlerResponsePayload | null): void {
	if (!payload || !payload.retryAfterSeconds || payload.retryAfterSeconds <= 0) {
		return
	}
	const cooldownMap = loadCooldownMap()
	cooldownMap[buildCooldownKey(url)] = {
		action: payload.action || 'cooldown',
		ruleCode: payload.ruleCode || null,
		expiresAt: Date.now() + Number(payload.retryAfterSeconds) * 1000
	}
	persistCooldownMap()
}

export function resolveRiskPayload(responseStatusCode: number, payload: unknown): AntiCrawlerResponsePayload | null {
	if (!isRecord(payload)) {
		return null
	}
	const businessCode = payload.code
	const riskyStatus = responseStatusCode === 429 || responseStatusCode === 403
	const riskyCode = businessCode === 429 || businessCode === 430
	if (!(riskyStatus || riskyCode) || !isRecord(payload.data)) {
		return null
	}
	const action = payload.data.action
	const retryAfterSeconds = Number(payload.data.retryAfterSeconds)
	if ((action === 'cooldown' || action === 'blocked') && Number.isFinite(retryAfterSeconds) && retryAfterSeconds > 0) {
		return {
			action,
			retryAfterSeconds,
			ruleCode: typeof payload.data.ruleCode === 'string' ? payload.data.ruleCode : null
		}
	}
	return null
}

export function buildRiskMessage(
	payload: Pick<AntiCrawlerResponsePayload, 'action'> | null,
	fallback = '请求过于频繁，请稍后再试'
): string {
	if (payload && payload.action === 'blocked') {
		return '访问行为异常，已暂时限制访问'
	}
	return fallback
}
