/**
 * @file UserProfileServiceTest
 * @project pipker-do
 * @module 用户中心 / 微信自动注册测试
 * @description 验证微信自动注册默认用户名、openid 唯一冲突恢复与已有用户登录更新行为。
 * @logic 1. 捕获新用户插入对象；2. 模拟 openid 唯一索引冲突；3. 校验已有用户登录不重建账号。
 * @dependencies Service: UserProfileService; Mapper: AppUserMapper, AppUserVipMapper
 * @index_tags 微信登录测试, 自动注册测试, openid重复, 用户名时间戳
 * @author holic512
 */
package org.example.backend.shared.account.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.example.backend.shared.account.entity.AppUser;
import org.example.backend.shared.account.mapper.AppUserMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileServiceTest {

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        if (TableInfoHelper.getTableInfo(AppUser.class) == null) {
            TableInfoHelper.initTableInfo(
                    new MybatisMapperBuilderAssistant(new MybatisConfiguration(), "UserProfileServiceTest"),
                    AppUser.class
            );
        }
    }

    @Test
    void shouldBuildTimestampedUsernameForDifferentOpenidsWithSameSuffix() {
        RecordingAppUserMapper appUserMapper = new RecordingAppUserMapper();
        appUserMapper.selectOneResponses.add(null);
        appUserMapper.selectOneResponses.add(null);
        UserProfileService service = newService(appUserMapper.proxy());

        service.createOrUpdateWechatUser(
                "openid_a_TAIL12345678",
                null,
                LocalDateTime.of(2026, 6, 2, 10, 15, 30, 123_000_000)
        );
        service.createOrUpdateWechatUser(
                "openid_b_TAIL12345678",
                null,
                LocalDateTime.of(2026, 6, 2, 10, 15, 31, 456_000_000)
        );

        List<AppUser> insertedUsers = appUserMapper.insertedUsers;

        assertEquals("wx_20260602101530123_TAIL12345678", insertedUsers.get(0).getUsername());
        assertEquals("wx_20260602101531456_TAIL12345678", insertedUsers.get(1).getUsername());
        assertFalse(insertedUsers.get(0).getUsername().equals(insertedUsers.get(1).getUsername()));
    }

    @Test
    void shouldKeepOriginalWechatOpenidWhenCreatingUser() {
        RecordingAppUserMapper appUserMapper = new RecordingAppUserMapper();
        appUserMapper.selectOneResponses.add(null);
        UserProfileService service = newService(appUserMapper.proxy());

        service.createOrUpdateWechatUser(
                "original_wechat_openid_001",
                "union-001",
                LocalDateTime.of(2026, 6, 2, 10, 15, 30, 0)
        );

        AppUser insertedUser = appUserMapper.insertedUsers.get(0);

        assertEquals("original_wechat_openid_001", insertedUser.getWechatOpenid());
        assertEquals("union-001", insertedUser.getWechatUnionid());
        assertTrue(insertedUser.getUsername().startsWith("wx_20260602101530000_"));
    }

    @Test
    void shouldRecoverWhenDuplicateWechatOpenidIsInsertedConcurrently() {
        RecordingAppUserMapper appUserMapper = new RecordingAppUserMapper();
        UserProfileService service = newService(appUserMapper.proxy());
        AppUser existing = activeUser(7L, "same_openid", "wx_old", "old-union");
        LocalDateTime now = LocalDateTime.of(2026, 6, 2, 11, 20, 30, 0);

        appUserMapper.selectOneResponses.add(null);
        appUserMapper.selectOneResponses.add(existing);
        appUserMapper.insertException = new DuplicateKeyException("Duplicate entry 'same_openid'");

        UserProfileService.UpsertUserResult result = service.createOrUpdateWechatUser("same_openid", "new-union", now);

        assertFalse(result.isNewUser());
        assertSame(existing, result.user());
        assertEquals("new-union", existing.getWechatUnionid());
        assertEquals(now, existing.getLastLoginAt());
        assertEquals(1, appUserMapper.updateCount);
    }

    @Test
    void shouldUpdateExistingWechatUserWithoutRebuildingUsername() {
        RecordingAppUserMapper appUserMapper = new RecordingAppUserMapper();
        UserProfileService service = newService(appUserMapper.proxy());
        AppUser existing = activeUser(9L, "existing_openid", "wx_existing_name", "old-union");
        LocalDateTime now = LocalDateTime.of(2026, 6, 2, 12, 0, 0, 0);
        appUserMapper.selectOneResponses.add(existing);

        UserProfileService.UpsertUserResult result = service.createOrUpdateWechatUser("existing_openid", "new-union", now);

        assertFalse(result.isNewUser());
        assertSame(existing, result.user());
        assertEquals("wx_existing_name", existing.getUsername());
        assertEquals("new-union", existing.getWechatUnionid());
        assertEquals(now, existing.getLastLoginAt());
        assertTrue(appUserMapper.insertedUsers.isEmpty());
        assertEquals(1, appUserMapper.updateCount);
    }

    private UserProfileService newService(AppUserMapper appUserMapper) {
        return new UserProfileService(appUserMapper, null, null);
    }

    private AppUser activeUser(Long id, String openid, String username, String unionid) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setWechatOpenid(openid);
        user.setUsername(username);
        user.setWechatUnionid(unionid);
        user.setStatus(1);
        return user;
    }

    private static final class RecordingAppUserMapper implements InvocationHandler {
        private final List<AppUser> selectOneResponses = new ArrayList<>();
        private final List<AppUser> insertedUsers = new ArrayList<>();
        private RuntimeException insertException;
        private int selectOneIndex;
        private int updateCount;

        private AppUserMapper proxy() {
            return (AppUserMapper) Proxy.newProxyInstance(
                    AppUserMapper.class.getClassLoader(),
                    new Class<?>[]{AppUserMapper.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "selectOne" -> selectOneIndex < selectOneResponses.size()
                        ? selectOneResponses.get(selectOneIndex++)
                        : null;
                case "insert" -> {
                    if (insertException != null) {
                        throw insertException;
                    }
                    insertedUsers.add((AppUser) args[0]);
                    yield 1;
                }
                case "update" -> {
                    updateCount++;
                    yield 1;
                }
                case "toString" -> "RecordingAppUserMapper";
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == void.class) {
                return null;
            }
            return 0;
        }
    }
}
