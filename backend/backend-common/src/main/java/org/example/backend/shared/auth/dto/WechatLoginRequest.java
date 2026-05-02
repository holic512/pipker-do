package org.example.backend.shared.auth.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {

    private String code;
}
