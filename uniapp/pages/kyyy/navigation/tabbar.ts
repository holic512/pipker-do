/**
 * @file KyyyTabbarConfig
 * @project pipker-do
 * @module 考研英语 / 底部导航
 * @description 定义考研英语小程序底部导航项、文案与页面跳转方式。
 * @logic 1. 维护首页、作文、单词、阅读、翻译、我的六个导航项；2. 为每项提供 pagePath、图标与导航类型；3. 通过工厂函数返回浅拷贝，避免运行时直接修改常量配置。
 * @dependencies Component: @/components/kyyy/kyyy-tabbar.vue, Page: @/pages/kyyy/practice/index, Page: @/pages/kyyy/reading/index
 * @index_tags 考研英语, 底部导航, tabbar, 阅读, 导航配置
 * @author holic512
 */
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
		text: '单词',
		pagePath: '/pages/kyyy/practice/index',
		icon: 'checkbox',
		activeIcon: 'checkbox-filled',
		navigationType: 'reLaunch'
	},
	{
		key: 'reading',
		text: '阅读',
		pagePath: '/pages/kyyy/reading/index',
		icon: 'compose',
		activeIcon: 'compose',
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
