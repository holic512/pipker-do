package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipCardGroupResponse {

    private Long id;

    private String groupName;

    private String vipType;

    private Integer durationDays;

    private Integer status;

    private String remark;

    private Long createdBy;

    private String createdByName;

    private Integer totalKeyCount;

    private Integer unusedKeyCount;

    private Integer redeemedKeyCount;

    private Integer voidedKeyCount;

    private String createdAt;

    private String updatedAt;
}
