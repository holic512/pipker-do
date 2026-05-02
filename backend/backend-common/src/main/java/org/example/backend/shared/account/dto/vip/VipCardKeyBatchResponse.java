package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipCardKeyBatchResponse {

    private String batchNo;

    private Long groupId;

    private Integer totalKeyCount;

    private Integer unusedKeyCount;

    private Integer redeemedKeyCount;

    private Integer voidedKeyCount;

    private String createdAt;
}
