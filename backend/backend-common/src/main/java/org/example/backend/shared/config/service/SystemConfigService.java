package org.example.backend.shared.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.PageResponse;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.web.RequestIdHolder;
import org.example.backend.shared.admin.service.AdminPermissionService;
import org.example.backend.shared.config.dto.SystemConfigChangeLogResponse;
import org.example.backend.shared.config.dto.SystemConfigResponse;
import org.example.backend.shared.config.dto.SystemConfigUpdateRequest;
import org.example.backend.shared.config.entity.SysConfig;
import org.example.backend.shared.config.entity.SysConfigChangeLog;
import org.example.backend.shared.config.mapper.SysConfigChangeLogMapper;
import org.example.backend.shared.config.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: 后台系统配置管理服务。
 */
@Service
public class SystemConfigService {

    public static final String KEY_OPENAI_ENABLED = "llm.openai.enabled";
    public static final String KEY_OPENAI_API_KEY = "llm.openai.api_key";
    public static final String KEY_OPENAI_BASE_URL = "llm.openai.base_url";
    public static final String KEY_OPENAI_MODEL = "llm.openai.model";
    public static final String KEY_OPENAI_REASONING_EFFORT = "llm.openai.reasoning_effort";
    public static final String KEY_OPENAI_TEXT_VERBOSITY = "llm.openai.text_verbosity";
    public static final String KEY_OPENAI_MAX_OUTPUT_TOKENS = "llm.openai.max_output_tokens";
    public static final String KEY_OPENAI_TIMEOUT_SECONDS = "llm.openai.timeout_seconds";
    public static final String KEY_OPENAI_MAX_RETRIES = "llm.openai.max_retries";

    private static final List<DefaultConfig> DEFAULT_CONFIGS = List.of(
            new DefaultConfig("llm.openai", KEY_OPENAI_ENABLED, "启用 LLM", "boolean", "false", false, "是否允许公共 LLM 工具发起 OpenAI 调用", 10),
            new DefaultConfig("llm.openai", KEY_OPENAI_API_KEY, "OpenAI API Key", "secret", "", true, "OpenAI API Key，后台只展示脱敏值", 20),
            new DefaultConfig("llm.openai", KEY_OPENAI_BASE_URL, "OpenAI Base URL", "string", "https://api.openai.com/v1", false, "OpenAI API 基础地址", 30),
            new DefaultConfig("llm.openai", KEY_OPENAI_MODEL, "默认模型", "string", "gpt-5.5", false, "公共 LLM 工具默认模型", 40),
            new DefaultConfig("llm.openai", KEY_OPENAI_REASONING_EFFORT, "推理强度", "string", "medium", false, "none/minimal/low/medium/high/xhigh", 50),
            new DefaultConfig("llm.openai", KEY_OPENAI_TEXT_VERBOSITY, "输出详略", "string", "medium", false, "low/medium/high", 60),
            new DefaultConfig("llm.openai", KEY_OPENAI_MAX_OUTPUT_TOKENS, "最大输出 Tokens", "number", "2048", false, "单次调用最大输出 token 数", 70),
            new DefaultConfig("llm.openai", KEY_OPENAI_TIMEOUT_SECONDS, "超时秒数", "number", "60", false, "单次 OpenAI 请求超时时间", 80),
            new DefaultConfig("llm.openai", KEY_OPENAI_MAX_RETRIES, "最大重试次数", "number", "2", false, "OpenAI SDK 自动重试次数", 90)
    );

    private final SysConfigMapper sysConfigMapper;
    private final SysConfigChangeLogMapper changeLogMapper;
    private final ConfigValueCryptoService cryptoService;
    private final AdminPermissionService adminPermissionService;

    public SystemConfigService(SysConfigMapper sysConfigMapper,
                               SysConfigChangeLogMapper changeLogMapper,
                               ConfigValueCryptoService cryptoService,
                               AdminPermissionService adminPermissionService) {
        this.sysConfigMapper = sysConfigMapper;
        this.changeLogMapper = changeLogMapper;
        this.cryptoService = cryptoService;
        this.adminPermissionService = adminPermissionService;
    }

