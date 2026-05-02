# KYSX Rules

<!-- ai-index: kysx project rules -->

适用范围：考研数学业务。

## 1. 文件索引

- 后台模块占位：`admin-web/src/modules/kysx`
- 项目编码注册：`backend/src/main/java/org/example/backend/shared/admin/support/AdminProjectCatalog.java`

## 2. 当前状态

- `kysx` 当前仍以后台占位为主，还没有像 `kyzz` 那样完整的后端、小程序、后台闭环实现。

## 3. 后续建设约束

- 若继续建设后端，新增到 `backend/src/main/java/org/example/backend/biz/kysx`。
- 若继续建设小程序，新增到 `uniapp/pages/kysx`、`uniapp/components/kysx`。
- 若继续建设后台，新增到 `admin-web/src/modules/kysx`。
- 参考 `kyzz` 的目录组织方式，但不要把 `kyzz` 私有实现直接平铺复制到根级目录。
