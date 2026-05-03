import type {
	KyyyHomeDashboardResponse,
	KyyyHomeDashboardState,
	KyyyHomeDailyWordResponse,
	KyyyHomeDailyWordState
} from '@/pages/kyyy/home/types'

// AI 索引: KYYY 小程序首页展示态整理。

function toNumber(value: unknown, fallback = 0): number {
	const parsed = Number(value)
	return Number.isFinite(parsed) ? parsed : fallback
}

function normalizeText(value: unknown): string {
	return typeof value === 'string' ? value.trim() : ''
}

function isFallbackHelloWord(wordText: string): boolean {
	return wordText.trim().toLowerCase() === createHelloDailyWord().wordText
}

export function createEmptyHomeDashboard(): KyyyHomeDashboardState {
	return {
		studyCount: 0,
		reviewCount: 0,
		loaded: false
	}
}

export function normalizeHomeDashboard(result: KyyyHomeDashboardResponse | null | undefined): KyyyHomeDashboardState {
	return {
		studyCount: Math.max(toNumber(result?.studyCount), 0),
		reviewCount: Math.max(toNumber(result?.reviewCount), 0),
		loaded: true
	}
}

export function createHelloDailyWord(): KyyyHomeDailyWordState {
	return {
		wordId: null,
		wordText: 'hello',
		partOfSpeech: 'int.',
		meaningCn: '你好；喂',
		loaded: true
	}
}

export function createPendingDailyWord(): KyyyHomeDailyWordState {
	return {
		...createHelloDailyWord(),
		loaded: false
	}
}

export function normalizeDailyWord(result: KyyyHomeDailyWordResponse | null | undefined): KyyyHomeDailyWordState {
	const wordText = normalizeText(result?.wordText)
	if (!wordText) {
		return createHelloDailyWord()
	}
	const fallbackHelloWord = createHelloDailyWord()
	const partOfSpeech = normalizeText(result?.partOfSpeech)
	const meaningCn = normalizeText(result?.meaningCn)
	return {
		wordId: result?.wordId === null || result?.wordId === undefined ? null : toNumber(result?.wordId, 0),
		wordText,
		partOfSpeech: partOfSpeech || (isFallbackHelloWord(wordText) ? fallbackHelloWord.partOfSpeech : ''),
		meaningCn: meaningCn || (isFallbackHelloWord(wordText) ? fallbackHelloWord.meaningCn : ''),
		loaded: true
	}
}
