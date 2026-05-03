package org.example.backend.biz.kyyy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeDailyWordResponse;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KyyyHomeDailyWordServiceTest {

    @Mock
    private KyyyWordMapper kyyyWordMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private KyyyHomeDailyWordService kyyyHomeDailyWordService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        kyyyHomeDailyWordService = new KyyyHomeDailyWordService(
                kyyyWordMapper,
                stringRedisTemplate,
                new ObjectMapper()
        );
    }

    @Test
    void returnsCachedWordsWhenRedisContainsValidPayload() throws Exception {
        List<KyyyHomeDailyWordResponse> cachedWords = List.of(
                new KyyyHomeDailyWordResponse(1L, "hello", "int.", "你好")
        );
        when(valueOperations.get(anyString())).thenReturn(new ObjectMapper().writeValueAsString(cachedWords));

        List<KyyyHomeDailyWordResponse> result = kyyyHomeDailyWordService.getDailyWords();

        assertIterableEquals(cachedWords, result);
        verify(kyyyWordMapper, never()).selectList(any());
    }

    @Test
    void queriesDatabaseAndCachesWordsWhenRedisMisses() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(kyyyWordMapper.selectList(any())).thenReturn(List.of(
                createWord(1L, "abandon"),
                createWord(2L, "benefit"),
                createWord(3L, "capture")
        ));

        List<KyyyHomeDailyWordResponse> result = kyyyHomeDailyWordService.getDailyWords();

        assertEquals(3, result.size());
        assertEquals("abandon", result.get(0).getWordText());
        assertEquals("释义", result.get(0).getMeaningCn());
        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofHours(12)));
    }

    @Test
    void fallsBackToDatabaseWhenCachePayloadIsBroken() {
        when(valueOperations.get(anyString())).thenReturn("not-json");
        when(kyyyWordMapper.selectList(any())).thenReturn(List.of(
                createWord(9L, "derive")
        ));

        List<KyyyHomeDailyWordResponse> result = kyyyHomeDailyWordService.getDailyWords();

        assertEquals(1, result.size());
        assertEquals("derive", result.get(0).getWordText());
        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofHours(12)));
    }

    @Test
    void trimsMeaningAndExampleForHomeCard() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(kyyyWordMapper.selectList(any())).thenReturn(List.of(
                createWord(
                        10L,
                        "tourist",
                        "n.",
                        "1. n. 旅行者；游客；旅行团成员",
                        "The tourist explained the plan clearly before the discussion began. The rest of the sentence should not be kept.",
                        "那位旅行者在讨论开始前清楚地说明了计划。后面的内容不需要继续保留。"
                )
        ));

        List<KyyyHomeDailyWordResponse> result = kyyyHomeDailyWordService.getDailyWords();

        assertEquals(1, result.size());
        assertEquals("旅行者", result.get(0).getMeaningCn());
    }

    private KyyyWord createWord(Long id, String wordText) {
        return createWord(id, wordText, "n.", "释义", "example", "例句");
    }

    private KyyyWord createWord(Long id,
                                String wordText,
                                String partOfSpeech,
                                String meaningCn,
                                String exampleSentence,
                                String exampleTranslation) {
        KyyyWord word = new KyyyWord();
        word.setId(id);
        word.setWordText(wordText);
        word.setPartOfSpeech(partOfSpeech);
        word.setMeaningCn(meaningCn);
        word.setExampleSentence(exampleSentence);
        word.setExampleTranslation(exampleTranslation);
        return word;
    }
}
