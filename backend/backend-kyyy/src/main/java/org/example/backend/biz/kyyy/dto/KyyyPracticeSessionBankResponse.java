/**
 * @file KyyyPracticeSessionBankResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回当前学习会话关联的默认词库信息。
 * @logic 1. 暴露词库主键；2. 暴露词库编码与名称；3. 暴露副标题用于界面展示。
 * @dependencies DTO: KyyyPracticeSessionResponse
 * @index_tags 考研英语, 会话词库, 默认词库, 响应DTO
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionBankResponse implements Serializable {

    private Long id;

    private String bankCode;

    private String bankName;

    private String subtitle;
}
