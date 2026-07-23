package com.moneylog.service;

import com.moneylog.domain.User;
import com.moneylog.dto.request.LoginRequest;
import com.moneylog.dto.request.SignupRequest;
import com.moneylog.dto.request.RefreshTokenRequest;
import com.moneylog.dto.response.TokenResponse;
import com.moneylog.dto.response.UserResponse;
import com.moneylog.exception.CustomException;
import com.moneylog.exception.ErrorCode;
import com.moneylog.repository.UserRepository;
import com.moneylog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
        userRepository.save(user);

        // docs/erd.md 2절: 가입 시 기본 카테고리 자동 시드
        categoryService.seedDefaultCategories(user);

        return UserResponse.from(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    // 도전(F-14): Refresh Token으로 accessToken 재발급
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다.");
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
