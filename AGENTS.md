# Codex Project Rules

<!-- ai-index: repo rules, directory ownership, file lookup guide -->

本文件约束 Codex 在本仓库中的默认开发方式。除非用户明确要求例外，后续实现、重构、补功能、修 bug 时都按本规则执行。

## 1. 工作原则

- 先看结构，再写代码；新增文件前先确认归属目录和相邻实现。
- 优先复用现有共享能力，不重复实现登录、用户、上传、请求层、后台壳或主题体系。
- 新业务按业务域收束，不把 Controller、页面、API、工具继续平铺到根级目录。
- 涉及目录移动、跨模块重构、统一风格调整时，优先做增量整理，避免大面积推翻现有实现。
- 非用户明确要求，不引入新框架、新状态管理、新 UI 风格体系。
- 不编辑生成物、依赖目录和本地运行数据：`backend/target`、`backend/.m2repo`、`admin-web/node_modules`、`admin-web/dist`、`uniapp/uni_modules`、`uniapp/unpackage`、`file`、`.idea`。

## 2. 根目录索引

<!-- ai-index: root directory map -->

- `backend`：Spring Boot 后端接口与数据库结构基准。
- `uniapp`：uni-app 小程序端。
- `admin-web`：Vue 3 PC 后台。
- `有关文档`：产品、上线、管理端设计等说明文档。
- `.github`：仓库 CI 配置。
- `file`：本地上传和运行文件，默认不纳入代码变更。

## 3. 后端规则

后端根包：`backend/src/main/java/org/example/backend`

<!-- ai-index: backend package ownership -->

- `BackendApplication.java`：后端启动入口。
- `common`：框架基础设施，只放统一返回、全局异常、全局配置、filter、web 基础能力。
- `health`：健康检查接口。
- `shared/account`：用户、VIP、资料、账号聚合能力。
- `shared/admin`：后台登录、管理员、角色、项目权限、后台安全上下文。
- `shared/auth`：微信小程序登录、登出、登录态接入。
- `shared/security`：登录上下文、Sa-Token 封装、反爬等安全基础能力。
- `shared/storage`：上传、本地文件存储、URL 解析。
- `biz/kyzz`：考研政治业务域；用户端 Controller/Service/DTO/Entity/Mapper 在该目录闭环。
- `biz/kyzz/admin`：考研政治后台业务接口、DTO、Service 和后台辅助能力。
- `util/password`：密码相关底层工具；不要把业务逻辑放入 `util`。
- `backend/src/main/resources/application.yml`：公共配置。
- `backend/src/main/resources/application-*.example.yml`：环境配置模板。
- `backend/src/main/resources/db/schema.sql`：数据库结构基准。

后端新增代码落点：

- 可被多个业务复用的能力进入 `shared/<capability>`。
- 只服务一个业务的能力进入 `biz/<domain>`；后台业务能力放入 `biz/<domain>/admin`。
- Controller、Service、DTO、Entity、Mapper 优先在模块内部闭环，不跨业务引用私有 DTO 或 Service。
- 不新增根级 `user`、`auth`、`file`、`admin` 等平铺业务包；先判断属于 `shared` 还是 `biz/<domain>`。
- `common` 不放具体用户、VIP、题库、业务管理逻辑。
- 涉及当前用户的业务逻辑，统一从登录态取用户 ID，不信任前端传入的 `userId`。
- 接口出参、异常出参和鉴权处理延续 `common/api`、`common/exception`、`shared/security` 的现有方式。
- 表结构改变时新建可执行的增量 SQL 文件用于本地执行，并同步维护 `backend/src/main/resources/db/schema.sql`；按 `.gitignore` 规则，增量 SQL 默认不纳入 git。

## 4. uniapp 规则

前端小程序根目录：`uniapp`

<!-- ai-index: uniapp directory ownership -->

- `uniapp/pages.json`：页面路由与 tabBar 配置。
- `uniapp/uni.scss`：全局主题 token，包含主色、中性色、圆角、阴影、字体等基础变量。
- `uniapp/shared/config`：环境和域名配置。
- `uniapp/shared/network/request.ts`：统一请求与上传封装。
- `uniapp/shared/session/session.ts`：token、当前用户缓存、启动鉴权。
- `uniapp/shared/api`：跨业务复用的账号、登录、用户、VIP 接口。
- `uniapp/shared/auth`：协议确认、本地认证存储等轻量辅助。
- `uniapp/shared/platform`：微信头像昵称、设备等平台适配。
- `uniapp/shared/launch`、`shared/preload`、`shared/media`、`shared/navigation`：启动、预加载、媒体缓存和导航辅助。
- `uniapp/pages/common`：跨业务共用页面，如启动页、协议页、我的、资料编辑。
- `uniapp/pages/kyzz`：考研政治业务页面。
- `uniapp/pages/kyzz/api`：考研政治业务接口。
- `uniapp/components/page-shell`、`custom-navbar`、`custom-tabbar`：通用页面壳和导航组件。
- `uniapp/components/kyzz`：考研政治业务组件。
- `uniapp/static`：小程序静态资源。

uniapp 新增代码落点：

