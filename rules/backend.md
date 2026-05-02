# Backend Rules

<!-- ai-index: backend package rules -->

适用范围：`backend/**`

后端根包：`backend/src/main/java/org/example/backend`

## 1. 包索引

- `BackendApplication.java`：启动入口。
- `common`：统一返回、全局异常、全局配置、filter、web 基础能力。
- `health`：健康检查接口。
- `shared/account`：用户、VIP、资料、账号聚合能力。
- `shared/admin`：后台登录、管理员、角色、项目权限、后台安全上下文。
- `shared/auth`：微信小程序登录、登出、登录态接入。
- `shared/security`：登录上下文、Sa-Token 封装、反爬等安全基础能力。
- `shared/storage`：上传、本地文件存储、URL 解析。
- `util/password`：密码相关底层工具；不要放业务逻辑。
- `biz/<domain>`：单业务闭环目录。
- `biz/<domain>/admin`：单业务后台能力目录。
- `backend/src/main/resources/application.yml`：公共配置。
- `backend/src/main/resources/application-*.example.yml`：环境配置模板。
- `backend/src/main/resources/db/schema.sql`：数据库结构基准。

## 2. 新增代码落点

- 可被多个业务复用的能力进入 `shared/<capability>`。
- 只服务一个业务的能力进入 `biz/<domain>`；后台业务能力放入 `biz/<domain>/admin`。
- Controller、Service、DTO、Entity、Mapper 优先在模块内部闭环，不跨业务引用私有 DTO 或 Service。
- 不新增根级 `user`、`auth`、`file`、`admin` 等平铺业务包；先判断属于 `shared` 还是 `biz/<domain>`。
- `common` 不放具体用户、VIP、题库、业务管理逻辑。
- 涉及当前用户的业务逻辑，统一从登录态取用户 ID，不信任前端传入的 `userId`。
- 接口出参、异常出参和鉴权处理延续现有 `common`、`shared/security` 方式。

## 3. 数据库变更

- 表结构改变时，新建可执行的增量 SQL 供本地执行。
- 同步维护 `backend/src/main/resources/db/schema.sql`。
- 增量 SQL 是否纳入 git，遵循仓库现有 `.gitignore` 规则。
