# KYSX Rules

<!-- ai-index: kysx project rules -->

适用范围：考研数学业务。

## 1. 文件索引

- 后端模块占位：`backend/backend-kysx/src/main/java/org/example/backend/biz/kysx`
- 后台模块占位：`admin-web/src/modules/kysx`
- 项目编码注册：`backend/backend-common/src/main/java/org/example/backend/shared/project/support/ProjectCatalog.java`
- 后台项目编码注册：`backend/backend-common/src/main/java/org/example/backend/shared/admin/support/AdminProjectCatalog.java`

## 2. 当前状态

- `kysx` 已在全局项目目录中注册，并具备后端模块占位和后台模块占位。
- `kysx` 当前还没有小程序页面目录，也没有像 `kyzz` 那样完整的后端、小程序、后台闭环实现。

## 3. 后续建设约束

- 若继续建设后端，新增到 `backend/backend-kysx/src/main/java/org/example/backend/biz/kysx`。
- 若继续建设小程序，新增到 `uniapp/pages/kysx`、`uniapp/components/kysx`。
- 若继续建设后台，新增到 `admin-web/src/modules/kysx`。
- 参考 `kyzz` 的目录组织方式，但不要把 `kyzz` 私有实现直接平铺复制到根级目录。
