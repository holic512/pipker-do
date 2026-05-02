# Frontend Style Rules

<!-- ai-index: frontend style baseline -->

适用范围：`uniapp/**` 与 `admin-web/**`

## 1. 品牌基线

- `uniapp` 和 `admin-web` 延续同一品牌基线：浅色、克制、学院感。
- 颜色、阴影、圆角、字体优先复用已有 token。
- `admin-web` 样式参考 `uniapp/uni.scss` 的主色、中性色、圆角和阴影尺度。

## 2. 组件与布局

- 页面优先复用 `page-shell`、`custom-tabbar`、`custom-navbar`、`AdminLayout`、`PageContainer` 等已有公共布局。
- 表单页、资料页、用户中心页默认沿用当前“卡片 + 柔和边界 + 浅层背景”的表现。
- 不在不同页面中随意更换圆角尺度、阴影强度、按钮样式。

## 3. 避免事项

- 不引入新的主色体系。
- 不突然改成紫色系、霓虹风、纯黑科技风、全页玻璃拟态或高饱和渐变。
- 后台页如需统一观感，优先改 `admin-web/src/styles/element-overrides.scss` 或全局样式，不直接在业务页覆盖 Element Plus 大片结构。
