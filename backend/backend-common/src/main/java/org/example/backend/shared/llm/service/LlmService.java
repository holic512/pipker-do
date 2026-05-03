package org.example.backend.shared.llm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.errors.OpenAIException;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseTextConfig;
import com.openai.models.responses.ResponseUsage;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.api.PageResponse;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.web.RequestIdHolder;
import org.example.backend.shared.config.service.SystemConfigService;
import org.example.backend.shared.llm.dto.LlmCallRecordResponse;
import org.example.backend.shared.llm.dto.LlmGenerateResult;
import org.example.backend.shared.llm.dto.LlmUsageResponse;
import org.example.backend.shared.llm.entity.LlmCallRecord;
import org.example.backend.shared.llm.mapper.LlmCallRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: 公共 OpenAI LLM 调用服务。
 */
@Service
public class LlmService {

    private static final int PREVIEW_LIMIT = 1000;
    private static final int ERROR_LIMIT = 500;

    private final SystemConfigService systemConfigService;
    private final LlmCallRecordMapper llmCallRecordMapper;
    private final LlmPromptHashService promptHashService;
    private final ObjectMapper objectMapper;

    public LlmService(SystemConfigService systemConfigService,
                      LlmCallRecordMapper llmCallRecordMapper,
                      LlmPromptHashService promptHashService,
                      ObjectMapper objectMapper) {
        this.systemConfigService = systemConfigService;
        this.llmCallRecordMapper = llmCallRecordMapper;
        this.promptHashService = promptHashService;
        this.objectMapper = objectMapper;
    }

    public LlmGenerateResult generateText(String scene, String systemPrompt, String userPrompt) {
        return generateInternal(null, scene, systemPrompt, userPrompt, null, null);
    }

    public LlmGenerateResult generateText(Long operatorId, String scene, String systemPrompt, String userPrompt) {
        return generateInternal(operatorId, scene, systemPrompt, userPrompt, null, null);
    }

    public LlmGenerateResult generateJson(String scene,
                                          String systemPrompt,
                                          String userPrompt,
                                          String schemaName,
                                          String jsonSchema) {
        return generateInternal(null, scene, systemPrompt, userPrompt, schemaName, jsonSchema);
    }

    public LlmGenerateResult generateJson(Long operatorId,
                                          String scene,
                                          String systemPrompt,
                                          String userPrompt,
                                          String schemaName,
                                          String jsonSchema) {
        return generateInternal(operatorId, scene, systemPrompt, userPrompt, schemaName, jsonSchema);
    }

    public PageResponse<LlmCallRecordResponse> listRecords(Long operatorId,
                                                           String status,
                                                           String scene,
                                                           Long pageNo,
                                                           Long pageSize) {
        long normalizedPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        long normalizedPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        Page<LlmCallRecord> page = new Page<>(normalizedPageNo, normalizedPageSize);
        LambdaQueryWrapper<LlmCallRecord> wrapper = new LambdaQueryWrapper<LlmCallRecord>()
                .orderByDesc(LlmCallRecord::getId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(LlmCallRecord::getStatus, status.trim());
        }
        if (StringUtils.hasText(scene)) {
            wrapper.like(LlmCallRecord::getScene, scene.trim());
        }
        Page<LlmCallRecord> result = llmCallRecordMapper.selectPage(page, wrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords().stream().map(this::toRecordResponse).toList()
        );
    }

