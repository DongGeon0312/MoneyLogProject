# 머니로그(MoneyLog) REST API 명세서

## 0. 설계 원칙

- URL은 자원(복수형 명사) 중심: `/api/transactions`, `/api/categories`
- 행위는 HTTP 메서드로 표현: GET(조회) / POST(생성) / PUT(수정, 전체 교체) / DELETE(삭제)
- 필터·정렬·페이징은 쿼리 파라미터로: `?yearMonth=2026-07&type=EXPENSE&page=0&size=20`

| 코드 | 의미 | 사용 상황 |
|------|------|----------|
| 200 | OK | 조회/수정 성공 |
| 201 | Created | 회원가입, 거래 등록 성공 |
| 400 | Bad Request | 검증 실패(금액 ≤ 0, 날짜 누락 등) |
| 401 | Unauthorized | 토큰 없음/만료 |
| 403 | Forbidden | 본인 데이터가 아님(인가 실패) |
| 404 | Not Found | 존재하지 않는 자원 |
| 409 | Conflict | 이메일 중복 등 |

수정은 PUT으로 통일(PATCH는 이번 범위에서 다루지 않음). 남의 데이터 접근은 403으로 처리하고, 조회 쿼리 자체를 user_id로 필터해 애초에 결과에 섞이지 않게 한다.

## 1. API 목록

### 인증 (Auth) — 인증 불필요
| 메서드 | 경로 | 설명 | 요청 바디 | 응답 |
|--------|------|------|-----------|------|
| POST | /api/auth/signup | 회원가입 | `{email, password, nickname}` | 201 · 생성된 사용자 요약 |
| POST | /api/auth/login | 로그인(JWT 발급) | `{email, password}` | 200 · `{accessToken}` |

### 카테고리 (Category) — 인증 필요
| 메서드 | 경로 | 설명 | 요청 바디 | 응답 |
|--------|------|------|-----------|------|
| GET | /api/categories | 내 카테고리 목록 | - | 200 · `[{id, name, type}]` |
| POST | /api/categories | 카테고리 추가 | `{name, type}` | 201 · 생성된 카테고리 |
| PUT | /api/categories/{id} | 카테고리 수정 | `{name, type}` | 200 · 수정된 카테고리 |
| DELETE | /api/categories/{id} | 카테고리 삭제 | - | 200 (사용 중이면 409 `CATEGORY_IN_USE`) |

`type`은 `INCOME` 또는 `EXPENSE`. 삭제 정책은 RESTRICT(docs/erd.md 참고) — 거래가 참조 중이면 삭제 차단.

### 거래내역 (Transaction) — 인증 필요
| 메서드 | 경로 | 설명 | 요청 바디 | 응답 |
|--------|------|------|-----------|------|
| GET | /api/transactions?yearMonth=&type=&categoryId=&page=&size= | 목록(필터+페이징) | - | 200 · 페이지 응답 |
| POST | /api/transactions | 거래 등록 | `{type, amount, categoryId, description, transactionDate}` | 201 · 생성된 거래 |
| GET | /api/transactions/{id} | 거래 상세 | - | 200 · 거래 1건 |
| PUT | /api/transactions/{id} | 거래 수정 | `{type, amount, categoryId, description, transactionDate}` | 200 · 수정된 거래 |
| DELETE | /api/transactions/{id} | 거래 삭제 | - | 200 |

쿼리 파라미터는 전부 선택(없으면 전체, 기본 page=0&size=20). `amount`는 long(원 단위), `transactionDate`는 `"2026-07-08"` 형식.

### 통계 (Statistics) — 인증 필요
| 메서드 | 경로 | 설명 | 응답 |
|--------|------|------|------|
| GET | /api/statistics/monthly?yearMonth= | 월별 통계 | 200 · `{income, expense, balance, byCategory:[{categoryName, total}]}` |

## 2. 요청/응답 예시

모든 성공 응답은 공통 봉투(`{success, message, data}`)로 감싼다.

**로그인 — POST /api/auth/login**
```json
// 요청
{ "email": "hong@moneylog.com", "password": "pass1234!" }

// 응답 200
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": { "accessToken": "eyJhbGciOi..." }
}
```

**거래 등록 — POST /api/transactions** (헤더: `Authorization: Bearer {accessToken}`)
```json
// 요청
{
  "type": "EXPENSE",
  "amount": 12000,
  "categoryId": 3,
  "description": "점심 - 김치찌개",
  "transactionDate": "2026-07-08"
}

// 응답 201
{
  "success": true,
  "message": "거래내역이 등록되었습니다.",
  "data": {
    "id": 42, "type": "EXPENSE", "amount": 12000,
    "categoryId": 3, "categoryName": "식비",
    "description": "점심 - 김치찌개", "transactionDate": "2026-07-08",
    "createdAt": "2026-07-08T12:31:05"
  }
}
```

**거래 목록 — GET /api/transactions?yearMonth=2026-07&page=0&size=20**
```json
{
  "success": true,
  "message": "거래내역 목록을 조회했습니다.",
  "data": {
    "transactions": [
      { "id": 42, "type": "EXPENSE", "amount": 12000, "categoryId": 3, "categoryName": "식비", "description": "점심 - 김치찌개", "transactionDate": "2026-07-08" }
    ]
  },
  "meta": {
    "pagination": { "page": 0, "size": 20, "totalItems": 42, "totalPages": 3, "hasNext": true, "hasPrev": false }
  }
}
```

