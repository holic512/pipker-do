# Uniapp Rules

<!-- ai-index: uniapp directory rules -->

适用范围：`uniapp/**`

## 1. 目录索引

- `uniapp/pages.json`：页面路由与 tabBar 配置。
- `uniapp/uni.scss`：全局主题 token。
- `uniapp/shared/config`：环境和域名配置。
- `uniapp/shared/network/request.ts`：统一请求与上传封装。
- `uniapp/shared/session/session.ts`：token、当前用户缓存、启动鉴权。
- `uniapp/shared/api`：跨业务复用的账号、登录、用户、VIP 接口。
- `uniapp/shared/auth`：协议确认、本地认证存储等轻量辅助。
- `uniapp/shared/platform`：微信头像昵称、设备等平台适配。
- `uniapp/shared/launch`、`shared/preload`、`shared/media`、`shared/navigation`：启动与通用辅助能力。
- `uniapp/pages/common`：跨业务共用页面。
- `uniapp/pages/<domain>`：业务页面。
- `uniapp/pages/<domain>/api`：业务接口。
- `uniapp/components/page-shell`、`custom-navbar`、`custom-tabbar`：通用页面壳和导航组件。
- `uniapp/components/<domain>`：业务组件。
- `uniapp/static`：静态资源。

## 2. 新增代码落点

- 通用请求、登录态、用户资料读取统一走 `shared`，不在业务页重复封装。
- 只有跨业务复用接口才放 `shared/api`；业务接口放入 `pages/<domain>/api` 或该业务已有 API 目录。
- 业务页面放入 `pages/<domain>`，业务组件放入 `components/<domain>`。
- 新增或重构业务代码默认使用 TypeScript；业务接口、业务类型、页面脚本优先使用 `.ts` 与 `<script lang="ts">`。
- 接口返回、筛选条件、页面状态、组件入参必须补充明确类型，不让隐式 `any` 长期扩散。
- “我的”、VIP、资料页默认视为跨业务共用能力，除非用户明确要求某业务专属版本。

## 3. 配套同步

- 新页面若参与导航，检查是否需要同步 `pages.json`。
- 新 tab 或启动入口，检查 `shared/navigation`、`shared/launch`、相邻页面跳转逻辑。
- 业务预加载、业务缓存、业务协议状态，优先沿用现有 shared 机制。
