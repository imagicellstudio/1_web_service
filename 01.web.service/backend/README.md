# XLCfi Platform - K-Food 거래 플랫폼

하이브리드 아키텍처 기반의 K-Food 거래 플랫폼 백엔드 시스템

## 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      하이브리드 시스템                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Java Spring Boot (메인)        Python Flask (데이터)       │
│  ├── Auth Service               ├── Analytics Service       │
│  ├── Product Service            ├── Recommendation Service  │
│  ├── Order Service              ├── ML Service              │
│  ├── Payment Service            ├── Image Service           │
│  ├── Review Service             ├── Report Service          │
│  ├── Admin Service              └── ETL Service             │
│  └── Blockchain Service                                      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 기술 스택

### Backend (Java)
- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17 LTS
- **Build Tool**: Gradle 8.5 (Kotlin DSL)
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Message Queue**: Apache Kafka
- **Search**: Elasticsearch 8

### Backend (Python)
- **Framework**: Flask 3.0
- **Language**: Python 3.11
- **Data Analysis**: Pandas, NumPy
- **ML**: Scikit-learn, TensorFlow
- **Database**: PostgreSQL (SQLAlchemy)
- **Cache**: Redis

## 프로젝트 구조

```
backend/
├── java-services/                  # Java 마이크로서비스
│   ├── xlcfi-common/              # 공통 모듈
│   │   ├── common-core/           # 공통 유틸리티
│   │   ├── common-security/       # 보안 공통
│   │   └── common-data/           # 데이터 공통
│   ├── xlcfi-auth-service/        # 인증 서비스 (8081)
│   ├── xlcfi-product-service/     # 상품 서비스 (8082)
│   ├── xlcfi-order-service/       # 주문 서비스 (8083)
│   ├── xlcfi-payment-service/     # 결제 서비스 (8084)
│   ├── xlcfi-review-service/      # 리뷰 서비스 (8085)
│   ├── xlcfi-admin-service/       # 관리자 서비스 (8086)
│   └── xlcfi-blockchain-service/  # 블록체인 서비스 (8087)
│
├── python-services/               # Python 마이크로서비스
│   ├── analytics-service/         # 분석 서비스 (5001)
│   ├── recommendation-service/    # 추천 서비스 (5002)
│   ├── ml-service/                # ML 서비스 (5003)
│   ├── image-service/             # 이미지 처리 (5004)
│   ├── report-service/            # 리포트 (5005)
│   └── etl-service/               # ETL (5006)
│
└── docker-compose.yml             # Docker Compose 설정
```

## 시작하기

### 사전 요구사항

- Docker & Docker Compose
- Java 17 (로컬 개발 시)
- Python 3.11 (로컬 개발 시)
- Make (선택사항)

### 1. 환경 설정

```bash
# 환경 변수 파일 생성
cp .env.example .env

# 필요한 값 수정
vi .env
```

### 2. 인프라 시작 (개발 모드)

```bash
# Makefile 사용
make dev

# 또는 Docker Compose 직접 사용
docker-compose up -d postgres redis zookeeper kafka elasticsearch
```

이 명령은 다음 서비스를 시작합니다:
- PostgreSQL (5432)
- Redis (6379)
- Kafka (9092)
- Zookeeper (2181)
- Elasticsearch (9200)

### 3. 서비스 실행

#### 옵션 A: Docker로 전체 실행

```bash
# 모든 서비스 시작
make up-all

# 또는 Java/Python 서비스만 선택적으로
make up-java
make up-python
```

#### 옵션 B: 로컬 개발 (권장)

인프라만 Docker로 실행하고, 서비스는 IDE에서 실행:

```bash
# 1. 인프라 시작
make dev

# 2. Java 서비스 실행 (IntelliJ IDEA)
# - xlcfi-auth-service 실행 → http://localhost:8081
# - xlcfi-product-service 실행 → http://localhost:8082

# 3. Python 서비스 실행 (Terminal)
cd python-services/analytics-service
python app.py  # http://localhost:5001

cd ../recommendation-service
python app.py  # http://localhost:5002
```

### 4. 헬스 체크

