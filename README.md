# Pipker Do

<p align="center">
  <strong>One repository for learner flows, operator workflows, and three postgraduate exam domains.</strong>
  <br />
  Spring Boot backend, Vue admin console, and uni-app mini-program client in one monorepo.
</p>

<p align="center">
  <a href="./README.md">English</a>
  ·
  <a href="./README.zh-Hans.md">简体中文</a>
</p>

<p align="center">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-1f6feb?style=flat-square" />
  <img alt="Spring Boot 4.0.5" src="https://img.shields.io/badge/Spring%20Boot-4.0.5-3fb950?style=flat-square" />
  <img alt="Vue 3" src="https://img.shields.io/badge/Vue-3-42b883?style=flat-square" />
  <img alt="TypeScript 5.x" src="https://img.shields.io/badge/TypeScript-5.x-3178c6?style=flat-square" />
  <img alt="uni-app" src="https://img.shields.io/badge/uni--app-WeChat%20Mini%20Program-07c160?style=flat-square" />
</p>

## Features

- A Spring Boot 4 multi-module backend
- A Vue 3 admin console for operations and management
- A uni-app WeChat Mini Program for end-user learning flows
- Three exam domains:
  - `kyzz` for politics
  - `kyyy` for English
  - `kysx` for mathematics

The repository already contains question-bank, practice, exam, leaderboard, favorites, wrong-book review, profile, file storage, authentication, and project-switching related code and docs.

## Screenshots

These images come from the current `image/` folder in the repository.

| Question bank | Continue practice | Project switch |
| --- | --- | --- |
| ![Question bank screen](./image/01-政治-我的题库页.png) | ![Continue practicing screen](./image/02-政治-首页-继续刷题.png) | ![Project switch drawer](./image/03-政治-切换项目抽屉.png) |

| Practice answer | English word study | Account center |
| --- | --- | --- |
| ![Practice answer screen](./image/04-政治-刷题答题页.png) | ![English word study screen](./image/05-英语-单词学习页.png) | ![Account center screen](./image/06-我的-会员中心页.png) |

| Composition knowledge base | Reading practice | Translation knowledge base |
| --- | --- | --- |
| ![Composition knowledge base](./image/07-英语-作文知识库页.png) | ![Reading practice screen](./image/09-英语-阅读练习页.png) | ![Translation knowledge base](./image/10-英语-翻译知识库页.png) |

## Architecture

![System architecture](./image/system-architecture.png)

## Tech Stack

### Backend

- Java `17`
- Spring Boot `4.0.5`
- Maven multi-module
- MyBatis-Plus
- Sa-Token
- Redis integration
- MySQL datasource configuration
- OpenAI Java SDK present in dependency management

### Admin Web

- Vue `3`
- Vite
- TypeScript
- Element Plus
- Pinia
- Axios

### Mobile Client

- uni-app
- WeChat Mini Program target

## Configuration

| Item | Value | Notes |
| --- | --- | --- |
| Backend port | `8080` | Default Spring Boot port in the repo |
| Spring profile | `dev` | Default runtime profile |
| Admin API base path | `/api` | Proxied to `http://localhost:8080` in development |
| Backend config template | `backend/backend-app/src/main/resources/application-dev.example.yml` | Local example config |
| Image compression config | `storage.image-compression` | Present in backend application config |

## Getting Started

### Backend

Verified from the repository:

- Main class: `org.example.backend.BackendApplication`
- Default port: `8080`
- Default Spring profile: `dev`
- Example local config: `backend/backend-app/src/main/resources/application-dev.example.yml`

Run locally:

```bash
cd backend
mvn spring-boot:run -pl backend-app
```

Before starting, review these local settings:

- MySQL datasource
- Redis connection
- Local file storage path
- WeChat Mini Program credentials
- LLM config secret

### Admin Web

Verified from `admin-web/package.json` and `.env.development`:

- API base path: `/api`
- Proxy target: `http://localhost:8080`

Run locally:

```bash
cd admin-web
npm install
npm run dev
```

Build:

```bash
npm run build
```

### Uni-App

Current repository entrypoints:

- `uniapp/App.vue`
- `uniapp/main.js`
- `uniapp/pages.json`
- `uniapp/manifest.json`

Open `uniapp/` in HBuilderX or your existing uni-app workflow and run it against the WeChat Mini Program target.

## Development

```bash
cd backend
mvn test

cd admin-web
npm run build

cd backend
mvn spring-boot:run -pl backend-app
```

## Project Structure

```text
pipker-do/
├── admin-web/      # Vue admin console
├── backend/        # Spring Boot multi-module backend
│   ├── backend-app
│   ├── backend-common
│   ├── backend-kyyy
│   ├── backend-kysx
│   └── backend-kyzz
├── image/          # local screenshots and architecture assets
├── uniapp/         # uni-app WeChat Mini Program
├── rules/          # collaboration and architecture rules
├── 有关文档/        # product and design docs
└── file/           # local upload/runtime files
```

### Backend Modules

- `backend-app`: application entry and runtime configuration
- `backend-common`: shared auth, account, admin, project, config, storage, and LLM-related code
- `backend-kyzz`: politics business domain
- `backend-kyyy`: English business domain
- `backend-kysx`: mathematics business domain

### Admin Modules

- `system`: login, dashboard, project switching, backend shell
- `kyzz`: politics operations module
- `kyyy`: English operations module
- `kysx`: mathematics module scaffold

### Mini Program Pages

- `pages/common`: shared cross-business pages
- `pages/kyyy`: English learning and practice pages
- `pages/kyzz`: politics learning and practice pages

## Existing Docs

- [rules/README.md](./rules/README.md)
- [rules/common.md](./rules/common.md)
- [admin-web/README.md](./admin-web/README.md)
- [uniapp/README_STRUCTURE.md](./uniapp/README_STRUCTURE.md)
- [有关文档/页面功能描述.md](./有关文档/页面功能描述.md)
- [有关文档/管理员管理设计.md](./有关文档/管理员管理设计.md)
- [有关文档/上线流程.md](./有关文档/上线流程.md)

## License

TODO

## Notes

The repository already contains enough source and docs to describe the current system shape. The following items remain unverified and should stay `TODO` until they are available:

- Public production URL
- Official branding assets
- Open-source license
- Release workflow
- Contribution guide
