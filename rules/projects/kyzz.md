# KYZZ Rules

<!-- ai-index: kyzz project rules -->

适用范围：考研政治业务。

## 1. 文件索引

- 后端：`backend/backend-kyzz/src/main/java/org/example/backend/biz/kyzz`
- 小程序页面：`uniapp/pages/kyzz`
- 小程序组件：`uniapp/components/kyzz`
- 小程序预加载：`uniapp/shared/preload/kyzz.ts`
- 后台模块：`admin-web/src/modules/kyzz`
- 项目目录：`backend/backend-common/src/main/java/org/example/backend/shared/project/support/ProjectCatalog.java`

## 2. 当前状态

- `kyzz` 是当前最完整的业务域，已有后端、小程序、后台整套实现。
- 后台项目路由虽然使用 `:projectCode`，但当前完整业务页仍主要由 `kyzz` 模块承载。
- 小程序默认项目、原生 tabBar 占位和首屏主入口目前也以 `kyzz` 为基准。

## 3. 新增代码约束

- 新增政治业务后端代码，优先落到 `biz/kyzz` 或 `biz/kyzz/admin`。
- 新增政治业务小程序代码，优先落到 `pages/kyzz`、`components/kyzz`。
- 新增政治业务后台代码，优先落到 `admin-web/src/modules/kyzz`。
- 不跨项目直接复用 `kyzz` 私有 DTO、Service、页面逻辑；若确实可复用，先抽到 shared/common 级别。
- 政治小程序“我的页”复用 `components/account/mine-page.vue`；专项插槽当前填充刷题设置、重置刷题进度、意见反馈入口，政治页只保留这些专项交互逻辑。
