# Copilot 사용 가이드 (프로젝트 특화)

이 문서는 AI 코딩 에이전트가 이 코드베이스에서 즉시 생산적으로 일할 수 있도록, 핵심 아키텍처, 개발/빌드 워크플로우, 그리고 프로젝트별 규약을 요약합니다.

**1. 프로젝트 전반 개요**
- **모노리포/멀티모듈:** `backend/java-services`는 Gradle Kotlin DSL 멀티모듈 프로젝트입니다. 공통 모듈은 `xlcfi-common` 하위에 존재합니다.
- **언어/프레임워크:** Java(Spring Boot 3.x, Java 17) 기반 마이크로서비스 + 일부 Python(Flask) 서비스가 공존합니다.
- **주요 인프라:** PostgreSQL, Redis, Kafka, Elasticsearch가 `backend/docker-compose.yml`에 정의되어 있으며 로컬 통합 테스트 및 개발 환경에 사용됩니다.

**2. 핵심 위치 (바로 참조할 파일/디렉터리)**
- **서비스 코드:** `backend/java-services/*` (예: `xlcfi-product-service`, `xlcfi-auth-service`)
- **공통 모듈:** `backend/java-services/xlcfi-common/*` (공통 DTO/유틸/보안)
- **도커 구성:** `backend/docker-compose.yml` (서비스 간 네트워크/환경 변수 예시)
- **아키텍처 문서:** `004.architecture/` 및 `01.web.service/004.architecture/README.md`
- **루트 설명:** `README.md` 및 각 서브디렉터리 `README.md`들

**3. 빌드·실행·테스트 워크플로우 (구체 명령)**
- **권장 JDK:** Java 17 (root `build.gradle.kts`에서 `sourceCompatibility = JavaVersion.VERSION_17` 확인됨).
- **전체 Java 빌드:** (프로젝트 루트에서) `./gradlew build` 혹은 Windows에서 `gradlew.bat build`. wrapper가 없으면 시스템 Gradle 사용 `gradle build`.

```powershell
# 전체 빌드 (PowerShell)
# 루트가 `backend/java-services`인 경우
cd backend\java-services
.\gradlew.bat build   # 또는 'gradle build'
```

- **도커 통합 환경 띄우기:** `backend/docker-compose.yml`은 프로필로 Java/Python 서비스를 분리합니다.

```powershell
# Java 서비스만 띄우기
docker compose --profile java-services up --build
# 전체(Java + Python) 띄우기
docker compose up --build
```

- **테스트 주의사항:** 통합 테스트는 Testcontainers를 사용(예: `testImplementation("org.testcontainers:postgresql")`)하므로 Docker가 필요합니다. 로컬에서 Testcontainers 통합 테스트를 실행할 때 Docker 엔진가동을 확인하세요.

**4. 프로젝트 규약·패턴(구체적)**
- **공통 모듈 참조:** 서비스 모듈들은 `implementation(project(":xlcfi-common:common-core"))` 방식으로 내부 모듈을 참조합니다 — 모듈 경로 변경에 유의하세요.
- **Lombok 사용:** `lombok`가 `compileOnly` 및 `annotationProcessor`로 설정되어 있습니다. 코드 생성/빌드 시 Lombok이 활성화된 IDE 설정 권장.
- **QueryDSL:** `annotationProcessor("com.querydsl:querydsl-apt...")` 사용 — 빌드 시 Q-타입 생성이 발생합니다.
- **환경변수 관례:** `docker-compose.yml`과 서비스 `application-*.yml`에서 환경변수를 사용해 DB/Redis/Kafka 연결을 설정합니다. 로컬에선 `docker-compose` 서비스명을 호스트로 사용합니다(예: `jdbc:postgresql://postgres:5432/xlcfi_dev`).

**5. 통합점 / 외부 의존성**
- **데이터베이스:** PostgreSQL(이미지: `postgres:15-alpine`) — 초기화 스크립트는 `./init-db`에 위치할 수 있음.
- **메시징:** Kafka(Confluent 이미지), Zookeeper가 필요.
- **검색:** Elasticsearch 8.x (보안 비활성화로 구성됨).
- **테스트:** Testcontainers → Docker 필요.

**6. 에이전트가 유용하게 사용할 구체적 예시**
- 새로운 Spring Boot 엔드포인트 추가 후 로컬 테스트: 1) `./gradlew :xlcfi-product-service:bootRun` 또는 2) `docker compose --profile java-services up --build`로 전체 환경에서 기능 확인.
- 데이터 계층 작업 시 `xlcfi-common:common-data`와 `hypersistence-utils-hibernate-63` 설정(예: JSONB 사용)을 확인 — DDL/매핑 규약은 `common-data`를 먼저 참고.

**7. 금지/주의 항목 (프로젝트 특화)**
- `docker-compose.yml`의 민감값(예: `JWT_SECRET`, DB 비밀번호)은 실제 운영에 유출되지 않도록 주의 — 개발용 값만 로컬에서 사용하세요.
- Spring Boot 버전은 `3.2.x` 계열(루트 `build.gradle.kts` 참조)이고 `Java 17`을 가정하므로 다른 Java 버전으로 빌드하면 오류가 납니다.

**8. 빠른 탐색 링크(참고 파일)**
- `backend/java-services/build.gradle.kts` : 멀티모듈 공통 설정(호환 Java 버전, 플러그인)
- `backend/java-services/*/build.gradle.kts` : 각 서비스 의존성 확인
- `backend/docker-compose.yml` : 로컬 통합 실행 예시
- `004.architecture/` : 아키텍처 의사결정 근거

문서 내용이 충분하지 않거나 추가로 자동 생성할 내용(예: run configurations, 자주 쓰는 Gradle task 목록)을 원하시면 알려주세요. 수정·추가 반영하겠습니다.
