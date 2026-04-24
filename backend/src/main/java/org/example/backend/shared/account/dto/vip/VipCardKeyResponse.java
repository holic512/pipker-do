package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipCardKeyResponse {

    private Long id;

    private Long groupId;

    private String groupName;

    private String vipType;

    private Integer durationDays;

    private String cardKey;

    private String batchNo;

    private Integer status;

    private Long redeemedUserId;

    private String redeemedUserName;

    private String redeemedAt;

    private String voidedAt;

    private String voidReason;

    private Long createdBy;

    private String createdByName;

    private String createdAt;

    private String updatedAt;
}
