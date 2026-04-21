<template>
	<!-- 使用纯 CSS 处理底部安全距离，放弃内联 style 的 calc 和 env -->
	<view class="custom-tabbar">
		<!-- 占位元素，撑开页面底部空间，防止页面内容被 tabbar 遮挡 -->
		<view class="custom-tabbar__placeholder"></view>

		<view class="custom-tabbar__inner">
			<view class="custom-tabbar__panel">
				<view
					v-for="item in mergedItems"
					:key="item.key"
					class="tabbar-item"
					:class="{
						'is-active': isActive(item),
						'is-center-tab': item.position === 'center'
					}"
					@click="handleTabClick(item)"
				>
					<view class="icon-box">
						<uni-icons
							:type="resolveIcon(item)"
							:size="item.size || 24"
							:color="resolveColor(item)"
						/>
					</view>
					<text class="tabbar-label">{{ item.text }}</text>
				</view>
			</view>
			<!-- 安全区适配块 -->
			<view class="safe-area-inset"></view>
		</view>
	</view>
</template>

<script>
const DEFAULT_ITEMS = [
	{ key: 'study', text: '学习', pagePath: '/pages/study/index', icon: 'home', activeIcon: 'home-filled', position: 'left' },
	{ key: 'question-bank', text: '题库', pagePath: '/pages/question-bank/index', icon: 'compose', activeIcon: 'compose', position: 'left' },
	{ key: 'practice', text: '练习', pagePath: '/pages/practice/index', icon: 'plusempty', activeIcon: 'plusempty', position: 'center', iconColor: '#FFFFFF', size: 27 },
	{ key: 'exam', text: '考试', pagePath: '/pages/exam/index', icon: 'medal', activeIcon: 'medal-filled', position: 'right' },
	{ key: 'mine', text: '我的', pagePath: '/pages/mine/index', icon: 'person', activeIcon: 'person-filled', position: 'right' }
];

export default {
	name: 'CustomTabbar',
	props: {
		current: {
			type: String,
			default: ''
		},
		items: {
			type: Array,
			default: () => DEFAULT_ITEMS
		}
	},
	computed: {
		mergedItems() {
			const sourceItems = this.items?.length ? this.items : DEFAULT_ITEMS;
			return sourceItems.map(item => ({
				...item,
				activeColor: item.activeColor || '#2f3648', 
				color: item.color || '#bcc5d4'              
			}));
		}
	},
	// 每次组件挂载时，隐藏原生 Tabbar，防止重叠
	mounted() {
		uni.hideTabBar({ animation: false }).catch(() => {});
	},
	methods: {
		isActive(item) {
			return item.key === this.current;
		},
		resolveIcon(item) {
			if (item.position === 'center') return item.activeIcon || item.icon;
			return this.isActive(item) ? (item.activeIcon || item.icon) : item.icon;
		},
		resolveColor(item) {
			if (item.position === 'center') return item.iconColor || '#FFFFFF';
			return this.isActive(item) ? item.activeColor : item.color;
		},
		handleTabClick(item) {
			if (!item?.pagePath || this.isActive(item)) return;
			
			// 震动反馈 (提升用户体验)
			uni.vibrateShort({ type: 'light' }).catch(()=>{});
			
			uni.switchTab({
				url: item.pagePath,
				fail: () => {
					uni.redirectTo({ url: item.pagePath });
				}
			});
		}
	}
}
</script>

<style lang="scss" scoped>
$color-bg: #ffffff;
$color-text-normal: #bcc5d4;
$color-text-active: #2f3648;
$color-btn-center: #59647f; 

.custom-tabbar {
	position: relative;
	z-index: 999;

	/* 占位元素：纯 CSS 撑开高度，适配苹果小白条 */
	&__placeholder {
		height: 104rpx;
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
		box-sizing: content-box;
	}

	&__inner {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 999;
		background: $color-bg; /* 移到这里防止白边 */
		border-radius: 36rpx 36rpx 0 0;
		box-shadow: 0 -2rpx 16rpx rgba(43, 52, 55, 0.04);
		/* 取消之前的 pointer-events: none 技巧，改为常规布局 */
	}

	/* 苹果小白条安全区 */
	.safe-area-inset {
		background: $color-bg;
		height: constant(safe-area-inset-bottom);
		height: env(safe-area-inset-bottom);
	}

	&__panel {
		position: relative;
		display: flex;
		align-items: flex-end;
		height: 104rpx;
		width: 100%;
		background: transparent;
	}
}

.tabbar-item {
	position: relative;
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: flex-end;
	height: 100%;
	padding-bottom: 14rpx;

	&:active {
		opacity: 0.8; /* 优化：缩放容易导致凸起按钮错位，改为透明度反馈 */
	}

	.icon-box {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 50rpx;
		height: 50rpx;
		margin-bottom: 6rpx;
	}

	.tabbar-label {
		font-size: 20rpx;
		font-weight: 500;
		color: $color-text-normal;
		line-height: 1;
		transition: color 0.2s;
	}

	&.is-active .tabbar-label {
		color: $color-text-active;
		font-weight: bold;
	}

	/* ========================================= */
	/* 针对中间“练习”按钮的修复 */
	/* ========================================= */
	&.is-center-tab {
		/* 让父元素突破限制，以便捕获点击事件 */
		position: static; 
		
		.icon-box {
			position: absolute;
			bottom: 46rpx; 
			left: 50%;
			transform: translateX(-50%); /* 居中对齐更稳 */
			width: 84rpx;
			height: 84rpx;
			background: $color-btn-center;
			border-radius: 22rpx;
			box-shadow: 0 10rpx 20rpx rgba($color-btn-center, 0.3);
			z-index: 10;
		}

		.tabbar-label {
			margin-top: 50rpx; 
		}
	}
}
</style>