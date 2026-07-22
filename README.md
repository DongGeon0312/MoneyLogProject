# MoneyLog (머니로그)

개인 가계부 웹 서비스 — 자바/스프링/시큐리티/프론트/DevOps 전 과정을 복습하는 캡스톤 프로젝트.

## 진행 상태
- [x] 1-1 프로젝트 개요
- [x] 1-2 요구사항 정의 → `docs/requirements.md`
- [x] 1-3 ERD 설계 → `docs/erd.md`
- [ ] 1-4 REST API 명세 → `docs/api-spec.md` (예정)
- [ ] 1-5 진행 계획
- [ ] 1-6~1-10 백엔드 구현 / 인증 / 프론트 연동 / 배포
- [ ] 1-11 도전 과제
- [ ] 1-12 마무리

## 폴더 구조
```
MoneyLogProject/
├── docs/           # 요구사항, ERD, API 명세 등 설계 문서
├── backend/        # Spring Boot (Java 17, Gradle)
│   └── src/main/java/com/moneylog/
│       ├── domain/       # 엔티티
│       ├── repository/   # JPA Repository
│       ├── service/      # 비즈니스 로직
│       ├── controller/   # REST API
│       ├── dto/          # 요청/응답 DTO
│       ├── security/     # Spring Security + JWT
│       ├── exception/    # 전역 예외 처리
│       └── common/       # 공통 응답 봉투 등
└── frontend/       # 정적 HTML/JS 라이트 프론트 (로그인/목록+등록/통계)
```

현재 backend/frontend 하위 파일은 전부 껍데기(스켈레톤) 상태이며, 실제 로직은 각 파일의 `TODO` 주석에 표시된 단계에서 채워집니다.

## 실행 (준비 중)
로컬 실행 방법은 1-6 백엔드 셋업 완료 후 이 섹션에 채웁니다.
