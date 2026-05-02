package org.example.backend.shared.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WechatCode2SessionResponse {

    private String openid;

    private String sessionKey;

    private String unionid;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    public boolean success() {
        return errCode == null || errCode == 0;
    }
}
