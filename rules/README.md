# Rules Index

<!-- ai-index: rules directory index -->

本目录存放可按需读取的项目规则。目标是让根 `AGENTS.md` 保持轻量，只在当前任务真正需要时打开相关规则文件。

## 1. 基础规则

- [common.md](common.md)：全仓库通用原则、根目录索引、禁改目录、修改前检查。
- [frontend-style.md](frontend-style.md)：`uniapp` 和 `admin-web` 共用的前端风格基线。

## 2. 端侧规则

- [backend.md](backend.md)：Spring Boot 后端目录归属、包落点、SQL 同步约束。
- [uniapp.md](uniapp.md)：小程序目录归属、共享能力与业务页落点。
- [admin-web.md](admin-web.md)：后台目录归属、菜单/路由/样式落点。

## 3. 项目规则

- [projects/kyzz.md](projects/kyzz.md)：考研政治业务索引与约束。
- [projects/kyyy.md](projects/kyyy.md)：考研英语业务索引与约束。
- [projects/kysx.md](projects/kysx.md)：考研数学业务索引与约束。

## 4. 选读建议

- 只改后端通用能力：`common.md` + `backend.md`
- 只改小程序某业务：`common.md` + `uniapp.md` + `frontend-style.md` + 对应项目规则
- 只改后台某业务：`common.md` + `admin-web.md` + `frontend-style.md` + 对应项目规则
- 同时跨端改某业务：先读对应项目规则，再补读涉及端的规则