```bash
# Java 서비스
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Python 서비스
curl http://localhost:5001/health
curl http://localhost:5002/health
```

## API 문서

각 서비스가 실행되면 Swagger UI를 통해 API 문서를 확인할 수 있습니다:

- Auth Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html
- Analytics Service: http://localhost:5001/api-docs

## 개발 가이드

### Java 서비스 개발

```bash
# 빌드
cd java-services
./gradlew build

# 특정 서비스만 빌드
./gradlew :xlcfi-product-service:build

# 테스트
./gradlew test

# 특정 서비스 실행
./gradlew :xlcfi-product-service:bootRun
```

### Python 서비스 개발

```bash
# 가상환경 생성
cd python-services/analytics-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 개발 서버 실행
python app.py

# 또는 Gunicorn으로 실행
gunicorn --bind 0.0.0.0:5001 --workers 4 app:app
```

### 데이터베이스 마이그레이션

```bash
# Java (Flyway)
# - src/main/resources/db/migration/ 에 SQL 파일 작성
# - V1__init_schema.sql 형식
# - 애플리케이션 시작 시 자동 실행

# Python (Alembic - 향후 추가 예정)
```

## 테스트

```bash
# Java 테스트
cd java-services
./gradlew test

# Python 테스트
cd python-services/analytics-service
pytest

# 통합 테스트
make test
```

## Docker 명령어

```bash
# 전체 서비스 시작
make up-all

# 서비스 중지
make down

# 로그 확인
make logs

# 특정 서비스 로그
docker-compose logs -f auth-service
docker-compose logs -f analytics-service

# 실행 중인 서비스 확인
make ps

# 컨테이너 및 볼륨 삭제
make clean
```

## 포트 목록

### Java Services
- 8081: Auth Service
- 8082: Product Service
- 8083: Order Service
- 8084: Payment Service
- 8085: Review Service
- 8086: Admin Service
- 8087: Blockchain Service

### Python Services
- 5001: Analytics Service
- 5002: Recommendation Service
- 5003: ML Service
- 5004: Image Service
- 5005: Report Service
- 5006: ETL Service

### Infrastructure
- 5432: PostgreSQL
- 6379: Redis
- 9092: Kafka
- 2181: Zookeeper
- 9200: Elasticsearch

## 트러블슈팅

### PostgreSQL 연결 실패
```bash
# PostgreSQL 상태 확인
docker-compose ps postgres

# 로그 확인
docker-compose logs postgres

# 재시작
docker-compose restart postgres
```

### Kafka 연결 실패
```bash
# Kafka가 완전히 시작될 때까지 대기 (약 30초)
docker-compose logs -f kafka

# Kafka가 "started" 메시지를 출력하면 준비 완료
```

### Port already in use
```bash
# 사용 중인 포트 확인 (macOS/Linux)
lsof -i :8081

# 프로세스 종료
kill -9 <PID>

# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

## 성능 최적화

### JVM 옵션 조정
```bash
# docker-compose.yml 또는 Dockerfile에서
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### Redis 메모리 설정
```bash
# docker-compose.yml에서
command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

## 모니터링

### 메트릭 확인
```bash
# Java Actuator
curl http://localhost:8081/actuator/metrics

# Prometheus 엔드포인트
curl http://localhost:8081/actuator/prometheus
```

## 다음 단계

1. **데이터베이스 스키마 생성**: Flyway 마이그레이션 파일 작성
2. **JPA Entity 구현**: 데이터베이스 테이블을 Java Entity로 변환
3. **API 구현**: 각 서비스의 REST API 엔드포인트 구현
4. **Kafka 이벤트 설정**: 서비스 간 비동기 통신 구현
5. **인증/권한 구현**: JWT 기반 인증 시스템 구축

## 참고 문서

- [하이브리드 아키텍처 설계서](../004.design/10_hybrid_architecture_design.md)
- [Java API 명세서](../004.design/11_java_api_specs_detailed.md)
- [Python API 명세서](../004.design/12_python_api_specs_detailed.md)
- [서비스 간 통신 시퀀스](../004.design/13_service_communication_sequences.md)

## 라이선스

Proprietary - XLCfi Platform

## 문의

기술 지원: support@xlcfi.com

