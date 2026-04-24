package org.example.backend.shared.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipInfoResponse {

    @JsonProperty("isVip")
    private boolean vip;

    private String vipType;

    private String expireAt;
}
