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

export interface KyyyHomeDailyWordResponse {
	wordId?: number | string | null
	wordText?: string | null
	partOfSpeech?: string | null
	meaningCn?: string | null
}

export interface KyyyHomeDailyWordState {
	wordId: number | null
	wordText: string
	partOfSpeech: string
	meaningCn: string
	loaded: boolean
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
