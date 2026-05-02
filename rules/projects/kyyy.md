# KYYY Rules

<!-- ai-index: kyyy project rules -->

适用范围：考研英语业务。

## 1. 文件索引

- 后端：`backend/src/main/java/org/example/backend/biz/kyyy`
- 小程序页面：`uniapp/pages/kyyy`
- 小程序组件：`uniapp/components/kyyy`
- 后台模块：`admin-web/src/modules/kyyy`
- 项目编码注册：`backend/src/main/java/org/example/backend/shared/admin/support/AdminProjectCatalog.java`

## 2. 当前状态

- `kyyy` 目前是初始化骨架，目录已建好，但业务菜单、业务路由、业务页面和表结构还未完整接入。
- 后续开发可参考 `kyzz`，但应按增量迁移，不建议一次性大范围复制整套实现。

## 3. 新增代码约束

- 后端新增英语业务代码，统一放到 `biz/kyyy` 或 `biz/kyyy/admin`。
- 小程序新增英语业务代码，统一放到 `pages/kyyy`、`components/kyyy`。
- 后台新增英语业务代码，统一放到 `admin-web/src/modules/kyyy`。
- 涉及后台可达入口时，同步检查是否需要补 `router`、`menu.ts`、项目切换落地页。
- 涉及小程序可达入口时，同步检查是否需要补 `pages.json`、`shared/navigation`、`shared/launch`。
