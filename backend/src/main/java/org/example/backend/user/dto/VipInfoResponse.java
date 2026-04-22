package org.example.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipInfoResponse {

    private boolean isVip;

    private String vipType;

    private String expireAt;
}
