# Admin Web Rules

<!-- ai-index: admin web directory rules -->

适用范围：`admin-web/**`

## 1. 目录索引

- `admin-web/src/main.ts`、`App.vue`：启动入口。
- `admin-web/src/app/router`：后台路由和守卫。
- `admin-web/src/app/layout/AdminLayout.vue`：统一后台壳。
- `admin-web/src/app/store/theme.ts`：后台主题状态。
- `admin-web/src/shared/http/client.ts`：Axios 请求封装。
- `admin-web/src/shared/components/PageContainer.vue`：页面容器。
- `admin-web/src/shared/types`、`constants`、`utils`：共享类型、常量、工具。
- `admin-web/src/modules/system`：登录、工作台、管理员、角色、用户、VIP、项目切换等通用后台能力。
- `admin-web/src/modules/system/config/menu.ts`：后台菜单配置。
- `admin-web/src/modules/system/store/session.ts`：后台登录 session。
- `admin-web/src/modules/<domain>`：业务后台模块。
- `admin-web/src/styles`：后台全局样式、layout、Element Plus 覆盖。

## 2. 新增代码落点

- 系统壳、登录、项目切换、用户、管理员、角色、VIP 等跨业务后台能力放入 `modules/system`。
- 具体业务页面、API、类型放入 `src/modules/<domain>`，不要平铺到 `src` 或混入 `modules/system`。
- 后台基础壳统一走 `AdminLayout.vue`；页面内容优先复用 `PageContainer.vue`。
- 菜单配置统一维护在 `modules/system/config/menu.ts`，不要在页面内手写散乱菜单。
- 后台主题偏好和壳级 UI 状态进入 `src/app/store`；登录态进入 `modules/system/store/session.ts`。
- 后台全局 token、亮暗主题、layout 和 Element Plus 覆盖统一收口到 `src/styles`。

## 3. 配套同步

- 新业务页若可达，检查是否需要同步 `router`、`menu.ts`、项目切换入口。
- 涉及项目上下文，优先复用现有 `projectCode` 路由参数和 session 逻辑。
- 需要统一观感时，优先改全局样式或 Element Plus 覆盖，不在业务页重复铺一套皮肤。
