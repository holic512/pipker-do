package org.example.backend.shared.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.shared.account.dto.CurrentUserResponse;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private boolean isNewUser;

    private CurrentUserResponse user;
}
