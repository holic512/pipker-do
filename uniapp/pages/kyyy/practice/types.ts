// AI 索引: KYYY 小程序刷题设置类型。

export type KyyyExamDirection = 'english_one' | 'english_two'

export interface KyyyPracticeSettingOption {
	value: KyyyExamDirection
	label: string
}

export interface KyyyPracticeSettingResponse {
	examDirection?: KyyyExamDirection | string | null
	examDirectionLabel?: string | null
	defaultWordBankId?: number | string | null
	defaultWordBankName?: string | null
	examDirectionOptions?: KyyyPracticeSettingOption[] | null
}

export interface KyyyPracticeSettingRequest {
	examDirection?: KyyyExamDirection
	defaultWordBankId?: number | null
}

export interface KyyyPracticeSettingState {
	examDirection: KyyyExamDirection
	examDirectionLabel: string
	defaultWordBankId: number | null
	defaultWordBankName: string
	examDirectionOptions: KyyyPracticeSettingOption[]
	loaded: boolean
	syncing: boolean
}
