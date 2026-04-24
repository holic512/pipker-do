// AI 索引: 小程序底部导航共享配置。

export interface TabbarItem {
	key: string
	text: string
	pagePath: string
	icon: string
	activeIcon?: string
	size?: number
	activeColor?: string
	color?: string
}

export const DEFAULT_TABBAR_ITEMS: TabbarItem[] = [
	{
		key: 'study',
		text: '学习',
		pagePath: '/pages/kyzz/study/index',
		icon: 'home',
		activeIcon: 'home-filled'
	},
	{
		key: 'question-bank',
		text: '题库',
		pagePath: '/pages/kyzz/question-bank/index',
		icon: 'compose',
		activeIcon: 'compose'
	},
	{
		key: 'practice',
		text: '练习',
		pagePath: '/pages/kyzz/practice/index',
		icon: 'plusempty',
		activeIcon: 'plusempty'
	},
	{
		key: 'exam',
		text: '考试',
		pagePath: '/pages/kyzz/exam/index',
		icon: 'medal',
		activeIcon: 'medal-filled'
	},
	{
		key: 'mine',
		text: '我的',
		pagePath: '/pages/common/mine/index',
		icon: 'person',
		activeIcon: 'person-filled'
	}
]

export function createDefaultTabbarItems(): TabbarItem[] {
	return DEFAULT_TABBAR_ITEMS.map((item) => ({ ...item }))
}
