package com.moneylog.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// docs/api-spec.md 3절 "표준 에러 코드"와 1:1 매칭
@Getter
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "본인 데이터가 아닙니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "거래내역에서 사용 중인 카테고리는 삭제할 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 거래내역입니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
