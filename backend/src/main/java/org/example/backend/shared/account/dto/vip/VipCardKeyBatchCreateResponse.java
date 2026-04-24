package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VipCardKeyBatchCreateResponse {

    private Long groupId;

    private String batchNo;

    private Integer count;

    private List<VipCardKeyResponse> keys;
}
