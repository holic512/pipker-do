package org.example.backend.biz.kyyy.service;

import org.example.backend.biz.kyyy.dto.KyyyHomeWordSearchResponse;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @file KyyyHomeWordSearchServiceTest
 * @project pipker-do
 * @module 考研英语 / 首页查词测试
 * @description 验证首页查词服务的关键词处理、排序优先级与数量裁剪。
 * @logic 1. 模拟 Mapper 查询结果；2. 覆盖空关键词、精确优先、去重与结果上限；3. 校验响应字段。
 * @dependencies Service: KyyyHomeWordSearchService, Mapper: KyyyWordMapper
 * @index_tags 考研英语, 首页查词, 单元测试, Mockito
 * @author holic512
 */
@ExtendWith(MockitoExtension.class)
class KyyyHomeWordSearchServiceTest {

    @Mock
    private KyyyWordMapper kyyyWordMapper;

    private KyyyHomeWordSearchService kyyyHomeWordSearchService;

    @BeforeEach
    void setUp() {
        kyyyHomeWordSearchService = new KyyyHomeWordSearchService(kyyyWordMapper);
    }

    @Test
    void returnsEmptyListAndSkipsMapperWhenKeywordIsBlank() {
        List<KyyyHomeWordSearchResponse> result = kyyyHomeWordSearchService.searchWords("   ");

        assertTrue(result.isEmpty());
        verify(kyyyWordMapper, never()).selectList(any());
    }

    @Test
    void keepsExactResultsBeforePrefixAndContainsResults() {
        when(kyyyWordMapper.selectList(any()))
                .thenReturn(
                        List.of(createWord(1L, "abandon")),
                        List.of(createWord(2L, "abandonment"), createWord(1L, "abandon")),
                        List.of(createWord(3L, "preabandon"), createWord(2L, "abandonment"))
                );

        List<KyyyHomeWordSearchResponse> result = kyyyHomeWordSearchService.searchWords("abandon");

        assertEquals(3, result.size());
        assertEquals("abandon", result.get(0).getWordText());
        assertEquals("abandonment", result.get(1).getWordText());
        assertEquals("preabandon", result.get(2).getWordText());
    }

    @Test
    void limitsMergedResultsToTwelveItems() {
        List<KyyyWord> containsWords = new ArrayList<>();
        for (long index = 1; index <= 18; index++) {
            containsWords.add(createWord(index, "word" + index));
        }
        when(kyyyWordMapper.selectList(any()))
                .thenReturn(List.of(), List.of(), containsWords);

        List<KyyyHomeWordSearchResponse> result = kyyyHomeWordSearchService.searchWords("word");

        assertEquals(12, result.size());
        assertEquals("word1", result.get(0).getWordText());
        assertEquals("word12", result.get(11).getWordText());
    }

    private KyyyWord createWord(Long id, String wordText) {
        KyyyWord word = new KyyyWord();
        word.setId(id);
        word.setWordText(wordText);
        word.setPhoneticUs("/" + wordText + "/");
        word.setPhoneticUk("");
        word.setPartOfSpeech("n.");
        word.setMeaningCn("释义");
        return word;
    }
}
