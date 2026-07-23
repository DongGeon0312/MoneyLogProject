package com.moneylog.dto.response;

import lombok.Getter;

// docs/api-spec.md: 로그인 응답 {accessToken}
// (도전 F-14 Refresh Token 적용 시 refreshToken 필드 추가 예정)
@Getter
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
