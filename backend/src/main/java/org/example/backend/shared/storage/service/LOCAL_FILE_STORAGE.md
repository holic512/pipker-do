# 本地文件存储使用说明

本文档说明当前项目的本地文件存储机制怎么配置、怎么调用、返回什么、兼容什么，避免后续维护时忘掉。

## 1. 设计目标

当前本地文件存储不再使用单纯的 `#相对路径` 作为主格式，而是使用结构化存储键：

```text
local:v1:{bizType}:{relativePath}
```

典型示例：

```text
local:v1:avatar:ab/cd/abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890.png
```

这套方案的特点：

- 使用内容哈希命名，同内容文件天然去重。
- 使用哈希分片目录，避免单目录文件过多。
- 存储键包含驱动和版本信息，后续迁移更稳。
- `resolvePath()` 会做路径规范化和越界校验，安全边界更清晰。
- 旧的 `#...` 历史数据仍然可以解析，不会直接失效。

## 2. 相关类

主要实现类：

- `org.example.backend.util.file.LocalFileStorage`
- `org.example.backend.util.file.LocalStorageProperties`
- `org.example.backend.util.file.StorageKey`
- `org.example.backend.util.file.LegacyStorageKeyResolver`
- `org.example.backend.util.file.StorageUrlResolver`
- `org.example.backend.util.file.StoredFileInfo`

## 3. 配置项

`application.yml` 示例：

```yaml
storage:
  local:
    base-path: /data/app-files
    public-base-url: http://localhost:8080/files
    temp-dir: .tmp
    hash-algorithm: SHA-256
    shard-depth: 2
    shard-width: 2
    keep-extension: true
```

配置说明：

- `base-path`
  - 文件存储根目录。
  - 新配置优先使用这个字段。
- `root-dir`
  - 旧字段，仍兼容。
  - 如果没有配置 `base-path`，会自动回退到 `root-dir`。
- `public-base-url`
  - 对外访问前缀。
  - `resolveUrl()` 会基于它拼接完整 URL。
- `temp-dir`
  - 临时文件目录。
  - 相对路径时，会拼到 `base-path` 下。
- `hash-algorithm`
  - 文件哈希算法，默认 `SHA-256`。
- `shard-depth`
  - 哈希分片层级，默认 `2`。
- `shard-width`
  - 每层分片宽度，默认 `2`。
- `keep-extension`
  - 是否保留安全扩展名，默认 `true`。
- `allowed-biz-types`
  - 可选白名单。
  - 配置后，只允许白名单内的 `bizType`。

示例：

```yaml
storage:
  local:
    base-path: /data/app-files
    public-base-url: https://cdn.example.com/files
    allowed-biz-types:
      - avatar
      - flower
      - knowledge
      - doc
```

## 4. 保存文件

最常用的方法：

```java
String storageKey = localFileStorage.save(file, "avatar");
```

返回值示例：

```text
local:v1:avatar:ab/cd/abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890.jpg
```

如果希望一次拿到更多信息，用：

```java
StoredFileInfo info = localFileStorage.saveAndGetInfo(file, "avatar");
```

`StoredFileInfo` 包含：

- `storageKey`
- `url`
- `size`
- `contentType`
- `originalFilename`
- `hash`

### 保存流程

内部实际流程如下：

1. 先把上传内容写到临时文件。
2. 写入过程中同步计算哈希。
3. 基于哈希生成最终存储 key。
4. 如果目标文件已经存在且大小一致，直接复用。
5. 如果不存在，则把临时文件原子移动到最终路径。

这意味着：

- 同内容重复上传不会反复占用磁盘。
- 文件名稳定，不再是随机 UUID。
- 高并发下比直接 `transferTo()` 最终路径更稳。

## 5. 存储路径规则

物理目录规则：

```text
{base-path}/{bizType}/{shard1}/{shard2}/{fullHash}.{ext}
```

示例：

```text
/data/app-files/avatar/ab/cd/abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890.png
```

说明：

- `bizType` 是业务目录，例如 `avatar`、`flower`、`doc`。
- `ab/cd` 是哈希分片目录。
- 文件名主体是完整内容哈希。
- 扩展名只用于展示和 MIME 兜底，不参与唯一性判断。

## 6. 解析 URL

如果数据库里存的是受管存储键，可以这样拿 URL：

```java
String url = localFileStorage.resolveUrl(storedValue);
```

行为规则：

- 如果是新格式 `local:v1:...`，会解析并拼接成完整 URL。
- 如果是旧格式 `#...`，也会兼容解析。
- 如果本来就是第三方完整 URL，会原样返回。

示例：

```java
localFileStorage.resolveUrl("local:v1:avatar:ab/cd/hash.png");
```

结果：

```text
http://localhost:8080/files/avatar/ab/cd/hash.png
```