**월별 통계 — GET /api/statistics/monthly?yearMonth=2026-07**
```json
{
  "success": true,
  "message": "월별 통계를 조회했습니다.",
  "data": {
    "income": 2500000, "expense": 830000, "balance": 1670000,
    "byCategory": [
      { "categoryName": "식비", "total": 420000 },
      { "categoryName": "교통", "total": 180000 }
    ]
  }
}
```
`balance = income - expense`는 서버에서 계산해 내려준다(프론트에서 재계산하지 않음).

## 3. 공통 응답 규약

**인증 헤더**: 로그인 이후 모든 요청에 `Authorization: Bearer {accessToken}` 포함. 헤더 없음/만료 → 401, 토큰은 유효하나 남의 자원 접근 → 403.

**성공 봉투**
```json
{ "success": true, "message": "...", "data": { } }
```
목록 조회는 `data` 옆에 `meta.pagination`(page/size/totalItems/totalPages/hasNext/hasPrev) 추가.

**에러 봉투**
```json
{ "success": false, "code": "TRANSACTION_NOT_FOUND", "message": "거래내역을 찾을 수 없습니다.", "data": null }
```

**표준 에러 코드**
| HTTP 상태 | code | 상황 |
|-----------|------|------|
| 400 | VALIDATION_ERROR | 입력 검증 실패 |
| 401 | INVALID_CREDENTIALS | 로그인 시 이메일/비밀번호 불일치 |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |
| 403 | FORBIDDEN | 본인 데이터가 아님 |
| 409 | DUPLICATE_EMAIL | 이미 가입된 이메일 |
| 409 | CATEGORY_IN_USE | 거래가 참조 중인 카테고리 삭제 시도 (RESTRICT 정책) |
| 404 | CATEGORY_NOT_FOUND | 존재하지 않는 카테고리 |
| 404 | TRANSACTION_NOT_FOUND | 존재하지 않는 거래 |

구현은 `common/ApiResponse<T>` 제네릭 래퍼 + `exception/GlobalExceptionHandler`(`@RestControllerAdvice`)로 일원화한다. API 문서는 별도 커스텀 페이지 없이 Swagger UI(springdoc-openapi)로 노출한다.

## 4. 화면 흐름 & 화면-API 매핑

화면 4종: ① 로그인 → ② 거래 목록 → ③ 거래 등록 → ④ 월별 통계 (③은 ②에서 진입, ④는 선택적으로 이동)

| 화면 | 사용자 행동 | 호출 API |
|------|------------|---------|
| ① 로그인 | 회원가입 | POST /api/auth/signup |
| ① 로그인 | 로그인 → 토큰 저장 | POST /api/auth/login |
| ② 거래 목록 | 이번 달 목록 로드 | GET /api/transactions?yearMonth=...&page=0&size=20 |
| ② 거래 목록 | 필터용 카테고리 로드 | GET /api/categories |
| ② 거래 목록 | 항목 삭제 | DELETE /api/transactions/{id} |
| ③ 거래 등록 | 카테고리 선택지 로드 | GET /api/categories |
| ③ 거래 등록 | 저장(신규/수정) | POST /api/transactions 또는 PUT /api/transactions/{id} |
| ④ 월별 통계 | 이번 달 집계 로드 | GET /api/statistics/monthly?yearMonth=... |

**대표 시나리오**: 로그인(토큰 저장) → 거래 목록 조회 → '+ 거래 추가' → 카테고리 로드 후 저장 → 목록으로 복귀(반영 확인) → 통계 화면에서 집계 확인.

## 5. 개발 우선순위

| 순위 | 묶음 | API | 비고 |
|------|------|-----|------|
| 1 | 인증 | signup, login | 토큰이 있어야 이후 API 테스트 가능 |
| 2 | 카테고리 | GET/POST/PUT/DELETE /categories | 거래가 카테고리를 참조하므로 먼저 |
| 3 | 거래 CRUD | GET/POST/PUT/DELETE /transactions | 앱의 핵심 데이터 |
| 4 | 목록 필터/페이징 | GET /transactions?... | CRUD 이후 조회 조건 확장 |
| 5 | 통계 | GET /statistics/monthly | 거래 데이터가 쌓인 뒤 의미 있음 |
| 6 | 프론트 연동 | 화면 4종 | 위 API 준비 후 |

## 6. 도전 과제 API (기본 완료 후 진행)

기본 과제(위 1~5)와 EC2 배포가 끝난 뒤에만 착수한다.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/auth/refresh | Refresh Token으로 accessToken 재발급 |
| GET | /api/transactions/search?keyword=&from=&to=&minAmount=&maxAmount= | 거래 검색(키워드/기간/금액범위) |
| GET | /api/transactions/export?yearMonth= | CSV 내보내기 (text/csv) |

## 7. 체크리스트
- [x] REST 원칙(자원 URL·메서드·상태코드) 정리
- [x] 인증/카테고리/거래/통계 4영역 엔드포인트 표 작성, 인증 필요 여부 표시
- [x] 로그인·거래 등록·거래 목록·월별 통계 요청/응답 예시(공통 봉투 형태) 작성
- [x] meta.pagination 포함
- [x] 에러 봉투 + 표준 에러 코드 정리
- [x] 화면 4종 흐름도 + 화면-API 매핑 표 작성
- [x] 개발 우선순위(인증→카테고리→거래→필터→통계→프론트) 정리
- [x] 도전 API는 별도 절로 분리, 기본 완료 후 진행 명시
