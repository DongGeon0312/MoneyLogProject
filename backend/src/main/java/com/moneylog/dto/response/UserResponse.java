package com.moneylog.dto.response;

import com.moneylog.domain.User;
import lombok.Getter;

// docs/api-spec.md: POST /api/auth/signup 응답 (생성된 사용자 요약)
@Getter
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname;

    private UserResponse(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
