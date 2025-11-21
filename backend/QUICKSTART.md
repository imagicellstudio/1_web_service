# 빠른 시작 가이드

5분 안에 XLCfi 플랫폼을 로컬에서 실행해보세요!

## 1단계: 사전 준비 (2분)

### 필수 설치
```bash
# Docker 설치 확인
docker --version
docker-compose --version

# 없다면 Docker Desktop 설치
# https://www.docker.com/products/docker-desktop
```

### 프로젝트 클론
```bash
cd XLCfi/01.web.service/backend
```

## 2단계: 환경 설정 (1분)

```bash
# 환경 변수 파일 생성
cp .env.example .env

# .env 파일은 그대로 사용 (개발용 기본값)
```

## 3단계: 시스템 시작 (2분)

### 옵션 A: 인프라만 실행 (개발 모드 - 권장)

```bash
make dev
```

이제 PostgreSQL, Redis, Kafka가 실행됩니다!

**서비스를 IDE에서 실행하세요:**

#### Java 서비스 (IntelliJ IDEA)
1. `java-services` 폴더를 IntelliJ로 열기
2. Gradle 동기화 대기
3. `AuthServiceApplication.java` 실행 → http://localhost:8081
4. `ProductServiceApplication.java` 실행 → http://localhost:8082

#### Python 서비스 (Terminal)
```bash
# Analytics Service
cd python-services/analytics-service
pip install -r requirements.txt
python app.py  # http://localhost:5001

# 새 터미널에서
# Recommendation Service
cd python-services/recommendation-service
pip install -r requirements.txt
python app.py  # http://localhost:5002
```

### 옵션 B: Docker로 전체 실행

```bash
# 모든 서비스 Docker로 실행 (빌드 시간: 약 5-10분)
make build
make up-all
```

## 4단계: 동작 확인

### 헬스 체크
```bash
# 인프라
curl http://localhost:5432  # PostgreSQL
redis-cli -h localhost ping  # Redis

# Java 서비스
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Python 서비스
curl http://localhost:5001/health
curl http://localhost:5002/health
```

### API 문서 확인
브라우저에서 열기:
- Auth API: http://localhost:8081/swagger-ui.html
- Product API: http://localhost:8082/swagger-ui.html

## 5단계: 첫 API 호출

```bash
# 회원가입 (Auth Service)
curl -X POST http://localhost:8081/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "name": "테스트 사용자",
    "role": "BUYER"
  }'

# 상품 목록 조회 (Product Service)
curl http://localhost:8082/v1/products

# 대시보드 분석 (Analytics Service)
curl http://localhost:5001/v1/analytics/dashboard
```

## 종료 및 정리

```bash
# 서비스 중지
make down

# 모든 데이터 삭제 (주의!)
make clean
```

## 문제 해결

### "port already in use" 에러
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8081  # macOS/Linux
netstat -ano | findstr :8081  # Windows

# 프로세스 종료 후 재시작
```

### Docker 메모리 부족
```bash
# Docker Desktop 설정에서 메모리 할당 증가
# 권장: 최소 4GB, 이상적 8GB
```

### Kafka 연결 실패
```bash
# Kafka가 완전히 시작될 때까지 30초 대기
docker-compose logs -f kafka
# "started" 메시지 확인 후 서비스 재시작
```

## 다음 단계

1. **데이터베이스 스키마 생성** - Flyway 마이그레이션 작성
2. **API 구현 시작** - 각 서비스의 컨트롤러 작성
3. **테스트 작성** - 단위 테스트 및 통합 테스트

자세한 내용은 [README.md](README.md)를 참고하세요!

## 도움이 필요하신가요?

- 📚 [전체 문서](README.md)
- 🏗️ [아키텍처 문서](../004.architecture/)
- 💬 문의: support@xlcfi.com

