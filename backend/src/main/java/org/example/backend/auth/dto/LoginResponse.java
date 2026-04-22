package org.example.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.user.dto.CurrentUserResponse;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private boolean isNewUser;

    private CurrentUserResponse user;
}
