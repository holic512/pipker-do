package org.example.backend.shared.account.dto.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminVipUserResponse {

    private Long userId;

    private String nickname;

    private String username;

    private String phone;

    private String email;

    private Integer status;

    private Boolean isVip;

    private String vipType;

    private String vipExpireAt;

    private Integer vipRecordCount;

    private String lastLoginAt;

    private String createdAt;
}
