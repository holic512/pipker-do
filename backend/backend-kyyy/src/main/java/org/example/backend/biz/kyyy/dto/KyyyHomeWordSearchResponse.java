package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @file KyyyHomeWordSearchResponse
 * @project pipker-do
 * @module 考研英语 / 首页查词
 * @description 承载小程序首页查词列表的单词摘要结果。
 * @logic 1. 暴露单词基础展示字段；2. 使用 wordId 对齐前端列表主键；3. 保持响应字段轻量。
 * @dependencies Entity: KyyyWord
 * @index_tags 考研英语, 首页查词, 单词搜索, 小程序响应
 * @author holic512
 */
@Data
@AllArgsConstructor
public class KyyyHomeWordSearchResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String phoneticUs;

    private String phoneticUk;

    private String partOfSpeech;

    private String meaningCn;

    private Boolean isFavorite;
}
