# Codex Project Rules

本文件用于约束 Codex 在本仓库中的默认开发方式。除非用户明确要求例外，后续实现、重构、补功能、修 bug 时都按本规则执行。

## 1. 总体原则

- 先看结构，再写代码；不要在未确认目录归属前直接新增文件。
- 优先复用现有共享能力，不重复造一套登录、用户、上传、请求层。
- 新业务要按业务域收束，不能继续把代码平铺到根目录。
- 涉及目录移动、跨模块重构、统一风格调整时，优先做增量整理，不大面积推翻现有实现。
- 非用户明确要求，不擅自引入新框架、新状态管理、新 UI 风格体系。

## 2. 后端目录规则

后端根包：`backend/src/main/java/org/example/backend`

### 2.1 目录定位

- `common`
  - 只放框架基础设施。
  - 包括统一返回、全局异常、全局配置、filter、web 基础能力。
  - 不放具体业务逻辑，不放用户资料、VIP、题库实体。

- `shared`
  - 放跨业务复用能力。
  - 当前共用模块：
  - `shared/account`：用户、VIP、资料、账号聚合能力。
  - `shared/auth`：微信登录、登出、登录态接入。
  - `shared/security`：登录上下文、Sa-Token 统一封装。
  - `shared/storage`：上传、本地文件存储、URL 解析。

- `biz`
  - 放具体业务域。
  - 例如：
  - `biz/kyzz`：考研政治。
  - 后续 `biz/kysx`：考研数学。

### 2.2 新代码落点规则

- 如果能力可被多个业务复用，优先进入 `shared`。
- 如果只服务一个业务，必须进入 `biz/<domain>`。
- Controller、Service、DTO、Entity、Mapper 在各自模块内部闭环，不跨业务乱放。
- 不再新增 `user`、`auth`、`file` 这种根级平铺模块；新能力应先判断是 `shared` 还是 `biz/<domain>`。

### 2.3 后端文件快速定位

- 登录相关：`shared/auth`
- 当前用户/VIP/资料：`shared/account`
- 上传与静态文件：`shared/storage`
- 登录态与鉴权：`shared/security`、`common/config`
- 统一接口出参与异常：`common/api`、`common/exception`
- 某个具体业务的数据模型和 mapper：`biz/<domain>`

## 3. 前端目录规则

前端根目录：`uniapp`

### 3.1 目录定位

- `shared`
  - 放跨业务复用基础设施。
  - `shared/config`：环境配置、域名配置。
  - `shared/network`：统一请求与上传封装。
  - `shared/session`：token、用户缓存、启动鉴权。
  - `shared/api`：通用账号/资料/登录接口。
  - `shared/platform`：微信原生能力适配。
  - `shared/auth`：本地认证存储等轻量辅助。

- `pages/common`
  - 放跨业务共用页面。
  - 当前包括“我的”、“资料编辑”等。

- `pages/<domain>`
  - 放业务页面。
  - 当前 `pages/kyzz` 对应考研政治。
  - 后续新增业务时按 `pages/kysx` 这种方式扩展。

- `components`
  - 放全局通用组件。

- `components/<domain>`
  - 放业务专属组件。
  - 例如 `components/kyzz`。

### 3.2 新代码落点规则

- 通用请求、登录态、用户资料读取，不得在业务页内重复封装，统一走 `shared`。
- 业务接口不要继续堆到 `shared/api`；只有跨业务复用的接口才允许进入 `shared/api`。
- 某个业务自己的 API、组件、工具，优先在对应业务目录中补齐。
- “我的”、VIP、资料页默认视为跨业务共用能力，除非用户明确要求某业务做专属版本。

### 3.3 前端文件快速定位

- 环境配置：`uniapp/shared/config`
- 请求封装：`uniapp/shared/network`
- 登录态：`uniapp/shared/session`
- 用户与登录 API：`uniapp/shared/api`
- 微信头像昵称工具：`uniapp/shared/platform`
- 公共页面：`uniapp/pages/common`
- 业务页面：`uniapp/pages/<domain>`
- 通用组件：`uniapp/components`
- 业务组件：`uniapp/components/<domain>`

## 4. 前端统一风格规则

前端开发必须参考当前统一风格，不允许每个页面单独起一套视觉语言。

### 4.1 风格基线

- 全局主题 token 以 `uniapp/uni.scss` 为准。
- 颜色、阴影、圆角、字体优先复用已有 token。
- 页面视觉保持当前项目的“浅色、克制、学院感”方向。
- 默认使用白底或浅灰层级背景，不擅自切成深色风格。
- 默认使用已有字体系：
  - 正文：`$body-font-family`
  - 标题：`$heading-font-family`

### 4.2 明确禁止

- 不要引入新的主色体系，尤其不要突然改成紫色系、霓虹风、纯黑科技风。
- 不要大量使用高饱和渐变、发光描边、玻璃拟态覆盖全页。
- 不要在不同页面中随意更换圆角尺度、阴影强度、按钮样式。
- 不要绕开 `page-shell`、`custom-tabbar`、现有 token 去硬写一套完全不同的容器规范。

### 4.3 页面实现约束

- 页面优先复用 `page-shell`、`custom-tabbar`、已有公共布局。
- 样式里优先引用 `uni.scss` 中的 token，不直接写大量魔法值。
- 新页面应先对齐已有页面的间距、留白、卡片层级、文案密度。
- 表单页、资料页、用户中心页默认沿用当前“卡片 + 柔和边界 + 浅层背景”的表现。
- 如果要做业务差异化，只允许在统一基线之上做局部变化，不允许完全脱离当前项目视觉。

## 5. 接口与鉴权规则

- 前端请求统一走 `uniapp/shared/network/request.js`。
- 登录态统一走 `uniapp/shared/session/session.js`。
- 不在页面里直接手写 token 存取。
- 后端涉及当前用户的业务逻辑，统一从登录态取用户 ID，不信任前端传入的 `userId`。
- 通用返回体、异常出参、鉴权处理必须延续现有基础设施，不允许单个模块自定义一套响应结构。

## 6. 修改前的判断顺序

Codex 在新增功能前，默认按以下顺序判断落点：

1. 这是基础设施还是具体业务？
2. 这是跨业务复用还是单业务专属？
3. 后端应落到 `common`、`shared` 还是 `biz/<domain>`？
4. 前端应落到 `shared`、`pages/common` 还是 `pages/<domain>`？
5. UI 是否已对齐 `uni.scss` 和现有页面风格？

如果以上任一项不明确，先查现有目录和相邻页面，再动手实现。
