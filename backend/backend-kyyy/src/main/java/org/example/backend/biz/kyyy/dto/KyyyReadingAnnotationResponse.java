/**
 * @file KyyyReadingAnnotationResponse
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 返回阅读正文或题干标注的区间与备注信息。
 * @logic 1. 输出内容类型与区间；2. 输出选中文本快照；3. 输出备注内容供前端渲染和编辑。
 * @dependencies DTO: KyyyReadingPassageResponse, DTO: KyyyReadingQuestionResponse
 * @index_tags 考研英语, 阅读标注响应, 高亮备注, 区间信息
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyReadingAnnotationResponse implements Serializable {

    private Long id;

    private String contentType;

    private Integer startOffset;

    private Integer endOffset;

    private String selectedText;

    private String noteContent;
}
