package org.example.backend.shared.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUserResponse {

    private Long id;

    private String nickname;

    private String avatarUrl;

    private Integer gender;

    private String bio;

    private VipInfoResponse vipInfo;

    private boolean profileCompleted;
}
