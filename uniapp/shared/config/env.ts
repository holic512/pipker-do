// AI 索引: 小程序运行时环境配置。

export type RuntimeEnvName = 'development' | 'test' | 'production'

interface RuntimeEnvPreset {
	apiBaseUrl: string
	timeout: number
}

export interface RuntimeEnv {
	envName: RuntimeEnvName
	apiBaseUrl: string
	timeout: number
	loginUrl: string
}

const ENV_MAP: Record<RuntimeEnvName, RuntimeEnvPreset> = {
	development: {
		apiBaseUrl: 'http://127.0.0.1:8080',
		timeout: 15000
	},
	test: {
		apiBaseUrl: 'http://127.0.0.1:8080',
		timeout: 15000
	},
	production: {
		apiBaseUrl: 'http://127.0.0.1:8080',
		timeout: 15000
	}
}

const STORAGE_KEY = 'APP_RUNTIME_ENV'
const BASE_URL_OVERRIDE_KEY = 'APP_BASE_URL'

function resolveEnvName(): RuntimeEnvName {
	const runtimeEnv = uni.getStorageSync(STORAGE_KEY)
	if (typeof runtimeEnv === 'string' && runtimeEnv in ENV_MAP) {
		return runtimeEnv as RuntimeEnvName
	}

	// #ifdef MP-WEIXIN
	return 'development'
	// #endif

	// #ifndef MP-WEIXIN
	return process.env?.NODE_ENV === 'production' ? 'production' : 'development'
	// #endif
}

const envName = resolveEnvName()
const currentEnv = ENV_MAP[envName] || ENV_MAP.development
const baseUrlOverride = uni.getStorageSync(BASE_URL_OVERRIDE_KEY)
const apiBaseUrl = typeof baseUrlOverride === 'string' && baseUrlOverride
	? baseUrlOverride
	: currentEnv.apiBaseUrl

const env: RuntimeEnv = {
	envName,
	apiBaseUrl,
	timeout: currentEnv.timeout,
	loginUrl: `${apiBaseUrl}/api/auth/wechat/login`
}

export default env
