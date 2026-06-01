# 文件存储使用说明

<!-- ai-index: shared storage engineering guide -->

## 1. 设计目标

共享文件能力统一放在 `org.example.backend.shared.storage` 下，业务侧只注入 `FileStorage`，不直接关心当前使用本地文件还是腾讯云 COS。

新上传统一返回系统文件 key：

```text
pipker#avatar/ab/cd/hash.png
pipker#kyzz-question-bank-cover/ab/cd/hash.jpg
```

只要值包含 `pipker#`，就按系统自管文件处理。旧数据继续兼容：

- `local:v1:{bizType}:{relativePath}`
- `cos:v1:{bizType}:{relativePath}`
- `#{bizType}/{relativePath}`

外部完整 URL 不处理，原样返回。

## 2. 包结构

- `config`：`storage.*` 配置属性。
- `core`：`FileStorage`、`StorageKey`、上传预处理、存储结果和历史 key 解析。
- `driver/local`：本地文件驱动、本地代理处理器和本地 URL 解析。
- `driver/cos`：腾讯云 COS 驱动和 `COSClient` 配置。
- `image`：上传图片压缩服务。
- `controller`：当前只保留共享用户头像上传接口。

## 3. 配置

```yaml
storage:
  type: local
  local:
    base-path: ./file
    public-base-url: http://localhost:8080/static/files
    temp-dir: .tmp
    hash-algorithm: SHA-256
    shard-depth: 2
    shard-width: 2
    keep-extension: true
    allowed-biz-types:
      - avatar
      - kyzz-question-bank-cover
  image-compression:
    enabled: true
    max-width: 1600
    max-height: 1600
    jpeg-quality: 0.82
    min-saving-ratio: 0.05
  cos:
    secret-id: ${TENCENT_COS_SECRET_ID}
    secret-key: ${TENCENT_COS_SECRET_KEY}
    region: ap-guangzhou
    bucket: your-bucket-1250000000
    signed-url-expire-seconds: 1800
```

本地模式下，`public-base-url` 同时决定返回 URL 和代理路径。示例配置会注册 `/static/files/**`，并由后端代理输出 `storage.local.base-path` 下的文件。

COS 模式下不注册本地代理，`resolveUrl()` 返回 COS 私有桶临时签名 URL。

## 4. 上传与路径

上传流程：

1. 写入临时文件。
2. JPEG/PNG 执行保守压缩，GIF/WebP/SVG 跳过。
3. 只有压缩后体积达到收益阈值才替换原临时文件。
4. 基于最终文件计算 hash。
5. 生成 `{bizType}/{shard1}/{shard2}/{hash}.{ext}`。
6. 返回 `pipker#{bizType}/{relativePath}`。

业务示例：

```java
StoredFileInfo info = fileStorage.saveAndGetInfo(file, "avatar");
String url = fileStorage.resolveUrl(info.storageKey());
```

## 5. COS 迁移

COS 桶根目录保持与本地一致：

- 本地 `file/avatar` 上传到 COS 根目录 `avatar`
- 本地 `file/kyzz-question-bank-cover` 上传到 COS 根目录 `kyzz-question-bank-cover`

不要额外包一层 `file/`。这样 `pipker#...`、历史 `local:v1:...` 和旧 `#...` 都可以按同一相对路径解析。
