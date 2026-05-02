import { getPracticeSettings, updatePracticeSettings } from '@/pages/kyyy/api/practice'
import type { KyyyPracticeSettingRequest, KyyyPracticeSettingState } from '@/pages/kyyy/practice/types'
import { createDefaultPracticeSettings, normalizeExamDirection, normalizePracticeSettings, resolveExamDirectionLabel } from '@/pages/kyyy/practice/view'

// AI 索引: KYYY 刷题设置本地缓存与后端同步辅助。

const CACHE_KEY = 'kyyy_practice_settings'

interface PracticeSettingsCache {
	examDirection?: string
	examDirectionLabel?: string
}

function normalizeCachedSettings(value: unknown): KyyyPracticeSettingState {
	if (!value || typeof value !== 'object') {
		return createDefaultPracticeSettings()
	}
	const cache = value as PracticeSettingsCache
	const examDirection = normalizeExamDirection(cache.examDirection)
	return {
		...createDefaultPracticeSettings(),
		examDirection,
		examDirectionLabel: cache.examDirectionLabel || resolveExamDirectionLabel(examDirection),
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

export function cachePracticeSettings(settings: Pick<KyyyPracticeSettingState, 'examDirection' | 'examDirectionLabel'>): void {
	try {
		uni.setStorageSync(CACHE_KEY, {
			examDirection: normalizeExamDirection(settings.examDirection),
			examDirectionLabel: settings.examDirectionLabel || resolveExamDirectionLabel(normalizeExamDirection(settings.examDirection))
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
		examDirection: request.examDirection ? normalizeExamDirection(request.examDirection) : undefined
	}))
	cachePracticeSettings(settings)
	return settings
}