    public LlmCallRecordResponse getRecordDetail(Long operatorId, Long recordId) {
        LlmCallRecord record = llmCallRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "LLM 调用记录不存在");
        }
        return toRecordResponse(record);
    }

    private LlmGenerateResult generateInternal(Long operatorId,
                                               String scene,
                                               String systemPrompt,
                                               String userPrompt,
                                               String schemaName,
                                               String jsonSchema) {
        String normalizedScene = normalizeScene(scene);
        String normalizedSystemPrompt = systemPrompt == null ? "" : systemPrompt.trim();
        String normalizedUserPrompt = normalizeRequired(userPrompt, "用户提示词不能为空");
        boolean jsonMode = StringUtils.hasText(jsonSchema);
        if (jsonMode && !StringUtils.hasText(schemaName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "JSON 输出必须提供 schemaName");
        }

        OpenAiLlmConfig config = loadOpenAiConfig();
        String promptHash = promptHashService.sha256(normalizedSystemPrompt + "\n\n" + normalizedUserPrompt + "\n\n" + (jsonSchema == null ? "" : jsonSchema));
        String inputPreview = truncate((StringUtils.hasText(normalizedSystemPrompt)
                ? "System:\n" + normalizedSystemPrompt + "\n\nUser:\n" + normalizedUserPrompt
                : normalizedUserPrompt), PREVIEW_LIMIT);
        long start = System.nanoTime();

        try {
            validateReady(config);
            ResponseCreateParams params = buildParams(config, normalizedSystemPrompt, normalizedUserPrompt, schemaName, jsonSchema);
            Response response = buildClient(config).responses().create(params);
            long latencyMs = elapsedMs(start);
            LlmUsageResponse usage = response.usage()
                    .map(item -> new LlmUsageResponse(toInt(item.inputTokens()), toInt(item.outputTokens()), toInt(item.totalTokens())))
                    .orElse(new LlmUsageResponse(null, null, null));
            String content = extractOutputText(response);
            LlmCallRecord record = recordCall(
                    operatorId,
                    normalizedScene,
                    response.model().asString(),
                    "success",
                    latencyMs,
                    usage,
                    promptHash,
                    inputPreview,
                    truncate(content, PREVIEW_LIMIT),
                    null
            );
            return new LlmGenerateResult(
                    jsonMode ? null : content,
                    jsonMode ? content : null,
                    response.model().asString(),
                    usage,
                    latencyMs,
                    record.getId(),
                    RequestIdHolder.getRequestId()
            );
        } catch (BusinessException ex) {
            recordFailedCall(operatorId, normalizedScene, config.model(), start, promptHash, inputPreview, ex.getMessage());
            throw ex;
        } catch (OpenAIException ex) {
            String message = "AI 调用失败：" + truncate(ex.getMessage(), 220);
            recordFailedCall(operatorId, normalizedScene, config.model(), start, promptHash, inputPreview, message);
            throw new BusinessException(ApiResponseCode.BUSINESS_ERROR, message);
        } catch (Exception ex) {
            String message = "AI 调用失败：" + truncate(ex.getMessage(), 220);
            recordFailedCall(operatorId, normalizedScene, config.model(), start, promptHash, inputPreview, message);
            throw new BusinessException(ApiResponseCode.BUSINESS_ERROR, message);
        }
    }

    private ResponseCreateParams buildParams(OpenAiLlmConfig config,
                                             String systemPrompt,
                                             String userPrompt,
                                             String schemaName,
                                             String jsonSchema) throws Exception {
        ResponseCreateParams.Builder builder = ResponseCreateParams.builder()
                .model(config.model())
                .input(userPrompt)
                .store(false)
                .maxOutputTokens((long) config.maxOutputTokens());

        if (StringUtils.hasText(systemPrompt)) {
            builder.instructions(systemPrompt);
        }
        if (StringUtils.hasText(config.reasoningEffort())) {
            builder.reasoning(Reasoning.builder()
                    .effort(ReasoningEffort.of(config.reasoningEffort()))
                    .build());
        }

        ResponseTextConfig.Builder textBuilder = ResponseTextConfig.builder();
        if (StringUtils.hasText(config.textVerbosity())) {
            textBuilder.verbosity(ResponseTextConfig.Verbosity.of(config.textVerbosity()));
        }
        if (StringUtils.hasText(jsonSchema)) {
            ResponseFormatTextJsonSchemaConfig.Schema schema = buildJsonSchema(jsonSchema);
            textBuilder.format(ResponseFormatTextJsonSchemaConfig.builder()
                    .name(schemaName.trim())
                    .schema(schema)
                    .strict(true)
                    .build());
        }
        builder.text(textBuilder.build());
        return builder.build();
    }

    private ResponseFormatTextJsonSchemaConfig.Schema buildJsonSchema(String jsonSchema) throws Exception {
        JsonNode root = objectMapper.readTree(jsonSchema);
        if (!root.isObject()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "JSON Schema 根节点必须是对象");
        }
        ResponseFormatTextJsonSchemaConfig.Schema.Builder builder = ResponseFormatTextJsonSchemaConfig.Schema.builder();
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            builder.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue()));
        }
        return builder.build();
    }

    private OpenAIClient buildClient(OpenAiLlmConfig config) {
        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                .apiKey(config.apiKey())
                .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                .maxRetries(config.maxRetries());
        if (StringUtils.hasText(config.baseUrl())) {
            builder.baseUrl(config.baseUrl());
        }
        return builder.build();
    }

    private OpenAiLlmConfig loadOpenAiConfig() {
        Map<String, String> values = systemConfigService.getPlainValues(List.of(
                SystemConfigService.KEY_OPENAI_ENABLED,
                SystemConfigService.KEY_OPENAI_API_KEY,
                SystemConfigService.KEY_OPENAI_BASE_URL,
                SystemConfigService.KEY_OPENAI_MODEL,
                SystemConfigService.KEY_OPENAI_REASONING_EFFORT,
                SystemConfigService.KEY_OPENAI_TEXT_VERBOSITY,
                SystemConfigService.KEY_OPENAI_MAX_OUTPUT_TOKENS,
                SystemConfigService.KEY_OPENAI_TIMEOUT_SECONDS,
                SystemConfigService.KEY_OPENAI_MAX_RETRIES
        ));
        return new OpenAiLlmConfig(
                Boolean.parseBoolean(values.getOrDefault(SystemConfigService.KEY_OPENAI_ENABLED, "false")),
                values.getOrDefault(SystemConfigService.KEY_OPENAI_API_KEY, ""),
                values.getOrDefault(SystemConfigService.KEY_OPENAI_BASE_URL, "https://api.openai.com/v1"),
                values.getOrDefault(SystemConfigService.KEY_OPENAI_MODEL, "gpt-5.5"),
                values.getOrDefault(SystemConfigService.KEY_OPENAI_REASONING_EFFORT, "medium"),
                values.getOrDefault(SystemConfigService.KEY_OPENAI_TEXT_VERBOSITY, "medium"),
                parseInt(values.get(SystemConfigService.KEY_OPENAI_MAX_OUTPUT_TOKENS), 2048),
                parseInt(values.get(SystemConfigService.KEY_OPENAI_TIMEOUT_SECONDS), 60),
                parseInt(values.get(SystemConfigService.KEY_OPENAI_MAX_RETRIES), 2)
        );
    }

    private void validateReady(OpenAiLlmConfig config) {
        if (!config.enabled()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "LLM 功能未启用");
        }
        if (!StringUtils.hasText(config.apiKey())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI API Key 未配置");
        }
        if (looksLikeMaskedApiKey(config.apiKey())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI API Key 不能使用脱敏值，请重新保存完整密钥");
        }
        if (!isAsciiHeaderValue(config.apiKey())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI API Key 包含非法字符，请重新保存完整密钥");
        }
        if (!StringUtils.hasText(config.model())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "OpenAI 模型未配置");
        }
    }

    private boolean looksLikeMaskedApiKey(String apiKey) {
        return apiKey.contains("…") || apiKey.contains("****") || apiKey.contains("***");
    }

    private boolean isAsciiHeaderValue(String value) {
        for (int index = 0; index < value.length(); index++) {
            char item = value.charAt(index);
            if (item < 0x21 || item > 0x7E) {
                return false;
            }
        }
        return true;
    }

    private String extractOutputText(Response response) {
        StringBuilder builder = new StringBuilder();
        for (ResponseOutputItem item : response.output()) {
            ResponseOutputMessage message = item.message().orElse(null);
            if (message == null) {
                continue;
            }
            for (ResponseOutputMessage.Content content : message.content()) {
                ResponseOutputText outputText = content.outputText().orElse(null);
                if (outputText != null && StringUtils.hasText(outputText.text())) {
                    if (!builder.isEmpty()) {
                        builder.append("\n");
                    }
                    builder.append(outputText.text());
                }
            }
        }
        return builder.toString();
    }

    private LlmCallRecord recordFailedCall(Long operatorId,
                                           String scene,
                                           String model,
                                           long start,
                                           String promptHash,
                                           String inputPreview,
                                           String errorMessage) {
        return recordCall(
                operatorId,
                scene,
                model,
                "failed",
                elapsedMs(start),
                new LlmUsageResponse(null, null, null),
                promptHash,
                inputPreview,
                null,
                truncate(errorMessage, ERROR_LIMIT)
        );
    }

    private LlmCallRecord recordCall(Long operatorId,
                                     String scene,
                                     String model,
                                     String status,
                                     long latencyMs,
                                     LlmUsageResponse usage,
                                     String promptHash,
                                     String inputPreview,
                                     String outputPreview,
                                     String errorMessage) {
        LlmCallRecord record = new LlmCallRecord();
        record.setScene(scene);
        record.setModel(model);
        record.setStatus(status);
        record.setLatencyMs(latencyMs);
        record.setInputTokens(usage == null ? null : usage.getInputTokens());
        record.setOutputTokens(usage == null ? null : usage.getOutputTokens());
        record.setTotalTokens(usage == null ? null : usage.getTotalTokens());
        record.setRequestId(RequestIdHolder.getRequestId());
        record.setPromptHash(promptHash);
        record.setInputPreview(inputPreview);
        record.setOutputPreview(outputPreview);
        record.setErrorMessage(errorMessage);
        record.setOperatorId(operatorId);
        record.setCreatedAt(LocalDateTime.now());
        llmCallRecordMapper.insert(record);
        return record;
    }

    private LlmCallRecordResponse toRecordResponse(LlmCallRecord record) {
        return new LlmCallRecordResponse(
                record.getId(),
                record.getScene(),
                record.getModel(),
                record.getStatus(),
                record.getLatencyMs(),
                record.getInputTokens(),
                record.getOutputTokens(),
                record.getTotalTokens(),
                record.getRequestId(),
                record.getPromptHash(),
                record.getInputPreview(),
                record.getOutputPreview(),
                record.getErrorMessage(),
                record.getOperatorId(),
                record.getCreatedAt()
        );
    }

    private String normalizeScene(String scene) {
        String normalized = scene == null ? "" : scene.trim();
        if (!StringUtils.hasText(normalized)) {
            return "admin-test";
        }
        if (normalized.length() > 80) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "调用场景不能超过 80 个字符");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, message);
        }
        return normalized;
    }

    private int parseInt(String value, int defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int toInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private long elapsedMs(long start) {
        return Math.max(0, (System.nanoTime() - start) / 1_000_000);
    }

    private String truncate(String value, int limit) {
        if (value == null) {
            return null;
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }

    private record OpenAiLlmConfig(boolean enabled,
                                   String apiKey,
                                   String baseUrl,
                                   String model,
                                   String reasoningEffort,
                                   String textVerbosity,
                                   int maxOutputTokens,
                                   int timeoutSeconds,
                                   int maxRetries) {
    }
}
