/**
 * @file KyyyPracticeSessionEmptyStateResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回无可学内容或缺少默认词库时的空态提示。
 * @logic 1. 返回标题与描述；2. 返回建议动作文本；3. 返回建议跳转模式供前端决策。
 * @dependencies DTO: KyyyPracticeSessionResponse
 * @index_tags 考研英语, 空态, 默认词库, 会话提示
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionEmptyStateResponse implements Serializable {

    private String title;

    private String description;

    private String actionText;

    private String suggestedMode;
}
