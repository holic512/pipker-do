<!-- AI 索引: 小程序通用顶部导航与项目切换抽屉。 -->
<template>
	<view class="custom-navbar" :style="placeholderStyle">
		<view class="custom-navbar__inner" :style="navbarStyle">
			<view class="custom-navbar__bar" :style="barStyle">
				<view
					class="custom-navbar__side custom-navbar__side--left"
					:style="sideStyle"
					@tap="handleMenuTap"
				>
					<uni-icons type="bars" :size="iconSize" color="#1f2937" />
				</view>
				<view class="custom-navbar__center"></view>
				<view class="custom-navbar__side" :style="sideStyle"></view>
			</view>
		</view>

		<view
			v-if="drawerRendered"
			class="project-drawer-layer"
			:class="{ 'is-visible': drawerVisible }"
		>
			<view class="project-drawer-layer__mask" @tap="closeProjectDrawer"></view>
			<view class="project-drawer-layer__panel" :style="drawerPanelShellStyle" @tap.stop>
				<view class="project-drawer" :style="drawerPanelStyle">
					<view class="project-drawer__header">
						<view class="project-drawer__header-copy">
							<text class="project-drawer__eyebrow">PROJECT SWITCHER</text>
							<text class="project-drawer__title">切换项目</text>
							<text class="project-drawer__desc">通过左侧抽屉在不同学科项目间快速切换。</text>
						</view>
						<view class="project-drawer__close" @tap="closeProjectDrawer">
							<uni-icons type="closeempty" size="22" color="#5f6970" />
						</view>
					</view>

					<view class="project-drawer__body">
						<view
							v-for="section in projectSections"
							:key="section.key"
							class="project-drawer__section"
						>
							<view class="project-drawer__section-head">
								<view class="project-drawer__section-copy">
									<text class="project-drawer__section-overline">分类</text>
									<text class="project-drawer__section-title">{{ section.title }}</text>
								</view>
								<text class="project-drawer__section-meta">{{ section.items.length }} 个项目</text>
							</view>

							<view class="project-drawer__cards">
								<view
									v-for="item in section.items"
									:key="item.code"
									class="project-drawer__card"
									:class="{ 'is-active': isCurrentProject(item) }"
									@tap="selectProject(item)"
								>
									<view class="project-drawer__cover" :style="{ background: item.coverBackground }">
										<view class="project-drawer__cover-orb"></view>
										<view class="project-drawer__cover-sheet" :style="{ background: item.coverSheetBackground }">
											<view
												v-for="(lineWidth, lineIndex) in item.coverLines"
												:key="`${item.code}-line-${lineIndex}`"
												class="project-drawer__cover-line"
												:style="{ width: lineWidth }"
											></view>
										</view>
										<view class="project-drawer__cover-badge" :style="{ background: item.coverBadgeBackground }">
											<text class="project-drawer__cover-badge-text">{{ item.coverCode }}</text>
										</view>
									</view>
									<view class="project-drawer__card-body">
										<text class="project-drawer__card-overline">项目</text>
										<text class="project-drawer__card-title">{{ item.title }}</text>
										<text class="project-drawer__card-desc">{{ item.description }}</text>
										<text class="project-drawer__card-tag">{{ item.badgeText }}</text>
									</view>
									<view class="project-drawer__card-trailing">
										<text
											v-if="isCurrentProject(item)"
											class="project-drawer__card-status"
										>
											当前
										</text>
										<uni-icons
											v-else
											type="right"
											size="14"
											color="#8e98aa"
										/>
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import {
	createProjectSections,
	isCurrentMiniappProjectRoute,
	openMiniappProject,
	syncUserDefaultProjectCode
} from '@/shared/project';

