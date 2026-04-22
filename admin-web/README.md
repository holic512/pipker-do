# Admin Web

独立后台管理前端项目，技术栈为 `Vue 3 + Vite + TypeScript + Element Plus + Axios`。

## 目录

- `src/app`: 应用启动、router、layout
- `src/shared`: 通用请求层、类型、组件、工具
- `src/modules/system`: 登录、工作台、项目切换、后台基础壳
- `src/modules/kyzz` / `src/modules/kysx`: 后续项目业务模块

## 环境

- `npm install`
- `npm run dev`
- `npm run build`

默认开发代理会将 `/api` 转发到 `http://localhost:8080`。
