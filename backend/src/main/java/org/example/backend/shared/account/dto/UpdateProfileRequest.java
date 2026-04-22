package org.example.backend.shared.account.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;

    private String avatarUrl;

    private Integer gender;

    private String bio;
}
