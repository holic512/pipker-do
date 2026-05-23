package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @file KyyyHomeWordDetailResponse
 * @project pipker-do
 * @module 考研英语 / 首页查词
 * @description 承载小程序首页查词详情页的单词释义与多例句内容。
 * @logic 1. 暴露单词基础释义字段；2. 保留旧单例句字段；3. 补充 examples 列表用于多例句展示。
 * @dependencies Entity: KyyyWord, DTO: KyyyWordExampleResponse
 * @index_tags 考研英语, 首页查词, 单词详情, 多例句
 * @author holic512
 */
@Data
@AllArgsConstructor
public class KyyyHomeWordDetailResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String phoneticUs;

    private String phoneticUk;

    private String partOfSpeech;

    private String meaningCn;

    private String exampleSentence;

    private String exampleTranslation;

    private List<KyyyWordExampleResponse> examples;

    private Boolean isFavorite;
}
