package org.example.backend.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;

    private String avatarUrl;

    private Integer gender;

    private String bio;
}
