package org.example.backend.biz.kyyy.service;

import org.example.backend.biz.kyyy.dto.KyyyHomeWordDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordSearchResponse;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.entity.KyyyWordExample;
import org.example.backend.biz.kyyy.mapper.KyyyWordExampleMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @file KyyyHomeWordSearchServiceTest
 * @project pipker-do
 * @module 考研英语 / 首页查词测试
 * @description 验证首页查词服务的关键词处理、排序优先级、数量裁剪与详情查询。
 * @logic 1. 模拟 Mapper 查询结果；2. 覆盖空关键词、精确优先、去重与结果上限；3. 校验详情例句字段。
 * @dependencies Service: KyyyHomeWordSearchService, Mapper: KyyyWordMapper, Mapper: KyyyWordExampleMapper
 * @index_tags 考研英语, 首页查词, 多例句, Mockito
 * @author holic512
 */
@ExtendWith(MockitoExtension.class)
class KyyyHomeWordSearchServiceTest {

    @Mock
    private KyyyWordMapper kyyyWordMapper;

    @Mock
    private KyyyWordExampleMapper kyyyWordExampleMapper;

    private KyyyHomeWordSearchService kyyyHomeWordSearchService;

    @BeforeEach
    void setUp() {
        kyyyHomeWordSearchService = new KyyyHomeWordSearchService(kyyyWordMapper, kyyyWordExampleMapper);
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

    @Test
    void returnsNullAndSkipsMapperWhenDetailWordIdIsInvalid() {
        assertNull(kyyyHomeWordSearchService.getWordDetail(0L));

        verify(kyyyWordMapper, never()).selectOne(any());
    }

    @Test
    void returnsActiveWordDetailWithExample() {
        KyyyWord word = createWord(12L, "abandon");
        word.setExampleSentence("Never abandon your plan.");
        word.setExampleTranslation("永远不要放弃你的计划。");
        when(kyyyWordMapper.selectOne(any())).thenReturn(word);
        when(kyyyWordExampleMapper.selectList(any())).thenReturn(List.of(
                createExample(1L, "Never abandon your plan.", "永远不要放弃你的计划。"),
                createExample(2L, "They refused to abandon the project.", "他们拒绝放弃这个项目。")
        ));

        KyyyHomeWordDetailResponse result = kyyyHomeWordSearchService.getWordDetail(12L);

        assertEquals(12L, result.getWordId());
        assertEquals("abandon", result.getWordText());
        assertEquals("Never abandon your plan.", result.getExampleSentence());
        assertEquals("永远不要放弃你的计划。", result.getExampleTranslation());
        assertEquals(2, result.getExamples().size());
        assertEquals("They refused to abandon the project.", result.getExamples().get(1).getExampleSentence());
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

    private KyyyWordExample createExample(Long id, String sentence, String translation) {
        KyyyWordExample example = new KyyyWordExample();
        example.setId(id);
        example.setExampleSentence(sentence);
        example.setExampleTranslation(translation);
        return example;
    }
}
