package org.example.backend.shared.account.dto.vip;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VipManualGrantRequest {

    private String vipType;

    private Integer durationDays;

    private BigDecimal amount;

    private String remark;
}
