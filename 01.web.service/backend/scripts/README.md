# Database Scripts

이 디렉토리에는 데이터베이스 초기화 및 관리를 위한 스크립트가 포함되어 있습니다.

## 스크립트 목록

### 1. init-database.sql
데이터베이스와 사용자를 생성하고 권한을 부여하는 초기화 스크립트입니다.

**사용법:**
```bash
# PostgreSQL superuser로 실행
psql -U postgres -f init-database.sql
```

**주요 작업:**
- `xlcfi_db` 데이터베이스 생성
- `xlcfi_user` 사용자 생성
- 필요한 권한 부여
- 필수 PostgreSQL 확장 기능 활성화 (uuid-ossp, pgcrypto)

### 2. seed-data.sql
개발/테스트를 위한 초기 데이터를 삽입하는 스크립트입니다.

**사용법:**
```bash
# Flyway 마이그레이션 실행 후
psql -U xlcfi_user -d xlcfi_db -f seed-data.sql
```

**포함된 데이터:**
- 테스트 사용자 (Admin, Seller, Buyer)
- 샘플 상품 (고추장, 김치, 된장, 고춧가루, 잡채)
- 샘플 리뷰

**테스트 계정:**
| 이메일 | 역할 | 비밀번호 |
|--------|------|----------|
| admin@xlcfi.com | ADMIN | password123 |
| seller1@xlcfi.com | SELLER | password123 |
| seller2@xlcfi.com | SELLER | password123 |
| buyer1@xlcfi.com | BUYER | password123 |
| buyer2@xlcfi.com | BUYER | password123 |

### 3. reset-database.sh (Linux/Mac)
데이터베이스를 완전히 초기화하는 스크립트입니다. **주의: 모든 데이터가 삭제됩니다!**

**사용법:**
```bash
chmod +x reset-database.sh
./reset-database.sh
```

### 4. reset-database.bat (Windows)
Windows용 데이터베이스 초기화 스크립트입니다.

**사용법:**
```cmd
reset-database.bat
```

## 환경 변수

스크립트는 다음 환경 변수를 사용합니다 (선택사항):

```bash
# PostgreSQL 연결 설정
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=xlcfi_db
export DB_USER=xlcfi_user
export POSTGRES_USER=postgres
```

## 데이터베이스 설정 전체 순서

### 첫 설치 시:

```bash
# 1. 데이터베이스 생성
psql -U postgres -f scripts/init-database.sql

# 2. Flyway 마이그레이션 실행
./gradlew flywayMigrate

# 3. 테스트 데이터 삽입
psql -U xlcfi_user -d xlcfi_db -f scripts/seed-data.sql
```

### 데이터베이스 초기화 (개발 중):

```bash
# Linux/Mac
cd scripts
./reset-database.sh

# Windows
cd scripts
reset-database.bat

# 그 다음 Flyway 마이그레이션 및 시드 데이터 삽입
```

## Docker Compose 사용 시

Docker Compose를 사용하는 경우, 데이터베이스는 자동으로 생성됩니다.

```bash
# 1. Docker Compose로 서비스 시작
docker-compose up -d postgres

# 2. 데이터베이스 초기화 (컨테이너 내부)
docker-compose exec postgres psql -U postgres -f /scripts/init-database.sql

# 3. Flyway 마이그레이션은 각 서비스 시작 시 자동 실행됨

# 4. 시드 데이터 삽입 (선택사항)
docker-compose exec postgres psql -U xlcfi_user -d xlcfi_db -f /scripts/seed-data.sql
```

## 주의사항

1. **reset-database 스크립트는 모든 데이터를 삭제합니다!** 프로덕션 환경에서는 절대 사용하지 마세요.
2. seed-data.sql은 개발/테스트용입니다. 프로덕션 환경에서는 사용하지 마세요.
3. 테스트 계정의 비밀번호는 반드시 변경하세요.
4. 프로덕션 환경에서는 강력한 비밀번호를 사용하고 환경 변수로 관리하세요.

## 문제 해결

### "database does not exist" 오류
```bash
# init-database.sql을 먼저 실행하세요
psql -U postgres -f scripts/init-database.sql
```

### "permission denied" 오류
```bash
# Linux/Mac에서 실행 권한 부여
chmod +x scripts/*.sh
```

### Flyway 마이그레이션 실패
```bash
# Flyway 정보 확인
./gradlew flywayInfo

# 마이그레이션 재시도
./gradlew flywayMigrate

# 문제가 계속되면 초기화
./gradlew flywayClean flywayMigrate
```

