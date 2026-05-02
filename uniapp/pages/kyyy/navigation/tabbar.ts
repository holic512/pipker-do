// AI 索引: KYYY 小程序底部导航配置。

export type KyyyTabbarNavigationType = 'reLaunch' | 'switchTab' | 'navigateTo'

export interface KyyyTabbarItem {
	key: string
	text: string
	pagePath: string
	icon: string
	activeIcon?: string
	size?: number
	activeColor?: string
	color?: string
	navigationType?: KyyyTabbarNavigationType
}

export const KYYY_TABBAR_ITEMS: KyyyTabbarItem[] = [
	{
		key: 'home',
		text: '首页',
		pagePath: '/pages/kyyy/index',
		icon: 'home',
		activeIcon: 'home-filled',
		navigationType: 'reLaunch'
	},
	{
		key: 'composition',
		text: '作文',
		pagePath: '/pages/kyyy/composition/index',
		icon: 'compose',
		activeIcon: 'compose',
		navigationType: 'reLaunch'
	},
	{
		key: 'practice',
		text: '练习',
		pagePath: '/pages/kyyy/practice/index',
		icon: 'checkbox',
		activeIcon: 'checkbox-filled',
		navigationType: 'reLaunch'
	},
	{
		key: 'translation',
		text: '翻译',
		pagePath: '/pages/kyyy/translation/index',
		icon: 'paperplane',
		activeIcon: 'paperplane-filled',
		navigationType: 'reLaunch'
	},
	{
		key: 'mine',
		text: '我的',
		pagePath: '/pages/kyyy/mine/index',
		icon: 'person',
		activeIcon: 'person-filled',
		navigationType: 'reLaunch'
	}
]

export function createKyyyTabbarItems(): KyyyTabbarItem[] {
	return KYYY_TABBAR_ITEMS.map((item) => ({ ...item }))
}
