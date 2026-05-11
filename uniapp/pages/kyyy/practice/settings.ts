import { getPracticeSettings, updatePracticeSettings } from '@/pages/kyyy/api/practice'
import type { KyyyPracticeSettingRequest, KyyyPracticeSettingState } from '@/pages/kyyy/practice/types'
import { createDefaultPracticeSettings, normalizeExamDirection, normalizePracticeSettings, resolveExamDirectionLabel } from '@/pages/kyyy/practice/view'

// AI 索引: KYYY 刷题设置本地缓存与后端同步辅助。

const CACHE_KEY = 'kyyy_practice_settings'

interface PracticeSettingsCache {
	examDirection?: string
	examDirectionLabel?: string
	defaultWordBankId?: number | null
	defaultWordBankName?: string | null
}

function normalizeCachedSettings(value: unknown): KyyyPracticeSettingState {
	if (!value || typeof value !== 'object') {
		return createDefaultPracticeSettings()
	}
	const cache = value as PracticeSettingsCache
	const examDirection = normalizeExamDirection(cache.examDirection)
	const defaultWordBankId = cache.defaultWordBankId === null || cache.defaultWordBankId === undefined
		? null
		: Number(cache.defaultWordBankId)
	return {
		...createDefaultPracticeSettings(),
		examDirection,
		examDirectionLabel: cache.examDirectionLabel || resolveExamDirectionLabel(examDirection),
		defaultWordBankId: defaultWordBankId !== null && Number.isFinite(defaultWordBankId) && defaultWordBankId > 0 ? defaultWordBankId : null,
		defaultWordBankName: typeof cache.defaultWordBankName === 'string' ? cache.defaultWordBankName : '',
		loaded: true
	}
}

export function readCachedPracticeSettings(): KyyyPracticeSettingState {
	try {
		return normalizeCachedSettings(uni.getStorageSync(CACHE_KEY))
	} catch (error) {
		return createDefaultPracticeSettings()
	}
}

export function cachePracticeSettings(settings: Pick<KyyyPracticeSettingState, 'examDirection' | 'examDirectionLabel' | 'defaultWordBankId' | 'defaultWordBankName'>): void {
	try {
		uni.setStorageSync(CACHE_KEY, {
			examDirection: normalizeExamDirection(settings.examDirection),
			examDirectionLabel: settings.examDirectionLabel || resolveExamDirectionLabel(normalizeExamDirection(settings.examDirection)),
			defaultWordBankId: settings.defaultWordBankId === null || settings.defaultWordBankId === undefined ? null : Number(settings.defaultWordBankId),
			defaultWordBankName: settings.defaultWordBankName || ''
		})
	} catch (error) {
		console.warn('[kyyy-practice-settings] cache failed', error)
	}
}

export async function loadPracticeSettingsWithFallback(): Promise<KyyyPracticeSettingState> {
	try {
		const settings = normalizePracticeSettings(await getPracticeSettings())
		cachePracticeSettings(settings)
		return settings
	} catch (error) {
		return {
			...readCachedPracticeSettings(),
			loaded: false,
			syncing: false
		}
	}
}

export async function syncPracticeSettings(request: KyyyPracticeSettingRequest): Promise<KyyyPracticeSettingState> {
	const settings = normalizePracticeSettings(await updatePracticeSettings({
		examDirection: request.examDirection ? normalizeExamDirection(request.examDirection) : undefined,
		defaultWordBankId: request.defaultWordBankId === null || request.defaultWordBankId === undefined
			? undefined
			: Number(request.defaultWordBankId)
	}))
	cachePracticeSettings(settings)
	return settings
}
