import { getPracticeSettings, updatePracticeSettings } from '@/pages/kyzz/api/practice'
import type { KyzzPracticeSettingRequest, KyzzPracticeSettingState } from '@/pages/kyzz/practice/types'
import { createDefaultPracticeSettings, normalizePracticeSettings, toBoolean } from '@/pages/kyzz/practice/view'

// AI 索引: KYZZ 刷题设置本地缓存与后端同步辅助。

const PRACTICE_SETTINGS_CACHE_KEY = 'KYZZ_PRACTICE_SETTINGS'

interface PracticeSettingsCache {
	autoJumpOnCorrect?: boolean | number | string | null
	bankPracticeChoiceOnly?: boolean | number | string | null
}

function normalizeCachedSettings(value: unknown): KyzzPracticeSettingState {
	if (!value || typeof value !== 'object' || Array.isArray(value)) {
		return createDefaultPracticeSettings()
	}
	const cache = value as PracticeSettingsCache
	return {
		autoJumpOnCorrect: toBoolean(cache.autoJumpOnCorrect, true),
		bankPracticeChoiceOnly: toBoolean(cache.bankPracticeChoiceOnly, false),
		loaded: true,
		syncing: false
	}
}

export function readCachedPracticeSettings(): KyzzPracticeSettingState {
	try {
		return normalizeCachedSettings(uni.getStorageSync(PRACTICE_SETTINGS_CACHE_KEY))
	} catch (error) {
		return createDefaultPracticeSettings()
	}
}

export function cachePracticeSettings(settings: Pick<KyzzPracticeSettingState, 'autoJumpOnCorrect' | 'bankPracticeChoiceOnly'>): void {
	try {
		uni.setStorageSync(PRACTICE_SETTINGS_CACHE_KEY, {
			autoJumpOnCorrect: Boolean(settings.autoJumpOnCorrect),
			bankPracticeChoiceOnly: Boolean(settings.bankPracticeChoiceOnly)
		})
	} catch (error) {
		console.warn('[practice-settings] cache failed', error)
	}
}

export async function loadPracticeSettingsWithFallback(): Promise<KyzzPracticeSettingState> {
	try {
		const settings = normalizePracticeSettings(await getPracticeSettings())
		cachePracticeSettings(settings)
		return settings
	} catch (error) {
		return {
			...readCachedPracticeSettings(),
			loaded: true,
			syncing: false
		}
	}
}

export async function syncPracticeSettings(request: KyzzPracticeSettingRequest): Promise<KyzzPracticeSettingState> {
	const settings = normalizePracticeSettings(await updatePracticeSettings(request))
	cachePracticeSettings(settings)
	return settings
}
