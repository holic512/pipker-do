// AI 索引: 小程序设备指纹与安装级 deviceId。

const DEVICE_PROFILE_KEY = 'PIPKER_DEVICE_PROFILE'

export interface DeviceProfile {
	deviceId: string
	platform: string
	brand: string
	model: string
	system: string
	appVersion: string
}

let cachedDeviceProfile: DeviceProfile | null = null

function isRecord(value: unknown): value is Record<string, unknown> {
	return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function generateDeviceId(): string {
	const randomSegment = () => Math.random().toString(36).slice(2, 10)
	return `dev_${Date.now().toString(36)}_${randomSegment()}${randomSegment()}`
}

function safeSystemInfo(): Record<string, unknown> {
	try {
		return (uni.getSystemInfoSync() || {}) as Record<string, unknown>
	} catch (error) {
		return {}
	}
}

function safeAppVersion(systemInfo: Record<string, unknown>): string {
	if (typeof uni.getAppBaseInfo === 'function') {
		try {
			const appBaseInfo = (uni.getAppBaseInfo() || {}) as Record<string, unknown>
			if (appBaseInfo.version) {
				return String(appBaseInfo.version)
			}
		} catch (error) {
			// ignore app base info failure
		}
	}
	return String(systemInfo.appVersion || systemInfo.version || 'unknown')
}

function normalizeStoredProfile(profile: unknown): DeviceProfile | null {
	if (!isRecord(profile) || !profile.deviceId) {
		return null
	}
	const systemInfo = safeSystemInfo()
	return {
		deviceId: String(profile.deviceId),
		platform: String(profile.platform || systemInfo.platform || 'unknown'),
		brand: String(profile.brand || systemInfo.brand || 'unknown'),
		model: String(profile.model || systemInfo.model || 'unknown'),
		system: String(profile.system || systemInfo.system || 'unknown'),
		appVersion: String(profile.appVersion || safeAppVersion(systemInfo))
	}
}

export function getOrCreateDeviceProfile(): DeviceProfile {
	if (cachedDeviceProfile) {
		return cachedDeviceProfile
	}
	const storedProfile = normalizeStoredProfile(uni.getStorageSync(DEVICE_PROFILE_KEY))
	if (storedProfile) {
		cachedDeviceProfile = storedProfile
		return cachedDeviceProfile
	}
	const systemInfo = safeSystemInfo()
	cachedDeviceProfile = {
		deviceId: generateDeviceId(),
		platform: String(systemInfo.platform || 'unknown'),
		brand: String(systemInfo.brand || 'unknown'),
		model: String(systemInfo.model || 'unknown'),
		system: String(systemInfo.system || 'unknown'),
		appVersion: safeAppVersion(systemInfo)
	}
	uni.setStorageSync(DEVICE_PROFILE_KEY, cachedDeviceProfile)
	return cachedDeviceProfile
}

export function getDeviceId(): string {
	return getOrCreateDeviceProfile().deviceId
}
