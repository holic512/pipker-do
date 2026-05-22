/**
 * @file KyyyReadingPassageResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 返回阅读篇章基础信息与全文内容。
 * @logic 1. 输出文章来源与篇次；2. 输出当前考试方向及中文标签；3. 输出全文供整页做题展示。
 * @dependencies DTO: KyyyReadingSessionResponse
 * @index_tags 考研英语, 阅读文章, 全文展示, 会话响应
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyReadingPassageResponse implements Serializable {

    private Long id;

    private Integer sourceYear;

    private String sourceName;

    private Integer passageNo;

    private String title;

    private String passageText;

    private String examDirection;

    private String examDirectionLabel;

    private List<KyyyReadingAnnotationResponse> annotations;
}
