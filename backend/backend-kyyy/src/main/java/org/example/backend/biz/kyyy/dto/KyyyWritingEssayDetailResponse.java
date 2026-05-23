/**
 * @file KyyyWritingEssayDetailResponse
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 定义作文详情结构，承载题目、范文与中英双语内容。
 * @logic 1. 返回基础元数据；2. 返回英文题目和范文；3. 返回中文翻译与标签辅助信息。
 * @dependencies API: /api/kyyy/writing/essays/{essayId}
 * @index_tags 考研英语, 作文详情, 范文, 翻译, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyWritingEssayDetailResponse implements Serializable {

    private Long id;

    private String writingCode;

    private String sourceTitle;

    private String examDirection;

    private Integer sourceYear;

    private String essaySection;

    private String promptCategory;

    private Integer scoreValue;

    private Integer wordLimitMin;

    private Integer wordLimitMax;

    private String promptContent;

    private String promptTranslation;

    private String sampleContent;

    private String sampleTranslation;

    private List<String> knowledgeTags;

    private String sourcePath;
}
