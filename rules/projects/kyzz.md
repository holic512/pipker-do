# KYZZ Rules

<!-- ai-index: kyzz project rules -->

适用范围：考研政治业务。

## 1. 文件索引

- 后端：`backend/src/main/java/org/example/backend/biz/kyzz`
- 小程序页面：`uniapp/pages/kyzz`
- 小程序组件：`uniapp/components/kyzz`
- 小程序预加载：`uniapp/shared/preload/kyzz.ts`
- 后台模块：`admin-web/src/modules/kyzz`

## 2. 当前状态

- `kyzz` 是当前最完整的业务域，已有后端、小程序、后台整套实现。
- 后台项目路由虽然使用 `:projectCode`，但当前完整业务页主要由 `kyzz` 模块承载。

## 3. 新增代码约束

- 新增政治业务后端代码，优先落到 `biz/kyzz` 或 `biz/kyzz/admin`。
- 新增政治业务小程序代码，优先落到 `pages/kyzz`、`components/kyzz`。
- 新增政治业务后台代码，优先落到 `admin-web/src/modules/kyzz`。
- 不跨项目直接复用 `kyzz` 私有 DTO、Service、页面逻辑；若确实可复用，先抽到 shared/common 级别。
