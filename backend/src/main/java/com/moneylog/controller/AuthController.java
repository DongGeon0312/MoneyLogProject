package com.moneylog.controller;

import com.moneylog.common.ApiResponse;
import com.moneylog.dto.request.LoginRequest;
import com.moneylog.dto.request.RefreshTokenRequest;
import com.moneylog.dto.request.SignupRequest;
import com.moneylog.dto.response.TokenResponse;
import com.moneylog.dto.response.UserResponse;
import com.moneylog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// docs/api-spec.md 1절 "인증" + 6절 "도전 과제 API" 참고
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody @Valid SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.success("로그인에 성공했습니다.", authService.login(request));
    }

    // 도전(F-14)
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.success("토큰이 재발급되었습니다.", authService.refresh(request));
    }
}
