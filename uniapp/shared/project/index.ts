// AI 索引: 小程序全局项目目录、默认项目缓存与跳转。

import { getUserDefaultProject, updateUserDefaultProject } from '@/shared/api/user'

export type MiniappProjectNavigationType = 'switchTab' | 'reLaunch'

export interface MiniappProject {
	code: string
	title: string
	pagePath: string
	routePrefix: string
	navigationType: MiniappProjectNavigationType
}

export interface MiniappProjectSection {
	key: string
	title: string
	items: MiniappProject[]
}

export interface MiniappProjectNavigationTarget {
	code: string
	title: string
	pagePath: string
	navigationType: MiniappProjectNavigationType
}

const DEFAULT_PROJECT_CODE = 'kyzz'
const DEFAULT_PROJECT_STORAGE_KEY = 'PIPKER_DEFAULT_PROJECT'

const PROJECT_SECTIONS: MiniappProjectSection[] = [
	{
		key: 'graduate',
		title: '考研',
		items: [
			{
				code: 'kyzz',
				title: '考研政治',
				pagePath: '/pages/kyzz/study/index',
				routePrefix: 'pages/kyzz/',
				navigationType: 'switchTab'
			},
			{
				code: 'kyyy',
				title: '考研英语',
				pagePath: '/pages/kyyy/index',
				routePrefix: 'pages/kyyy/',
				navigationType: 'reLaunch'
			}
		]
	}
]

const MINIAPP_PROJECTS = PROJECT_SECTIONS.flatMap((section) => section.items)
const MINIAPP_PROJECT_MAP = new Map(MINIAPP_PROJECTS.map((project) => [project.code, project]))

export function getDefaultMiniappProject(): MiniappProject {
	return MINIAPP_PROJECT_MAP.get(DEFAULT_PROJECT_CODE) || MINIAPP_PROJECTS[0]!
}

export function createProjectSections(): MiniappProjectSection[] {
	return PROJECT_SECTIONS.map((section) => ({
		...section,
		items: section.items.map((item) => ({ ...item }))
	}))
}

export function resolveMiniappProject(projectCode: unknown): MiniappProject {
	if (typeof projectCode === 'string') {
		const project = MINIAPP_PROJECT_MAP.get(projectCode.trim())
		if (project) {
			return project
		}
	}
	return getDefaultMiniappProject()
}

export function toProjectNavigationTarget(projectCode: unknown): MiniappProjectNavigationTarget {
	const project = resolveMiniappProject(projectCode)
	return {
		code: project.code,
		title: project.title,
		pagePath: project.pagePath,
		navigationType: project.navigationType
	}
}

export function isCurrentMiniappProjectRoute(project: MiniappProject, route: string): boolean {
	return !!project && !!route && route.startsWith(project.routePrefix)
}

export function getCachedDefaultProjectCode(): string {
	const projectCode = uni.getStorageSync(DEFAULT_PROJECT_STORAGE_KEY)
	return typeof projectCode === 'string' ? projectCode : ''
}

export function getCachedProjectNavigationTarget(): MiniappProjectNavigationTarget {
	return toProjectNavigationTarget(getCachedDefaultProjectCode())
}

export function cacheDefaultProjectCode(projectCode: unknown): string {
	const project = resolveMiniappProject(projectCode)
	uni.setStorageSync(DEFAULT_PROJECT_STORAGE_KEY, project.code)
	return project.code
}

export async function fetchUserDefaultProjectCode(): Promise<string> {
	const response = await getUserDefaultProject()
	return cacheDefaultProjectCode(response.projectCode)
}

export async function syncUserDefaultProjectCode(projectCode: unknown): Promise<string> {
	const normalizedProjectCode = cacheDefaultProjectCode(projectCode)
	await updateUserDefaultProject(normalizedProjectCode)
	return normalizedProjectCode
}

export function openMiniappProject(projectOrCode: MiniappProject | MiniappProjectNavigationTarget | string): Promise<void> {
	const target = typeof projectOrCode === 'string'
		? toProjectNavigationTarget(projectOrCode)
		: toProjectNavigationTarget(projectOrCode.code)

	if (target.navigationType === 'switchTab') {
		return switchTabWithRelaunchFallback(target.pagePath)
	}
	return relaunchTo(target.pagePath)
}

function switchTabWithRelaunchFallback(url: string): Promise<void> {
	return new Promise((resolve, reject) => {
		uni.switchTab({
			url,
			success: () => resolve(),
			fail: () => {
				uni.reLaunch({
					url,
					success: () => resolve(),
					fail: reject
				})
			}
		})
	})
}

function relaunchTo(url: string): Promise<void> {
	return new Promise((resolve, reject) => {
		uni.reLaunch({
			url,
			success: () => resolve(),
			fail: reject
		})
	})
}