## 7. Spring 静态资源映射

当前项目已经补上了 Spring MVC 静态资源映射，配置类是：

- `org.example.backend.util.file.LocalFileStorageWebMvcConfigurer`

作用：

- 自动把 `storage.local.public-base-url` 的路径部分提取出来
- 再把这个路径映射到 `storage.local.base-path`

例如：

- `public-base-url = http://localhost:8080/files`
- `base-path = /data/app-files`

最终等价于：

- 请求 `/files/**`
- 实际读取 `/data/app-files/**`

如果以后改成：

- `public-base-url = https://cdn.example.com/static/files`

那么 Spring 会自动映射为：

- `/static/files/** -> base-path`

也就是说，`resolveUrl()` 负责“生成 URL”，`LocalFileStorageWebMvcConfigurer` 负责“让这个 URL 真能访问到本地文件”。

## 8. 解析本地路径

如果要拿磁盘绝对路径：

```java
Path path = localFileStorage.resolvePath(storedValue);
```

行为规则：

- 新格式 key 可以解析。
- 旧格式 `#...` 也可以解析。
- 非受管值，例如第三方 URL，会返回 `null`。
- 非法路径或越界路径会直接抛异常，不会返回危险路径。

## 9. 删除文件

删除受管文件：

```java
boolean deleted = localFileStorage.deleteByKey(storedValue);
```

行为规则：

- 文件存在则删除。
- 文件不存在也视为成功。
- 非受管值返回 `false`。

兼容旧方法：

```java
boolean deleted = localFileStorage.deleteByTag(storedValue);
```

但这个方法已经是兼容接口，后续新代码统一用 `deleteByKey()`。

## 10. 判断是否为本地存储管理的值

推荐使用：

```java
boolean managed = localFileStorage.isManagedKey(value);
```

兼容旧方法：

```java
boolean managed = localFileStorage.isManagedTag(value);
```

新方法会识别两类值：

- 新格式 `local:v1:...`
- 旧格式 `#...`

## 11. 旧格式兼容说明

历史数据如果还是这种格式：

```text
#avatar/2025/01/01/xxx.png
```

当前版本仍支持：

- `resolveUrl()`
- `resolvePath()`
- `deleteByKey()`
- `isManagedKey()`

但新上传文件不会再生成 `#...`，而是统一生成 `local:v1:...`。

## 12. bizType 规则

`bizType` 不能乱传。

只允许：

- 字母
- 数字
- 点 `.`
- 下划线 `_`
- 中划线 `-`

例如这些是合法的：

- `avatar`
- `flower`
- `knowledge`
- `doc`
- `user-avatar`

这些是不合法的：

- `../avatar`
- `avatar/test`
- `avatar\\test`
- 空字符串

如果配置了 `allowed-biz-types`，还必须在白名单内。

## 13. 扩展名规则

如果开启 `keep-extension`：

- 只保留安全扩展名。
- 扩展名会转成小写。
- 非法扩展名会被忽略。

当前安全扩展名规则：

- 只允许字母和数字
- 长度 1 到 16

例如：

- `a.PNG` 会保存为 `.png`
- `evil.tar.gz;` 不会保留 `gz;`

## 14. 推荐用法

推荐数据库字段只存 `storageKey`，不要存物理路径。

推荐写法：

```java
StoredFileInfo info = localFileStorage.saveAndGetInfo(file, "avatar");
entity.setAvatar(info.storageKey());
```

需要返回给前端时：

```java
String avatarUrl = localFileStorage.resolveUrl(entity.getAvatar());
```

不推荐：

- 数据库存物理绝对路径
- 业务层自己拼接本地路径
- 业务层自己拼接 `public-base-url`
- 新代码继续手写 `#...`

## 15. 常见问题

### 1. 为什么同一文件多次上传返回同一个 key？

因为现在按内容哈希命名，这是预期行为。这样可以去重，减少重复占盘。

### 2. 为什么不再使用按日期分目录？

日期目录更适合人工浏览，不适合长期高数量文件管理。哈希分片目录在文件系统层面更稳。

### 3. 为什么有时候扩展名没保留？

因为扩展名不安全，或者原始文件名本身没有合法扩展名。系统会优先保证安全和稳定，而不是强保留原名。

### 4. 旧数据要不要迁移？

当前不用强制迁移。旧 `#...` 值仍然能解析。后续如果要统一格式，可以做批量迁移，但不是运行前提。

## 16. 后续维护建议

- 新增业务类型时，优先更新 `allowed-biz-types` 白名单。
- 如果以后要接 OSS / MinIO / S3，可以保留当前 `storageKey` 的驱动和版本前缀思路继续扩展。
- 如果业务需要展示原始文件名，建议单独存字段，不要从存储路径反推。
- 如果以后要做文件秒传或引用计数，可以继续基于当前内容哈希方案演进。
