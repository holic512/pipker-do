/**
 * @file KyyyWordAiEnrichmentMain
 * @project pipker-do
 * @module 考研英语 / 词库AI补齐
 * @description 提供可单独 main 执行的词库例句与近义词 AI 补齐工具。
 * @logic 1. 通过交互菜单或命令行参数选择运行模式；2. 读取本地 OpenAI 与数据库配置；3. 按 apiType 调用 Responses 或 Chat Completions；4. 以单词级事务写入例句、近义词和补齐日志。
 * @dependencies OpenAI Responses/Chat Completions API, JDBC: kyyy_word/kyyy_word_example/kyyy_word_related/kyyy_word_ai_enrichment_log
 * @index_tags 考研英语, 词库补齐, OpenAI, 多线程, main工具
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
import java.util.Arrays;
import java.util.Collections;
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

public final class KyyyWordAiEnrichmentMain {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_CONFIG_PATH = "backend/backend-kyyy/src/main/resources/kyyy-word-ai-enrichment.kyyy-word-ai-enrichment.local.properties";
    private static final DateTimeFormatter RUN_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_EXAMPLE_COUNT = 4;
    private static final int MAX_SYNONYM_COUNT = 6;
    private static final int MAX_SENTENCE_LENGTH = 500;
    private static final int MAX_TRANSLATION_LENGTH = 500;
    private static final int MAX_WORD_LENGTH = 100;
    private static final int MAX_MEANING_LENGTH = 255;
    private static final Map<String, Set<String>> IRREGULAR_WORD_FORMS = buildIrregularWordForms();

    private KyyyWordAiEnrichmentMain() {
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
            this.runId = "kyyy-ai-" + LocalDateTime.now().format(RUN_TIME_FORMATTER) + "-" + UUID.randomUUID().toString().substring(0, 8);
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
                statement.executeQuery("SELECT 1 FROM kyyy_word_example LIMIT 1").close();
                statement.executeQuery("SELECT source_type FROM kyyy_word_related LIMIT 1").close();
                statement.executeQuery("SELECT 1 FROM kyyy_word_ai_enrichment_log LIMIT 1").close();
            }
        }

        private void printHeader() {
            System.out.println("KYYY 词库 AI 补齐工具");
            System.out.println("runId=" + runId);
            System.out.println("mode=" + args.mode() + ", apply=" + args.apply() + ", validateOnly=" + args.validateOnly());
            System.out.println("apiType=" + config.openAiApiType() + ", model=" + config.openAiModel() + ", reasoningEffort=" + config.openAiReasoningEffort());
            System.out.println("aiReview=" + config.qualityAiReviewEnabled() + ", aiMaxAttempts=" + config.qualityAiMaxAttempts());
            System.out.println();
        }

        private void runValidateOnly() throws SQLException {
            List<WordRecord> words = loadTargetWords();
            int needsAiCount = 0;
            for (WordRecord word : words) {
                EnrichmentAssessment assessment = assessWord(word);
                if (assessment.needsAi()) {
                    needsAiCount++;
                }
                printAssessment(word, assessment);
            }
            System.out.printf(Locale.ROOT, "validate summary: total=%d, needsAi=%d%n", words.size(), needsAiCount);
        }

        private void runSingle() throws Exception {
            if (!hasText(args.word())) {
                throw new IllegalArgumentException("--mode single 必须提供 --word");
            }
            WordRecord word = loadWordByText(args.word());
            if (word == null) {
                throw new IllegalArgumentException("未找到启用单词：" + args.word());
            }
            ProcessResult result = processWord(word, true);
            System.out.println(result.message());
            if (args.apply() && result.enrichment() != null && confirmSingleApply()) {
                WriteSummary summary = writeEnrichment(word, result.assessment(), result.enrichment(), result.aiCall());
                System.out.printf(Locale.ROOT, "写入完成：examples=%d, synonyms=%d%n", summary.exampleCount(), summary.synonymCount());
            } else if (args.apply()) {
                System.out.println("未确认写入，数据库未修改。");
            }
        }

        private boolean confirmSingleApply() throws IOException {
            if (args.yes()) {
                return true;
            }
            System.out.print("输入 YES 确认写入数据库：");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            return "YES".equals(reader.readLine());
        }

        private void runAll() throws Exception {
            List<WordRecord> words = loadTargetWords();
            if (!args.apply()) {
                System.out.println("全量模式未提供 --apply：只执行缺口校验，不调用 AI、不写库。");
                int needsAiCount = 0;
                for (WordRecord word : words) {
                    EnrichmentAssessment assessment = assessWord(word);
                    if (assessment.needsAi()) {
                        needsAiCount++;
                    }
                    printAssessment(word, assessment);
                }
                System.out.printf(Locale.ROOT, "dry summary: total=%d, needsAi=%d%n", words.size(), needsAiCount);
                return;
            }

            int threads = args.threads() == null ? config.runThreads() : args.threads();
            ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, threads));
            AtomicInteger completed = new AtomicInteger();
            List<Future<ProcessResult>> futures = new ArrayList<>();
            for (WordRecord word : words) {
                futures.add(executor.submit(() -> {
                    try {
                        ProcessResult result = processWord(word, false);
                        if (result.enrichment() != null) {
                            writeEnrichment(word, result.assessment(), result.enrichment(), result.aiCall());
                        } else if (!result.assessment().needsAi()) {
                            recordLog(word, result.assessment(), "skipped", null, null, 0, 0, null);
                        }
                        int done = completed.incrementAndGet();
                        synchronized (System.out) {
                            System.out.printf(Locale.ROOT, "[%d/%d] %s%n", done, words.size(), result.message());
                        }
                        return result;
                    } catch (Exception error) {
                        EnrichmentAssessment fallback = EnrichmentAssessment.failed("exception:" + error.getMessage());
                        recordFailureQuietly(word, fallback, error);
                        int done = completed.incrementAndGet();
                        synchronized (System.out) {
                            System.out.printf(Locale.ROOT, "[%d/%d] failed word=%s error=%s%n", done, words.size(), word.wordText(), error.getMessage());
                        }
                        return ProcessResult.failed(word, fallback, error.getMessage());
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
            System.out.printf(Locale.ROOT, "all summary: total=%d, success=%d, skipped=%d, failed=%d%n", words.size(), success, skipped, failed);
        }

        private ProcessResult processWord(WordRecord word, boolean printDiff) throws Exception {
            EnrichmentAssessment assessment = assessWord(word);
            if (!assessment.needsAi()) {
                return ProcessResult.skipped(word, assessment);
            }
            if (assessment.blocked()) {
                if (args.apply() && !"single".equals(args.mode())) {
                    recordLog(word, assessment, "skipped", null, null, 0, 0, assessment.reason());
                }
                return ProcessResult.failed(word, assessment, assessment.reason());
            }
            AiGenerator aiGenerator = new AiGenerator(config);
            List<String> retryFeedback = new ArrayList<>();
            Exception lastError = null;
            int maxAttempts = Math.max(1, config.qualityAiMaxAttempts());
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    AiCallResult aiCall = aiGenerator.generate(word, assessment, retryFeedback);
                    ValidatedEnrichment enrichment = validateAiResult(word, aiCall.response(), config.qualityAiReviewEnabled());
                    validateQualityThreshold(enrichment);
                    if (config.qualityAiReviewEnabled()) {
                        AiReviewResult review = aiGenerator.review(word, enrichment, aiCall.rawJson());
                        if (!review.approved()) {
                            String reason = "AI 审核未通过：" + review.feedback();
                            retryFeedback.add(reason);
                            lastError = new IllegalArgumentException(reason);
                            continue;
                        }
                    }
                    if (printDiff) {
                        printWriteDiff(word, assessment, enrichment, aiCall);
                    }
                    return ProcessResult.ready(word, assessment, enrichment, aiCall);
                } catch (IllegalArgumentException | IOException error) {
                    String reason = error.getMessage();
                    retryFeedback.add("本地校验未通过：" + reason);
                    lastError = error;
                }
            }
            String reason = retryFeedback.isEmpty() ? "待确认" : retryFeedback.get(retryFeedback.size() - 1);
            throw new IllegalArgumentException("AI 生成连续 " + maxAttempts + " 次未通过写库前校验，最后原因：" + reason, lastError);
        }

        private void validateQualityThreshold(ValidatedEnrichment enrichment) {
            if (enrichment.examples().size() < config.qualityMinExamples()) {
                throw new IllegalArgumentException("AI 有效例句不足 "
                        + enrichment.examples().size() + "/" + config.qualityMinExamples());
            }
            if (enrichment.synonyms().size() < config.qualityMinSynonyms()) {
                throw new IllegalArgumentException("AI 有效近义词不足 "
                        + enrichment.synonyms().size() + "/" + config.qualityMinSynonyms());
            }
        }

        private EnrichmentAssessment assessWord(WordRecord word) throws SQLException {
            if (!hasText(word.meaningCn())) {
                return EnrichmentAssessment.blocked("meaning_cn 为空，默认不让 AI 补造基础释义");
            }
            int effectiveExampleCount = countExamples(word.id());
            int synonymCount = countSynonyms(word.id());
            List<String> reasons = new ArrayList<>();
            if (effectiveExampleCount < config.qualityMinExamples()) {
                reasons.add("有效例句 " + effectiveExampleCount + "/" + config.qualityMinExamples());
            }
            if (synonymCount < config.qualityMinSynonyms()) {
                reasons.add("近义词 " + synonymCount + "/" + config.qualityMinSynonyms());
            }
            if (reasons.isEmpty()) {
                return EnrichmentAssessment.complete(effectiveExampleCount, synonymCount);
            }
            return EnrichmentAssessment.needsAi(effectiveExampleCount, synonymCount, String.join("；", reasons));
        }

        private List<WordRecord> loadTargetWords() throws SQLException {
            if ("single".equals(args.mode())) {
                WordRecord word = loadWordByText(args.word());
                return word == null ? List.of() : List.of(word);
            }
            StringBuilder sql = new StringBuilder("""
                    SELECT word_row.id,
                           word_row.word_text,
                           word_row.normalized_word,
                           word_row.phonetic_us,
                           word_row.phonetic_uk,
                           word_row.part_of_speech,
                           word_row.meaning_cn,
                           (
                               SELECT example_row.example_sentence
                               FROM kyyy_word_example example_row
                               WHERE example_row.word_id = word_row.id
                                 AND example_row.status = 1
                               ORDER BY example_row.sort_no, example_row.id
                               LIMIT 1
                           ) AS primary_example_sentence,
                           (
                               SELECT example_row.example_translation
                               FROM kyyy_word_example example_row
                               WHERE example_row.word_id = word_row.id
                                 AND example_row.status = 1
                               ORDER BY example_row.sort_no, example_row.id
                               LIMIT 1
                           ) AS primary_example_translation
                    FROM kyyy_word word_row
                    WHERE word_row.status = 1
                    """);
            sql.append(" ORDER BY word_row.id\n");
            if (args.limit() != null && args.limit() > 0) {
                sql.append(" LIMIT ").append(args.limit());
            }
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                try (ResultSet resultSet = statement.executeQuery()) {
                List<WordRecord> words = new ArrayList<>();
                while (resultSet.next()) {
                    words.add(toWordRecord(resultSet));
                }
                return words;
                }
            }
        }

        private WordRecord loadWordByText(String wordText) throws SQLException {
            if (!hasText(wordText)) {
                return null;
            }
            String normalized = normalizeWord(wordText);
            String sql = """
                    SELECT word_row.id,
                           word_row.word_text,
                           word_row.normalized_word,
                           word_row.phonetic_us,
                           word_row.phonetic_uk,
                           word_row.part_of_speech,
                           word_row.meaning_cn,
                           (
                               SELECT example_row.example_sentence
                               FROM kyyy_word_example example_row
                               WHERE example_row.word_id = word_row.id
                                 AND example_row.status = 1
                               ORDER BY example_row.sort_no, example_row.id
                               LIMIT 1
                           ) AS primary_example_sentence,
                           (
                               SELECT example_row.example_translation
                               FROM kyyy_word_example example_row
                               WHERE example_row.word_id = word_row.id
                                 AND example_row.status = 1
                               ORDER BY example_row.sort_no, example_row.id
                               LIMIT 1
                           ) AS primary_example_translation
                    FROM kyyy_word word_row
                    WHERE word_row.status = 1
                      AND (word_row.normalized_word = ? OR word_row.word_text = ?)
                    ORDER BY word_row.id
                    LIMIT 1
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalized);
                statement.setString(2, wordText.trim());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? toWordRecord(resultSet) : null;
                }
            }
        }

        private WordRecord toWordRecord(ResultSet resultSet) throws SQLException {
            return new WordRecord(
                    resultSet.getLong("id"),
                    resultSet.getString("word_text"),
                    resultSet.getString("normalized_word"),
                    resultSet.getString("phonetic_us"),
                    resultSet.getString("phonetic_uk"),
                    resultSet.getString("part_of_speech"),
                    resultSet.getString("meaning_cn"),
                    resultSet.getString("primary_example_sentence"),
                    resultSet.getString("primary_example_translation")
            );
        }

        private int countExamples(Long wordId) throws SQLException {
            String sql = """
                    SELECT COUNT(*)
                    FROM kyyy_word_example
                    WHERE word_id = ?
                      AND status = 1
                      AND TRIM(example_sentence) <> ''
                      AND TRIM(example_translation) <> ''
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, wordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        }

        private int countSynonyms(Long wordId) throws SQLException {
            String sql = """
                    SELECT COUNT(*)
                    FROM kyyy_word_related
                    WHERE word_id = ?
                      AND status = 1
                      AND relation_type = 'synonym'
                      AND TRIM(related_word_text) <> ''
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, wordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        }

        private WriteSummary writeEnrichment(WordRecord word,
                                             EnrichmentAssessment assessment,
                                             ValidatedEnrichment enrichment,
                                             AiCallResult aiCall) throws SQLException {
            try (Connection connection = openConnection()) {
                boolean originalAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    int exampleCount = upsertExamples(connection, word, enrichment.examples());
                    int synonymCount = upsertSynonyms(connection, word, enrichment.synonyms());
                    recordLog(connection, word, assessment, "success", aiCall, enrichment, exampleCount, synonymCount, null);
                    connection.commit();
                    connection.setAutoCommit(originalAutoCommit);
                    return new WriteSummary(exampleCount, synonymCount);
                } catch (Exception error) {
                    connection.rollback();
                    connection.setAutoCommit(originalAutoCommit);
                    throw error;
                }
            }
        }

        private int upsertExamples(Connection connection, WordRecord word, List<ValidatedExample> examples) throws SQLException {
            if (examples.isEmpty()) {
                return 0;
            }
            int nextSortNo = nextSortNo(connection, "kyyy_word_example", "word_id", word.id());
            String sql = """
                    INSERT INTO kyyy_word_example (
                        word_id, example_sentence, example_translation, example_hash,
                        source_type, source_run_id, sort_no, status
                    ) VALUES (?, ?, ?, ?, 'ai', ?, ?, 1)
                    ON DUPLICATE KEY UPDATE
                        example_translation = VALUES(example_translation),
                        status = 1,
                        source_type = IF(source_type = 'manual', source_type, VALUES(source_type)),
                        source_run_id = IF(source_type = 'manual', source_run_id, VALUES(source_run_id)),
                        updated_at = CURRENT_TIMESTAMP
                    """;
            int count = 0;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < examples.size(); index++) {
                    ValidatedExample example = examples.get(index);
                    statement.setLong(1, word.id());
                    statement.setString(2, example.sentence());
                    statement.setString(3, example.translation());
                    statement.setString(4, sha256(normalizeText(example.sentence()).toLowerCase(Locale.ROOT)));
                    statement.setString(5, runId);
                    statement.setInt(6, nextSortNo + index);
                    statement.addBatch();
                    count++;
                }
                statement.executeBatch();
            }
            return count;
        }

        private int upsertSynonyms(Connection connection, WordRecord word, List<ValidatedSynonym> synonyms) throws SQLException {
            if (synonyms.isEmpty()) {
                return 0;
            }
            int nextSortNo = nextSortNo(connection, "kyyy_word_related", "word_id", word.id());
            String sql = """
                    INSERT INTO kyyy_word_related (
                        word_id, related_word_id, related_word_text, normalized_related_word, meaning_cn,
                        relation_type, source_type, source_run_id, sort_no, status
                    ) VALUES (?, ?, ?, ?, ?, 'synonym', 'ai', ?, ?, 1)
                    ON DUPLICATE KEY UPDATE
                        related_word_id = IF(related_word_id IS NULL, VALUES(related_word_id), related_word_id),
                        meaning_cn = IF(source_type = 'manual' AND meaning_cn IS NOT NULL AND TRIM(meaning_cn) <> '', meaning_cn, VALUES(meaning_cn)),
                        relation_type = IF(source_type = 'manual', relation_type, VALUES(relation_type)),
                        source_type = IF(source_type = 'manual', source_type, VALUES(source_type)),
                        source_run_id = IF(source_type = 'manual', source_run_id, VALUES(source_run_id)),
                        status = 1,
                        updated_at = CURRENT_TIMESTAMP
                    """;
            int count = 0;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < synonyms.size(); index++) {
                    ValidatedSynonym synonym = synonyms.get(index);
                    Long relatedWordId = findWordId(connection, synonym.normalizedWord());
                    statement.setLong(1, word.id());
                    if (relatedWordId == null) {
                        statement.setNull(2, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(2, relatedWordId);
                    }
                    statement.setString(3, synonym.wordText());
                    statement.setString(4, synonym.normalizedWord());
                    statement.setString(5, synonym.meaningCn());
                    statement.setString(6, runId);
                    statement.setInt(7, nextSortNo + index);
                    statement.addBatch();
                    count++;
                }
                statement.executeBatch();
            }
            return count;
        }

        private int nextSortNo(Connection connection, String tableName, String wordIdColumn, Long wordId) throws SQLException {
            String sql = "SELECT COALESCE(MAX(sort_no), -1) + 1 FROM " + tableName + " WHERE " + wordIdColumn + " = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, wordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        }

        private Long findWordId(Connection connection, String normalizedWord) throws SQLException {
            String sql = "SELECT id FROM kyyy_word WHERE normalized_word = ? AND status = 1 LIMIT 1";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedWord);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getLong(1) : null;
                }
            }
        }

        private void recordLog(WordRecord word,
                               EnrichmentAssessment assessment,
                               String status,
                               AiCallResult aiCall,
                               ValidatedEnrichment enrichment,
                               int exampleCount,
                               int synonymCount,
                               String errorMessage) throws SQLException {
            try (Connection connection = openConnection()) {
                recordLog(connection, word, assessment, status, aiCall, enrichment, exampleCount, synonymCount, errorMessage);
            }
        }

        private void recordLog(Connection connection,
                               WordRecord word,
                               EnrichmentAssessment assessment,
                               String status,
                               AiCallResult aiCall,
                               ValidatedEnrichment enrichment,
                               int exampleCount,
                               int synonymCount,
                               String errorMessage) throws SQLException {
            String sql = """
                    INSERT INTO kyyy_word_ai_enrichment_log (
                        run_id, word_id, word_text, run_mode, status, needs_ai, reason, model,
                        prompt_hash, response_hash, example_count, synonym_count,
                        input_tokens, output_tokens, total_tokens, error_message
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, runId);
                statement.setLong(2, word.id());
                statement.setString(3, truncate(word.wordText(), MAX_WORD_LENGTH));
                statement.setString(4, args.mode());
                statement.setString(5, status);
                statement.setInt(6, assessment.needsAi() ? 1 : 0);
                statement.setString(7, truncate(assessment.reason(), 500));
                statement.setString(8, aiCall == null ? config.openAiModel() : aiCall.model());
                statement.setString(9, aiCall == null ? null : aiCall.promptHash());
                statement.setString(10, aiCall == null ? null : aiCall.responseHash());
                statement.setInt(11, exampleCount);
                statement.setInt(12, synonymCount);
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().inputTokens() == null) {
                    statement.setNull(13, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(13, aiCall.usage().inputTokens());
                }
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().outputTokens() == null) {
                    statement.setNull(14, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(14, aiCall.usage().outputTokens());
                }
                if (aiCall == null || aiCall.usage() == null || aiCall.usage().totalTokens() == null) {
                    statement.setNull(15, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(15, aiCall.usage().totalTokens());
                }
                statement.setString(16, truncate(errorMessage, 1000));
                statement.executeUpdate();
            }
        }

        private void recordFailureQuietly(WordRecord word, EnrichmentAssessment assessment, Exception error) {
            try {
                String status = error instanceof IllegalArgumentException ? "validation_failed" : "failed";
                recordLog(word, assessment, status, null, null, 0, 0, error.getMessage());
            } catch (Exception logError) {
                synchronized (System.out) {
                    System.out.println("写入失败日志失败：" + logError.getMessage());
                }
            }
        }

        private void printAssessment(WordRecord word, EnrichmentAssessment assessment) {
            System.out.printf(Locale.ROOT,
                    "word=%s id=%d needsAi=%s blocked=%s reason=%s%n",
                    word.wordText(),
                    word.id(),
                    assessment.needsAi(),
                    assessment.blocked(),
                    assessment.reason());
        }

        private void printWriteDiff(WordRecord word,
                                    EnrichmentAssessment assessment,
                                    ValidatedEnrichment enrichment,
                                    AiCallResult aiCall) {
            System.out.println("AI 结果校验通过：");
            printAssessment(word, assessment);
            System.out.println("promptHash=" + aiCall.promptHash() + ", responseHash=" + aiCall.responseHash());
            System.out.println("将写入例句：");
            for (ValidatedExample example : enrichment.examples()) {
                System.out.println("- " + example.sentence() + " / " + example.translation());
            }
            System.out.println("将写入近义词：");
            for (ValidatedSynonym synonym : enrichment.synonyms()) {
                System.out.println("- " + synonym.wordText() + " / " + synonym.meaningCn());
            }
            System.out.println();
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

        private AiCallResult generate(WordRecord word, EnrichmentAssessment assessment, List<String> retryFeedback) throws Exception {
            String systemPrompt = """
                    你是考研英语词库编辑。只基于给定单词、词性和中文释义补充学习资料。
                    不要编造中文释义，不要输出隐藏推理过程，不要解释 JSON 外的内容。
                    例句必须适合考研英语阅读或写作语境，英文自然、简洁、包含目标词本身，或同一词条的常见屈折词形变化。
                    不要把派生词、同词根但不同词条的词、近义替换词当作“词形变化”。
                    近义词必须是英文词或短语，不能与目标词相同，中文释义要短。
                    """;
            String userPrompt = buildUserPrompt(word, assessment, retryFeedback);
            String jsonSchema = buildJsonSchema();
            String promptHash = sha256(systemPrompt + "\n\n" + userPrompt + "\n\n" + jsonSchema);
            StructuredOutput output = callStructuredOutput(
                    systemPrompt,
                    userPrompt,
                    "kyyy_word_enrichment",
                    jsonSchema,
                    config.openAiMaxOutputTokens()
            );
            AiResponse aiResponse = parseAiResponse(output.outputText());
            return new AiCallResult(
                    output.model(),
                    promptHash,
                    sha256(output.outputText()),
                    output.outputText(),
                    aiResponse,
                    output.usage()
            );
        }

        private AiReviewResult review(WordRecord word, ValidatedEnrichment enrichment, String rawJson) throws Exception {
            String systemPrompt = """
                    你是考研英语词库质检员。只判断给定 AI 补齐内容是否可以写入词库。
                    审核重点是语义准确、中文翻译忠实、例句包含目标词本身或同一词条的常见屈折词形、近义词与给定中文释义相关。
                    如果本地规则未命中词形，你需要判断例句中的词是否仍属于目标词同一词条的合法屈折变化。
                    如果只是派生词、同词根但不同词条的词、或近义替换词，必须拒绝。
                    不追求文采完美；只有存在明显错误、无关内容、格式缺失或翻译不匹配时才拒绝。
                    只返回 JSON，不要输出解释性正文。
                    """;
            String userPrompt = buildReviewPrompt(word, enrichment, rawJson);
            String jsonSchema = buildReviewJsonSchema();
            StructuredOutput output = callStructuredOutput(
                    systemPrompt,
                    userPrompt,
                    "kyyy_word_review",
                    jsonSchema,
                    Math.min(config.openAiMaxOutputTokens(), 800)
            );
            return parseAiReviewResult(output.outputText());
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
            return new StructuredOutput(
                    response.model().asString(),
                    outputText,
                    usage
            );
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
            return new StructuredOutput(
                    model,
                    outputText,
                    usage
            );
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
            root.fields().forEachRemaining(field -> builder.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
            return builder.build();
        }

        private String buildUserPrompt(WordRecord word, EnrichmentAssessment assessment, List<String> retryFeedback) {
            return """
                    请为下面考研英语单词补齐学习内容。

                    目标单词：%s
                    标准化单词：%s
                    词性：%s
                    中文释义：%s
                    美式音标：%s
                    英式音标：%s
                    已有例句：%s
                    已有例句翻译：%s
                    缺口原因：%s
                    上轮退回原因：%s

                    输出要求：
                    1. examples 返回 2 到 4 条英文例句，每条必须包含目标词本身，或同一词条的常见屈折词形变化。
                       不要把派生词、同词根但不同词条的词、近义替换词当作“词形变化”。
                    2. synonyms 返回 3 到 6 个近义词，不能包含目标词本身。
                    3. 不要补写或改写中文基础释义 meaning_cn。
                    4. 只返回下面形状的 JSON，不要返回 Markdown，不要把 examples 或 synonyms 写成字符串数组：
                       {"examples":[{"sentence":"英文例句","translation":"中文翻译","focus":"目标词或词形"}],"synonyms":[{"word":"英文近义词","meaningCn":"中文短释"}],"qualityNotes":"简短质量说明"}
                    """.formatted(
                    safeText(word.wordText()),
                    safeText(word.normalizedWord()),
                    safeText(word.partOfSpeech()),
                    safeText(word.meaningCn()),
                    safeText(word.phoneticUs()),
                    safeText(word.phoneticUk()),
                    safeText(word.exampleSentence()),
                    safeText(word.exampleTranslation()),
                    safeText(assessment.reason()),
                    buildRetryFeedback(retryFeedback)
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

        private String buildReviewPrompt(WordRecord word, ValidatedEnrichment enrichment, String rawJson) {
            StringBuilder builder = new StringBuilder();
            builder.append("目标单词：").append(safeText(word.wordText())).append('\n');
            builder.append("标准化单词：").append(safeText(word.normalizedWord())).append('\n');
            builder.append("词性：").append(safeText(word.partOfSpeech())).append('\n');
            builder.append("中文释义：").append(safeText(word.meaningCn())).append('\n');
            builder.append("候选例句：\n");
            for (ValidatedExample example : enrichment.examples()) {
                builder.append("- ").append(example.sentence())
                        .append(" / ")
                        .append(example.translation())
                        .append(" / 本地词形命中=")
                        .append(example.localWordFormMatched() ? "是" : "否")
                        .append('\n');
            }
            builder.append("候选近义词：\n");
            for (ValidatedSynonym synonym : enrichment.synonyms()) {
                builder.append("- ").append(synonym.wordText()).append(" / ").append(synonym.meaningCn()).append('\n');
            }
            if (enrichment.hasLocalValidationWarnings()) {
                builder.append("本地待复核项：\n");
                for (String warning : enrichment.localValidationWarnings()) {
                    builder.append("- ").append(warning).append('\n');
                }
            }
            builder.append("原始 JSON：\n").append(truncate(rawJson, 3000)).append('\n');
            builder.append("""

                    请审核候选内容是否可以写入数据库。
                    如果本地词形命中=否，必须重点判断它是否仍然是目标词同一词条的合法屈折变化。
                    返回 JSON 形状：
                    {"approved":true,"reason":"通过或拒绝原因","retryInstruction":"如果拒绝，写出下一轮必须修正的具体要求"}
                    """);
            return builder.toString();
        }

        private String buildJsonSchema() {
            return """
                    {
                      "type": "object",
                      "additionalProperties": false,
                      "properties": {
                        "examples": {
                          "type": "array",
                          "minItems": 1,
                          "maxItems": 4,
                          "items": {
                            "type": "object",
                            "additionalProperties": false,
                            "properties": {
                              "sentence": { "type": "string" },
                              "translation": { "type": "string" },
                              "focus": { "type": "string" }
                            },
                            "required": ["sentence", "translation", "focus"]
                          }
                        },
                        "synonyms": {
                          "type": "array",
                          "minItems": 0,
                          "maxItems": 6,
                          "items": {
                            "type": "object",
                            "additionalProperties": false,
                            "properties": {
                              "word": { "type": "string" },
                              "meaningCn": { "type": "string" }
                            },
                            "required": ["word", "meaningCn"]
                          }
                        },
                        "qualityNotes": { "type": "string" }
                      },
                      "required": ["examples", "synonyms", "qualityNotes"]
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

    private static ValidatedEnrichment validateAiResult(WordRecord word, AiResponse response, boolean allowAiWordFormReview) {
        if (response == null) {
            throw new IllegalArgumentException("AI JSON 为空");
        }
        List<ValidatedExample> examples = validateExamples(word, response.examples(), allowAiWordFormReview);
        List<ValidatedSynonym> synonyms = validateSynonyms(word, response.synonyms());
        if (examples.isEmpty()) {
            throw new IllegalArgumentException("AI 未返回有效例句");
        }
        List<String> localValidationWarnings = examples.stream()
                .filter(example -> !example.localWordFormMatched())
                .map(example -> "例句未命中本地词形规则，需 AI 复核是否属于同一词条合法词形：" + truncate(example.sentence(), 160))
                .toList();
        return new ValidatedEnrichment(examples, synonyms, localValidationWarnings);
    }

    private static List<ValidatedExample> validateExamples(WordRecord word, List<AiExample> examples, boolean allowAiWordFormReview) {
        if (examples == null || examples.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<ValidatedExample> result = new ArrayList<>();
        for (AiExample item : examples) {
            String sentence = normalizeText(item == null ? null : item.sentence());
            String translation = normalizeText(item == null ? null : item.translation());
            if (hasText(sentence) && !hasText(translation)) {
                throw new IllegalArgumentException("AI 例句缺少 translation 字段：" + truncate(sentence, 160));
            }
            if (!hasText(sentence) || !hasText(translation)) {
                continue;
            }
            if (sentence.length() > MAX_SENTENCE_LENGTH || translation.length() > MAX_TRANSLATION_LENGTH) {
                throw new IllegalArgumentException("AI 例句超过字段长度限制：" + sentence);
            }
            boolean localWordFormMatched = sentenceContainsWordForm(sentence, word.normalizedWord());
            if (!localWordFormMatched && !allowAiWordFormReview) {
                throw new IllegalArgumentException("AI 例句未包含目标词或合理词形：" + sentence);
            }
            String key = sentence.toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                result.add(new ValidatedExample(sentence, translation, localWordFormMatched));
            }
            if (result.size() >= MAX_EXAMPLE_COUNT) {
                break;
            }
        }
        return result;
    }

    private static List<ValidatedSynonym> validateSynonyms(WordRecord word, List<AiSynonym> synonyms) {
        if (synonyms == null || synonyms.isEmpty()) {
            return List.of();
        }
        String target = normalizeWord(word.normalizedWord());
        Set<String> seen = new LinkedHashSet<>();
        List<ValidatedSynonym> result = new ArrayList<>();
        for (AiSynonym item : synonyms) {
            String wordText = normalizeText(item == null ? null : item.word());
            String normalized = normalizeWord(wordText);
            String meaningCn = normalizeText(item == null ? null : item.meaningCn());
            if (!hasText(wordText) || !hasText(normalized) || !hasText(meaningCn)) {
                continue;
            }
            if (Objects.equals(target, normalized)) {
                continue;
            }
            if (wordText.length() > MAX_WORD_LENGTH || meaningCn.length() > MAX_MEANING_LENGTH) {
                throw new IllegalArgumentException("AI 近义词超过字段长度限制：" + wordText);
            }
            if (seen.add(normalized)) {
                result.add(new ValidatedSynonym(wordText, normalized, meaningCn));
            }
            if (result.size() >= MAX_SYNONYM_COUNT) {
                break;
            }
        }
        return result;
    }

    private static boolean sentenceContainsWordForm(String sentence, String normalizedWord) {
        String lowerSentence = " " + sentence.toLowerCase(Locale.ROOT).replaceAll("[^a-z]+", " ") + " ";
        for (String form : wordForms(normalizedWord)) {
            if (lowerSentence.contains(" " + form + " ")) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> wordForms(String word) {
        String normalized = normalizeWord(word);
        Set<String> forms = new LinkedHashSet<>();
        if (!hasText(normalized)) {
            return forms;
        }
        forms.add(normalized);
        forms.addAll(IRREGULAR_WORD_FORMS.getOrDefault(normalized, Collections.emptySet()));
        addPluralOrThirdPersonForms(normalized, forms);
        addPastForms(normalized, forms);
        addProgressiveForms(normalized, forms);
        return forms;
    }

    private static void addPluralOrThirdPersonForms(String normalized, Set<String> forms) {
        forms.add(normalized + "s");
        if (endsWithAny(normalized, "s", "ss", "sh", "ch", "x", "z", "o")) {
            forms.add(normalized + "es");
        }
        if (endsWithConsonantY(normalized)) {
            forms.add(normalized.substring(0, normalized.length() - 1) + "ies");
        }
    }

    private static void addPastForms(String normalized, Set<String> forms) {
        forms.add(normalized + "ed");
        if (normalized.endsWith("e") && normalized.length() > 2) {
            forms.add(normalized + "d");
        }
        if (endsWithConsonantY(normalized)) {
            forms.add(normalized.substring(0, normalized.length() - 1) + "ied");
        }
        if (shouldDoubleFinalConsonant(normalized)) {
            forms.add(normalized + normalized.charAt(normalized.length() - 1) + "ed");
        }
    }

    private static void addProgressiveForms(String normalized, Set<String> forms) {
        forms.add(normalized + "ing");
        if (normalized.endsWith("ie") && normalized.length() > 2) {
            forms.add(normalized.substring(0, normalized.length() - 2) + "ying");
        } else if (normalized.endsWith("e") && !normalized.endsWith("ee") && normalized.length() > 2) {
            forms.add(normalized.substring(0, normalized.length() - 1) + "ing");
        }
        if (shouldDoubleFinalConsonant(normalized)) {
            forms.add(normalized + normalized.charAt(normalized.length() - 1) + "ing");
        }
    }

    private static boolean endsWithConsonantY(String word) {
        return word.endsWith("y") && word.length() > 1 && !isVowel(word.charAt(word.length() - 2));
    }

    private static boolean shouldDoubleFinalConsonant(String word) {
        if (word.length() < 3) {
            return false;
        }
        char last = word.charAt(word.length() - 1);
        char middle = word.charAt(word.length() - 2);
        char first = word.charAt(word.length() - 3);
        return isConsonant(last)
                && !endsWithAny(word, "w", "x", "y")
                && isVowel(middle)
                && isConsonant(first);
    }

    private static boolean isVowel(char ch) {
        return "aeiou".indexOf(Character.toLowerCase(ch)) >= 0;
    }

    private static boolean isConsonant(char ch) {
        return ch >= 'a' && ch <= 'z' && !isVowel(ch);
    }

    private static boolean endsWithAny(String text, String... suffixes) {
        return Arrays.stream(suffixes).anyMatch(text::endsWith);
    }

    private static Map<String, Set<String>> buildIrregularWordForms() {
        Map<String, Set<String>> forms = new LinkedHashMap<>();
        forms.put("awake", linkedSet("awakes", "awoke", "awoken", "awaked", "awaking"));
        forms.put("begin", linkedSet("begins", "began", "begun", "beginning"));
        return Collections.unmodifiableMap(forms);
    }

    private static Set<String> linkedSet(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static boolean isValidLegacyExample(WordRecord word) {
        String sentence = normalizeText(word.exampleSentence());
        String translation = normalizeText(word.exampleTranslation());
        if (!hasText(sentence) || !hasText(translation)) {
            return false;
        }
        if (sentence.length() < 12 || sentence.length() > MAX_SENTENCE_LENGTH) {
            return false;
        }
        if (translation.length() < 4 || translation.length() > MAX_TRANSLATION_LENGTH) {
            return false;
        }
        return sentenceContainsWordForm(sentence, word.normalizedWord());
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

    private static String normalizeWord(String value) {
        return normalizeText(value).toLowerCase(Locale.ROOT);
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

	    private static AiResponse parseAiResponse(String outputText) throws IOException {
	        JsonNode root = OBJECT_MAPPER.readTree(outputText);
	        if (!root.isObject()) {
	            throw new IllegalArgumentException("AI JSON 根节点必须是对象");
	        }
	        return new AiResponse(
	                parseAiExamples(root.path("examples")),
	                parseAiSynonyms(root.path("synonyms")),
	                firstText(root, "qualityNotes", "quality_notes", "notes")
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

	    private static List<AiExample> parseAiExamples(JsonNode examplesNode) {
	        if (!examplesNode.isArray()) {
	            return List.of();
	        }
	        List<AiExample> examples = new ArrayList<>();
	        for (JsonNode item : examplesNode) {
	            if (item.isObject()) {
	                examples.add(new AiExample(
	                        firstText(item, "sentence", "example", "text", "english", "en"),
	                        firstText(item, "translation", "translationCn", "chinese", "zh", "cn"),
	                        firstText(item, "focus", "keyword", "word")
	                ));
	            } else if (item.isTextual()) {
	                examples.add(new AiExample(item.asText(), "", ""));
	            }
	        }
	        return examples;
	    }

	    private static List<AiSynonym> parseAiSynonyms(JsonNode synonymsNode) {
	        if (!synonymsNode.isArray()) {
	            return List.of();
	        }
	        List<AiSynonym> synonyms = new ArrayList<>();
	        for (JsonNode item : synonymsNode) {
	            if (item.isObject()) {
	                synonyms.add(new AiSynonym(
	                        firstText(item, "word", "synonym", "text", "english", "en"),
	                        firstText(item, "meaningCn", "meaning", "translation", "chinese", "zh", "cn")
	                ));
	            } else if (item.isTextual()) {
	                synonyms.add(new AiSynonym(item.asText(), ""));
	            }
	        }
	        return synonyms;
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
            return new InteractiveMenu(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))).promptArgs();
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
	                        promptOptionalPositiveInteger("校验数量上限，留空表示全部"),
	                        null,
	                        false,
	                        true,
	                        false,
	                        false
	                );
                case "2" -> new CliArgs(
                        configPath,
                        "single",
	                        promptRequired("请输入要测试的单词"),
	                        null,
	                        null,
	                        false,
	                        false,
	                        false,
	                        false
	                );
                case "3" -> new CliArgs(
                        configPath,
                        "single",
	                        promptRequired("请输入要补齐并写库的单词"),
	                        null,
	                        null,
	                        true,
	                        false,
	                        promptYesNo("是否跳过后续 YES 二次确认", false),
	                        false
	                );
                case "4" -> new CliArgs(
                        configPath,
                        "all",
	                        null,
	                        promptOptionalPositiveInteger("全量执行数量上限，留空表示全部"),
	                        promptOptionalPositiveInteger("线程数，留空使用配置文件 run.threads"),
	                        true,
	                        false,
	                        false,
	                        false
	                );
                default -> throw new IllegalArgumentException("未知操作编号：" + choice);
            };
        }

        private void printMenu() {
            System.out.println("========================================");
            System.out.println("KYYY 词库 AI 补齐操作菜单");
            System.out.println("1. 只校验缺口，不调用 AI，不写库");
            System.out.println("2. 单词测试，调用 AI 并打印结果，不写库");
            System.out.println("3. 单词补齐，调用 AI 并写库");
            System.out.println("4. 全量补齐，多线程调用 AI 并写库");
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

    private record CliArgs(String configPath,
                           String mode,
	                           String word,
	                           Integer limit,
	                           Integer threads,
	                           boolean apply,
	                           boolean validateOnly,
	                           boolean yes,
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
	            return new CliArgs(DEFAULT_CONFIG_PATH, "single", null, null, null, false, false, false, true);
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
            return new CliArgs(
                    values.getOrDefault("config", DEFAULT_CONFIG_PATH),
                    mode,
	                    values.get("word"),
	                    parsePositiveInteger(values.get("limit")),
	                    parsePositiveInteger(values.get("threads")),
	                    flags.contains("apply"),
	                    flags.contains("validate-only"),
	                    flags.contains("yes"),
	                    false
	            );
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
                              int qualityMinExamples,
                              int qualityMinSynonyms,
                              int qualityAiMaxAttempts,
                              boolean qualityAiReviewEnabled,
                              boolean qualityOverwriteLegacyExample) {

        private static ToolConfig load(String configPath, boolean requireOpenAiConfig) throws IOException {
            Path path = Path.of(configPath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("配置文件不存在：" + path.toAbsolutePath()
                        + "，请在菜单中输入实际配置路径，或复制 kyyy-word-ai-enrichment.example.properties 为本地 local.properties 后填写配置。");
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
                    parseInt(properties, "openai.maxOutputTokens", 2200),
                    parseInt(properties, "openai.timeoutSeconds", 90),
                    parseInt(properties, "openai.maxRetries", 2),
                    parseInt(properties, "run.threads", 4),
                    parseInt(properties, "quality.minExamples", 2),
                    parseInt(properties, "quality.minSynonyms", 3),
                    parseInt(properties, "quality.aiMaxAttempts", 2),
                    Boolean.parseBoolean(properties.getProperty("quality.aiReviewEnabled", "false").trim()),
                    Boolean.parseBoolean(properties.getProperty("quality.overwriteLegacyExample", "false").trim())
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
            if (openAiApiKey.contains("replace_with") || openAiApiKey.contains("***") || openAiApiKey.contains("…")) {
                throw new IllegalArgumentException("openai.apiKey 仍是占位或脱敏值");
            }
            for (int index = 0; index < openAiApiKey.length(); index++) {
                char item = openAiApiKey.charAt(index);
                if (item < 0x21 || item > 0x7E) {
                    throw new IllegalArgumentException("openai.apiKey 包含非法字符");
                }
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
    }

    private record WordRecord(Long id,
                              String wordText,
                              String normalizedWord,
                              String phoneticUs,
                              String phoneticUk,
                              String partOfSpeech,
                              String meaningCn,
                              String exampleSentence,
                              String exampleTranslation) {
    }

    private record EnrichmentAssessment(boolean needsAi,
                                        boolean blocked,
                                        int exampleCount,
                                        int synonymCount,
                                        String reason) {

        private static EnrichmentAssessment complete(int exampleCount, int synonymCount) {
            return new EnrichmentAssessment(false, false, exampleCount, synonymCount, "内容已满足阈值");
        }

        private static EnrichmentAssessment needsAi(int exampleCount, int synonymCount, String reason) {
            return new EnrichmentAssessment(true, false, exampleCount, synonymCount, reason);
        }

        private static EnrichmentAssessment blocked(String reason) {
            return new EnrichmentAssessment(true, true, 0, 0, reason);
        }

        private static EnrichmentAssessment failed(String reason) {
            return new EnrichmentAssessment(true, false, 0, 0, reason);
        }
    }

    private record AiResponse(List<AiExample> examples,
                              List<AiSynonym> synonyms,
                              String qualityNotes) {
    }

    private record AiExample(String sentence,
                             String translation,
                             String focus) {
    }

    private record AiSynonym(String word,
                             String meaningCn) {
    }

    private record AiReviewResult(boolean approved,
                                  String reason,
                                  String retryInstruction) {

        private String feedback() {
            String combined = normalizeText((hasText(reason) ? reason : "") + " " + (hasText(retryInstruction) ? retryInstruction : ""));
            return hasText(combined) ? combined : "待确认";
        }
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

    private record Usage(Integer inputTokens,
                         Integer outputTokens,
                         Integer totalTokens) {
    }

    private record ValidatedEnrichment(List<ValidatedExample> examples,
                                       List<ValidatedSynonym> synonyms,
                                       List<String> localValidationWarnings) {

        private boolean hasLocalValidationWarnings() {
            return localValidationWarnings != null && !localValidationWarnings.isEmpty();
        }
    }

    private record ValidatedExample(String sentence,
                                    String translation,
                                    boolean localWordFormMatched) {
    }

    private record ValidatedSynonym(String wordText,
                                    String normalizedWord,
                                    String meaningCn) {
    }

    private record WriteSummary(int exampleCount,
                                int synonymCount) {
    }

    private record ProcessResult(WordRecord word,
                                 EnrichmentAssessment assessment,
                                 ValidatedEnrichment enrichment,
                                 AiCallResult aiCall,
                                 boolean success,
                                 String message) {

        private static ProcessResult skipped(WordRecord word, EnrichmentAssessment assessment) {
            return new ProcessResult(word, assessment, null, null, false,
                    "skipped word=" + word.wordText() + " reason=" + assessment.reason());
        }

        private static ProcessResult ready(WordRecord word,
                                           EnrichmentAssessment assessment,
                                           ValidatedEnrichment enrichment,
                                           AiCallResult aiCall) {
            return new ProcessResult(word, assessment, enrichment, aiCall, true,
                    "ready word=" + word.wordText()
                            + " examples=" + enrichment.examples().size()
                            + " synonyms=" + enrichment.synonyms().size());
        }

        private static ProcessResult failed(WordRecord word, EnrichmentAssessment assessment, String message) {
            return new ProcessResult(word, assessment, null, null, false,
                    "failed word=" + word.wordText() + " reason=" + message);
        }
    }
}
