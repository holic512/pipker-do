import type {
	KyyyExamDirection,
	KyyyPracticeSettingOption,
	KyyyPracticeSettingResponse,
	KyyyPracticeSettingState
} from '@/pages/kyyy/practice/types'

// AI 索引: KYYY 刷题设置视图归一化辅助。

export const DEFAULT_KYYY_EXAM_DIRECTION: KyyyExamDirection = 'english_one'

export const DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS: KyyyPracticeSettingOption[] = [
	{ value: 'english_one', label: '英一' },
	{ value: 'english_two', label: '英二' }
]

export function normalizeExamDirection(value: unknown): KyyyExamDirection {
	if (typeof value !== 'string') {
		return DEFAULT_KYYY_EXAM_DIRECTION
	}
	const normalized = value.trim().toLowerCase().replace(/-/g, '_')
	if (['english_two', 'two', 'english2', 'english_2', 'english_ii', '英二', '英语二'].includes(normalized)) {
		return 'english_two'
	}
	return DEFAULT_KYYY_EXAM_DIRECTION
}

export function resolveExamDirectionLabel(value: KyyyExamDirection, options = DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS): string {
	return options.find((item) => item.value === value)?.label || (value === 'english_two' ? '英二' : '英一')
}

export function createDefaultPracticeSettings(): KyyyPracticeSettingState {
	return {
		examDirection: DEFAULT_KYYY_EXAM_DIRECTION,
		examDirectionLabel: resolveExamDirectionLabel(DEFAULT_KYYY_EXAM_DIRECTION),
		defaultWordBankId: null,
		defaultWordBankName: '',
		examDirectionOptions: DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS,
		loaded: false,
		syncing: false
	}
}

export function normalizePracticeSettings(result: KyyyPracticeSettingResponse | null | undefined): KyyyPracticeSettingState {
	const resultOptions = Array.isArray(result?.examDirectionOptions) ? result.examDirectionOptions : []
	const options = resultOptions.length
		? resultOptions.map((item) => ({
			value: normalizeExamDirection(item.value),
			label: item.label || resolveExamDirectionLabel(normalizeExamDirection(item.value))
		}))
		: DEFAULT_KYYY_EXAM_DIRECTION_OPTIONS
	const examDirection = normalizeExamDirection(result?.examDirection)
	const defaultWordBankId = result?.defaultWordBankId === null || result?.defaultWordBankId === undefined
		? null
		: Number(result.defaultWordBankId)
	return {
		examDirection,
		examDirectionLabel: result?.examDirectionLabel || resolveExamDirectionLabel(examDirection, options),
		defaultWordBankId: defaultWordBankId !== null && Number.isFinite(defaultWordBankId) && defaultWordBankId > 0 ? defaultWordBankId : null,
		defaultWordBankName: typeof result?.defaultWordBankName === 'string' ? result.defaultWordBankName.trim() : '',
		examDirectionOptions: options,
		loaded: true,
		syncing: false
	}
}
