package org.example.backend.shared.account.dto.vip;

import lombok.Data;

@Data
public class VipCardGroupUpsertRequest {

    private String groupName;

    private String vipType;

    private Integer durationDays;

    private Integer status;

    private String remark;
}
