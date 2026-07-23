package com.moneylog.security;

import com.moneylog.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// docs/api-spec.md 3절 인증 헤더 규칙을 그대로 구현: /api/auth/**만 개방, 나머지는 JWT 필요.
// 401/403도 공통 에러 봉투 형태로 내려주기 위해 커스텀 entryPoint/accessDeniedHandler를 붙인다.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/**",
            "/h2-console/**",
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
            // 라이트 프론트(정적 리소스, 1-9): 화면 자체는 공개, API 호출만 JWT로 보호
            "/", "/index.html", "/transactions.html", "/statistics.html",
            "/css/**", "/js/**", "/favicon.ico",
            // K8s readiness/liveness probe (도전 F-16)
            "/actuator/health"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 콘솔 iframe 허용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeErrorResponse(HttpServletResponse response, int status, ErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        String json = String.format(
                "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"data\":null}",
                errorCode.name(), errorCode.getDefaultMessage());
        response.getWriter().write(json);
    }
}
