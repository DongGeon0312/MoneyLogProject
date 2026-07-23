package com.moneylog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 도전(F-14): POST /api/auth/refresh 요청 바디 {refreshToken}
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}