    public List<SystemConfigResponse> listConfigs(Long operatorId, String group, String keyword) {
        adminPermissionService.requireSystemManager(operatorId);
        ensureDefaultConfigs();

        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigGroup)
                .orderByAsc(SysConfig::getSortNo)
                .orderByAsc(SysConfig::getId);
        if (StringUtils.hasText(group)) {
            wrapper.eq(SysConfig::getConfigGroup, group.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(item -> item.like(SysConfig::getConfigKey, trimmedKeyword)
                    .or()
                    .like(SysConfig::getConfigName, trimmedKeyword));
        }
        return sysConfigMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SystemConfigResponse updateConfig(Long operatorId, String configKey, SystemConfigUpdateRequest request) {
        adminPermissionService.requireSystemManager(operatorId);
        ensureDefaultConfigs();
        if (!StringUtils.hasText(configKey)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "配置键不能为空");
        }
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "配置更新参数不能为空");
        }

        SysConfig config = requireConfig(configKey);
        String oldMaskedValue = maskStoredValue(config);
        boolean sensitive = isSensitive(config);
        boolean keepSensitiveValue = Boolean.TRUE.equals(request.getKeepSensitiveValue());
        boolean shouldUpdateValue = !sensitive || !keepSensitiveValue;

        Integer nextEnabled = request.getEnabled() == null ? config.getEnabled() : request.getEnabled();
        validateEnabled(nextEnabled);

        LambdaUpdateWrapper<SysConfig> updateWrapper = new LambdaUpdateWrapper<SysConfig>()
                .eq(SysConfig::getId, config.getId())
                .set(SysConfig::getEnabled, nextEnabled)
                .set(SysConfig::getUpdatedBy, operatorId);

        if (shouldUpdateValue) {
            String normalizedValue = normalizeValue(config, request.getValue());
            updateWrapper.set(SysConfig::getConfigValue, sensitive ? cryptoService.encrypt(normalizedValue) : normalizedValue);
        }

        sysConfigMapper.update(null, updateWrapper);
        SysConfig updated = requireConfig(configKey);
        recordChange(updated, oldMaskedValue, maskStoredValue(updated), operatorId);
        return toResponse(updated);
    }

    public PageResponse<SystemConfigChangeLogResponse> listChangeLogs(Long operatorId,
                                                                      String configKey,
                                                                      Long pageNo,
                                                                      Long pageSize) {
        adminPermissionService.requireSystemManager(operatorId);
        long normalizedPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        long normalizedPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        Page<SysConfigChangeLog> page = new Page<>(normalizedPageNo, normalizedPageSize);
        LambdaQueryWrapper<SysConfigChangeLog> wrapper = new LambdaQueryWrapper<SysConfigChangeLog>()
                .orderByDesc(SysConfigChangeLog::getId);
        if (StringUtils.hasText(configKey)) {
            wrapper.eq(SysConfigChangeLog::getConfigKey, configKey.trim());
        }
        Page<SysConfigChangeLog> result = changeLogMapper.selectPage(page, wrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords().stream().map(this::toChangeLogResponse).toList()
        );
    }

    public Map<String, String> getPlainValues(List<String> configKeys) {
        ensureDefaultConfigs();
        List<SysConfig> configs = sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .in(SysConfig::getConfigKey, configKeys));
        return configs.stream().collect(java.util.stream.Collectors.toMap(SysConfig::getConfigKey, this::plainValue));
    }

    public String getPlainValue(String configKey) {
        ensureDefaultConfigs();
        SysConfig config = requireConfig(configKey);
        return plainValue(config);
    }

    private void ensureDefaultConfigs() {
        for (DefaultConfig defaultConfig : DEFAULT_CONFIGS) {
            Long count = sysConfigMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, defaultConfig.configKey()));
            if (count != null && count > 0) {
                continue;
            }
            SysConfig config = new SysConfig();
            config.setConfigGroup(defaultConfig.configGroup());
            config.setConfigKey(defaultConfig.configKey());
            config.setConfigName(defaultConfig.configName());
            config.setConfigType(defaultConfig.configType());
            config.setConfigValue(defaultConfig.sensitive() ? cryptoService.encrypt(defaultConfig.defaultValue()) : defaultConfig.defaultValue());
            config.setSensitive(defaultConfig.sensitive() ? 1 : 0);
            config.setEnabled(1);
            config.setDescription(defaultConfig.description());
            config.setSortNo(defaultConfig.sortNo());
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.insert(config);
        }
    }

    private SysConfig requireConfig(String configKey) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("limit 1"));
        if (config == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "系统配置不存在");
        }
        return config;
    }

    private String normalizeValue(SysConfig config, String value) {
        String normalizedValue = value == null ? "" : value.trim();
        if (KEY_OPENAI_API_KEY.equals(config.getConfigKey()) && StringUtils.hasText(normalizedValue)) {
            validateOpenAiApiKey(normalizedValue);
        }
        if ("boolean".equals(config.getConfigType()) && !List.of("true", "false").contains(normalizedValue.toLowerCase())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "布尔配置仅支持 true 或 false");
        }
        if ("number".equals(config.getConfigType())) {
            try {
                long parsed = Long.parseLong(normalizedValue);
                if (parsed < 0) {
                    throw new NumberFormatException("negative");
                }
            } catch (NumberFormatException ex) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "数字配置必须是非负整数");
            }
        }
        return normalizedValue;
    }

    private void validateOpenAiApiKey(String apiKey) {
        if (apiKey.contains("…") || apiKey.contains("****") || apiKey.contains("***")) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI API Key 不能保存脱敏值，请输入完整密钥");
        }
        for (int index = 0; index < apiKey.length(); index++) {
            char item = apiKey.charAt(index);
            if (item < 0x21 || item > 0x7E) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI API Key 包含非法字符，请输入完整密钥");
            }
        }
    }

    private void validateEnabled(Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "配置启用状态仅支持 0 或 1");
        }
    }

    private void recordChange(SysConfig config, String oldMaskedValue, String newMaskedValue, Long operatorId) {
        SysConfigChangeLog log = new SysConfigChangeLog();
        log.setConfigGroup(config.getConfigGroup());
        log.setConfigKey(config.getConfigKey());
        log.setOldValueMasked(oldMaskedValue);
        log.setNewValueMasked(newMaskedValue);
        log.setChangedBy(operatorId);
        log.setRequestId(RequestIdHolder.getRequestId());
        log.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(log);
    }

    private SystemConfigResponse toResponse(SysConfig config) {
        boolean sensitive = isSensitive(config);
        String plainValue = sensitive ? "" : plainValue(config);
        String maskedValue = maskStoredValue(config);
        return new SystemConfigResponse(
                config.getId(),
                config.getConfigGroup(),
                config.getConfigKey(),
                config.getConfigName(),
                config.getConfigType(),
                sensitive ? null : plainValue,
                maskedValue,
                sensitive,
                StringUtils.hasText(plainValue(config)),
                config.getEnabled(),
                config.getDescription(),
                config.getSortNo(),
                config.getUpdatedBy(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }

    private SystemConfigChangeLogResponse toChangeLogResponse(SysConfigChangeLog log) {
        return new SystemConfigChangeLogResponse(
                log.getId(),
                log.getConfigGroup(),
                log.getConfigKey(),
                log.getOldValueMasked(),
                log.getNewValueMasked(),
                log.getChangedBy(),
                log.getRequestId(),
                log.getCreatedAt()
        );
    }

    private String plainValue(SysConfig config) {
        if (config == null) {
            return "";
        }
        return isSensitive(config) ? cryptoService.decrypt(config.getConfigValue()) : (config.getConfigValue() == null ? "" : config.getConfigValue());
    }

    private String maskStoredValue(SysConfig config) {
        String plainValue = plainValue(config);
        if (!StringUtils.hasText(plainValue)) {
            return "";
        }
        if (!isSensitive(config)) {
            return plainValue.length() > 120 ? plainValue.substring(0, 120) + "..." : plainValue;
        }
        if (plainValue.length() <= 8) {
            return "********";
        }
        return plainValue.substring(0, Math.min(4, plainValue.length())) + "****" + plainValue.substring(plainValue.length() - 4);
    }

    private boolean isSensitive(SysConfig config) {
        return config.getSensitive() != null && config.getSensitive() == 1;
    }

    private record DefaultConfig(String configGroup,
                                 String configKey,
                                 String configName,
                                 String configType,
                                 String defaultValue,
                                 boolean sensitive,
                                 String description,
                                 int sortNo) {
    }
}
