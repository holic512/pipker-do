# Common Rules

<!-- ai-index: common repo rules -->

适用范围：全仓库。

## 1. 工作原则

- 先看结构，再写代码；新增文件前先确认归属目录和相邻实现。
- 优先复用现有共享能力，不重复实现登录、用户、上传、请求层、后台壳或主题体系。
- 新业务按业务域收束，不把 Controller、页面、API、工具继续平铺到根级目录。
- 涉及目录移动、跨模块重构、统一风格调整时，优先做增量整理，避免大面积推翻现有实现。
- 非用户明确要求，不引入新框架、新状态管理、新 UI 风格体系。

## 2. 根目录索引

- `backend`：Spring Boot Maven 多模块后端，含启动模块、共享模块和业务模块。
- `uniapp`：uni-app 小程序端。
- `admin-web`：Vue 3 PC 后台。
- `rules`：项目规则目录，按任务范围按需读取。
- `有关文档`：产品、上线、管理端设计等说明文档。
- `file`：本地上传和运行文件，默认不纳入代码变更。

## 3. 默认不编辑

- `backend/**/target`
- `backend/.m2repo`
- `admin-web/node_modules`
- `admin-web/dist`
- `uniapp/uni_modules`
- `uniapp/unpackage`
- `file`
- `.idea`

## 4. 文件索引注释

- Java：`// ai-index: kyzz practice user controller`
- TypeScript：`// ai-index: admin kyzz question bank api`
- Vue：`<!-- ai-index: uniapp kyzz practice page -->`
- SCSS：`/* ai-index: admin layout theme styles */`
- SQL：`-- ai-index: add kyzz practice setting table`
- 只有本次确实编辑该文件时，再顺手补索引注释，不为了补注释单独改文件。

## 5. 修改前检查

1. 这是基础设施、跨业务能力，还是单业务能力？
2. 是否已经有可复用的共享实现、相邻页面或相邻接口？
3. 是否需要同步路由、菜单、配置、数据库结构、接口类型或项目索引？
4. 若有不明确项，先查相邻目录、README、已有接口或已有页面，再动手。
