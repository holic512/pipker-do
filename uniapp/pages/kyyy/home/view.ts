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
		phoneticUs: '',
		phoneticUk: 'həˈləʊ',
		partOfSpeech: '',
		meaningCn: '1. int. 喂；你好\n2. n. 表示问候，惊奇或唤起注意时的用语\n3. n. (Hello)人名；(法)埃洛',
		exampleSentence: '',
		exampleTranslation: '',
		sourceBankName: '',
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
	const sourceBanks = Array.isArray(result?.sourceBanks) ? result?.sourceBanks : []
	const phoneticUs = normalizeText(result?.phoneticUs)
	const phoneticUk = normalizeText(result?.phoneticUk)
	const partOfSpeech = normalizeText(result?.partOfSpeech)
	const meaningCn = normalizeText(result?.meaningCn)
	return {
		wordId: result?.wordId === null || result?.wordId === undefined ? null : toNumber(result?.wordId, 0),
		wordText,
		phoneticUs: phoneticUs || (isFallbackHelloWord(wordText) ? fallbackHelloWord.phoneticUs : ''),
		phoneticUk: phoneticUk || (isFallbackHelloWord(wordText) ? fallbackHelloWord.phoneticUk : ''),
		partOfSpeech: partOfSpeech || (isFallbackHelloWord(wordText) ? fallbackHelloWord.partOfSpeech : ''),
		meaningCn: meaningCn || (isFallbackHelloWord(wordText) ? fallbackHelloWord.meaningCn : ''),
		exampleSentence: normalizeText(result?.exampleSentence),
		exampleTranslation: normalizeText(result?.exampleTranslation),
		sourceBankName: normalizeText(sourceBanks[0]?.bankName || result?.sourceBankName),
		loaded: true
	}
}
