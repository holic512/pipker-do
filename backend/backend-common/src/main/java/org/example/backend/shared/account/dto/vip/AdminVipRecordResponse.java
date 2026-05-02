package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminVipRecordResponse {

    private Long id;

    private Long userId;

    private String vipType;

    private Integer vipStatus;

    private String sourceType;

    private Long sourceRefId;

    private BigDecimal amount;

    private String startTime;

    private String endTime;

    private String invalidReason;

    private String invalidAt;

    private Long invalidBy;

    private String createdAt;

    private String updatedAt;
}
