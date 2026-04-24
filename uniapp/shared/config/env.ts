// AI 索引: 小程序运行时环境配置、本地测试地址、体验版地址、正式版地址。
// 不要直接修改 unpackage/dist/dev/mp-weixin/shared/config/env.js。
// 上面的 dist 文件是编译产物，下次重新运行/打包会被当前源码覆盖。
//
// 环境约定:
// development: 本地开发/联调地址
// test: 体验版/测试环境地址
// production: 正式发布地址
//
// 微信小程序会自动读取 envVersion 并映射:
// develop -> development
// trial -> test
// release -> production
//
// 如需临时手工覆盖:
// uni.setStorageSync('APP_RUNTIME_ENV', 'test')
// uni.setStorageSync('APP_BASE_URL', 'https://test-api.example.com')
// 取消覆盖时执行:
// uni.removeStorageSync('APP_RUNTIME_ENV')
// uni.removeStorageSync('APP_BASE_URL')

export type RuntimeEnvName = 'development' | 'test' | 'production'

interface RuntimeEnvPreset {
	apiBaseUrl: string
	timeout: number
}

type MpWeixinEnvVersion = 'develop' | 'trial' | 'release'

interface WeixinAccountInfo {
	miniProgram?: {
		envVersion?: MpWeixinEnvVersion
	}
}

export interface RuntimeEnv {
	envName: RuntimeEnvName
	apiBaseUrl: string
	timeout: number
	loginUrl: string
}

const ENV_MAP: Record<RuntimeEnvName, RuntimeEnvPreset> = {
	development: {
		// 本地开发地址。真机调试时不要继续使用 http://127.0.0.1:8080，应改成电脑局域网 IP 或可访问的开发域名。
		// apiBaseUrl: 'https://do.pipker.com',
		apiBaseUrl: 'http://127.0.0.1:8080',
		timeout: 15000
	},
	test: {
		// 体验版/测试版地址。这里改成你的测试服务器域名。
		apiBaseUrl: 'https://do.pipker.com',
		timeout: 15000
	},
	production: {
		// 正式发布地址。这里应改成线上域名，且通常需要是已配置到微信后台的 HTTPS 域名。
		apiBaseUrl: 'https://do.pipker.com',
		timeout: 15000
	}
}

const STORAGE_KEY = 'APP_RUNTIME_ENV'
const BASE_URL_OVERRIDE_KEY = 'APP_BASE_URL'

function resolveMpWeixinEnvName(): RuntimeEnvName {
	try {
		const wxApi = (globalThis as {
			wx?: {
				getAccountInfoSync?: () => WeixinAccountInfo
			}
		}).wx
		const envVersion = wxApi?.getAccountInfoSync?.()?.miniProgram?.envVersion

		switch (envVersion) {
			case 'trial':
				return 'test'
			case 'release':
				return 'production'
			default:
				return 'development'
		}
	} catch {
		return 'development'
	}
}

function resolveEnvName(): RuntimeEnvName {
	const runtimeEnv = uni.getStorageSync(STORAGE_KEY)
	if (typeof runtimeEnv === 'string' && runtimeEnv in ENV_MAP) {
		return runtimeEnv as RuntimeEnvName
	}

	// #ifdef MP-WEIXIN
	return resolveMpWeixinEnvName()
	// #endif

	// #ifndef MP-WEIXIN
	return process.env?.NODE_ENV === 'production' ? 'production' : 'development'
	// #endif
}

const envName = resolveEnvName()
const currentEnv = ENV_MAP[envName] || ENV_MAP.development
const baseUrlOverride = uni.getStorageSync(BASE_URL_OVERRIDE_KEY)
// APP_BASE_URL 优先级最高，适合临时把某个包指到指定服务做联调。
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
