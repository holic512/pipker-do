package org.example.backend.util.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Component
public class LocalFileStorage {

    /**
     * 数据库存储的前缀标记，用于抗迁移
     * 例如：#image/2025/11/15/xxx.jpg
     */
    public static final String TAG_PREFIX = "#";

    private final LocalStorageProperties props;

    public LocalFileStorage(LocalStorageProperties props) {
        this.props = props;
    }

    /**
     * 保存 MultipartFile 到本地，并返回以 # 开头的存储标记
     *
     * @param file    上传文件
     * @param bizType 业务类型（用作目录前缀），如：avatar、flower、knowledge、doc 等
     * @return 例如：#flower/2025/11/15/xxx.jpg
     */
    public String save(MultipartFile file, String bizType) {
        Objects.requireNonNull(file, "file 不能为空");
        requireNonEmpty(props.getRootDir(), "请在配置中设置 storage.local.root-dir");
        requireNonEmpty(props.getPublicBaseUrl(), "请在配置中设置 storage.local.public-base-url");
        requireNonNonEmpty(bizType, "bizType 不能为空");

        // 决定相对目录：bizType + 日期路径
        StringBuilder relativeDir = new StringBuilder();
        relativeDir.append(bizType);
        if (props.getEnableDatePath() == null || props.getEnableDatePath()) {
            LocalDate today = LocalDate.now();
            relativeDir.append("/")
                    .append(today.getYear())
                    .append("/")
                    .append(String.format("%02d", today.getMonthValue()))
                    .append("/")
                    .append(String.format("%02d", today.getDayOfMonth()));
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (props.getKeepExtension() == null || props.getKeepExtension()) {
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 物理路径：rootDir + relativeDir + fileName
        Path dirPath = Paths.get(props.getRootDir(), relativeDir.toString());
        Path filePath = dirPath.resolve(fileName);

        try {
            Files.createDirectories(dirPath);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败：" + e.getMessage(), e);
        }

        // 相对路径统一用 / 分隔
        String relativePath = relativeDir.append("/").append(fileName).toString().replace("\\", "/");
        // 数据库存储为 # 开头的tag
        return TAG_PREFIX + relativePath;
    }

    /**
     * 根据存储标记解析出对外访问的 URL
     * 若不是 # 开头，则视为已是完整 URL 或非本工具管理的路径，直接返回
     *
     * @param stored 在数据库中的值
     * @return 可访问的 HTTP(S) URL，或原值
     */
    public String resolveUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (!stored.startsWith(TAG_PREFIX)) {
            // 非本工具管理的路径，直接返回，例如第三方完整URL
            return stored;
        }
        requireNonEmpty(props.getPublicBaseUrl(), "请在配置中设置 storage.local.public-base-url");

        String relativePath = stored.substring(TAG_PREFIX.length());
        String base = props.getPublicBaseUrl();
        if (!base.endsWith("/")) {
            base += "/";
        }
        // 确保相对路径不以 / 开头
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return base + relativePath;
    }

    /**
     * 根据存储标记解析本地磁盘绝对路径
     *
     * @param stored 在数据库中的值
     * @return 本地 Path；若不是 # 开头，返回 null（不处理非本工具路径）
     */
    public Path resolvePath(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (!stored.startsWith(TAG_PREFIX)) {
            return null;
        }
        requireNonEmpty(props.getRootDir(), "请在配置中设置 storage.local.root-dir");
        String relativePath = stored.substring(TAG_PREFIX.length());
        return Paths.get(props.getRootDir(), relativePath);
    }

    /**
     * 删除对应文件（仅对 # 开头的标记生效）
     *
     * @param stored 在数据库中的值
     * @return 是否删除成功（不存在也视为成功）
     */
    public boolean deleteByTag(String stored) {
        Path path = resolvePath(stored);
        if (path == null) {
            return false;
        }
        File f = path.toFile();
        if (!f.exists()) {
            return true;
        }
        return f.delete();
    }

    /**
     * 判断是否为本工具管理的存储标记（#开头）
     */
    public boolean isManagedTag(String value) {
        return value != null && value.startsWith(TAG_PREFIX);
    }

    private void requireNonEmpty(String v, String msg) {
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(msg);
        }
    }

    private void requireNonNonEmpty(String v, String msg) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(msg);
        }
    }
}
