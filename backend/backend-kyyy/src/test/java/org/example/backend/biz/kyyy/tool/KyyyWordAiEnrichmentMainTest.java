/**
 * @file KyyyWordAiEnrichmentMainTest
 * @project pipker-do
 * @module 考研英语 / 词库AI补齐测试
 * @description 验证词库 AI 补齐工具的目标词词形校验规则，防止常见词形误判或误放行。
 * @logic 1. 通过反射调用词形匹配私有方法；2. 覆盖双写辅音、部分不规则变形与派生词拒绝；3. 固定本地写库前校验边界。
 * @dependencies Tool: KyyyWordAiEnrichmentMain, JUnit Jupiter
 * @index_tags 考研英语, 词形校验, AI补齐, 单元测试
 * @author holic512
 */
package org.example.backend.biz.kyyy.tool;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KyyyWordAiEnrichmentMainTest {

    @Test
    void acceptsDoubleConsonantIngFormForBegin() throws Exception {
        assertTrue(sentenceContainsWordForm(
                "The beginning of the novel is particularly captivating.",
                "begin"
        ));
    }

    @Test
    void acceptsIrregularPastFormForAwake() throws Exception {
        assertTrue(sentenceContainsWordForm(
                "She awoke before dawn to review the passage again.",
                "awake"
        ));
    }

    @Test
    void rejectsDerivedWordFromDifferentLemmaForAwake() throws Exception {
        assertFalse(sentenceContainsWordForm(
                "The documentary awakened public awareness of the endangered species.",
                "awake"
        ));
    }

    private boolean sentenceContainsWordForm(String sentence, String normalizedWord) throws Exception {
        Method method = KyyyWordAiEnrichmentMain.class.getDeclaredMethod(
                "sentenceContainsWordForm",
                String.class,
                String.class
        );
        method.setAccessible(true);
        return (boolean) method.invoke(null, sentence, normalizedWord);
    }
}
