// AI 索引: 小程序冷启动品牌页与初始化状态机。

import { getToken } from '@/shared/auth/storage'
import env from '@/shared/config/env'
import { createKyzzTabbarItems } from '@/pages/kyzz/navigation/tabbar'
import { getOrCreateDeviceProfile } from '@/shared/platform/device'
import { warmKyzzCorePreload } from '@/shared/preload/kyzz'
import { bootstrapAuth } from '@/shared/session/session'

export type LaunchStatus = 'idle' | 'loading' | 'ready' | 'error'

export interface LaunchMenuItem {
	key: string
	text: string
	pagePath: string
	icon: string
	activeIcon?: string
	size?: number
	activeColor?: string
	color?: string
}

export interface LaunchSnapshot {
	status: LaunchStatus
	stepKey: string
	stepText: string
	visible: boolean
	errorMessage: string
	menuItems: LaunchMenuItem[]
}

type LaunchListener = (snapshot: LaunchSnapshot) => void

const MIN_VISIBLE_MS = 820
const STEP_PAUSE_MS = 68
const EXIT_TRANSITION_MS = 220

const listeners = new Set<LaunchListener>()

const state: LaunchSnapshot = {
	status: 'idle',
	stepKey: 'idle',
	stepText: '',
	visible: false,
	errorMessage: '',
	menuItems: createKyzzTabbarItems()
}

let launchPromise: Promise<LaunchSnapshot> | null = null
let coldStartCompleted = false
let launchStartedAt = 0

function cloneMenuItems(items: LaunchMenuItem[]): LaunchMenuItem[] {
	return items.map((item) => ({ ...item }))
}

function wait(ms: number): Promise<void> {
	return new Promise((resolve) => {
		setTimeout(resolve, ms)
	})
}

function notify(): void {
	const snapshot = getLaunchSnapshot()
	listeners.forEach((listener) => {
		try {
			listener(snapshot)
		} catch (error) {
			console.warn('[launch] listener error', error)
		}
	})
}

function updateLaunchState(patch: Partial<LaunchSnapshot>): void {
	Object.assign(state, patch)
	notify()
}

function resolveErrorMessage(error: unknown): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return '网络异常，请稍后重试'
}

async function syncStep(stepKey: string, stepText: string, executor?: () => void | Promise<void>): Promise<void> {
	updateLaunchState({
		status: 'loading',
		stepKey,
		stepText,
		errorMessage: ''
	})
	if (executor) {
		await executor()
	}
	await wait(STEP_PAUSE_MS)
}

async function runLaunchBootstrap(): Promise<LaunchSnapshot> {
	launchStartedAt = Date.now()
	updateLaunchState({
		status: 'loading',
		stepKey: 'token',
		stepText: '读取登录态',
		visible: true,
		errorMessage: '',
		menuItems: createKyzzTabbarItems()
	})

	await syncStep('token', '读取登录态', () => {
		getToken()
	})

	await syncStep('runtime', '准备运行环境', () => {
		void env.envName
		void env.apiBaseUrl
		getOrCreateDeviceProfile()
	})

	const menuItems = createKyzzTabbarItems()
	await syncStep('menu', '整理导航入口', () => {
		updateLaunchState({
			menuItems
		})
	})

	await syncStep('auth', '同步用户信息', async () => {
		try {
			await bootstrapAuth({ silent: true })
			warmKyzzCorePreload()
		} catch (error) {
			console.warn('[launch] auth bootstrap skipped', error)
		}
	})

	const elapsed = Date.now() - launchStartedAt
	if (elapsed < MIN_VISIBLE_MS) {
		await wait(MIN_VISIBLE_MS - elapsed)
	}

	updateLaunchState({
		status: 'ready',
		stepKey: 'ready',
		stepText: '准备就绪'
	})

	await wait(EXIT_TRANSITION_MS)
	coldStartCompleted = true
	updateLaunchState({
		visible: false,
		errorMessage: ''
	})

	return getLaunchSnapshot()
}

function beginLaunchBootstrap(): Promise<LaunchSnapshot> {
	if (coldStartCompleted) {
		return Promise.resolve(getLaunchSnapshot())
	}

	if (launchPromise) {
		return launchPromise
	}

	launchPromise = runLaunchBootstrap()
		.catch((error: unknown) => {
			updateLaunchState({
				status: 'error',
				stepKey: 'error',
				stepText: '启动失败，请重试',
				visible: true,
				errorMessage: resolveErrorMessage(error)
			})
			throw error
		})
		.finally(() => {
			launchPromise = null
		})

	return launchPromise
}

export function getLaunchSnapshot(): LaunchSnapshot {
	return {
		status: state.status,
		stepKey: state.stepKey,
		stepText: state.stepText,
		visible: state.visible,
		errorMessage: state.errorMessage,
		menuItems: cloneMenuItems(state.menuItems)
	}
}

export function subscribeLaunchState(listener: LaunchListener): () => boolean {
	listeners.add(listener)
	return () => listeners.delete(listener)
}

export function startLaunchBootstrap(): Promise<LaunchSnapshot> {
	return beginLaunchBootstrap()
}

export function retryLaunchBootstrap(): Promise<LaunchSnapshot> {
	return beginLaunchBootstrap()
}

export function hasCompletedLaunchBootstrap(): boolean {
	return coldStartCompleted
}
