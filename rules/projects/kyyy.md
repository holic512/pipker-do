# KYYY Rules

<!-- ai-index: kyyy project rules -->

适用范围：考研英语业务。

## 1. 文件索引

- 后端：`backend/backend-kyyy/src/main/java/org/example/backend/biz/kyyy`
- 小程序页面：`uniapp/pages/kyyy`
- 小程序组件：`uniapp/components/kyyy`
- 后台模块：`admin-web/src/modules/kyyy`
- 小程序项目目录：`uniapp/shared/project/index.ts`
- 项目编码注册：`backend/backend-common/src/main/java/org/example/backend/shared/project/support/ProjectCatalog.java`
- 后台项目编码注册：`backend/backend-common/src/main/java/org/example/backend/shared/admin/support/AdminProjectCatalog.java`

## 2. 当前状态

- `kyyy` 已接入小程序项目目录，并在 `pages.json` 中注册了首页、作文、练习、翻译、我的、资料编辑等入口。
- `kyyy` 的后端模块和后台模块目前仍以骨架/占位为主，尚未像 `kyzz` 一样接入完整业务菜单、业务路由和表结构。
- 后续开发可参考 `kyzz`，但应按增量迁移，不建议一次性大范围复制整套实现。

## 3. 新增代码约束

- 后端新增英语业务代码，统一放到 `biz/kyyy` 或 `biz/kyyy/admin`。
- 小程序新增英语业务代码，统一放到 `pages/kyyy`、`components/kyyy`。
- 后台新增英语业务代码，统一放到 `admin-web/src/modules/kyyy`。
- 涉及后台可达入口时，同步检查是否需要补 `router`、`menu.ts`、项目切换落地页。
- 涉及小程序可达入口时，同步检查是否需要补 `pages.json`、`shared/navigation`、`shared/launch`。
- 英语小程序“我的页”复用 `components/account/mine-page.vue`；当前专项插槽留空，不渲染空专项分区，后续有英语专属入口时再通过 `#special` 补入。
