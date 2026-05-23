# Pipker Do

<p align="center">
  <strong>一个仓库，覆盖学习端、运营端与多学科考研业务。</strong>
  <br />
  面向考研备考产品的一体化全栈交付方案。
</p>

<p align="center">
  <a href="./README.md">English</a>
  ·
  <a href="./README.zh-Hans.md">简体中文</a>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-1f6feb?style=flat-square" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.0.5-3fb950?style=flat-square" />
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3-42b883?style=flat-square" />
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5.x-3178c6?style=flat-square" />
  <img alt="uni-app" src="https://img.shields.io/badge/uni--app-%E5%BE%AE%E4%BF%A1%E5%B0%8F%E7%A8%8B%E5%BA%8F-07c160?style=flat-square" />
  <img alt="License" src="https://img.shields.io/badge/License-TODO-8b949e?style=flat-square" />
</p>

## 目录

- [项目介绍](#项目介绍)
- [为什么是 Pipker Do](#为什么是-pipker-do)
- [产品能力](#产品能力)
- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [现有文档](#现有文档)
- [待补充项](#待补充项)

## 项目介绍

Pipker Do 是一个面向考研备考场景的全栈单仓库项目。  
当前仓库已经明确包含：

- Spring Boot 4 多模块后端
- Vue 3 管理后台
- uni-app 微信小程序
- 三个业务学科域：
  - `kyzz`：考研政治
  - `kyyy`：考研英语
  - `kysx`：考研数学

从现有代码结构和配置可以确认，项目已经围绕题库、练习、考试、排行榜、收藏、错题/错本、用户资料、文件存储、鉴权、项目切换以及部分 AI 能力完成了较完整的产品骨架。

## 为什么是 Pipker Do

很多教育产品会把学习端、运营端、后端能力拆成多个分散仓库，后期很容易出现协作成本高、业务边界混乱、重复实现基础能力的问题。  
Pipker Do 当前仓库的价值，在于它已经把这些关键部分统一在同一套工程体系中：

- 一个后端承载共享鉴权、配置、存储与业务编排
- 一个后台承载运营管理与项目切换
- 一个小程序承载真实用户学习流程
- 三个学科域按模块独立演进，而不是把所有功能揉成一个大工程

这不是抽象愿景，而是当前代码目录已经体现出的架构方向。

## 产品能力

### 面向学习用户

- 题库浏览
- 练习与考试流程
- 收藏与错题回顾
- 排行榜与个人资料相关页面
- 英语专项页面：阅读、翻译、作文、单词、AI 练习等

### 面向运营管理

- 后台登录与项目切换
- 按学科拆分的业务管理模块
- 共享账户、权限、配置、存储等后端服务

### 面向研发交付

- 多模块后端，而不是单体堆叠
- 管理后台与小程序端明确分离
- `kyzz`、`kyyy`、`kysx` 三个业务域清晰分层
- 鉴权、上传、限流、运行配置等基础设施已经集中沉淀

## 系统架构

![系统架构图](./image/system-architecture.svg)

## 技术栈

### 后端

- Java `17`
- Spring Boot `4.0.5`
- Maven 多模块
- MyBatis-Plus
- Sa-Token
- Redis 集成
- MySQL 数据源配置
- 依赖管理中已存在 OpenAI Java SDK

### 后台

- Vue `3`
- Vite
- TypeScript
- Element Plus
- Pinia
- Axios

### 小程序端

- uni-app
- 微信小程序目标平台

## 快速开始

### 启动后端

已从仓库中确认：

- 启动类：`org.example.backend.BackendApplication`
- 默认端口：`8080`
- 默认 Profile：`dev`
- 示例本地配置：`backend/backend-app/src/main/resources/application-dev.example.yml`

本地运行：

```bash
cd backend
mvn spring-boot:run -pl backend-app
```

启动前建议先本地确认这些配置：

- MySQL 数据源
- Redis 连接
- 本地文件存储路径
- 微信小程序凭据
- LLM 配置密钥

### 启动管理后台

已从 `admin-web/package.json` 与 `.env.development` 确认：

- API 前缀：`/api`
- 开发代理目标：`http://localhost:8080`

本地运行：

```bash
cd admin-web
npm install
npm run dev
```

构建：

```bash
npm run build
```

### 启动 uni-app 小程序

当前仓库已存在：

- `uniapp/App.vue`
- `uniapp/main.js`
- `uniapp/pages.json`
- `uniapp/manifest.json`

使用 HBuilderX 或你现有的 uni-app 工作流打开 `uniapp/`，然后运行到微信小程序目标即可。

## 项目结构

```text
pipker-do/
├── admin-web/      # Vue 管理后台
├── backend/        # Spring Boot 多模块后端
│   ├── backend-app
│   ├── backend-common
│   ├── backend-kyyy
│   ├── backend-kysx
│   └── backend-kyzz
├── uniapp/         # uni-app 微信小程序
├── rules/          # 协作与架构规则
├── 有关文档/        # 产品与设计说明
└── file/           # 本地上传/运行文件
```

### 后端模块

- `backend-app`：应用启动入口与运行配置
- `backend-common`：共享鉴权、账户、后台、项目、配置、存储、LLM 相关能力
- `backend-kyzz`：考研政治业务域
- `backend-kyyy`：考研英语业务域
- `backend-kysx`：考研数学业务域

### 后台模块

- `system`：登录、工作台、项目切换、后台壳
- `kyzz`：政治运营模块
- `kyyy`：英语运营模块
- `kysx`：数学模块骨架已存在

### 小程序页面分组

- `pages/common`：跨业务共用页面
- `pages/kyyy`：英语学习与练习页面
- `pages/kyzz`：政治学习与练习页面

## 现有文档

- [rules/README.md](/Volumes/HARDDRIVE1/project/private/pipker-do/rules/README.md)
- [rules/common.md](/Volumes/HARDDRIVE1/project/private/pipker-do/rules/common.md)
- [admin-web/README.md](/Volumes/HARDDRIVE1/project/private/pipker-do/admin-web/README.md)
- [uniapp/README_STRUCTURE.md](/Volumes/HARDDRIVE1/project/private/pipker-do/uniapp/README_STRUCTURE.md)
- [有关文档/页面功能描述.md](/Volumes/HARDDRIVE1/project/private/pipker-do/%E6%9C%89%E5%85%B3%E6%96%87%E6%A1%A3/%E9%A1%B5%E9%9D%A2%E5%8A%9F%E8%83%BD%E6%8F%8F%E8%BF%B0.md)
- [有关文档/管理员管理设计.md](/Volumes/HARDDRIVE1/project/private/pipker-do/%E6%9C%89%E5%85%B3%E6%96%87%E6%A1%A3/%E7%AE%A1%E7%90%86%E5%91%98%E7%AE%A1%E7%90%86%E8%AE%BE%E8%AE%A1.md)
- [有关文档/上线流程.md](/Volumes/HARDDRIVE1/project/private/pipker-do/%E6%9C%89%E5%85%B3%E6%96%87%E6%A1%A3/%E4%B8%8A%E7%BA%BF%E6%B5%81%E7%A8%8B.md)

## 待补充项

当前这版 README 已经改成偏产品宣传页的表达方式，但内容仍然严格约束在仓库已确认事实之内。  
下面这些内容如果后续有明确素材，可以继续补强：

- 正式产品名与品牌视觉
- 截图、横幅图、产品演示 GIF
- 对外访问地址
- 开源许可证说明
- 发布流程与贡献指南
