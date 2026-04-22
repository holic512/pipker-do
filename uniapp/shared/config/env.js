const ENV_MAP = {
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

function resolveEnvName() {
	const runtimeEnv = uni.getStorageSync(STORAGE_KEY)
	if (runtimeEnv && ENV_MAP[runtimeEnv]) {
		return runtimeEnv
	}

	// #ifdef MP-WEIXIN
	return 'development'
	// #endif

	// #ifndef MP-WEIXIN
	return process.env.NODE_ENV === 'production' ? 'production' : 'development'
	// #endif
}

const envName = resolveEnvName()
const currentEnv = ENV_MAP[envName] || ENV_MAP.development
const apiBaseUrl = uni.getStorageSync(BASE_URL_OVERRIDE_KEY) || currentEnv.apiBaseUrl

export default {
	envName,
	apiBaseUrl,
	timeout: currentEnv.timeout,
	loginUrl: `${apiBaseUrl}/api/auth/wechat/login`
}