- 通用请求、登录态、用户资料读取统一走 `shared`，不在业务页重复封装。
- 只有跨业务复用接口才放 `shared/api`；业务接口放入 `pages/<domain>/api` 或该业务已有 API 目录。
- 业务页面放入 `pages/<domain>`，业务组件放入 `components/<domain>`。
- 新增或重构业务代码默认使用 TypeScript；业务接口、业务类型、页面脚本优先使用 `.ts` 与 `<script lang="ts">`。
- 接口返回、筛选条件、页面状态、组件入参必须补充明确类型，不让隐式 `any` 长期扩散。
- “我的”、VIP、资料页默认视为跨业务共用能力，除非用户明确要求某业务专属版本。

## 5. admin-web 规则

后台根目录：`admin-web`

<!-- ai-index: admin-web directory ownership -->

- `admin-web/src/main.ts`、`App.vue`：后台启动入口。
- `admin-web/src/app/router`：后台路由和守卫。
- `admin-web/src/app/layout/AdminLayout.vue`：统一后台壳。
- `admin-web/src/app/store/theme.ts`：后台主题状态。
- `admin-web/src/shared/http/client.ts`：Axios 请求封装。
- `admin-web/src/shared/components/PageContainer.vue`：后台页面容器。
- `admin-web/src/shared/types`、`constants`、`utils`：后台共享类型、常量、工具。
- `admin-web/src/modules/system`：登录、工作台、管理员、角色、用户、VIP、项目切换等通用后台能力。
- `admin-web/src/modules/system/config/menu.ts`：后台菜单配置。
- `admin-web/src/modules/system/store/session.ts`：后台登录 session。
- `admin-web/src/modules/kyzz`：考研政治后台业务模块。
- `admin-web/src/modules/kysx`：考研数学后台模块占位或后续业务模块。
- `admin-web/src/styles`：后台全局样式、layout、Element Plus 覆盖。

admin-web 新增代码落点：

- 系统壳、登录、项目切换、用户、管理员、角色、VIP 等跨业务后台能力放入 `modules/system`。
- 具体业务页面、API、类型放入 `src/modules/<domain>`，不要平铺到 `src` 或混入 `modules/system`。
- 后台基础壳统一走 `AdminLayout.vue`；页面内容优先复用 `PageContainer.vue`。
- 菜单配置统一维护在 `modules/system/config/menu.ts`，不要在页面内手写散乱菜单。
- 后台主题偏好和壳级 UI 状态进入 `src/app/store`；登录态进入 `modules/system/store/session.ts`。
- 后台全局 token、亮暗主题、layout 和 Element Plus 覆盖统一收口到 `src/styles`，不要在单页 scoped 样式里重复定义整套后台皮肤。

## 6. 前端风格规则

<!-- ai-index: frontend style baseline -->

- `uniapp` 和 `admin-web` 延续同一品牌基线：浅色、克制、学院感。
- 颜色、阴影、圆角、字体优先复用已有 token；`admin-web` 样式参考 `uniapp/uni.scss` 的主色、中性色、圆角和阴影尺度。
- 页面优先复用 `page-shell`、`custom-tabbar`、`custom-navbar`、`AdminLayout`、`PageContainer` 等已有公共布局。
- 表单页、资料页、用户中心页默认沿用当前“卡片 + 柔和边界 + 浅层背景”的表现。
- 不引入新的主色体系，不突然改成紫色系、霓虹风、纯黑科技风、全页玻璃拟态或高饱和渐变。
- 不在不同页面中随意更换圆角尺度、阴影强度、按钮样式。
- 后台页如需统一观感，优先改 `admin-web/src/styles/element-overrides.scss` 或全局样式，不直接在业务页覆盖 Element Plus 大片结构。

## 7. 文件索引注释

<!-- ai-index: ai-search comment convention -->

为方便后续定位，新建或大幅改动的业务入口文件可以在文件顶部加入一行简短索引注释。索引只写“文件职责 + 关键业务词”，不要给每个函数都加注释，不制造噪声。

推荐格式：

- Java：`// ai-index: kyzz practice user controller`
- TypeScript：`// ai-index: admin kyzz question bank api`
- Vue：`<!-- ai-index: uniapp kyzz practice page -->`
- SCSS：`/* ai-index: admin layout theme styles */`
- SQL：`-- ai-index: add kyzz practice setting table`

已有文件没有索引时，不需要为了补索引单独改文件；只有在本次确实编辑该文件时顺手补一行。

## 8. 修改前判断顺序

<!-- ai-index: change decision checklist -->

1. 这是基础设施、跨业务能力，还是单业务能力？
2. 后端应落到 `common`、`shared`、`biz/<domain>` 还是 `biz/<domain>/admin`？
3. uniapp 应落到 `shared`、`pages/common`、`pages/<domain>`、`components/<domain>` 还是业务 API 目录？
4. admin-web 应落到 `app`、`shared`、`modules/system` 还是 `modules/<domain>`？
5. 是否已经复用统一请求、登录态、返回体、异常处理和鉴权能力？
6. UI 是否对齐 `uniapp/uni.scss`、`admin-web/src/styles` 和相邻页面风格？
7. 是否需要同步 `schema.sql`、增量 SQL、本地配置模板或菜单配置？

如果任一项不明确，先查现有目录、相邻页面、README 或已有接口，再动手实现。
