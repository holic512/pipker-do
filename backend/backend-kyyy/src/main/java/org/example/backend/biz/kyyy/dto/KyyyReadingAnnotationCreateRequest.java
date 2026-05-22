/**
 * @file KyyyReadingAnnotationCreateRequest
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 承接阅读正文或题干标注的新建请求参数。
 * @logic 1. 接收篇章与题目定位；2. 接收字符区间与选中文本；3. 接收高亮备注内容。
 * @dependencies API: /api/kyyy/reading/annotations
 * @index_tags 考研英语, 阅读标注, 创建请求, 高亮备注
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.Data;

@Data
public class KyyyReadingAnnotationCreateRequest {

    private Long passageId;

    private Long questionId;

    private String contentType;

    private Integer startOffset;

    private Integer endOffset;

    private String selectedText;

    private String noteContent;
}
