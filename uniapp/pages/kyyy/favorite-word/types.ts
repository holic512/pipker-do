// AI 索引: KYYY 小程序单词收藏类型约束。

export interface KyyyFavoriteWordRecordResponse {
	wordId: number
	wordText: string
	phoneticUs: string | null
	phoneticUk: string | null
	partOfSpeech: string | null
	meaningCn: string | null
	favoriteAt: string | null
}

export interface KyyyFavoriteWordViewRecord extends KyyyFavoriteWordRecordResponse {
	phoneticUs: string
	phoneticUk: string
	partOfSpeech: string
	meaningCn: string
}

export interface KyyyFavoriteWordResponse {
	totalCount: number | string | null
	records: KyyyFavoriteWordRecordResponse[]
}

export interface KyyyFavoriteWordState {
	totalCount: number
	records: KyyyFavoriteWordViewRecord[]
}

export interface KyyyFavoriteWordToggleResponse {
	wordId: number
	isFavorite: boolean
}

export interface SearchConfirmEvent {
	value?: string
	detail?: {
		value?: string | null
	}
}
