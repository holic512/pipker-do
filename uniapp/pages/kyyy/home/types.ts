// AI 索引: KYYY 小程序首页类型约束。

export interface KyyyHomeDashboardResponse {
	studyCount?: number | string | null
	reviewCount?: number | string | null
}

export interface KyyyHomeDashboardState {
	studyCount: number
	reviewCount: number
	loaded: boolean
}

export interface KyyyHomeDailyWordSourceBank {
	id?: number | string | null
	bankCode?: string | null
	bankName?: string | null
}

export interface KyyyHomeDailyWordResponse {
	wordId?: number | string | null
	wordText?: string | null
	phoneticUs?: string | null
	phoneticUk?: string | null
	partOfSpeech?: string | null
	meaningCn?: string | null
	exampleSentence?: string | null
	exampleTranslation?: string | null
	sourceBankName?: string | null
	sourceBanks?: KyyyHomeDailyWordSourceBank[] | null
}

export interface KyyyHomeDailyWordState {
	wordId: number | null
	wordText: string
	phoneticUs: string
	phoneticUk: string
	partOfSpeech: string
	meaningCn: string
	exampleSentence: string
	exampleTranslation: string
	sourceBankName: string
	loaded: boolean
}

export interface KyyyHomeDailyWordCache {
	userKey: string
	dateKey: string
	word: KyyyHomeDailyWordState
}

export type KyyyPracticeEntryMode = 'default' | 'study' | 'review'

export type KyyyHomeShortcutNavigationType = 'reLaunch' | 'navigateTo'

export interface KyyyHomeShortcutItem {
	key: string
	title: string
	description: string
	icon: string
	iconColor: string
	iconBackground: string
	path: string
	navigationType: KyyyHomeShortcutNavigationType
}
