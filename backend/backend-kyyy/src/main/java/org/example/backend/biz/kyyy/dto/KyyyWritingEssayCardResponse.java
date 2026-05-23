/**
 * @file KyyyWritingEssayCardResponse
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 定义作文知识库列表卡片返回结构，供小程序列表与首页推荐共用。
 * @logic 1. 返回作文基础信息；2. 返回字数与分值摘要；3. 返回标签数组供前端展示。
 * @dependencies API: /api/kyyy/writing/essays, API: /api/kyyy/writing/overview
 * @index_tags 考研英语, 作文卡片, 列表, 真题, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyWritingEssayCardResponse implements Serializable {

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

    private List<String> knowledgeTags;
}
