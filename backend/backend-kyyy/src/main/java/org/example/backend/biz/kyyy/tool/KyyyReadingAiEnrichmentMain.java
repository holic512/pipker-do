/**
 * @file KyyyReadingAiEnrichmentMain
 * @project pipker-do
 * @module 考研英语 / 阅读 AI 补齐
 * @description 提供可单独 main 执行的阅读题答案与解析 AI 补齐工具。
 * @logic 1. 读取本地 OpenAI 与数据库配置；2. 查询缺少答案或解析的阅读题；3. 按篇章正文、题干与选项调用 AI 推断标准答案和中文解析；4. 事务回写题目答案、选项正确标记与补齐日志。
 * @dependencies OpenAI Responses/Chat Completions API, JDBC: kyyy_reading_passage/kyyy_reading_question/kyyy_reading_question_option/kyyy_reading_ai_enrichment_log
 * @index_tags 考研英语, 阅读补齐, OpenAI, main工具, 真题解析
 * @author holic512
 */
package org.example.backend.biz.kyyy.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public final class KyyyReadingAiEnrichmentMain {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_CONFIG_PATH = "backend/backend-kyyy/src/main/resources/kyyy-word-ai-enrichment.kyyy-word-ai-enrichment.local.properties";
    private static final DateTimeFormatter RUN_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_ANALYSIS_LENGTH = 1200;
    private static final int MAX_EVIDENCE_LENGTH = 320;
    private static final int MIN_EVIDENCE_LENGTH = 12;
    private static final int MAX_EVIDENCE_SENTENCE_LENGTH = 280;
    private static final int MAX_REASONING_SUMMARY_LENGTH = 220;
    private static final int MAX_CORRECT_REASON_LENGTH = 280;
    private static final int MAX_OPTION_ANALYSIS_LENGTH = 180;
    private static final int VALIDATION_RETRY_BONUS_ATTEMPTS = 2;

    private KyyyReadingAiEnrichmentMain() {
    }

    public static void main(String[] rawArgs) throws Exception {
        CliArgs args = CliArgs.shouldUseMenu(rawArgs)
                ? InteractiveMenu.prompt()
                : CliArgs.parse(rawArgs);
        if (args.shouldExit()) {
            System.out.println("已退出。");
            return;
        }
        boolean requireOpenAiConfig = args.validateOnly() || ("all".equals(args.mode()) && !args.apply())
                ? false
                : true;
        ToolConfig config = ToolConfig.load(args.configPath(), requireOpenAiConfig);
        new Runner(args, config).run();
    }

    private static final class Runner {

        private final CliArgs args;
        private final ToolConfig config;
        private final String runId;

        private Runner(CliArgs args, ToolConfig config) {
            this.args = args;
            this.config = config;
            this.runId = "kyyy-reading-ai-" + LocalDateTime.now().format(RUN_TIME_FORMATTER) + "-"
                    + UUID.randomUUID().toString().substring(0, 8);
        }

        private void run() throws Exception {
            verifySchema();
            printHeader();
            if (args.validateOnly()) {
                runValidateOnly();
                return;
            }
            if ("single".equals(args.mode())) {
                runSingle();
                return;
            }
            runAll();
        }

        private void verifySchema() throws SQLException {
            try (Connection connection = openConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT 1 FROM kyyy_reading_passage LIMIT 1").close();
                statement.executeQuery("SELECT 1 FROM kyyy_reading_question LIMIT 1").close();
                statement.executeQuery("SELECT 1 FROM kyyy_reading_question_option LIMIT 1").close();
                statement.executeQuery("SELECT 1 FROM kyyy_reading_ai_enrichment_log LIMIT 1").close();
            }
        }

        private void printHeader() {
            System.out.println("KYYY 阅读题 AI 补齐工具");
            System.out.println("runId=" + runId);
            System.out.println("mode=" + args.mode()
                    + ", apply=" + args.apply()
                    + ", validateOnly=" + args.validateOnly()
                    + ", overwriteExisting=" + args.overwriteExisting()
                    + ", direction=" + safeText(args.direction())
                    + ", year=" + (args.year() == null ? "全部" : args.year()));
            System.out.println("apiType=" + config.openAiApiType()
                    + ", model=" + config.openAiModel()
                    + ", reasoningEffort=" + config.openAiReasoningEffort()
                    + ", minConfidence=" + config.qualityMinConfidence());
            System.out.println("aiReview=" + config.qualityAiReviewEnabled()
                    + ", aiMaxAttempts=" + config.qualityAiMaxAttempts());
            System.out.println();
        }

        private void runValidateOnly() throws SQLException {
            List<ReadingQuestionRecord> questions = loadTargetQuestions();
            int needsAiCount = 0;
            for (ReadingQuestionRecord question : questions) {
                EnrichmentAssessment assessment = assessQuestion(question);
                if (assessment.needsAi()) {
                    needsAiCount++;
                }
                System.out.printf(Locale.ROOT,
                        "questionId=%d passage=%s q=%d gap=%s%n",
                        question.questionId(),
                        question.passageCode(),
                        question.questionNo(),
                        assessment.reason());
            }
            System.out.printf(Locale.ROOT,
                    "validate summary: total=%d, needsAi=%d%n",
                    questions.size(),
                    needsAiCount);
        }

        private void runSingle() throws Exception {
            ReadingQuestionRecord question = loadSingleTargetQuestion();
            if (question == null) {
                throw new IllegalArgumentException("未找到指定阅读题");
            }
            ProcessResult result = prepareQuestion(question);
            System.out.println(result.message());
            if (result.validatedEnrichment() != null) {
                System.out.println("answerKey=" + result.validatedEnrichment().answerKey());
                System.out.println("confidence=" + result.validatedEnrichment().confidence());
                System.out.println("analysis=" + result.validatedEnrichment().analysis());
                System.out.println("evidence=" + result.validatedEnrichment().evidence());
            }
            if (args.apply() && result.validatedEnrichment() != null && confirmSingleApply()) {
                writeEnrichment(question, result.validatedEnrichment(), result.aiCall());
                System.out.println("写库完成。");
            } else if (args.apply()) {
                System.out.println("未确认写入或未生成可写入结果，数据库未修改。");
            }
        }

        private boolean confirmSingleApply() throws IOException {
            if (args.yes()) {
                return true;
            }
            String confirmToken = args.overwriteExisting() ? "OVERWRITE" : "YES";
            System.out.print("输入 " + confirmToken + " 确认" + (args.overwriteExisting() ? "覆盖" : "") + "写入数据库：");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            return confirmToken.equals(reader.readLine());
        }

        private void runAll() throws Exception {
            List<ReadingQuestionRecord> questions = loadTargetQuestions();
            if (!args.apply()) {
                System.out.println(args.overwriteExisting()
                        ? "范围覆盖模式未提供 --apply：只输出目标题目，不调用 AI、不写库。"
                        : "全量模式未提供 --apply：只输出缺口，不调用 AI、不写库。");
                int needsAiCount = 0;
                for (ReadingQuestionRecord question : questions) {
                    EnrichmentAssessment assessment = assessQuestion(question);
                    if (assessment.needsAi()) {
                        needsAiCount++;
                    }
                    System.out.printf(Locale.ROOT,
                            "questionId=%d passage=%s q=%d gap=%s%n",
                            question.questionId(),
                            question.passageCode(),
                            question.questionNo(),
                            assessment.reason());
                }
                System.out.printf(Locale.ROOT,
                        "dry summary: total=%d, needsAi=%d%n",
                        questions.size(),
                        needsAiCount);
                return;
            }

            int threads = args.threads() == null ? config.runThreads() : args.threads();
            ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, threads));
            AtomicInteger completed = new AtomicInteger();
            List<Future<ProcessResult>> futures = new ArrayList<>();
            for (ReadingQuestionRecord question : questions) {
                futures.add(executor.submit(() -> {
                    try {
                        ProcessResult result = prepareQuestion(question);
                        if (result.validatedEnrichment() != null) {
                            writeEnrichment(question, result.validatedEnrichment(), result.aiCall());
                        } else if (!result.assessment().needsAi()) {
                            recordLog(question, result.assessment(), "skipped", null, null, result.assessment().reason());
                        }
                        int done = completed.incrementAndGet();
                        synchronized (System.out) {
                            System.out.printf(Locale.ROOT,
                                    "[%d/%d] %s%n",
                                    done,
                                    questions.size(),
                                    result.message());
                        }
                        return result;
                    } catch (Exception error) {
                        EnrichmentAssessment assessment = assessQuestion(question);
                        recordFailureQuietly(question, assessment, error);
                        int done = completed.incrementAndGet();
                        synchronized (System.out) {
                            System.out.printf(Locale.ROOT,
                                    "[%d/%d] failed questionId=%d error=%s%n",
                                    done,
                                    questions.size(),
                                    question.questionId(),
                                    error.getMessage());
                        }
                        return ProcessResult.failed(question, assessment, error.getMessage());
                    }
                }));
            }
            executor.shutdown();

            int success = 0;
            int skipped = 0;
            int failed = 0;
            for (Future<ProcessResult> future : futures) {
                ProcessResult result = future.get();
                if (result.success()) {
                    success++;
                } else if (!result.assessment().needsAi()) {
                    skipped++;
                } else {
                    failed++;
                }
            }
            System.out.printf(Locale.ROOT,
                    "all summary: total=%d, success=%d, skipped=%d, failed=%d%n",
                    questions.size(),
                    success,
                    skipped,
                    failed);
        }

        private ProcessResult prepareQuestion(ReadingQuestionRecord question) throws Exception {
            EnrichmentAssessment assessment = assessQuestion(question);
            if (!assessment.needsAi()) {
                return ProcessResult.skipped(question, assessment);
            }
            AiGenerator aiGenerator = new AiGenerator(config);
            List<String> retryFeedback = new ArrayList<>();
            Exception lastError = null;
            int maxAttempts = Math.max(1, config.qualityAiMaxAttempts());
            int bonusAttempts = 0;
            for (int attempt = 1; attempt <= maxAttempts + bonusAttempts; attempt++) {
                try {
                    AiCallResult aiCall = aiGenerator.generate(
                            question,
                            assessment,
                            retryFeedback,
                            args.overwriteExisting()
                    );
                    ValidatedEnrichment enrichment = validateAiResult(
                            question,
                            aiCall.response(),
                            config.qualityMinConfidence(),
                            args.overwriteExisting()
                    );
                    if (config.qualityAiReviewEnabled()) {
                        AiReviewResult review = aiGenerator.review(
                                question,
                                enrichment,
                                aiCall.rawJson(),
                                args.overwriteExisting()
                        );
                        if (!review.approved()) {
                            String reason = buildReviewFailureReason(review);
                            retryFeedback.add(reason);
                            lastError = new IllegalArgumentException(reason);
                            continue;
                        }
                    }
                    return ProcessResult.success(question, assessment, enrichment, aiCall);
                } catch (IllegalArgumentException | IOException error) {
                    String reason = error.getMessage();
                    retryFeedback.add("本地校验未通过：" + reason);
                    lastError = error;
                    if (shouldGrantBonusRetry(error) && bonusAttempts < VALIDATION_RETRY_BONUS_ATTEMPTS) {
                        bonusAttempts++;
                    }
                }
            }
            String reason = retryFeedback.isEmpty() ? "待确认" : retryFeedback.get(retryFeedback.size() - 1);
            throw new IllegalArgumentException("AI 生成连续 " + (maxAttempts + bonusAttempts) + " 次未通过写库前校验，最后原因：" + reason, lastError);
        }

        private String buildReviewFailureReason(AiReviewResult review) {
            StringBuilder builder = new StringBuilder("AI 审核未通过");
            if (hasText(review.feedback())) {
                builder.append("：").append(review.feedback());
            }
            if (hasText(review.retryInstruction())) {
                builder.append("；重试指令：").append(review.retryInstruction());
            }
            return builder.toString();
        }

        private boolean shouldGrantBonusRetry(Exception error) {
            if (error instanceof IOException) {
                return true;
            }
            String message = error.getMessage();
            if (!hasText(message)) {
                return false;
            }
            return message.contains("Unexpected character")
                    || message.contains("AI JSON")
                    || message.contains("合法 JSON")
                    || message.contains("本地校验未通过")
                    || message.contains("AI 审核未通过")
                    || message.contains("evidence")
                    || message.contains("correctReason")
                    || message.contains("reasoningSummary")
                    || message.contains("选项分析");
        }

        private EnrichmentAssessment assessQuestion(ReadingQuestionRecord question) {
            List<String> gaps = new ArrayList<>();
            if (!hasText(question.answerText())) {
                gaps.add("缺少标准答案");
            }
            if (!hasText(question.analysis())) {
                gaps.add("缺少题目解析");
            }
            if (args.overwriteExisting()) {
                if (gaps.isEmpty()) {
                    return EnrichmentAssessment.needsAi("覆盖更新已有答案与解析");
                }
                return EnrichmentAssessment.needsAi("覆盖更新，且当前仍有缺口：" + String.join("，", gaps));
            }
            if (gaps.isEmpty()) {
                return EnrichmentAssessment.complete("题目已存在答案与解析");
            }
            return EnrichmentAssessment.needsAi(String.join("，", gaps));
        }

        private ReadingQuestionRecord loadSingleTargetQuestion() throws SQLException {
            List<ReadingQuestionRecord> records = loadQuestions(true);
            return records.isEmpty() ? null : records.get(0);
        }

        private List<ReadingQuestionRecord> loadTargetQuestions() throws SQLException {
            return loadQuestions(false);
        }

        private List<ReadingQuestionRecord> loadQuestions(boolean singleMode) throws SQLException {
            try (Connection connection = openConnection()) {
                StringBuilder sql = new StringBuilder("""
                        SELECT
                            q.id AS question_id,
                            q.passage_id,
                            p.passage_code,
                            p.exam_direction,
                            p.source_year,
                            p.source_name,
                            p.passage_no,
                            p.passage_text,
                            q.question_no,
                            q.stem,
                            q.answer_text,
                            q.analysis
                        FROM kyyy_reading_question q
                        JOIN kyyy_reading_passage p ON p.id = q.passage_id
                        WHERE q.status = 1
                          AND p.status = 1
                        """);
                List<Object> parameters = new ArrayList<>();

                if (singleMode) {
                    if (args.questionId() != null) {
                        sql.append(" AND q.id = ?");
                        parameters.add(args.questionId());
                    } else {
                        sql.append(" AND p.passage_code = ?");
                        sql.append(" AND q.question_no = ?");
                        parameters.add(args.passageCode());
                        parameters.add(args.questionNo());
                    }
                } else if (!args.overwriteExisting()) {
                    sql.append("""
                             AND (
                                 q.answer_text IS NULL OR TRIM(q.answer_text) = ''
                                 OR q.analysis IS NULL OR TRIM(q.analysis) = ''
                             )
                            """);
                }

                if (hasText(args.direction())) {
                    sql.append(" AND p.exam_direction = ?");
                    parameters.add(args.direction());
                }
                if (args.year() != null) {
                    sql.append(" AND p.source_year = ?");
                    parameters.add(args.year());
                }
                sql.append(" ORDER BY p.exam_direction, p.source_year, p.passage_no, q.question_no");
                if (args.limit() != null) {
                    sql.append(" LIMIT ?");
                    parameters.add(args.limit());
                }

                List<ReadingQuestionRecord> records = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                    bindParameters(statement, parameters);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            long questionId = resultSet.getLong("question_id");
                            records.add(new ReadingQuestionRecord(
                                    questionId,
                                    resultSet.getLong("passage_id"),
                                    resultSet.getString("passage_code"),
                                    resultSet.getString("exam_direction"),
                                    getInteger(resultSet, "source_year"),
                                    resultSet.getString("source_name"),
                                    getInteger(resultSet, "passage_no"),
                                    resultSet.getString("passage_text"),
                                    resultSet.getInt("question_no"),
                                    resultSet.getString("stem"),
                                    resultSet.getString("answer_text"),
                                    resultSet.getString("analysis"),
                                    loadOptions(connection, questionId)
                            ));
                        }
                    }
                }
                return records;
            }
        }

        private List<ReadingOptionRecord> loadOptions(Connection connection, Long questionId) throws SQLException {
            String sql = """
                    SELECT option_key, option_content
                    FROM kyyy_reading_question_option
                    WHERE question_id = ?
                    ORDER BY sort_no ASC, option_key ASC
                    """;
            List<ReadingOptionRecord> options = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, questionId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        options.add(new ReadingOptionRecord(
                                resultSet.getString("option_key"),
                                resultSet.getString("option_content")
                        ));
                    }
                }
            }
            return options;
        }

        private void writeEnrichment(ReadingQuestionRecord question,
                                     ValidatedEnrichment enrichment,
                                     AiCallResult aiCall) throws SQLException {
            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                try {
                    updateQuestion(connection, question.questionId(), enrichment);
                    resetQuestionOptions(connection, question.questionId());
                    markCorrectOption(connection, question.questionId(), enrichment.answerKey());
                    recordLog(connection, question, EnrichmentAssessment.needsAi("写入成功"), "success", enrichment, aiCall, null);
                    connection.commit();
                } catch (SQLException error) {
                    connection.rollback();
                    throw error;
                }
            }
        }

        private void updateQuestion(Connection connection,
                                    Long questionId,
                                    ValidatedEnrichment enrichment) throws SQLException {
            String sql = """
                    UPDATE kyyy_reading_question
                    SET answer_text = ?, analysis = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, enrichment.answerKey());
                statement.setString(2, enrichment.analysis());
                statement.setLong(3, questionId);
                statement.executeUpdate();
            }
        }

        private void resetQuestionOptions(Connection connection, Long questionId) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE kyyy_reading_question_option
                    SET is_correct = 0, updated_at = CURRENT_TIMESTAMP
                    WHERE question_id = ?
                    """)) {
                statement.setLong(1, questionId);
                statement.executeUpdate();
            }
        }

        private void markCorrectOption(Connection connection,
                                       Long questionId,
                                       String answerKey) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE kyyy_reading_question_option
                    SET is_correct = 1, updated_at = CURRENT_TIMESTAMP
                    WHERE question_id = ? AND option_key = ?
                    """)) {
                statement.setLong(1, questionId);
                statement.setString(2, answerKey);
                int updated = statement.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("未找到对应正确选项：" + answerKey + " questionId=" + questionId);
                }
            }
        }

        private void recordFailureQuietly(ReadingQuestionRecord question,
                                          EnrichmentAssessment assessment,
                                          Exception error) {
            try (Connection connection = openConnection()) {
                recordLog(connection, question, assessment, "failed", null, null, truncate(error.getMessage(), 1000));
            } catch (Exception ignored) {
                // Ignore secondary logging failures.
            }
        }

        private void recordLog(ReadingQuestionRecord question,
                               EnrichmentAssessment assessment,
                               String status,
                               ValidatedEnrichment enrichment,
                               AiCallResult aiCall,
                               String errorMessage) throws SQLException {
            try (Connection connection = openConnection()) {
                recordLog(connection, question, assessment, status, enrichment, aiCall, errorMessage);
            }
        }

        private void recordLog(Connection connection,
                               ReadingQuestionRecord question,
                               EnrichmentAssessment assessment,
                               String status,
                               ValidatedEnrichment enrichment,
                               AiCallResult aiCall,
                               String errorMessage) throws SQLException {
            String sql = """
                    INSERT INTO kyyy_reading_ai_enrichment_log (
                        run_id, question_id, passage_id, passage_code, exam_direction, source_year, question_no,
                        status, model, prompt_hash, response_hash, answer_key, confidence, analysis_length,
                        input_tokens, output_tokens, total_tokens, error_message
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, runId);
                statement.setLong(2, question.questionId());
                statement.setLong(3, question.passageId());
                statement.setString(4, question.passageCode());
                statement.setString(5, question.examDirection());
                if (question.sourceYear() == null) {
                    statement.setNull(6, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(6, question.sourceYear());
                }
                statement.setInt(7, question.questionNo());
                statement.setString(8, status);
                statement.setString(9, aiCall == null ? config.openAiModel() : aiCall.model());
                statement.setString(10, aiCall == null ? null : aiCall.promptHash());
                statement.setString(11, aiCall == null ? null : aiCall.responseHash());
                statement.setString(12, enrichment == null ? null : enrichment.answerKey());
                if (enrichment == null) {
                    statement.setNull(13, java.sql.Types.DECIMAL);
                } else {
                    statement.setBigDecimal(13, enrichment.confidence().stripTrailingZeros());
                }
                statement.setInt(14, enrichment == null ? 0 : enrichment.analysis().length());
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().inputTokens() == null) {
                    statement.setNull(15, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(15, aiCall.usage().inputTokens());
                }
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().outputTokens() == null) {
                    statement.setNull(16, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(16, aiCall.usage().outputTokens());
                }
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().totalTokens() == null) {
                    statement.setNull(17, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(17, aiCall.usage().totalTokens());
                }
                statement.setString(18, hasText(errorMessage) ? errorMessage : assessment.reason());
                statement.executeUpdate();
            }
        }

        private Connection openConnection() throws SQLException {
            return DriverManager.getConnection(config.dbUrl(), config.dbUsername(), config.dbPassword());
        }
    }

    private static final class AiGenerator {

        private final ToolConfig config;
        private final OpenAIClient client;

        private AiGenerator(ToolConfig config) {
            this.config = config;
            if ("responses".equals(config.openAiApiType())) {
                OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                        .apiKey(config.openAiApiKey())
                        .timeout(Duration.ofSeconds(config.openAiTimeoutSeconds()))
                        .maxRetries(config.openAiMaxRetries());
                if (hasText(config.openAiBaseUrl())) {
                    builder.baseUrl(config.openAiBaseUrl());
                }
                this.client = builder.build();
            } else {
                this.client = null;
            }
        }

        private AiCallResult generate(ReadingQuestionRecord question,
                                      EnrichmentAssessment assessment,
                                      List<String> retryFeedback,
                                      boolean overwriteExisting) throws Exception {
            String systemPrompt = buildGenerationSystemPrompt(overwriteExisting);
            String userPrompt = buildUserPrompt(question, assessment, retryFeedback, overwriteExisting);
            String jsonSchema = buildJsonSchema();
            String promptHash = sha256(systemPrompt + "\n\n" + userPrompt + "\n\n" + jsonSchema);
            StructuredOutput output = callStructuredOutput(
                    systemPrompt,
                    userPrompt,
                    "kyyy_reading_enrichment",
                    jsonSchema,
                    config.openAiMaxOutputTokens()
            );
            AiResponse response = parseAiResponse(output.outputText());
            return new AiCallResult(
                    output.model(),
                    promptHash,
                    sha256(output.outputText()),
                    output.outputText(),
                    response,
                    output.usage()
            );
        }

        private AiReviewResult review(ReadingQuestionRecord question,
                                      ValidatedEnrichment enrichment,
                                      String rawJson,
                                      boolean overwriteExisting) throws Exception {
            String systemPrompt = buildReviewSystemPrompt(overwriteExisting);
            String userPrompt = buildReviewPrompt(question, enrichment, rawJson, overwriteExisting);
            String jsonSchema = buildReviewJsonSchema();
            StructuredOutput output = callStructuredOutput(
                    systemPrompt,
                    userPrompt,
                    "kyyy_reading_review",
                    jsonSchema,
                    Math.min(config.openAiMaxOutputTokens(), 900)
            );
            return parseAiReviewResult(output.outputText());
        }

        private String buildGenerationSystemPrompt(boolean overwriteExisting) {
            if (overwriteExisting) {
                return """
                        你是考研英语阅读真题编辑。只根据给定文章、题干和选项判断单选题的最优答案，并输出结构化解析草稿。
                        不要编造 PDF 中不存在的题目、选项、段落或背景知识，不要输出隐藏推理过程，不要输出 JSON 之外的正文。
                        evidence 优先给出阅读正文中的原文英文摘录；必要时可以用省略号裁剪，但不要改写原文词语。
                        当前任务允许覆盖库内旧答案与旧解析，你应以当前原文证据和题干判断为准，而不是机械沿用旧值。
                        解析草稿要覆盖正确项理由、四个选项的逐项判断和一句简短总结，便于后续本地模板化入库。
                        """;
            }
            return """
                    你是考研英语阅读真题编辑。只根据给定文章、题干和选项判断单选题的最优答案，并输出结构化解析草稿。
                    不要编造 PDF 中不存在的题目、选项、段落或背景知识，不要输出隐藏推理过程，不要输出 JSON 之外的正文。
                    evidence 优先给出阅读正文中的原文英文摘录；必要时可以用省略号裁剪，但不要改写原文词语。
                    如果给定题目已有标准答案，你的新答案必须与已有标准答案一致，否则返回 UNKNOWN 并说明冲突。
                    解析草稿要覆盖正确项理由、四个选项的逐项判断和一句简短总结，便于后续本地模板化入库。
                    """;
        }

        private String buildReviewSystemPrompt(boolean overwriteExisting) {
            if (overwriteExisting) {
                return """
                        你是考研英语阅读题质检员。只判断给定候选答案、原文证据和中文解析是否可以直接写入题库。
                        审核重点：
                        1. evidence 应与原文一致并能支持答案判断，允许合理裁剪或省略号省写；
                        2. 正确项分析不能与 evidence 或题干冲突；
                        3. 干扰项分析不能明显张冠李戴；
                        4. 解析要简洁、稳定、适合题库落库。
                        当前任务允许覆盖旧答案与旧解析，不要因为与库内旧值不同就拒绝；只看新的判断是否自洽、是否有证据支撑。
                        只有存在明显事实错误、证据失配、答案不稳、或解析结构失真时才拒绝。
                        只返回 JSON，不要输出解释性正文。
                        """;
            }
            return """
                    你是考研英语阅读题质检员。只判断给定候选答案、原文证据和中文解析是否可以直接写入题库。
                    审核重点：
                    1. evidence 应与原文一致并能支持答案判断，允许合理裁剪或省略号省写；
                    2. 正确项分析不能与 evidence 或题干冲突；
                    3. 干扰项分析不能明显张冠李戴；
                    4. 解析要简洁、稳定、适合题库落库。
                    只有存在明显事实错误、证据失配、答案不稳、或解析结构失真时才拒绝。
                    只返回 JSON，不要输出解释性正文。
                    """;
        }

        private StructuredOutput callStructuredOutput(String systemPrompt,
                                                      String userPrompt,
                                                      String schemaName,
                                                      String jsonSchema,
                                                      int maxOutputTokens) throws Exception {
            if ("chat_completions".equals(config.openAiApiType())) {
                return generateWithChatCompletions(systemPrompt, userPrompt, schemaName, jsonSchema, maxOutputTokens);
            }
            return generateWithResponses(systemPrompt, userPrompt, schemaName, jsonSchema, maxOutputTokens);
        }

        private StructuredOutput generateWithResponses(String systemPrompt,
                                                       String userPrompt,
                                                       String schemaName,
                                                       String jsonSchema,
                                                       int maxOutputTokens) throws Exception {
            ResponseCreateParams params = buildParams(systemPrompt, userPrompt, schemaName, jsonSchema, maxOutputTokens);
            Response response = client.responses().create(params);
            String outputText = extractOutputText(response);
            if (!hasText(outputText)) {
                throw new IllegalStateException("Responses API 返回内容为空");
            }
            outputText = stripJsonEnvelope(outputText);
            Usage usage = response.usage()
                    .map(AiGenerator::toUsage)
                    .orElse(new Usage(null, null, null));
            return new StructuredOutput(response.model().asString(), outputText, usage);
        }

        private StructuredOutput generateWithChatCompletions(String systemPrompt,
                                                             String userPrompt,
                                                             String schemaName,
                                                             String jsonSchema,
                                                             int maxOutputTokens) throws Exception {
            ObjectNode body = OBJECT_MAPPER.createObjectNode();
            body.put("model", config.openAiModel());
            body.put("stream", false);
            body.put("max_tokens", maxOutputTokens);
            if (hasText(config.openAiReasoningEffort())) {
                body.put("reasoning_effort", config.openAiReasoningEffort());
            }
            ArrayNode messages = body.putArray("messages");
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            ObjectNode responseFormat = body.putObject("response_format");
            responseFormat.put("type", "json_schema");
            ObjectNode jsonSchemaNode = responseFormat.putObject("json_schema");
            jsonSchemaNode.put("name", schemaName);
            jsonSchemaNode.put("strict", true);
            jsonSchemaNode.set("schema", OBJECT_MAPPER.readTree(jsonSchema));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl("/chat/completions")))
                    .timeout(Duration.ofSeconds(config.openAiTimeoutSeconds()))
                    .header("Authorization", "Bearer " + config.openAiApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(config.openAiTimeoutSeconds()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Chat Completions 调用失败 HTTP " + response.statusCode() + ": "
                        + truncate(response.body(), 1000));
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("Chat Completions 返回缺少 choices: " + truncate(response.body(), 1000));
            }
            JsonNode contentNode = choices.get(0).path("message").path("content");
            String outputText = contentNode.isTextual() ? contentNode.asText() : contentNode.toString();
            outputText = stripJsonEnvelope(outputText);
            if (!hasText(outputText)) {
                throw new IllegalStateException("Chat Completions 返回内容为空");
            }
            Usage usage = parseChatUsage(root.path("usage"));
            String model = root.path("model").asText(config.openAiModel());
            return new StructuredOutput(model, outputText, usage);
        }

        private String apiUrl(String path) {
            String baseUrl = hasText(config.openAiBaseUrl())
                    ? config.openAiBaseUrl().trim()
                    : "https://api.openai.com/v1";
            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            if (baseUrl.endsWith("/")) {
                return baseUrl.substring(0, baseUrl.length() - 1) + normalizedPath;
            }
            return baseUrl + normalizedPath;
        }

        private Usage parseChatUsage(JsonNode usageNode) {
            if (usageNode == null || usageNode.isMissingNode() || usageNode.isNull()) {
                return new Usage(null, null, null);
            }
            Integer inputTokens = usageNode.has("prompt_tokens") ? usageNode.path("prompt_tokens").asInt() : null;
            Integer outputTokens = usageNode.has("completion_tokens") ? usageNode.path("completion_tokens").asInt() : null;
            Integer totalTokens = usageNode.has("total_tokens") ? usageNode.path("total_tokens").asInt() : null;
            return new Usage(inputTokens, outputTokens, totalTokens);
        }

        private ResponseCreateParams buildParams(String systemPrompt,
                                                 String userPrompt,
                                                 String schemaName,
                                                 String jsonSchema,
                                                 int maxOutputTokens) throws Exception {
            ResponseCreateParams.Builder builder = ResponseCreateParams.builder()
                    .model(config.openAiModel())
                    .instructions(systemPrompt)
                    .input(userPrompt)
                    .store(false)
                    .maxOutputTokens((long) maxOutputTokens);
            if (hasText(config.openAiReasoningEffort())) {
                builder.reasoning(Reasoning.builder()
                        .effort(ReasoningEffort.of(config.openAiReasoningEffort()))
                        .build());
            }
            ResponseTextConfig.Builder textBuilder = ResponseTextConfig.builder();
            if (hasText(config.openAiTextVerbosity())) {
                textBuilder.verbosity(ResponseTextConfig.Verbosity.of(config.openAiTextVerbosity()));
            }
            textBuilder.format(ResponseFormatTextJsonSchemaConfig.builder()
                    .name(schemaName)
                    .schema(buildSchema(jsonSchema))
                    .strict(true)
                    .build());
            builder.text(textBuilder.build());
            return builder.build();
        }

        private ResponseFormatTextJsonSchemaConfig.Schema buildSchema(String jsonSchema) throws Exception {
            JsonNode root = OBJECT_MAPPER.readTree(jsonSchema);
            ResponseFormatTextJsonSchemaConfig.Schema.Builder builder = ResponseFormatTextJsonSchemaConfig.Schema.builder();
            root.fields().forEachRemaining(field ->
                    builder.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
            return builder.build();
        }

        private String buildUserPrompt(ReadingQuestionRecord question,
                                       EnrichmentAssessment assessment,
                                       List<String> retryFeedback,
                                       boolean overwriteExisting) {
            StringBuilder optionsBuilder = new StringBuilder();
            for (ReadingOptionRecord option : question.options()) {
                optionsBuilder.append(option.optionKey())
                        .append(". ")
                        .append(option.optionContent())
                        .append('\n');
            }
            return """
                    请为下面考研英语阅读题补齐标准答案和中文解析。

                    考试方向：%s
                    来源年份：%s
                    来源标题：%s
                    篇章编码：%s
                    篇章序号：Text %d
                    题号：%d
                    当前缺口：%s
                    是否允许覆盖旧值：%s
                    已有标准答案：%s
                    已有解析：%s
                    上轮退回原因：%s

                    阅读正文：
                    %s

                    题干：
                    %s

                    选项：
                    %s

                    输出要求：
                    1. answerKey 只能是 A/B/C/D/UNKNOWN。
                    2. evidence 优先使用阅读正文中的英文原文摘录；必要时可用省略号裁剪，但不要改写原文词语。
                    3. correctReason 用中文写成自然段解析，先结合文章位置、关键信息和题干说明为什么答案成立，再自然点出正确选项。
                    4. optionAnalysis 必须给出 A/B/C/D 四项，各用中文简短说明该项为何对或为何不对，语气自然，不要写成僵硬模板。
                    5. reasoningSummary 用中文给一句补充概括，避免与 correctReason 完全重复。
                    6. confidence 返回 0 到 1 的小数。
                    7. 只返回下面形状的 JSON，不要返回 Markdown：
                       {"answerKey":"A","evidence":"英文原文摘录","correctReason":"中文","reasoningSummary":"中文","optionAnalysis":{"A":"中文","B":"中文","C":"中文","D":"中文"},"confidence":0.91}
                    """.formatted(
                    question.examDirection(),
                    question.sourceYear() == null ? "待确认" : question.sourceYear(),
                    safeText(question.sourceName()),
                    question.passageCode(),
                    question.passageNo() == null ? 0 : question.passageNo(),
                    question.questionNo(),
                    assessment.reason(),
                    overwriteExisting ? "是" : "否",
                    safeText(question.answerText()),
                    safeText(question.analysis()),
                    buildRetryFeedback(retryFeedback),
                    safeText(question.passageText()),
                    safeText(question.stem()),
                    optionsBuilder.toString().trim()
            );
        }

        private String buildRetryFeedback(List<String> retryFeedback) {
            if (retryFeedback == null || retryFeedback.isEmpty()) {
                return "无";
            }
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < retryFeedback.size(); index++) {
                if (index > 0) {
                    builder.append("；");
                }
                builder.append(index + 1).append(". ").append(truncate(retryFeedback.get(index), 240));
            }
            return builder.toString();
        }

        private String buildReviewPrompt(ReadingQuestionRecord question,
                                         ValidatedEnrichment enrichment,
                                         String rawJson,
                                         boolean overwriteExisting) {
            StringBuilder optionsBuilder = new StringBuilder();
            for (ReadingOptionRecord option : question.options()) {
                optionsBuilder.append(option.optionKey())
                        .append(". ")
                        .append(option.optionContent())
                        .append('\n');
            }
            return """
                    请审核下面阅读题候选补齐结果是否可直接入库。

                    考试方向：%s
                    来源年份：%s
                    篇章编码：%s
                    题号：%d

                    阅读正文：
                    %s

                    题干：
                    %s

                    选项：
                    %s

                    候选答案：%s
                    是否允许覆盖旧值：%s
                    候选证据摘录：%s
                    本地证据命中：%s
                    候选原文定位句：%s
                    候选最终解析：
                    %s

                    首轮原始 JSON：
                    %s

                    输出要求：
                    1. approved 返回 true/false。
                    2. reason 用中文，说明是否通过；不通过时要指出核心问题。
                    3. retryInstruction 只有在不通过时才填写，给下一轮生成一个具体修正指令。
                    4. 只返回 JSON，不要返回 Markdown。
                    """.formatted(
                    question.examDirection(),
                    question.sourceYear() == null ? "待确认" : question.sourceYear(),
                    question.passageCode(),
                    question.questionNo(),
                    safeText(question.passageText()),
                    safeText(question.stem()),
                    optionsBuilder.toString().trim(),
                    enrichment.answerKey(),
                    overwriteExisting ? "是" : "否",
                    enrichment.evidence(),
                    enrichment.evidenceLocallyMatched() ? "是" : "否",
                    enrichment.evidenceSentence(),
                    enrichment.analysis(),
                    safeText(rawJson)
            );
        }

        private String buildJsonSchema() {
            return """
                    {
                      "type": "object",
                      "additionalProperties": false,
                      "properties": {
                        "answerKey": {
                          "type": "string",
                          "enum": ["A", "B", "C", "D", "UNKNOWN"]
                        },
                        "evidence": { "type": "string" },
                        "correctReason": { "type": "string" },
                        "reasoningSummary": { "type": "string" },
                        "optionAnalysis": {
                          "type": "object",
                          "additionalProperties": false,
                          "properties": {
                            "A": { "type": "string" },
                            "B": { "type": "string" },
                            "C": { "type": "string" },
                            "D": { "type": "string" }
                          },
                          "required": ["A", "B", "C", "D"]
                        },
                        "confidence": { "type": "number" }
                      },
                      "required": ["answerKey", "evidence", "correctReason", "reasoningSummary", "optionAnalysis", "confidence"]
                    }
                    """;
        }

        private String buildReviewJsonSchema() {
            return """
                    {
                      "type": "object",
                      "additionalProperties": false,
                      "properties": {
                        "approved": { "type": "boolean" },
                        "reason": { "type": "string" },
                        "retryInstruction": { "type": "string" }
                      },
                      "required": ["approved", "reason", "retryInstruction"]
                    }
                    """;
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
                    if (outputText != null && hasText(outputText.text())) {
                        if (!builder.isEmpty()) {
                            builder.append("\n");
                        }
                        builder.append(outputText.text());
                    }
                }
            }
            return builder.toString();
        }

        private static Usage toUsage(ResponseUsage usage) {
            return new Usage(toInt(usage.inputTokens()), toInt(usage.outputTokens()), toInt(usage.totalTokens()));
        }

        private static int toInt(long value) {
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
        }
    }

    private static ValidatedEnrichment validateAiResult(ReadingQuestionRecord question,
                                                        AiResponse response,
                                                        double minConfidence,
                                                        boolean overwriteExisting) {
        if (response == null) {
            throw new IllegalArgumentException("AI JSON 为空");
        }
        String answerKey = normalizeAnswerKey(response.answerKey());
        if (!List.of("A", "B", "C", "D").contains(answerKey)) {
            throw new IllegalArgumentException("AI 返回答案无效：" + answerKey);
        }
        Set<String> availableOptions = new LinkedHashSet<>();
        for (ReadingOptionRecord option : question.options()) {
            availableOptions.add(option.optionKey());
        }
        if (!availableOptions.contains(answerKey)) {
            throw new IllegalArgumentException("AI 返回答案不在题目选项中：" + answerKey);
        }
        if (!overwriteExisting
                && hasText(question.answerText())
                && !Objects.equals(normalizeAnswerKey(question.answerText()), answerKey)) {
            throw new IllegalArgumentException("AI 返回答案与库内已有答案冲突：" + answerKey + " vs " + question.answerText());
        }
        String evidence = normalizeEvidence(response.evidence());
        if (evidence.length() < MIN_EVIDENCE_LENGTH) {
            throw new IllegalArgumentException("AI 返回 evidence 过短，无法作为原文依据：" + evidence);
        }
        if (evidence.length() > MAX_EVIDENCE_LENGTH) {
            evidence = truncate(evidence, MAX_EVIDENCE_LENGTH);
        }
        EvidenceMatch evidenceMatch = locateEvidence(question.passageText(), evidence);
        if (evidenceMatch == null) {
            evidenceMatch = new EvidenceMatch(evidence, evidence, false);
        }
        AnalysisDraft draft = validateAnalysisDraft(response, availableOptions, answerKey);
        String analysis = formatAnalysis(question, answerKey, draft);
        java.math.BigDecimal confidence = normalizeConfidence(response.confidence());
        if (confidence.doubleValue() < minConfidence) {
            throw new IllegalArgumentException("AI 置信度过低：" + confidence);
        }
        return new ValidatedEnrichment(
                answerKey,
                analysis,
                evidenceMatch.evidenceQuote(),
                evidenceMatch.sourceSentence(),
                evidenceMatch.locallyMatched(),
                confidence
        );
    }

    private static AiResponse parseAiResponse(String outputText) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(outputText);
        if (!root.isObject()) {
            throw new IllegalArgumentException("AI JSON 根节点必须是对象");
        }
        JsonNode optionAnalysisNode = root.path("optionAnalysis");
        return new AiResponse(
                firstText(root, "answerKey", "answer_key", "answer"),
                firstText(root, "evidence", "evidenceQuote", "evidence_quote"),
                firstText(root, "correctReason", "correct_reason", "correctOptionReason"),
                firstText(root, "reasoningSummary", "reasoning_summary", "summary"),
                parseOptionAnalysisDraft(optionAnalysisNode),
                firstText(root, "analysis", "analysisZh", "analysis_zh", "explanation"),
                decimalValue(root.path("confidence"))
        );
    }

    private static AiReviewResult parseAiReviewResult(String outputText) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(outputText);
        if (!root.isObject()) {
            throw new IllegalArgumentException("AI 审核 JSON 根节点必须是对象");
        }
        boolean approved = booleanValue(root.path("approved"));
        String reason = firstText(root, "reason", "message", "comment");
        String retryInstruction = firstText(root, "retryInstruction", "retry_instruction", "instruction");
        if (!approved && !hasText(reason) && !hasText(retryInstruction)) {
            throw new IllegalArgumentException("AI 审核拒绝但未返回原因");
        }
        return new AiReviewResult(approved, reason, retryInstruction);
    }

    private static OptionAnalysisDraft parseOptionAnalysisDraft(JsonNode node) {
        return new OptionAnalysisDraft(
                firstText(node, "A", "optionA", "a"),
                firstText(node, "B", "optionB", "b"),
                firstText(node, "C", "optionC", "c"),
                firstText(node, "D", "optionD", "d")
        );
    }

    private static AnalysisDraft validateAnalysisDraft(AiResponse response,
                                                       Set<String> availableOptions,
                                                       String answerKey) {
        String correctReason = normalizeText(response.correctReason());
        if (!hasText(correctReason)) {
            correctReason = normalizeText(response.legacyAnalysis());
        }
        if (!hasText(correctReason)) {
            throw new IllegalArgumentException("AI 未返回 correctReason");
        }
        String reasoningSummary = normalizeText(response.reasoningSummary());
        if (!hasText(reasoningSummary)) {
            reasoningSummary = normalizeText(response.legacyAnalysis());
        }
        if (!hasText(reasoningSummary)) {
            reasoningSummary = "根据原文证据可判断正确答案。";
        }
        OptionAnalysisDraft optionAnalysis = response.optionAnalysis();
        LinkedHashMap<String, String> optionComments = new LinkedHashMap<>();
        for (String optionKey : availableOptions) {
            String comment = optionAnalysis == null ? "" : normalizeText(optionAnalysis.commentFor(optionKey));
            if (!hasText(comment)) {
                comment = defaultOptionComment(optionKey, answerKey);
            }
            optionComments.put(optionKey, truncate(comment, MAX_OPTION_ANALYSIS_LENGTH));
        }
        return new AnalysisDraft(
                truncate(correctReason, MAX_CORRECT_REASON_LENGTH),
                truncate(reasoningSummary, MAX_REASONING_SUMMARY_LENGTH),
                optionComments
        );
    }

    private static String defaultOptionComment(String optionKey,
                                               String answerKey) {
        if (Objects.equals(optionKey, answerKey)) {
            return "与题干要求和原文证据一致，能够支撑正确判断。";
        }
        return "与题干要求或原文证据不完全匹配，不能作为最佳答案。";
    }

    private static String formatAnalysis(ReadingQuestionRecord question,
                                         String answerKey,
                                         AnalysisDraft draft) {
        StringBuilder builder = new StringBuilder();
        String correctReason = ensureSentence(draft.correctReason());
        String reasoningSummary = normalizeText(draft.reasoningSummary());
        builder.append(correctReason);

        if (!containsNormalized(correctReason, reasoningSummary)
                && hasText(reasoningSummary)
                && reasoningSummary.length() >= 12
                && reasoningSummary.length() <= 80) {
            builder.append(reasoningSummaryEndsWithPunctuation(reasoningSummary) ? reasoningSummary : reasoningSummary + "。");
        }

        if (!containsOptionReference(correctReason, answerKey)) {
            builder.append("选项").append(answerKey).append("准确概括了这一观点。");
        }

        boolean hasWrongOption = false;
        for (ReadingOptionRecord option : question.options()) {
            if (Objects.equals(option.optionKey(), answerKey)) {
                continue;
            }
            String comment = draft.optionComments().get(option.optionKey());
            if (!hasText(comment)) {
                continue;
            }
            if (!hasWrongOption) {
                builder.append(" ");
                hasWrongOption = true;
            } else {
                builder.append("；");
            }
            builder.append("选项")
                    .append(option.optionKey())
                    .append(normalizeOptionComment(comment));
        }

        String analysis = builder.toString().trim();
        if (analysis.length() > MAX_ANALYSIS_LENGTH) {
            analysis = truncate(analysis, MAX_ANALYSIS_LENGTH);
        }
        return analysis;
    }

    private static String ensureSentence(String value) {
        String normalized = normalizeText(value);
        if (!hasText(normalized)) {
            return "";
        }
        char tail = normalized.charAt(normalized.length() - 1);
        if ("。！？!?；;".indexOf(tail) >= 0) {
            return normalized;
        }
        return normalized + "。";
    }

    private static String normalizeOptionComment(String value) {
        String normalized = normalizeText(value);
        if (!hasText(normalized)) {
            return "";
        }
        if (normalized.startsWith("错误，")) {
            normalized = normalized.substring("错误，".length()).trim();
        } else if (normalized.startsWith("错误")) {
            normalized = normalized.substring("错误".length()).trim();
        } else if (normalized.startsWith("不正确，")) {
            normalized = normalized.substring("不正确，".length()).trim();
        } else if (normalized.startsWith("正确，")) {
            normalized = normalized.substring("正确，".length()).trim();
        }
        if (normalized.startsWith("，") || normalized.startsWith(",")) {
            normalized = normalized.substring(1).trim();
        }
        if (!hasText(normalized)) {
            return "不符合题意。";
        }
        if (normalized.startsWith("选项")) {
            normalized = normalized.substring("选项".length()).trim();
        }
        if (normalized.length() >= 2
                && normalized.charAt(0) >= 'A'
                && normalized.charAt(0) <= 'D'
                && (normalized.charAt(1) == '项' || normalized.charAt(1) == '.' || normalized.charAt(1) == '：' || normalized.charAt(1) == ':')) {
            normalized = normalized.substring(2).trim();
        }
        char tail = normalized.charAt(normalized.length() - 1);
        if ("。！？!?；;".indexOf(tail) >= 0) {
            return normalized;
        }
        return normalized + "。";
    }

    private static boolean containsOptionReference(String text,
                                                   String optionKey) {
        if (!hasText(text) || !hasText(optionKey)) {
            return false;
        }
        return text.contains("选项" + optionKey)
                || text.contains(optionKey + "项")
                || text.contains(optionKey + "正确")
                || text.contains(optionKey + "是正确")
                || text.contains("答案" + optionKey);
    }

    private static boolean containsNormalized(String source,
                                              String target) {
        if (!hasText(source) || !hasText(target)) {
            return false;
        }
        return normalizeText(source).contains(normalizeText(target));
    }

    private static boolean reasoningSummaryEndsWithPunctuation(String value) {
        if (!hasText(value)) {
            return false;
        }
        char tail = value.charAt(value.length() - 1);
        return "。！？!?；;".indexOf(tail) >= 0;
    }

    private static String normalizeEvidence(String value) {
        String normalized = normalizeText(value);
        while (hasText(normalized) && List.of("\"", "'", "“", "”", "‘", "’").contains(String.valueOf(normalized.charAt(0)))) {
            normalized = normalized.substring(1).trim();
        }
        while (hasText(normalized) && List.of("\"", "'", "“", "”", "‘", "’").contains(String.valueOf(normalized.charAt(normalized.length() - 1)))) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private static EvidenceMatch locateEvidence(String passageText,
                                                String evidence) {
        if (!hasText(passageText) || !hasText(evidence)) {
            return null;
        }
        String normalizedPassage = normalizeText(passageText);
        String normalizedEvidence = normalizeEvidence(evidence);
        if (!hasText(normalizedPassage) || !hasText(normalizedEvidence)) {
            return null;
        }
        int index = normalizedPassage.toLowerCase(Locale.ROOT).indexOf(normalizedEvidence.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return locateEvidenceBySegments(normalizedPassage, normalizedEvidence);
        }
        int start = sentenceStart(normalizedPassage, index);
        int end = sentenceEnd(normalizedPassage, index + normalizedEvidence.length());
        String sourceSentence = normalizedPassage.substring(start, end).trim();
        if (!hasText(sourceSentence)) {
            sourceSentence = normalizedEvidence;
        }
        return new EvidenceMatch(normalizedEvidence, sourceSentence, true);
    }

    private static EvidenceMatch locateEvidenceBySegments(String normalizedPassage,
                                                          String normalizedEvidence) {
        String[] segments = normalizedEvidence.split("(?:\\.\\.\\.+|…+)");
        for (String segment : segments) {
            String candidate = normalizeEvidence(segment);
            if (!hasText(candidate) || candidate.length() < MIN_EVIDENCE_LENGTH) {
                continue;
            }
            int index = normalizedPassage.toLowerCase(Locale.ROOT).indexOf(candidate.toLowerCase(Locale.ROOT));
            if (index < 0) {
                continue;
            }
            int start = sentenceStart(normalizedPassage, index);
            int end = sentenceEnd(normalizedPassage, index + candidate.length());
            String sourceSentence = normalizedPassage.substring(start, end).trim();
            if (!hasText(sourceSentence)) {
                sourceSentence = candidate;
            }
            return new EvidenceMatch(normalizedEvidence, sourceSentence, true);
        }
        return null;
    }

    private static int sentenceStart(String text, int index) {
        int cursor = Math.max(0, Math.min(index, text.length()));
        while (cursor > 0) {
            char current = text.charAt(cursor - 1);
            if (isSentenceBoundary(current)) {
                break;
            }
            cursor--;
        }
        return cursor;
    }

    private static int sentenceEnd(String text, int index) {
        int cursor = Math.max(0, Math.min(index, text.length()));
        while (cursor < text.length()) {
            char current = text.charAt(cursor);
            cursor++;
            if (isSentenceBoundary(current)) {
                break;
            }
        }
        return cursor;
    }

    private static boolean isSentenceBoundary(char value) {
        return ".!?;。！？；".indexOf(value) >= 0;
    }

    private static java.math.BigDecimal decimalValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return java.math.BigDecimal.ZERO;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual() && hasText(node.asText())) {
            return new java.math.BigDecimal(node.asText().trim());
        }
        return java.math.BigDecimal.ZERO;
    }

    private static java.math.BigDecimal normalizeConfidence(java.math.BigDecimal confidence) {
        if (confidence == null) {
            return java.math.BigDecimal.ZERO;
        }
        if (confidence.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return java.math.BigDecimal.ZERO;
        }
        if (confidence.compareTo(java.math.BigDecimal.ONE) > 0) {
            return java.math.BigDecimal.ONE;
        }
        return confidence;
    }

    private static String normalizeAnswerKey(String value) {
        String normalized = normalizeText(value).toUpperCase(Locale.ROOT);
        if ("UNKNOWN".equals(normalized) || "待确认".equals(normalized)) {
            return "UNKNOWN";
        }
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static void bindParameters(PreparedStatement statement,
                                       List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            int parameterIndex = index + 1;
            if (value == null) {
                statement.setObject(parameterIndex, null);
            } else if (value instanceof Integer integer) {
                statement.setInt(parameterIndex, integer);
            } else if (value instanceof Long longValue) {
                statement.setLong(parameterIndex, longValue);
            } else {
                statement.setString(parameterIndex, value.toString());
            }
        }
    }

    private static Integer getInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private static String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);
            if (value.isTextual() && hasText(value.asText())) {
                return value.asText().trim();
            }
        }
        return "";
    }

    private static boolean booleanValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isTextual()) {
            String normalized = node.asText().trim().toLowerCase(Locale.ROOT);
            return List.of("true", "yes", "pass", "approved", "通过", "是").contains(normalized);
        }
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String normalizeText(String value) {
        if (!hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String safeText(String value) {
        return hasText(value) ? value.trim() : "待确认";
    }

    private static String stripJsonEnvelope(String value) {
        String normalized = normalizeText(value);
        if (normalized.startsWith("```json")) {
            normalized = normalized.substring("```json".length()).trim();
        } else if (normalized.startsWith("```")) {
            normalized = normalized.substring("```".length()).trim();
        }
        if (normalized.endsWith("```")) {
            normalized = normalized.substring(0, normalized.length() - "```".length()).trim();
        }
        return normalized;
    }

    private static String truncate(String value, int limit) {
        if (value == null) {
            return null;
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, Math.max(0, limit));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 计算失败", error);
        }
    }

    private static final class InteractiveMenu {

        private final BufferedReader reader;

        private InteractiveMenu(BufferedReader reader) {
            this.reader = reader;
        }

        private static CliArgs prompt() throws IOException {
            return new InteractiveMenu(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)))
                    .promptArgs();
        }

        private CliArgs promptArgs() throws IOException {
            printMenu();
            String choice = promptRequired("请选择操作编号");
            if ("0".equals(choice)) {
                return CliArgs.exitArgs();
            }

            String configPath = promptWithDefault("配置文件路径", DEFAULT_CONFIG_PATH);
            return switch (choice) {
                case "1" -> new CliArgs(
                        configPath,
                        "all",
                        null,
                        null,
                        null,
                        promptOptionalDirection("考试方向，留空表示全部（english_one/english_two/英一/英二）"),
                        promptOptionalPositiveInteger("年份，留空表示全部"),
                        promptOptionalPositiveInteger("校验数量上限，留空表示全部"),
                        null,
                        false,
                        true,
                        false,
                        false,
                        false
                );
                case "2" -> promptSingleArgs(configPath, false, false);
                case "3" -> promptSingleArgs(configPath, true, false);
                case "4" -> new CliArgs(
                        configPath,
                        "all",
                        null,
                        null,
                        null,
                        promptOptionalDirection("考试方向，留空表示全部（english_one/english_two/英一/英二）"),
                        promptOptionalPositiveInteger("年份，留空表示全部"),
                        promptOptionalPositiveInteger("全量执行数量上限，留空表示全部"),
                        promptOptionalPositiveInteger("线程数，留空使用配置文件 run.threads"),
                        true,
                        false,
                        false,
                        false,
                        false
                );
                case "5" -> promptSingleArgs(configPath, true, true);
                case "6" -> promptOverwriteAllArgs(configPath);
                default -> throw new IllegalArgumentException("未知操作编号：" + choice);
            };
        }

        private CliArgs promptSingleArgs(String configPath, boolean apply, boolean overwriteExisting) throws IOException {
            SingleTargetInput target = promptSingleTarget();
            return new CliArgs(
                    configPath,
                    "single",
                    target.questionId(),
                    target.passageCode(),
                    target.questionNo(),
                    null,
                    null,
                    null,
                    null,
                    apply,
                    false,
                    apply && promptYesNo("是否跳过后续 " + (overwriteExisting ? "OVERWRITE" : "YES") + " 二次确认", false),
                    overwriteExisting,
                    false
            );
        }

        private CliArgs promptOverwriteAllArgs(String configPath) throws IOException {
            if (!promptYesNo("确认覆盖更新已存在答案/解析的数据", false)) {
                return CliArgs.exitArgs();
            }
            return new CliArgs(
                    configPath,
                    "all",
                    null,
                    null,
                    null,
                    promptOptionalDirection("考试方向，留空表示全部（english_one/english_two/英一/英二）"),
                    promptOptionalPositiveInteger("年份，留空表示全部"),
                    promptOptionalPositiveInteger("覆盖更新数量上限，留空表示全部"),
                    promptOptionalPositiveInteger("线程数，留空使用配置文件 run.threads"),
                    true,
                    false,
                    false,
                    true,
                    false
            );
        }

        private SingleTargetInput promptSingleTarget() throws IOException {
            System.out.print("问题 ID，留空则按 passageCode + questionNo 定位：");
            String questionIdText = reader.readLine();
            if (hasText(questionIdText)) {
                return new SingleTargetInput(CliArgs.parsePositiveLong(questionIdText), null, null);
            }
            String passageCode = promptRequired("请输入 passageCode");
            Integer questionNo = promptRequiredPositiveInteger("请输入 questionNo");
            return new SingleTargetInput(null, passageCode.trim(), questionNo);
        }

        private void printMenu() {
            System.out.println("========================================");
            System.out.println("KYYY 阅读题 AI 补齐操作菜单");
            System.out.println("1. 只校验缺口，不调用 AI，不写库");
            System.out.println("2. 单题测试，调用 AI 并打印结果，不写库");
            System.out.println("3. 单题补齐，调用 AI 并写库");
            System.out.println("4. 全量补齐，多线程调用 AI 并写库");
            System.out.println("5. 单题覆盖更新，重新生成并写库");
            System.out.println("6. 范围覆盖更新，多线程重新生成并写库");
            System.out.println("0. 退出");
            System.out.println("========================================");
        }

        private String promptRequired(String label) throws IOException {
            while (true) {
                System.out.print(label + "：");
                String value = reader.readLine();
                if (hasText(value)) {
                    return value.trim();
                }
                System.out.println("不能为空。");
            }
        }

        private String promptWithDefault(String label, String defaultValue) throws IOException {
            System.out.print(label + " [" + defaultValue + "]：");
            String value = reader.readLine();
            return hasText(value) ? value.trim() : defaultValue;
        }

        private Integer promptOptionalPositiveInteger(String label) throws IOException {
            while (true) {
                System.out.print(label + "：");
                String value = reader.readLine();
                if (!hasText(value)) {
                    return null;
                }
                try {
                    int parsed = Integer.parseInt(value.trim());
                    if (parsed > 0) {
                        return parsed;
                    }
                } catch (NumberFormatException ignored) {
                    // Fall through to the shared validation message.
                }
                System.out.println("请输入大于 0 的整数，或直接回车跳过。");
            }
        }

        private Integer promptRequiredPositiveInteger(String label) throws IOException {
            while (true) {
                Integer value = promptOptionalPositiveInteger(label);
                if (value != null) {
                    return value;
                }
                System.out.println("不能为空。");
            }
        }

        private String promptOptionalDirection(String label) throws IOException {
            while (true) {
                System.out.print(label + "：");
                String value = reader.readLine();
                if (!hasText(value)) {
                    return null;
                }
                try {
                    return CliArgs.normalizeDirectionValue(value, true);
                } catch (IllegalArgumentException error) {
                    System.out.println(error.getMessage());
                }
            }
        }

        private boolean promptYesNo(String label, boolean defaultValue) throws IOException {
            String suffix = defaultValue ? "Y/n" : "y/N";
            while (true) {
                System.out.print(label + " [" + suffix + "]：");
                String value = reader.readLine();
                if (!hasText(value)) {
                    return defaultValue;
                }
                String normalized = value.trim().toLowerCase(Locale.ROOT);
                if (List.of("y", "yes", "是").contains(normalized)) {
                    return true;
                }
                if (List.of("n", "no", "否").contains(normalized)) {
                    return false;
                }
                System.out.println("请输入 y 或 n。");
            }
        }
    }

    private record SingleTargetInput(Long questionId,
                                     String passageCode,
                                     Integer questionNo) {
    }

    private record CliArgs(String configPath,
                           String mode,
                           Long questionId,
                           String passageCode,
                           Integer questionNo,
                           String direction,
                           Integer year,
                           Integer limit,
                           Integer threads,
                           boolean apply,
                           boolean validateOnly,
                           boolean yes,
                           boolean overwriteExisting,
                           boolean shouldExit) {

        private static boolean shouldUseMenu(String[] rawArgs) {
            if (rawArgs == null || rawArgs.length == 0) {
                return true;
            }
            for (String arg : rawArgs) {
                if ("--menu".equals(arg)) {
                    return true;
                }
            }
            return false;
        }

        private static CliArgs exitArgs() {
            return new CliArgs(DEFAULT_CONFIG_PATH, "single", null, null, null, null, null, null, null, false, false, false, false, true);
        }

        private static CliArgs parse(String[] rawArgs) {
            Map<String, String> values = new LinkedHashMap<>();
            Set<String> flags = new LinkedHashSet<>();
            for (int index = 0; index < rawArgs.length; index++) {
                String arg = rawArgs[index];
                if (!arg.startsWith("--")) {
                    continue;
                }
                String key;
                String value;
                int equalsIndex = arg.indexOf('=');
                if (equalsIndex > 2) {
                    key = arg.substring(2, equalsIndex);
                    value = arg.substring(equalsIndex + 1);
                } else {
                    key = arg.substring(2);
                    if (index + 1 < rawArgs.length && !rawArgs[index + 1].startsWith("--")) {
                        value = rawArgs[++index];
                    } else {
                        value = "true";
                    }
                }
                if ("true".equalsIgnoreCase(value)) {
                    flags.add(key);
                }
                values.put(key, value);
            }
            String mode = values.getOrDefault("mode", "single").trim().toLowerCase(Locale.ROOT);
            if (!List.of("single", "all").contains(mode)) {
                throw new IllegalArgumentException("--mode 仅支持 single 或 all");
            }
            Long questionId = parsePositiveLong(values.get("question-id"));
            String passageCode = normalizeDirectionValue(values.get("passage-code"), false);
            Integer questionNo = parsePositiveInteger(values.get("question-no"));
            if ("single".equals(mode) && questionId == null && (!hasText(passageCode) || questionNo == null)) {
                throw new IllegalArgumentException("--mode single 必须提供 --question-id，或同时提供 --passage-code 与 --question-no");
            }
            String direction = normalizeDirectionValue(values.get("direction"), true);
            return new CliArgs(
                    values.getOrDefault("config", DEFAULT_CONFIG_PATH),
                    mode,
                    questionId,
                    passageCode,
                    questionNo,
                    direction,
                    parsePositiveInteger(values.get("year")),
                    parsePositiveInteger(values.get("limit")),
                    parsePositiveInteger(values.get("threads")),
                    flags.contains("apply"),
                    flags.contains("validate-only"),
                    flags.contains("yes"),
                    flags.contains("overwrite-existing"),
                    false
            );
        }

        private static String normalizeDirectionValue(String value, boolean asDirection) {
            if (!hasText(value)) {
                return null;
            }
            String normalized = value.trim();
            if (!asDirection) {
                return normalized;
            }
            String lower = normalized.toLowerCase(Locale.ROOT).replace('-', '_');
            return switch (lower) {
                case "english_one", "one", "english1", "英一", "英语一" -> "english_one";
                case "english_two", "two", "english2", "英二", "英语二" -> "english_two";
                default -> throw new IllegalArgumentException("考试方向仅支持 english_one 或 english_two");
            };
        }

        private static Integer parsePositiveInteger(String value) {
            if (!hasText(value)) {
                return null;
            }
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("数字参数必须大于 0：" + value);
            }
            return parsed;
        }

        private static Long parsePositiveLong(String value) {
            if (!hasText(value)) {
                return null;
            }
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException("数字参数必须大于 0：" + value);
            }
            return parsed;
        }
    }

    private record ToolConfig(String dbUrl,
                              String dbUsername,
                              String dbPassword,
                              String openAiBaseUrl,
                              String openAiApiType,
                              String openAiApiKey,
                              String openAiModel,
                              String openAiReasoningEffort,
                              String openAiTextVerbosity,
                              int openAiMaxOutputTokens,
                              int openAiTimeoutSeconds,
                              int openAiMaxRetries,
                              int runThreads,
                              double qualityMinConfidence,
                              int qualityAiMaxAttempts,
                              boolean qualityAiReviewEnabled) {

        private static ToolConfig load(String configPath, boolean requireOpenAiConfig) throws IOException {
            Path path = Path.of(configPath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("配置文件不存在：" + path.toAbsolutePath()
                        + "，请复制 kyyy-reading-ai-enrichment.example.properties 为本地 local.properties 后填写配置。");
            }
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            ToolConfig config = new ToolConfig(
                    required(properties, "db.url"),
                    required(properties, "db.username"),
                    properties.getProperty("db.password", ""),
                    properties.getProperty("openai.baseUrl", "https://api.openai.com/v1").trim(),
                    normalizeApiType(properties.getProperty("openai.apiType", "responses")),
                    requireOpenAiConfig ? required(properties, "openai.apiKey") : properties.getProperty("openai.apiKey", "").trim(),
                    properties.getProperty("openai.model", "gpt-5.5").trim(),
                    properties.getProperty("openai.reasoningEffort", "medium").trim(),
                    properties.getProperty("openai.textVerbosity", "medium").trim(),
                    parseInt(properties, "openai.maxOutputTokens", 900),
                    parseInt(properties, "openai.timeoutSeconds", 90),
                    parseInt(properties, "openai.maxRetries", 2),
                    parseInt(properties, "run.threads", 4),
                    parseDouble(properties, "quality.minConfidence", 0.60d),
                    parseInt(properties, "quality.aiMaxAttempts", 2),
                    Boolean.parseBoolean(properties.getProperty("quality.aiReviewEnabled", "true").trim())
            );
            if (requireOpenAiConfig) {
                config.validate();
            }
            return config;
        }

        private void validate() {
            if (!List.of("responses", "chat_completions").contains(openAiApiType)) {
                throw new IllegalArgumentException("openai.apiType 仅支持 responses 或 chat_completions");
            }
            if (qualityAiMaxAttempts < 1) {
                throw new IllegalArgumentException("quality.aiMaxAttempts 必须大于 0");
            }
            if (!hasText(openAiApiKey)) {
                throw new IllegalArgumentException("缺少配置：openai.apiKey");
            }
            if (openAiApiKey.contains("replace_with") || openAiApiKey.contains("***") || openAiApiKey.contains("…")) {
                throw new IllegalArgumentException("openai.apiKey 仍是占位或脱敏值");
            }
        }

        private static String normalizeApiType(String value) {
            String normalized = hasText(value) ? value.trim().toLowerCase(Locale.ROOT).replace('-', '_') : "responses";
            if ("chat".equals(normalized) || "chat_completion".equals(normalized) || "chat_completions".equals(normalized)) {
                return "chat_completions";
            }
            return "responses";
        }

        private static String required(Properties properties, String key) {
            String value = properties.getProperty(key);
            if (!hasText(value)) {
                throw new IllegalArgumentException("缺少配置：" + key);
            }
            return value.trim();
        }

        private static int parseInt(Properties properties, String key, int defaultValue) {
            String value = properties.getProperty(key);
            if (!hasText(value)) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        }

        private static double parseDouble(Properties properties, String key, double defaultValue) {
            String value = properties.getProperty(key);
            if (!hasText(value)) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        }
    }

    private record ReadingQuestionRecord(Long questionId,
                                         Long passageId,
                                         String passageCode,
                                         String examDirection,
                                         Integer sourceYear,
                                         String sourceName,
                                         Integer passageNo,
                                         String passageText,
                                         Integer questionNo,
                                         String stem,
                                         String answerText,
                                         String analysis,
                                         List<ReadingOptionRecord> options) {
    }

    private record ReadingOptionRecord(String optionKey,
                                       String optionContent) {
    }

    private record EnrichmentAssessment(boolean needsAi,
                                        String reason) {

        private static EnrichmentAssessment complete(String reason) {
            return new EnrichmentAssessment(false, reason);
        }

        private static EnrichmentAssessment needsAi(String reason) {
            return new EnrichmentAssessment(true, reason);
        }
    }

    private record AiResponse(String answerKey,
                              String evidence,
                              String correctReason,
                              String reasoningSummary,
                              OptionAnalysisDraft optionAnalysis,
                              String legacyAnalysis,
                              java.math.BigDecimal confidence) {
    }

    private record ValidatedEnrichment(String answerKey,
                                       String analysis,
                                       String evidence,
                                       String evidenceSentence,
                                       boolean evidenceLocallyMatched,
                                       java.math.BigDecimal confidence) {
    }

    private record OptionAnalysisDraft(String optionA,
                                       String optionB,
                                       String optionC,
                                       String optionD) {

        private String commentFor(String optionKey) {
            return switch (optionKey) {
                case "A" -> optionA;
                case "B" -> optionB;
                case "C" -> optionC;
                case "D" -> optionD;
                default -> "";
            };
        }
    }

    private record AnalysisDraft(String correctReason,
                                 String reasoningSummary,
                                 LinkedHashMap<String, String> optionComments) {
    }

    private record EvidenceMatch(String evidenceQuote,
                                 String sourceSentence,
                                 boolean locallyMatched) {
    }

    private record AiReviewResult(boolean approved,
                                  String feedback,
                                  String retryInstruction) {
    }

    private record Usage(Integer inputTokens,
                         Integer outputTokens,
                         Integer totalTokens) {
    }

    private record StructuredOutput(String model,
                                    String outputText,
                                    Usage usage) {
    }

    private record AiCallResult(String model,
                                String promptHash,
                                String responseHash,
                                String rawJson,
                                AiResponse response,
                                Usage usage) {
    }

    private record ProcessResult(boolean success,
                                 ReadingQuestionRecord question,
                                 EnrichmentAssessment assessment,
                                 ValidatedEnrichment validatedEnrichment,
                                 AiCallResult aiCall,
                                 String message) {

        private static ProcessResult skipped(ReadingQuestionRecord question,
                                             EnrichmentAssessment assessment) {
            return new ProcessResult(false, question, assessment, null, null,
                    "跳过 questionId=" + question.questionId() + " reason=" + assessment.reason());
        }

        private static ProcessResult success(ReadingQuestionRecord question,
                                             EnrichmentAssessment assessment,
                                             ValidatedEnrichment enrichment,
                                             AiCallResult aiCall) {
            return new ProcessResult(true, question, assessment, enrichment, aiCall,
                    "完成 questionId=" + question.questionId()
                            + " passage=" + question.passageCode()
                            + " q=" + question.questionNo()
                            + " answer=" + enrichment.answerKey()
                            + " confidence=" + enrichment.confidence());
        }

        private static ProcessResult failed(ReadingQuestionRecord question,
                                            EnrichmentAssessment assessment,
                                            String errorMessage) {
            return new ProcessResult(false, question, assessment, null, null,
                    "失败 questionId=" + question.questionId() + " error=" + errorMessage);
        }
    }
}
