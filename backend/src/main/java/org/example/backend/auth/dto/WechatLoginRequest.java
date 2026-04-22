package org.example.backend.auth.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {

    private String code;
}
