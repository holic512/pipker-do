/**
 * @file KyyyReadingAnnotationUpdateRequest
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 承接阅读标注备注更新请求参数。
 * @logic 1. 仅允许修改备注内容；2. 不允许改动原始选区；3. 为前端详情弹层保存备注服务。
 * @dependencies API: /api/kyyy/reading/annotations/{annotationId}
 * @index_tags 考研英语, 阅读标注, 更新请求, 备注修改
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.Data;

@Data
public class KyyyReadingAnnotationUpdateRequest {

    private String noteContent;
}
