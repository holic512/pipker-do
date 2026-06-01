/**
 * @file StorageKeyTest
 * @project pipker-do
 * @module 共享存储 / 结构化存储键测试
 * @description 验证系统文件 key 和历史存储键的生成、识别与路径校验。
 * @logic 1. 校验 pipker#、local:v1 和 cos:v1 解析；2. 校验相对对象路径转换；3. 拒绝非法 driver 和穿越路径。
 * @dependencies JUnit 5, StorageKey
 * @index_tags storageKey测试, pipker#, local:v1, cos:v1, 路径校验
 * @author holic512
 */
package org.example.backend.util.file;

import org.example.backend.shared.storage.core.StorageKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageKeyTest {

    @Test
    void shouldGenerateAndParseSystemManagedKey() {
        StorageKey key = StorageKey.of("avatar", "ab/cd/hash.png");

        assertEquals("pipker#avatar/ab/cd/hash.png", key.asString());
        assertEquals(StorageKey.SYSTEM_DRIVER, key.getDriver());
        assertEquals("avatar/ab/cd/hash.png", key.toRelativeStoragePath());
        assertTrue(StorageKey.isManaged(key.asString()));
        assertTrue(StorageKey.isSystemManaged(key.asString()));
        assertEquals(key, StorageKey.parse("https://cdn.example.com/static/files/pipker#avatar/ab/cd/hash.png"));
    }

    @Test
    void shouldParseLocalAndCosManagedKeys() {
        StorageKey localKey = StorageKey.parse("local:v1:avatar:ab/cd/hash.png");
        StorageKey cosKey = StorageKey.parse("cos:v1:avatar:ab/cd/hash.png");

        assertEquals(StorageKey.LOCAL_DRIVER, localKey.getDriver());
        assertEquals(StorageKey.COS_DRIVER, cosKey.getDriver());
        assertEquals("avatar/ab/cd/hash.png", localKey.toRelativeStoragePath());
        assertEquals("avatar/ab/cd/hash.png", cosKey.toRelativeStoragePath());
        assertTrue(StorageKey.isManaged(localKey.asString()));
        assertTrue(StorageKey.isManaged(cosKey.asString()));
    }

    @Test
    void shouldRejectUnsupportedDriverAndTraversalPath() {
        assertFalse(StorageKey.isManaged("oss:v1:avatar:ab/cd/hash.png"));
        assertThrows(IllegalArgumentException.class, () -> StorageKey.parse("oss:v1:avatar:ab/cd/hash.png"));
        assertThrows(IllegalArgumentException.class, () -> StorageKey.parse("cos:v1:avatar:../secret.png"));
        assertThrows(IllegalArgumentException.class, () -> StorageKey.parse("pipker#avatar/../secret.png"));
        assertThrows(IllegalArgumentException.class, () -> StorageKey.parseRelativeStoragePath("/avatar/../secret.png"));
    }
}