export default {
	name: 'CustomNavbar',
	data() {
		return {
			statusBarHeight: 20,
			horizontalPadding: '32rpx',
			buttonSize: 32,
			menuButtonInfo: {
				top: 0,
				height: 32,
				width: 88,
				right: 10
			},
			windowWidth: 375,
			drawerVisible: false,
			drawerRendered: false,
			drawerTimer: null,
			currentRoute: '',
			projectSections: createProjectSections()
		};
	},
	computed: {
		placeholderStyle() {
			return {
				height: `${this.statusBarHeight + this.barHeight}px`
			};
		},
		navbarStyle() {
			return {
				width: '100%',
				paddingTop: `${this.statusBarHeight}px`,
				background: 'transparent'
			};
		},
		barHeight() {
			const verticalGap = Math.max(this.menuButtonInfo.top - this.statusBarHeight, 0);
			return this.menuButtonInfo.height + verticalGap * 2;
		},
		barStyle() {
			return {
				width: '100%',
				height: `${this.barHeight}px`,
				paddingLeft: this.horizontalPadding,
				paddingRight: this.horizontalPadding
			};
		},
		sideStyle() {
			return {
				width: `${this.buttonSize}px`,
				height: `${this.buttonSize}px`
			};
		},
		iconSize() {
			return 20;
		},
		drawerWidth() {
			return Math.min(Math.max(this.windowWidth * 0.82, 288), 340);
		},
		drawerPanelShellStyle() {
			return {
				width: `${this.drawerWidth}px`
			};
		},
		drawerPanelStyle() {
			return {
				paddingTop: `${this.statusBarHeight + 16}px`
			};
		}
	},
	created() {
		const systemInfo = uni.getSystemInfoSync();
		this.statusBarHeight = systemInfo.statusBarHeight || 20;
		this.windowWidth = systemInfo.windowWidth || 375;
		if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
			const menuButtonInfo = uni.getMenuButtonBoundingClientRect();
			if (menuButtonInfo && menuButtonInfo.height) {
				this.menuButtonInfo = {
					top: menuButtonInfo.top || this.statusBarHeight,
					height: menuButtonInfo.height,
					width: menuButtonInfo.width,
					right: this.windowWidth - (menuButtonInfo.right || this.windowWidth - 10)
				};
			}
		}
		this.syncCurrentRoute();
	},
	beforeUnmount() {
		this.clearDrawerTimer();
	},
	methods: {
		handleMenuTap() {
			this.openProjectDrawer();
			this.$emit('menu-click');
		},
		clearDrawerTimer() {
			if (this.drawerTimer) {
				clearTimeout(this.drawerTimer);
				this.drawerTimer = null;
			}
		},
		syncCurrentRoute() {
			const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
			const currentPage = pages[pages.length - 1];
			this.currentRoute = currentPage && currentPage.route ? currentPage.route : '';
		},
		openProjectDrawer() {
			this.syncCurrentRoute();
			this.clearDrawerTimer();
			if (this.drawerRendered && this.drawerVisible) {
				return;
			}
			this.drawerRendered = true;
			setTimeout(() => {
				this.drawerVisible = true;
			}, 16);
		},
		closeProjectDrawer() {
			if (!this.drawerRendered) {
				return;
			}
			this.clearDrawerTimer();
			this.drawerVisible = false;
			this.drawerTimer = setTimeout(() => {
				this.drawerRendered = false;
				this.drawerTimer = null;
			}, 280);
		},
		isCurrentProject(project) {
			return isCurrentMiniappProjectRoute(project, this.currentRoute);
		},
		selectProject(project) {
			if (!project) {
				return;
			}
			const isCurrent = this.isCurrentProject(project);
			this.closeProjectDrawer();
			if (isCurrent) {
				return;
			}
			setTimeout(() => {
				this.navigateToProject(project);
			}, 280);
		},
		navigateToProject(project) {
			if (!project || !project.pagePath) {
				return;
			}
			const showNavigateError = (error) => {
				console.warn(`[custom-navbar] open project ${project.code} failed.`, error);
				uni.showToast({
					title: `打开${project.title}失败`,
					icon: 'none'
				});
			};
			openMiniappProject(project)
				.then(() => {
					syncUserDefaultProjectCode(project.code).catch((error) => {
						console.warn(`[custom-navbar] sync default project ${project.code} failed.`, error);
					});
				})
				.catch(showNavigateError);
		}
	}
};
</script>

<style lang="scss" scoped>
.custom-navbar {
	position: relative;
	z-index: 2000;
}

.custom-navbar__inner {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 1;
}

.custom-navbar__bar {
	display: grid;
	grid-template-columns: auto 1fr auto;
	align-items: center;
	box-sizing: border-box;
}

.custom-navbar__side {
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
}

.custom-navbar__side--left {
	border-radius: 999rpx;
}

.custom-navbar__center {
	display: flex;
	align-items: center;
	justify-content: center;
}

.project-drawer-layer {
	position: fixed;
	inset: 0;
	z-index: 10;
	pointer-events: none;
}

.project-drawer-layer.is-visible {
	pointer-events: auto;
}

.project-drawer-layer__mask {
	position: absolute;
	inset: 0;
	background: rgba(31, 41, 55, 0.22);
	opacity: 0;
	transition: opacity 0.28s ease;
}

.project-drawer-layer.is-visible .project-drawer-layer__mask {
	opacity: 1;
}

.project-drawer-layer__panel {
	position: absolute;
	top: 0;
	bottom: 0;
	left: 0;
	transform: translateX(-100%);
	transition: transform 0.28s ease;
	box-shadow: 24rpx 0 56rpx rgba(43, 52, 55, 0.12);
}

