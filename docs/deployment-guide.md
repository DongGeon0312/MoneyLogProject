# 배포 가이드 (Docker → GitHub Actions → AWS EC2)

이 문서는 코드/설정 파일은 준비되어 있다는 전제로, AWS 계정·GitHub 계정처럼 자격증명이 필요해서
자동화 도구가 대신할 수 없는 부분을 사람이 직접 따라 할 수 있도록 정리한 절차입니다.

## 0. 준비물
- GitHub 계정 및 이 저장소의 원격 repo
- Docker Hub 계정 (이미지 저장용)
- AWS 계정 (EC2 프리티어로 충분)

## 1. 로컬에서 최종 점검
```
cd backend
# IntelliJ에서 정상 실행되는지 이미 확인했다면 생략 가능
```

## 2. Docker Hub 준비
1. https://hub.docker.com 가입/로그인
2. Account Settings → Security → New Access Token 발급 (읽기/쓰기 권한)
3. 토큰 값을 잠시 보관 (GitHub Secrets에 등록할 예정)

## 3. AWS EC2 인스턴스 생성
1. AWS 콘솔 → EC2 → 인스턴스 시작
2. AMI: Ubuntu Server 22.04 LTS, 인스턴스 유형: t2.micro (프리티어)
3. 키 페어 생성 (.pem 다운로드, 이후 SSH 접속과 GitHub Secrets에 사용)
4. 보안 그룹 인바운드 규칙 추가:
   - SSH (22) - 내 IP
   - Custom TCP (8080) - Anywhere (0.0.0.0/0) - 애플리케이션 접속용
5. 인스턴스 시작 후 퍼블릭 IP 확인

## 4. EC2에 Docker 설치
EC2에 SSH 접속 후:
```
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER
# 재접속 후 docker 명령이 sudo 없이 되는지 확인
docker --version
docker compose version
```

## 5. EC2에 배포 파일 올리기
가장 간단한 방법은 저장소를 그대로 클론하는 것입니다.
```
mkdir -p ~/moneylog
cd ~/moneylog
git clone <본인 GitHub 저장소 URL> .
```
(private 저장소라면 GitHub 개인 액세스 토큰으로 clone하거나, `docker-compose.yml`만 scp로 옮겨도 됩니다.)

## 6. GitHub 저장소 Secrets 등록
GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret

| Secret | 값 |
|--------|-----|
| DOCKERHUB_USERNAME | Docker Hub 아이디 |
| DOCKERHUB_TOKEN | 2번에서 발급한 Access Token |
| EC2_HOST | EC2 퍼블릭 IP |
| EC2_USERNAME | `ubuntu` |
| EC2_SSH_KEY | 3번에서 받은 .pem 파일의 전체 내용 |
| DB_PASSWORD | MySQL 애플리케이션 계정 비밀번호 (직접 정하기) |
| JWT_SECRET | JWT 서명용 임의의 긴 문자열 (32자 이상 권장) |

## 7. 최초 1회 수동 기동 (GitHub Actions가 아직 안 돌았을 때)
EC2에서:
```
cd ~/moneylog
export DB_PASSWORD=<6번에서 정한 값>
export JWT_SECRET=<6번에서 정한 값>
docker compose up -d --build
```
정상 기동되면 `http://<EC2_퍼블릭IP>:8080` 접속해 로그인 화면이 뜨는지 확인합니다.

## 8. 이후 자동 배포
`main` 브랜치에 push하면 `.github/workflows/ci-cd.yml`이 자동으로:
1. 빌드 + 테스트
2. Docker 이미지 빌드 후 Docker Hub에 push
3. EC2에 SSH 접속해 `docker compose pull && docker compose up -d` 실행

즉 이후로는 코드를 수정해 push만 하면 EC2에 자동 반영됩니다.

## 9. 배포 확인 체크리스트
- [ ] `http://<EC2_IP>:8080` 접속 시 로그인 화면이 보인다
- [ ] 회원가입 → 로그인 → 거래 등록/조회가 정상 동작한다
- [ ] `http://<EC2_IP>:8080/swagger-ui.html`에서 API 문서가 보인다
- [ ] GitHub Actions 탭에서 워크플로가 초록색(성공)으로 완료된다

## 10. 문제 해결 팁
- 컨테이너 로그 확인: `docker compose logs -f app`
- MySQL 연결 실패 시: `docker compose logs mysql`, 헬스체크 통과 여부 확인
- 8080 접속이 안 되면: 보안 그룹 인바운드 규칙과 EC2 인스턴스 상태(running) 확인
