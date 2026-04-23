package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 管理端用户题库选择列表项。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminUserItemResponse {

    private Long userId;

    private String nickname;

    private String username;

    private String phone;

    private Integer status;

    private Integer selectedBankCount;

    private LocalDateTime lastPracticeAt;

    private LocalDateTime updatedAt;
}