.project-drawer-layer.is-visible .project-drawer-layer__panel {
	transform: translateX(0);
}

.project-drawer {
	height: 100%;
	display: flex;
	flex-direction: column;
	box-sizing: border-box;
	padding-right: 24rpx;
	padding-left: 24rpx;
	padding-bottom: calc(env(safe-area-inset-bottom) + 36rpx);
	background:
		radial-gradient(circle at top right, rgba(215, 226, 255, 0.82), rgba(215, 226, 255, 0) 34%),
		linear-gradient(180deg, #f9fbfc 0%, #f2f5f7 100%);
}

.project-drawer__header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 28rpx;
}

.project-drawer__header-copy {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.project-drawer__eyebrow {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.2em;
	color: #8d97a7;
}

.project-drawer__title {
	font-size: 44rpx;
	line-height: 1.18;
	font-weight: 700;
	color: #2b3437;
}

.project-drawer__desc {
	font-size: 24rpx;
	line-height: 1.6;
	color: #6d7782;
}

.project-drawer__close {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 72rpx;
	height: 72rpx;
	padding-bottom: 10rpx;
	flex-shrink: 0;
}

.project-drawer__body {
	flex: 1;
	min-height: 0;
	overflow-y: auto;
	-webkit-overflow-scrolling: touch;
	padding-bottom: 20rpx;
}

.project-drawer__section + .project-drawer__section {
	margin-top: 32rpx;
}

.project-drawer__section-head {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 18rpx;
}

.project-drawer__section-copy {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.project-drawer__section-overline {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.16em;
	color: #98a2b2;
}

.project-drawer__section-title {
	font-size: 30rpx;
	line-height: 1.3;
	font-weight: 700;
	color: #3d4854;
}

.project-drawer__section-meta {
	font-size: 22rpx;
	line-height: 1.4;
	color: #8b95a4;
}

.project-drawer__cards {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.project-drawer__card {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 20rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.94);
	border: 1rpx solid rgba(215, 226, 255, 0.9);
	box-shadow: 0 16rpx 34rpx rgba(43, 52, 55, 0.07);
}

.project-drawer__card.is-active {
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(244, 247, 251, 0.98));
	border-color: rgba(84, 94, 118, 0.26);
	box-shadow: 0 20rpx 42rpx rgba(43, 52, 55, 0.1);
}

.project-drawer__cover {
	position: relative;
	width: 168rpx;
	height: 120rpx;
	border-radius: 22rpx;
	overflow: hidden;
	flex-shrink: 0;
	padding: 14rpx;
}

.project-drawer__cover-orb {
	position: absolute;
	top: -28rpx;
	right: -18rpx;
	width: 84rpx;
	height: 84rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.18);
}

.project-drawer__cover-sheet {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	width: 84rpx;
	height: 92rpx;
	padding: 16rpx 14rpx;
	border-radius: 18rpx;
	box-shadow: 0 8rpx 20rpx rgba(43, 52, 55, 0.08);
}

.project-drawer__cover-line {
	height: 10rpx;
	border-radius: 999rpx;
	background: #dfe7f4;
}

.project-drawer__cover-badge {
	position: absolute;
	right: 14rpx;
	bottom: 14rpx;
	z-index: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 72rpx;
	padding: 10rpx 14rpx;
	border-radius: 999rpx;
	backdrop-filter: blur(10rpx);
}

.project-drawer__cover-badge-text {
	font-size: 20rpx;
	line-height: 1;
	font-weight: 700;
	letter-spacing: 0.14em;
	color: #ffffff;
}

.project-drawer__card-body {
	display: flex;
	flex: 1;
	flex-direction: column;
	gap: 6rpx;
	min-width: 0;
}

.project-drawer__card-overline {
	font-size: 18rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.12em;
	color: #9aa3b2;
}

.project-drawer__card-title {
	font-size: 32rpx;
	line-height: 1.25;
	font-weight: 700;
	color: #2f3940;
}

.project-drawer__card-desc {
	font-size: 22rpx;
	line-height: 1.55;
	color: #6e7882;
}

.project-drawer__card-tag {
	align-self: flex-start;
	margin-top: 4rpx;
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
	background: #edf2f7;
	font-size: 20rpx;
	line-height: 1.2;
	font-weight: 600;
	color: #5e697a;
}

.project-drawer__card-trailing {
	display: flex;
	align-items: center;
	justify-content: center;
	min-width: 52rpx;
	flex-shrink: 0;
}

.project-drawer__card-status {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	background: #dde7f8;
	font-size: 20rpx;
	line-height: 1.2;
	font-weight: 700;
	color: #4b5a72;
}
</style>
