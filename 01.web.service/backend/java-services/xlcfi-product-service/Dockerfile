# Multi-stage build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle 설정 복사
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

# 공통 모듈 복사
COPY xlcfi-common ./xlcfi-common

# Product Service 복사
COPY xlcfi-product-service ./xlcfi-product-service

# 빌드 실행
RUN gradle :xlcfi-product-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/xlcfi-product-service/build/libs/*.jar app.jar

# JVM 옵션
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 포트 노출
EXPOSE 8082

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

