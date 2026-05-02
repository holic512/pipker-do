// AI 索引: KYZZ 小程序底部导航配置。

export interface KyzzTabbarItem {
	key: string
	text: string
	pagePath: string
	icon: string
	activeIcon?: string
	size?: number
	activeColor?: string
	color?: string
}

export const KYZZ_TABBAR_ITEMS: KyzzTabbarItem[] = [
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
		pagePath: '/pages/kyzz/mine/index',
		icon: 'person',
		activeIcon: 'person-filled'
	}
]

export function createKyzzTabbarItems(): KyzzTabbarItem[] {
	return KYZZ_TABBAR_ITEMS.map((item) => ({ ...item }))
}
