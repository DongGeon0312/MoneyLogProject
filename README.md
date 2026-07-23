# MoneyLog (머니로그)

개인 가계부 웹 서비스 — 자바/스프링/시큐리티/프론트/DevOps 전 과정을 복습하는 캡스톤 프로젝트.
로그인한 사용자가 수입/지출을 기록하고, 카테고리별·월별로 돈의 흐름을 확인합니다. 핵심 원칙은
**"내 데이터는 나만 접근한다"**(인가) 입니다.

## 진행 상태
- [x] 1-1 프로젝트 개요
- [x] 1-2 요구사항 정의 → `docs/requirements.md`
- [x] 1-3 ERD 설계 → `docs/erd.md`
- [x] 1-4 REST API 명세 → `docs/api-spec.md`
- [x] 1-5 진행 계획 → `docs/progress-plan.md`
- [x] 1-6 백엔드 셋업 (JPA 엔티티/Repository)
- [x] 1-7 가계부 핵심 기능 (카테고리/거래 CRUD)
- [x] 1-8 인증/보안 (Spring Security + JWT, 검증, 통계)
- [x] 1-9 프론트엔드 라이트 연동 (정적 HTML/JS)
- [x] 1-10 DevOps 코드/설정 완료 (Docker, docker-compose, GitHub Actions) — **실제 AWS 배포 실행은 미완료, `docs/deployment-guide.md` 따라 진행 필요**
- [x] 1-11 도전 과제 (Refresh Token, 검색/CSV, 통계 시각화, 테스트, Jenkins/K8s 매니페스트)
- [x] 1-12 마무리 (이 문서 + `docs/retrospective.md`)

## 기본 기능
- 회원가입 / 로그인 (JWT 발급, BCrypt)
- 카테고리 기본 시드(식비/교통/주거/문화/급여/용돈) + 추가·수정·삭제 (사용 중인 카테고리는 삭제 제한)
- 거래내역(수입/지출) CRUD + 월별·타입별·카테고리별 필터 + 페이징
- 본인 데이터 인가(JWT 기반), 입력 검증, 공통 에러 응답
- 월별 통계(총수입/총지출/잔액 + 카테고리별 지출)
- 로그인/거래목록+등록/통계 화면 (정적 HTML/JS)
- Swagger UI API 문서

## 도전 기능
- Refresh Token 재발급 (`POST /api/auth/refresh`)
- 거래 검색(키워드/기간/금액범위) + CSV 내보내기
- 통계 시각화 (Chart.js 파이차트)
- 단위 테스트 (CategoryService, TransactionService) + 컨텍스트 스모크 테스트
- Jenkinsfile + Kubernetes 매니페스트(`k8s/`, 무중단 롤링 업데이트 설정 포함)

## 폴더 구조
```
MoneyLogProject/
├── docs/                      # 요구사항, ERD, API 명세, 진행계획, 배포 가이드, 회고
├── .github/workflows/         # GitHub Actions CI/CD
├── k8s/                       # 도전: Kubernetes 매니페스트
├── Jenkinsfile                # 도전: Jenkins 파이프라인
├── docker-compose.yml         # app + MySQL
└── backend/                   # Spring Boot (Java 17, Gradle)
    ├── Dockerfile
    └── src/main/
        ├── java/com/moneylog/
        │   ├── domain/           # 엔티티 (User, Category, Transaction)
        │   ├── repository/       # JPA Repository
        │   ├── service/          # 비즈니스 로직
        │   ├── controller/       # REST API
        │   ├── dto/              # 요청/응답 DTO
        │   ├── security/         # Spring Security + JWT
        │   ├── exception/        # 전역 예외 처리
        │   └── common/           # 공통 응답 봉투(ApiResponse) 등
        └── resources/
            ├── static/           # 라이트 프론트 - Spring Boot가 그대로 서빙
            ├── application.yml
            ├── application-local.yml   # H2 (로컬)
            └── application-prod.yml    # MySQL (Docker/EC2)
```

프론트를 백엔드의 `static/` 리소스로 합쳐서, 별도 프론트 서버 없이 Spring Boot 하나로 API + 화면을 함께 서빙합니다.

## 로컬 실행
1. `backend` 폴더를 IntelliJ에서 Gradle 프로젝트로 열기 (JDK 17 이상)
2. `MoneyLogApplication` 실행 (기본 프로필 `local`, H2 인메모리 DB 사용)
3. 브라우저에서 `http://localhost:8080` 접속 → 회원가입 → 로그인 → 거래 등록/조회/통계 확인
4. API 문서: `http://localhost:8080/swagger-ui.html`
5. H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:moneylog`, user: `sa`, 비밀번호 없음)

## 테스트
```
cd backend
./gradlew test    # 또는 IntelliJ에서 CategoryServiceTest / TransactionServiceTest 실행
```

## 배포
Docker/CI-CD/AWS EC2 배포 절차는 `docs/deployment-guide.md`를 따라 진행합니다. 이 저장소에는
Dockerfile, docker-compose.yml, GitHub Actions 워크플로가 모두 준비되어 있으며, AWS 계정 생성·EC2
프로비저닝·GitHub Secrets 등록처럼 자격증명이 필요한 단계만 직접 수행하면 됩니다.

## 알려진 제한사항 / 향후 개선
- `ddl-auto: update`로 운영 중 — 마이그레이션 도구(Flyway 등) 도입 시 `validate`로 전환 권장
- 프론트에서 Refresh Token 자동 재시도(무중단 세션 연장)는 미구현 — accessToken 만료 시 재로그인 필요
- 통계 화면은 이번 달 파이차트만 제공 — 월별 추이(라인차트)는 향후 확장 가능
- Jenkins/K8s 매니페스트는 실제 클러스터 없이 작성된 템플릿 — 클러스터 보유 시 이미지 경로/도메인만 교체하면 적용 가능

## 문서 목록
- `docs/requirements.md` — 요구사항 정의서
- `docs/erd.md` — 도메인 모델 / ERD
- `docs/api-spec.md` — REST API 명세
- `docs/progress-plan.md` — 일차별 진행 계획
- `docs/deployment-guide.md` — 배포 절차
- `docs/retrospective.md` — 회고
