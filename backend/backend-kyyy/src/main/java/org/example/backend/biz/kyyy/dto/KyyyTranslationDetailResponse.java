/**
 * @file KyyyTranslationDetailResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译知识库详情结构，承载整题原文、说明、译文与分段数据。
 * @logic 1. 返回基础元数据；2. 返回整题说明、原文与全文译文；3. 返回分段列表、来源与标签。
 * @dependencies API: /api/kyyy/translation/passages/{passageId}
 * @index_tags 考研英语, 翻译详情, 知识库, 真题, 分段
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyTranslationDetailResponse implements Serializable {

    private Long id;

    private String translationCode;

    private String sourceTitle;

    private String examDirection;

    private Integer sourceYear;

    private String translationMode;

    private Integer scoreValue;

    private Integer segmentCount;

    private String promptInstruction;

    private String promptContent;

    private String promptTranslation;

    private String referenceTranslation;

    private String referenceNote;

    private List<KyyyTranslationSegmentResponse> segments;

    private List<String> knowledgeTags;

    private String sourcePath;

    private String sourcePromptRef;

    private String sourceAnswerRef;
}
