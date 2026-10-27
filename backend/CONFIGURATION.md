# 后端配置

本地只维护 `backend-app/src/main/resources/application.yml`。所有连接信息、密码和开关均直接填写值，不依赖环境变量、不使用 dev/prod Profile，也不使用 `backend/config` 覆盖目录。

## 首次运行

在 `backend` 目录执行：

```bash
cp -n backend-app/src/main/resources/application.example.yml backend-app/src/main/resources/application.yml
```

编辑新文件，填写 MySQL 账号和密码、Redis 连接、微信参数以及 `llm.config-secret`。Redis 无密码时保留空字符串；微信与 COS 凭据按需填写。加密密钥用于保存系统中的敏感配置，已有数据库应保留原密钥。修改端口时同步修改 `storage.local.public-base-url`。

`application.yml` 是真实私有配置，已被 Git 忽略；提交到 GitHub 的是脱敏 `application.example.yml`。IDEA 直接运行 `org.example.backend.BackendApplication` 即可读取资源目录中的配置。Maven 的运行工作目录为 `backend/`，本地上传文件默认保存到仓库根目录的 `file/`。

## Docker 挂载

准备一份完整 YAML，挂载其所在目录，再通过 Java 启动参数明确指定文件：

```bash
docker run --rm -p 8080:8080 \
  --mount type=bind,source=/absolute/path/to/settings,target=/app/config,readonly \
  --entrypoint java IMAGE \
  -jar /app/app.jar --spring.config.location=file:/app/config/application.yml
```

把 `IMAGE` 和 `/absolute/path/to/settings` 换成实际镜像及配置目录；目录中应有 `application.yml`。它需要包含完整配置，挂载目录和文件应允许容器 UID 10001 读取。本地存储与日志也应按配置挂载到宿主机目录。

容器内的 `127.0.0.1` 指容器自身。数据库在 Docker 宿主机时可填写 `host.docker.internal`；Linux Engine 还需添加 `--add-host=host.docker.internal:host-gateway`。数据库在其他机器时填写实际地址。

`--spring.config.location` 替换 Spring Boot 默认配置搜索位置，因此不会混入 JAR 中的本地配置。不要添加 `optional:`，缺失配置应直接启动失败。挂载配置修改后需要重启 Java 进程；目录挂载也能看到脚本通过原子替换更新的文件。[Spring Boot 官方说明](https://docs.spring.io/spring-boot/reference/features/external-config.html)

## 启动错误中的地址

`Access denied for user 'pipkerdo'@'192.168.65.1' (using password: NO)` 表示数据库拒绝了账号 `pipkerdo` 从来源地址 `192.168.65.1` 发起的无密码连接。该 IP 是数据库看到的客户端来源，不是 JDBC 配置的数据库目标地址，因此不要求出现在配置中；经过容器网络转发时两者可以不同。[MySQL 官方说明](https://dev.mysql.com/doc/refman/8.0/en/problems-connecting.html)

出现 `using password: NO` 时，检查实际加载的 `application.yml` 中数据库密码是否为空，并在 IDEA 中重新构建资源后重启。`using password: YES` 表示已携带密码，再核对密码及数据库用户的来源地址授权。
