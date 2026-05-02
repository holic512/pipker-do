package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipCardKeyBatchVoidResponse {

    private String batchNo;

    private Integer voidedCount;

    private Integer skippedCount;
}
