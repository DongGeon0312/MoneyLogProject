package com.moneylog.common;

import lombok.Getter;

// docs/api-spec.md 3절 공통 응답 봉투: {success, message, data} (+ 목록은 meta)
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String code; // 실패 시 표준 에러 코드, 성공 시 null
    private Object meta;       // 목록 조회 시 pagination 등 부가 정보

    private ApiResponse(boolean success, String message, T data, String code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data, Object meta) {
        ApiResponse<T> response = new ApiResponse<>(true, message, data, null);
        response.meta = meta;
        return response;
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, message, null, code);
    }
}
